import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs += "-Xexpect-actual-classes"
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    // iOS (future)
    // listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    //     target.binaries.framework { baseName = "shared"; isStatic = true }
    // }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.multiplatform.settings)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.play.services.location)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            // SQLDelight web-worker-driver is wired up separately
            // See WebDatabaseDriverFactory for the wasmJs actual
        }
    }
}

android {
    namespace = "com.eatai.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("EatAiDatabase") {
            packageName.set("com.eatai.shared.db")
            // wasmJs target uses web-worker-driver, wired via expect/actual
        }
    }
}
