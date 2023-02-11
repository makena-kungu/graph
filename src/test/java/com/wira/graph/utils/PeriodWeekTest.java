package com.wira.graph.utils;

import com.google.common.truth.Truth;
import com.wira.core.period.Period;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class PeriodWeekTest {

    @Test
    public void ofWeekStart() {
        Period period = Period.ofWeek();
        Calendar instance = Calendar.getInstance();
        instance.set(2023, 0, 2, 0, 0, 0);
        Truth.assertThat(new Date(period.start())).isEquivalentAccordingToCompareTo(instance.getTime());
    }

    @Test
    public void ofWeekEnd() {
        Period period = Period.ofWeek();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, 0, 8, 23, 59, 59);
        Truth.assertThat(new Date(period.end())).isEquivalentAccordingToCompareTo(calendar.getTime());
    }

    @Test
    public void ofCalendarMinDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(2022, 11, 31);
        int minimum = cal.getMinimum(Calendar.DAY_OF_WEEK);
        cal.set(Calendar.DAY_OF_WEEK, minimum);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
//        Truth.assertThat(cal.getTime()).isEqualTo(1);
    }
}