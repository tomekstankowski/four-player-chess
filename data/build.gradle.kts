import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}

tasks.withType<Jar> { enabled = true }
tasks.withType<BootJar> { enabled = false }
tasks.withType<BootRun> { enabled = false }
tasks.withType<BootWar> { enabled = false }
