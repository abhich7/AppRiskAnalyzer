package com.cyberprotect.privacyanalyzer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.ui.components.RiskBadge
import com.cyberprotect.privacyanalyzer.ui.components.colorForRisk
import com.cyberprotect.privacyanalyzer.ui.theme.Cyan
import com.cyberprotect.privacyanalyzer.ui.theme.Surface
import com.cyberprotect.privacyanalyzer.ui.theme.TextMuted
import com.cyberprotect.privacyanalyzer.viewmodel.AppFilter
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: ScanViewModel,
    onAppClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val apps = viewModel.filteredApps

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            text = "All Apps (${state.apps.size})",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by app name or package…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan)
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(AppFilter.values().toList()) { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (apps.isEmpty()) {
                item {
                    Text(
                        text = if (state.isScanning) "Scanning…" else "No apps match your search.",
                        color = TextMuted,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            items(apps, key = { it.packageName }) { app ->
                AppListItem(app = app, onClick = { onAppClick(app.packageName) })
            }
        }
    }
}

@Composable
private fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    val riskColor = colorForRisk(app.riskLevel)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, riskColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = BitmapPainter(app.icon.toBitmap().asImageBitmap()),
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
                Text(app.packageName, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${app.dangerousPermissionCount} dangerous permission${if (app.dangerousPermissionCount == 1) "" else "s"} granted",
                    fontSize = 11.sp, color = TextMuted
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${app.riskScore}%", fontWeight = FontWeight.ExtraBold, color = riskColor, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                RiskBadge(level = app.riskLevel)
            }
        }
    }
}
