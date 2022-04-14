package com.badzzz.apksigninfoextractor.utils;

import android.content.Context;
import android.content.Intent;

public class ShareUtils {

    public static void shareSimgpleText(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }
}
