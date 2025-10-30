package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeDTO
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun createEmployee(request: EmployeeDTO): EmployeeDTO {
        val employee = employeeRepository.createEmployee(request)
        val subordinatesCount = employeeRepository.getSubordinates(employee.id!!).count()
        return employee.copy(subordinatesCount = subordinatesCount)
    }

    suspend fun getEmployee(id: UUID): EmployeeDTO {
        return employeeRepository.getById(id)?.let {
            val subordinatesCount = employeeRepository.getSubordinates(it.id!!).count()
            it.copy(subordinatesCount = subordinatesCount)
        } ?: run {
            throw EmployeeNotFoundException(id.toString())
        }
    }

    suspend fun updateEmployee(updatedInfo: EmployeeDTO): EmployeeDTO {
        return employeeRepository.updateEmployee(updatedInfo)
    }

    suspend fun getEmployeeSubordinates(id: UUID) = try {
        employeeRepository.getSubordinates(id.toString())
    } catch (_: Exception) {
        throw EmployeeNotFoundException(id.toString())
    }

    suspend fun getEmployeeAncestors(id: UUID) {

    }

    suspend fun getAllEmployees() = employeeRepository.fetchAllEmployees()

}