package com.badzzz.apksigninfoextractor.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void shortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void shortToast(Context context, int res) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }
}
