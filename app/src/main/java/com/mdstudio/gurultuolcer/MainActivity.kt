package com.mdstudio.gurultuolcer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.mdstudio.gurultuolcer.ui.NoiseMeterRoute
import com.mdstudio.gurultuolcer.ui.theme.GurultuOlcerTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("app_settings", MODE_PRIVATE) }
    private var hasAudioPermission by mutableStateOf(false)
    private var shouldShowPermissionRationale by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        refreshPermissionState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguage()
        refreshPermissionState()

        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity)
        }

        enableEdgeToEdge()
        setContent {
            GurultuOlcerTheme {
                NoiseMeterRoute(
                    hasAudioPermission = hasAudioPermission,
                    shouldShowPermissionRationale = shouldShowPermissionRationale,
                    selectedLanguage = prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onOpenAppSettings = {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null),
                            ),
                        )
                    },
                    onLanguageSelected = { languageCode ->
                        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
                        applyLanguage(languageCode)
                        recreate()
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        shouldShowPermissionRationale = shouldShowRequestPermissionRationale(
            Manifest.permission.RECORD_AUDIO,
        )
    }

    private fun applySavedLanguage() {
        val saved = prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
        applyLanguage(saved)
    }

    private fun applyLanguage(languageCode: String) {
        val locales = if (languageCode == LANGUAGE_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val LANGUAGE_SYSTEM = "system"
    }
}

