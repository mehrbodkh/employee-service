package com.mehrbod.controller

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.util.initializedTestApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class EmployeeControllerTest {

    companion object {
        const val API_PREFIX = "/api/v1/employees"
    }

    @Test
    fun testInvalidRequestObject() = initializedTestApplication {
        val response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("", "test2", "test3", "test4"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val message = response.bodyAsText()
        assertEquals("Mandatory fields cannot be empty.", message)
    }

    @Test
    fun testInvalidUUIDRequestObject() = initializedTestApplication {
        val response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("test1", "test2", "test3", "test4", "094324"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val message = response.bodyAsText()
        assertEquals("Invalid supervisorId: 094324", message)
    }

    @Test
    fun testEmployeeCreation() = initializedTestApplication {
        val response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("test1", "test2", "creation@gmail.com", "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.bodyAsText()
        assertDoesNotThrow { UUID.fromString(uuid) }
    }

    @Test
    fun testEmployeeRetrieval() = initializedTestApplication {
        var response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(CreateEmployeeRequest("test1", "test2", "retrieval@gmail.com", "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.bodyAsText()
        assertDoesNotThrow { UUID.fromString(uuid) }

        response = client.get("$API_PREFIX/$uuid")
        val employeeDTO = response.body<EmployeeDTO>()

        assertEquals(
            EmployeeDTO(employeeDTO.id, "test1", "test2", "retrieval@gmail.com", "test4", null, 0),
            employeeDTO
        )
    }
}
