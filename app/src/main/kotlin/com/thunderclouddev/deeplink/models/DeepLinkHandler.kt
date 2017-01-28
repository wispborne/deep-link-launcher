package com.thunderclouddev.deeplink.models

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.thunderclouddev.deeplink.empty

/**
 * Created by David Whitman on 23 Jan, 2017.
 */
data class DeepLinkHandler(val packageName: String, val appName: String? = null) {
    constructor(resolveInfo: ResolveInfo, packageManager: PackageManager)
            : this(resolveInfo.resolvePackageName ?: String.empty, resolveInfo.loadLabel(packageManager).toString())
}