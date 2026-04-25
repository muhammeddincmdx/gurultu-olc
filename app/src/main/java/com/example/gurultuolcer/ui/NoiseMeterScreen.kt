package com.example.gurultuolcer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

data class NoiseUiState(
    val level: Float,
    val label: String,
    val description: String,
    val isMeasuring: Boolean,
    val hasPermission: Boolean,
    val shouldShowPermissionRationale: Boolean,
    val history: List<Float>,
)

@Composable
fun NoiseMeterScreen(
    state: NoiseUiState,
    onPrimaryAction: () -> Unit,
    onPermissionAction: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red < 0.5f
    val accent = when {
        state.level < 32f -> if (isDark) Color(0xFF7EE081) else Color(0xFF2E7D32)
        state.level < 50f -> if (isDark) Color(0xFF62E4D7) else Color(0xFF00897B)
        state.level < 70f -> if (isDark) Color(0xFFFFD166) else Color(0xFFF29F05)
        else -> if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
    }
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val animatedLevel by animateFloatAsState(
        targetValue = state.level,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 190f),
        label = "noise-level",
    )
    val scrollState = rememberScrollState()
    val adHeight = 106.dp

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorScheme.background,
                            colorScheme.surfaceVariant,
                            accent.copy(alpha = if (isDark) 0.16f else 0.08f),
                        ),
                    ),
                ),
        ) {
            BackgroundGlow(accent = accent)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        start = 22.dp,
                        end = 22.dp,
                        top = topPadding + 14.dp,
                        bottom = bottomPadding + adHeight + 20.dp,
                    ),
            ) {
                HeaderBlock(accent = accent, label = state.label, isMeasuring = state.isMeasuring)
                Spacer(modifier = Modifier.height(18.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Gauge(level = animatedLevel, accent = accent)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "${animatedLevel.toInt()} dB",
                        fontSize = 52.sp,
                        lineHeight = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    MetricsRow(level = animatedLevel, accent = accent, label = state.label)
                }
                Spacer(modifier = Modifier.height(18.dp))
                GlassCard { MiniWaveform(points = state.history, accent = accent) }
                Spacer(modifier = Modifier.height(14.dp))
                if (!state.hasPermission) {
                    GlassCard {
                        PermissionCard(
                            shouldShowPermissionRationale = state.shouldShowPermissionRationale,
                            onPermissionAction = onPermissionAction,
                            onOpenSettings = onOpenSettings,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color.Black,
                        contentColor = if (isDark) Color.Black else Color.White,
                    ),
                ) {
                    Text(
                        text = when {
                            !state.hasPermission -> "Mikrofon izni ver"
                            state.isMeasuring -> "Ölçümü durdur"
                            else -> "Ölçümü başlat"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 22.dp, end = 22.dp, bottom = bottomPadding + 10.dp),
            ) {
                BottomAdCard()
            }
        }
    }
}

@Composable
private fun HeaderBlock(accent: Color, label: String, isMeasuring: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = colorScheme.surface.copy(alpha = 0.58f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gürültü Ölçer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ortam sesini canlı olarak izle ve daha dengeli bir ölçüm gör.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = if (isMeasuring) "Canlı" else "Hazır",
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.72f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(text = label, color = accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MetricsRow(level: Float, accent: Color, label: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(Modifier.weight(1f), "Durum", label, accent)
        MetricCard(Modifier.weight(1f), "Seviye", "${level.toInt()} dB", accent.copy(alpha = 0.85f))
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Denge",
            value = when {
                level < 38f -> "Sakin"
                level < 62f -> "Dengeli"
                else -> "Yoğun"
            },
            accent = accent.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun MetricCard(modifier: Modifier = Modifier, title: String, value: String, accent: Color) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = colorScheme.surface.copy(alpha = 0.8f)) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colorScheme.surface.copy(alpha = 0.74f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(26.dp))
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun BottomAdCard() {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface.copy(alpha = 0.92f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.92f))
                    .border(1.dp, colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
            ) {
                AdBannerCard()
            }
        }
    }
}

@Composable
private fun BackgroundGlow(accent: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.78f, size.height * 0.22f),
                radius = size.minDimension * 0.32f,
            ),
            radius = size.minDimension * 0.32f,
            center = Offset(size.width * 0.78f, size.height * 0.22f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.76f),
                radius = size.minDimension * 0.28f,
            ),
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.18f, size.height * 0.76f),
        )
    }
}

@Composable
private fun AdBannerCard() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val adView = remember(context) {
        AdView(context).apply {
            adUnitId = context.getString(com.example.gurultuolcer.R.string.admob_banner_ad_unit_id)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerWidthDp = with(density) { maxWidth.toPx() / density.density }
        val adWidth = containerWidthDp.toInt().coerceAtLeast(320)
        val adSize = remember(adWidth) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

        DisposableEffect(adView, adSize) {
            adView.setAdSize(adSize)
            adView.loadAd(AdRequest.Builder().build())
            onDispose { adView.destroy() }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.getHeightInPixels(context).let { px ->
                    with(density) { (px / density.density).dp }
                }),
            factory = { adView },
        )
    }
}

@Composable
private fun Gauge(level: Float, accent: Color) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = (level / 105f).coerceIn(0f, 1f)
    Box(modifier = Modifier.size(278.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 24.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colorScheme.surface.copy(alpha = 0.95f), colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                ),
                radius = size.minDimension / 2.45f,
            )
            drawArc(
                color = colorScheme.outlineVariant.copy(alpha = 0.65f),
                startAngle = 148f,
                sweepAngle = 244f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke, stroke),
                size = size.copy(width = size.width - (stroke * 2), height = size.height - (stroke * 2)),
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.34f), accent, accent.copy(alpha = 0.45f))),
                startAngle = 148f,
                sweepAngle = 244f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke, stroke),
                size = size.copy(width = size.width - (stroke * 2), height = size.height - (stroke * 2)),
            )
            drawCircle(color = accent.copy(alpha = 0.08f), radius = size.minDimension / 3.1f, style = Stroke(width = 10.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(accent))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Canlı seviye", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MiniWaveform(points: List<Float>, accent: Color) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Text(text = "Son birkaç saniye", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(90.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEachIndexed { index, value ->
                val animatedBar by animateFloatAsState(
                    targetValue = value,
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 180f),
                    label = "wave-$index",
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height((18 + (animatedBar * 70)).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.84f)))),
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    shouldShowPermissionRationale: Boolean,
    onPermissionAction: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Text(text = "Mikrofon izni gerekli", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (shouldShowPermissionRationale) {
                "Mikrofon izni verilmeden canlı ölçüm çalışmaz. İzni tekrar iste."
            } else {
                "İzin kapalı olabilir. Gerekirse uygulama ayarlarından mikrofon iznini aç."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onPermissionAction,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer, contentColor = colorScheme.onSecondaryContainer),
            ) {
                Text("İzin iste", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
            ) {
                Text("Ayarları aç", fontWeight = FontWeight.Bold)
            }
        }
    }
}



