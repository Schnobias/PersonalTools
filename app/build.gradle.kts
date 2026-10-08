plugins { id("com.android.application") }
android {
    namespace = "nl.schnobias.personaltools"
    compileSdk = 36
    defaultConfig {
        applicationId = "nl.schnobias.personaltools"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies { testImplementation("junit:junit:4.13.2") }
