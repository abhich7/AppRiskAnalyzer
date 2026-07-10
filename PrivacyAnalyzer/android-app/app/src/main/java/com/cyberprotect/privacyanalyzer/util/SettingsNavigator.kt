package com.cyberprotect.privacyanalyzer.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Opens the correct Android Settings screen for a package or an individual
 * permission, adapting the Intent to the device's Android version. Every
 * attempt falls back to the generic App Info page if the OS doesn't support
 * a more specific screen — so "Manage Permission" always does *something*.
 */
object SettingsNavigator {

    /** Opens the app's main "App info" details page. */
    fun openAppDetails(context: Context, packageName: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeStart(context, intent)
    }

    /**
     * Opens the most specific permission-management screen available for
     * [permissionId] on this app. Android 11+ (API 30) exposes a dedicated
     * per-permission screen; older versions fall back to the app's full
     * permission manager, then to App Info.
     */
    fun openManagePermission(context: Context, packageName: String, permissionId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val perPermission = Intent(Settings.ACTION_APP_PERMISSION_SETTINGS).apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
                putExtra("android.intent.extra.PERMISSION_NAME", permissionId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(context, perPermission)) return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionManager = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(context, permissionManager)) return
        }

        openAppDetails(context, packageName)
    }

    /** Opens the OS-wide "All apps with this permission" manager, when available (Android 11+). */
    fun openPermissionManagerForType(context: Context, permissionGroup: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Intent.ACTION_MANAGE_PERMISSION_APPS).apply {
                putExtra(Intent.EXTRA_PERMISSION_GROUP_NAME, permissionGroup)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(context, intent)) return
        }
        // Fallback: general app permission settings landing page.
        val fallback = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeStart(context, fallback)
    }

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun safeStart(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No settings app available to handle this intent; nothing more we can do.
        }
    }
}
