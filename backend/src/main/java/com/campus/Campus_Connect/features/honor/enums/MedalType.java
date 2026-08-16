package com.campus.Campus_Connect.features.honor.enums;

public enum MedalType {

    GOLD("Gold Medal"),
    SILVER("Silver Medal"),
    BRONZE("Bronze Medal");

    private final String title;

    MedalType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}