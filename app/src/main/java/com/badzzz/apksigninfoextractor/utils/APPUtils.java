package com.badzzz.apksigninfoextractor.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class APPUtils {

    public static String getSourceAPKFile(Context context, String pkg) {
        try {
            return context.getPackageManager().getApplicationInfo(pkg, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate generateCertificate(byte[] bytes) {
        try {
            InputStream certStream = new ByteArrayInputStream(bytes);
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);

            return x509Cert;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }


    public static byte[] getAppSignatureBytes(Context context, String pkg) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
            return packageInfo.signatures[0].toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public enum DigestAlgorithmType {
        MD5, SHA1, SHA256
    }

    public static String sign(byte[] bytes, DigestAlgorithmType algorithmType) {
        String key = null;
        switch (algorithmType) {
            case MD5:
                key = "MD5";
                break;
            case SHA1:
                key = "SHA1";
                break;
            case SHA256:
                key = "SHA256";
                break;
        }

        final StringBuilder toRet = new StringBuilder();
        try {
            final MessageDigest md = MessageDigest.getInstance(key);
            md.update(bytes);

            final byte[] digest = md.digest();
            for (int i = 0; i < digest.length; i++) {
                if (i != 0) toRet.append(":");
                int b = digest[i] & 0xff;
                String hex = Integer.toHexString(b);
                if (hex.length() == 1) toRet.append("0");
                toRet.append(hex);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return toRet.toString().toUpperCase();
    }


}
