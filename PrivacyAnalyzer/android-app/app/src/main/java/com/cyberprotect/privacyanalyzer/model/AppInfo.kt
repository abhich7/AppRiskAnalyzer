package com.cyberprotect.privacyanalyzer.model

import android.graphics.drawable.Drawable

/** Five-tier risk classification, shared by apps, permissions, and the overall device score. */
enum class RiskLevel(val label: String, val floor: Int) {
    SAFE("Safe", 0),
    LOW("Low", 21),
    MEDIUM("Medium", 41),
    HIGH("High", 61),
    CRITICAL("Critical", 81);

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score >= CRITICAL.floor -> CRITICAL
            score >= HIGH.floor -> HIGH
            score >= MEDIUM.floor -> MEDIUM
            score >= LOW.floor -> LOW
            else -> SAFE
        }
    }
}

/** A single requested permission on an app, together with its live grant status. */
data class PermissionGrant(
    val permissionId: String,
    val isGranted: Boolean
)

/** Everything Privacy Analyzer knows about one installed application. */
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val category: String,
    val versionName: String?,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val allPermissions: List<PermissionGrant>,
    val riskScore: Int,
    val riskLevel: RiskLevel
) {
    /** Requested + granted dangerous permissions that Privacy Analyzer tracks. */
    val dangerousGranted: List<PermissionGrant>
        get() = allPermissions.filter { it.isGranted && PermissionCatalog.isTracked(it.permissionId) }

    /** Requested but not currently granted tracked permissions. */
    val dangerousNotGranted: List<PermissionGrant>
        get() = allPermissions.filter { !it.isGranted && PermissionCatalog.isTracked(it.permissionId) }

    /** Requested permissions Privacy Analyzer doesn't specifically weight (shown as "Normal"). */
    val normalPermissions: List<PermissionGrant>
        get() = allPermissions.filterNot { PermissionCatalog.isTracked(it.permissionId) }

    val dangerousPermissionCount: Int get() = dangerousGranted.size
}

/** A rolled-up snapshot of the whole device, recomputed after every scan. */
data class DeviceSnapshot(
    val overallScore: Int,
    val grade: Char,
    val totalInstalled: Int,
    val appsScanned: Int,
    val appsAtRisk: Int,          // HIGH or CRITICAL
    val criticalApps: Int,
    val dangerousPermissionCount: Int,
    val lastScanTime: Long,
    val riskLevel: RiskLevel
) {
    companion object {
        val EMPTY = DeviceSnapshot(0, 'A', 0, 0, 0, 0, 0, 0L, RiskLevel.SAFE)
    }
}
