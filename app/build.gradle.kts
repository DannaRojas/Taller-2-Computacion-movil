import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val mapboxProperties = Properties().apply {
    val file = rootProject.file("mapbox.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val publicToken = (mapboxProperties.getProperty("MAPBOX_ACCESS_TOKEN")
    ?: providers.environmentVariable("MAPBOX_ACCESS_TOKEN").orNull ?: "").trim()

android {
    namespace = "edu.javeriana.rutafoto"
    compileSdk = 37
    defaultConfig {
        applicationId = "edu.javeriana.rutafoto"
        minSdk = 29
        targetSdk = 37
        versionCode = 9
        versionName = "1.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "mapbox_access_token", publicToken)
    }
    buildFeatures { compose = true; resValues = true }
    packaging { jniLibs { useLegacyPackaging = true } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt.coil3:coil-compose:3.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("androidx.camera:camera-core:1.6.0")
    implementation("androidx.camera:camera-camera2:1.6.0")
    implementation("androidx.camera:camera-lifecycle:1.6.0")
    implementation("androidx.camera:camera-compose:1.6.0")
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("com.mapbox.maps:android-ndk27:11.31.1")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.31.1")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
