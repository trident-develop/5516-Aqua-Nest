import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.androidx.splashscreen)
    implementation(libs.play.services.ads)
    implementation(libs.firebase.messaging)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.ludia.jurassicwo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ludia.jurassicwo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

afterEvaluate {
    tasks.named("uploadCrashlyticsMappingFileRelease")
        .configure { enabled = false }
}

afterEvaluate {
    tasks.named("bundleRelease").configure {
        finalizedBy("removeProguardMap")
    }
}

tasks.register("removeProguardMap") {
    notCompatibleWithConfigurationCache("Uses ZipFile/ZipOutputStream from script scope")
    doLast {
        val generatedAabPath = "${projectDir}/release"
        val aabFile = file("${generatedAabPath}/composeApp-release.aab")

        val zipFile = file("${generatedAabPath}/composeApp-release.zip")
        val savedProguardMapFile = file("${generatedAabPath}/proguard.map")
        val tempZipFilePath = file("${generatedAabPath}/composeApp-release-temp.zip")
        val targetFilePath = "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map"

        aabFile.renameTo(zipFile)

        val zf = ZipFile(zipFile)
        val zos = ZipOutputStream(tempZipFilePath.outputStream())
        try {
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.name != targetFilePath) {
                    zos.putNextEntry(ZipEntry(entry.name))
                    zf.getInputStream(entry).use { it.copyTo(zos) }
                    zos.closeEntry()
                } else {
                    zf.getInputStream(entry).use { input ->
                        savedProguardMapFile.outputStream().use { input.copyTo(it) }
                    }
                }
            }
        } finally {
            zos.close()
            zf.close()
        }

        zipFile.delete()
        tempZipFilePath.renameTo(aabFile)
    }
}