package com.mehrbod.service

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.OwnManagerException
import com.mehrbod.model.EmployeeDTO
import java.util.*

class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun createEmployee(request: EmployeeDTO): EmployeeDTO {
        val savedEmployee = employeeRepository.createEmployee(request)
        val subordinatesCount = employeeRepository.getSubordinates(savedEmployee.id.getUuidOrThrow()).count()
        return savedEmployee.copy(subordinatesCount = subordinatesCount)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO {
        val employee = employeeRepository.getById(id)
        val subordinatesCount = employeeRepository.getSubordinates(id).count()
        return employee.copy(subordinatesCount = subordinatesCount)
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