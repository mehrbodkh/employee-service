package com.mehrbod.controller

import com.mehrbod.model.EmployeeNodeDTO
import com.mehrbod.util.initializedTestApplication
import com.mehrbod.util.recreateTables
import com.mehrbod.util.setupHierarchy
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
    fun `should get root hierarchy - different levels`() = initializedTestApplication {
        val hierarchy = setupHierarchy()

        val response = client.get("$API_PREFIX/root?depth").body<List<EmployeeNodeDTO>>()

        println(response)
    }

}