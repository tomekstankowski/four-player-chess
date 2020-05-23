import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("plugin.spring") version "1.3.61"
}

group = "pl.tomaszstankowski"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        jcenter()
        maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    ext {
        set("spekVersion", "2.0.9")
        set("kluentVersion", "1.60")
        set("valiktorVersion", "0.11.0")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}