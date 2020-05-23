apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(plugin = "kotlin-spring")

dependencies {
    val postgreSQL = "42.2.6"
    val testContainers = "1.12.2"
    implementation(project(":matchmaking"))
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.postgresql:postgresql:$postgreSQL")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.testcontainers:postgresql:$testContainers")
}

tasks.withType<Test> {
    useJUnitPlatform()
}