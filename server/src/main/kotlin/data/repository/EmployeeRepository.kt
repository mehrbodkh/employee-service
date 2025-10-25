package com.mehrbod.data.repository

import com.mehrbod.data.dao.EmployeeDao
import com.mehrbod.data.dao.EmployeesTable
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class EmployeeRepository(private val db: R2dbcDatabase) {
    suspend fun createEmployee() = suspendTransaction(db = db) {
        SchemaUtils.create(EmployeesTable)

        EmployeeDao.new {
            name = "Something"
            surname = "Something"
            email = "Something"
            position = "Something"
        }
    }
}