package pl.tomaszstankowski.fourplayerchess

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.*

class LobbyIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
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
    fun `can delete lobby`() {
        mockMvc.delete("/lobbies/{id}", FirstLobby.ID)
            .andExpect { status { isOk } }
        mockMvc.get("/lobbies/{id}", FirstLobby.ID)
            .andExpect { status { isNotFound } }
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