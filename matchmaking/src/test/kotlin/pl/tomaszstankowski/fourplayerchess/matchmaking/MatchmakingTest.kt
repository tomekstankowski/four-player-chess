package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.transaction.support.TransactionOperations
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MatchmakingTest : Spek({
    val createLobbyDto = CreateLobbyDto(
            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój Tomka")
    )

    val unknownLobbyId = UUID.fromString("48d56d55-0c21-4032-af5f-df30c7c7c8f4")

    val now = Instant.parse("2020-05-23T18:39:00.240455Z")

    fun createServiceForTesting() =
            MatchmakingService.create(
                    clock = Clock.fixed(now, ZoneId.of("UTC")),
                    idGenerator = SimpleIdGenerator(),
                    lobbyRepository = InMemoryLobbyRepository(InMemoryDataSource()),
                    transactionOperations = TransactionOperations.withoutTransaction()
            )



    describe("creating lobbing") {

        it("returns error when name is already used") {
            val service = createServiceForTesting()
            service.createLobby(createLobbyDto)

            val result = service.createLobby(createLobbyDto)

            result shouldBeEqualTo CreateLobbyResult.NameConflict(name = createLobbyDto.lobbyEditableDetails.name)
        }

        it("returns error when lobby details are not valid") {
            val service = createServiceForTesting()
            val invalidDetails = createLobbyDto.lobbyEditableDetails.copy(
                    name = "Pok"
            )

            val result = service.createLobby(CreateLobbyDto(invalidDetails))

            result shouldBeEqualTo CreateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("returns created lobby") {
            val service = createServiceForTesting()

            val result = service.createLobby(createLobbyDto)

            result shouldBeEqualTo CreateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = createLobbyDto.lobbyEditableDetails.name,
                            createdAt = now
                    )
            )
        }

        it("persists created lobby") {
            val service = createServiceForTesting()

            service.createLobby(createLobbyDto)
            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = createLobbyDto.lobbyEditableDetails.name,
                            createdAt = now
                    )
        }
    }

    describe("updating lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            val cmd = UpdateLobbyDto(
                    lobbyId = unknownLobbyId,
                    lobbyEditableDetails = createLobbyDto.lobbyEditableDetails
            )

            val result = service.updateLobby(cmd)

            result shouldBeEqualTo UpdateLobbyResult.LobbyNotFound(
                    lobbyId = unknownLobbyId
            )
        }

        it("should return error when lobby name is already used") {
            val service = createServiceForTesting()
            service.createLobby(createLobbyDto)
            service.createLobby(
                    CreateLobbyDto(
                            lobbyEditableDetails = createLobbyDto.lobbyEditableDetails.copy(
                                    name = "Pokój Tomka 2"
                            )
                    )
            )

            val result = service.updateLobby(
                    UpdateLobbyDto(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            lobbyEditableDetails = createLobbyDto.lobbyEditableDetails
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.NameConflict(createLobbyDto.lobbyEditableDetails.name)
        }

        it("should return error when lobby details are invalid") {
            val service = createServiceForTesting()
            service.createLobby(createLobbyDto)

            val result = service.updateLobby(
                    UpdateLobbyDto(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            lobbyEditableDetails = createLobbyDto.lobbyEditableDetails.copy(
                                    name = ""
                            )
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Must not be blank"),
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("should return updated lobby") {
            val service = createServiceForTesting()
            val newDetails = LobbyEditableDetails(
                    name = "Pokój Tomasza"
            )

            service.createLobby(createLobbyDto)
            val result = service.updateLobby(
                    UpdateLobbyDto(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            lobbyEditableDetails = newDetails
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = newDetails.name,
                            createdAt = now
                    )
            )
        }

        it("should persist updated lobby") {
            val service = createServiceForTesting()
            val newDetails = LobbyEditableDetails(
                    name = "Pokój Tomasza"
            )

            service.createLobby(createLobbyDto)
            service.updateLobby(
                    UpdateLobbyDto(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            lobbyEditableDetails = newDetails
                    )
            )
            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo LobbyDto(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    name = newDetails.name,
                    createdAt = now
            )
        }
    }

    describe("deleting lobby") {

        it("should return false when lobby not found") {
            val service = createServiceForTesting()

            val isDeleted = service.deleteLobby(unknownLobbyId)

            isDeleted shouldBeEqualTo false
        }

        it("should return true when lobby found") {
            val service = createServiceForTesting()

            service.createLobby(createLobbyDto)
            val isDeleted = service.deleteLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            isDeleted shouldBeEqualTo true
        }

        it("should remove lobby from db") {
            val service = createServiceForTesting()

            service.createLobby(createLobbyDto)
            service.deleteLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo null
        }
    }
})