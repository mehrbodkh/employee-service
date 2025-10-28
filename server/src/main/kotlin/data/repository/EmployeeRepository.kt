package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.datasource.DatabaseDataSource
import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EmployeeRepository(
    private val dbDataSource: DatabaseDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun createEmployee(employee: CreateEmployeeRequest): UUID = withContext(ioDispatcher) {
        dbDataSource.createEmployee(employee)
    }

    suspend fun getById(id: String): EmployeeDTO? = withContext(ioDispatcher) {
        dbDataSource.getById(id)
    }

    suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        dbDataSource.fetchAllEmployees()
    }
}
