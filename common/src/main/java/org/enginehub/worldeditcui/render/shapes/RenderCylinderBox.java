package org.enginehub.worldeditcui.render.shapes;

import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.render.points.PointCube;

/**
 * Draws the top and bottom circles around a cylindrical region
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderCylinderBox extends RenderRegion
{
	private final double radX, radZ;
	private final int minY, maxY;
	private final double centreX, centreZ;
	
	public RenderCylinderBox(RenderStyle style, PointCube centre, double radX, double radZ, int minY, int maxY)
	{
		super(style);
		this.radX = radX;
		this.radZ = radZ;
		this.minY = minY;
		this.maxY = maxY;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{

		double xPos = this.centreX - ctx.cameraPos().getX();
		double zPos = this.centreZ - ctx.cameraPos().getZ();

		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}
			
			double twoPi = Math.PI * 2;
			ctx.color(line);
			for (int yBlock : new int[] { this.minY, this.maxY + 1 })
			{
				ctx.beginLineLoop();
				for (int i = 0; i <= 75; i++)
				{
					double tempTheta = i * twoPi / 75;
					double tempX = this.radX * Math.cos(tempTheta);
					double tempZ = this.radZ * Math.sin(tempTheta);

					ctx.vertex(xPos + tempX, yBlock - ctx.cameraPos().getY(), zPos + tempZ);
				}

				ctx.endLineLoop();
			}
		}
	}
}
