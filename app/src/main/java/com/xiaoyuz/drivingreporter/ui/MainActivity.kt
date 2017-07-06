package com.xiaoyuz.drivingreporter.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import com.xiaoyuz.drivingreporter.R
import com.xiaoyuz.drivingreporter.extensions.manageNotificationAuthorize
import com.xiaoyuz.drivingreporter.service.NotificationCollectorService
import com.xiaoyuz.drivingreporter.tts.TTSManager

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private var mBackBtnFirstTime: Long = 0

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
            toast("助手停止")
            manageNotificationAuthorize(false)
            TTSManager.stop()
        }

        pause.setOnClickListener {
            TTSManager.pause()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val secondTime = System.currentTimeMillis()
                if (secondTime - mBackBtnFirstTime > 2000) {
                    toast("再按一次退出程序")
                    mBackBtnFirstTime = secondTime
                    return true
                } else {
                    System.exit(0)
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}
