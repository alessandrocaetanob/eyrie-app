import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

val minSdkVersion = libs.versions.minSdk.get().toInt()
val compileSdkVersion = libs.versions.compileSdk.get().toInt()
val compileSdkMinorVersion = libs.versions.compileSdkMinor.get().toInt()

subprojects {
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            namespace = "io.hyperius.eyrie.${project.name}"
            compileSdk {
                version = release(compileSdkVersion) { minorApiLevel = compileSdkMinorVersion }
            }
            defaultConfig.minSdk = minSdkVersion
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            compileSdk {
                version = release(compileSdkVersion) { minorApiLevel = compileSdkMinorVersion }
            }
            defaultConfig.minSdk = minSdkVersion
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.jvmTarget = JvmTarget.JVM_17
        }
    }
}
