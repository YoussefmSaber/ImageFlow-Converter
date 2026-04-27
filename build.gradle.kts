plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.github.yousseflabs.imageflowconverter"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
}

kotlin { jvmToolchain(17) }

intellijPlatform {
    pluginConfiguration {
        id = "com.github.yousseflabs.imageflowconverter"
        name = "ImageFlow Converter"
        version = "1.0.0"
        ideaVersion {
            sinceBuild = "221"   // IntelliJ 2022.1
            untilBuild = "252.*"
        }
        vendor {
            name = "ImageFlow Converter"
            email = "youssefsaber.dev@gmail.com"
        }
    }
    pluginVerification { ides { recommended() } }
}

tasks { wrapper { gradleVersion = "8.10" } }