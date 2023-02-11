package com.wira.graph.utils;

import com.google.common.truth.Truth;
import com.wira.core.period.Period;

import org.junit.Test;

import java.util.Calendar;
import java.util.logging.Logger;

public class PeriodQuarterTest {
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @Test
    public void test_whether_quarter_start_is_correct() {
        Period quarter = Period.ofQuarter();
        Calendar instance = Calendar.getInstance();
        instance.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar instance1 = Calendar.getInstance();
        instance1.setTimeInMillis(quarter.start());
        Truth.assertThat(instance).isEquivalentAccordingToCompareTo(instance1);
    }

    @Test
    public void test_whether_quarter_end_is_correct() {
        Period quarter = Period.ofQuarter();
        long start = quarter.end();
        Calendar instance = Calendar.getInstance();
        instance.set(2023, Calendar.MARCH, 31, 23, 59, 59);
        Calendar instance1 = Calendar.getInstance();
        instance1.setTimeInMillis(start);
        Truth.assertThat(instance).isEquivalentAccordingToCompareTo(instance1);
    }

    @Test
    public void test_whether_the_dates_of_quarter_period_are_correct() {
        Period quarter = Period.ofQuarter();

        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();

        cal.setTimeInMillis(quarter.start());

        int count = 1;
        do {
            cal1.setTimeInMillis(quarter.timeInMillis(count));
            Truth.assertThat(cal).isEquivalentAccordingToCompareTo(cal1);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } while (++count < quarter.getDuration());
    }
}
