plugins {
    application
}

dependencies {
    implementation(project(":engine"))

    implementation("commons-cli:commons-cli:1.4")
}

application {
    mainClassName = "pl.tomaszstankowski.fourplayerchess.tester.ProgramKt"
}