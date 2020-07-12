package pl.tomaszstankowski.fourplayerchess

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

class GameControlTest : IntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "aa856852-22bc-4713-8300-8c8f907a8e18")
    fun `can get game`() {
        mockMvc.get("/games/{id}/", Game.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("id") { value(Game.ID) }
                    jsonPath("createdAt") { value(Game.CREATED_AT) }
                    jsonPath("isCancelled") { value(Game.IS_CANCELLED) }
                    jsonPath("isFinished") { value(Game.IS_FINISHED) }
                }
    }

    @Test
    @WithMockUser(username = "aa856852-22bc-4713-8300-8c8f907a8e18")
    fun `can get players of the game`() {
        mockMvc.get("/games/{id}/players", Game.ID)
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(4))
                    jsonPath("[0].playerId") { value("02d37d81-d1e1-4719-872a-ccab471ea908") }
                    jsonPath("[0].color") { value("red") }
                    jsonPath("[0].type") { value("human") }
                    jsonPath("[1].playerId") { value("4f56f39c-2f52-4015-9353-49c7580458b3") }
                    jsonPath("[1].color") { value("blue") }
                    jsonPath("[1].type") { value("human") }
                    jsonPath("[2].playerId") { value("2f0bc4e1-5b36-47f7-a0b3-49bf9109696d") }
                    jsonPath("[2].color") { value("yellow") }
                    jsonPath("[2].type") { value("human") }
                    jsonPath("[3].playerId") { isEmpty }
                    jsonPath("[3].color") { value("green") }
                    jsonPath("[3].type") { value("randomBot") }
                }
    }

    @Test
    @WithMockUser(username = "02d37d81-d1e1-4719-872a-ccab471ea908")
    fun `can get active games of player`() {
        mockMvc.get("/games/active-for-me")
                .andExpect {
                    status { isOk }
                    jsonPath("$", hasSize<Any>(1))
                    jsonPath("[0].id") { value(Game.ID) }
                }
    }
}

object Game {
    const val ID = "3cc49522-81f2-44d1-a73c-8ba35dddc219"
    const val CREATED_AT = "2020-06-24T21:13:02Z"
    const val IS_CANCELLED = false
    const val IS_FINISHED = false
}