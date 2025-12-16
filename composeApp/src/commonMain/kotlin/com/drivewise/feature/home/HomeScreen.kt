package com.drivewise.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.design.theme.DriveWiseGreen
import kotlinx.coroutines.delay

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current

        var isRunning by remember { mutableStateOf(false) }
        var elapsedSeconds by remember { mutableStateOf(0) }
        var km by remember { mutableStateOf(0.0) }
        var gpsReady by remember { mutableStateOf(true) } // şimdilik sabit

        LaunchedEffect(isRunning) {
            while (isRunning) {
                delay(1000)
                elapsedSeconds += 1
                // Demo: km artışı (gerçek GPS gelince kaldıracağız)
                km = (km + 0.01).coerceAtMost(999.9)
            }
        }

        val timeText = remember(elapsedSeconds) { formatHms(elapsedSeconds) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // Top bar
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

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Sürüşe Hazır Mısın?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Çevreni kontrol et ve deneme dersi için\nkaydı başlat.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(22.dp))

            // Time card (big)
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
                            text = timeText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
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

            // Small cards row
            Row(Modifier.fillMaxWidth()) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Timer,
                    label = "KM",
                    value = formatKm(km),
                    valueColor = Color(0xFF111827)
                )

                Spacer(Modifier.width(12.dp))

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.GpsFixed,
                    label = "GPS SİNYALİ",
                    value = if (gpsReady) "Hazır" else "Zayıf",
                    valueColor = if (gpsReady) DriveWiseGreen else Color(0xFFF59E0B),
                    showDot = true,
                    dotColor = if (gpsReady) DriveWiseGreen else Color(0xFFF59E0B)
                )
            }

            Spacer(Modifier.height(26.dp))

            // Big Play button with glow
            val glow = Brush.radialGradient(
                colors = listOf(
                    DriveWiseGreen.copy(alpha = 0.35f),
                    Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(glow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        isRunning = true
                    },
                    containerColor = DriveWiseGreen,
                    shape = CircleShape,
                    modifier = Modifier.size(88.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start",
                        tint = Color(0xFF0B1220),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Dersi Başlat",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(14.dp))

            // Stop button (disabled until running)
            Button(
                onClick = { isRunning = false },
                enabled = isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5E7EB),
                    disabledContainerColor = Color(0xFFE5E7EB),
                    contentColor = Color(0xFF111827),
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                Icon(Icons.Filled.Square, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Dersi Bitir",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.weight(1f))

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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
    return rounded.toString()
}

private fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60

    return "${pad2(h)}:${pad2(m)}:${pad2(s)}"
}

private fun pad2(value: Int): String =
    if (value < 10) "0$value" else value.toString()

