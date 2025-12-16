package com.drivewise.feature.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.core.OnboardingStore
import com.drivewise.design.theme.DriveWiseGreen
import com.drivewise.feature.location_permission.LocationPermissionScreen

class PrivacyScreen(
    private val store: OnboardingStore
) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current!!
        store.setDone(true)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GİZLİLİK",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Icon + glow
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        DriveWiseGreen.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFFAF2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = DriveWiseGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Small shield overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 28.dp, y = 28.dp)
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

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Güvenli sürüş için\nkonum verileri",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF121826)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sınav odaklı geri bildirim sağlamak için sürüş derslerinizi analiz ediyoruz. Verileriniz şu şekilde işlenir:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(20.dp))

                PrivacyItem(
                    icon = Icons.Filled.LocationOn,
                    title = "Rota Kaydı",
                    description = "Gelişmeniz gereken yerleri tam olarak göstermek için sürüşünüzü haritalarız."
                )

                PrivacyItem(
                    icon = Icons.Filled.TipsAndUpdates,
                    title = "Kişisel Geri Bildirim",
                    description = "Karşılaştığınız trafik durumlarına özel ipuçları alırsınız."
                )

                PrivacyItem(
                    icon = Icons.Filled.Lock,
                    title = "%100 Gizlilik",
                    description = "Verileriniz şifrelenir ve GDPR ile tam uyumludur."
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        store.setDone(true)
                        nav.push(LocationPermissionScreen())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriveWiseGreen
                    )
                ) {
                    Text(
                        "Devam Et  →",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Gizlilik politikamız hakkında daha fazla bilgi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.clickable {
                        // TODO open privacy policy
                    }
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PrivacyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFFAF2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DriveWiseGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF121826)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
