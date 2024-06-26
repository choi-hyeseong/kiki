import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
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

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    // common 모듈 의존성 추가


    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
        if (name != "common")
        // circular error 방지
            implementation(project(":common"))

    }
}