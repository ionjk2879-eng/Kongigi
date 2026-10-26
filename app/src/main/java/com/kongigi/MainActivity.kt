package com.kongigi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.kongigi.ui.AddScreen
import com.kongigi.ui.BarcodeScreen
import com.kongigi.ui.ListScreen
import com.kongigi.ui.theme.KongigiProjectTheme
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        scheduleNotificationWorker()
        enableEdgeToEdge()
        setContent {
            KongigiProjectTheme { KongigiApp() }
        }
    }

    private fun scheduleNotificationWorker() {
        val req = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil9am(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kongigi_notify",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }

    private fun delayUntil9am(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }
}

@Composable
private fun KongigiApp() {
    val navController = rememberNavController()
    val vm: GifticonViewModel = viewModel()

    NavHost(navController, startDestination = "list") {
        composable("list") {
            ListScreen(
                vm = vm,
                onAdd = { navController.navigate("add") },
                onBarcode = { g ->
                    vm.selected = g
                    navController.navigate("barcode")
                }
            )
        }
        composable("add") {
            AddScreen(vm = vm, onBack = { navController.popBackStack() })
        }
        composable("barcode") {
            val g = vm.selected
            if (g != null) {
                BarcodeScreen(g = g, onBack = { navController.popBackStack() })
            }
        }
    }
}
