package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmailAlreadyExistsException
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.exception.OwnReferenceException
import com.mehrbod.util.getDefaultEmployeeDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@ExtendWith(MockKExtension::class)
class EmployeeServiceTest {

    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @RelaxedMockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var service: EmployeeService

    @Nested
    inner class Create {

        @Test
        fun `should throw exception - email already exists`() = runTest {
            coEvery { employeeRepository.getByEmail(any()) } returns mockk()

            assertThrows<EmailAlreadyExistsException> { service.createEmployee(getDefaultEmployeeDTO()) }
        }

        @Test
        fun `should throw exception - supervisor doesn't exist`() = runTest {
            coEvery { employeeRepository.getByEmail(any()) } returns null
            coEvery { employeeRepository.getById(any()) } returns null

            assertThrows<EmployeeNotFoundException> { service.createEmployee(getDefaultEmployeeDTO(supervisorId = UUID.randomUUID())) }
        }

        @Test
        fun `should create employee - without supervisor`() = runTest {
            val employee = getDefaultEmployeeDTO()
            coEvery { employeeRepository.getByEmail(any()) } returns null
            coEvery { employeeRepository.createEmployee(any()) } returns employee

            val result = service.createEmployee(employee)

            coVerify { employeeRepository.getByEmail(employee.email) }
            coVerify(inverse = true) { employeeRepository.getById(any()) }
            coVerify { employeeRepository.createEmployee(employee) }

            assertEquals(employee, result)
        }

        @Test
        fun `should create employee - with supervisor`() = runTest {
            val employee = getDefaultEmployeeDTO(supervisorId = UUID.randomUUID())
            coEvery { employeeRepository.getByEmail(any()) } returns null
            coEvery { employeeRepository.getById(any()) } returns mockk()
            coEvery { employeeRepository.createEmployee(any()) } returns employee

            val result = service.createEmployee(employee)

            coVerify { employeeRepository.getByEmail(employee.email) }
            coVerify { employeeRepository.getById(employee.supervisorId!!) }
            coVerify { employeeRepository.createEmployee(employee) }

            assertEquals(employee, result)
        }
    }

    @Nested
    inner class Fetch {

        @Test
        fun `should throw exception - employee not found`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns null

            assertThrows<EmployeeNotFoundException> { service.getEmployee(id) }

            coVerify { employeeRepository.getById(id) }
        }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 5, 10])
        fun `should return employee`(count: Long) = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns getDefaultEmployeeDTO()
            coEvery { employeeRepository.getSubordinatesCount(any()) } returns count

            val result = service.getEmployee(id)

            coVerify { employeeRepository.getById(id) }
            coVerify { employeeRepository.getSubordinatesCount(id) }

            assertEquals(getDefaultEmployeeDTO(subordinatesCount = count.toInt()), result)
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `should throw exception - employee doesn't exist`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns null

            assertThrows<EmployeeNotFoundException> { service.updateEmployee(id, getDefaultEmployeeDTO()) }
        }

        @Test
        fun `should throw exception - new email already exits`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns getDefaultEmployeeDTO()
            coEvery { employeeRepository.getByEmail(any()) } returns mockk()

            assertThrows<EmailAlreadyExistsException> {
                service.updateEmployee(
                    id,
                    getDefaultEmployeeDTO(email = "new@email.com")
                )
            }
        }

        @Test
        fun `should throw exception - own id reference`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(any()) } returns getDefaultEmployeeDTO(id = id)

            assertThrows<OwnReferenceException> {
                service.updateEmployee(
                    id,
                    getDefaultEmployeeDTO(id = id, supervisorId = id)
                )
            }
        }

        @Test
        fun `should throw exception - supervisor does not exist`() = runTest {
            val id = UUID.randomUUID()
            val supervisorId = UUID.randomUUID()
            coEvery { employeeRepository.getById(id) } returns getDefaultEmployeeDTO(id = id)
            coEvery { employeeRepository.getById(supervisorId) } returns null
            assertThrows<EmployeeNotFoundException> {
                service.updateEmployee(
                    id,
                    getDefaultEmployeeDTO(id = id, supervisorId = supervisorId)
                )
            }
        }

        @Test
        fun `should update employee`() = runTest {
            val id = UUID.randomUUID()
            val supervisorId = UUID.randomUUID()
            coEvery { employeeRepository.getById(id) } returns getDefaultEmployeeDTO(id = id)
            coEvery { employeeRepository.getById(supervisorId) } returns mockk()
            coEvery { employeeRepository.updateEmployee(any()) } returns getDefaultEmployeeDTO(
                id = id,
                supervisorId = supervisorId
            )


            val result = service.updateEmployee(id, getDefaultEmployeeDTO(id = id, supervisorId = supervisorId))

            coVerify { employeeRepository.updateEmployee(getDefaultEmployeeDTO(id = id, supervisorId = supervisorId)) }
            coVerify { notificationService.sendManagerChangedNotification(id, supervisorId) }
            assertEquals(getDefaultEmployeeDTO(id = id, supervisorId = supervisorId), result)
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `should throw exception - employee doesn't exist`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(id) } returns null

            assertThrows<EmployeeNotFoundException> { service.deleteEmployee(id) }

            coVerify { employeeRepository.getById(id) }
        }

        @Test
        fun `should delete employee`() = runTest {
            val id = UUID.randomUUID()
            coEvery { employeeRepository.getById(id) } returns mockk()
            coEvery { employeeRepository.deleteEmployee(id) } returns Unit

            service.deleteEmployee(id)

            coVerify { employeeRepository.getById(id) }
            coVerify { employeeRepository.deleteEmployee(id) }
        }
    }
}
