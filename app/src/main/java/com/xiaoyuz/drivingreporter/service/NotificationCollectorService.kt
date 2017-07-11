package com.xiaoyuz.drivingreporter.service

import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.xiaoyuz.drivingreporter.App
import com.xiaoyuz.drivingreporter.R
import com.xiaoyuz.drivingreporter.tts.TTSManager
import org.jetbrains.anko.toast

class NotificationCollectorService: NotificationListenerService() {

    override fun onBind(intent: Intent?): IBinder {
        TTSManager.start { result ->
            when(result) {
                TTSManager.TTS_ALREADY_EXISTS -> toast("Already started.")
                TTSManager.TTS_FAILED -> toast("tts failed.")
                TTSManager.LANG_NOT_SUPPORTED -> toast("Language not supported.")
                TTSManager.LANG_MISSING_DATA -> toast("Missing language data")
            }
        }
        toast("助手启动")
        startForegroundNotificationBar()

        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        TTSManager.stop()
        toast("助手停止")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.tencent.mm") {
            TTSManager.speak(genSpokenString(sbn.notification?.extras?.get("android.title").toString(),
                    sbn.notification?.extras?.get("android.text").toString()))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    private fun startForegroundNotificationBar() {
        val notification: Notification = Notification.Builder(App.context)
                .setContentTitle("DriverReporter").setContentText("正在工作")
                .setSmallIcon(R.drawable.notification_icon_background).build()
        startForeground(1, notification)
    }

    private fun genSpokenString(title: String, text: String): String {
        var result = ""
        val splits = text.split(":")
        if (splits.size == 1) {
//            if (text.length > 50) {
//                result = text.dropLast(text.length - 49) + " 以下省略"
//            }
            result = title + "说: " + text
        } else {
            val head = splits[0]
            val content = text.drop(head.length)
            val headSplits = head.split("]")
            if (headSplits.size == 1) {
                if (title == head) {
                    result = head + "说: " + content
                } else {
                    result = title + " 群的 " + head + "说: " + content
                }
            } else {
                val author = headSplits[1]
                if (author == title) {
                    result = author + "说: " + content
                } else {
                    result = title + " 群的 " + author + "说: " + content
                }
            }
        }
        return result
    }
}