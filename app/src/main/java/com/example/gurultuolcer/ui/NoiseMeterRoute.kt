package com.example.gurultuolcer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.gurultuolcer.audio.NoiseMonitor
import com.example.gurultuolcer.audio.NoiseSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun NoiseMeterRoute(
    hasAudioPermission: Boolean,
    shouldShowPermissionRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
) {
    val context = LocalContext.current
    val monitor = remember(context) { NoiseMonitor(context) }
    var isMeasuring by remember { mutableStateOf(false) }
    var hasAskedPermission by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(24f) }
    val history = remember { mutableStateListOf<Float>().apply { repeat(20) { add(0.18f) } } }

    DisposableEffect(Unit) {
        onDispose {
            monitor.stop()
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && !isMeasuring) {
            isMeasuring = true
        }
        if (!hasAudioPermission && !hasAskedPermission) {
            hasAskedPermission = true
            onRequestPermission()
        }
    }

    LaunchedEffect(hasAudioPermission, isMeasuring) {
        if (!hasAudioPermission || !isMeasuring) {
            monitor.stop()
            return@LaunchedEffect
        }

        runCatching { monitor.start() }
            .onFailure {
                isMeasuring = false
            }

        var smooth = level
        while (isMeasuring) {
            val sample = withContext(Dispatchers.Default) {
                runCatching { monitor.readSample() }.getOrDefault(
                    NoiseSample(level = 24f, peak = 0, read = -99, recordingState = -99, source = -99),
                )
            }

            val displayLevel = sample.level.coerceAtLeast(20f)
            val delta = abs(displayLevel - smooth)
            val stableLevel = if (delta < 2.2f) smooth else displayLevel
            smooth = (smooth * 0.80f) + (stableLevel * 0.20f)

            level = smooth
            history.removeFirstOrNull()
            history.add((smooth / 105f).coerceIn(0.08f, 1f))

            if (!isActive) break
            delay(180)
        }
    }

    val category = classifyNoise(level)
    NoiseMeterScreen(
        state = NoiseUiState(
            level = level,
            label = category.title,
            description = category.description,
            isMeasuring = isMeasuring,
            hasPermission = hasAudioPermission,
            shouldShowPermissionRationale = shouldShowPermissionRationale,
            history = history.toList(),
        ),
        onPrimaryAction = {
            if (!hasAudioPermission) {
                onRequestPermission()
            } else {
                isMeasuring = !isMeasuring
            }
        },
        onPermissionAction = onRequestPermission,
        onOpenSettings = onOpenAppSettings,
    )
}

private fun classifyNoise(level: Float): NoiseCategory {
    return when {
        level < 32f -> NoiseCategory("Sessiz", "Kütüphane, yatak odası veya sakin ofis seviyesi.")
        level < 50f -> NoiseCategory("Rahat", "Normal konuşma öncesi ev içi arka plan seviyesi.")
        level < 70f -> NoiseCategory("Canlı", "Konuşma, cadde veya hareketli çalışma alanı.")
        else -> NoiseCategory("Yüksek", "Uzun süre maruz kalırsan yorucu olabilir.")
    }
}

private data class NoiseCategory(
    val title: String,
    val description: String,
)
