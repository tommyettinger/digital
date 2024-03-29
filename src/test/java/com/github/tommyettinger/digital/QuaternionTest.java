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

		Quaternion quat = new Quaternion().setEulerAngles(0f, 0f, 86f);
		System.out.println(quat.getYaw());
		System.out.println(quat.getPitch());
		System.out.println(quat.getRoll());
		// prints:
		//0.0
		//90.0
		//1.906113E-4
		assertEquals(86f, quat.getRoll(), 0.05f);
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
	 * 6.1781583 vs. 178.46837
	 * 10.768361 vs. 84.00357
	 * 22.631712 vs. 179.59442
	 * </pre>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 6.1743784 vs. 179.99883
	 * 10.768361 vs. 83.99919
	 * 22.487045 vs. 179.99895
	 * </pre>
	 * On libGDX 1.12.0:
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
	 * 45.979195 vs. 43.60692
	 * -34.185814 vs. 87.9963
	 * -144.73949 vs. 175.65279
	 * </pre>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 45.835953 vs. 43.998928
	 * -34.185814 vs. 88.00129
	 * -145.0157 vs. 175.99911
	 * </pre>
	 * On libGDX 1.12.0:
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
	 * 4.001034 vs. 4.001034
	 * 7.9999995 vs. 7.9999995
	 * 16.059574 vs. 16.059574
	 * </pre>
	 * <br>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 4.0000935 vs. 4.0001884
	 * 7.9999995 vs. 7.9999976
	 * 16.000097 vs. 16.00019
	 * </pre>
	 * On libGDX 1.12.0:
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
	 * 18.422924 vs. 12.035653
	 * 18.951181 vs. 23.999992
	 * 50.804157 vs. 48.26616
	 * </pre>
	 * <br>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 18.336832 vs. 12.000094
	 * 18.951181 vs. 23.999992
	 * 51.003944 vs. 48.000126
	 * </pre>
	 * On libGDX 1.12.0:
	 * <pre>
	 * 18.336832 vs. 11.999998
	 * 18.951181 vs. 24.000002
	 * 51.003944 vs. 48.00004
	 * </pre>
	 * <br>
	 * With 10 rotations:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 57.81222 vs. 41.24367
	 * -16.239264 vs. 79.99993
	 * 171.73581 vs. 160.04382
	 * </pre>
	 * <br>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 58.085564 vs. 40.00021
	 * -16.239264 vs. 79.99995
	 * 171.74478 vs. 160.00023
	 * </pre>
	 * On libGDX 1.12.0:
	 * <pre>
	 * 58.085564 vs. 40.00013
	 * -16.239264 vs. 79.999985
	 * 171.74478 vs. 160.0001
	 * </pre>
	 * <br>
	 * With 25 rotations:
	 * <br>
	 * On libGDX 1.9.11:
	 * <pre>
	 * 38.022102 vs. 100.09401
	 * 19.21521 vs. 87.99924
	 * 90.29191 vs. 40.19374
	 * </pre>
	 * <br>
	 * On libGDX 1.11.0:
	 * <pre>
	 * 37.786522 vs. 99.99995
	 * 19.21521 vs. 87.99943
	 * 90.29189 vs. 40.000713
	 * </pre>
	 * On libGDX 1.12.0:
	 * <pre>
	 * 37.786522 vs. 100.000275
	 * 19.21521 vs. 87.999725
	 * 90.29189 vs. 40.000153
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
		r.idt();
		q.idt();
		System.out.println("With 10: ");
		for (int i = 0; i < 10; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
		r.idt();
		q.idt();
		System.out.println("With 25: ");
		for (int i = 0; i < 25; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
		r.idt();
		q.idt();
		System.out.println("With 360: ");
		for (int i = 0; i < 360; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());
		r.idt();
		q.idt();
		System.out.println("With 371: ");
		for (int i = 0; i < 371; i++) {
			r.mulLeft(rot);
			q.setEulerAngles(q.getYaw()+4f, q.getPitch()+8f, q.getRoll()+16f);
		}
		System.out.println(r.getYaw() + " vs. " + q.getYaw());
		System.out.println(r.getPitch() + " vs. " + q.getPitch());
		System.out.println(r.getRoll() + " vs. " + q.getRoll());

	}

}