plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.simplefreeapps.pcmsfx"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    testImplementation(libs.junit)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = project.findProperty("group")?.toString()?.takeIf { it.isNotBlank() && it != "ComposePcmSfx" }
                    ?: "com.github.simple-free-apps"
                artifactId = "compose-pcm-sfx"
                version = project.findProperty("version")?.toString()?.takeIf { it.isNotBlank() && it != "unspecified" }
                    ?: "1.0.1"
            }
        }
    }
}
