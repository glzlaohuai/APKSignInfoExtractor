package com.badzzz.apksigninfoextractor.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AppViewUtils {

    public static void viewAPPInPlayStore(Context context, String pkgName) throws ActivityNotFoundException {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkgName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean error = false;
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
            error = true;
        }

        if (error) {
            context.startActivity(intent);
        }
    }


    public static void viewAppInSettings(Context context, String pkgName) throws ActivityNotFoundException {
        // Open the specific App Info page:
        Intent intent = new Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + pkgName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
