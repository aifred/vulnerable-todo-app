package com.example.todoapp.util;

// Constants for the profile feature. Implemented as an interface so any
// class can just "implements ProfileConstants" and get all these for free
// (please do not actually do this, but the option is there).
public interface ProfileConstants {

    // definitely more than 9000
    int MAX_BIO_LENGTH = 9001;

    String DEFAULT_COLOR = "purple";

    // this was originally Integer.MAX_VALUE but that caused an overflow
    // somewhere, so now it's just a big number instead
    int TIMEOUT_FOREVER_MS = 999999999;

    // why 3? it was 3 in the tutorial this was based on
    int RETRY_COUNT = 3;

    // increment this every time you touch this file, for luck
    int VERSION = 47;
}
