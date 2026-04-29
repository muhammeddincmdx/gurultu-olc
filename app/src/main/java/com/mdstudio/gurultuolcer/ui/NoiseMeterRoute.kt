package com.mdstudio.gurultuolcer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mdstudio.gurultuolcer.R
import com.mdstudio.gurultuolcer.audio.NoiseMonitor
import com.mdstudio.gurultuolcer.audio.NoiseSample
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
    var isThresholdAlarmEnabled by rememberSaveable { mutableStateOf(false) }
    var thresholdDb by rememberSaveable { mutableFloatStateOf(80f) }
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

            val adjustedLevel = smooth.coerceIn(20f, 105f)
            level = adjustedLevel
            history.removeFirstOrNull()
            history.add((adjustedLevel / 105f).coerceIn(0.08f, 1f))

            if (!isActive) break
            delay(180)
        }
    }

    val category = classifyNoise(level)
    NoiseMeterScreen(
        state = NoiseUiState(
            level = level,
            labelRes = category.titleRes,
            descriptionRes = category.descriptionRes,
            isMeasuring = isMeasuring,
            hasPermission = hasAudioPermission,
            shouldShowPermissionRationale = shouldShowPermissionRationale,
            history = history.toList(),
            isThresholdAlarmEnabled = isThresholdAlarmEnabled,
            thresholdDb = thresholdDb,
            isThresholdExceeded = isThresholdAlarmEnabled && level >= thresholdDb,
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
        onThresholdAlarmEnabledChange = { isThresholdAlarmEnabled = it },
        onThresholdDbChange = { thresholdDb = it.coerceIn(60f, 100f) },
    )
}

private fun classifyNoise(level: Float): NoiseCategory {
    return when {
        level < 32f -> NoiseCategory(R.string.noise_state_silent, R.string.noise_desc_silent)
        level < 50f -> NoiseCategory(R.string.noise_state_comfort, R.string.noise_desc_comfort)
        level < 70f -> NoiseCategory(R.string.noise_state_live, R.string.noise_desc_live)
        else -> NoiseCategory(R.string.noise_state_high, R.string.noise_desc_high)
    }
}

private data class NoiseCategory(
    val titleRes: Int,
    val descriptionRes: Int,
)

