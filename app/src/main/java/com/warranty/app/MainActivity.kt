package com.warranty.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.warranty.app.ui.edit.EditWarrantyScreen
import com.warranty.app.ui.edit.EditWarrantyViewModel
import com.warranty.app.ui.list.WarrantyListScreen
import com.warranty.app.ui.list.WarrantyListViewModel
import com.warranty.app.ui.theme.WarrantyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WarrantyTheme {
                RequestNotificationPermission()
                WarrantyApp(onPersistImportPermission = { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                        // Ignore if we cannot persist permission
                    }
                })
            }
        }
    }
}

@Composable
private fun WarrantyApp(
    onPersistImportPermission: (android.net.Uri) -> Unit
) {
    val navController = rememberNavController()
    var pendingReplace by rememberSaveable { mutableStateOf(false) }
    var listViewModelRef by remember { mutableStateOf<WarrantyListViewModel?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onPersistImportPermission(uri)
            listViewModelRef?.onImport(uri, pendingReplace)
        }
    }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") { entry ->
            val viewModel = hiltViewModel<WarrantyListViewModel>(entry)
            listViewModelRef = viewModel
            val state by viewModel.state.collectAsState()

            WarrantyListScreen(
                state = state,
                onAddClick = { navController.navigate("edit") },
                onItemClick = { id -> navController.navigate("edit?itemId=") },
                onDelete = viewModel::onDelete,
                onReminderToggle = viewModel::onReminderToggle,
                onFilterChange = viewModel::onFilterChange,
                onQueryChange = viewModel::onQueryChange,
                onExport = viewModel::onExport,
                onImportSelected = { replaceExisting ->
                    pendingReplace = replaceExisting
                    importLauncher.launch(arrayOf("application/json"))
                },
                onMessageShown = viewModel::consumeMessage
            )
        }

        composable(
            route = "edit?itemId={itemId}",
            arguments = listOf(navArgument("itemId") {
                type = NavType.LongType
                defaultValue = -1
            })
        ) { entry ->
            val viewModel = hiltViewModel<EditWarrantyViewModel>(entry)
            val itemId = entry.arguments?.getLong("itemId")?.takeIf { it > 0 }
            LaunchedEffect(itemId) {
                viewModel.load(itemId)
            }
            val state by viewModel.state.collectAsState()

            EditWarrantyScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onSaved = {
                    viewModel.consumeSaved()
                    navController.popBackStack()
                },
                onNameChange = viewModel::onNameChange,
                onCategoryChange = viewModel::onCategoryChange,
                onPriceChange = viewModel::onPriceChange,
                onStoreChange = viewModel::onStoreChange,
                onPurchaseDateChange = viewModel::onPurchaseDateChange,
                onExpirationDateChange = viewModel::onExpirationDateChange,
                onDurationMonthsChange = viewModel::onDurationMonthsChange,
                onReminderChange = viewModel::onReminderChange,
                onSave = viewModel::onSave,
                onConsumeError = viewModel::consumeError
            )
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
