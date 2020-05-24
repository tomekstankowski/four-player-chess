import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

val spekVersion: String? by ext
val kluentVersion: String? by ext
val jjwtVersion = "0.11.1"

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

tasks.withType<Jar> { enabled = true }
tasks.withType<BootJar> { enabled = false }
tasks.withType<BootRun> { enabled = false }
tasks.withType<BootWar> { enabled = false }

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines = setOf("spek2")
    }
}