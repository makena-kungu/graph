package com.nemido.lib.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.util.Calendar;

import static com.nemido.lib.utils.ChronoUnit.ANNUAL;
import static com.nemido.lib.utils.ChronoUnit.DAY;
import static com.nemido.lib.utils.ChronoUnit.MAX;
import static com.nemido.lib.utils.ChronoUnit.MONTH;
import static com.nemido.lib.utils.ChronoUnit.QUARTER;
import static com.nemido.lib.utils.ChronoUnit.SEMI_ANNUAL;
import static com.nemido.lib.utils.ChronoUnit.WEEK;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.getInstance;

public class Period implements Parcelable {
    public ChronoUnit unit;
    private long start;
    private long end;

    private Period(@NonNull ChronoUnit period, long start, long end) {
        unit = period;
        this.start = start;
        this.end = end;
    }

    protected Period(@NonNull Parcel in) {
        unit = (ChronoUnit) in.readSerializable();
        start = in.readLong();
        end = in.readLong();
    }

    public static final Creator<Period> CREATOR = new Creator<Period>() {
        @Override
        @NonNull
        public Period createFromParcel(Parcel in) {
            return new Period(in);
        }

        @Override
        @NonNull
        public Period[] newArray(int size) {
            return new Period[size];
        }
    };

    @NonNull
    private static Period create(ChronoUnit u, long start, long end) {
        return new Period(u, start, end);
    }

    @NonNull
    private static Period create(int field, ChronoUnit p, long time) {
        final Calendar c = getInstance();
        c.setTimeInMillis(time);
        c.set(field, c.getActualMinimum(field));
        min(c, p);
        long start = c.getTimeInMillis();
        reset(c);
        c.set(field, c.getActualMaximum(field));
        max(c, p);
        return create(p, start, c.getTimeInMillis());
    }

    @NonNull
    private static Period create(ChronoUnit u, int count, long time) {
        final Calendar c = getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MONTH, min(count));
        min(c, u);
        final long start = c.getTimeInMillis();
        c.set(Calendar.MONTH, max(count));
        max(c, u);

