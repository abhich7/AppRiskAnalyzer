package com.cyberprotect.privacyanalyzer.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.model.PermissionGrant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads installed apps and their live permission-grant status using the
 * official PackageManager API. No root, no special permissions beyond
 * QUERY_ALL_PACKAGES. Everything runs off the main thread.
 */
class AppScanner(private val context: Context) {

    /** Full scan of every non-system app. Runs on Dispatchers.IO. */
    suspend fun scanAll(includeSystemApps: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = getInstalledPackages(pm)

        packages.mapNotNull { pkgInfo ->
            val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
            if (!includeSystemApps && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                return@mapNotNull null
            }
            buildAppInfo(pm, pkgInfo, appInfo)
        }.sortedByDescending { it.riskScore }
    }

    /** Re-reads a single package — used right after the user returns from Settings. */
    suspend fun rescanPackage(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            val pkgInfo = getPackageInfo(pm, packageName)
            val appInfo = pkgInfo.applicationInfo ?: return@withContext null
            buildAppInfo(pm, pkgInfo, appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /** Total installed app count (system + user), for the "Total Installed Apps" stat. */
    suspend fun totalInstalledCount(): Int = withContext(Dispatchers.IO) {
        getInstalledPackages(context.packageManager).size
    }

    private fun buildAppInfo(
        pm: PackageManager,
        pkgInfo: PackageInfo,
        appInfo: ApplicationInfo
    ): AppInfo {
        val requested = pkgInfo.requestedPermissions?.toList() ?: emptyList()
        val flags = pkgInfo.requestedPermissionsFlags

        val grants = requested.mapIndexed { index, permissionId ->
            val granted = flags != null && index < flags.size &&
                (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            PermissionGrant(permissionId, granted)
        }

        val riskScore = RiskCalculator.calculateAppRisk(grants)

        return AppInfo(
            appName = appInfo.loadLabel(pm).toString(),
            packageName = pkgInfo.packageName,
            icon = appInfo.loadIcon(pm),
            category = categoryLabel(appInfo),
            versionName = pkgInfo.versionName,
            firstInstallTime = pkgInfo.firstInstallTime,
            lastUpdateTime = pkgInfo.lastUpdateTime,
            allPermissions = grants,
            riskScore = riskScore,
            riskLevel = RiskCalculator.levelFor(riskScore)
        )
    }

    private fun categoryLabel(appInfo: ApplicationInfo): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Other"
        return when (appInfo.category) {
            ApplicationInfo.CATEGORY_GAME -> "Games"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            ApplicationInfo.CATEGORY_AUDIO -> "Music & Audio"
            ApplicationInfo.CATEGORY_VIDEO -> "Video & Streaming"
            ApplicationInfo.CATEGORY_MAPS -> "Navigation"
            ApplicationInfo.CATEGORY_IMAGE -> "Photography"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            else -> "Other"
        }
    }

    @Suppress("DEPRECATION")
    private fun getInstalledPackages(pm: PackageManager): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfo(pm: PackageManager, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }
    }
}
