package com.mehrbod.data.repository

import com.mehrbod.data.dao.EmployeesTable
import com.mehrbod.model.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class EmployeeRepository(private val db: R2dbcDatabase) {
    suspend fun createEmployee() = withContext(Dispatchers.IO) {
        suspendTransaction(db) {
            EmployeesTable.insert {
                it[name] = "something"
                it[surname] = "something else"
                it[email] = "really"
                it[position] = "Senior nothing"
            }
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