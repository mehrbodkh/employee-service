package com.mehrbod.data.datasource

import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import java.util.*

interface EmployeeDataSource {
    suspend fun save(employee: EmployeeDTO): EmployeeDTO
    suspend fun update(newEmployee: EmployeeDTO): EmployeeDTO
    suspend fun delete(id: UUID)
    suspend fun getSubordinates(managerId: UUID, depth: Int): List<EmployeeNodeDTO>
    suspend fun getSupervisors(employeeId: UUID, depth: Int): List<EmployeeNodeDTO>
    suspend fun getRootSubordinates(depth: Int): List<EmployeeNodeDTO>
    suspend fun getById(id: UUID): EmployeeDTO?
    suspend fun fetchAllEmployees(): List<EmployeeDTO>
}