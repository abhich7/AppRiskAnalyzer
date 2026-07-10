package com.cyberprotect.privacyanalyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cyberprotect.privacyanalyzer.model.RiskLevel
import com.cyberprotect.privacyanalyzer.ui.components.ChartLegend
import com.cyberprotect.privacyanalyzer.ui.components.DonutChart
import com.cyberprotect.privacyanalyzer.ui.components.GlassCard
import com.cyberprotect.privacyanalyzer.ui.components.ScoreRing
import com.cyberprotect.privacyanalyzer.ui.components.Slice
import com.cyberprotect.privacyanalyzer.ui.components.StatBlock
import com.cyberprotect.privacyanalyzer.ui.components.colorForRisk
import com.cyberprotect.privacyanalyzer.ui.theme.Cyan
import com.cyberprotect.privacyanalyzer.ui.theme.RiskColors
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ScanViewModel,
    onExportPdf: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snapshot = state.snapshot
    val scoreColor = colorForRisk(snapshot.riskLevel)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValuesHome,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Cyan)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Privacy Analyzer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = if (snapshot.lastScanTime > 0)
                            "Last scan " + SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(snapshot.lastScanTime))
                        else "Not scanned yet",
                        fontSize = 12.sp, color = TextMuted
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Overall Privacy Score", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (state.isScanning) {
                            CircularProgressIndicator(color = Cyan, modifier = Modifier.wrapContentSize())
                        } else {
                            ScoreRing(score = snapshot.overallScore, color = scoreColor, size = 150)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Grade ${snapshot.grade}  ·  ${snapshot.riskLevel.label} Risk",
                        color = scoreColor, fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.weight(1f), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    StatBlock(value = "${snapshot.totalInstalled}", label = "Total Installed")
                }
                GlassCard(modifier = Modifier.weight(1f), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    StatBlock(value = "${snapshot.appsScanned}", label = "Apps Scanned")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.weight(1f), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    StatBlock(value = "${snapshot.appsAtRisk}", label = "Apps At Risk", color = RiskColors.high)
                }
                GlassCard(modifier = Modifier.weight(1f), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    StatBlock(value = "${snapshot.criticalApps}", label = "Critical Apps", color = RiskColors.critical)
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                StatBlock(value = "${snapshot.dangerousPermissionCount}", label = "Dangerous Permissions Granted", color = RiskColors.medium)
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Risk Distribution", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                val counts = RiskLevel.values().associateWith { level -> state.apps.count { it.riskLevel == level } }
                val slices = listOf(
                    Slice("Critical", counts[RiskLevel.CRITICAL] ?: 0, RiskColors.critical),
                    Slice("High", counts[RiskLevel.HIGH] ?: 0, RiskColors.high),
                    Slice("Medium", counts[RiskLevel.MEDIUM] ?: 0, RiskColors.medium),
                    Slice("Low", counts[RiskLevel.LOW] ?: 0, RiskColors.low),
                    Slice("Safe", counts[RiskLevel.SAFE] ?: 0, RiskColors.safe)
                ).filter { it.value > 0 }.ifEmpty { listOf(Slice("No data", 1, TextMuted)) }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DonutChart(slices = slices)
                    Column(modifier = Modifier.weight(1f)) { ChartLegend(slices) }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.runScan(deep = false) },
                    enabled = !state.isScanning,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color(0xFF040810))
                ) {
                    Text(if (state.isScanning) "Scanning…" else "Run Quick Scan", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { viewModel.runScan(deep = true) },
                    enabled = !state.isScanning,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Run Deep Scan (incl. system apps)")
                }
                OutlinedButton(
                    onClick = onExportPdf,
                    enabled = !state.isExporting && state.hasScannedOnce,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.height(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isExporting) "Generating…" else "Generate PDF Report")
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

private val PaddingValuesHome = androidx.compose.foundation.layout.PaddingValues(
    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp
)
