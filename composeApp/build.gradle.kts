import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Read MAPS_API_KEY from local.properties (gitignored, machine-local).
// Falls back to empty string so the project still configures without the key.
val mapsApiKey: String = run {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
    props.getProperty("MAPS_API_KEY", "")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "11" }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    // iOS (future)
    // listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    //     target.binaries.framework { baseName = "ComposeApp"; isStatic = true }
    // }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(projects.shared)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)
            implementation(libs.play.services.location)
        }

        wasmJsMain.dependencies {
            // Google Maps JS API is loaded via <script> tag in index.html.
        }
    }
}

android {
    namespace = "com.eatai"
    compileSdk = 35

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.eatai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Surfaces MAPS_API_KEY into AndroidManifest.xml (${MAPS_API_KEY}).
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Inject MAPS_API_KEY into wasm/index.html at build time.
// The web Maps key is restricted by HTTP referrer in Cloud Console, so embedding
// it in the served HTML is fine — but we still keep it out of source control.
tasks.matching { it.name == "wasmJsProcessResources" }.configureEach {
    val key = mapsApiKey
    inputs.property("MAPS_API_KEY", key)
    (this as org.gradle.api.tasks.Copy).filesMatching("index.html") {
        filter { line -> line.replace("__GOOGLE_MAPS_API_KEY__", key) }
    }
}
