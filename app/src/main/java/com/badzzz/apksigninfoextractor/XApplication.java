package com.badzzz.apksigninfoextractor;

import android.app.Application;

import com.badzzz.apksigninfoextractor.utils.ADUtils;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XApplication extends Application {

    private static final String TAG = "XApplication";

    public static XApplication instance;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static XApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                cleanupCopiedFiles();
            }
        });
        admobInit();
        initFlurry();
    }

    private void initFlurry() {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "ST9BPB97YSCF3P5MC77B");
    }

    private void admobInit() {
        //        ADUtils.setIsForTest(true);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        ADUtils.loadInterstitial(this, Others.AD_UNIT_INTERSTITIAL_VIEW);
    }


    private void cleanupCopiedFiles() {
        File rootDir = new File(getFilesDir(), Others.SHARED_APKS_DIR);
        if (rootDir.exists()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }


}
