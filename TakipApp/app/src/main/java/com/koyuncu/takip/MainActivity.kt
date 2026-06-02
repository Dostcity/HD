package com.koyuncu.takip

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.koyuncu.takip.ui.nav.AppNav
import com.koyuncu.takip.ui.theme.TakipTheme
import com.koyuncu.takip.ui.todo.TodoViewModel
import com.koyuncu.takip.ui.tracking.TrackingViewModel

class MainActivity : FragmentActivity() {

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* sonucu yok say */ }

    // WEAK + DEVICE_CREDENTIAL tum API seviyelerinde desteklenir.
    // (STRONG + DEVICE_CREDENTIAL Android 9/10'da desteklenmez ve cokme yapar.)
    private val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as TakipApplication

        // Cihazda parmak izi VEYA PIN/desen varsa kilitle; hata olursa kilitleme
        val canLock = try {
            BiometricManager.from(this).canAuthenticate(authenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS
        } catch (t: Throwable) {
            false
        }

        setContent {
            TakipTheme {
                var unlocked by rememberSaveable { mutableStateOf(!canLock) }

                LaunchedEffect(Unit) {
                    if (!unlocked) showAuthPrompt { unlocked = true }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (unlocked) {
                        AppNav(
                            todoVmFactory = { TodoViewModel(app.todoRepository) },
                            trackingVmFactory = { TrackingViewModel(app.productRepository) }
                        )
                    } else {
                        LockScreen(onUnlock = { showAuthPrompt { unlocked = true } })
                    }
                }
            }
        }
    }

    private fun showAuthPrompt(onSuccess: () -> Unit) {
        try {
            val executor = ContextCompat.getMainExecutor(this)
            val prompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Takip kilitli")
                .setSubtitle("Devam etmek için kimliğini doğrula")
                .setAllowedAuthenticators(authenticators)
                .build()
            prompt.authenticate(info)
        } catch (t: Throwable) {
            // Biyometrik baslatilamazsa kullaniciyi disarida birakma: iceri al
            onSuccess()
        }
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Uygulama kilitli", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUnlock) { Text("Kilidi aç") }
    }
}
