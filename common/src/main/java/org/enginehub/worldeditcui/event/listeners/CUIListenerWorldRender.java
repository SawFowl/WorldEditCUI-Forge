package org.enginehub.worldeditcui.event.listeners;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.RenderSink;
import org.enginehub.worldeditcui.util.Vector3;
import org.lwjgl.opengl.GL32;

import java.util.List;

/**
 * Listener for WorldRenderEvent
 *
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerWorldRender
{
	private final WorldEditCUI controller;

	private final Minecraft minecraft;
	private final CUIRenderContext ctx = new CUIRenderContext();
	private final List<PipelineProvider> pipelines;
	private int currentPipelineIdx;
	private RenderSink sink;

	public CUIListenerWorldRender(final WorldEditCUI controller, final Minecraft minecraft, final List<PipelineProvider> pipelines)
	{
		this.controller = controller;
		this.minecraft = minecraft;
		this.pipelines = List.copyOf(pipelines);
	}

	private RenderSink providePipeline()
	{
		if (this.sink != null)
		{
			return this.sink;
		}

		for (int i = this.currentPipelineIdx; i < this.pipelines.size(); i++)
		{
			final PipelineProvider pipeline = this.pipelines.get(i);
			if (pipeline.available())
			{
				try
				{
					final RenderSink sink = pipeline.provide();
					this.currentPipelineIdx = i;
					return this.sink = sink;
				}
				catch (final Exception ex)
				{
					this.controller.getDebugger().info("Failed to render with pipeline " + pipeline.id() + ", which declared itself as available... trying next");
				}
			}
		}

		throw new IllegalStateException("No pipeline available to render with!");
	}

	private void invalidatePipeline() {
		if (this.currentPipelineIdx < this.pipelines.size() - 1) {
			this.currentPipelineIdx++;
			this.sink = null;
		}
	}

	public void onRender(final float partialTicks) {
		try {
			final RenderSink sink = this.providePipeline();
			if (!this.pipelines.get(this.currentPipelineIdx).shouldRender())
			{
				// allow ignoring eg. shadow pass
				return;
			}
			Minecraft.getInstance().getProfiler().push("worldeditcui");
			this.ctx.init(new Vector3(this.minecraft.gameRenderer.getMainCamera().getPosition()), partialTicks, sink);
			final float fogStart = RenderSystem.getShaderFogStart();
			FogRenderer.setupNoFog();
			final PoseStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushPose();
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			// RenderSystem.disableTexture();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.depthMask(true);
			RenderSystem.lineWidth(LineStyle.DEFAULT_WIDTH);

			final ShaderInstance oldShader = RenderSystem.getShader();
			try {
				this.controller.renderSelections(this.ctx);
				this.sink.flush();
			} catch (final Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
				this.invalidatePipeline();
			}

			RenderSystem.depthFunc(GL32.GL_LEQUAL);
			RenderSystem.setShader(() -> oldShader);
			// RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			poseStack.popPose();
			RenderSystem.setShaderFogStart(fogStart);
			Minecraft.getInstance().getProfiler().pop();
		} catch (final Exception ex)
		{
			this.controller.getDebugger().error("Failed while preparing state for WorldEdit CUI", ex);
			this.invalidatePipeline();
		}
	}
}
