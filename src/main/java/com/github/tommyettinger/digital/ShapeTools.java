package com.github.tommyettinger.digital;

import static com.github.tommyettinger.digital.MathTools.GOLDEN_RATIO;
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

    /**
     * The vertices of a dodecahedron with unitary edge length, as float[3] items representing points.
     */
    public static final float[][] dodecahedronVertices = {
            {-0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO,},    // 0
            {-0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO,},    // 1
            {-0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO,},    // 2
            {-0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO,},    // 3
            {+0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO,},    // 4
            {+0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO,},    // 5
            {+0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO, -0.5f * GOLDEN_RATIO,},    // 6
            {+0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO, +0.5f * GOLDEN_RATIO,},    // 7
            {0f, -0.5f * GOLDEN_RATIO * GOLDEN_RATIO, -0.5f,},                      // 8
            {0f, -0.5f * GOLDEN_RATIO * GOLDEN_RATIO, +0.5f,},                      // 9
            {0f, +0.5f * GOLDEN_RATIO * GOLDEN_RATIO, -0.5f,},                      // 10
            {0f, +0.5f * GOLDEN_RATIO * GOLDEN_RATIO, +0.5f,},                      // 11
            {-0.5f, 0f, -0.5f * GOLDEN_RATIO * GOLDEN_RATIO,},                      // 12
            {+0.5f, 0f, -0.5f * GOLDEN_RATIO * GOLDEN_RATIO,},                      // 13
            {-0.5f, 0f, +0.5f * GOLDEN_RATIO * GOLDEN_RATIO,},                      // 14
            {+0.5f, 0f, +0.5f * GOLDEN_RATIO * GOLDEN_RATIO,},                      // 15
            {-0.5f * GOLDEN_RATIO * GOLDEN_RATIO, -0.5f, 0f,},                      // 16
            {-0.5f * GOLDEN_RATIO * GOLDEN_RATIO, +0.5f, 0f,},                      // 17
            {+0.5f * GOLDEN_RATIO * GOLDEN_RATIO, -0.5f, 0f,},                      // 18
            {+0.5f * GOLDEN_RATIO * GOLDEN_RATIO, +0.5f, 0f,},                      // 19
    };

    /**
     * The faces of a dodecahedron, as int[5] items representing indices into {@link #dodecahedronVertices}.
     */
    public static final int[][] dodecahedronFaces = {
            {8, 9, 0, 1, 16,}, //touching bottom edge, tip of left edge
            {8, 9, 4, 5, 18,}, //touching bottom edge, tip of right edge
            {10, 11, 2, 3, 17,}, //touching top edge, tip of left edge
            {10, 11, 6, 7, 19,}, //touching top edge, tip of right edge
            {12, 13, 0, 4, 8,}, //touching near edge, tip of bottom edge
            {12, 13, 2, 6, 10,}, //touching near edge, tip of top edge
            {14, 15, 1, 5, 9,}, //touching far edge, tip of bottom edge
            {14, 15, 3, 7, 11,}, //touching far edge, tip of top edge
            {16, 17, 0, 2, 12,}, //touching left edge, tip of near edge
            {16, 17, 1, 3, 14,}, //touching left edge, tip of far edge
            {18, 19, 4, 6, 13,}, //touching right edge, tip of near edge
            {18, 19, 5, 7, 15,}, //touching right edge, tip of far edge
    };
}
