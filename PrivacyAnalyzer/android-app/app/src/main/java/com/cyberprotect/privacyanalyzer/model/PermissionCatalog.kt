package com.cyberprotect.privacyanalyzer.model

/**
 * Metadata for a single dangerous / trackable Android permission.
 * weight drives the risk-scoring algorithm in RiskCalculator.
 */
data class PermissionMeta(
    val id: String,               // Android permission constant, e.g. android.permission.CAMERA
    val displayName: String,      // Human friendly name, e.g. "Camera"
    val emoji: String,            // Icon shown in cards
    val why: String,              // Why it's considered dangerous
    val weight: Int                // Contribution to an app's risk score
)

/**
 * Central source of truth for every permission Privacy Analyzer understands.
 * Weights follow the spec: Microphone 20, SMS 20, Call Log 18, Contacts 15,
 * Location 15, Camera 12, Storage 8, Bluetooth 4, Notification 3, Nearby Devices 2.
 */
object PermissionCatalog {

    val ALL: List<PermissionMeta> = listOf(
        PermissionMeta(
            id = "android.permission.RECORD_AUDIO",
            displayName = "Microphone",
            emoji = "🎙️",
            why = "Can record audio at any time the app is running, including private conversations.",
            weight = 20
        ),
        PermissionMeta(
            id = "android.permission.READ_SMS",
            displayName = "SMS Messages",
            emoji = "📱",
            why = "Can read text messages, including one-time passcodes used for two-factor login.",
            weight = 20
        ),
        PermissionMeta(
            id = "android.permission.RECEIVE_SMS",
            displayName = "SMS Messages",
            emoji = "📱",
            why = "Can intercept incoming text messages, including one-time passcodes.",
            weight = 20
        ),
        PermissionMeta(
            id = "android.permission.READ_CALL_LOG",
            displayName = "Call Log",
            emoji = "📋",
            why = "Exposes who you call, when, and for how long — your full social graph.",
            weight = 18
        ),
        PermissionMeta(
            id = "android.permission.READ_CONTACTS",
            displayName = "Contacts",
            emoji = "👤",
            why = "Full access to every saved name, phone number, and email address.",
            weight = 15
        ),
        PermissionMeta(
            id = "android.permission.ACCESS_FINE_LOCATION",
            displayName = "Precise Location",
            emoji = "📍",
            why = "Tracks your exact real-time GPS position, even indoors.",
            weight = 15
        ),
        PermissionMeta(
            id = "android.permission.ACCESS_COARSE_LOCATION",
            displayName = "Approximate Location",
            emoji = "📍",
            why = "Tracks your general location, usually accurate to a city block.",
            weight = 10
        ),
        PermissionMeta(
            id = "android.permission.CAMERA",
            displayName = "Camera",
            emoji = "📷",
            why = "Front and back camera access, which could be used without a visible indicator misuse.",
            weight = 12
        ),
        PermissionMeta(
            id = "android.permission.READ_EXTERNAL_STORAGE",
            displayName = "Storage",
            emoji = "📁",
            why = "Can read photos, downloads, and documents saved on your device.",
            weight = 8
        ),
        PermissionMeta(
            id = "android.permission.MANAGE_EXTERNAL_STORAGE",
            displayName = "All Files Access",
            emoji = "🗂️",
            why = "Near-unrestricted access to your entire file system.",
            weight = 10
        ),
        PermissionMeta(
            id = "android.permission.READ_CALENDAR",
            displayName = "Calendar",
            emoji = "📅",
            why = "Can read your events, meetings, and who you're meeting with.",
            weight = 8
        ),
        PermissionMeta(
            id = "android.permission.BODY_SENSORS",
            displayName = "Body Sensors",
            emoji = "❤️",
            why = "Reads heart rate and other biometric sensor data.",
            weight = 10
        ),
        PermissionMeta(
            id = "android.permission.ACTIVITY_RECOGNITION",
            displayName = "Physical Activity",
            emoji = "🏃",
            why = "Tracks walking, running, and driving to build a picture of your daily routine.",
            weight = 6
        ),
        PermissionMeta(
            id = "android.permission.BLUETOOTH_CONNECT",
            displayName = "Bluetooth",
            emoji = "🔵",
            why = "Can connect to and communicate with nearby Bluetooth devices.",
            weight = 4
        ),
        PermissionMeta(
            id = "android.permission.BLUETOOTH_SCAN",
            displayName = "Nearby Devices",
            emoji = "📶",
            why = "Can scan for and detect other nearby devices without your explicit awareness.",
            weight = 2
        ),
        PermissionMeta(
            id = "android.permission.NEARBY_WIFI_DEVICES",
            displayName = "Nearby Devices",
            emoji = "📶",
            why = "Can discover nearby Wi-Fi devices to infer your location and surroundings.",
            weight = 2
        ),
        PermissionMeta(
            id = "android.permission.POST_NOTIFICATIONS",
            displayName = "Notifications",
            emoji = "🔔",
            why = "Can send you push notifications at any time.",
            weight = 3
        ),
        PermissionMeta(
            id = "android.permission.CALL_PHONE",
            displayName = "Phone",
            emoji = "☎️",
            why = "Can place phone calls on your behalf without confirmation.",
            weight = 14
        )
    )

    val byId: Map<String, PermissionMeta> = ALL.associateBy { it.id }

    fun metaFor(permissionId: String): PermissionMeta? = byId[permissionId]

    /** True if this permission is one Privacy Analyzer actively tracks & scores. */
    fun isTracked(permissionId: String): Boolean = byId.containsKey(permissionId)
}
