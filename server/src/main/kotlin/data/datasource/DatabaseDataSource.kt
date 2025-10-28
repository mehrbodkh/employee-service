package com.mehrbod.data.datasource

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.table.EmployeesTable
import com.mehrbod.data.table.convertToEmployeeDTO
import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

class DatabaseDataSource(
    private val db: R2dbcDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun createEmployee(employee: CreateEmployeeRequest): UUID = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable.insertAndGetId {
                it[name] = employee.name
                it[surname] = employee.surname
                it[email] = employee.email
                it[position] = employee.position
            }.value
        }
    }

    suspend fun getById(id: UUID): EmployeeDTO? = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable
                .selectAll()
                .where { EmployeesTable.id eq id }
                .singleOrNull()
                ?.convertToEmployeeDTO()
        }
    }

    suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable.selectAll().map { it.convertToEmployeeDTO() }.toList()
        }
    }
}
