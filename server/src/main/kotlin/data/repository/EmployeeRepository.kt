package com.mehrbod.data.repository

import com.mehrbod.data.datasource.EmployeeDataSource
import com.mehrbod.exception.EmployeeCouldNotBeCreatedException
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EmployeeRepository(
    private val dbDataSource: EmployeeDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun createEmployee(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        try {
            dbDataSource.save(employee)
        } catch (_: Exception) {
            throw EmployeeCouldNotBeCreatedException()
        }
    }

    suspend fun updateEmployee(updatedEmployee: EmployeeDTO) = withContext(ioDispatcher) {
        dbDataSource.update(updatedEmployee)
    }

    suspend fun deleteEmployee(id: UUID) {
        dbDataSource.delete(id)
    }

    suspend fun getSubordinates(id: UUID, depth: Int = 1) = withContext(ioDispatcher) {
        dbDataSource.getSubordinates(id, depth)
    }

    suspend fun getSupervisors(id: UUID, depth: Int) = withContext(ioDispatcher) {
        dbDataSource.getSupervisors(id, depth)
    }

    suspend fun getById(id: UUID): EmployeeDTO = withContext(ioDispatcher) {
        dbDataSource.getById(id) ?: throw EmployeeNotFoundException(id.toString())
    }

    suspend fun getRootSubordinates(depth: Int) = withContext(ioDispatcher) {
        dbDataSource.getRootSubordinates(depth)
    }

    suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        dbDataSource.fetchAllEmployees()
    }
}
