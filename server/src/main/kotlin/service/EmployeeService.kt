package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.exception.OwnManagerException
import com.mehrbod.model.EmployeeDTO
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun createEmployee(request: EmployeeDTO): EmployeeDTO {
        val savedEmployee = employeeRepository.createEmployee(request)
        val subordinatesCount = employeeRepository.getSubordinates(UUID.fromString(savedEmployee.id)).count()
        return savedEmployee.copy(subordinatesCount = subordinatesCount)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO {
        return employeeRepository.getById(id)?.let {
            val subordinatesCount = employeeRepository.getSubordinates(id).count()
            it.copy(subordinatesCount = subordinatesCount)
        } ?: run {
            throw EmployeeNotFoundException(id.toString())
        }
    }

    suspend fun updateEmployee(updatedInfo: EmployeeDTO): EmployeeDTO {
        if (updatedInfo.supervisorId == updatedInfo.id) {
            throw OwnManagerException()
        }
        return employeeRepository.updateEmployee(updatedInfo)
    }

    suspend fun deleteEmployee(id: UUID) {
        return employeeRepository.deleteEmployee(id)
    }

    suspend fun getAllEmployees() = employeeRepository.fetchAllEmployees()

}