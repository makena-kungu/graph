package com.wira.graph.utils;

import com.google.common.truth.Truth;
import com.wira.core.period.Period;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class PeriodTest {
    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @Test
    public void ofDay() {
    }

    @Test
    public void ofWeek() {
        Period period = Period.ofWeek();
        Calendar instance = Calendar.getInstance();
        instance.set(2022, Calendar.DECEMBER, 26, 0, 0, 0);
        logger.info("" + instance.getTime());
//        Truth.assertThat(new Date(period.start())).isEquivalentAccordingToCompareTo(instance.getTime());
        instance.set(2022, Calendar.DECEMBER, 31, 0, 0, 0);
        Truth.assertThat(new Date(period.end())).isEquivalentAccordingToCompareTo(instance.getTime());
    }

    @Test
    public void ofMonth() {
        Period month = Period.ofMonth();
        long start = month.start();
        long end = month.end();
        logger.info("start: "+new Date(start) + " end:"+ new Date(end));
    }

    @Test
    public void ofQuarter() {
    }

    @Test
    public void ofSemi() {
    }

    @Test
    public void ofYear() {
    }

    @Test
    public void ofMax() {
    }
}