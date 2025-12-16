package com.drivewise.feature.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.drivewise.core.Language
import com.drivewise.core.OnboardingStore
import com.drivewise.feature.onboarding.OnboardingIntroScreen

class LanguageScreen(private val store: OnboardingStore) : Screen {
  @Composable
  override fun Content() {
    val nav = LocalNavigator.current!!
    var selected by remember { mutableStateOf(store.getLanguage()) }

      Column(Modifier.fillMaxSize().padding(20.dp)) {
          Text("Welcome", style = MaterialTheme.typography.headlineMedium)
          Spacer(Modifier.height(6.dp))
          Text("Select language")

          Spacer(Modifier.height(20.dp))

          Language.entries.forEach { lang ->
              Card(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                      .clickable { selected = lang }
              ) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(14.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                      Text(lang.label)
                      RadioButton(selected = selected == lang, onClick = { selected = lang })
                  }
              }
          }

          Spacer(Modifier.weight(1f))

          Button(
              modifier = Modifier.fillMaxWidth().height(52.dp),
              onClick = {
                  store.setLanguage(selected)
                  nav.push(OnboardingIntroScreen(store))
              }
          ) { Text("Continue") }
      }
  }
}