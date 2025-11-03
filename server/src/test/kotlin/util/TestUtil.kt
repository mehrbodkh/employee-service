package com.mehrbod.util

import com.mehrbod.controller.EmployeeControllerTest.Companion.API_PREFIX
import com.mehrbod.data.table.EmployeeHierarchyTable
import com.mehrbod.data.table.EmployeesTable
import com.mehrbod.model.EmployeeDTO
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.coroutines.EmptyCoroutineContext

suspend fun measureRPS(requestCount: Int, block: suspend () -> Unit) {
    val startTime = System.currentTimeMillis()
    block()
    val endTime = System.currentTimeMillis()
    val totalTimeInSeconds = (endTime - startTime) / 1000.0
    println("RPS: ${requestCount / totalTimeInSeconds}")
}

fun initializedTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication(
    EmptyCoroutineContext,
    {
        initializeApplication()
        block()
    }
)

private fun ApplicationTestBuilder.initializeApplication() {
    environment {
        config = ApplicationConfig("application-test.yaml")
    }
    client = createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }
}

fun getDefaultEmployeeDTO(
    id: String? = null,
    name: String = "John",
    surname: String = "Doe",
    position: String = "CTO",
    email: String = "default@mail.com",
    supervisorId: String? = null,
    subordinatesCount: Int? = null,
) = EmployeeDTO(id, name, surname, email, position, supervisorId, subordinatesCount)

suspend fun ApplicationTestBuilder.createEmployee(employee: EmployeeDTO) = client.post(API_PREFIX) {
    contentType(ContentType.Application.Json)
    setBody(employee)
}.body<EmployeeDTO>()

suspend fun ApplicationTestBuilder.deleteEmployee(id: String) = client.delete("$API_PREFIX/$id") {}

suspend fun ApplicationTestBuilder.updateEmployee(id: String, employee: EmployeeDTO) =
    client.put("$API_PREFIX/$id") {
        contentType(ContentType.Application.Json)
        setBody(employee)
    }.body<EmployeeDTO>()

suspend fun ApplicationTestBuilder.getEmployee(id: String) =
    client.get("$API_PREFIX/$id") {}.body<EmployeeDTO>()


suspend fun recreateTables() = suspendTransaction {
    SchemaUtils.drop(EmployeesTable, EmployeeHierarchyTable)
    SchemaUtils.create(EmployeesTable, EmployeeHierarchyTable)
}

suspend fun ApplicationTestBuilder.setupHierarchy(rootsCount: Int = 1): List<List<EmployeeDTO>> {
    val result = mutableListOf<List<EmployeeDTO>>()
    repeat(rootsCount) {
        val employee1 = createEmployee(getDefaultEmployeeDTO(name = "employee1", email = "email1"))
        val employee2 =
            createEmployee(getDefaultEmployeeDTO(name = "employee2", email = "email2", supervisorId = employee1.id))
        val employee3 =
            createEmployee(getDefaultEmployeeDTO(name = "employee3", email = "email3", supervisorId = employee1.id))
        val employee4 =
            createEmployee(getDefaultEmployeeDTO(name = "employee4", email = "email4", supervisorId = employee3.id))
        val employee5 =
            createEmployee(getDefaultEmployeeDTO(name = "employee5", email = "email5", supervisorId = employee3.id))
        val employee6 =
            createEmployee(getDefaultEmployeeDTO(name = "employee6", email = "email6", supervisorId = employee5.id))
        val employee7 =
            createEmployee(getDefaultEmployeeDTO(name = "employee7", email = "email7", supervisorId = employee5.id))
        val employee8 =
            createEmployee(getDefaultEmployeeDTO(name = "employee8", email = "email8", supervisorId = employee5.id))

        result.add(listOf(employee1, employee2, employee3, employee4, employee5, employee6, employee7, employee8))
    }
    return result
}
