package com.wira.graph;

import com.google.common.truth.Truth;

import org.junit.Test;

public class BitTest {
    @Test
    public void testWhetherNumberOfBitsMethodWorks() {
        int sample = 1000;
        Truth.assertThat(count1(sample)).isNotEqualTo(4);
        Truth.assertThat(count(sample)).isEqualTo(4);
    }

    private int count(int counterand) {
        return Integer.bitCount(counterand);
    }

    private int count1(int counterand) {
        return String.valueOf(counterand).length();
    }
}