        return create(u, start, c.getTimeInMillis());
    }

    public void set(@NonNull Period period) {
        unit = period.unit;
        start = period.start;
        end = period.end;
    }

    @NonNull
    public static Period ofDay() {
        return ofDay(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofDay(long date) {
        final Calendar c = getInstance();
        c.setTimeInMillis(date);
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        min(c, DAY);
        final long start = c.getTimeInMillis();
        max(c, DAY);
        return create(DAY, start, c.getTimeInMillis());
    }

    @NonNull
    public static Period ofWeek() {
        return ofWeek(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofWeek(long time) {
        return create(DAY_OF_WEEK, WEEK, time);
    }

    @NonNull
    public static Period ofMonth() {
        return ofMonth(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofMonth(long time) {
        return create(DAY_OF_MONTH, MONTH, time);
    }

    @NonNull
    public static Period ofQuarter() {
        return ofQuarter(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofQuarter(long time) {
        return create(QUARTER, 4, time);
    }

    @NonNull
    public static Period ofSemi() {
        return ofSemi(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofSemi(long time) {
        return create(SEMI_ANNUAL, 2, time);
    }

    @NonNull
    public static Period ofYear() {
        return ofYear(System.currentTimeMillis());
    }

    @NonNull
    public static Period ofYear(long time) {
        return create(DAY_OF_YEAR, ANNUAL, time);
    }

    @NonNull
    public static Period ofMax(long start, long end) {
        final Calendar c = getInstance();
        c.setTimeInMillis(start);
        min(c, MAX);
        start = c.getTimeInMillis();
        c.setTimeInMillis(end);
        max(c, MAX);
        end = c.getTimeInMillis();

        return create(MAX, start, end);
    }

    public int getDuration() {
        return getDuration(this);
    }

    /**
     * Returns the count for instance of hours in Period DAY
     *
     * @param period the period in which the duration belongs to
     * @return the count of the respective time instances
     */
    public static int getDuration(@NonNull Period period) {
        Duration d = Duration.ofMillis(period.end - period.start);
        /*if (period.id == DAY.id) {
            return getInstance().getActualMaximum(Calendar.HOUR_OF_DAY);
        } else if (period.id == WEEK.id) {
            return getInstance().getActualMaximum(Calendar.DAY_OF_WEEK);
        } else if (period.id == MONTH.id) {
            return getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        } else if (period.id == QUARTER.id || period.id == SEMI_ANNUAL.id) {
            //there are four quarters and two halves so use that as a guide

            int count = period.id == QUARTER.id ? 4 : 6;
            final Calendar inst = getInstance();
            final int year = Year.now().getValue();
            final int max = max(count);
            inst.set(Calendar.MONTH, max);
            int dayMin = inst.getActualMinimum(Calendar.DAY_OF_MONTH);
            int dayMax = inst.getActualMaximum(Calendar.DAY_OF_MONTH);
            final Duration duration = Duration.between(LocalDate.of(year, min(count), dayMin),
                    LocalDate.of(year, max, dayMax));

            return Math.toIntExact(duration.toDays());
        } else if (period.id == ANNUAL.id) {
            getInstance().getActualMaximum(Calendar.DAY_OF_YEAR);
        } else if (period.id == MAX.id) {

            final Duration duration = between(
                    LocalDate.of(Year.now().getValue(), 0, 1),
                    LocalDate.now()
            );
            return Math.toIntExact(duration.toDays());
        }*/
        if (period.unit == DAY) {
            return Math.toIntExact(d.toHours()) + 1;
        } else {
            return Math.toIntExact(d.toDays()) + 1;
        }
    }

    /**
     * Computes the first month of a given period where the period is {@link ChronoUnit#QUARTER} or
     * {@link ChronoUnit#SEMI_ANNUAL} with respect to the current date.
     *
     * @param count the number of months in the defined period on a year e.g for a quarter the count
     *              is 4 whereas for a half the count is 2
     * @return first month of the specified period
     */
    private static int min(final int count) {

        // count is say 4 and max count is 12
        // div = 12 / count
        final int div = div(count);

        //2nd month in 1st quarter
        int current = getInstance().get(Calendar.MONTH);
        return current - current%div;
    }

    /**
     * Computes the last month of a given period where the period is {@link ChronoUnit#QUARTER} or
     * {@link ChronoUnit#SEMI_ANNUAL} with respect to the current date.
     *
     * @param count the number of months in the defined period on a year e.g for a quarter the count
     *              is 4 whereas for a half the count is 2
     * @return last month of the specified period
     */
    private static int max(final int count) {
        final int div = div(count);
        int current = getInstance().get(Calendar.MONTH);
        int max = div - 1;
        return current + max - current%div;
    }

    private static int div(int value) {
        return 12/value;
    }

    public long time(int value) {
        return time(value, this);
    }

    /**
     * Extract a date value from a given period and with the value(count of the date within the
     * period) that's being given.
     *
     * @param value the value of date in the period
     * @param p the period instance from which to extract the date
     * @return the date extracted
     */
    public static long time(int value, Period p) {
        final Calendar c = getInstance();
        switch (p.unit) {
            case DAY:
                c.set(Calendar.HOUR_OF_DAY, value);
                break;
            case WEEK:
                c.set(Calendar.DAY_OF_WEEK, value);
                break;
            case MONTH:
                c.set(Calendar.DAY_OF_MONTH, value);
                break;
            case QUARTER:
            case SEMI_ANNUAL:
                if (value > p.getDuration()) {
                    throw new IllegalArgumentException("Value is too big: ");
                }

                c.setTimeInMillis(p.start);
                c.add(DAY_OF_MONTH, value - 1);
                break;
            case ANNUAL:
                c.set(DAY_OF_YEAR, value);
                break;
            default:
                c.setTimeInMillis(p.start);
                c.add(DAY_OF_YEAR, value - 1);
                break;
        }

        return c.getTimeInMillis();
    }

    public void union(Period p) {
        union(this, p);
    }

    public static void union(@NonNull Period p1, @NonNull Period p2) {
        if (p1.unit != p2.unit) {
            throw new IllegalArgumentException("Cannot perform this operation of different periods");
        }

        if (p1.start > p2.start) {
            p1.start = p2.start;
        }
        if (p1.end < p2.end) {
            p1.end = p2.end;
        }
    }

    private static void reset(@NonNull Calendar c) {
        c.setTimeInMillis(System.currentTimeMillis());
    }

    private static void min(@NonNull Calendar c, @NonNull ChronoUnit u) {
        if (u.ordinal() > SEMI_ANNUAL.ordinal())
            c.set(DAY_OF_YEAR, c.getActualMinimum(DAY_OF_YEAR));
        else if (u.ordinal() > WEEK.ordinal())
            c.set(DAY_OF_MONTH, c.getActualMinimum(DAY_OF_MONTH));
        else if (u == WEEK)
            c.set(DAY_OF_WEEK, c.getActualMinimum(DAY_OF_WEEK));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
    }

    private static void max(@NonNull Calendar c, @NonNull ChronoUnit u) {
        if (u.ordinal() > SEMI_ANNUAL.ordinal())
            c.set(DAY_OF_YEAR, c.getActualMaximum(DAY_OF_YEAR));
        else if (u.ordinal() > WEEK.ordinal())
            c.set(DAY_OF_MONTH, c.getActualMaximum(DAY_OF_MONTH));
        else if (u == WEEK)
            c.set(DAY_OF_WEEK, c.getActualMaximum(DAY_OF_WEEK));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeSerializable(unit);
        dest.writeLong(start);
        dest.writeLong(end);
    }
}
