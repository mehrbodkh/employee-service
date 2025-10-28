package com.mehrbod.controller

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.util.initApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*
import kotlin.test.Test

class EmployeeControllerTest {

    companion object {
        const val API_PREFIX = "/api/v1/employees"
    }

    @Test
    fun testEmployeeCreation() = testApplication {
        initApplication()

        val response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("test1", "test2", "test3", "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.bodyAsText()
        assertDoesNotThrow { UUID.fromString(uuid) }
    }

    @Test
    fun testEmployeeRetrieval() = testApplication {
        initApplication()

        var response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("test1", "test2", "test3", "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.bodyAsText()
        assertDoesNotThrow { UUID.fromString(uuid) }

        response = client.get("$API_PREFIX/$uuid")
        val employeeDTO = response.body<EmployeeDTO>()

        assertEquals(EmployeeDTO("test1", "test2", "test3", "test4"), employeeDTO)
    }

}