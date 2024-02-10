package org.enginehub.worldeditcui.event.cui;

import org.enginehub.worldeditcui.config.Colour;
import org.enginehub.worldeditcui.event.CUIEvent;
import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.event.CUIEventType;
import org.enginehub.worldeditcui.render.CustomStyle;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.render.region.Region;

/**
 * Called when style event is received
 * 
 * @author Adam Mummery-Smith
 */
public class CUIEventColour extends CUIEvent
{
	public CUIEventColour(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.COLOUR;
	}
	
	@Override
	public void prepare()
	{
		if (!this.multi)
		{
			throw new IllegalStateException("COLOUR event is not valid for non-multi selections");
		}
		
		super.prepare();
	}
	
	@Override
	public String raise()
	{
		Region selection = this.controller.getSelection(true);
		if (selection == null)
		{
			this.controller.getDebugger().debug("No active multi selection.");
			return null;
		}
		
		RenderStyle[] defaultStyles = selection.getDefaultStyles();
		RenderStyle[] styles = new RenderStyle[defaultStyles.length];

		for (int i = 0; i < defaultStyles.length; i++)
		{
			String str = this.getString(i);
			if (!str.startsWith("#"))
			{
				str = "#" + str;
			}
			styles[i] = new CustomStyle(Colour.parseRgbaOr(str, defaultStyles[i].getColour()));
			
		}
		
		selection.setStyles(styles);
		return null;
	}
}
