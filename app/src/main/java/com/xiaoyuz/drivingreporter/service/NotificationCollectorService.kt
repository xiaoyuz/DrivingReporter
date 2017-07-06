package com.xiaoyuz.drivingreporter.service

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import com.squareup.otto.Subscribe
import com.xiaoyuz.drivingreporter.extensions.EventDispatcher
import com.xiaoyuz.drivingreporter.extensions.ServicePauseEvent
import com.xiaoyuz.drivingreporter.extensions.ServiceStopEvent
import org.jetbrains.anko.toast
import java.util.*

class NotificationCollectorService: NotificationListenerService() {

    inner class EventHandler {
        @Subscribe
        fun onServicePauseEvent(event: ServicePauseEvent) {
            mTts?.stop()
        }

        @Subscribe
        fun onServiceStopEvent(event: ServiceStopEvent) {
            toast("助手停止")
            mTts?.shutdown()
        }
    }

    private var mTts: TextToSpeech? = null
    private val mEventHandler: EventHandler = EventHandler()

    override fun onBind(intent: Intent?): IBinder {
        initListeners()
        EventDispatcher.register(mEventHandler)
        toast("助手启动")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        EventDispatcher.unregister(mEventHandler)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTts?.shutdown()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.tencent.mm") {
            mTts?.speak(genSpokenString(sbn.notification?.extras?.get("android.title").toString(),
                    sbn.notification?.extras?.get("android.text").toString()),
                    TextToSpeech.QUEUE_ADD, null)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
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
                result = head + "说: " + content
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

    private fun initListeners() {
        mTts = TextToSpeech(this, { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = mTts?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toast("Missing language data")
                }
            } else {
                toast("tts failed.")
            }
        })
    }
}