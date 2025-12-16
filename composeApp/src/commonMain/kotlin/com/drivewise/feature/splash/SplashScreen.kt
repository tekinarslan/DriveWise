package com.drivewise.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.core.OnboardingStore
import com.drivewise.feature.home.HomeScreen
import com.drivewise.feature.language.LanguageScreen
import com.drivewise.feature.location_permission.LocationPermissionScreen
import com.drivewise.permission.LocationPermissionChecker
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

class SplashScreen(private val store: OnboardingStore) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.current!!
        val checker: LocationPermissionChecker = koinInject()

        LaunchedEffect(Unit) {
            delay(900)
            if (!store.isDone()) {
                nav.replace(LanguageScreen(store))
                return@LaunchedEffect
            }

            // ✅ Onboarding done
            if (checker.isGranted()) {
                nav.replace(HomeScreen())
            } else {
                nav.replace(LocationPermissionScreen())
            }
        }

        SplashContent(
            title = "DriveWise",
            subtitle = tr("Drive smart, pass with ease", store) // slogan (dile göre)
            ,
            welcome = tr("Welcome", store),
            footer = tr("Secure & Private", store),
            progress = 0.55f
        )
    }
}

@Composable
private fun SplashContent(
    title: String,
    subtitle: String,
    welcome: String,
    footer: String,
    progress: Float
) {
    val bg = Brush.radialGradient(
        colors = listOf(Color(0xFFEFF7F0), Color(0xFFFFFFFF)),
        radius = 1400f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Icon card
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF45D26A),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF121826)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = welcome,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF121826)
            )

            Spacer(Modifier.weight(1f))

            // Progress bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF8BE6A3))
                )
            }

            Spacer(Modifier.height(18.dp))

            // Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar, // istersen lock icon ekleriz
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = footer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(Modifier.height(26.dp))
        }
    }
}

/**
 * Splash dilini, store'da seçili dile göre basitçe çeviriyoruz.
 * (Language ekranına geçince zaten kullanıcı seçiyor.)
 */
private fun tr(en: String, store: OnboardingStore): String {
    val lang = store.getLanguage().code
    return when (en) {
        "Drive smart, pass with ease" -> when (lang) {
            "de" -> "Fahre klug, bestehe leicht."
            "tr" -> "Akıllı sürün, kolayca geçin."
            else -> "Drive smart, pass with ease."
        }

        "Welcome" -> when (lang) {
            "de" -> "Willkommen"
            "tr" -> "Hoş Geldiniz"
            else -> "Welcome"
        }

        "Secure & Private" -> when (lang) {
            "de" -> "Sicher & Privat"
            "tr" -> "Güvenli ve Gizli"
            else -> "Secure & Private"
        }

        else -> en
    }
}
