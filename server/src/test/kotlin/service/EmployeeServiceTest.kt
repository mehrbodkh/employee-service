package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeDTO
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
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class EmployeeServiceTest {

    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @InjectMockKs
    private lateinit var service: EmployeeService


    @Nested
    inner class `Employee Creation` {

        @Test
        fun `should create employee`() = runTest {
            val employeeDTO = EmployeeDTO(
                "id", "name", "surname", "email", "position", "supervisor", 1
            )
            coEvery { employeeRepository.createEmployee(any()) } returns employeeDTO
            coEvery { employeeRepository.getSubordinates(any()) } returns emptyList()

            val result = service.createEmployee(mockk())

            coVerify { employeeRepository.createEmployee(any()) }
            coVerify { employeeRepository.getSubordinates(any()) }
            assertEquals(employeeDTO.copy(subordinatesCount = 0), result)
        }
    }

    @Nested
    inner class `Employee Fetch` {

        @Test
        fun `should fetch employee`() = runTest {
            val employeeDTO = EmployeeDTO(
                "id", "name", "surname", "email", "position", "supervisor", 0
            )
            coEvery { employeeRepository.getById(any()) } returns employeeDTO
            coEvery { employeeRepository.getSubordinates(any()) } returns listOf(mockk(), mockk())

            val result = service.getEmployee(mockk())

            coVerify { employeeRepository.getById(any()) }
            coVerify { employeeRepository.getSubordinates(any()) }
            assertEquals(employeeDTO.copy(subordinatesCount = 2), result)
        }
    }

}
