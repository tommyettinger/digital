package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.digital.Distributor;

import java.io.Externalizable;

public class Vector5 implements Externalizable, Vector<Vector5> {
    /** the x-component of this vector **/
    public float x;
    /** the y-component of this vector **/
    public float y;
    /** the z-component of this vector **/
    public float z;
    /** the w-component of this vector **/
    public float w;
    /** the u-component of this vector **/
    public float u;

    public final static Vector5 X = new Vector5(1, 0, 0, 0, 0);
    public final static Vector5 Y = new Vector5(0, 1, 0, 0, 0);
    public final static Vector5 Z = new Vector5(0, 0, 1, 0, 0);
    public final static Vector5 W = new Vector5(0, 0, 0, 1, 0);
    public final static Vector5 U = new Vector5(0, 0, 0, 0, 1);
    public final static Vector5 Zero = new Vector5(0, 0, 0, 0, 0);

    /** Constructs a vector at (0,0,0,0,0) */
    public Vector5 () {
    }

    /** Creates a vector with the given components
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component * */
    public Vector5 (float x, float y, float z, float w, float u) {
        this.set(x, y, z, w, u);
    }

    /** Creates a vector from the given Vector5
     * @param vector The vector */
    public Vector5 (final Vector5 vector) {
        this.set(vector.x, vector.y, vector.z, vector.w, vector.u);
    }

    /** Creates a vector from the given array. The array must have at least 5 elements.
     *
     * @param values The array */
    public Vector5 (final float[] values) {
        this.set(values[0], values[1], values[2], values[3], values[4]);
    }

    /** Creates a vector from the given Vector2 and z-, w-, and u-components
     *
     * @param vector The vector
     * @param z The z-component
     * @param w The w-component */
    public Vector5 (final Vector2 vector, float z, float w, float u) {
        this.set(vector.x, vector.y, z, w, u);
    }

    /** Creates a vector from the given Vector3 and w- and u-components
     *
     * @param vector The vector
     * @param w The w-component */
    public Vector5 (final Vector3 vector, float w, float u) {
        this.set(vector.x, vector.y, vector.z, w, u);
    }

    /** Creates a vector from the given Vector4 and u-component.
     *
     * @param vector The vector
     * @param u The u-component */
    public Vector5 (final Vector4 vector, float u) {
        this.set(vector.x, vector.y, vector.z, vector.w, u);
    }

