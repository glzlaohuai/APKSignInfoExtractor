package com.badzzz.apksigninfoextractor.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

public class ADUtils {

    private static final String TEST_ADUNIT_ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_ADUNIT_ID_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private static final String TAG = "ADUtils";
    private static boolean isForTest = false;
    private static Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private static Set<String> inLoadingInterstitialIDSet = new HashSet<>();
    private static ADLoadListenerGroup interstitialLoadListenerGroup = new ADLoadListenerGroup();

    public static void setIsForTest(boolean isForTest) {
        ADUtils.isForTest = isForTest;
    }

    public static void setupBannerAD(@NonNull Context context, @NonNull ViewGroup adContainer, @NonNull String unitID) {
        if (isForTest) {
            unitID = TEST_ADUNIT_ID_BANNER;
        }

        adContainer.removeAllViews();
        AdView adView = new AdView(context);
        adView.setAdUnitId(unitID);
        adContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize(context);
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private static AdSize getAdSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    public synchronized static boolean isInterstitialAvailable(String unitID) {
        if (isForTest) {
            unitID = TEST_ADUNIT_ID_INTERSTITIAL;
        }
        return interstitialAdMap.containsKey(unitID);
    }

    public synchronized static boolean showInterstitial(String unitID, Activity activity) {
        if (isForTest) {
            unitID = TEST_ADUNIT_ID_INTERSTITIAL;
        }
        if (isInterstitialAvailable(unitID)) {
            interstitialAdMap.get(unitID).show(activity);
            interstitialAdMap.remove(unitID);
            return true;
        }
        return false;
    }

    public synchronized static boolean loadInterstitial(Context context, String unitID) {
        if (isForTest) {
            unitID = TEST_ADUNIT_ID_INTERSTITIAL;
        }
        if (interstitialAdMap.containsKey(unitID) || inLoadingInterstitialIDSet.contains(unitID)) {
            return false;
        } else {
            inLoadingInterstitialIDSet.add(unitID);
            AdRequest adRequest = new AdRequest.Builder().build();
            String finalUnitID = unitID;
            InterstitialAd.load(context, unitID, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    inLoadingInterstitialIDSet.remove(finalUnitID);
                    interstitialAdMap.put(finalUnitID, interstitialAd);
                    interstitialLoadListenerGroup.onLoaded(finalUnitID, interstitialAd);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    inLoadingInterstitialIDSet.remove(finalUnitID);
                    Logger.i(TAG, "adLoadfailed: " + loadAdError);
                    interstitialLoadListenerGroup.onLoadFailed(finalUnitID, loadAdError.toString());
                }
            });
            return true;
        }
    }

    private synchronized static boolean isInterstitialADLoaded(String unitID) {
        return interstitialAdMap.containsKey(unitID);
    }

    public synchronized static void showInterstitialWithTimeout(Activity adActivity, String unitID, long ts) {
        if (adActivity == null) {
            return;
        }

        if (isInterstitialADLoaded(unitID)) {
            showInterstitial(unitID, adActivity);
        } else {
            Timer timer = new Timer();
            AtomicBoolean timeout = new AtomicBoolean(false);

            ADLoadListener listener = new ADLoadListener() {
                @Override
                public void onLoaded(String id, Object ad) {
                    interstitialAdMap.remove(this);
                    if (!timeout.get()) {
                        timer.cancel();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            if (!adActivity.isDestroyed() && !adActivity.isFinishing()) {
                                showInterstitial(unitID, adActivity);
                            }
                        } else {
                            showInterstitial(unitID, adActivity);
                        }
                    }
                }

                @Override
                public void onLoadFailed(String id, String err) {
                    interstitialLoadListenerGroup.remove(this);
                    if (!timeout.get()) {
                        timer.cancel();
                    }
                }
            };

            interstitialLoadListenerGroup.add(listener);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timeout.set(true);
                    interstitialAdMap.remove(listener);
                }
            }, ts);


        }

    }

    public interface ADLoadListener<T> {
        void onLoaded(String id, T ad);

        void onLoadFailed(String id, String err);
    }


    public interface AdShowListener<T> {
        void onShowed(String id, T ad);

        void onClosed(String id, T ad);
    }

    public static class ADLoadListenerGroup implements ADLoadListener {

        private Queue<ADLoadListener> queue = new ConcurrentLinkedQueue();

        public void add(ADLoadListener listener) {
            queue.add(listener);
        }

        public void remove(ADLoadListener listener) {
            queue.remove(listener);
        }

        @Override
        public void onLoaded(String id, Object ad) {
            for (ADLoadListener listener : queue) {
                if (listener != null) {
                    listener.onLoaded(id, ad);
                }
            }
        }

        @Override
        public void onLoadFailed(String id, String err) {
            for (ADLoadListener listener : queue) {
                if (listener != null) {
                    listener.onLoadFailed(id, err);
                }
            }
        }
    }

}
