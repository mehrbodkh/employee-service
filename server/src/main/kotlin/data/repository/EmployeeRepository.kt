package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.dao.EmployeesTable
import com.mehrbod.model.Employee
import com.mehrbod.model.convertToEmployee
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

class EmployeeRepository(private val db: R2dbcDatabase) {
    suspend fun createEmployee(employee: CreateEmployeeRequest) = withContext(Dispatchers.IO) {
        suspendTransaction(db) {
            EmployeesTable.insertAndGetId {
                it[name] = employee.name
                it[surname] = employee.surname
                it[email] = employee.email
                it[position] = employee.position
            }.value
        }
    }

    suspend fun getById(id: String) = withContext(Dispatchers.IO) {
        suspendTransaction(db) {
            EmployeesTable
                .selectAll()
                .where { EmployeesTable.id eq UUID.fromString(id) }
                .singleOrNull()
                ?.convertToEmployee()
        }
    }

    suspend fun fetchAllEmployees() = withContext(Dispatchers.IO) {
        suspendTransaction(db) {
            EmployeesTable.selectAll().map {
                Employee(
                    it[EmployeesTable.name],
                    it[EmployeesTable.surname],
                    it[EmployeesTable.email],
                    it[EmployeesTable.position],
                )
            }
                .toList()
        }
    }
}