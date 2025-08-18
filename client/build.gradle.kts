plugins {
    id("plugin-multiplatform-library")
    id("plugin-multiplatform-publish")
    id("plugin-compose")
}

android.namespace = "com.pedrobneto.mock.engine.client"

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.jetbrains.serialization)
        implementation(libs.ktor.client.core)

        implementation(compose.material3)
        implementation(compose.runtime)
    }
}
