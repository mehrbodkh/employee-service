package com.mehrbod.controller

import com.mehrbod.data.table.EmployeeHierarchyTable
import com.mehrbod.data.table.EmployeesTable
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.util.initializedTestApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.util.*

class EmployeeControllerTest {

    companion object {
        const val API_PREFIX = "/api/v1/employees"
    }

    @AfterEach
    fun setup() {
        runBlocking {
            suspendTransaction {
                SchemaUtils.drop(EmployeesTable, EmployeeHierarchyTable)
                SchemaUtils.create(EmployeesTable, EmployeeHierarchyTable)
            }
        }
    }

    @Nested
    inner class Validation {
        @Test
        fun testInvalidRequestObject() = initializedTestApplication {
            val response = client.post(API_PREFIX) {
                contentType(ContentType.Application.Json)
                setBody(EmployeeDTO(name = "", surname = "test2", email = "test3", position = "test4"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val message = response.bodyAsText()
            assertEquals("Mandatory fields cannot be empty.", message)
        }

        @Test
        fun testInvalidUUIDRequestObject() = initializedTestApplication {
            val response = client.post(API_PREFIX) {
                contentType(ContentType.Application.Json)
                setBody(
                    EmployeeDTO(
                        name = "test1",
                        surname = "test2",
                        email = "test3",
                        position = "test4",
                        supervisorId = "094324"
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val message = response.bodyAsText()
            assertEquals("Invalid supervisorId: 094324", message)
        }
    }

    @Nested
    inner class HierarchyUpdates {

        @Test
        fun `should delete supervisor if supervisor is removed`() = initializedTestApplication {
            val employee1 = createEmployee(EmployeeDTO(null, "name", "surname", "email", "position"))
            var employee2 = createEmployee(EmployeeDTO(null, "name", "surname", "email2", "position", employee1.id))

            deleteEmployee(employee1.id.toString()).bodyAsText()

            employee2 = getEmployee(employee2.id!!)
            assertNull(employee2.supervisorId)
        }

        @Test
        fun `should change supervisor if supervisor is removed`() = initializedTestApplication {
            val employee1 = createEmployee(EmployeeDTO(null, "name", "surname", "email", "position"))
            val employee2 = createEmployee(EmployeeDTO(null, "name", "surname", "email2", "position", employee1.id))
            var employee3 = createEmployee(EmployeeDTO(null, "name", "surname", "email3", "position", employee2.id))
            var employee4 = createEmployee(EmployeeDTO(null, "name", "surname", "email4", "position", employee2.id))

            deleteEmployee(employee2.id.toString()).bodyAsText()

            employee3 = getEmployee(employee3.id!!)
            employee4 = getEmployee(employee4.id!!)
            assertEquals(employee3.supervisorId, employee1.id)
            assertEquals(employee4.supervisorId, employee1.id)
        }

        @Test
        fun `should change supervisor if supervisor is updated`() = initializedTestApplication {
            val employee0 = createEmployee(EmployeeDTO(null, "name", "surname", "email0", "position"))
            val employee1 = createEmployee(EmployeeDTO(null, "name", "surname", "email1", "position", employee0.id))
            var employee2 = createEmployee(EmployeeDTO(null, "name", "surname", "email2", "position", employee1.id))
            var employee3 = createEmployee(EmployeeDTO(null, "name", "surname", "email3", "position", employee2.id))
            var employee4 = createEmployee(EmployeeDTO(null, "name", "surname", "email4", "position", employee3.id))
            var employee5 = createEmployee(EmployeeDTO(null, "name", "surname", "email5", "position", employee3.id))

            employee2 = updateEmployee(employee2.id.toString(), employee2.copy(supervisorId = employee0.id))

            employee3 = getEmployee(employee3.id!!)
            employee4 = getEmployee(employee4.id!!)
            employee5 = getEmployee(employee5.id!!)
            assertEquals(employee2.supervisorId, employee0.id)
            assertEquals(employee3.supervisorId, employee2.id)
            assertEquals(employee4.supervisorId, employee3.id)
            assertEquals(employee5.supervisorId, employee3.id)
        }

    }

    @Test
    fun shouldWorkFineWithTransactions() = initializedTestApplication {
        runTest {
            val jobs = mutableListOf<Deferred<*>>()
            repeat(1000) {
                jobs.add(
                    async {
                        try {
                            createEmployee(
                                EmployeeDTO(
                                    name = "name",
                                    surname = "surname",
                                    email = "email",
                                    position = "position"
                                )
                            )
                        } catch (_: Exception) {}
                    }
                )
            }
            jobs.joinAll()

            val response = client.get("$API_PREFIX/fetch-all").body<List<EmployeeDTO>>()
            assertEquals(1, response.size)
        }
    }

    @Test
    fun testEmployeeCreation() = initializedTestApplication {
        val response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(EmployeeDTO(name = "test1", surname = "test2", email = "creation@gmail.com", position = "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.body<EmployeeDTO>().id
        assertDoesNotThrow { UUID.fromString(uuid) }
    }

    @Test
    fun testEmployeeRetrieval() = initializedTestApplication {
        var response = client.post(API_PREFIX) {
            contentType(ContentType.Application.Json)
            setBody(EmployeeDTO(name = "test1", surname = "test2", email = "retrieval@gmail.com", position = "test4"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = response.body<EmployeeDTO>().id
        assertDoesNotThrow { UUID.fromString(uuid) }

        response = client.get("$API_PREFIX/$uuid")
        val employeeDTO = response.body<EmployeeDTO>()

        assertEquals(
            EmployeeDTO(employeeDTO.id, "test1", "test2", "retrieval@gmail.com", "test4", null, 0),
            employeeDTO
        )
    }

    private suspend fun ApplicationTestBuilder.createEmployee(employee: EmployeeDTO) = client.post(API_PREFIX) {
        contentType(ContentType.Application.Json)
        setBody(employee)
    }.body<EmployeeDTO>()

    private suspend fun ApplicationTestBuilder.deleteEmployee(id: String) = client.delete("$API_PREFIX/$id") {}

    private suspend fun ApplicationTestBuilder.updateEmployee(id: String, employee: EmployeeDTO) = client.put("$API_PREFIX/$id") {
        contentType(ContentType.Application.Json)
        setBody(employee)
    }.body<EmployeeDTO>()

    private suspend fun ApplicationTestBuilder.getEmployee(id: String) =
        client.get("$API_PREFIX/$id") {}.body<EmployeeDTO>()
}
