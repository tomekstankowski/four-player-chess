package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import pl.tomaszstankowski.fourplayerchess.matchmaking.*
import pl.tomaszstankowski.fourplayerchess.websecurity.getAuthenticatedUserId
import java.util.*

@RestController
@RequestMapping("/lobbies")
class LobbyController(private val matchmakingService: MatchmakingService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun createLobby(@RequestBody lobbyEditableDetails: LobbyEditableDetails): LobbyDto {
        val dto = CreateLobbyDto(
                lobbyEditableDetails = lobbyEditableDetails,
                requestingPlayerId = getAuthenticatedUserId()
        )
        return when (val result = matchmakingService.createLobby(dto)) {
            is CreateLobbyResult.Success -> result.lobby
            is CreateLobbyResult.NameConflict -> throw nameConflictException(result.name)
            is CreateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
            is CreateLobbyResult.RequestingPlayerAlreadyInLobby ->
                throw ApiException.unprocessableEntity("Already in lobby")
        }
    }

    @GetMapping("/{id}")
    fun getLobby(@PathVariable id: UUID): LobbyDto =
            matchmakingService.getLobby(id) ?: throw ApiException.resourceNotFound("lobby", id)

    @GetMapping
    fun getAllLobbies(): List<LobbyDto> = matchmakingService.getAllLobbies()

    @PutMapping("/{id}")
    fun updateLobby(@PathVariable id: UUID, @RequestBody lobbyEditableDetails: LobbyEditableDetails): LobbyDto {
        val dto = UpdateLobbyDto(
                lobbyId = id,
                lobbyEditableDetails = lobbyEditableDetails,
                requestingPlayerId = getAuthenticatedUserId()
        )
        return when (val result = matchmakingService.updateLobby(dto)) {
            is UpdateLobbyResult.Success -> result.lobby
            is UpdateLobbyResult.LobbyNotFound -> throw lobbyNotFoundException(id)
            is UpdateLobbyResult.NameConflict -> throw nameConflictException(result.name)
            is UpdateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
            is UpdateLobbyResult.RequestingPlayerNotAnOwner ->
                throw ApiException.forbidden("Requesting player is not an owner of the lobby")
        }
    }

    @DeleteMapping("/{id}")
    fun deleteLobby(@PathVariable id: UUID) {
        val dto = DeleteLobbyDto(
                lobbyId = id,
                requestingPlayerId = getAuthenticatedUserId()
        )
        return when (matchmakingService.deleteLobby(dto)) {
            is DeleteLobbyResult.Deleted -> Unit
            is DeleteLobbyResult.RequestingPlayerNotAnOwner ->
                throw ApiException.forbidden("Requesting player is not an owner of the lobby")
            is DeleteLobbyResult.LobbyNotFound -> throw lobbyNotFoundException(id)
        }
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(CREATED)
    fun joinLobby(@PathVariable id: UUID): LobbyMembershipDto {
        val dto = JoinLobbyDto(
                lobbyId = id,
                requestingPlayerId = getAuthenticatedUserId()
        )
        return when (val result = matchmakingService.joinLobby(dto)) {
            is JoinLobbyResult.Success -> result.membership
            is JoinLobbyResult.PlayerAlreadyInLobby -> throw ApiException.unprocessableEntity("Already in lobby")
            is JoinLobbyResult.LobbyNotFound -> throw lobbyNotFoundException(id)
            JoinLobbyResult.LobbyIsFull -> throw ApiException.unprocessableEntity("Lobby is full")
        }
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(NO_CONTENT)
    fun leaveLobby(@PathVariable id: UUID) {
        val dto = LeaveLobbyDto(
                lobbyId = id,
                requestingPlayerId = getAuthenticatedUserId()
        )
        return when (matchmakingService.leaveLobby(dto)) {
            LeaveLobbyResult.Left -> Unit
            LeaveLobbyResult.OwnerMemberMustNotLeaveLobby ->
                throw ApiException.unprocessableEntity("Owner of the lobby must not leave")
            LeaveLobbyResult.RequestingPlayerNotAMember ->
                throw ApiException.forbidden("Not a member of the lobby")
            is LeaveLobbyResult.LobbyNotFound ->
                throw lobbyNotFoundException(id)
        }
    }

    @GetMapping("/{id}/players")
    fun getPlayersInLobby(@PathVariable id: UUID): List<LobbyMembershipDto> =
            matchmakingService.getPlayersInLobby(id) ?: throw lobbyNotFoundException(id)

    @GetMapping("/joined-by-me")
    fun getCurrentLobbyOfPlayer(): LobbyJoinedByPlayerDto =
            LobbyJoinedByPlayerDto(
                    lobby = matchmakingService.getCurrentLobbyOfPLayer(getAuthenticatedUserId())
            )

    private fun lobbyNotFoundException(id: UUID) = ApiException.resourceNotFound("lobby", id)

    private fun nameConflictException(name: String) =
            ApiException.unprocessableEntity(
                    message = "Lobby with name $name already exists",
                    data = mapOf(
                            "cause" to "NAME_CONFLICT"
                    )
            )
}

data class LobbyJoinedByPlayerDto(val lobby: LobbyDto?)