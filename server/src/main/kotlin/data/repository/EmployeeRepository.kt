package com.mehrbod.data.repository

import com.mehrbod.data.dao.EmployeesTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
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
}