package com.mehrbod.data.datasource.employee

import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import java.util.*

interface EmployeeDataSource {
    suspend fun save(employee: EmployeeDTO): EmployeeDTO
    suspend fun update(newEmployee: EmployeeDTO): EmployeeDTO
    suspend fun delete(id: UUID)
    suspend fun getSubordinatesCount(managerId: UUID): Long
    suspend fun getSubordinates(managerId: UUID, depth: Int): List<EmployeeNodeDTO>
    suspend fun getSupervisors(employeeId: UUID, depth: Int): List<EmployeeNodeDTO>
    suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>>
    suspend fun getById(id: UUID): EmployeeDTO?
    suspend fun getByEmail(email: String): EmployeeDTO?

    /**
     * Exists only for development purposes
     */
    suspend fun fetchAllEmployees(): List<EmployeeDTO>
}