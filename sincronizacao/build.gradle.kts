plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":formato"))
    implementation(project(":armazenamento"))
    implementation(project(":indice"))
}
