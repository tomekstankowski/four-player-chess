package pl.tomaszstankowski.fourplayerchess

import org.hamcrest.Matchers.hasSize
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.*

class MatchmakingTest : IntegrationTest() {

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
                    jsonPath("ownerId") { value("abda40e1-3d1e-41ba-b272-58d4fcc91403") }
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
                    jsonPath("ownerId") { value(FirstLobby.OWNER_ID) }
                }
    }

    @Test
    @WithMockUser(username = "abda40e1-3d1e-41ba-b272-58d4fcc91403")
    fun `can get all lobbies`() {
        mockMvc.get("/lobbies")
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(3))
                    jsonPath("[0].id") { value(FirstLobby.ID) }
                    jsonPath("[0].name") { value(FirstLobby.NAME) }
                    jsonPath("[0].createdAt") { value(FirstLobby.CREATED_AT) }
                    jsonPath("[0].numberOfPlayers") { value(FirstLobby.NUM_OF_PLAYERS) }

                    jsonPath("[1].id") { value(SecondLobby.ID) }
                    jsonPath("[1].name") { value(SecondLobby.NAME) }
                    jsonPath("[1].createdAt") { value(SecondLobby.CREATED_AT) }
                    jsonPath("[1].numberOfPlayers") { value(SecondLobby.NUM_OF_PLAYERS) }

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
                    jsonPath("ownerId") { value(FirstLobby.OWNER_ID) }
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
                    jsonPath("userId") { value("abda40e1-3d1e-41ba-b272-58d4fcc91403") }
                    jsonPath("joinedAt") { isString }
                }

        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(4))
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
                    jsonPath("$", hasSize<Any>(2))
                }
    }

    @Test
    @WithMockUser(username = "df1a63de-c6b9-4383-ab56-761e3339be6c")
    fun `can add random bot to lobby`() {
        mockMvc.post("/lobbies/{id}/random-bots", FirstLobby.ID)
                .andExpect {
                    status { isCreated }
                    jsonPath("type") { value("randomBot") }
                    jsonPath("botId") { isString }
                    jsonPath("joinedAt") { isString }
                }

        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(4))
                }
    }

    @Test
    @WithMockUser(username = "df1a63de-c6b9-4383-ab56-761e3339be6c")
    fun `can remove random bot from lobby`() {
        mockMvc.delete("/lobbies/{lobbyId}/random-bots/{botId}", FirstLobby.ID, "50939121-87a9-4247-8322-07d0c72d00c9")
                .andExpect {
                    status { isNoContent }
                }

        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(2))
                }
    }

    @Test
    @WithMockUser(username = "ff698d5e-d1a7-45d4-9e12-18d88bdf517e")
    fun `can get players in lobby`() {
        mockMvc.get("/lobbies/{id}/players", FirstLobby.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(3))
                    jsonPath("[0].userId") { value("df1a63de-c6b9-4383-ab56-761e3339be6c") }
                    jsonPath("[0].joinedAt") { value("2020-01-31T22:08:12Z") }
                    jsonPath("[0].type") { value("human") }
                    jsonPath("[1].userId") { value("ff698d5e-d1a7-45d4-9e12-18d88bdf517e") }
                    jsonPath("[1].joinedAt") { value("2020-01-31T22:08:15Z") }
                    jsonPath("[1].type") { value("human") }
                    jsonPath("[2].botId") { value("50939121-87a9-4247-8322-07d0c72d00c9") }
                    jsonPath("[2].joinedAt") { value("2020-01-31T22:08:16Z") }
                    jsonPath("[2].type") { value("randomBot") }
                }
    }

    @Test
    @WithMockUser(username = "ff698d5e-d1a7-45d4-9e12-18d88bdf517e")
    fun `can get player's active lobbies`() {
        mockMvc.get("/lobbies/joined-by-me")
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(1))
                    jsonPath("[0].id") { value(FirstLobby.ID) }
                    jsonPath("[0].name") { value(FirstLobby.NAME) }
                    jsonPath("[0].createdAt") { value(FirstLobby.CREATED_AT) }
                    jsonPath("[0].ownerId") { value(FirstLobby.OWNER_ID) }
                }
    }

    @Test
    @WithMockUser(username = "7a2c4088-045e-4513-97f0-7900a7231305")
    fun `can start game`() {
        val gameId = mockMvc.post("/lobbies/c84ca410-d97a-4ef5-8fb3-e59309848c96/start-game")
                .andExpect {
                    status { isOk }
                    jsonPath("id") { isString }
                }
                .andReturn()
                .response
                .contentAsString
                .let { JSONObject(it) }
                .getString("id")

        mockMvc.get("/games/{id}", gameId)
                .andExpect {
                    status { isOk }
                    jsonPath("id") { value(gameId) }
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
    const val OWNER_ID = "df1a63de-c6b9-4383-ab56-761e3339be6c"
    const val NUM_OF_PLAYERS = 2
}

private object SecondLobby {
    const val ID = "2025c4cf-3e70-4730-8474-21cc6a760647"
    const val NAME = "Pokój #2"
    const val CREATED_AT = "2020-01-31T22:08:33Z"
    const val OWNER_ID = "b4467e4d-e9cd-416c-93c6-29d60f682ba8"
    const val NUM_OF_PLAYERS = 1
}