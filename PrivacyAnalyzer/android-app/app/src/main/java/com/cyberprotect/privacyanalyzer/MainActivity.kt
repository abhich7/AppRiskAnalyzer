package com.cyberprotect.privacyanalyzer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cyberprotect.privacyanalyzer.ui.screens.AppDetailScreen
import com.cyberprotect.privacyanalyzer.ui.screens.AppListScreen
import com.cyberprotect.privacyanalyzer.ui.screens.HomeScreen
import com.cyberprotect.privacyanalyzer.ui.screens.PermissionDetailScreen
import com.cyberprotect.privacyanalyzer.ui.screens.PermissionsScreen
import com.cyberprotect.privacyanalyzer.ui.theme.Cyan
import com.cyberprotect.privacyanalyzer.ui.theme.PrivacyAnalyzerTheme
import com.cyberprotect.privacyanalyzer.ui.theme.Surface as SurfaceColor
import com.cyberprotect.privacyanalyzer.viewmodel.ScanViewModel

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Tab("home", "Home", Icons.Filled.Home)
    object Apps : Tab("apps", "Apps", Icons.Filled.Shield)
    object Permissions : Tab("permissions", "Permissions", Icons.Filled.Lock)
}

private val tabs = listOf(Tab.Home, Tab.Apps, Tab.Permissions)

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyAnalyzerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PrivacyAnalyzerApp(viewModel = viewModel, onSharePdf = ::sharePdf)
                }
            }
        }
    }

    private fun sharePdf(file: java.io.File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Privacy Report"))
    }
}

@Composable
private fun PrivacyAnalyzerApp(viewModel: ScanViewModel, onSharePdf: (java.io.File) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SurfaceColor) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Cyan,
                                selectedTextColor = Cyan,
                                indicatorColor = Cyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onExportPdf = { viewModel.exportPdf { file -> onSharePdf(file) } }
                )
            }
            composable(Tab.Apps.route) {
                AppListScreen(
                    viewModel = viewModel,
                    onAppClick = { pkg -> navController.navigate("appDetail/$pkg") }
                )
            }
            composable(Tab.Permissions.route) {
                PermissionsScreen(
                    viewModel = viewModel,
                    onPermissionClick = { permId -> navController.navigate("permissionDetail/${Uri.encode(permId)}") }
                )
            }
            composable("appDetail/{pkg}") { backStack ->
                val pkg = backStack.arguments?.getString("pkg") ?: return@composable
                AppDetailScreen(
                    packageName = pkg,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("permissionDetail/{permId}") { backStack ->
                val permId = Uri.decode(backStack.arguments?.getString("permId") ?: return@composable)
                PermissionDetailScreen(
                    permissionId = permId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAppClick = { pkg -> navController.navigate("appDetail/$pkg") }
                )
            }
        }
    }
}
