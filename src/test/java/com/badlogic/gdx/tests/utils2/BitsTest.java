package com.badlogic.gdx.tests.utils2;

import com.badlogic.gdx.utils2.Bits;
import org.junit.Test;

public class BitsTest {
    @Test
    public void testHashCode(){
        Bits bits = new Bits(1);
        int empty = bits.hashCode();
        bits.set(63);
        System.out.println("Length is " + bits.length());
        int one = bits.hashCode();
        System.out.println(one);
        for (int i = 62; i >= 0; i--) {
            bits.set(i);
            System.out.println(bits.hashCode());
        }
    }
}
