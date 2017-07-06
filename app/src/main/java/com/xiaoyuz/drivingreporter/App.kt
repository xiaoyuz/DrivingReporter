package com.xiaoyuz.drivingreporter

import android.content.Context

object App {

    var context: Context? = null

    fun initialize(context: Context) {
        this.context = context
    }
}