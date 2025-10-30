package com.mehrbod.data.datasource

import com.mehrbod.model.EmployeeDTO
import java.util.*

interface EmployeeDataSource {
    suspend fun createEmployee(employee: EmployeeDTO): EmployeeDTO
    suspend fun updateEmployee(employee: EmployeeDTO): EmployeeDTO
    suspend fun deleteEmployee(id: UUID)
    suspend fun getSubordinates(managerId: String): List<EmployeeDTO>
    suspend fun getSupervisors(employeeId: String): List<EmployeeDTO>
    suspend fun getById(id: UUID): EmployeeDTO?
    suspend fun fetchAllEmployees(): List<EmployeeDTO>
}