package com.cyberprotect.privacyanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cyberprotect.privacyanalyzer.model.RiskLevel
import com.cyberprotect.privacyanalyzer.ui.theme.RiskColors
import com.cyberprotect.privacyanalyzer.ui.theme.Surface
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted

fun colorForRisk(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE -> RiskColors.safe
    RiskLevel.LOW -> RiskColors.low
    RiskLevel.MEDIUM -> RiskColors.medium
    RiskLevel.HIGH -> RiskColors.high
    RiskLevel.CRITICAL -> RiskColors.critical
}

/** Small colored pill showing a risk level, e.g. "CRITICAL". */
@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    val color = colorForRisk(level)
    Text(
        text = level.label.uppercase(),
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Small granted/denied chip for permission rows. */
@Composable
fun GrantStatusChip(granted: Boolean, modifier: Modifier = Modifier) {
    val color = if (granted) RiskColors.critical else RiskColors.safe
    Text(
        text = if (granted) "GRANTED" else "DENIED",
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** Generic card surface used across the dashboard, matching website card styling. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    content: @Composable ColumnScopeAlias.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope

/** A single stat block: big number + small muted label, used on the dashboard grid. */
@Composable
fun StatBlock(value: String, label: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
}
