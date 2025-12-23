package com.drivewise.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.drivewise.design.theme.DriveWiseGreen
import com.drivewise.permission.areNotificationsEnabled
import com.drivewise.permission.isNotificationPermissionRequired
import com.drivewise.permission.rememberNotificationPermissionRequester

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val model: HomeScreenModel = koinScreenModel()

        val driveSessionState by model.state.collectAsState()
        val debugSimulate by model.debugSimulateFlow.collectAsState() // ✅ FIX

        val notifRequester = rememberNotificationPermissionRequester { _ ->
            // MVP: result sonrası ek işlem yok. Banner zaten koşula göre görünür.
        }

        var showNotifBanner by remember { mutableStateOf(false) }

        // İlk girişte check
        LaunchedEffect(Unit) {
            showNotifBanner = isNotificationPermissionRequired() && !areNotificationsEnabled()
        }

        // İstersen her Start/Stop sonrası tekrar check (settings’ten dönünce de tetiklenebilir)
        LaunchedEffect(driveSessionState.isRunning) {
            showNotifBanner = isNotificationPermissionRequired() && !areNotificationsEnabled()
        }

        val scrollState = rememberScrollState()
        val isRunning = driveSessionState.isRunning

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
                .verticalScroll(scrollState) // ✅ scroll
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // Top title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Deneme Dersi",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.weight(1f))
            }

            // Notification banner
            if (showNotifBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Enable notifications for background tracking",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "So DriveWise can keep tracking reliably and show the live session status.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                        TextButton(onClick = { notifRequester.openSettings() }) {
                            Text("Open Settings")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // Debug simulation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Debug: Route Simulation", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (debugSimulate) "Fake GPS aktif (Berlin test rotası)"
                            else "Gerçek GPS aktif",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Switch(
                        checked = debugSimulate,
                        onCheckedChange = { enabled ->
                            model.toggleSimulation(enabled)
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Sürüşe Hazır Mısın?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Çevreni kontrol et ve deneme dersi için\nkaydı başlat.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(22.dp))

            // Time card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Timer,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "SÜRE",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = formatHms(driveSessionState.elapsedSeconds),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF111827)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Small cards
            Row(Modifier.fillMaxWidth()) {

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Timer,
                    label = "KM",
                    value = formatKm(driveSessionState.km),
                    valueColor = Color(0xFF111827)
                )

                Spacer(Modifier.width(12.dp))

                val gpsLabel = when {
                    debugSimulate -> "Simülasyon"
                    driveSessionState.gpsReady -> "Gercek Sürüş"
                    else -> "Zayıf"
                }

                val gpsColor = when {
                    debugSimulate -> Color(0xFFF59E0B)
                    driveSessionState.gpsReady -> DriveWiseGreen
                    else -> Color(0xFFF59E0B)
                }

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.GpsFixed,
                    label = "GPS SİNYALİ",
                    value = gpsLabel,
                    valueColor = gpsColor,
                    showDot = true,
                    dotColor = gpsColor
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("${driveSessionState.currentSpeedKmh.toInt()} km/h")
            Spacer(Modifier.height(10.dp))

            // START BUTTON
            FloatingActionButton(
                onClick = {
                    if (!isRunning) {
                        model.start()
                    }
                },
                containerColor = if (!isRunning) DriveWiseGreen else Color(0xFFE5E7EB),
                contentColor = if (!isRunning) Color.Black else Color(0xFF9CA3AF),
                shape = CircleShape,
                modifier = Modifier
                    .size(88.dp)
                    .alpha(if (!isRunning) 1f else 0.6f)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Dersi Başlat",
                color = if (!isRunning) Color(0xFF111827) else Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            // Stop button
            Button(
                onClick = { model.stop() },
                enabled = isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) DriveWiseGreen else Color(0xFFE5E7EB),
                    disabledContainerColor = Color(0xFFE5E7EB),
                    contentColor = Color.Black,
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                Icon(Icons.Filled.Square, null)
                Spacer(Modifier.width(8.dp))
                Text("Dersi Bitir", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(18.dp))

            // Privacy notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFFAF2))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F8ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = DriveWiseGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "GİZLİLİK",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Arka planda kayıt yapıyor. Sürüş verilerin yerel olarak şifrelenir ve cihazında kalır.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }

            // bottom padding so last card isn't glued
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    showDot: Boolean = false,
    dotColor: Color = DriveWiseGreen
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDot) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = valueColor
                )
            }
        }
    }
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
    return "${pad2(h)}:${pad2(m)}:${pad2(s)}"
}

private fun pad2(value: Int): String = if (value < 10) "0$value" else value.toString()
