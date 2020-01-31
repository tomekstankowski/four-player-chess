# Four player chess
Application to play four-player variant of chess (single).

## Build
`./gradlew app:bootJar` (output jar is located at app/build/libs)

## Run
Execute `docker-compose up -d` to create development database instance.

`./gradlew app:bootRun` or `java -jar app/build/libs/app.jar`

## Test
`./gradlew check`