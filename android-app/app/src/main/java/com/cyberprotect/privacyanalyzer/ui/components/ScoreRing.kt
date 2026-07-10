package com.cyberprotect.privacyanalyzer.ui.components
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import kotlinx.coroutines.delay

/**
 * Circular 0-100 gauge used for both the overall device score and
 * individual app risk. Animates its sweep in on first composition.
 */
@Composable
fun ScoreRing(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 140,
    strokeWidth: Float = 14f,
    label: String = "/ 100"
) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffectOnce { animate = true }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "scoreRingProgress"
    )

    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val sweepBase = 270f
            val startAngle = 135f
            drawArc(
                color = Color.White.copy(alpha = 0.06f),
                startAngle = startAngle,
                sweepAngle = sweepBase,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepBase * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}",
                    fontSize = (size * 0.22f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Text(text = label, fontSize = (size * 0.08f).sp, color = TextMuted)
            }
        }
    }
}

/** Tiny helper so callers don't need to pull in LaunchedEffect boilerplate everywhere. */
@Composable
private fun LaunchedEffectOnce(block: suspend () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        delay(80)
        block()
    }
}
