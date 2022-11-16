package com.github.tommyettinger.digital;

/**
 * Static data for 3D shapes, for now.
 */
public final class ShapeTools {
    /**
     * No need to instantiate.
     */
    private ShapeTools() {
    }

    /**
     * The vertices of a cube with unitary edge length, as float[3] items representing points.
     */
    public static final float[][] cubeVertices = {
            {-0.5f, -0.5f, -0.5f,},
            {-0.5f, -0.5f, +0.5f,},
            {-0.5f, +0.5f, -0.5f,},
            {-0.5f, +0.5f, +0.5f,},
            {+0.5f, -0.5f, -0.5f,},
            {+0.5f, -0.5f, +0.5f,},
            {+0.5f, +0.5f, -0.5f,},
            {+0.5f, +0.5f, +0.5f,},
    };

    /**
     * The faces of a cube, as int[4] items representing indices into {@link #cubeVertices}.
     */
    public static final int[][] cubeFaces = {
            {0, 1, 2, 3,},
            {0, 1, 4, 5,},
            {2, 3, 6, 7,},
            {4, 5, 6, 7,},
            {0, 2, 4, 6,},
            {1, 3, 5, 7,},
    };
}
