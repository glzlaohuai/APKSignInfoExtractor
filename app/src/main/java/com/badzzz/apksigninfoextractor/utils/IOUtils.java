package com.badzzz.apksigninfoextractor.utils;

public class IOUtils {

    public static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (java.io.IOException e) {
                // ignore
            }
        }
    }
}
