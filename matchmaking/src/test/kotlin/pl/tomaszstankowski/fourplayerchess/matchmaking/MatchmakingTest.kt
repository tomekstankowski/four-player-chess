package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.transaction.support.TransactionOperations
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.CREATE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.DELETE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.JOIN_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.LEAVE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.NOW
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.UPDATE_LOBBY_DTO
import java.time.Clock
import java.time.ZoneId
import java.util.*

class MatchmakingTest : Spek({

    fun createServiceForTesting(): MatchmakingService {
        val dataSource = InMemoryDataSource()
        return MatchmakingService.create(
                clock = Clock.fixed(NOW, ZoneId.of("UTC")),
                idGenerator = SimpleIdGenerator(),
                lobbyRepository = InMemoryLobbyRepository(dataSource),
                lobbyMembershipRepository = InMemoryLobbyMembershipRepository(dataSource),
                transactionOperations = TransactionOperations.withoutTransaction()
        )
    }

    describe("create lobby") {

        it("should return error when name is already used") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój #1"),
                            requestingPlayerId = UUID.fromString("b416265f-4e7b-4fe7-9c58-ef8e692dea7f")
                    )
            )

            val result = service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój #1"),
                            requestingPlayerId = UUID.fromString("1b07f216-c96f-4cf7-90a5-bf2eac87d09b")
                    )
            )

            result shouldBeEqualTo CreateLobbyResult.NameConflict(name = "Pokój #1")
        }

        it("should return error when player is already in other lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój #1")
                    )
            )

            val result = service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój #2")
                    )
            )

            result shouldBeEqualTo CreateLobbyResult.RequestingPlayerAlreadyInLobby
        }

        it("should return error when lobby details are not valid") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pok")
            )

            val result = service.createLobby(dto)

            result shouldBeEqualTo CreateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("should return created lobby") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy!")
            )
            val result = service.createLobby(dto)

            result shouldBeEqualTo CreateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój testowy!",
                            createdAt = NOW
                    )
            )
        }

        it("should persist created lobby") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy!")
            )

            service.createLobby(dto)
            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój testowy!",
                            createdAt = NOW
                    )
        }

        it("should add owner membership") {
            val service = createServiceForTesting()

            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("d3f3f0e7-ea2d-4a3e-9942-774138e588be"))
            )
            val memberships = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            memberships shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            playerId = UUID.fromString("d3f3f0e7-ea2d-4a3e-9942-774138e588be"),
                            joinedAt = NOW
                    )
            )
        }
    }

    describe("update lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            val dto = UPDATE_LOBBY_DTO.copy(lobbyId = UUID.fromString("8c3d359b-6f40-44b1-8d8c-e69958c8ac02"))

            val result = service.updateLobby(dto)

            result shouldBeEqualTo UpdateLobbyResult.LobbyNotFound(
                    lobbyId = UUID.fromString("8c3d359b-6f40-44b1-8d8c-e69958c8ac02")
            )
        }

        it("should return error when lobby name is already used") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój pierwszy"),
                            requestingPlayerId = UUID.fromString("e389d50c-1861-48e9-8c41-66f1d2b8fba1")
                    )
            )
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój drugi"),
                            requestingPlayerId = UUID.fromString("439628c1-eafd-4e75-b37d-18512e1546ab")
                    )
            )

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój pierwszy"),
                            requestingPlayerId = UUID.fromString("439628c1-eafd-4e75-b37d-18512e1546ab")
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.NameConflict("Pokój pierwszy")
        }

        it("should return error when lobby details are invalid") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "  ")
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Must not be blank"),
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("should return error when requesting player is not an owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("8a5c65f4-7d0d-4c28-93e1-e396e0d0c3ef"))
            )

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("c1b508b6-fa46-417a-896f-09ba93ab8d26"))
            )

            result shouldBeEqualTo UpdateLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return updated lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy")
                    )
            )

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój produkcyjny")
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój produkcyjny",
                            createdAt = NOW
                    )
            )
        }

        it("should persist updated lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy")
                    )
            )

            service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój produkcyjny")
                    )
            )
            val lobby = service.getLobby(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
            )

            lobby shouldBeEqualTo LobbyDto(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    name = "Pokój produkcyjny",
                    createdAt = NOW
            )
        }
    }

    describe("delete lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(lobbyId = UUID.fromString("2c985725-0600-4913-a33e-4cc3a9766b2e"))
            )

            result shouldBeEqualTo DeleteLobbyResult.LobbyNotFound(UUID.fromString("2c985725-0600-4913-a33e-4cc3a9766b2e"))
        }

        it("should return error when requesting player is not member of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("592b327a-6309-439b-8c3f-de0893539390"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("3542cc04-5cae-4400-a708-21660a9548aa"))
            )

            result shouldBeEqualTo DeleteLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return error when requesting player is not owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("255e1317-0628-4d7d-859c-108906635ed6"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("83d888a7-6531-45e3-aa90-02729239d064"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("f90fd0ec-1ec0-40cb-bf5b-be6fc41e8ad6"))
            )

            result shouldBeEqualTo DeleteLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return deleted result") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.deleteLobby(DELETE_LOBBY_DTO)

            result shouldBeEqualTo DeleteLobbyResult.Deleted
        }

        it("should remove lobby from db") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            service.deleteLobby(DELETE_LOBBY_DTO)

            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo null
        }

        it("should remove lobby after some players joined it") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )

            result shouldBeEqualTo DeleteLobbyResult.Deleted
        }

        it("should remove memberships") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))
            )

            service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            val currentLobbyOfPlayer = service.getCurrentLobbyOfPLayer(UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))

            currentLobbyOfPlayer shouldBeEqualTo null
        }
    }

    describe("join lobby") {

        it("should return error if lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(lobbyId = UUID.fromString("fd489d64-a1e7-4375-bd2c-3b1d42b0470f"))
            )

            result shouldBeEqualTo JoinLobbyResult.LobbyNotFound(UUID.fromString("fd489d64-a1e7-4375-bd2c-3b1d42b0470f"))
        }

        it("should return error if player is already in the same lobby") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.joinLobby(JOIN_LOBBY_DTO)

            val result = service.joinLobby(JOIN_LOBBY_DTO)

            result shouldBeEqualTo JoinLobbyResult.PlayerAlreadyInLobby
        }

        it("should return error if player is already in other lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            requestingPlayerId = UUID.fromString("b33f84f6-7b53-4caf-bae7-dc1c15d3e6b8"),
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój 1")
                    )
            )
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            requestingPlayerId = UUID.fromString("9cce4d8f-16f5-4dcf-823f-839c4c3c7792"),
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój 2"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            requestingPlayerId = UUID.fromString("eae112d7-7460-4a63-a6b1-a3256d6a98ac")
                    )
            )

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(
                            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            requestingPlayerId = UUID.fromString("eae112d7-7460-4a63-a6b1-a3256d6a98ac")
                    )
            )

            result shouldBeEqualTo JoinLobbyResult.PlayerAlreadyInLobby
        }

        it("should return error if player is owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )

            result shouldBeEqualTo JoinLobbyResult.PlayerAlreadyInLobby
        }

        it("should return error if lobby is full") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("fcd7ab99-d75f-47e7-a6e1-52aac4c851a7"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("46e4ff66-b034-4701-9126-7724c1ecb9a5"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("1936636d-80c1-4509-8338-1b25591b9d20"))
            )

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("04beb701-65af-4977-af21-bddb8b66860e"))
            )

            result shouldBeEqualTo JoinLobbyResult.LobbyIsFull
        }

        it("should return membership") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"))
            )

            result shouldBeEqualTo JoinLobbyResult.Success(
                    LobbyMembershipDto(
                            playerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"),
                            joinedAt = NOW
                    )
            )
        }

        it("should persist membership") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("d4542f81-4659-4d42-b01f-095542db705e"))
            )

            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"))
            )
            val memberships = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            memberships shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            playerId = UUID.fromString("d4542f81-4659-4d42-b01f-095542db705e"),
                            joinedAt = NOW
                    ),
                    LobbyMembershipDto(
                            playerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"),
                            joinedAt = NOW
                    )
            )
        }
    }

    describe("leave lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(lobbyId = UUID.fromString("1d81af2c-b8d2-49db-a341-69ab6c69d8ae"))
            )

            result shouldBeEqualTo LeaveLobbyResult.LobbyNotFound(UUID.fromString("1d81af2c-b8d2-49db-a341-69ab6c69d8ae"))
        }

        it("should return error when requesting player is owner") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )

            result shouldBeEqualTo LeaveLobbyResult.OwnerMemberMustNotLeaveLobby
        }

        it("should return error when requesting player is not a member") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )
            service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            result shouldBeEqualTo LeaveLobbyResult.RequestingPlayerNotAMember
        }

        it("should return left") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            result shouldBeEqualTo LeaveLobbyResult.Left
        }

        it("should remove membership") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )
            val playersInLobby = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            playersInLobby shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            playerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"),
                            joinedAt = NOW
                    )
            )
        }
    }
})