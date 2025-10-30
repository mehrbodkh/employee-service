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
            dbDataSource.createEmployee(employee)
        } catch (_: Exception) {
            throw EmployeeCouldNotBeCreatedException()
        }
    }

    suspend fun updateEmployee(updatedEmployee: EmployeeDTO) = withContext(ioDispatcher) {
        dbDataSource.updateEmployee(updatedEmployee)
    }

    suspend fun deleteEmployee(id: UUID) = dbDataSource.deleteEmployee(id)

    suspend fun getSubordinates(id: String) = withContext(ioDispatcher) {
        try {
            dbDataSource.getSubordinates(id)
        } catch (_: Exception) {
            throw EmployeeNotFoundException(id)
        }
    }


    suspend fun getSupervisors(id: String) = withContext(ioDispatcher) {
        dbDataSource.getSupervisors(id)
    }

    suspend fun getById(id: UUID): EmployeeDTO? = withContext(ioDispatcher) {
        dbDataSource.getById(id)
    }

    suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        dbDataSource.fetchAllEmployees()
    }
}
