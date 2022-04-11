package com.badzzz.apksigninfoextractor.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageUtils {

    public static boolean copyFile(File src, File dst) {
        FileInputStream in = null;
        FileOutputStream out = null;
        boolean isSuccess = false;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            isSuccess = true;
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return isSuccess;
    }


}
