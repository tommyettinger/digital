package com.github.tommyettinger.digital;

import static com.github.tommyettinger.digital.MathTools.ROOT2_INVERSE;

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
     * The vertices of a tetrahedron with unitary edge length, as float[3] items representing points.
     */
    public static final float[][] tetrahedronVertices = {
            {-0.5f, 0f, -0.5f * ROOT2_INVERSE,},
            {+0.5f, 0f, -0.5f * ROOT2_INVERSE,},
            {0f, -0.5f, +0.5f * ROOT2_INVERSE,},
            {0f, +0.5f, +0.5f * ROOT2_INVERSE,},
    };

    /**
     * The faces of a tetrahedron, as int[3] items representing indices into {@link #tetrahedronVertices}.
     */
    public static final int[][] tetrahedronFaces = {
            {0, 1, 2,},
            {0, 1, 3,},
            {0, 2, 3,},
            {1, 2, 3,},
    };

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

    /**
     * The vertices of an octahedron with unitary edge length, as float[3] items representing points.
     */
    public static final float[][] octahedronVertices = {
            {-ROOT2_INVERSE, 0f, 0f},
            {0f, -ROOT2_INVERSE, 0f},
            {0f, 0f, -ROOT2_INVERSE},
            {0f, +ROOT2_INVERSE, 0f},
            {0f, 0f, +ROOT2_INVERSE},
            {+ROOT2_INVERSE, 0f, 0f},
    };

    /**
     * The faces of an octahedron, as int[3] items representing indices into {@link #octahedronVertices}.
     */
    public static final int[][] octahedronFaces = {
            {0, 1, 2,},
            {0, 2, 3,},
            {0, 3, 4,},
            {0, 1, 4,},
            {1, 2, 5,},
            {2, 3, 5,},
            {3, 4, 5,},
            {1, 4, 5,},
    };
}
