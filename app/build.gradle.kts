plugins { id("com.android.application") }
android {
    namespace = "nl.schnobias.personaltools"
    compileSdk = 36
    defaultConfig {
        applicationId = "nl.schnobias.personaltools"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "0.7.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies { implementation("com.google.mlkit:genai-prompt:1.0.0-beta4"); testImplementation("junit:junit:4.13.2") }


