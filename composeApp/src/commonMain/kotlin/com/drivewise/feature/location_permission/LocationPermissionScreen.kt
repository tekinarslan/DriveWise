package com.drivewise.feature.location_permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
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
import com.drivewise.feature.home.HomeScreen
import com.drivewise.permission.LocationPermissionStatus
import com.drivewise.permission.rememberLocationPermissionRequester
import com.drivewise.permission.NotificationPermissionStatus
import com.drivewise.permission.isNotificationPermissionRequired
import com.drivewise.permission.rememberNotificationPermissionRequester
import kotlinx.coroutines.launch

class LocationPermissionScreen : Screen {

    @Composable
    override fun Content() {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var showSettingsDialog by remember { mutableStateOf(false) }

        val nav = LocalNavigator.current!!

        // ✅ notification requester (location granted sonrası çağıracağız)
        val notifRequester = rememberNotificationPermissionRequester { _ ->
            // MVP: sonuç ne olursa olsun Home'a geç
            nav.replace(HomeScreen())
        }

        var notifRequestTriggered by remember { mutableStateOf(false) }

        fun goNext() {
            // Android <13 vs iOS vs Android 13+ kontrolü
            if (isNotificationPermissionRequired() && !notifRequestTriggered) {
                notifRequestTriggered = true
                notifRequester.request()
            } else {
                nav.replace(HomeScreen())
            }
        }

        val locationRequester = rememberLocationPermissionRequester { status ->
            when (status) {
                LocationPermissionStatus.GRANTED -> {
                    goNext()
                }

                LocationPermissionStatus.DENIED -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Konum izni gerekli. Lütfen izin verin.")
                    }
                }

                LocationPermissionStatus.DENIED_PERMANENTLY -> {
                    showSettingsDialog = true
                }
            }
        }

        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(24.dp))

                // Icon + glow
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        DriveWiseGreen.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFFAF2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = DriveWiseGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Lock overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 36.dp, y = 36.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Badge (iki izin vurgusu yapalım)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE7F8ED))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "KONUM + BİLDİRİM İZNİ",
                        style = MaterialTheme.typography.labelMedium,
                        color = DriveWiseGreen
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Arka planda kayıt için\nizinleri etkinleştirin",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF121826)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Sürüş dersiniz sırasında konumu kaydederiz. Dersi arka planda takip edebilmek ve durum bilgisini gösterebilmek için bildirim izni de isteyebiliriz.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(20.dp))

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFFAF2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = DriveWiseGreen
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Konum (GPS)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Rotayı ve hız profilini çıkarırız.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFFAF2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = DriveWiseGreen
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Bildirim (Notification)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Arka planda çalıştığını gösterir ve dersi durdurmanı sağlar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Primary CTA: önce konum ister, granted olursa notification’a geçer
                Button(
                    onClick = { locationRequester.request() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DriveWiseGreen)
                ) {
                    Text(
                        "İzinleri Etkinleştir  →",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Konum izni kapalı") },
                text = { Text("Konum izni Ayarlar’dan açılmalı. Ayarlar’a gitmek ister misin?") },
                confirmButton = {
                    TextButton(onClick = {
                        showSettingsDialog = false
                        locationRequester.openSettings()
                    }) { Text("Ayarlar'a Git") }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text("İptal") }
                }
            )
        }
    }
}
