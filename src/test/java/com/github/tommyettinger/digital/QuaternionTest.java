package com.github.tommyettinger.digital;

import static org.junit.Assert.*;

import com.badlogic.gdx.math.Quaternion;
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
}