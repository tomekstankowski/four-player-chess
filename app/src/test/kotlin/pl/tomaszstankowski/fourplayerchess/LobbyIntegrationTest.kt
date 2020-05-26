package pl.tomaszstankowski.fourplayerchess

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.*

class LobbyIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "abda40e1-3d1e-41ba-b272-58d4fcc91403")
    fun `can create lobby`() {
        val json = """
            {
                "name": "${NewLobby.NAME}"
            }
        """

        mockMvc.post("/lobbies") {
            contentType = APPLICATION_JSON
            content = json
        }
                .andExpect {
                    status { isCreated }
                    jsonPath("id") { isString }
                    jsonPath("name") { value(NewLobby.NAME) }
                    jsonPath("createdAt") { isString }
                }
    }

    @Test
    @WithMockUser(username = "abda40e1-3d1e-41ba-b272-58d4fcc91403")
    fun `can get lobby`() {
        mockMvc.get("/lobbies/{id}", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("id") { value(FirstLobby.ID) }
                    jsonPath("name") { value(FirstLobby.NAME) }
                    jsonPath("createdAt") { value(FirstLobby.CREATED_AT) }
                }
    }

    @Test
    @WithMockUser(username = "abda40e1-3d1e-41ba-b272-58d4fcc91403")
    fun `can get all lobbies`() {
        mockMvc.get("/lobbies")
                .andExpect {
                    status { isOk }
                    jsonPath("[0].id") { value(FirstLobby.ID) }
                    jsonPath("[0].name") { value(FirstLobby.NAME) }
                    jsonPath("[0].createdAt") { value(FirstLobby.CREATED_AT) }

                    jsonPath("[1].id") { value(SecondLobby.ID) }
                    jsonPath("[1].name") { value(SecondLobby.NAME) }
                    jsonPath("[1].createdAt") { value(SecondLobby.CREATED_AT) }
                }
    }

    @Test
    @WithMockUser(username = "df1a63de-c6b9-4383-ab56-761e3339be6c")
    fun `can update lobby`() {
        val json = """
           {
                "name": "${NewLobby.NAME}"
            }
        """

        mockMvc.put("/lobbies/{id}", FirstLobby.ID) {
            contentType = APPLICATION_JSON
            content = json
        }
                .andExpect {
                    status { isOk }
                    jsonPath("id") { value(FirstLobby.ID) }
                    jsonPath("name") { value(NewLobby.NAME) }
                    jsonPath("createdAt") { value(FirstLobby.CREATED_AT) }
                }
    }

    @Test
    @WithMockUser(username = "df1a63de-c6b9-4383-ab56-761e3339be6c")
    fun `can delete lobby`() {
        mockMvc.delete("/lobbies/{id}", FirstLobby.ID)
                .andExpect { status { isOk } }
        mockMvc.get("/lobbies/{id}", FirstLobby.ID)
                .andExpect { status { isNotFound } }
    }

    @Test
    @WithMockUser(username = "abda40e1-3d1e-41ba-b272-58d4fcc91403")
    fun `can join lobby`() {
        mockMvc.post("/lobbies/{id}/join", FirstLobby.ID)
                .andExpect {
                    status { isCreated }
                    jsonPath("playerId") { value("abda40e1-3d1e-41ba-b272-58d4fcc91403") }
                    jsonPath("joinedAt") { isString }
                }

        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(3))
                }
    }

    @Test
    @WithMockUser(username = "ff698d5e-d1a7-45d4-9e12-18d88bdf517e")
    fun `can leave lobby`() {
        mockMvc.post("/lobbies/{id}/leave", FirstLobby.ID)
                .andExpect {
                    status { isNoContent }
                }

        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(1))
                }
    }

    @Test
    @WithMockUser(username = "ff698d5e-d1a7-45d4-9e12-18d88bdf517e")
    fun `can get players in lobby`() {
        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("[0].playerId") { value("ff698d5e-d1a7-45d4-9e12-18d88bdf517e") }
                    jsonPath("[0].joinedAt") { value("2020-01-31T22:08:15Z") }
                    jsonPath("[1].playerId") { value("df1a63de-c6b9-4383-ab56-761e3339be6c") }
                    jsonPath("[1].joinedAt") { value("2020-01-31T22:08:12Z") }
                }
    }

    @Test
    @WithMockUser(username = "ff698d5e-d1a7-45d4-9e12-18d88bdf517e")
    fun `can get player's current lobby`() {
        mockMvc.get("/lobbies/joined-by-me")
                .andExpect {
                    status { isOk }
                    jsonPath("lobby.id") { value(FirstLobby.ID) }
                    jsonPath("lobby.name") { value(FirstLobby.NAME) }
                    jsonPath("lobby.createdAt") { value(FirstLobby.CREATED_AT) }
                }
    }
}

private object NewLobby {
    const val NAME = "Pokój Tomka"
}

private object FirstLobby {
    const val ID = "78004565-85e8-4258-bd7e-cebe59571284"
    const val NAME = "Pokój #1"
    const val CREATED_AT = "2020-01-31T22:08:12Z"
}

private object SecondLobby {
    const val ID = "2025c4cf-3e70-4730-8474-21cc6a760647"
    const val NAME = "Pokój #2"
    const val CREATED_AT = "2020-01-31T22:08:33Z"
}