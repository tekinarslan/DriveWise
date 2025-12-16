package com.drivewise.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.core.OnboardingStore
import com.drivewise.design.theme.DriveWiseGreen
import com.drivewise.feature.privacy.PrivacyScreen

class OnboardingIntroScreen(
    private val store: OnboardingStore
) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current!!
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✅ Scrollable content (top part)
            Column(
                modifier = Modifier
                    .weight(1f) // remaining height
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Uygulama nasıl çalışır",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF121826)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Ehliyet yolculuğunuzda kişisel rehberiniz",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(24.dp))

                FeatureCard(
                    icon = Icons.Filled.LocationOn,
                    title = "Rota ve Hız Takibi",
                    description = "Sürüş derslerinde rotanızı ve hızınızı takip edin."
                )

                FeatureCard(
                    icon = Icons.Filled.Check,
                    title = "Sınav Odaklı Özet",
                    description = "Her ders sonrası 2 dakikalık sınav odaklı özet."
                )

                FeatureCard(
                    icon = Icons.Filled.VisibilityOff,
                    title = "Anonim Deneme",
                    description = "Bir kez anonim olarak deneyin – hesap gerekmez.",
                    highlighted = true,
                    badge = "ÜCRETSİZ"
                )

                Spacer(Modifier.height(16.dp))
            }

            // ✅ Fixed bottom area (always visible)
            Button(
                onClick = { nav.push(PrivacyScreen(store)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriveWiseGreen)
            ) {
                Text(
                    "Devam Et  →",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "%100 Veri Gizliliği",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    highlighted: Boolean = false,
    badge: String? = null
) {
    val bgColor =
        if (highlighted) Color(0xFFEFFAF2) else Color.White

    val borderColor =
        if (highlighted) DriveWiseGreen else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (highlighted) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0FDF4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DriveWiseGreen
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE7F8ED))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = DriveWiseGreen
                            )
                        }
                    }
                }

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
