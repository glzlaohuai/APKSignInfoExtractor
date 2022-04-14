package com.badzzz.apksigninfoextractor.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipUtils {

    public static boolean copy(Context context, String content) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("app-sign", content);
            clipboardManager.setPrimaryClip(clipData);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}