    /** Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     * @return this vector for chaining */
    public Vector5 set (float x, float y, float z, float w, float u) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        return this;
    }

    @Override
    public Vector5 set (final Vector5 vector) {
        return this.set(vector.x, vector.y, vector.z, vector.w, vector.u);
    }

    /** Sets the components from the array. The array must have at least 5 elements
     *
     * @param values The array
     * @return this vector for chaining */
    public Vector5 set (final float[] values) {
        return this.set(values[0], values[1], values[2], values[3], values[4]);
    }

    /** Sets the components to the given Vector2, z-, w-, and u-components
     *
     * @param vector The vector2 holding the x- and y-components
     * @param z The z-component
     * @param w The w-component
     * @param u The u-component
     * @return This vector for chaining */
    public Vector5 set (final Vector2 vector, float z, float w, float u) {
        return this.set(vector.x, vector.y, z, w, u);
    }

    /** Sets the components of the given Vector3 and w- and u-components
     *
     * @param vector The vector
     * @param w The w-component
     * @param u The u-component
     * @return This vector for chaining */
    public Vector5 set (final Vector3 vector, float w, float u) {
        return this.set(vector.x, vector.y, vector.z, w, u);
    }

    /** Sets the components of the given Vector4 and u-component
     *
     * @param vector The vector
     * @param u The u-component
     * @return This vector for chaining */
    public Vector5 set (final Vector4 vector, float u) {
        return this.set(vector.x, vector.y, vector.z, vector.w, u);
    }

    @Override
    public Vector5 setToRandomDirection () {
        // The algorithm here is #19 at
        // https://extremelearning.com.au/how-to-generate-uniformly-random-points-on-n-spheres-and-n-balls/ .
        // It is the only recommended way to randomly generate a point on the surface of the unit 5D hypersphere.

        x = (float)Distributor.normal(MathUtils.random.nextLong());
        y = (float)Distributor.normal(MathUtils.random.nextLong());
        z = (float)Distributor.normal(MathUtils.random.nextLong());
        w = (float)Distributor.normal(MathUtils.random.nextLong());
        u = (float)Distributor.normal(MathUtils.random.nextLong());
        // Once we normalize five normal-distributed floats, we have a point on the unit hypersphere's surface.
        return this.nor();
    }

    @Override
    public Vector5 cpy () {
        return new Vector5(this);
    }

    @Override
    public Vector5 add (final Vector5 vector) {
        return this.add(vector.x, vector.y, vector.z, vector.w, vector.u);
    }

    /** Adds the given components to this vector.
     * @param x Added to the x-component
     * @param y Added to the y-component
     * @param z Added to the z-component
     * @param w Added to the w-component
     * @param u Added to the u-component
     * @return This vector for chaining. */
    public Vector5 add (float x, float y, float z, float w, float u) {
        return this.set(this.x + x, this.y + y, this.z + z, this.w + w, this.u + u);
    }

    /** Adds the given value to all five components of the vector.
     *
     * @param value The value
     * @return This vector for chaining */
    public Vector5 add (float value) {
        return this.set(this.x + value, this.y + value, this.z + value, this.w + value, this.u + value);
    }

    @Override
    public Vector5 sub (final Vector5 vector) {
        return this.sub(vector.x, vector.y, vector.z, vector.w, vector.u);
    }

    /** Subtracts the given components from this vector.
     *
     * @param x Subtracted from the x-component
     * @param y Subtracted from the y-component
     * @param z Subtracted from the z-component
     * @param w Subtracted from the w-component
     * @param u Subtracted from the u-component
     * @return This vector for chaining */
    public Vector5 sub (float x, float y, float z, float w, float u) {
        return this.set(this.x - x, this.y - y, this.z - z, this.w - w, this.u - u);
    }

    /** Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining */
    public Vector5 sub (float value) {
        return this.set(this.x - value, this.y - value, this.z - value, this.w - value, this.u - value);
    }

    /** Multiplies each component of this vector by the given scalar
     * @param scalar Each component will be multiplied by this float
     * @return This vector for chaining */
    @Override
    public Vector5 scl (float scalar) {
        return this.set(this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar, this.u * scalar);
    }

    /** Multiplies each component of this vector by the corresponding component in other
     * @param other Another Vector5 that will be used to scale this
     * @return This vector for chaining */
    @Override
    public Vector5 scl (final Vector5 other) {
        return this.set(x * other.x, y * other.y, z * other.z, w * other.w, u * other.u);
    }

    /** Scales this vector by the given values
     * @param vx Multiplied with the X value
     * @param vy Multiplied with the Y value
     * @param vz Multiplied with the Z value
     * @param vw Multiplied with the W value
     * @param vu Multiplied with the U value
     * @return This vector for chaining */
    public Vector5 scl (float vx, float vy, float vz, float vw, float vu) {
        return this.set(this.x * vx, this.y * vy, this.z * vz, this.w * vu, this.w * vu);
    }

    @Override
    public Vector5 mulAdd (Vector5 vec, float scalar) {
        this.x += vec.x * scalar;
        this.y += vec.y * scalar;
        this.z += vec.z * scalar;
        this.w += vec.w * scalar;
        this.u += vec.u * scalar;
        return this;
    }

    @Override
    public Vector5 mulAdd (Vector5 vec, Vector5 mulVec) {
        this.x += vec.x * mulVec.x;
        this.y += vec.y * mulVec.y;
        this.z += vec.z * mulVec.z;
        this.w += vec.w * mulVec.w;
        this.u += vec.u * mulVec.u;
        return this;
    }

    /** @return The Euclidean length */
    public static float len (final float x, final float y, final float z, float w, float u) {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w + u * u);
    }

    @Override
    public float len () {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w + u * u);
    }

    /** @return The squared Euclidean length */
    public static float len2 (final float x, final float y, final float z, float w, float u) {
        return x * x + y * y + z * z + w * w + u * u;
    }

    @Override
    public float len2 () {
        return x * x + y * y + z * z + w * w + u * u;
    }

    /** Returns true if this vector and the vector parameter have identical components.
     * @param vector The other vector
     * @return Whether this and the other vector are equal with exact precision */
    public boolean idt (final Vector5 vector) {
        return x == vector.x && y == vector.y && z == vector.z && w == vector.w && u == vector.u;
    }

    /** @return The Euclidean distance between the two specified vectors */
    public static float dst (final float x1, final float y1, final float z1, final float w1, final float u1,
                             final float x2, final float y2, final float z2, final float w2, final float u2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        final float d = w2 - w1;
        final float e = u2 - u1;
        return (float)Math.sqrt(a * a + b * b + c * c + d * d + e * e);
    }

    @Override
    public float dst (final Vector5 vector) {
        final float a = vector.x - x;
        final float b = vector.y - y;
        final float c = vector.z - z;
        final float d = vector.w - w;
        final float e = vector.u - u;
        return (float)Math.sqrt(a * a + b * b + c * c + d * d + e * e);
    }

    /** @return the distance between this point and the given point */
    public float dst (float x, float y, float z, float w, float u) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        final float d = w - this.w;
        final float e = u - this.u;
        return (float)Math.sqrt(a * a + b * b + c * c + d * d + e * e);
    }

    /** @return the squared distance between the given points */
    public static float dst2 (final float x1, final float y1, final float z1, final float w1, final float u1,
                              final float x2, final float y2, final float z2, final float w2, final float u2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        final float d = w2 - w1;
        final float e = u2 - u1;
        return a * a + b * b + c * c + d * d + e * e;
    }

    @Override
    public float dst2 (Vector5 point) {
        final float a = point.x - x;
        final float b = point.y - y;
        final float c = point.z - z;
        final float d = point.w - w;
        final float e = point.u - u;
        return a * a + b * b + c * c + d * d + e * e;
    }

    /** Returns the squared distance between this point and the given point
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @param w The w-component of the other point
     * @return The squared distance */
    public float dst2 (float x, float y, float z, float w, float u) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        final float d = w - this.w;
        final float e = u - this.u;
        return a * a + b * b + c * c + d * d + e * e;
    }

    @Override
    public Vector5 nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.scl(1f / (float)Math.sqrt(len2));
    }

    /** @return The dot product between the two vectors */
    public static float dot (float x1, float y1, float z1, float w1, float u1,
                             float x2, float y2, float z2, float w2, float u2) {
        return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2 + u1 * u2;
    }

    @Override
    public float dot (final Vector5 vector) {
        return x * vector.x + y * vector.y + z * vector.z + w * vector.w + u * vector.u;
    }

    /** Returns the dot product between this and the given vector (given as 4 components).
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @param w The w-component of the other vector
     * @param u The u-component of the other vector
     * @return The dot product */
    public float dot (float x, float y, float z, float w, float u) {
        return this.x * x + this.y * y + this.z * z + this.w * w + this.u * u;
    }

    @Override
    public boolean isUnit () {
        return isUnit(0.000000001f);
    }

    @Override
    public boolean isUnit (final float margin) {
        return Math.abs(len2() - 1f) < margin;
    }

    @Override
    public boolean isZero () {
        return x == 0 && y == 0 && z == 0 && w == 0 && u == 0;
    }

    @Override
    public boolean isZero (final float margin) {
        return len2() < margin;
    }

    /** @return true if this vector is in line with the other vector (either in the same or the opposite direction) */
    @Override
    public boolean isOnLine (Vector5 other, float epsilon) {
        // The algorithm used here is based on the one in yama, a C++ math library.
        // https://github.com/iboB/yama/blob/f08a71c6fd84df5eed62557000373f17f14e1ec7/include/yama/vector4.hpp#L566-L598
        // This code uses a flags variable to avoid allocating a float array.
        int flags = 0;
        float dx = 0, dy = 0, dz = 0, dw = 0, du = 0;

        if (MathUtils.isZero(x, epsilon)) {
            if (!MathUtils.isZero(other.x, epsilon)) {
                return false;
            }
        } else {
            dx = x / other.x;
            flags |= 1;
        }
        if (MathUtils.isZero(y, epsilon)) {
            if (!MathUtils.isZero(other.y, epsilon)) {
                return false;
            }
        } else {
            dy = y / other.y;
            flags |= 2;
        }
        if (MathUtils.isZero(z, epsilon)) {
            if (!MathUtils.isZero(other.z, epsilon)) {
                return false;
            }
        } else {
            dz = z / other.z;
            flags |= 4;
        }
        if (MathUtils.isZero(w, epsilon)) {
            if (!MathUtils.isZero(other.w, epsilon)) {
                return false;
            }
        } else {
            dw = w / other.w;
            flags |= 8;
        }
        if (MathUtils.isZero(u, epsilon)) {
            if (!MathUtils.isZero(other.u, epsilon)) {
                return false;
            }
        } else {
            du = u / other.u;
            flags |= 16;
        }

        int lowest = flags & -flags;
        flags ^= lowest;
        if(flags == 0) return true;

        boolean on = true;
        float left;
        switch (lowest) {
            case 1:  left = dx; break;
            case 2:  left = dy; break;
            case 4:  left = dz; break;
            default: left = dw; break;
        }

        while (true){
            int next = flags & -flags; // get the lowest remaining bit of flags
            switch (next){
                case 2:  on &= MathUtils.isEqual(left, dy, epsilon); break;
                case 4:  on &= MathUtils.isEqual(left, dz, epsilon); break;
                case 8:  on &= MathUtils.isEqual(left, dw, epsilon); break;
                default: on &= MathUtils.isEqual(left, du, epsilon); break;
            }
            flags ^= next;
            if(flags == 0) {
                return on;
            }
        }
    }

    /** @return true if this vector is in line with the other vector (either in the same or the opposite direction) */
    @Override
    public boolean isOnLine (Vector5 other) {
        return isOnLine(other, MathUtils.FLOAT_ROUNDING_ERROR);
    }

    /** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector5, float)} &&
     *         {@link #hasSameDirection(Vector5)}). */
    @Override
    public boolean isCollinear (Vector5 other, float epsilon) {
        return isOnLine(other, epsilon) && hasSameDirection(other);
    }

    /** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector5)} &&
     *         {@link #hasSameDirection(Vector5)}). */
    @Override
    public boolean isCollinear (Vector5 other) {
        return isOnLine(other) && hasSameDirection(other);
    }

    /** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector5, float)} &&
     *         {@link #hasSameDirection(Vector5)}). */
    @Override
    public boolean isCollinearOpposite (Vector5 other, float epsilon) {
        return isOnLine(other, epsilon) && hasOppositeDirection(other);
    }

    /** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector5)} &&
     *         {@link #hasSameDirection(Vector5)}). */
    @Override
    public boolean isCollinearOpposite (Vector5 other) {
        return isOnLine(other) && hasOppositeDirection(other);
    }

    @Override
    public boolean isPerpendicular (Vector5 vector) {
        return MathUtils.isZero(dot(vector));
    }

    @Override
    public boolean isPerpendicular (Vector5 vector, float epsilon) {
        return MathUtils.isZero(dot(vector), epsilon);
    }

    @Override
    public boolean hasSameDirection (Vector5 vector) {
        return dot(vector) > 0;
    }

    @Override
    public boolean hasOppositeDirection (Vector5 vector) {
        return dot(vector) < 0;
    }

    @Override
    public Vector5 lerp (final Vector5 target, float alpha) {
        x += alpha * (target.x - x);
        y += alpha * (target.y - y);
        z += alpha * (target.z - z);
        w += alpha * (target.w - w);
        u += alpha * (target.u - u);
        return this;
    }

    @Override
    public Vector5 interpolate (Vector5 target, float alpha, Interpolation interpolator) {
        return lerp(target, interpolator.apply(alpha));
    }

    /** Converts this {@code Vector5} to a string in the format {@code (x,y,z,w)}. Strings with this exact format can be parsed
     * with {@link #fromString(String)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + x + "," + y + "," + z + "," + w + ")";
    }

    /** Sets this {@code Vector5} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param v the string.
     * @return this vector, set with the value from v, for chaining */
    public Vector5 fromString (String v) {
        int s0 = v.indexOf(',', 1);
        int s1 = v.indexOf(',', s0 + 1);
        int s2 = v.indexOf(',', s1 + 1);
        if (s0 != -1 && s1 != -1 && v.charAt(0) == '(' && v.charAt(v.length() - 1) == ')') {
            try {
                float x = Float.parseFloat(v.substring(1, s0));
                float y = Float.parseFloat(v.substring(s0 + 1, s1));
                float z = Float.parseFloat(v.substring(s1 + 1, s2));
                float w = Float.parseFloat(v.substring(s2 + 1, v.length() - 1));
                return this.set(x, y, z, w);
            } catch (NumberFormatException ex) {
                // Throw a GdxRuntimeException...
            }
        }
        throw new GdxRuntimeException("Malformed Vector5: " + v);
    }

    @Override
    public Vector5 limit (float limit) {
        return limit2(limit * limit);
    }

    @Override
    public Vector5 limit2 (float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }

    @Override
    public Vector5 setLength (float len) {
        return setLength2(len * len);
    }

    @Override
    public Vector5 setLength2 (float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    @Override
    public Vector5 clamp (float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + NumberUtils.floatToIntBits(x);
        result = prime * result + NumberUtils.floatToIntBits(y);
        result = prime * result + NumberUtils.floatToIntBits(z);
        result = prime * result + NumberUtils.floatToIntBits(w);
        return result;
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector5 other = (Vector5)obj;
        if (NumberUtils.floatToIntBits(x) != NumberUtils.floatToIntBits(other.x)) return false;
        if (NumberUtils.floatToIntBits(y) != NumberUtils.floatToIntBits(other.y)) return false;
        if (NumberUtils.floatToIntBits(z) != NumberUtils.floatToIntBits(other.z)) return false;
        if (NumberUtils.floatToIntBits(w) != NumberUtils.floatToIntBits(other.w)) return false;
        return true;
    }

    @Override
    public boolean epsilonEquals (final Vector5 other, float epsilon) {
        if (other == null) return false;
        if (Math.abs(other.x - x) > epsilon) return false;
        if (Math.abs(other.y - y) > epsilon) return false;
        if (Math.abs(other.z - z) > epsilon) return false;
        if (Math.abs(other.w - w) > epsilon) return false;
        return true;
    }

    /** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
     * @param x x component of the other vector to compare
     * @param y y component of the other vector to compare
     * @param z z component of the other vector to compare
     * @param w w component of the other vector to compare
     * @param epsilon how much error to tolerate and still consider two floats equal
     * @return whether the vectors are the same. */
    public boolean epsilonEquals (float x, float y, float z, float w, float epsilon) {
        if (Math.abs(x - this.x) > epsilon) return false;
        if (Math.abs(y - this.y) > epsilon) return false;
        if (Math.abs(z - this.z) > epsilon) return false;
        if (Math.abs(w - this.w) > epsilon) return false;
        return true;
    }

    /** Compares this vector with the other vector using {@link MathUtils#FLOAT_ROUNDING_ERROR} for its epsilon.
     *
     * @param other other vector to compare
     * @return true if the vectors are equal, otherwise false */
    public boolean epsilonEquals (final Vector5 other) {
        return epsilonEquals(other, MathUtils.FLOAT_ROUNDING_ERROR);
    }

    /** Compares this vector with the other vector using {@link MathUtils#FLOAT_ROUNDING_ERROR} for its epsilon.
     *
     * @param x x component of the other vector to compare
     * @param y y component of the other vector to compare
     * @param z z component of the other vector to compare
     * @param w w component of the other vector to compare
     * @return true if the vectors are equal, otherwise false */
    public boolean epsilonEquals (float x, float y, float z, float w) {
        return epsilonEquals(x, y, z, w, MathUtils.FLOAT_ROUNDING_ERROR);
    }

    @Override
    public Vector5 setZero () {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
        return this;
    }
}
