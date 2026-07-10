package com.cyberprotect.privacyanalyzer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberprotect.privacyanalyzer.data.AppScanner
import com.cyberprotect.privacyanalyzer.data.RiskCalculator
import com.cyberprotect.privacyanalyzer.model.AppInfo
import com.cyberprotect.privacyanalyzer.model.DeviceSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class AppFilter { ALL, CRITICAL, HIGH, MEDIUM, LOW, SAFE }

data class ScanUiState(
    val isScanning: Boolean = false,
    val hasScannedOnce: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val snapshot: DeviceSnapshot = DeviceSnapshot.EMPTY,
    val searchQuery: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val lastExportedReport: File? = null,
    val isExporting: Boolean = false
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = AppScanner(application)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /** The full, unfiltered scan result — screens derive their own filtered views from this. */
    val filteredApps: List<AppInfo>
        get() {
            val state = _uiState.value
            var list = state.apps
            if (state.filter != AppFilter.ALL) {
                list = list.filter { it.riskLevel.name == state.filter.name }
            }
            if (state.searchQuery.isNotBlank()) {
                val q = state.searchQuery.trim().lowercase()
                list = list.filter {
                    it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
                }
            }
            return list
        }

    init {
        runScan(deep = false)
    }

    fun runScan(deep: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val includeSystem = deep
            val apps = scanner.scanAll(includeSystemApps = includeSystem)
            val totalInstalled = scanner.totalInstalledCount()
            val snapshot = RiskCalculator.buildDeviceSnapshot(apps, totalInstalled, System.currentTimeMillis())
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                hasScannedOnce = true,
                apps = apps,
                snapshot = snapshot
            )
        }
    }

    /** Called when the user returns from Android Settings for a specific app. */
    fun rescanPackage(packageName: String) {
        viewModelScope.launch {
            val updated = scanner.rescanPackage(packageName) ?: return@launch
            val newApps = _uiState.value.apps.map { if (it.packageName == packageName) updated else it }
            val totalInstalled = scanner.totalInstalledCount()
            val snapshot = RiskCalculator.buildDeviceSnapshot(newApps, totalInstalled, System.currentTimeMillis())
            _uiState.value = _uiState.value.copy(apps = newApps, snapshot = snapshot)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilter(filter: AppFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun appByPackage(packageName: String): AppInfo? =
        _uiState.value.apps.firstOrNull { it.packageName == packageName }

    fun exportPdf(onDone: (File) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val state = _uiState.value
            val file = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.cyberprotect.privacyanalyzer.util.PdfReportGenerator.generate(
                    getApplication(), state.snapshot, state.apps
                )
            }
            _uiState.value = _uiState.value.copy(isExporting = false, lastExportedReport = file)
            onDone(file)
        }
    }
}
