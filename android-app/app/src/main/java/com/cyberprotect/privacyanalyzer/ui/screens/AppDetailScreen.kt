package com.cyberprotect.privacyanalyzer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cyberprotect.privacyanalyzer.data.RiskCalculator
import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.model.PermissionCatalog
import com.cyberprotect.privacyanalyzer.model.PermissionGrant
import com.cyberprotect.privacyanalyzer.ui.components.GlassCard
import com.cyberprotect.privacyanalyzer.ui.components.GrantStatusChip
import com.cyberprotect.privacyanalyzer.ui.components.RiskBadge
import com.cyberprotect.privacyanalyzer.ui.components.ScoreRing
import com.cyberprotect.privacyanalyzer.ui.components.colorForRisk
import com.cyberprotect.privacyanalyzer.ui.theme.Cyan
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import com.cyberprotect.privacyanalyzer.util.SettingsNavigator
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: ScanViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val app = state.apps.firstOrNull { it.packageName == packageName }
    val context = LocalContext.current

    // Rescan this specific app every time the screen (Activity) resumes —
    // i.e. right after the user comes back from Android Settings.
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentPackage = rememberUpdatedState(packageName)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.rescanPackage(currentPackage.value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.appName ?: "App Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (app == null) {
            Column(modifier = Modifier.padding(padding).padding(24.dp)) {
                Text("This app is no longer installed, or hasn't been scanned yet.", color = TextMuted)
            }
            return@Scaffold
        }

        val riskColor = colorForRisk(app.riskLevel)
        val recommendations = remember(app) { RiskCalculator.recommendationsFor(app) }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = BitmapPainter(app.icon.toBitmap().asImageBitmap()),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(app.appName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(app.packageName, fontSize = 11.sp, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "v${app.versionName ?: "—"}  ·  Updated ${formatDate(app.lastUpdateTime)}",
                            fontSize = 11.sp, color = TextMuted
                        )
                        Text(
                            text = "Installed ${formatDate(app.firstInstallTime)}  ·  ${app.category}",
                            fontSize = 11.sp, color = TextMuted
                        )
                        Spacer(Modifier.height(16.dp))
                        ScoreRing(score = app.riskScore, color = riskColor, size = 120, label = "risk")
                        Spacer(Modifier.height(10.dp))
                        RiskBadge(level = app.riskLevel)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = riskExplanation(app),
                            fontSize = 12.sp, color = TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { SettingsNavigator.openAppDetails(context, app.packageName) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color(0xFF040810))
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open App Settings", fontWeight = FontWeight.Bold)
                }
            }

            if (app.dangerousGranted.isNotEmpty() || app.dangerousNotGranted.isNotEmpty()) {
                item { SectionTitle("Dangerous Permissions") }
                items(app.dangerousGranted + app.dangerousNotGranted, key = { it.permissionId }) { grant ->
                    PermissionRow(grant = grant, packageName = app.packageName, context = context)
                }
            }

            if (app.normalPermissions.isNotEmpty()) {
                item { SectionTitle("Other Permissions") }
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        app.normalPermissions.take(10).forEach { grant ->
                            Text(
                                text = "• ${grant.permissionId.substringAfterLast('.')}",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        if (app.normalPermissions.size > 10) {
                            Text("+ ${app.normalPermissions.size - 10} more", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            item { SectionTitle("Recommendations") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    recommendations.forEach { rec ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("• ", color = Cyan, fontWeight = FontWeight.Bold)
                            Text(rec, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun PermissionRow(
    grant: PermissionGrant,
    packageName: String,
    context: android.content.Context
) {
    val meta = PermissionCatalog.metaFor(grant.permissionId) ?: return
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(meta.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(meta.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(meta.why, fontSize = 11.sp, color = TextMuted, maxLines = 2)
            }
            Spacer(Modifier.width(8.dp))
            GrantStatusChip(granted = grant.isGranted)
        }
        if (grant.isGranted) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { SettingsNavigator.openManagePermission(context, packageName, grant.permissionId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF111E33))
            ) {
                Text("Manage Permission", fontSize = 12.sp)
            }
        }
    }
}

private fun riskExplanation(app: AppInfo): String {
    val count = app.dangerousPermissionCount
    return when {
        count == 0 -> "${app.appName} holds no tracked dangerous permissions. It looks safe."
        count == 1 -> "${app.appName} holds 1 dangerous permission that contributes to its risk score."
        else -> "${app.appName} holds $count dangerous permissions, driving its ${app.riskLevel.label.lowercase()} risk rating."
    }
}

private fun formatDate(millis: Long): String {
    if (millis <= 0) return "—"
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
}
