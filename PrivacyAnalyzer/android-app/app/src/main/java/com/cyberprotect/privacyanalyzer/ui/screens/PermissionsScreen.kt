package com.cyberprotect.privacyanalyzer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cyberprotect.privacyanalyzer.model.PermissionCatalog
import com.cyberprotect.privacyanalyzer.ui.components.GlassCard
import com.cyberprotect.privacyanalyzer.ui.theme.RiskColors
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel

@Composable
fun PermissionsScreen(
    viewModel: ScanViewModel,
    onPermissionClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Group the catalog by display name so SMS's two underlying permission IDs
    // (READ_SMS / RECEIVE_SMS) show up as a single "SMS Messages" row.
    val grouped = PermissionCatalog.ALL.groupBy { it.displayName }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Tap a permission to see which apps hold it",
            fontSize = 12.sp, color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(grouped.entries.toList(), key = { it.key }) { (displayName, metas) ->
                val ids = metas.map { it.id }.toSet()
                val meta = metas.first()
                val appsWithPermission = state.apps.filter { app ->
                    app.allPermissions.any { it.isGranted && it.permissionId in ids }
                }
                PermissionOverviewRow(
                    emoji = meta.emoji,
                    name = displayName,
                    why = meta.why,
                    appCount = appsWithPermission.size,
                    onClick = { onPermissionClick(ids.first()) }
                )
            }
        }
    }
}

@Composable
private fun PermissionOverviewRow(
    emoji: String,
    name: String,
    why: String,
    appCount: Int,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(0.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(0.dp)
                .then(Modifier)
                .clickableRow(onClick)
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(why, fontSize = 11.sp, color = TextMuted, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$appCount app${if (appCount == 1) "" else "s"} currently granted",
                    fontSize = 11.sp,
                    color = if (appCount > 0) RiskColors.high else RiskColors.safe
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
