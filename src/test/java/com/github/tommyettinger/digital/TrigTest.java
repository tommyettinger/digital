package com.github.tommyettinger.digital;

import org.junit.Test;

import static com.github.tommyettinger.digital.TrigTools.*;

public class TrigTest {
    @Test
    public void testTrigVersusCos(){
        for (int i = -9; i < 10; i++) {
            System.out.printf("Math %d:          , sin %.10f, cos %.10f\n", i,
                    Math.sin(i),
                    Math.cos(i));
            System.out.printf("Trig %d: idx %05d, sin %.10f, cos %.10f\n", i, TrigTools.radiansToTableIndex(i),
                    SIN_TABLE[TrigTools.radiansToTableIndex(i)],
                    SIN_TABLE[TrigTools.radiansToTableIndex(i) + TrigTools.SIN_TO_COS & TABLE_MASK]);
            System.out.printf("Cos  %d: idx %05d, sin %.10f, cos %.10f\n", i, CosTools.radiansToTableIndex(i),
                    SIN_TABLE[TrigTools.radiansToTableIndex(i)],
                    CosTools.COS_TABLE[TrigTools.radiansToTableIndex(i)]);
        }
    }
}
