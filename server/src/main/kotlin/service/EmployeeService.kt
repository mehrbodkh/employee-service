package com.mehrbod.service

import com.mehrbod.controller.model.request.EmployeeRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeDTO
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun createEmployee(employee: EmployeeRequest): EmployeeDTO {
        val employee = employeeRepository.createEmployee(employee)
        val subordinatesCount = employeeRepository.getSubordinates(employee.id).count()
        return employee.copy(subordinatesCount = subordinatesCount)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO {
        return employeeRepository.getById(id)?.let {
            val subordinatesCount = employeeRepository.getSubordinates(it.id).count()
            it.copy(subordinatesCount = subordinatesCount)
        } ?: run {
            throw EmployeeNotFoundException(id.toString())
        }
    }

    suspend fun updateEmployee(id: UUID, updatedInfo: EmployeeRequest) {

    }

    suspend fun getEmployeeSubordinates(id: UUID) = employeeRepository.getSubordinates(id.toString())

    suspend fun getEmployeeAncestors(id: UUID) {

    }

}