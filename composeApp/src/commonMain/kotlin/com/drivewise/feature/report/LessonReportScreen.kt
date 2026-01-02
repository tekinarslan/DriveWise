package com.drivewise.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.design.theme.DriveWiseGreen
import com.drivewise.design.theme.DriveWiseGreenSoft
import com.drivewise.map.RouteMap
import org.koin.core.parameter.parametersOf

class LessonReportScreen(
    private val lessonId: String
) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current!!
        val model: LessonReportScreenModel = koinScreenModel(parameters = { parametersOf(lessonId) })

        val st by model.state.collectAsState()
        val route by model.route.collectAsState()

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
                .verticalScroll(scroll)
                .padding(18.dp)
        ) {

            Text(
                text = "Lesson Report",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = st.lessonId,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(16.dp))

            when {
                st.loading -> {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                st.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Hata", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(Modifier.height(6.dp))
                            Text(st.error!!, color = Color(0xFF6B7280))
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { model.refreshAll() }) { Text("Retry") }
                        }
                    }
                }

                else -> {
                    SummaryRow(
                        durationSec = st.durationSec,
                        km = st.totalKm,
                        avgSpeed = st.avgSpeedKmh,
                        maxSpeed = st.maxSpeedKmh
                    )

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Raw Stats", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            Spacer(Modifier.height(10.dp))
                            StatLine("Points saved", st.pointsCount.toString())
                            StatLine("Duration", formatHms(st.durationSec))
                            StatLine("Total km", formatKm(st.totalKm))
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ---- ROUTE SECTION (MVP) ----
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Route (snapped)", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            Spacer(Modifier.height(10.dp))

                            when {
                                route.loading -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(Modifier.width(10.dp))
                                        Text("Matching…", color = Color(0xFF6B7280))
                                    }
                                }

                                route.error != null -> {
                                    Text("Matching failed: ${route.error}", color = Color(0xFFB91C1C))
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = { model.buildMatchedRoute(force = true) }) {
                                        Text("Retry Matching")
                                    }
                                }

                                route.polyline.isEmpty() -> {
                                    Text("No route points.", color = Color(0xFF6B7280))
                                }

                                else -> {
                                    // Şimdilik: iOS placeholder bile olsa RouteMap expect/actual ile çözüyoruz.
                                    RouteMap(
                                        points = route.polyline,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row {
                        Button(
                            onClick = { nav.pop() },
                            modifier = Modifier.height(54.dp).weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DriveWiseGreen)
                        ) { Text("Back", fontWeight = FontWeight.Bold) }

                        Spacer(Modifier.width(10.dp))

                        Button(
                            onClick = { nav.push(LessonHistoryScreen()) },
                            modifier = Modifier.height(54.dp).weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DriveWiseGreenSoft)
                        ) { Text("History", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SummaryRow(durationSec: Int, km: Double, avgSpeed: Double, maxSpeed: Double) {
    Row(Modifier.fillMaxWidth()) {
        SummaryCard("SÜRE", formatHms(durationSec), Modifier.weight(1f))
        Spacer(Modifier.width(10.dp))
        SummaryCard("Toplam KM", formatKm(km), Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth()) {
        SummaryCard("ORT. HIZ", "${avgSpeed.toInt()} km/h", Modifier.weight(1f))
        Spacer(Modifier.width(10.dp))
        SummaryCard("MAX HIZ", "${maxSpeed.toInt()} km/h", Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF6B7280))
        Text(value, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(6.dp))
}

private fun formatKm(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    val parts = rounded.toString().split(".")
    val dec = (parts.getOrNull(1) ?: "0").padEnd(1, '0').take(1)
    return "${parts[0]}.$dec"
}

private fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    fun p2(v: Int) = if (v < 10) "0$v" else v.toString()
    return "${p2(h)}:${p2(m)}:${p2(s)}"
}
