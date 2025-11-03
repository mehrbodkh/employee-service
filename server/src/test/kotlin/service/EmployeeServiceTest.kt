package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.OwnReferenceException
import com.mehrbod.model.EmployeeDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class EmployeeServiceTest {

    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @InjectMockKs
    private lateinit var service: EmployeeService


    @Test
    fun `should create employee`() = runTest {
        val employeeDTO = EmployeeDTO(
            "id", "name", "surname", "email", "position", "supervisor", 1
        )
        coEvery { employeeRepository.createEmployee(any()) } returns employeeDTO
        coEvery { employeeRepository.getSubordinates(any()) } returns emptyList()

        val result = service.createEmployee(mockk())

        coVerify { employeeRepository.createEmployee(any()) }
        assertEquals(employeeDTO.copy(subordinatesCount = 0), result)
    }

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

    @Test
    fun `should update employee - success`() = runTest {
        val employeeDTO = EmployeeDTO(
            "id", "name", "surname", "email", "position", UUID.randomUUID().toString(), 0
        )
        coEvery { employeeRepository.updateEmployee(any()) } returns employeeDTO

        service.updateEmployee(employeeDTO)

        coVerify { employeeRepository.updateEmployee(any()) }
    }

    @Test
    fun `should update employee - failure`() = runTest {
        val id = UUID.randomUUID().toString()
        val employeeDTO = EmployeeDTO(
            id, "name", "surname", "email", "position", id, 0
        )
        coEvery { employeeRepository.updateEmployee(any()) } returns employeeDTO

        assertThrows<OwnReferenceException> { service.updateEmployee(employeeDTO) }

        coVerify(inverse = true) { employeeRepository.updateEmployee(any()) }
    }

    @Test
    fun `should delete employee`() = runTest {
        val id = UUID.randomUUID()
        coEvery { employeeRepository.deleteEmployee(any()) } returns Unit

        service.deleteEmployee(id)

        coVerify { employeeRepository.deleteEmployee(id) }
    }

    @Test
    fun `should get all employees`() = runTest {
        coEvery { employeeRepository.fetchAllEmployees() } returns mockk()

        service.getAllEmployees()

        coVerify { employeeRepository.fetchAllEmployees() }
    }

}
