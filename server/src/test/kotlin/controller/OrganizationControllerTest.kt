package com.mehrbod.controller

import com.mehrbod.model.EmployeeNodeDTO
import com.mehrbod.util.initializedTestApplication
import com.mehrbod.util.recreateTables
import com.mehrbod.util.setupHierarchy
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrganizationControllerTest {

    companion object {
        const val API_PREFIX = "/api/v1/org"
    }

    @AfterEach
    fun tearDown() {
        runBlocking {
            recreateTables()
        }
    }

    @Test
    fun `should get root hierarchy - depth 1`() = initializedTestApplication {
        setupHierarchy()

        val response = client.get("$API_PREFIX/root?depth=1").body<List<List<EmployeeNodeDTO>>>()
        assertEquals(3, response.flatten().size)
    }

    @Test
    fun `should get root hierarchy - depth 2`() = initializedTestApplication {
        setupHierarchy()

        val response = client.get("$API_PREFIX/root?depth=2").body<List<List<EmployeeNodeDTO>>>()
        assertEquals(5, response.flatten().size)
    }

    @Test
    fun `should get root hierarchy - depth 3`() = initializedTestApplication {
        setupHierarchy()

        val response = client.get("$API_PREFIX/root?depth=3").body<List<List<EmployeeNodeDTO>>>()
        assertEquals(8, response.flatten().size)
    }

    @Test
    fun `should get root hierarchy - multiple roots - depth 3`() = initializedTestApplication {
        setupHierarchy(2)

        val response = client.get("$API_PREFIX/root?depth=3").body<List<List<EmployeeNodeDTO>>>()
        assertEquals(16, response.flatten().size)
        assertEquals(2, response.size)
    }

    @Test
    fun `should get hierarchy for specific employee`() = initializedTestApplication {
        val hierarchy = setupHierarchy()
        var rootId = hierarchy.flatten().find { it.name == "employee3" }!!.id

        var response = client.get("$API_PREFIX/$rootId/hierarchy?depth=10").body<List<EmployeeNodeDTO>>()
        assertEquals(6, response.size)

        response = client.get("$API_PREFIX/$rootId/hierarchy?depth=1").body<List<EmployeeNodeDTO>>()
        assertEquals(3, response.size)

        rootId = hierarchy.flatten().find { it.name == "employee2" }!!.id
        response = client.get("$API_PREFIX/$rootId/hierarchy?depth=3").body<List<EmployeeNodeDTO>>()
        assertEquals(1, response.size)
    }

    @Test
    fun `should get supervisors`() = initializedTestApplication {
        val hierarchy = setupHierarchy()
        val rootId = hierarchy.flatten().find { it.name == "employee6" }!!.id

        var response = client.get("$API_PREFIX/$rootId/supervisors?depth=3").body<List<EmployeeNodeDTO>>()
        assertEquals(4, response.size)

        response = client.get("$API_PREFIX/$rootId/supervisors?depth=2").body<List<EmployeeNodeDTO>>()
        assertEquals(3, response.size)

        response = client.get("$API_PREFIX/$rootId/supervisors?depth=1").body<List<EmployeeNodeDTO>>()
        assertEquals(2, response.size)
    }

}