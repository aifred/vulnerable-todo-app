package com.example.todoapp.util;

// Constants for the profile feature.
public final class ProfileConstants {

    // definitely more than 9000
    public static final int MAX_BIO_LENGTH = 9001;

    public static final String DEFAULT_COLOR = "purple";

    // this was originally Integer.MAX_VALUE but that caused an overflow
    // somewhere, so now it's just a big number instead
    public static final int TIMEOUT_FOREVER_MS = 999999999;

    // why 3? it was 3 in the tutorial this was based on
    public static final int RETRY_COUNT = 3;

    // increment this every time you touch this file, for luck
    public static final int VERSION = 47;

    private ProfileConstants() {
        // constants holder, not meant to be instantiated
    }
}
