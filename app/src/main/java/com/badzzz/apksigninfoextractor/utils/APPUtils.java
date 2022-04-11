package com.badzzz.apksigninfoextractor.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class APPUtils {

    public static String getSourceAPKFile(Context context, String pkg) {
        try {
            return context.getPackageManager().getApplicationInfo(pkg, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
