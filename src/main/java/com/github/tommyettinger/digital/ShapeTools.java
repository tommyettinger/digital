package com.github.tommyettinger.digital;

import static com.github.tommyettinger.digital.MathTools.GOLDEN_RATIO;
import static com.github.tommyettinger.digital.MathTools.GOLDEN_RATIO_D;
import static com.github.tommyettinger.digital.MathTools.ROOT2_INVERSE;
import static com.github.tommyettinger.digital.MathTools.ROOT2_INVERSE_D;

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
    public static final float[][] TETRAHEDRON_VERTICES = {
            {-0.5f, 0f, -0.5f * ROOT2_INVERSE,},
            {+0.5f, 0f, -0.5f * ROOT2_INVERSE,},
            {0f, -0.5f, +0.5f * ROOT2_INVERSE,},
            {0f, +0.5f, +0.5f * ROOT2_INVERSE,},
    };

    /**
     * The vertices of a tetrahedron with unitary edge length, as double[3] items representing points.
     */
    public static final double[][] TETRAHEDRON_VERTICES_D = {
            {-0.5, 0, -0.5 * ROOT2_INVERSE_D,},
            {+0.5, 0, -0.5 * ROOT2_INVERSE_D,},
            {0, -0.5, +0.5 * ROOT2_INVERSE_D,},
            {0, +0.5, +0.5 * ROOT2_INVERSE_D,},
    };

    /**
     * The faces of a tetrahedron, as int[3] items representing indices into {@link #TETRAHEDRON_VERTICES}.
     */
    public static final int[][] TETRAHEDRON_FACES = {
            {0, 1, 2,},
            {0, 1, 3,},
            {0, 2, 3,},
            {1, 2, 3,},
    };

    /**
     * The vertices of a cube with unitary edge length, as float[3] items representing points.
     */
    public static final float[][] CUBE_VERTICES = {
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
     * The vertices of a cube with unitary edge length, as double[3] items representing points.
     */
    public static final double[][] CUBE_VERTICES_D = {
            {-0.5, -0.5, -0.5,},
            {-0.5, -0.5, +0.5,},
            {-0.5, +0.5, -0.5,},
            {-0.5, +0.5, +0.5,},
            {+0.5, -0.5, -0.5,},
            {+0.5, -0.5, +0.5,},
            {+0.5, +0.5, -0.5,},
            {+0.5, +0.5, +0.5,},
    };

    /**
     * The faces of a cube, as int[4] items representing indices into {@link #CUBE_VERTICES}.
     */
    public static final int[][] CUBE_FACES = {
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
    public static final float[][] OCTAHEDRON_VERTICES = {
            {-ROOT2_INVERSE, 0f, 0f},
            {0f, -ROOT2_INVERSE, 0f},
            {0f, 0f, -ROOT2_INVERSE},
            {0f, +ROOT2_INVERSE, 0f},
            {0f, 0f, +ROOT2_INVERSE},
            {+ROOT2_INVERSE, 0f, 0f},
    };

    /**
     * The vertices of an octahedron with unitary edge length, as double[3] items representing points.
     */
    public static final double[][] OCTAHEDRON_VERTICES_D = {
            {-ROOT2_INVERSE_D, 0, 0},
            {0, -ROOT2_INVERSE_D, 0},
            {0, 0, -ROOT2_INVERSE_D},
            {0, +ROOT2_INVERSE_D, 0},
            {0, 0, +ROOT2_INVERSE_D},
            {+ROOT2_INVERSE_D, 0, 0},
    };

    /**
     * The faces of an octahedron, as int[3] items representing indices into {@link #OCTAHEDRON_VERTICES}.
     */
    public static final int[][] OCTAHEDRON_FACES = {
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
    public static final float[][] DODECAHEDRON_VERTICES = {
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
     * The vertices of a dodecahedron with unitary edge length, as double[3] items representing points.
     */
    public static final double[][] DODECAHEDRON_VERTICES_D = {
            {-0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D,},    // 0
            {-0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D,},    // 1
            {-0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D,},    // 2
            {-0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D,},    // 3
            {+0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D,},    // 4
            {+0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D,},    // 5
            {+0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D, -0.5 * GOLDEN_RATIO_D,},    // 6
            {+0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D, +0.5 * GOLDEN_RATIO_D,},    // 7
            {0, -0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, -0.5,},                      // 8
            {0, -0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, +0.5,},                      // 9
            {0, +0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, -0.5,},                      // 10
            {0, +0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, +0.5,},                      // 11
            {-0.5, 0, -0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D,},                      // 12
            {+0.5, 0, -0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D,},                      // 13
            {-0.5, 0, +0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D,},                      // 14
            {+0.5, 0, +0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D,},                      // 15
            {-0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, -0.5, 0,},                      // 16
            {-0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, +0.5, 0,},                      // 17
            {+0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, -0.5, 0,},                      // 18
            {+0.5 * GOLDEN_RATIO_D * GOLDEN_RATIO_D, +0.5, 0,},                      // 19
    };

    /**
     * The faces of a dodecahedron, as int[5] items representing indices into {@link #DODECAHEDRON_VERTICES}.
     */
    public static final int[][] DODECAHEDRON_FACES = {
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

    /**
     * The vertices of an icosahedron with unitary edge length, as float[3] items representing points.
     * These points are specially organized so that {@code ICOSAHEDRON_VERTICES[i]} will always contain the opposite
     * point of {@code ICOSAHEDRON_VERTICES[i ^ 1]} (such as with the North Pole and South Pole).
     */
    public static final float[][] ICOSAHEDRON_VERTICES = {
            {0f, -0.5f, -0.5f * GOLDEN_RATIO,}, // 0 bottom edge, touching near
            {0f, +0.5f, +0.5f * GOLDEN_RATIO,}, // 1 top edge, touching far
            {0f, +0.5f, -0.5f * GOLDEN_RATIO,}, // 2 bottom edge, touching far
            {0f, -0.5f, +0.5f * GOLDEN_RATIO,}, // 3 top edge, touching near
            {-0.5f * GOLDEN_RATIO, 0f, -0.5f,}, // 4 left edge, touching bottom
            {+0.5f * GOLDEN_RATIO, 0f, +0.5f,}, // 5 right edge, touching top
            {-0.5f * GOLDEN_RATIO, 0f, +0.5f,}, // 6 left edge, touching top
            {+0.5f * GOLDEN_RATIO, 0f, -0.5f,}, // 7 right edge, touching bottom
            {-0.5f, -0.5f * GOLDEN_RATIO, 0f,}, // 8 near edge, touching left
            {+0.5f, +0.5f * GOLDEN_RATIO, 0f,}, // 9 far edge, touching right
            {+0.5f, -0.5f * GOLDEN_RATIO, 0f,}, //10 near edge, touching right
            {-0.5f, +0.5f * GOLDEN_RATIO, 0f,}, //11 far edge, touching left
    };

    /**
     * The vertices of an icosahedron with unitary edge length, as double[3] items representing points.
     * These points are specially organized so that {@code ICOSAHEDRON_VERTICES_D[i]} will always contain the opposite
     * point of {@code ICOSAHEDRON_VERTICES_D[i ^ 1]} (such as with the North Pole and South Pole).
     */
    public static final double[][] ICOSAHEDRON_VERTICES_D = {
            {0, -0.5, -0.5 * GOLDEN_RATIO_D,}, // 0 bottom edge, touching near
            {0, +0.5, +0.5 * GOLDEN_RATIO_D,}, // 1 top edge, touching far
            {0, +0.5, -0.5 * GOLDEN_RATIO_D,}, // 2 bottom edge, touching far
            {0, -0.5, +0.5 * GOLDEN_RATIO_D,}, // 3 top edge, touching near
            {-0.5 * GOLDEN_RATIO_D, 0, -0.5,}, // 4 left edge, touching bottom
            {+0.5 * GOLDEN_RATIO_D, 0, +0.5,}, // 5 right edge, touching top
            {-0.5 * GOLDEN_RATIO_D, 0, +0.5,}, // 6 left edge, touching top
            {+0.5 * GOLDEN_RATIO_D, 0, -0.5,}, // 7 right edge, touching bottom
            {-0.5, -0.5 * GOLDEN_RATIO_D, 0,}, // 8 near edge, touching left
            {+0.5, +0.5 * GOLDEN_RATIO_D, 0,}, // 9 far edge, touching right
            {+0.5, -0.5 * GOLDEN_RATIO_D, 0,}, //10 near edge, touching right
            {-0.5, +0.5 * GOLDEN_RATIO_D, 0,}, //11 far edge, touching left
    };

    /**
     * The faces of an icosahedron, as int[3] items representing indices into {@link #ICOSAHEDRON_VERTICES}.
     * These faces are specially organized so that {@code ICOSAHEDRON_FACES[i]} will always contain the opposite face
     * of {@code ICOSAHEDRON_FACES[i ^ 1]}.
     */
    public static final int[][] ICOSAHEDRON_FACES = {
            {0, 2, 4,},
            {1, 3, 5,},
            {0, 2, 7,},
            {1, 3, 6,},
            {4, 6, 8,},
            {5, 7, 9,},
            {4, 6, 11,},
            {5, 7, 10,},
            {8, 10, 0,},
            {9, 11, 1,},
            {8, 10, 3,},
            {9, 11, 2,},
            {0, 8, 4,},
            {1, 9, 5,},
            {0, 10, 7,},
            {1, 11, 6,},
            {2, 9, 7,},
            {3, 8, 6,},
            {2, 11, 4,},
            {3, 10, 5,},
    };

    private static double len2_d(double[] v) {
        return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
    }
    private static float len2(float[] v) {
        return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
    }

    private static double len_d(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    private static float len(float[] v) {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    private static double[] nor_d(double[] v) {
        final double il = 1.0/len_d(v);
        return new double[] {v[0] * il, v[1] * il, v[2] * il};
    }
    private static float[] nor(float[] v) {
        final float il = 1f/len(v);
        return new float[] {v[0] * il, v[1] * il, v[2] * il};
    }

    /**
     * A variant on {@link #TETRAHEDRON_VERTICES} that has each vertex at distance 1 from the origin.
     */
    public static final float[][] UNIT_TETRAHEDRON_VERTICES = new float[TETRAHEDRON_VERTICES.length][];

    /**
     * A variant on {@link #TETRAHEDRON_VERTICES_D} that has each vertex at distance 1 from the origin.
     */
    public static final double[][] UNIT_TETRAHEDRON_VERTICES_D = new double[TETRAHEDRON_VERTICES_D.length][];

    static {
        for (int i = 0; i < TETRAHEDRON_VERTICES.length; i++) {
            UNIT_TETRAHEDRON_VERTICES[i] = nor(TETRAHEDRON_VERTICES[i]);
            UNIT_TETRAHEDRON_VERTICES_D[i] = nor_d(TETRAHEDRON_VERTICES_D[i]);
        }
    }
}
