import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("de.jjohannes.extra-java-module-info")  version "0.14"
    application
}

group = "lt.markmerkk"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("commons-io:commons-io:2.11.0")
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")

    implementation("org.slf4j:jul-to-slf4j:1.7.36")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.8.0")
    testImplementation("io.mockk:mockk:1.13.3")
//    testImplementation("org.mockito:mockito-core:2.23.0")
//    testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("lt.markmerkk.mp3cutter.MainKt")
}

// Source: https://plugins.gradle.org/plugin/de.jjohannes.extra-java-module-info
// Source: https://github.com/jjohannes/extra-java-module-info
extraJavaModuleInfo {
    // ffmpeg
    automaticModule("ffmpeg-0.7.0.jar", "net.bramp.ffmpeg")
    automaticModule("modelmapper-3.1.0.jar", "modelmapper")
    automaticModule("annotations-13.0.jar", "annotations")
    automaticModule("failureaccess-1.0.1.jar", "failureaccess")
    automaticModule("jsr305-3.0.2.jar", "jsr305")
    automaticModule("j2objc-annotations-1.3.jar", "j2objc.annotations")
    automaticModule(
        "listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar",
        "listenablefuture.9999.0.empty.to.avoid.conflict.with.guava"
    )

    // Logging
    automaticModule("jul-to-slf4j-1.7.36.jar", "jul.to.slf4j")
    automaticModule("logback-classic-1.2.11.jar", "logback.classic")
    automaticModule("logback-core-1.2.11.jar", "logback.core")

    // Kotlin
    automaticModule("kotlinx-cli-jvm-0.3.4.jar", "kotlinx.cli.jvm")
    automaticModule("kotlin-stdlib-common-1.6.21.jar", "kotlin.stdlib.common")
    automaticModule("kotlin-stdlib-common-1.7.20.jar", "kotlin.stdlib.common")
    automaticModule("kotlinx-coroutines-core-jvm-1.6.4.jar", "kotlin.coroutines.core")

    // Tests
    automaticModule("mockito-kotlin-1.5.0.jar", "mockito.kotlin")
    automaticModule("assertj-core-3.8.0.jar", "assertj.core")
    automaticModule("kotlin-reflect-1.0.7.jar", "kotlin.reflect")
    automaticModule("objenesis-2.6.jar", "objenesis")
    automaticModule("hamcrest-core-1.3.jar", "hamcrest")
    automaticModule("mockk-jvm-1.13.3.jar", "mockk")
    automaticModule("mockk-dsl-1.13.3.jar", "mockk")
    automaticModule("mockk-dsl-jvm-1.13.3.jar", "mockk")
    automaticModule("mockk-agent-jvm-1.13.3.jar", "mockk")
    automaticModule("mockk-agent-api-jvm-1.13.3.jar", "mockk")
    automaticModule("mockk-core-jvm-1.13.3.jar", "mockk")
}
