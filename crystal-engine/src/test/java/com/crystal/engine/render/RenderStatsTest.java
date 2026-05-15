package com.crystal.engine.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderStatsTest {

    @Test
    void resetClearsCounts() {
        RenderStats stats = new RenderStats();

        stats.setRenderableObjectCount(3);
        stats.setVisibleObjectCount(2);
        stats.setCulledObjectCount(1);
        stats.incrementSceneDrawCommandCount();
        stats.incrementSkyboxDrawCommandCount();
        stats.incrementSubmittedCommandCount();
        stats.incrementShadowDrawCount();

        stats.reset();

        assertEquals(0, stats.getRenderableObjectCount());
        assertEquals(0, stats.getVisibleObjectCount());
        assertEquals(0, stats.getCulledObjectCount());
        assertEquals(0, stats.getSceneDrawCount());
        assertEquals(0, stats.getSkyboxDrawCount());
        assertEquals(0, stats.getTotalDrawCount());
        assertEquals(0, stats.getSubmittedCommandCount());
        assertTrue(stats.summary().contains("shadowDraws=0"));
    }

    @Test
    void totalDrawCountIncludesSceneAndSkyboxDraws() {
        RenderStats stats = new RenderStats();

        stats.incrementSceneDrawCommandCount();
        stats.incrementSceneDrawCommandCount();
        stats.incrementSkyboxDrawCommandCount();

        assertEquals(3, stats.getTotalDrawCount());
    }

    @Test
    void summaryIncludesCoreCounts() {
        RenderStats stats = new RenderStats();

        stats.setRenderableObjectCount(4);
        stats.setVisibleObjectCount(3);
        stats.setCulledObjectCount(1);
        stats.incrementSceneDrawCommandCount();
        stats.incrementSkyboxDrawCommandCount();
        stats.incrementSubmittedCommandCount();
        stats.incrementShadowDrawCount();

        String summary = stats.summary();

        assertTrue(summary.contains("renderable=4"));
        assertTrue(summary.contains("visible=3"));
        assertTrue(summary.contains("culled=1"));
        assertTrue(summary.contains("draws=2"));
        assertTrue(summary.contains("sceneDraws=1"));
        assertTrue(summary.contains("skyboxDraws=1"));
        assertTrue(summary.contains("submittedCommands=1"));
        assertTrue(summary.contains("shadowDraws=1"));
    }
}
