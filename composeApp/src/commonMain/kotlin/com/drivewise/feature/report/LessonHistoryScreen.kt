package com.drivewise.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.design.theme.DriveWiseGreen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
class LessonHistoryScreen : Screen {

    @Composable
    override fun Content() {
        val model: LessonHistoryModel = koinScreenModel()
        val state by model.state.collectAsState()
        val nav = LocalNavigator.current

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Lesson History", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { model.refresh() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF4F5F7))
            ) {
                when {
                    state.loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    state.error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Bir hata oluştu", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(state.error ?: "", color = Color(0xFF6B7280))
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { model.refresh() }) { Text("Retry") }
                        }
                    }

                    state.lessons.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Henüz ders yok", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text("Sürüş başlatınca burada görünecek.", color = Color(0xFF6B7280))
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.lessons, key = { it.lessonId }) { item ->
                                LessonRow(
                                    item = item,
                                    onClick = { nav?.push(LessonReportScreen(item.lessonId)) }
                                )
                            }

                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonRow(
    item: LessonHistoryUiItem,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    // Fixed metric colors (soft)
    val cPointsBg = Color(0xFFEFF6FF)  // blue-50
    val cPointsFg = Color(0xFF1D4ED8)  // blue-700

    val cTimeBg = Color(0xFFF5F3FF)    // purple-50
    val cTimeFg = Color(0xFF6D28D9)    // purple-700

    val cKmBg = Color(0xFFECFDF5)      // emerald-50
    val cKmFg = Color(0xFF047857)      // emerald-700

    // Dynamic speed colors (based on km/h)
    val (avgBg, avgFg) = speedColors(item.avgSpeedKmh)
    val (maxBg, maxFg) = speedColors(item.maxSpeedKmh)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color(0xFFE5E7EB), shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // Top gradient header (subtle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                DriveWiseGreen.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // left "badge dot"
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DriveWiseGreen.copy(alpha = 0.9f))
                    )
                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // right chevron pill
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = DriveWiseGreen.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "View",
                                style = MaterialTheme.typography.labelMedium,
                                color = DriveWiseGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = DriveWiseGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricPill(
                        icon = Icons.Filled.Timer,
                        label = "Süre",
                        value = formatHms(item.durationSec),
                        bg = cTimeBg,
                        fg = cTimeFg
                    )

                    MetricPill(
                        label = "Points",
                        value = item.pointsSaved.toString(),
                        bg = cPointsBg,
                        fg = cPointsFg
                    )

                    // ✅ Total KM
                    MetricPill(
                        icon = Icons.Filled.Route,
                        label = "KM",
                        value = formatKm(item.totalKm),
                        bg = cKmBg,
                        fg = cKmFg
                    )

                    // ✅ Avg speed dynamic color
                    MetricPill(
                        icon = Icons.Filled.Speed,
                        label = "Avg",
                        value = "${item.avgSpeedKmh} km/h",
                        bg = avgBg,
                        fg = avgFg
                    )

                    // ✅ Max speed dynamic color
                    MetricPill(
                        icon = Icons.Filled.Speed,
                        label = "Max",
                        value = "${item.maxSpeedKmh} km/h",
                        bg = maxBg,
                        fg = maxFg
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    bg: Color,
    fg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = fg.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280),
                maxLines = 1
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 0–10  => grey (slow/stop)
 * 10–30 => green (normal)
 * 30+   => amber (fast)
 *
 * Returns Pair(bg, fg)
 */
private fun speedColors(speedKmh: Int): Pair<Color, Color> = when {
    speedKmh < 10 -> Color(0xFFF3F4F6) to Color(0xFF6B7280) // grey-100 / grey-500
    speedKmh < 30 -> Color(0xFFEFFAF2) to DriveWiseGreen     // green-50 / brand green
    else -> Color(0xFFFFFBEB) to Color(0xFFF59E0B)          // amber-50 / amber-500
}

private fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    fun p2(v: Int) = if (v < 10) "0$v" else v.toString()
    return "${p2(h)}:${p2(m)}:${p2(s)}"
}

private fun formatKm(value: Double): String {
    // 1 decimal: 1.4, 0.0 etc
    val rounded = (value * 10).toInt() / 10.0
    val parts = rounded.toString().split(".")
    val dec = (parts.getOrNull(1) ?: "0").padEnd(1, '0').take(1)
    return "${parts[0]}.$dec"
}
