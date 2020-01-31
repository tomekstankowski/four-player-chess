package pl.tomaszstankowski.fourplayerchess.app.lobby

import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.*
import pl.tomaszstankowski.fourplayerchess.app.error.ApiException
import pl.tomaszstankowski.fourplayerchess.lobby.*
import java.util.*

@RestController
@RequestMapping("/lobbies")
class LobbyController(private val lobbyService: LobbyService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun createLobby(@RequestBody lobbyDetails: LobbyDetails): LobbyDTO =
        when (val result = lobbyService.createLobby(CreateLobbyCommand(lobbyDetails))) {
            is CreateLobbyResult.Success -> result.lobby
            is CreateLobbyResult.NameConflict -> throw nameConflictException(result.name)
            is CreateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
        }

    @GetMapping("/{id}")
    fun getLobby(@PathVariable id: UUID): LobbyDTO =
        lobbyService.getLobby(id) ?: throw ApiException.resourceNotFound("lobby", id)

    @GetMapping
    fun getAllLobbies(): List<LobbyDTO> = lobbyService.getAllLobbies()

    @PutMapping("/{id}")
    fun updateLobby(@PathVariable id: UUID, @RequestBody lobbyDetails: LobbyDetails): LobbyDTO {
        val cmd = UpdateLobbyCommand(
            lobbyId = id,
            lobbyDetails = lobbyDetails
        )
        return when (val result = lobbyService.updateLobby(cmd)) {
            is UpdateLobbyResult.Success -> result.lobby
            is UpdateLobbyResult.LobbyNotFound -> throw ApiException.resourceNotFound("lobby", id)
            is UpdateLobbyResult.NameConflict -> throw nameConflictException(result.name)
            is UpdateLobbyResult.LobbyDetailsNotValid -> throw ApiException.invalidBody(result.errors)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteLobby(@PathVariable id: UUID) {
        return when (lobbyService.deleteLobby(id)) {
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