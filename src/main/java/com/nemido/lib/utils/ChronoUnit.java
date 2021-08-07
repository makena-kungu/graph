package com.nemido.lib.utils;

import androidx.annotation.NonNull;

public enum ChronoUnit {
    HOUR("Hour"),
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    SEMI_ANNUAL("Semiannual"),
    ANNUAL("Annual"),
    MAX("Max");

    public static ChronoUnit[] units = ChronoUnit.values();

    private final String name;

    ChronoUnit(String name) {
        this.name = name;
    }

    public ChronoUnit unit(String name) {
        return valueOf(name);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
