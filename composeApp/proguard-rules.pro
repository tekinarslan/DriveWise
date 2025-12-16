# Kotlin metadata (reflection / some libs)
-keep class kotlin.Metadata { *; }

# Jetpack Compose (genelde gerekmiyor ama güvenli)
-keep class androidx.compose.** { *; }

# SQLDelight (genelde ok ama sürüme göre gerekebilir)
-keep class app.cash.sqldelight.** { *; }

# Koin (reflection kullanıyorsan önemli)
-keep class org.koin.** { *; }
-dontwarn org.koin.**
