package com.xiaoyuz.drivingreporter.tts

import android.speech.tts.TextToSpeech
import com.xiaoyuz.drivingreporter.App
import java.util.*

object TTSManager {

    val LANG_MISSING_DATA = TextToSpeech.LANG_MISSING_DATA
    val LANG_NOT_SUPPORTED = TextToSpeech.LANG_NOT_SUPPORTED
    val TTS_FAILED = 404
    val TTS_ALREADY_EXISTS = 502

    private var mTts: TextToSpeech? = null

    fun start(callback: (Int?) -> Unit) {
        if (mTts == null) {
            mTts = TextToSpeech(App.context, { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = mTts?.setLanguage(Locale.CHINESE)
                    when(result) {
                        TextToSpeech.LANG_MISSING_DATA -> callback(LANG_MISSING_DATA)
                        TextToSpeech.LANG_NOT_SUPPORTED -> callback(LANG_NOT_SUPPORTED)
                        else -> callback(result)
                    }
                } else {
                    callback(TTS_FAILED)
                }
            })
        } else {
            callback(TTS_ALREADY_EXISTS)
        }
    }

    fun pause() {
        mTts?.stop()
    }

    fun stop() {
        mTts?.shutdown()
        mTts = null
    }

    fun speak(string: String) {
        mTts?.speak(string, TextToSpeech.QUEUE_ADD, null)
    }

    fun isWorking(): Boolean {
        return mTts != null
    }
}