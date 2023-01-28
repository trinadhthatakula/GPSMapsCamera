package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera

import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors

class GPSCameraApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}