package com.github.tommyettinger.digital;

import org.junit.Test;

public class TrigTest {
    @Test
    public void testTrigVersusCos(){
        for (int i = -9; i < 10; i++) {
            System.out.printf("Trig %d: idx %05d, sin %.10f, cos %.10f\n", i, TrigTools.radiansToTableIndex(i),
                    TrigTools.SIN_TABLE[TrigTools.radiansToTableIndex(i)],
                    TrigTools.SIN_TABLE[TrigTools.radiansToTableIndex(i) + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK]);
            System.out.printf("Cos  %d: idx %05d, sin %.10f, cos %.10f\n", i, CosTools.radiansToTableIndex(i),
                    CosTools.COS_TABLE[CosTools.radiansToTableIndex(i) + CosTools.COS_TO_SIN & CosTools.TABLE_MASK],
                    CosTools.COS_TABLE[CosTools.radiansToTableIndex(i)]);
        }
    }
}
