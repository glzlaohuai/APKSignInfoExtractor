package com.badzzz.apksigninfoextractor.utils;

import android.util.Log;

import com.badzzz.apkfileextractor.BuildConfig;

public class Logger {
    private static String logPrefix = "Logger";
    private static boolean enabled = BuildConfig.DEBUG;

    public static void setLogPrefix(String prefix) {
        Logger.logPrefix = prefix;
    }


    public static void i(String tag, String msg) {
        if (enabled) {
            tag = logPrefix + " - " + tag;
            Log.i(tag, msg);
        }
    }


}
