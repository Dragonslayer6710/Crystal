package com.crystal.engine.render.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderStateTest {

    @Test
    void defaultsToDepthTestAndCullFaceEnabled() {
        RenderState state = new RenderState();

        assertFalse(state.isWireframe());
        assertTrue(state.isCullFace());
        assertTrue(state.isDepthTest());
    }

    @Test
    void settersUpdateFlagsAndReturnStateForChaining() {
        RenderState state = new RenderState();

        assertSame(state, state.setWireframe(true));
        assertSame(state, state.setCullFace(false));
        assertSame(state, state.setDepthTest(false));

        assertTrue(state.isWireframe());
        assertFalse(state.isCullFace());
        assertFalse(state.isDepthTest());
    }

    @Test
    void sortKeyReflectsEnabledRenderStateFlags() {
        RenderState state = new RenderState()
                .setDepthTest(false)
                .setCullFace(false)
                .setWireframe(false);

        assertEquals(0, state.getSortKey());

        state.setDepthTest(true);
        assertEquals(1, state.getSortKey());

        state.setCullFace(true);
        assertEquals(3, state.getSortKey());

        state.setWireframe(true);
        assertEquals(7, state.getSortKey());
    }
}
