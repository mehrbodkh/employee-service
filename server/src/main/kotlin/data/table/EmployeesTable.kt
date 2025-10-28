package com.mehrbod.data.table

import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object EmployeesTable : UUIDTable("employees") {
    val name = varchar("name", length = 50)
    val surname = varchar("surname", length = 50)
    val email = varchar("email", length = 150)
    val position = varchar("position", length = 150)

    init {
        runBlocking {
            suspendTransaction {
                SchemaUtils.create(EmployeesTable)
            }
        }
    }
}

fun ResultRow.convertToEmployeeDTO() = EmployeeDTO(
    name = this[EmployeesTable.name],
    surname = this[EmployeesTable.surname],
    email = this[EmployeesTable.email],
    position = this[EmployeesTable.position],
)
