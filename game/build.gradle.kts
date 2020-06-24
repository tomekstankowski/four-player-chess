import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

val spekVersion: String? by ext
val kluentVersion: String? by ext
val valiktorVersion: String by ext

dependencies {
    implementation(project(":common"))
    implementation(project(":engine"))
    implementation(project(":data"))

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

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