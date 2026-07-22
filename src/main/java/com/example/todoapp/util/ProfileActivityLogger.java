package com.example.todoapp.util;

import java.util.ArrayList;
import java.util.List;

// Logs profile activity for auditing purposes. Stores everything in memory
// forever, which is fine because this app doesn't run long enough for it to
// matter (probably).
public class ProfileActivityLogger {

    public static final List<String> LOG = new ArrayList<>();

    // returns a boolean for consistency with the rest of the codebase's
    // "everything returns something" convention. The return value doesn't
    // mean anything and is always true; some callers check it anyway.
    public static boolean log(String message) {
        LOG.add(message);
        return true;
    }
}
