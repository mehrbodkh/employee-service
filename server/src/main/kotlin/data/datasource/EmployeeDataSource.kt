package com.mehrbod.data.datasource

import com.mehrbod.model.EmployeeDTO
import java.util.*

interface EmployeeDataSource {
    suspend fun save(employee: EmployeeDTO): EmployeeDTO
    suspend fun update(newEmployee: EmployeeDTO): EmployeeDTO
    suspend fun delete(id: UUID)
    suspend fun getSubordinates(managerId: String): List<EmployeeDTO>
    suspend fun getSupervisors(employeeId: String): List<EmployeeDTO>
    suspend fun getById(id: UUID): EmployeeDTO?
    suspend fun fetchAllEmployees(): List<EmployeeDTO>
}