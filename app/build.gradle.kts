import java.util.Base64

plugins {
    id("com.android.application")
}

android {
    namespace = "com.github.nclok1405.textviewer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.nclok1405.textviewer"
        minSdk = 19
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (System.getenv("ENV_SIGN_KEYSTORE_FILE") != null) {
                // Sign from keystore file
                project.logger.lifecycle("ENV_SIGN_KEYSTORE_FILE environment variable exists, enabling apk signing via keystore file")
                storeFile = File(System.getenv("ENV_SIGN_KEYSTORE_FILE"))
                storePassword = System.getenv("ENV_SIGN_STORE_PASSWORD")
                keyAlias = System.getenv("ENV_SIGN_KEY_ALIAS")
                keyPassword = System.getenv("ENV_SIGN_KEY_PASSWORD")
            }
            else if (System.getenv("ENV_SIGN_KEYSTORE_BASE64") != null) {
                // Sign from environment variables:
                // https://takusan.negitoro.dev/posts/github_actions_android_release_apk/
                project.logger.lifecycle("ENV_SIGN_KEYSTORE_BASE64 environment variable exists, enabling apk signing via base64")

                val tempKeyStore = File.createTempFile("temp_keystore", ".kjs")
                project.logger.lifecycle("keystore file will be created at: {}", tempKeyStore)
                tempKeyStore.createNewFile()

                System.getenv("ENV_SIGN_KEYSTORE_BASE64").let { base64 ->
                    val decoder = Base64.getDecoder()
                    tempKeyStore.writeBytes(decoder.decode(base64))
                }

                storeFile = tempKeyStore
                storePassword = System.getenv("ENV_SIGN_STORE_PASSWORD")
                keyAlias = System.getenv("ENV_SIGN_KEY_ALIAS")
                keyPassword = System.getenv("ENV_SIGN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
}