plugins {
    id("plugin-multiplatform-library")
    id("plugin-multiplatform-publish")
}

android.namespace = "com.pedrobneto.mock.engine.annotation"

kotlin {
    androidTarget()
    jvm()
    js { browser() }
}
