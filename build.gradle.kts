import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
}

group = "lt.markmerkk"
version = "1.0-SNAPSHOT"

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
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("org.assertj:assertj-core:3.8.0")
    testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}