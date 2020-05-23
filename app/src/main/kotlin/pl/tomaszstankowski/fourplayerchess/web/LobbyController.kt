package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.*
import pl.tomaszstankowski.fourplayerchess.matchmaking.*
import java.util.*

@RestController
@RequestMapping("/lobbies")
class LobbyController(private val matchmakingService: MatchmakingService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun createLobby(@RequestBody lobbyEditableDetails: LobbyEditableDetails): LobbyDto =
            when (val result = matchmakingService.createLobby(CreateLobbyDto(lobbyEditableDetails))) {
                is CreateLobbyResult.Success -> result.lobby
                is CreateLobbyResult.NameConflict -> throw nameConflictException(result.name)
                is CreateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
            }

    @GetMapping("/{id}")
    fun getLobby(@PathVariable id: UUID): LobbyDto =
            matchmakingService.getLobby(id) ?: throw ApiException.resourceNotFound("lobby", id)

    @GetMapping
    fun getAllLobbies(): List<LobbyDto> = matchmakingService.getAllLobbies()

    @PutMapping("/{id}")
    fun updateLobby(@PathVariable id: UUID, @RequestBody lobbyEditableDetails: LobbyEditableDetails): LobbyDto {
        val cmd = UpdateLobbyDto(
                lobbyId = id,
                lobbyEditableDetails = lobbyEditableDetails
        )
        return when (val result = matchmakingService.updateLobby(cmd)) {
            is UpdateLobbyResult.Success -> result.lobby
            is UpdateLobbyResult.LobbyNotFound -> throw ApiException.resourceNotFound("lobby", id)
            is UpdateLobbyResult.NameConflict -> throw nameConflictException(result.name)
            is UpdateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteLobby(@PathVariable id: UUID) {
        return when (matchmakingService.deleteLobby(id)) {
            true -> Unit
            false -> throw ApiException.resourceNotFound("lobby", id)
        }
    }


    private fun nameConflictException(name: String) =
        ApiException.unprocessableEntity(
            message = "Lobby with name $name already exists",
            data = mapOf(
                "cause" to "NAME_CONFLICT"
            )
        )
}