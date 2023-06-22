/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.github.tommyettinger.digital;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/** A simple quaternion class.
 * @see <a href="http://en.wikipedia.org/wiki/Quaternion">http://en.wikipedia.org/wiki/Quaternion</a>
 * @author badlogicgames@gmail.com
 * @author vesuvio
 * @author xoppa */
public class QuaternionDouble {
	private static final QuaternionDouble tmp1 = new QuaternionDouble(0, 0, 0, 0);
	private static final QuaternionDouble tmp2 = new QuaternionDouble(0, 0, 0, 0);

	public double x;
	public double y;
	public double z;
	public double w;

	/** Constructor, sets the four components of the quaternion.
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component */
	public QuaternionDouble(double x, double y, double z, double w) {
		this.set(x, y, z, w);
	}

	public QuaternionDouble() {
		idt();
	}

	/** Constructor, sets the quaternion components from the given quaternion.
	 * 
	 * @param quaternion The quaternion to copy. */
	public QuaternionDouble(QuaternionDouble quaternion) {
		this.set(quaternion);
	}

	/** Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees. */
	public QuaternionDouble(Vector3 axis, double angle) {
		this.set(axis, angle);
	}

	/** Sets the components of the quaternion
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component
	 * @return This quaternion for chaining */
	public QuaternionDouble set (double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/** Sets the quaternion components from the given quaternion.
	 * @param quaternion The quaternion.
	 * @return This quaternion for chaining. */
	public QuaternionDouble set (QuaternionDouble quaternion) {
		return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining. */
	public QuaternionDouble set (Vector3 axis, double angle) {
		return setFromAxis(axis.x, axis.y, axis.z, angle);
	}

	/** @return a copy of this quaternion */
	public QuaternionDouble cpy () {
		return new QuaternionDouble(this);
	}

	/** @return the Euclidean length of the specified quaternion */
	public static double len (final double x, final double y, final double z, final double w) {
		return Math.sqrt(x * x + y * y + z * z + w * w);
	}

	/** @return the Euclidean length of this quaternion */
	public double len () {
		return Math.sqrt(x * x + y * y + z * z + w * w);
	}

	@Override
	public String toString () {
		return "[" + x + "|" + y + "|" + z + "|" + w + "]";
	}

	/** Sets the quaternion to the given euler angles in degrees.
	 * @param yaw the rotation around the y axis in degrees
	 * @param pitch the rotation around the x axis in degrees
	 * @param roll the rotation around the z axis degrees
	 * @return this quaternion */
	public QuaternionDouble setEulerAngles (double yaw, double pitch, double roll) {
		return setEulerAnglesRad(yaw * TrigTools.degreesToRadiansD, pitch * TrigTools.degreesToRadiansD,
			roll * TrigTools.degreesToRadiansD);
	}

	/** Sets the quaternion to the given euler angles in radians.
	 * @param yaw the rotation around the y axis in radians
	 * @param pitch the rotation around the x axis in radians
	 * @param roll the rotation around the z axis in radians
	 * @return this quaternion */
	public QuaternionDouble setEulerAnglesRad (double yaw, double pitch, double roll) {
		final double hr = roll * 0.5;
		final double shr = Math.sin(hr);
		final double chr = Math.cos(hr);
		final double hp = pitch * 0.5;
		final double shp = Math.sin(hp);
		final double chp = Math.cos(hp);
		final double hy = yaw * 0.5;
		final double shy = Math.sin(hy);
		final double chy = Math.cos(hy);
		final double chy_shp = chy * shp;
		final double shy_chp = shy * chp;
		final double chy_chp = chy * chp;
		final double shy_shp = shy * shp;

		// x = cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
		x = (chy_shp * chr) + (shy_chp * shr);
		// y = sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
		y = (shy_chp * chr) - (chy_shp * shr);
		// z = cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
		z = (chy_chp * shr) - (shy_shp * chr);
		// w = cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
		w = (chy_chp * chr) + (shy_shp * shr);
		return this;
	}

	/** Get the pole of the gimbal lock, if any.
	 * @return positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock */
	public int getGimbalPole () {
		final double t = y * x + z * w;
		return t > 0.499 ? 1 : (t < -0.499 ? -1 : 0);
	}

	/** Get the roll euler angle in radians, which is the rotation around the z axis. Requires that this quaternion is normalized.
	 * @return the rotation around the z axis in radians (between -PI and +PI) */
	public double getRollRad () {
		final int pole = getGimbalPole();
		return pole == 0 ? Math.atan2(2.0 * (w * z + y * x), 1.0 - 2.0 * (x * x + z * z)) : pole * 2.0 * Math.atan2(y, w);
	}

	/** Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
	 * @return the rotation around the z axis in degrees (between -180 and +180) */
	public double getRoll () {
		return Math.toDegrees(getRollRad());
	}

	/** Get the pitch euler angle in radians, which is the rotation around the x axis. Requires that this quaternion is normalized.
	 * @return the rotation around the x axis in radians (between -(PI/2) and +(PI/2)) */
	public double getPitchRad () {
		final int pole = getGimbalPole();
		return pole == 0 ? Math.asin(Math.min(Math.max(2.0 * (w * x - z * y), -1.0), 1.0)) : pole * Math.PI * 0.5;
	}

	/** Get the pitch euler angle in degrees, which is the rotation around the x axis. Requires that this quaternion is normalized.
	 * @return the rotation around the x axis in degrees (between -90 and +90) */
	public double getPitch () {
		return Math.toDegrees(getPitchRad());
	}

	/** Get the yaw euler angle in radians, which is the rotation around the y axis. Requires that this quaternion is normalized.
	 * @return the rotation around the y axis in radians (between -PI and +PI) */
	public double getYawRad () {
		return getGimbalPole() == 0 ? Math.atan2(2.0 * (y * w + x * z), 1.0 - 2.0 * (y * y + x * x)) : 0.0;
	}

	/** Get the yaw euler angle in degrees, which is the rotation around the y axis. Requires that this quaternion is normalized.
	 * @return the rotation around the y axis in degrees (between -180 and +180) */
	public double getYaw () {
		return Math.toDegrees(getYawRad());
	}

	public final static double len2 (final double x, final double y, final double z, final double w) {
		return x * x + y * y + z * z + w * w;
	}

	/** @return the length of this quaternion without square root */
	public double len2 () {
		return x * x + y * y + z * z + w * w;
	}

	/** Normalizes this quaternion to unit length
	 * @return the quaternion for chaining */
	public QuaternionDouble nor () {
		double len = len2();
		if (len != 0.0 && !MathTools.isEqual(len, 1.0, MathTools.EPSILON_D)) {
			len = Math.sqrt(len);
			w /= len;
			x /= len;
			y /= len;
			z /= len;
		}
		return this;
	}

	/** Conjugate the quaternion.
	 * 
	 * @return This quaternion for chaining */
	public QuaternionDouble conjugate () {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	// TODO : this would better fit into the vector3 class
	/** Transforms the given vector using this quaternion
	 * 
	 * @param v Vector to transform */
	public Vector3 transform (Vector3 v) {
		tmp2.set(this);
		tmp2.conjugate();
		tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0)).mulLeft(this);

		v.x = (float) tmp2.x;
		v.y = (float) tmp2.y;
		v.z = (float) tmp2.z;
		return v;
	}

	/** Multiplies this quaternion with another one in the form of this = this * other
	 * 
	 * @param other QuaternionDouble to multiply with
	 * @return This quaternion for chaining */
	public QuaternionDouble mul (final QuaternionDouble other) {
		final double newX = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
		final double newY = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
		final double newZ = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
		final double newW = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = this * other
	 * 
	 * @param x the x component of the other quaternion to multiply with
	 * @param y the y component of the other quaternion to multiply with
	 * @param z the z component of the other quaternion to multiply with
	 * @param w the w component of the other quaternion to multiply with
	 * @return This quaternion for chaining */
	public QuaternionDouble mul (final double x, final double y, final double z, final double w) {
		final double newX = this.w * x + this.x * w + this.y * z - this.z * y;
		final double newY = this.w * y + this.y * w + this.z * x - this.x * z;
		final double newZ = this.w * z + this.z * w + this.x * y - this.y * x;
		final double newW = this.w * w - this.x * x - this.y * y - this.z * z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = other * this
	 * 
	 * @param other QuaternionDouble to multiply with
	 * @return This quaternion for chaining */
	public QuaternionDouble mulLeft (QuaternionDouble other) {
		final double newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y;
		final double newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
		final double newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
		final double newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = other * this
	 * 
	 * @param x the x component of the other quaternion to multiply with
	 * @param y the y component of the other quaternion to multiply with
	 * @param z the z component of the other quaternion to multiply with
	 * @param w the w component of the other quaternion to multiply with
	 * @return This quaternion for chaining */
	public QuaternionDouble mulLeft (final double x, final double y, final double z, final double w) {
		final double newX = w * this.x + x * this.w + y * this.z - z * this.y;
		final double newY = w * this.y + y * this.w + z * this.x - x * this.z;
		final double newZ = w * this.z + z * this.w + x * this.y - y * this.x;
		final double newW = w * this.w - x * this.x - y * this.y - z * this.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
	public QuaternionDouble add (QuaternionDouble quaternion) {
		this.x += quaternion.x;
		this.y += quaternion.y;
		this.z += quaternion.z;
		this.w += quaternion.w;
		return this;
	}

	/** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
	public QuaternionDouble add (double qx, double qy, double qz, double qw) {
		this.x += qx;
		this.y += qy;
		this.z += qz;
		this.w += qw;
		return this;
	}

	// TODO : the matrix4 set(quaternion) doesnt set the last row+col of the matrix to 0,0,0,1 so... that's why there is this
// method
	/** Fills a 4x4 matrix with the rotation matrix represented by this quaternion.
	 * 
	 * @param matrix Matrix to fill */
	public void toMatrix (final double[] matrix) {
		final double xx = x * x;
		final double xy = x * y;
		final double xz = x * z;
		final double xw = x * w;
		final double yy = y * y;
		final double yz = y * z;
		final double yw = y * w;
		final double zz = z * z;
		final double zw = z * w;
		// Set matrix from quaternion
		matrix[Matrix4.M00] = 1 - 2 * (yy + zz);
		matrix[Matrix4.M01] = 2 * (xy - zw);
		matrix[Matrix4.M02] = 2 * (xz + yw);
		matrix[Matrix4.M03] = 0;
		matrix[Matrix4.M10] = 2 * (xy + zw);
		matrix[Matrix4.M11] = 1 - 2 * (xx + zz);
		matrix[Matrix4.M12] = 2 * (yz - xw);
		matrix[Matrix4.M13] = 0;
		matrix[Matrix4.M20] = 2 * (xz - yw);
		matrix[Matrix4.M21] = 2 * (yz + xw);
		matrix[Matrix4.M22] = 1 - 2 * (xx + yy);
		matrix[Matrix4.M23] = 0;
		matrix[Matrix4.M30] = 0;
		matrix[Matrix4.M31] = 0;
		matrix[Matrix4.M32] = 0;
		matrix[Matrix4.M33] = 1;
	}

	/** Sets the quaternion to an identity QuaternionDouble
	 * @return this quaternion for chaining */
	public QuaternionDouble idt () {
		return this.set(0, 0, 0, 1);
	}

	/** @return If this quaternion is an identity QuaternionDouble */
	public boolean isIdentity () {
		return MathTools.isZero(x, MathTools.EPSILON_D) && MathTools.isZero(y, MathTools.EPSILON_D) && MathTools.isZero(z, MathTools.EPSILON_D) && MathTools.isEqual(w, 1.0, MathTools.EPSILON_D);
	}

	/** @return If this quaternion is an identity QuaternionDouble */
	public boolean isIdentity (final double tolerance) {
		return MathTools.isZero(x, tolerance) && MathTools.isZero(y, tolerance) && MathTools.isZero(z, tolerance)
			&& MathTools.isEqual(w, 1.0, tolerance);
	}

	// todo : the setFromAxis(v3,double) method should replace the set(v3,double) method
	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param degrees The angle in degrees
	 * @return This quaternion for chaining. */
	public QuaternionDouble setFromAxis (final Vector3 axis, final double degrees) {
		return setFromAxis(axis.x, axis.y, axis.z, degrees);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param radians The angle in radians
	 * @return This quaternion for chaining. */
	public QuaternionDouble setFromAxisRad (final Vector3 axis, final double radians) {
		return setFromAxisRad(axis.x, axis.y, axis.z, radians);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param degrees The angle in degrees
	 * @return This quaternion for chaining. */
	public QuaternionDouble setFromAxis (final double x, final double y, final double z, final double degrees) {
		return setFromAxisRad(x, y, z, degrees * TrigTools.degreesToRadiansD);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param radians The angle in radians
	 * @return This quaternion for chaining. */
	public QuaternionDouble setFromAxisRad (final double x, final double y, final double z, final double radians) {
		double d = Math.sqrt(x * x + y * y + z * z);
		if (d == 0.0) return idt();
		d = 1.0 / d;
		double l_ang = radians < 0 ? TrigTools.PI2_D - (-radians % TrigTools.PI2_D) : radians % TrigTools.PI2_D;
		double l_sin = Math.sin(l_ang / 2);
		double l_cos = Math.cos(l_ang / 2);
		return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).nor();
	}

	/** Sets the QuaternionDouble from the given matrix, optionally removing any scaling. */
	public QuaternionDouble setFromMatrix (boolean normalizeAxes, Matrix4 matrix) {
		return setFromAxes(normalizeAxes, matrix.val[Matrix4.M00], matrix.val[Matrix4.M01], matrix.val[Matrix4.M02],
			matrix.val[Matrix4.M10], matrix.val[Matrix4.M11], matrix.val[Matrix4.M12], matrix.val[Matrix4.M20],
			matrix.val[Matrix4.M21], matrix.val[Matrix4.M22]);
	}

	/** Sets the QuaternionDouble from the given rotation matrix, which must not contain scaling. */
	public QuaternionDouble setFromMatrix (Matrix4 matrix) {
		return setFromMatrix(false, matrix);
	}

	/** Sets the QuaternionDouble from the given matrix, optionally removing any scaling. */
	public QuaternionDouble setFromMatrix (boolean normalizeAxes, Matrix3 matrix) {
		return setFromAxes(normalizeAxes, matrix.val[Matrix3.M00], matrix.val[Matrix3.M01], matrix.val[Matrix3.M02],
			matrix.val[Matrix3.M10], matrix.val[Matrix3.M11], matrix.val[Matrix3.M12], matrix.val[Matrix3.M20],
			matrix.val[Matrix3.M21], matrix.val[Matrix3.M22]);
	}

	/** Sets the QuaternionDouble from the given rotation matrix, which must not contain scaling. */
	public QuaternionDouble setFromMatrix (Matrix3 matrix) {
		return setFromMatrix(false, matrix);
	}

	/**
	 * <p>
	 * Sets the QuaternionDouble from the given x-, y- and z-axis which have to be orthonormal.
	 * </p>
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
	 * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
	 * </p>
	 * 
	 * @param xx x-axis x-coordinate
	 * @param xy x-axis y-coordinate
	 * @param xz x-axis z-coordinate
	 * @param yx y-axis x-coordinate
	 * @param yy y-axis y-coordinate
	 * @param yz y-axis z-coordinate
	 * @param zx z-axis x-coordinate
	 * @param zy z-axis y-coordinate
	 * @param zz z-axis z-coordinate */
	public QuaternionDouble setFromAxes (double xx, double xy, double xz, double yx, double yy, double yz, double zx, double zy, double zz) {
		return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
	}

	/**
	 * <p>
	 * Sets the QuaternionDouble from the given x-, y- and z-axis.
	 * </p>
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
	 * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
	 * </p>
	 * 
	 * @param normalizeAxes whether to normalize the axes (necessary when they contain scaling)
	 * @param xx x-axis x-coordinate
	 * @param xy x-axis y-coordinate
	 * @param xz x-axis z-coordinate
	 * @param yx y-axis x-coordinate
	 * @param yy y-axis y-coordinate
	 * @param yz y-axis z-coordinate
	 * @param zx z-axis x-coordinate
	 * @param zy z-axis y-coordinate
	 * @param zz z-axis z-coordinate */
	public QuaternionDouble setFromAxes (boolean normalizeAxes, double xx, double xy, double xz, double yx, double yy, double yz, double zx,
										 double zy, double zz) {
		if (normalizeAxes) {
			final double lx = 1.0 / Math.sqrt(xx * xx + xy * xy + xz * xz);
			final double ly = 1.0 / Math.sqrt(yx * yx + yy * yy + yz * yz);
			final double lz = 1.0 / Math.sqrt(zx * zx + zy * zy + zz * zz);
			xx *= lx;
			xy *= lx;
			xz *= lx;
			yx *= ly;
			yy *= ly;
			yz *= ly;
			zx *= lz;
			zy *= lz;
			zz *= lz;
		}
		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		final double t = xx + yy + zz;

		// we protect the division by s by ensuring that s>=1
		if (t >= 0) { // |w| >= .5
			double s = Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5 * s;
			s = 0.5 / s; // so this division isn't bad
			x = (zy - yz) * s;
			y = (xz - zx) * s;
			z = (yx - xy) * s;
		} else if ((xx > yy) && (xx > zz)) {
			double s = Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
			x = s * 0.5; // |x| >= .5
			s = 0.5 / s;
			y = (yx + xy) * s;
			z = (xz + zx) * s;
			w = (zy - yz) * s;
		} else if (yy > zz) {
			double s = Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
			y = s * 0.5; // |y| >= .5
			s = 0.5 / s;
			x = (yx + xy) * s;
			z = (zy + yz) * s;
			w = (xz - zx) * s;
		} else {
			double s = Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
			z = s * 0.5; // |z| >= .5
			s = 0.5 / s;
			x = (xz + zx) * s;
			y = (zy + yz) * s;
			w = (yx - xy) * s;
		}

		return this;
	}

	/** Set this quaternion to the rotation between two vectors.
	 * @param v1 The base vector, which should be normalized.
	 * @param v2 The target vector, which should be normalized.
	 * @return This quaternion for chaining */
	public QuaternionDouble setFromCross (final Vector3 v1, final Vector3 v2) {
		final double dot = Math.min(Math.max(v1.dot(v2), -1.0), 1.0);
		final double angle = Math.acos(dot);
		return setFromAxisRad(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x, angle);
	}

	/** Set this quaternion to the rotation between two vectors.
	 * @param x1 The base vectors x value, which should be normalized.
	 * @param y1 The base vectors y value, which should be normalized.
	 * @param z1 The base vectors z value, which should be normalized.
	 * @param x2 The target vector x value, which should be normalized.
	 * @param y2 The target vector y value, which should be normalized.
	 * @param z2 The target vector z value, which should be normalized.
	 * @return This quaternion for chaining */
	public QuaternionDouble setFromCross (final double x1, final double y1, final double z1, final double x2, final double y2,
										  final double z2) {
		final double dot = Math.min(Math.max((x1 * x2 + y1 * y2 + z1 * z2), -1.0), 1.0);
		final double angle = Math.acos(dot);
		return setFromAxisRad(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
	}

	/** Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
	 * [0,1]. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
	 * @param end the end quaternion
	 * @param alpha alpha in the range [0,1]
	 * @return this quaternion for chaining */
	public QuaternionDouble slerp (QuaternionDouble end, double alpha) {
		final double d = this.x * end.x + this.y * end.y + this.z * end.z + this.w * end.w;
		double absDot = d < 0.0 ? -d : d;

		// Set the first and second scale for the interpolation
		double scale0 = 1.0 - alpha;
		double scale1 = alpha;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			final double angle = Math.acos(absDot);
			final double invSinTheta = 1.0 / Math.sin(angle);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = (Math.sin((1.0 - alpha) * angle) * invSinTheta);
			scale1 = (Math.sin((alpha * angle)) * invSinTheta);
		}

		if (d < 0.0) scale1 = -scale1;

		// Calculate the x, y, z and w values for the quaternion by using a
		// special form of linear interpolation for quaternions.
		x = (scale0 * x) + (scale1 * end.x);
		y = (scale0 * y) + (scale1 * end.y);
		z = (scale0 * z) + (scale1 * end.z);
		w = (scale0 * w) + (scale1 * end.w);

		// Return the interpolated quaternion
		return this;
	}

	/** Spherical linearly interpolates multiple quaternions and stores the result in this QuaternionDouble. Will not destroy the data
	 * previously inside the elements of q. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where w_i=1/n.
	 * @param q List of quaternions
	 * @return This quaternion for chaining */
	public QuaternionDouble slerp (QuaternionDouble[] q) {

		// Calculate exponents and multiply everything from left to right
		final double w = 1.0 / q.length;
		set(q[0]).exp(w);
		for (int i = 1; i < q.length; i++)
			mul(tmp1.set(q[i]).exp(w));
		nor();
		return this;
	}

	/** Spherical linearly interpolates multiple quaternions by the given weights and stores the result in this QuaternionDouble. Will
	 * not destroy the data previously inside the elements of q or w. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where the sum of
	 * w_i is 1. Lists must be equal in length.
	 * @param q List of quaternions
	 * @param w List of weights
	 * @return This quaternion for chaining */
	public QuaternionDouble slerp (QuaternionDouble[] q, double[] w) {

		// Calculate exponents and multiply everything from left to right
		set(q[0]).exp(w[0]);
		for (int i = 1; i < q.length; i++)
			mul(tmp1.set(q[i]).exp(w[i]));
		nor();
		return this;
	}

	/** Calculates (this quaternion)^alpha where alpha is a real number and stores the result in this quaternion. See
	 * http://en.wikipedia.org/wiki/Quaternion#Exponential.2C_logarithm.2C_and_power
	 * @param alpha Exponent
	 * @return This quaternion for chaining */
	public QuaternionDouble exp (double alpha) {

		// Calculate |q|^alpha
		double norm = len();
		double normExp = Math.pow(norm, alpha);

		// Calculate theta
		double theta = Math.acos(w / norm);

		// Calculate coefficient of basis elements
		double coeff = 0;
		if (Math.abs(theta) < 0.001) // If theta is small enough, use the limit of sin(alpha*theta) / sin(theta) instead of actual
// value
			coeff = normExp * alpha / norm;
		else
			coeff = normExp * Math.sin(alpha * theta) / (norm * Math.sin(theta));

		// Write results
		w = normExp * Math.cos(alpha * theta);
		x *= coeff;
		y *= coeff;
		z *= coeff;

		// Fix any possible discrepancies
		nor();

		return this;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + BitConversion.doubleToMixedIntBits(w);
		result = prime * result + BitConversion.doubleToMixedIntBits(x);
		result = prime * result + BitConversion.doubleToMixedIntBits(y);
		result = prime * result + BitConversion.doubleToMixedIntBits(z);
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof QuaternionDouble)) {
			return false;
		}
		QuaternionDouble other = (QuaternionDouble)obj;
		return (BitConversion.doubleToLongBits(w) == BitConversion.doubleToLongBits(other.w))
			&& (BitConversion.doubleToLongBits(x) == BitConversion.doubleToLongBits(other.x))
			&& (BitConversion.doubleToLongBits(y) == BitConversion.doubleToLongBits(other.y))
			&& (BitConversion.doubleToLongBits(z) == BitConversion.doubleToLongBits(other.z));
	}

	/** Get the dot product between the two quaternions (commutative).
	 * @param x1 the x component of the first quaternion
	 * @param y1 the y component of the first quaternion
	 * @param z1 the z component of the first quaternion
	 * @param w1 the w component of the first quaternion
	 * @param x2 the x component of the second quaternion
	 * @param y2 the y component of the second quaternion
	 * @param z2 the z component of the second quaternion
	 * @param w2 the w component of the second quaternion
	 * @return the dot product between the first and second quaternion. */
	public final static double dot (final double x1, final double y1, final double z1, final double w1, final double x2, final double y2,
		final double z2, final double w2) {
		return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
	}

	/** Get the dot product between this and the other quaternion (commutative).
	 * @param other the other quaternion.
	 * @return the dot product of this and the other quaternion. */
	public double dot (final QuaternionDouble other) {
		return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
	}

	/** Get the dot product between this and the other quaternion (commutative).
	 * @param x the x component of the other quaternion
	 * @param y the y component of the other quaternion
	 * @param z the z component of the other quaternion
	 * @param w the w component of the other quaternion
	 * @return the dot product of this and the other quaternion. */
	public double dot (final double x, final double y, final double z, final double w) {
		return this.x * x + this.y * y + this.z * z + this.w * w;
	}

	/** Multiplies the components of this quaternion with the given scalar.
	 * @param scalar the scalar.
	 * @return this quaternion for chaining. */
	public QuaternionDouble mul (double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		this.w *= scalar;
		return this;
	}

	/** Get the axis angle representation of the rotation in degrees. The supplied vector will receive the axis (x, y and z values)
	 * of the rotation and the value returned is the angle in degrees around that axis. Note that this method will alter the
	 * supplied vector, the existing value of the vector is ignored.
	 * </p>
	 * This will normalize this quaternion if needed. The received axis is a unit vector. However, if this is an identity
	 * quaternion (no rotation), then the length of the axis may be zero.
	 * 
	 * @param axis vector which will receive the axis
	 * @return the angle in degrees
	 * @see <a href="http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">wikipedia</a>
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle">calculation</a> */
	public double getAxisAngle (Vector3 axis) {
		return getAxisAngleRad(axis) * TrigTools.radiansToDegreesD;
	}

	/** Get the axis-angle representation of the rotation in radians. The supplied vector will receive the axis (x, y and z values)
	 * of the rotation and the value returned is the angle in radians around that axis. Note that this method will alter the
	 * supplied vector, the existing value of the vector is ignored.
	 * </p>
	 * This will normalize this quaternion if needed. The received axis is a unit vector. However, if this is an identity
	 * quaternion (no rotation), then the length of the axis may be zero.
	 * 
	 * @param axis vector which will receive the axis
	 * @return the angle in radians
	 * @see <a href="http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">wikipedia</a>
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle">calculation</a> */
	public double getAxisAngleRad (Vector3 axis) {
		if (this.w > 1) this.nor(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
		double angle = 2.0 * Math.acos(this.w);
		double s = Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less than 1, so term always positive.
		if (s < 0x1p-20) { // test to avoid divide by zero, s is always positive due to sqrt
			// if s close to zero then direction of axis not important
			axis.x = (float) this.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
			axis.y = (float) this.y;
			axis.z = (float) this.z;
		} else {
			axis.x = (float) (this.x / s); // normalise axis
			axis.y = (float) (this.y / s);
			axis.z = (float) (this.z / s);
		}

		return angle;
	}

	/** Get the angle in radians of the rotation this quaternion represents. Does not normalize the quaternion. Use
	 * {@link #getAxisAngleRad(Vector3)} to get both the axis and the angle of this rotation. Use
	 * {@link #getAngleAroundRad(Vector3)} to get the angle around a specific axis.
	 * @return the angle in radians of the rotation */
	public double getAngleRad () {
		return 2.0 * Math.acos((this.w > 1) ? (this.w / len()) : this.w);
	}

	/** Get the angle in degrees of the rotation this quaternion represents. Use {@link #getAxisAngle(Vector3)} to get both the
	 * axis and the angle of this rotation. Use {@link #getAngleAround(Vector3)} to get the angle around a specific axis.
	 * @return the angle in degrees of the rotation */
	public double getAngle () {
		return getAngleRad() * TrigTools.radiansToDegreesD;
	}

	/** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
	 * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
	 * axis perpendicular to the specified axis.
	 * </p>
	 * The swing and twist rotation can be used to reconstruct the original quaternion: this = swing * twist
	 * 
	 * @param axisX the X component of the normalized axis for which to get the swing and twist rotation
	 * @param axisY the Y component of the normalized axis for which to get the swing and twist rotation
	 * @param axisZ the Z component of the normalized axis for which to get the swing and twist rotation
	 * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
	 * @param twist will receive the twist rotation: the rotation around the specified axis
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
	public void getSwingTwist (final double axisX, final double axisY, final double axisZ, final QuaternionDouble swing,
		final QuaternionDouble twist) {
		final double d = (this.x * axisX + this.y * axisY + this.z * axisZ);
		twist.set(axisX * d, axisY * d, axisZ * d, this.w).nor();
		if (d < 0) twist.mul(-1.0);
		swing.set(twist).conjugate().mulLeft(this);
	}

	/** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
	 * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
	 * axis perpendicular to the specified axis.
	 * </p>
	 * The swing and twist rotation can be used to reconstruct the original quaternion: this = swing * twist
	 * 
	 * @param axis the normalized axis for which to get the swing and twist rotation
	 * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
	 * @param twist will receive the twist rotation: the rotation around the specified axis
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
	public void getSwingTwist (final Vector3 axis, final QuaternionDouble swing, final QuaternionDouble twist) {
		getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
	}

	/** Get the angle in radians of the rotation around the specified axis. The axis must be normalized.
	 * @param axisX the x component of the normalized axis for which to get the angle
	 * @param axisY the y component of the normalized axis for which to get the angle
	 * @param axisZ the z component of the normalized axis for which to get the angle
	 * @return the angle in radians of the rotation around the specified axis */
	public double getAngleAroundRad (final double axisX, final double axisY, final double axisZ) {
		final double d = (this.x * axisX + this.y * axisY + this.z * axisZ);
		final double l2 = QuaternionDouble.len2(axisX * d, axisY * d, axisZ * d, this.w);
		return MathTools.isZero(l2, MathTools.EPSILON_D) ?.0
			: (2.0 * Math.acos(Math.min(Math.max((d < 0 ? -this.w : this.w) / Math.sqrt(l2), -1.0), 1.0)));
	}

	/** Get the angle in radians of the rotation around the specified axis. The axis must be normalized.
	 * @param axis the normalized axis for which to get the angle
	 * @return the angle in radians of the rotation around the specified axis */
	public double getAngleAroundRad (final Vector3 axis) {
		return getAngleAroundRad(axis.x, axis.y, axis.z);
	}

	/** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized.
	 * @param axisX the x component of the normalized axis for which to get the angle
	 * @param axisY the y component of the normalized axis for which to get the angle
	 * @param axisZ the z component of the normalized axis for which to get the angle
	 * @return the angle in degrees of the rotation around the specified axis */
	public double getAngleAround (final double axisX, final double axisY, final double axisZ) {
		return getAngleAroundRad(axisX, axisY, axisZ) * TrigTools.radiansToDegreesD;
	}

	/** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized.
	 * @param axis the normalized axis for which to get the angle
	 * @return the angle in degrees of the rotation around the specified axis */
	public double getAngleAround (final Vector3 axis) {
		return getAngleAround(axis.x, axis.y, axis.z);
	}
}