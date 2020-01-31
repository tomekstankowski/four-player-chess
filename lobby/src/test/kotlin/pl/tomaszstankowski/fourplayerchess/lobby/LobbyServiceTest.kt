package pl.tomaszstankowski.fourplayerchess.lobby

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import pl.tomaszstankowski.fourplayerchess.common.data.IdGeneratorWithMemory
import pl.tomaszstankowski.fourplayerchess.common.data.TestTransactionExecutor
import pl.tomaszstankowski.fourplayerchess.common.time.fixedClock
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.time.Instant
import java.util.*

class LobbyServiceTest : Spek({
    val clock = fixedClock()
    val idGenerator = IdGeneratorWithMemory()
    val lobbyRepository = InMemoryLobbyRepository()
    val service = LobbyService.create(
        clock = clock,
        idGenerator = idGenerator,
        lobbyRepository = lobbyRepository,
        transactionExecutor = TestTransactionExecutor()
    )

    val validCreateCommand = CreateLobbyCommand(
        lobbyDetails = LobbyDetails(name = "Pokój Tomka")
    )

    val unknownLobbyId = UUID.fromString("48d56d55-0c21-4032-af5f-df30c7c7c8f4")

    afterEachTest { lobbyRepository.clear() }

    describe("creating lobbing") {

        it("returns error when name is already used") {
            service.createLobby(validCreateCommand)

            val result = service.createLobby(validCreateCommand)

            result shouldBeEqualTo CreateLobbyResult.NameConflict(name = validCreateCommand.lobbyDetails.name)
        }

        it("returns error when lobby details are not valid") {
            val invalidDetails = validCreateCommand.lobbyDetails.copy(
                name = "Pok"
            )

            val result = service.createLobby(CreateLobbyCommand(invalidDetails))

            result shouldBeEqualTo CreateLobbyResult.LobbyDetailsNotValid(
                errors = setOf(
                    ValidationError.InvalidLength(property = "name", minLength = 5, maxLength = 50)
                )
            )
        }

        it("returns created lobby") {
            val result = service.createLobby(validCreateCommand)

            result shouldBeEqualTo CreateLobbyResult.Success(
                LobbyDTO(
                    id = idGenerator.lastId,
                    name = validCreateCommand.lobbyDetails.name,
                    createdAt = Instant.now(clock)
                )
            )
        }

        it("persists created lobby") {
            service.createLobby(validCreateCommand)
            val lobby = service.getLobby(idGenerator.lastId)

            lobby shouldBeEqualTo
                    LobbyDTO(
                        id = idGenerator.lastId,
                        name = validCreateCommand.lobbyDetails.name,
                        createdAt = Instant.now(clock)
                    )
        }
    }

    describe("updating lobby") {

        it("should return error when lobby not found") {
            val cmd = UpdateLobbyCommand(
                lobbyId = unknownLobbyId,
                lobbyDetails = validCreateCommand.lobbyDetails
            )

            val result = service.updateLobby(cmd)

            result shouldBeEqualTo UpdateLobbyResult.LobbyNotFound(
                lobbyId = unknownLobbyId
            )
        }

        it("should return error when lobby name is already used") {
            service.createLobby(validCreateCommand)
            service.createLobby(
                CreateLobbyCommand(
                    lobbyDetails = validCreateCommand.lobbyDetails.copy(
                        name = "Pokój Tomka 2"
                    )
                )
            )

            val result = service.updateLobby(
                UpdateLobbyCommand(
                    lobbyId = idGenerator.lastId,
                    lobbyDetails = validCreateCommand.lobbyDetails
                )
            )

            result shouldBeEqualTo UpdateLobbyResult.NameConflict(validCreateCommand.lobbyDetails.name)
        }

        it("should return error when lobby details are invalid") {
            service.createLobby(validCreateCommand)

            val result = service.updateLobby(
                UpdateLobbyCommand(
                    lobbyId = idGenerator.lastId,
                    lobbyDetails = validCreateCommand.lobbyDetails.copy(
                        name = ""
                    )
                )
            )

            result shouldBeEqualTo UpdateLobbyResult.LobbyDetailsNotValid(
                errors = setOf(
                    ValidationError.Blank("name"),
                    ValidationError.InvalidLength(property = "name", minLength = 5, maxLength = 50)
                )
            )
        }

        it("should return updated lobby") {
            val newDetails = LobbyDetails(
                name = "Pokój Tomasza"
            )

            service.createLobby(validCreateCommand)
            val result = service.updateLobby(
                UpdateLobbyCommand(
                    lobbyId = idGenerator.lastId,
                    lobbyDetails = newDetails
                )
            )

            result shouldBeEqualTo UpdateLobbyResult.Success(
                LobbyDTO(
                    id = idGenerator.lastId,
                    name = newDetails.name,
                    createdAt = Instant.now(clock)
                )
            )
        }

        it("should persist updated lobby") {
            val newDetails = LobbyDetails(
                name = "Pokój Tomasza"
            )

            service.createLobby(validCreateCommand)
            service.updateLobby(
                UpdateLobbyCommand(
                    lobbyId = idGenerator.lastId,
                    lobbyDetails = newDetails
                )
            )
            val lobby = service.getLobby(idGenerator.lastId)

            lobby shouldBeEqualTo LobbyDTO(
                id = idGenerator.lastId,
                name = newDetails.name,
                createdAt = Instant.now(clock)
            )
        }
    }

    describe("deleting lobby") {

        it("should return false when lobby not found") {
            val deleted = service.deleteLobby(unknownLobbyId)

            deleted shouldBeEqualTo false
        }

        it("should return true when lobby found") {
            service.createLobby(validCreateCommand)
            val deleted = service.deleteLobby(idGenerator.lastId)

            deleted shouldBeEqualTo true
        }

        it("should remove lobby from db") {
            service.createLobby(validCreateCommand)
            service.deleteLobby(idGenerator.lastId)
            val lobby = service.getLobby(idGenerator.lastId)

            lobby shouldBeEqualTo null
        }
    }
})