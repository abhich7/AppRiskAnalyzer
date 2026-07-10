package com.cyberprotect.privacyanalyzer.data

import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.model.DeviceSnapshot
import com.cyberprotect.privacyanalyzer.model.PermissionCatalog
import com.cyberprotect.privacyanalyzer.model.PermissionGrant
import com.cyberprotect.privacyanalyzer.model.RiskLevel
import kotlin.math.roundToInt

/**
 * The weighted scoring engine. Only *granted* permissions count toward risk —
 * a requested-but-denied permission can't actually be used to collect data.
 */
object RiskCalculator {

    /** Sum of every tracked permission's weight, used to normalise scores to 0-100. */
    private val MAX_POSSIBLE_WEIGHT: Int = PermissionCatalog.ALL
        .groupBy { it.displayName }
        .values
        .sumOf { group -> group.maxOf { it.weight } }

    /** App-level risk: weighted sum of granted dangerous permissions, normalised to 0-100. */
    fun calculateAppRisk(grants: List<PermissionGrant>): Int {
        val grantedIds = grants.filter { it.isGranted }.map { it.permissionId }.toSet()

        // Avoid double-counting permissions that map to the same real-world capability
        // (e.g. READ_SMS and RECEIVE_SMS both represent "SMS access").
        val countedCapabilities = mutableSetOf<String>()
        var rawScore = 0
        for (id in grantedIds) {
            val meta = PermissionCatalog.metaFor(id) ?: continue
            if (countedCapabilities.add(meta.displayName)) {
                rawScore += meta.weight
            }
        }

        val normalised = (rawScore.toDouble() / MAX_POSSIBLE_WEIGHT.toDouble()) * 100.0
        return normalised.roundToInt().coerceIn(0, 100)
    }

    fun levelFor(score: Int): RiskLevel = RiskLevel.fromScore(score)

    fun gradeFor(score: Int): Char = when {
        score <= 20 -> 'A'
        score <= 40 -> 'B'
        score <= 60 -> 'C'
        score <= 80 -> 'D'
        else -> 'E'
    }

    /** Device-wide snapshot, aggregated across every scanned app. */
    fun buildDeviceSnapshot(apps: List<AppInfo>, totalInstalled: Int, scanTime: Long): DeviceSnapshot {
        if (apps.isEmpty()) {
            return DeviceSnapshot.EMPTY.copy(totalInstalled = totalInstalled, lastScanTime = scanTime)
        }

        val overall = (apps.sumOf { it.riskScore }.toDouble() / apps.size).roundToInt().coerceIn(0, 100)
        val atRisk = apps.count { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL }
        val critical = apps.count { it.riskLevel == RiskLevel.CRITICAL }
        val dangerousPerms = apps.flatMap { it.dangerousGranted.map { g -> g.permissionId } }.toSet().size

        return DeviceSnapshot(
            overallScore = overall,
            grade = gradeFor(overall),
            totalInstalled = totalInstalled,
            appsScanned = apps.size,
            appsAtRisk = atRisk,
            criticalApps = critical,
            dangerousPermissionCount = dangerousPerms,
            lastScanTime = scanTime,
            riskLevel = levelFor(overall)
        )
    }

    /** Human-readable recommendations for a single app, ordered by severity. */
    fun recommendationsFor(app: AppInfo): List<String> {
        val recs = mutableListOf<String>()
        val granted = app.dangerousGranted.mapNotNull { PermissionCatalog.metaFor(it.permissionId) }
            .sortedByDescending { it.weight }

        for (meta in granted.take(3)) {
            recs += when (meta.displayName) {
                "Microphone" -> "Revoke microphone access unless ${app.appName} needs it for calls or voice input."
                "SMS Messages" -> "Remove SMS access — this can expose one-time passcodes used for account logins."
                "Call Log" -> "Revoke call log access to protect your contact and communication history."
                "Contacts" -> "Limit contacts access — only grant it if ${app.appName} genuinely needs your address book."
                "Precise Location", "Approximate Location" -> "Switch location access to \"While using the app\" instead of \"Always\"."
                "Camera" -> "Revoke camera access when not actively using a camera feature."
                else -> "Review why ${app.appName} needs ${meta.displayName.lowercase()} access."
            }
        }

        if (app.riskLevel == RiskLevel.CRITICAL) {
            recs += "Consider uninstalling ${app.appName} if you rarely use it — it holds your highest-risk permission combination."
        }

        if (recs.isEmpty()) {
            recs += "${app.appName} currently holds no high-risk permissions. No action needed."
        }

        return recs
    }
}
