package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeNodeDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OrganizationServiceTest {
    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @InjectMockKs
    private lateinit var service: OrganizationService

    @Test
    fun `shout get root subordinates`() = runTest {
        val mockResponse = mockk<List<List<EmployeeNodeDTO>>>()
        coEvery { employeeRepository.getRootSubordinates(any()) } returns mockResponse

        val response = service.getRootsSubordinates(3)

        coVerify { employeeRepository.getRootSubordinates(3) }
        assertEquals(mockResponse, response)
    }

    @Test
    fun `shout get id subordinates`() = runTest {
        val mockResponse = mockk<List<EmployeeNodeDTO>>()
        val id = UUID.randomUUID()
        coEvery { employeeRepository.getSubordinates(any(), any()) } returns mockResponse

        val response = service.getSubordinates(id, 3)

        coVerify { employeeRepository.getSubordinates(id, 3) }
        assertEquals(mockResponse, response)
    }

    @Test
    fun `shout get id supervisors`() = runTest {
        val mockResponse = mockk<List<EmployeeNodeDTO>>()
        val id = UUID.randomUUID()
        coEvery { employeeRepository.getSupervisors(any(), any()) } returns mockResponse

        val response = service.getSupervisors(id, 3)

        coVerify { employeeRepository.getSupervisors(id, 3) }
        assertEquals(mockResponse, response)
    }

}
