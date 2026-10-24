package info.muge.appshare.utils

import android.app.Activity
import android.content.Context
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission

object PermissionExts {
    private const val KEY_SHOW_APP = "show_app"

    /**
     * 当前是否已经可以直接读取应用列表。
     * 与 SDKMonitor 一致：Manifest 声明了 QUERY_ALL_PACKAGES 的设备上无需额外授权，直接读取；
     * 只有系统确实还要求授权（如部分国产 ROM）时才需要弹授权。
     * 一旦检测到可读，顺手把本地标记置为 true，供其他地方沿用。
     */
    fun canReadInstalledApps(context: Context): Boolean {
        val sp = SPUtil.getGlobalSharedPreferences(context)
        if (sp.getBoolean(KEY_SHOW_APP, false)) return true
        // 只用系统自带 API 判断：能读到足够多的应用，说明系统已放行
        // （被限制时只能看到自己和寥寥几个系统包）
        val granted = try {
            context.packageManager.getInstalledPackages(0).size > 15
        } catch (e: Exception) {
            false
        }
        if (granted) sp.edit().putBoolean(KEY_SHOW_APP, true).apply()
        return granted
    }

    /*读取应用列表权限*/
    fun requestreadInstallApps(activity: Activity, listener: () -> Unit) {
        if (canReadInstalledApps(activity)) {
            listener()
            return
        }
        if (!SPUtil.getGlobalSharedPreferences(activity).getBoolean("show_app", false)) {
            try {
                XXPermissions.with(activity).permission(PermissionLists.getGetInstalledAppsPermission()).request(object : OnPermissionCallback {

                    override fun onResult(
                        grantedList: List<IPermission?>,
                        deniedList: List<IPermission?>
                    ) {
                        if (grantedList.isNotEmpty() && deniedList.isEmpty()) {
                            SPUtil.getGlobalSharedPreferences(activity).edit().putBoolean("show_app", true).apply()
                            listener()
                        } else {
                            "授权失败，请前往App设置页面手动授权".toast()
                        }
                    }
                })
            } catch (e: Exception) {
                "授权失败，请前往App设置页面手动授权".toast()
            }
        }
    }

}
