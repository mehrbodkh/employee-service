package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeNodeDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OrganizationServiceTest {
    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @InjectMockKs
    private lateinit var service: OrganizationService

    @Nested
    inner class Subordinates {

        @Test
        fun `should throw exception - if employee doesn't exist`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns null

            assertThrows<EmployeeNotFoundException> { service.getSubordinates(id, 3) }
            coVerify(inverse = true) { employeeRepository.getSubordinates(any(), any()) }
        }

        @Test
        fun `should return successfully`() = runTest {
            val id = UUID.randomUUID()
            val mockSubordinates = mockk<List<EmployeeNodeDTO>>()
            coEvery { employeeRepository.getById(any()) } returns mockk()
            coEvery { employeeRepository.getSubordinates(any(), any()) } returns mockSubordinates

            val result = service.getSubordinates(id, 3)
            coVerify{ employeeRepository.getSubordinates(id, 3) }
            assertEquals(mockSubordinates, result)
        }
    }

    @Nested
    inner class Supervisors {

        @Test
        fun `should throw exception - if employee doesn't exist`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns null

            assertThrows<EmployeeNotFoundException> { service.getSupervisors(id, 3) }
            coVerify(inverse = true) { employeeRepository.getSupervisors(any(), any()) }
        }

        @Test
        fun `should return successfully`() = runTest {
            val id = UUID.randomUUID()
            val mockSupervisors = mockk<List<EmployeeNodeDTO>>()
            coEvery { employeeRepository.getById(any()) } returns mockk()
            coEvery { employeeRepository.getSupervisors(any(), any()) } returns mockSupervisors

            val result = service.getSupervisors(id, 3)
            coVerify{ employeeRepository.getSupervisors(id, 3) }
            assertEquals(mockSupervisors, result)
        }
    }

    @Test
    fun `shout get root subordinates`() = runTest {
        val mockResponse = mockk<List<List<EmployeeNodeDTO>>>()
        coEvery { employeeRepository.getRootSubordinates(any()) } returns mockResponse

        val response = service.getRootsSubordinates(3)

        coVerify { employeeRepository.getRootSubordinates(3) }
        assertEquals(mockResponse, response)
    }
}
