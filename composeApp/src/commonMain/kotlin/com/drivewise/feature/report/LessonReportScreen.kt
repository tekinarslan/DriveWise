package com.drivewise.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.design.theme.DriveWiseGreen
import com.drivewise.map.RouteMap
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
class LessonReportScreen(
    private val lessonId: String
) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current!!
        val model: LessonReportScreenModel =
            koinScreenModel(parameters = { parametersOf(lessonId) })

        val st by model.state.collectAsState()
        val route by model.route.collectAsState()
        val scroll = rememberScrollState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Lesson Report", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { nav.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF4F5F7))
                    .verticalScroll(scroll)
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                when {
                    st.loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    st.error != null -> {
                        ErrorState(st.error!!) { model.refreshAll() }
                    }
                    else -> {
                        // Özet Kartları
                        SummarySection(st)

                        Spacer(Modifier.height(20.dp))

                        // Harita Bölümü
                        RouteCard(route, model)

                        Spacer(Modifier.height(20.dp))

                        // Ekstra Detaylar
                        RawStatsCard(st)

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(st: LessonReportState) {
    val (avgBg, avgFg) = speedColors(st.avgSpeedKmh.toInt())
    val (maxBg, maxFg) = speedColors(st.maxSpeedKmh.toInt())

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricPill("Süre", formatHms(st.durationSec), Color(0xFFF5F3FF), Color(0xFF6D28D9), Icons.Filled.Timer)
        MetricPill("KM", formatKm(st.totalKm), Color(0xFFECFDF5), Color(0xFF047857), Icons.Filled.Route)
        MetricPill("Points", st.pointsCount.toString(), Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        MetricPill("Avg", "${st.avgSpeedKmh.toInt()} km/h", avgBg, avgFg, Icons.Filled.Speed)
        MetricPill("Max", "${st.maxSpeedKmh.toInt()} km/h", maxBg, maxFg, Icons.Filled.Speed)
    }
}

@Composable
private fun RouteCard(route: RouteMatchState, model: LessonReportScreenModel) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Route Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FAFB)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    route.loading -> CircularProgressIndicator(modifier = Modifier.size(30.dp))
                    route.error != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Map Error", color = Color.Red)
                            Button(onClick = { model.buildMatchedRoute(true) }) { Text("Retry") }
                        }
                    }
                    else -> RouteMap(points = route.polyline, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun RawStatsCard(st: LessonReportState) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Raw Statistics", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(Modifier.height(12.dp))
            StatLine("Lesson ID", st.lessonId)
            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            StatLine("Total Duration", formatHms(st.durationSec))
            StatLine("Points Saved", st.pointsCount.toString())
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    bg: Color,
    fg: Color,
    icon: ImageVector? = null
) {
    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, tint = fg.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(Modifier.width(6.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
        Text(value, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Bir hata oluştu", fontWeight = FontWeight.Bold)
        Text(error, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
    }
}

private fun speedColors(speedKmh: Int): Pair<Color, Color> = when {
    speedKmh < 10 -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    speedKmh < 30 -> Color(0xFFEFFAF2) to DriveWiseGreen
    else -> Color(0xFFFFFBEB) to Color(0xFFF59E0B)
}

private fun formatKm(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return "$rounded"
}

private fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}