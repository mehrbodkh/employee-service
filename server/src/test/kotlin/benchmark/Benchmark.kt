package com.mehrbod.benchmark

import com.mehrbod.controller.EmployeeControllerTest.Companion.API_PREFIX
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.util.initializedTestApplication
import com.mehrbod.util.measureRPS
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class Benchmark {

    @Test
    @Disabled
    fun testEmployeeCreation() = initializedTestApplication {
        val requestNumber = 30000
        runTest {
            measureRPS(requestNumber) {
                val jobs = mutableListOf<Deferred<*>>()
                repeat(requestNumber) {
                    jobs.add(async {
                        val randomUUID = UUID.randomUUID()
                        client.post(API_PREFIX) {
                            contentType(ContentType.Application.Json)
                            setBody(EmployeeDTO(name = "test1", surname = "test2", email = "$randomUUID@gmail.com", position = "test4"))
                        }
                    })
                }
                jobs.onEach { it.await() }

                val response: List<EmployeeDTO> = client.get("$API_PREFIX/fetch-all").body()
                assertEquals(requestNumber, response.count())
            }
        }
    }
}