package com.cyberprotect.privacyanalyzer.ui.components
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted

data class Slice(val label: String, val value: Int, val color: Color)

/** Multi-segment donut chart, e.g. risk distribution across all scanned apps. */
@Composable
fun DonutChart(slices: List<Slice>, modifier: Modifier = Modifier, sizeDp: Int = 130) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1)
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(slices) { animate = true }
    val progress by animateFloatAsState(if (animate) 1f else 0f, tween(1100), label = "donut")

    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val strokeWidth = size.minDimension * 0.16f
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.value.toFloat() / total) * 360f * progress
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += (slice.value.toFloat() / total) * 360f
        }
    }
}

/** Legend rows to pair with DonutChart. */
@Composable
fun ChartLegend(slices: List<Slice>) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slices.forEach { slice ->
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(slice.color))
                Text(text = slice.label, fontSize = 13.sp, modifier = Modifier.weight(1f))
                val pct = (slice.value * 100f / total).toInt()
                Text(text = "$pct%", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

/** Single horizontal bar with animated fill, used for permission-frequency charts. */
@Composable
fun BarRow(label: String, percent: Int, color: Color, modifier: Modifier = Modifier) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(percent) { animate = true }
    val width by animateFloatAsState(if (animate) percent / 100f else 0f, tween(1100), label = "bar")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 12.sp)
            Text(text = "$percent%", fontSize = 11.sp, color = TextMuted)
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(width.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
