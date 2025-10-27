package com.mehrbod.data.repository

import com.mehrbod.data.dao.EmployeesTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class EmployeeRepository(private val db: Database) {
    suspend fun createEmployee() = withContext(Dispatchers.IO) {
        transaction(db) {
            SchemaUtils.create(EmployeesTable)
            EmployeesTable.insert {
                it[name] = "something"
                it[surname] = "something"
                it[email] = "something"
                it[position] = "something"
            }
        }
    }
}