package com.mehrbod.controller

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.controller.model.response.PaginatedResponse
import com.mehrbod.model.ReviewDTO
import com.mehrbod.util.createEmployee
import com.mehrbod.util.getDefaultEmployeeDTO
import com.mehrbod.util.initializedTestApplication
import com.mehrbod.util.recreateTables
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class ReviewControllerTest {

    companion object {
        const val API_PREFIX = "/api/v1/review"
    }

    @AfterEach
    fun tearDown() {
        runBlocking {
            recreateTables()
        }
    }

    @Nested
    inner class Submit {

        @Test
        fun `should fail - invalid input score`() = initializedTestApplication {
            val response = client.post("$API_PREFIX/${UUID.randomUUID()}/submit") {
                contentType(ContentType.Application.Json)
                setBody(SubmitReviewRequest(11, 10, 10, 10))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `should submit and fetch reviews`() = initializedTestApplication {
            runTest {
                val id = createEmployee(getDefaultEmployeeDTO()).id
                val requests = mutableListOf<Deferred<*>>()
                repeat(10) {
                    requests.add(async {
                        client.post("$API_PREFIX/$id/submit") {
                            contentType(ContentType.Application.Json)
                            setBody(SubmitReviewRequest(10, 10, 10, 10))
                        }
                    })
                }

                requests.onEach { it.await() }

                var response = client.get("$API_PREFIX/$id?page=1&pageSize=5").body<PaginatedResponse<ReviewDTO>>()

                assertEquals(5, response.data.size)

                response = client.get("$API_PREFIX/$id?page=1&pageSize=15").body<PaginatedResponse<ReviewDTO>>()

                assertEquals(10, response.data.size)
            }
        }

    }
}
