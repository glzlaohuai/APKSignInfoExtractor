package com.badzzz.apksigninfoextractor.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.badzzz.apksigninfoextractor.XApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsInfoHandler {


    private final static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void getAllInstalledApps(AppsInfoHandlerListener listener) {
        singleThreadExecutor.execute(() -> {
                    List<AppInfo> result = getAllInstalledApps(XApplication.getInstance());
                    listener.onAppsInfoHandler(result);
                }
        );
    }

    private final static List<AppInfo> getAllInstalledApps(Context context) {

        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        List<AppInfo> result = new ArrayList<>(installedApplications.size());

        for (ApplicationInfo applicationInfo : installedApplications) {
            boolean isSys = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            String appName = applicationInfo.loadLabel(packageManager).toString();
            String appPackage = applicationInfo.packageName;
            int versinoCode = 0;
            String versionName = null;

            try {
                versinoCode = packageManager.getPackageInfo(appPackage, 0).versionCode;
                versionName = packageManager.getPackageInfo(appPackage, 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Drawable icon = applicationInfo.loadIcon(packageManager);

            AppInfo appInfo = new AppInfo(isSys, appName, appPackage, versinoCode, versionName, icon);
            result.add(appInfo);
        }

        return result;

    }

    public interface AppsInfoHandlerListener {
        void onAppsInfoHandler(List<AppInfo> appsInfo);
    }

    public static class AppInfo implements Parcelable {
        public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
            @Override
            public AppInfo createFromParcel(Parcel in) {
                return new AppInfo(in);
            }

            @Override
            public AppInfo[] newArray(int size) {
                return new AppInfo[size];
            }
        };
        private boolean isSys;
        private String appName;
        private String appPackage;
        private int versinoCode;
        private String versionName;
        private Drawable icon;

        public AppInfo(boolean isSys, String appName, String appPackage, int versinoCode, String versionName, Drawable icon) {
            this.isSys = isSys;
            this.appName = appName;
            this.appPackage = appPackage;
            this.versinoCode = versinoCode;
            this.versionName = versionName;
            this.icon = icon;
        }

        protected AppInfo(Parcel in) {
            isSys = in.readByte() != 0;
            appName = in.readString();
            appPackage = in.readString();
            versinoCode = in.readInt();
            versionName = in.readString();
        }

        public boolean isSys() {
            return isSys;
        }

        public String getAppName() {
            return appName;
        }

        public String getAppPackage() {
            return appPackage;
        }

        public int getVersinoCode() {
            return versinoCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public Drawable getIcon() {
            return icon;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isSys ? 1 : 0));
            dest.writeString(appName);
            dest.writeString(appPackage);
            dest.writeInt(versinoCode);
            dest.writeString(versionName);
        }

        public void restoreIcon(Context context) {
            if (icon == null) {
                try {
                    icon = context.getPackageManager().getApplicationIcon(appPackage);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
