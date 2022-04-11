package com.badzzz.apksigninfoextractor.utils;

import android.app.Activity;
import android.content.Context;
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
import java.util.Set;

import androidx.annotation.NonNull;

public class ADUtils {

    private static final String TEST_ADUNIT_ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_ADUNIT_ID_BANNER = "ca-app-pub-3940256099942544/9214589741";

    private static boolean isForTest = false;

    private static final String TAG = "ADUtils";

    private static Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private static Set<String> inLoadingInterstitialIDSet = new HashSet<>();

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
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    inLoadingInterstitialIDSet.remove(finalUnitID);
                    Logger.i(TAG, "adLoadfailed: " + loadAdError);
                }
            });
            return true;
        }
    }


}
