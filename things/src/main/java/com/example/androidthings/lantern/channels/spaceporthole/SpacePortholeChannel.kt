package com.example.androidthings.lantern.channels.spaceporthole

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.space_porthole_channel.*
import processing.android.PFragment

/**
 * Uses your local latitude/longitude, the system time and a database of stars and constellations
 * to project the stars above you right now.
 *
 * Developed as a Processing sketch and embedding into a Lantern channel using the
 * Processing-Android project.
 *
 */
class SpacePortholeChannel : Channel() {
    private val sketch by lazy {
        SpacePortholeApplet().apply {
            latitude = config.settings.optDouble("latitude", 0.0).toFloat()
            longitude = config.settings.optDouble("longitude", 0.0).toFloat()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.space_porthole_channel, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager!!.beginTransaction()
                .add(R.id.sketchViewGroup, PFragment(sketch))
                .commit()
    }

    override fun onResume() {
        super.onResume()
        showNorthMarkerAndFadeOut()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            // don't waste cycles rendering in the background
            sketch.surface.pauseThread()
        } else {
            sketch.surface.resumeThread()
            showNorthMarkerAndFadeOut()
        }

        // work around a bug where this view appears atop other surface views even when the
        // fragment is hidden
        sketch.surface.surfaceView.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }

    private var northMarkerFadeOutAnimator: ObjectAnimator? = null

    private fun showNorthMarkerAndFadeOut() {
        if (northMarkerImageView == null) return

        northMarkerFadeOutAnimator?.cancel()
        northMarkerFadeOutAnimator = null

        northMarkerImageView.alpha = 0.7f

        northMarkerFadeOutAnimator = ObjectAnimator.ofFloat(northMarkerImageView, "alpha", 0.7f, 0f).apply {
            propertyName = "alpha"
            duration = 1000
            startDelay = 9000
            start()
        }
    }
}