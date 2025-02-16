plugins {
    id("plugin-android-application")
    id("plugin-compose")
}

android {
    namespace = "com.pedrbneto.mock.engine.android.app"

    defaultConfig {
        applicationId = "com.luli.mock.engine.android"
    }

    signingConfigs {
        named("debug") {
            storeFile = file("$rootDir/distribution/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("debug") }
    }
}

dependencies {
    implementation(project(":client"))
}
