package com.mehrbod.service

import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeNodeDTO
import java.util.UUID

class OrganizationService(
    private val employeeRepository: EmployeeRepository
) {

    suspend fun getSubordinates(id: UUID, depth: Int): List<EmployeeNodeDTO> {
        return employeeRepository.getSubordinates(id, depth)
    }
}