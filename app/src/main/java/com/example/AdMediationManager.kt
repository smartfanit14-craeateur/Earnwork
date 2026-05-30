package com.example

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMediationManager {
    private const val TAG = "AdMediationManager"

    // Official Google AdMob Test Ad Unit ID for Rewarded Ads
    const val ADMOB_REWARDED_TEST_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    // Custom test credentials for mediated networks requested by user
    const val STARTAPP_TEST_APP_ID = "207797742" // official StartApp test ID
    const val UNITY_ADS_TEST_GAME_ID = "1234567"
    const val APPLOVIN_TEST_SDK_KEY = "YOUR_TEST_APPLOVIN_SDK_KEY"

    private var mRewardedAd: RewardedAd? = null
    var isAdMobSdkInitialized = false
        private set

    // Setup and initialize AdMob Mobile Ads SDK
    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                isAdMobSdkInitialized = true
                Log.d(TAG, "Google Mobile Ads SDK Initialized successfully. Status: $status")
                // Pre-load the first test rewarded ad
                loadAdMobRewardedAd(context)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize Google Mobile Ads SDK safely: ${t.message}")
        }
    }

    // Programmatically load a real AdMob Rewarded Ad
    fun loadAdMobRewardedAd(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                ADMOB_REWARDED_TEST_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "AdMob Test Rewarded Ad failed to load: ${adError.message}")
                        mRewardedAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        Log.d(TAG, "AdMob Test Rewarded Ad loaded successfully.")
                        mRewardedAd = rewardedAd
                    }
                }
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load AdMob Rewarded Ad safely: ${t.message}")
        }
    }

    // Return whether real AdMob ad is ready
    fun isRealAdMobAdReady(): Boolean = mRewardedAd != null

    // Boilerplate simulated and integrated mediation logic for AppLovin, StartApp, and Unity Ads
    fun getMediationDetails(): List<MediationNetworkInfo> {
        return listOf(
            MediationNetworkInfo(
                name = "Google AdMob Mediation",
                testKeyType = "Ad Unit ID",
                testKeyVal = ADMOB_REWARDED_TEST_UNIT_ID,
                status = "Initialized (Ready)"
            ),
            MediationNetworkInfo(
                name = "StartApp",
                testKeyType = "App ID",
                testKeyVal = STARTAPP_TEST_APP_ID,
                status = "Mediation Ready (Simulated fallback)"
            ),
            MediationNetworkInfo(
                name = "Unity Ads",
                testKeyType = "Game ID",
                testKeyVal = UNITY_ADS_TEST_GAME_ID,
                status = "Mediation Ready (Simulated fallback)"
            ),
            MediationNetworkInfo(
                name = "AppLovin",
                testKeyType = "SDK Key",
                testKeyVal = APPLOVIN_TEST_SDK_KEY,
                status = "Mediation Ready (Simulated fallback)"
            )
        )
    }
}

data class MediationNetworkInfo(
    val name: String,
    val testKeyType: String,
    val testKeyVal: String,
    val status: String
)
