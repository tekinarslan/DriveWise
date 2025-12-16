import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // ✅ SQLDelight Android driver
            implementation(libs.sqldelight.android.driver)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // ✅ Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)

            // ✅ SQLDelight runtime
            implementation(libs.sqldelight.runtime)

            // ✅ Koin (CMP)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        // iOS source set'i explicit tanımlı değil; KMP template genelde iosMain'i otomatik yaratır.
        // Buraya eklemek daha net olsun diye iosMain dependencies ekleyelim:
        iosMain.dependencies {
            // ✅ SQLDelight iOS driver
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.drivewise.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.drivewise.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true          // ✅ R8 ON
                isShrinkResources = true        // ✅ (opsiyonel ama önerilir)

                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// ✅ SQLDelight configuration
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.drivewise.app.db")
            // sourceFolders.set(listOf("sqldelight")) // default zaten bu, istersen açabilirsin
        }
    }
}
