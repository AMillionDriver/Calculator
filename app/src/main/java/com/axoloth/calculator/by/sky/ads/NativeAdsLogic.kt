package com.axoloth.calculator.by.sky.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.axoloth.calculator.by.sky.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object NativeAdsLogic {

    private var currentNativeAd: NativeAd? = null

    fun loadAndShowNativeAd(context: Context, container: FrameLayout) {
        // Cek apakah diizinkan oleh Remote Config
        if (!com.axoloth.calculator.by.sky.ads.remote.AdsRemoteConfig.isNativeAdEnabled()) {
            container.visibility = View.GONE
            return
        }

        val adUnitId = com.axoloth.calculator.by.sky.ads.remote.AdsRemoteConfig.getNativeAdUnitId()

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                // Bersihkan iklan lama jika ada
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
                
                // Jika iklan berhasil dimuat, pasang ke UI
                displayNativeAd(context, container, nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    container.visibility = View.GONE
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    /**
     * Panggil ini saat fragment/layar dihancurkan untuk mencegah memory leak.
     */
    fun destroyAd() {
        currentNativeAd?.destroy()
        currentNativeAd = null
    }

    private fun displayNativeAd(context: Context, container: FrameLayout, nativeAd: NativeAd) {
        val inflater = LayoutInflater.from(context)
        val adView = inflater.inflate(R.layout.layout_native_ad, null) as NativeAdView

        // Map UI elements
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set Data
        (adView.headlineView as TextView).text = nativeAd.headline
        
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }

        // Penting: Masukkan nativeAd ke adView
        adView.setNativeAd(nativeAd)

        // Masukkan ke Container
        container.removeAllViews()
        container.addView(adView)
        container.visibility = View.VISIBLE
    }
}
