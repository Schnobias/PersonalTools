plugins { id("com.android.application") }
android {
    namespace = "nl.schnobias.personaltools"
    compileSdk = 34
    defaultConfig {
        applicationId = "nl.schnobias.personaltools"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies { testImplementation("junit:junit:4.13.2") }
