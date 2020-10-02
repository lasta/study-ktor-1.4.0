val ktor_version: String by project
val ideaActive: Boolean by project.extra

plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}
group = "me.lasta"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

kotlin {
    val nativeTarget = when (System.getProperty("os.name")) {
        "Mac OS X" -> macosX64("native")
        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        compilations {
            all {
                kotlinOptions.verbose = true
            }
            getByName("main") {
                @Suppress("UNUSED_VARIABLE")
                val libcurl by cinterops.creating {
                    defFile = File(projectDir, "src/nativeMain/interop/libcurl.def")
                    includeDirs.headerFilterOnly(
                        listOf(
                            "/opt/local/include/curl",
                            "/usr/local/include/curl",
                            "/usr/include/curl",
                            "/usr/local/opt/curl/include/curl",
                            "/usr/include/x86_64-linux-gnu/curl",
                            "/usr/lib/x86_64-linux-gnu/curl",
                            "/usr/local/Cellar/curl/7.62.0/include/curl",
                            "/usr/local/Cellar/curl/7.63.0/include/curl",
                            "/usr/local/Cellar/curl/7.65.3/include/curl",
                            "/usr/local/Cellar/curl/7.66.0/include/curl"
                        )
                    )
                }
            }
        }
        binaries {
            executable {
                baseName = "bootstrap"
                entryPoint = "me.lasta.studyktor.main"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
        val ktorVersion = "1.4.1"

        @kotlin.Suppress("UNUSED_VARIABLE")
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-network-tls:$ktorVersion")
                implementation("io.ktor:ktor-client-json:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
            }
        }

        @kotlin.Suppress("UNUSED_VARIABLE")
        val nativeTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
            }
        }

        // Hack: register the Native interop klibs as outputs of Kotlin source sets:
//        if (System.getProperty("idea.active") == "true") {
//            val libcurlInterop by creating
//            getByName("nativeMain").dependsOn(libcurlInterop)
//            apply(from = "$rootDir/gradle/interop-as-source-set-klib.gradle")
//            (project.ext.get("registerInteropAsSourceSetOutput") as groovy.lang.Closure<*>).invoke(
//                "libcurl",
//                libcurlInterop
//            )
//        }
    }
}

tasks {
    wrapper {
        gradleVersion = "6.6.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
