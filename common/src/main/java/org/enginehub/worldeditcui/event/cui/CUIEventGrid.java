package org.enginehub.worldeditcui.event.cui;

import org.enginehub.worldeditcui.event.CUIEvent;
import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.event.CUIEventType;
import org.enginehub.worldeditcui.render.RenderStyle.RenderType;
import org.enginehub.worldeditcui.render.region.Region;

/**
 * Called when grid spacing event is received
 * 
 * @author Adam Mummery-Smith
 */
public class CUIEventGrid extends CUIEvent
{
	public CUIEventGrid(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.GRID;
	}
	
	@Override
	public void prepare()
	{
		if (!this.multi)
		{
			throw new IllegalStateException("GRID event is not valid for non-multi selections");
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
		
		selection.setGridSpacing(this.getDouble(0));
		
		RenderType renderType = RenderType.ANY;
		if (this.params.length > 1 && "cull".equalsIgnoreCase(this.getString(1)))
		{
			renderType = RenderType.VISIBLE;
		}
		
		selection.setRenderType(renderType);
		return null;
	}
}
