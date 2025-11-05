package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.exception.OwnReferenceException
import com.mehrbod.exception.EmailAlreadyExistsException
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.notification.NotificationProducer
import com.mehrbod.notification.model.ManagerChangedEvent
import java.util.*

class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val notificationProducer: NotificationProducer,
) {

    suspend fun createEmployee(request: EmployeeDTO): EmployeeDTO {
        if (employeeRepository.getByEmail(request.email) != null) {
            throw EmailAlreadyExistsException()
        }

        if (request.supervisorId != null && employeeRepository.getById(request.supervisorId) == null) {
            throw EmployeeNotFoundException(request.supervisorId)
        }

        return employeeRepository.createEmployee(request)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO = employeeRepository.getById(id)?.let {
        val subordinatesCount = employeeRepository.getSubordinatesCount(id)
        it.copy(subordinatesCount = subordinatesCount.toInt())
    } ?: throw EmployeeNotFoundException(id)

    suspend fun updateEmployee(id: UUID, updatedInfo: EmployeeDTO): EmployeeDTO {
        val currentEmployee = employeeRepository.getById(id) ?: throw EmployeeNotFoundException(id)

        if (updatedInfo.email != currentEmployee.email && employeeRepository.getByEmail(updatedInfo.email) != null) {
            throw EmailAlreadyExistsException()
        }

        if (updatedInfo.supervisorId == id) {
            throw OwnReferenceException()
        }

        if (updatedInfo.supervisorId != null && employeeRepository.getById(updatedInfo.supervisorId) == null) {
            throw EmployeeNotFoundException(updatedInfo.supervisorId)
        }

        return employeeRepository.updateEmployee(updatedInfo).also {
            if (it.supervisorId != null && it.supervisorId != currentEmployee.supervisorId) {
                notificationProducer.sendEvent(ManagerChangedEvent(employeeID = id, managerID = it.supervisorId))
            }
        }
    }

    suspend fun deleteEmployee(id: UUID) {
        employeeRepository.getById(id) ?: throw EmployeeNotFoundException(id)
        employeeRepository.deleteEmployee(id)
    }

    suspend fun getAllEmployees() = employeeRepository.fetchAllEmployees()

}