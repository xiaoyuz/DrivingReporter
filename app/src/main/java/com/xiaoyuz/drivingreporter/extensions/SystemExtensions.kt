package com.xiaoyuz.drivingreporter.extensions

import android.content.ComponentName
import android.content.pm.PackageManager
import com.xiaoyuz.drivingreporter.App
import com.xiaoyuz.drivingreporter.service.NotificationCollectorService

fun manageNotificationAuthorize(enable: Boolean) {
    val service = ComponentName(App.context, NotificationCollectorService().javaClass)
    val newState = if (enable) PackageManager
            .COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    App.context?.packageManager?.setComponentEnabledSetting(service, newState,
            PackageManager.DONT_KILL_APP)
}
