package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.OwnReferenceException
import com.mehrbod.model.EmployeeDTO
import java.util.*

class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun createEmployee(request: EmployeeDTO): EmployeeDTO {
        val savedEmployee = employeeRepository.createEmployee(request)
        return savedEmployee.copy(subordinatesCount = 0)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO {
        val employee = employeeRepository.getById(id)
        val subordinatesCount = employeeRepository.getSubordinates(id).count()
        return employee.copy(subordinatesCount = subordinatesCount)
    }

    suspend fun updateEmployee(updatedInfo: EmployeeDTO): EmployeeDTO {
        if (updatedInfo.supervisorId == updatedInfo.id) {
            throw OwnReferenceException()
        }
        return employeeRepository.updateEmployee(updatedInfo)
    }

    suspend fun deleteEmployee(id: UUID) {
        return employeeRepository.deleteEmployee(id)
    }

    suspend fun getAllEmployees() = employeeRepository.fetchAllEmployees()

}