package pl.tomaszstankowski.fourplayerchess

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AuthenticationIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `can authenticate with token`() {
        val responseJson = mockMvc.post("/token")
                .andExpect {
                    status { isOk }
                    content {
                        contentType(APPLICATION_JSON)
                    }
                    jsonPath("token") { isString }
                }
                .andReturn()
                .response
                .contentAsString

        val token = objectMapper.readTree(responseJson)["token"].asText()

        mockMvc.get("/lobbies") {
            header("Authorization", "Bearer $token")
        }
                .andExpect {
                    status { isOk }
                }
    }

    @Test
    fun `cannot access secured endpoints without authentication`() {
        mockMvc.get("/lobbies")
                .andExpect {
                    status { isUnauthorized }
                }
    }
}