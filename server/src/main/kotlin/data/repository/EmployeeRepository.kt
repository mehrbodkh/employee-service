package com.mehrbod.data.repository

import com.mehrbod.data.dao.EmployeeDao
import com.mehrbod.data.dao.EmployeesTable
import com.mehrbod.data.util.suspendTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

class EmployeeRepository(private val db: Database) {
    suspend fun createEmployee() = withContext(Dispatchers.IO) {
        suspendTransaction {
            EmployeeDao.new {
                name = "something"
                surname = "something else"
                email = "really"
                position = "Senior nothing"
            }
        }
    }
}