package org.enginehub.worldeditcui.event;

import org.enginehub.worldeditcui.InitialisationFactory;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.exceptions.InitialisationException;

/**
 * @author Adam Mummery-Smith
 */
public class CUIEventDispatcher implements InitialisationFactory
{
	private WorldEditCUI controller;
	

	public CUIEventDispatcher(WorldEditCUI controller)
	{
		this.controller = controller;
	}

	@Override
	public void initialise() throws InitialisationException
	{
	}

	public void raiseEvent(CUIEventArgs eventArgs)
	{
		try
		{
			final CUIEventType type = CUIEventType.named(eventArgs.getType());
			if (type == null)
			{
				this.controller.getDebugger().debug("No such event " + eventArgs.getType());
				return;
			}
			
			CUIEvent event = type.make(eventArgs);
			event.prepare();
			
			String response = event.raise();
			if (response != null)
			{
				this.handleEventResponse(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			this.controller.getDebugger().debug("Error raising event " + eventArgs.getType() + ": " + ex.getClass().getSimpleName() + " " + ex.getMessage());
		}
	}

	private void handleEventResponse(String response)
	{
	}
}
