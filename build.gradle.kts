plugins {
    kotlin("jvm") version "1.9.25"
    application
}

group = "lt.markmerkk"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("commons-io:commons-io:2.11.0")
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")

    implementation("org.slf4j:jul-to-slf4j:2.0.16")
    implementation("org.slf4j:slf4j-api:2.0.16")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.8")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.8.0")
    testImplementation("io.mockk:mockk:1.13.3")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("lt.markmerkk.mp3cutter.MainKt")
}
