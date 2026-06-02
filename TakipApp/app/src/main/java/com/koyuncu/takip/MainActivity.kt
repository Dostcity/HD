package com.koyuncu.takip

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.koyuncu.takip.ui.nav.AppNav
import com.koyuncu.takip.ui.theme.TakipTheme
import com.koyuncu.takip.ui.todo.TodoViewModel
import com.koyuncu.takip.ui.tracking.TrackingViewModel

class MainActivity : ComponentActivity() {

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* sonucu yok say */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as TakipApplication

        setContent {
            TakipTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(
                        todoVmFactory = { TodoViewModel(app.todoRepository) },
                        trackingVmFactory = { TrackingViewModel(app.productRepository) }
                    )
                }
            }
        }
    }
}
