package org.enginehub.worldeditcui.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public final class VanillaPipelineProvider implements PipelineProvider {

    public static class DefaultTypeFactory implements BufferBuilderRenderSink.TypeFactory {
        public static final DefaultTypeFactory INSTANCE = new DefaultTypeFactory();

        private static final BufferBuilderRenderSink.RenderType QUADS = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, GameRenderer::getPositionColorShader);
        private static final BufferBuilderRenderSink.RenderType LINES = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL, GameRenderer::getRendertypeLinesShader);
        private static final BufferBuilderRenderSink.RenderType LINES_LOOP = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL, GameRenderer::getRendertypeLinesShader);

        private DefaultTypeFactory() {}

        @Override
        public BufferBuilderRenderSink.RenderType quads() {
            return QUADS;
        }

        @Override
        public BufferBuilderRenderSink.RenderType lines() {
            return LINES;
        }

        @Override
        public BufferBuilderRenderSink.RenderType linesLoop() {
            return LINES_LOOP;
        }
    }

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public RenderSink provide() {
        return new BufferBuilderRenderSink(DefaultTypeFactory.INSTANCE);
    }
}
