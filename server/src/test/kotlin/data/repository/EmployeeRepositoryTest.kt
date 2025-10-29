package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.EmployeeRequest
import com.mehrbod.data.datasource.DatabaseEmployeeDataSource
import com.mehrbod.model.EmployeeDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class EmployeeRepositoryTest {

    @MockK
    private lateinit var dataSource: DatabaseEmployeeDataSource

    private val repository by lazy {
        EmployeeRepository(dataSource, Dispatchers.Unconfined)
    }

    @Test
    fun shouldCreateEmployee() = runTest {
        val request = mockk<EmployeeRequest>()
        val employee = mockk<EmployeeDTO>()
        coEvery { dataSource.createEmployee(any()) } returns employee

        val response = repository.createEmployee(request)

        coVerify(exactly = 1) { dataSource.createEmployee(request) }
        assertEquals(employee, response)
    }

    @Test
    fun shouldFetchEmployee() = runTest {
        val uuid = mockk<UUID>()
        val employee = mockk<EmployeeDTO>()
        coEvery { dataSource.getById(any()) } returns employee

        val response = repository.getById(uuid)

        coVerify(exactly = 1) { dataSource.getById(uuid) }
        assertEquals(employee, response)
    }
}
