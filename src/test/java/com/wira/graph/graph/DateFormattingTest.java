package com.wira.graph.graph;

import com.google.common.truth.Truth;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormattingTest {
    @Test
    public void testDateFormatter() {
        DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.FULL);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        String today = dayFormat.format(new Date());
        Truth.assertThat(today).isEqualTo("Friday");
        Truth.assertThat(dateInstance.format(new Date()))
                .contains("Friday");
    }

    @Test
    public void testTimeFormatter(){
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
        Truth.assertThat(format.format(new Date())).isEqualTo("22:23");
    }
}
