package com.github.tommyettinger.digital;

import static org.junit.Assert.*;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.QuaternionDouble;
import com.badlogic.gdx.math.experimental.QuaternionX;
import org.junit.Test;

public class QuaternionTest {
	private static final float epsilon = 0x1p-11f; // compare to QuaternionDoubleTest

	@Test
	public void testRoundTrip () {
		float yaw = -5f;
		float pitch = 42.00055f;
		float roll = 164.32f;

		Quaternion q = new Quaternion().setEulerAngles(yaw, pitch, roll);

		assertEquals(yaw, q.getYaw(), epsilon);
		assertEquals(pitch, q.getPitch(), epsilon);
		assertEquals(roll, q.getRoll(), epsilon);
	}

	@Test
	public void testMultipleRotations () {
		Quaternion q = new Quaternion();
		Quaternion rot = new Quaternion().setEulerAngles(4f, 0, 0);

		for (int i = 0; i < 90; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0f, q.getYaw(), epsilon);

		q.idt();
		rot.setEulerAngles(0, 4f, 0);

		for (int i = 0; i < 90; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0.0, q.getPitch(), epsilon);

		q.idt();
		rot.setEulerAngles(0, 0, 4f);

		for (int i = 0; i < 21; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(84f, q.getRoll(), epsilon);

		Quaternion quat = new Quaternion().setEulerAngles(0f, 0f, 88f);
		System.out.println(quat.getYaw());
		System.out.println(quat.getPitch());
		System.out.println(quat.getRoll());
		// prints:
		//0.0
		//90.0
		//1.906113E-4
		assertEquals(88f, quat.getRoll(), 0.05f);
	}

	@Test
	public void testGdx() {
		Quaternion r = new Quaternion();
		Quaternion q = new Quaternion();
		Quaternion rot = new Quaternion().setEulerAngles(4f, 8f, 16f);
		for (int i = 0; i < 360; i++) {
			r.mul(rot);
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
	}

	@Test
	public void testExperimental() {
		QuaternionX r = new QuaternionX();
		QuaternionX q = new QuaternionX();
		QuaternionX rot = new QuaternionX().setEulerAngles(4f, 8f, 16f);
		for (int i = 0; i < 360; i++) {
			r.mul(rot);
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
	}

	@Test
	public void testWithDouble() {
		QuaternionDouble r = new QuaternionDouble();
		QuaternionDouble q = new QuaternionDouble();
		QuaternionDouble rot = new QuaternionDouble().setEulerAngles(4f, 8f, 16f);
		for (int i = 0; i < 360; i++) {
			r.mul(rot);
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
	}

	/**
	 * With 360 rotations:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 6.1743784 vs. 179.99883
	 * 10.768361 vs. 83.99919
	 * 22.487045 vs. 179.99895
	 * </pre>
	 * On libGDX 1.9.12:
	 * <pre>
	 * 6.1743784 vs. -179.99641
	 * 10.768361 vs. 84.00223
	 * 22.487045 vs. -179.9974
	 * </pre>
	 * <br>
	 * With 371 rotations:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 45.835953 vs. 43.998928
	 * -34.185814 vs. 88.00129
	 * -145.0157 vs. 175.99911
	 * </pre>
	 * On libGDX 1.9.12:
	 * <pre>
	 * 45.835953 vs. 44.004166
	 * -34.185814 vs. 87.99797
	 * -145.0157 vs. 176.00278
	 * </pre>
	 * <br>
	 * With 1 rotation:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 4.0000935 vs. 4.0001884
	 * 7.9999995 vs. 7.9999976
	 * 16.000097 vs. 16.00019
	 * </pre>
	 * On libGDX 1.9.12:
	 * <pre>
	 * 4.0000935 vs. 4.0000935
	 * 7.9999995 vs. 7.9999995
	 * 16.000097 vs. 16.000097
	 * </pre>
	 * <br>
	 * With 3 rotations:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 18.336832 vs. 12.000094
	 * 18.951181 vs. 23.999992
	 * 51.003944 vs. 48.000126
	 * </pre>
	 * On libGDX 1.9.12:
	 * <pre>
	 * 18.336832 vs. 11.999998
	 * 18.951181 vs. 24.000002
	 * 51.003944 vs. 48.00004
	 * </pre>
	 */
	@Test
	public void testWhat() {
		Quaternion r = new Quaternion();
		Quaternion q = new Quaternion();
		Quaternion rot = new Quaternion().setEulerAngles(4f, 8f, 16f);
		System.out.println("With 1: ");
		for (int i = 0; i < 1; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
		r.idt();
		q.idt();
		System.out.println("With 3: ");
		for (int i = 0; i < 3; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());

	}

}