package com.crystal.engine.render.mesh.primitive;

import com.crystal.engine.render.mesh.MeshData;
import com.crystal.engine.render.mesh.MeshDataBuilder;
import com.crystal.engine.render.mesh.VertexLayout;

public final class MeshPrimitives {

    private MeshPrimitives() {
    }

    public static MeshData cubePosition() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);

        int leftBottomFront  = builder.addVertex(-0.5f, -0.5f,  0.5f);
        int rightBottomFront = builder.addVertex( 0.5f, -0.5f,  0.5f);
        int rightTopFront    = builder.addVertex( 0.5f,  0.5f,  0.5f);
        int leftTopFront     = builder.addVertex(-0.5f,  0.5f,  0.5f);

        int leftBottomBack   = builder.addVertex(-0.5f, -0.5f, -0.5f);
        int rightBottomBack  = builder.addVertex( 0.5f, -0.5f, -0.5f);
        int rightTopBack     = builder.addVertex( 0.5f,  0.5f, -0.5f);
        int leftTopBack      = builder.addVertex(-0.5f,  0.5f, -0.5f);

        builder.addQuad(leftBottomFront, rightBottomFront, rightTopFront, leftTopFront); // Front
        builder.addQuad(rightBottomFront, rightBottomBack, rightTopBack, rightTopFront); // Right
        builder.addQuad(rightBottomBack, leftBottomBack, leftTopBack, rightTopBack);     // Back
        builder.addQuad(leftBottomBack, leftBottomFront, leftTopFront, leftTopBack);     // Left
        builder.addQuad(leftTopFront, rightTopFront, rightTopBack, leftTopBack);         // Top
        builder.addQuad(leftBottomBack, rightBottomBack, rightBottomFront, leftBottomFront); // Bottom

        return builder.build();
    }

    public static MeshData cubePositionUvNormalTangent() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION_UV_NORMAL_TANGENT);
        int a, b, c, d;

        // Building
        {
            // FRONT (+Z)
            a = builder.addVertex(-0.5f,  0.5f,  0.5f, 0, 1, 0, 0, 1, 1, 0, 0);
            b = builder.addVertex(-0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 1, 1, 0, 0);
            c = builder.addVertex( 0.5f, -0.5f,  0.5f, 1, 0, 0, 0, 1, 1, 0, 0);
            d = builder.addVertex( 0.5f,  0.5f,  0.5f, 1, 1, 0, 0, 1, 1, 0, 0);
            builder.addQuad(a, b, c, d);

            // RIGHT (+X)
            a = builder.addVertex( 0.5f,  0.5f,  0.5f, 0, 1, 1, 0, 0, 0, 0, -1);
            b = builder.addVertex( 0.5f, -0.5f,  0.5f, 0, 0, 1, 0, 0, 0, 0, -1);
            c = builder.addVertex( 0.5f, -0.5f, -0.5f, 1, 0, 1, 0, 0, 0, 0, -1);
            d = builder.addVertex( 0.5f,  0.5f, -0.5f, 1, 1, 1, 0, 0, 0, 0, -1);
            builder.addQuad(a, b, c, d);

            // BACK (-Z)
            a = builder.addVertex( 0.5f,  0.5f, -0.5f, 0, 1, 0, 0, -1, -1, 0, 0);
            b = builder.addVertex( 0.5f, -0.5f, -0.5f, 0, 0, 0, 0, -1, -1, 0, 0);
            c = builder.addVertex(-0.5f, -0.5f, -0.5f, 1, 0, 0, 0, -1, -1, 0, 0);
            d = builder.addVertex(-0.5f,  0.5f, -0.5f, 1, 1, 0, 0, -1, -1, 0, 0);
            builder.addQuad(a, b, c, d);

            // LEFT (-X)
            a = builder.addVertex(-0.5f,  0.5f, -0.5f, 0, 1, -1, 0, 0, 0, 0, 1);
            b = builder.addVertex(-0.5f, -0.5f, -0.5f, 0, 0, -1, 0, 0, 0, 0, 1);
            c = builder.addVertex(-0.5f, -0.5f,  0.5f, 1, 0, -1, 0, 0, 0, 0, 1);
            d = builder.addVertex(-0.5f,  0.5f,  0.5f, 1, 1, -1, 0, 0, 0, 0, 1);
            builder.addQuad(a, b, c, d);

            // TOP (+Y)
            a = builder.addVertex(-0.5f,  0.5f, -0.5f, 0, 1, 0, 1, 0, 1, 0, 0);
            b = builder.addVertex(-0.5f,  0.5f,  0.5f, 0, 0, 0, 1, 0, 1, 0, 0);
            c = builder.addVertex( 0.5f,  0.5f,  0.5f, 1, 0, 0, 1, 0, 1, 0, 0);
            d = builder.addVertex( 0.5f,  0.5f, -0.5f, 1, 1, 0, 1, 0, 1, 0, 0);
            builder.addQuad(a, b, c, d);

            // BOTTOM (-Y)
            a = builder.addVertex(-0.5f, -0.5f,  0.5f, 0, 1, 0, -1, 0, 1, 0, 0);
            b = builder.addVertex(-0.5f, -0.5f, -0.5f, 0, 0, 0, -1, 0, 1, 0, 0);
            c = builder.addVertex( 0.5f, -0.5f, -0.5f, 1, 0, 0, -1, 0, 1, 0, 0);
            d = builder.addVertex( 0.5f, -0.5f,  0.5f, 1, 1, 0, -1, 0, 1, 0, 0);
            builder.addQuad(a, b, c, d);
        }

        return builder.build();
    }

    public static MeshData fullscreenQuadPositionUv() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION_UV);

        int topLeft = builder.addVertex(-1.0f, 1.0f, 0.0f, 0.0f, 1.0f);
        int bottomLeft = builder.addVertex(-1.0f, -1.0f, 0.0f, 0.0f, 0.0f);
        int bottomRight = builder.addVertex(1.0f, -1.0f, 0.0f, 1.0f, 0.0f);
        int topRight = builder.addVertex(1.0f, 1.0f, 0.0f, 1.0f, 1.0f);

        builder.addQuad(topLeft, bottomLeft, bottomRight, topRight);

        return builder.build();
    }

    public static MeshData planePositionUvNormalTangent() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION_UV_NORMAL_TANGENT);

        int topLeft = builder.addVertex(
                -0.5f, 0.0f, -0.5f,
                0.0f, 1.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f
        );

        int bottomLeft = builder.addVertex(
                -0.5f, 0.0f, 0.5f,
                0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f
        );

        int bottomRight = builder.addVertex(
                0.5f, 0.0f, 0.5f,
                1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f
        );

        int topRight = builder.addVertex(
                0.5f, 0.0f, -0.5f,
                1.0f, 1.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f
        );

        builder.addQuad(topLeft, bottomLeft, bottomRight, topRight);

        return builder.build();
    }
}
