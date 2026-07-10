package com.cyberprotect.privacyanalyzer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import com.cyberprotect.privacyanalyzer.model.PermissionCatalog
import com.cyberprotect.privacyanalyzer.ui.components.GlassCard
import com.cyberprotect.privacyanalyzer.ui.components.RiskBadge
import com.cyberprotect.privacyanalyzer.ui.components.colorForRisk
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDetailScreen(
    permissionId: String,
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val meta = PermissionCatalog.metaFor(permissionId)
    val groupIds = PermissionCatalog.ALL.filter { it.displayName == meta?.displayName }.map { it.id }.toSet()

    val holders = state.apps.filter { app -> app.allPermissions.any { it.isGranted && it.permissionId in groupIds } }
    val nonHolders = state.apps.filter { app -> app.allPermissions.any { it.permissionId in groupIds && !it.isGranted } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(meta?.displayName ?: "Permission", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(meta?.emoji ?: "🔒", fontSize = 28.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(meta?.displayName ?: permissionId, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(meta?.why ?: "", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Risk weight: +${meta?.weight ?: 0} points when granted",
                        fontSize = 12.sp, color = TextMuted
                    )
                }
            }

            item {
                Text(
                    text = "Currently Granted (${holders.size})",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
            }
            if (holders.isEmpty()) {
                item { Text("No installed app currently holds this permission.", fontSize = 12.sp, color = TextMuted) }
            }
            items(holders, key = { "granted_" + it.packageName }) { app ->
                PermissionAppRow(app.appName, app.packageName, app.riskScore, colorForRisk(app.riskLevel), onAppClick, app.icon)
            }

            if (nonHolders.isNotEmpty()) {
                item {
                    Text(
                        text = "Requested but Not Granted (${nonHolders.size})",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(nonHolders, key = { "denied_" + it.packageName }) { app ->
                    PermissionAppRow(app.appName, app.packageName, app.riskScore, colorForRisk(app.riskLevel), onAppClick, app.icon)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PermissionAppRow(
    appName: String,
    packageName: String,
    riskScore: Int,
    riskColor: androidx.compose.ui.graphics.Color,
    onAppClick: (String) -> Unit,
    icon: android.graphics.drawable.Drawable
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(Modifier)
            .let { it.clickableModifier { onAppClick(packageName) } },
        padding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = BitmapPainter(icon.toBitmap().asImageBitmap()),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(packageName, fontSize = 10.sp, color = TextMuted, maxLines = 1)
            }
            Text("$riskScore%", fontWeight = FontWeight.Bold, color = riskColor, fontSize = 14.sp)
        }
    }
}

private fun Modifier.clickableModifier(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
