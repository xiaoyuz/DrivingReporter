package com.xiaoyuz.drivingreporter.engine

import android.app.Application
import com.xiaoyuz.drivingreporter.App

class DrivingReporterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        App.initialize(this)
    }
}