# Four player chess backend
Web application backend to play [four-player variant of chess](https://en.wikibooks.org/wiki/Four-Player_Chess) (FFA). 
Application allows to play against other people and bots. 
Project is written using Kotlin language and Spring Boot framework. Frontend app repository is available [here](https://github.com/tomekstankowski/four-player-chess-frontend).

## Engine
A chess engine is part of the project. The engine may use one of two algorithms:
- paranoid
- hypermax

Position evaluation is based on material only.
Some search optimization techniques were implemented:
- transposition tables
- iterative deepening
- move heuristics (MVV-LVA, killer heuristic, history heuristic)

`tester` module contains code used to compare quality of play and performance of both implemented algorithms.

## Run
Execute `docker-compose up -d` to create development database instance (postgres). 
Then run `./gradlew app:bootRun`. Server is listening on port 8080 by default.

## Run tests
`./gradlew check`
