package info.muge.appshare.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import info.muge.appshare.utils.toast

object PermissionExts {

    /**
     * 检查是否能够读取应用列表
     * 注意：在 Android 11+ 中，QUERY_ALL_PACKAGES 属于普通权限，无法通过代码弹窗动态申请，
     * 只要 Manifest 声明了即可。但在部分国产 ROM 中，系统会拦截，导致获取为空。
     */
    fun hasReadInstalledAppsPermission(context: android.content.Context): Boolean {
        return try {
            val packages = context.packageManager.getInstalledPackages(0)
            packages.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 请求读取应用列表权限（跳转到系统设置页手动开启）
     */
    fun requestReadInstallApps(activity: Activity, listener: () -> Unit) {
        if (hasReadInstalledAppsPermission(activity)) {
            // 如果已经有权限，直接回调
            listener()
            return
        }

        // 如果没有权限，跳转到 App 设置详情页让用户手动授权
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
            "请在设置中允许“应用列表”权限".toast()
        } catch (e: Exception) {
            "无法打开设置页面，请前往系统设置手动授权".toast()
        }
    }
}
