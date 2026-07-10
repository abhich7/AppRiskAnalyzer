package com.cyberprotect.privacyanalyzer.ui.theme

import androidx.compose.ui.graphics.Color

// Core surfaces — mirrors the website's --bg / --surface tokens
val BgDeep = Color(0xFF040810)
val BgSecondary = Color(0xFF070D1A)
val Surface = Color(0xFF0C1525)
val Surface2 = Color(0xFF111E33)
val BorderCyan = Color(0x1F00D2FF) // rgba(0,210,255,0.12)

// Accents
val Cyan = Color(0xFF00D2FF)
val Cyan2 = Color(0xFF00F5C8)
val Red = Color(0xFFFF4560)
val Orange = Color(0xFFFF8C42)
val Yellow = Color(0xFFFFD166)
val Green = Color(0xFF22B07D)

// Text
val TextPrimary = Color(0xFFE8F4F8)
val TextMuted = Color(0xFF7A97B0)

// Risk-level color lookup, shared across the whole app
object RiskColors {
    val safe = Green
    val low = Cyan2
    val medium = Yellow
    val high = Orange
    val critical = Red
}
