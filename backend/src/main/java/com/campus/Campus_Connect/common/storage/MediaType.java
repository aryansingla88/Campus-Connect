package com.campus.Campus_Connect.common.storage;

public enum MediaType {

    POST_IMAGE("posts"),
    EVENT_POSTER("posters"),
    PROFILE_IMAGE("profiles");

    private final String directory;

    MediaType(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}