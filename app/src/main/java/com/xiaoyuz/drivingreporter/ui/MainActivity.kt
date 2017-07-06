package com.xiaoyuz.drivingreporter.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.xiaoyuz.drivingreporter.R
import com.xiaoyuz.drivingreporter.extensions.EventDispatcher
import com.xiaoyuz.drivingreporter.extensions.ServicePauseEvent
import com.xiaoyuz.drivingreporter.extensions.ServiceStopEvent
import com.xiaoyuz.drivingreporter.extensions.manageNotificationAuthorize
import com.xiaoyuz.drivingreporter.service.NotificationCollectorService

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manageNotificationAuthorize(false)

        start.setOnClickListener {
            manageNotificationAuthorize(true)
            val string = Settings.Secure.getString(contentResolver,
                    "enabled_notification_listeners")

            if (!string.contains(NotificationCollectorService::javaClass.name)) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }

        stop.setOnClickListener {
            manageNotificationAuthorize(false)
            EventDispatcher.post(ServiceStopEvent())
        }

        pause.setOnClickListener {
            EventDispatcher.post(ServicePauseEvent())
        }
    }
}
