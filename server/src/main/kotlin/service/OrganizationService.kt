package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeNodeDTO
import java.util.*

class OrganizationService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun getRootsSubordinates(depth: Int): List<List<EmployeeNodeDTO>> {
        return employeeRepository.getRootSubordinates(depth)
    }

    suspend fun getSubordinates(id: UUID, depth: Int): List<EmployeeNodeDTO> {
        if (employeeRepository.getById(id) == null) {
            throw EmployeeNotFoundException(id)
        }
        return employeeRepository.getSubordinates(id, depth)
    }

    suspend fun getSupervisors(id: UUID, depth: Int): List<EmployeeNodeDTO> {
        if (employeeRepository.getById(id) == null) {
            throw EmployeeNotFoundException(id)
        }
        return employeeRepository.getSupervisors(id, depth)
    }
}