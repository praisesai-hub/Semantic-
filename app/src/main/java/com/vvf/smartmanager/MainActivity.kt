package com.vvf.smartmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.vvf.smartmanager.core.navigation.VvfNavHost
import com.vvf.smartmanager.core.theme.VvfSmartManagerTheme
import com.vvf.smartmanager.presentation.components.StoragePermissionGate
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Extends [FragmentActivity] (not plain [ComponentActivity]) because
 * [androidx.biometric.BiometricPrompt] (Vault unlock, Phase 6) requires a FragmentActivity.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VvfSmartManagerTheme {
                StoragePermissionGate {
                    VvfNavHost()
                }
            }
        }
    }
}
