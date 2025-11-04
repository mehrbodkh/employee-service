package com.mehrbod.data.table

import com.mehrbod.data.table.EmployeesTable.email
import com.mehrbod.data.table.EmployeesTable.name
import com.mehrbod.data.table.EmployeesTable.position
import com.mehrbod.data.table.EmployeesTable.supervisor
import com.mehrbod.data.table.EmployeesTable.surname
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object EmployeesTable : UUIDTable("employees") {
    val name = varchar("name", length = 50)
    val surname = varchar("surname", length = 50)
    val email = varchar("email", length = 150).uniqueIndex()
    val position = varchar("position", length = 150)
    val supervisor = optReference("supervisor_id", EmployeesTable, onDelete = ReferenceOption.CASCADE)

    init {
        runBlocking {
            suspendTransaction {
                SchemaUtils.create(EmployeesTable)
            }
        }
    }
}

fun ResultRow.mapToEmployeeDTO() = EmployeeDTO(
    id = this[EmployeesTable.id].value,
    name = this[name],
    surname = this[surname],
    email = this[email],
    position = this[position],
    supervisorId = this[supervisor]?.value,
    subordinatesCount = 0,
)

fun ResultRow.mapToEmployeeNodeDTO() = EmployeeNodeDTO(
    id = this[EmployeesTable.id].value,
    name = this[name],
    surname = this[surname],
    email = this[email],
    position = this[position],
    supervisorId = this[supervisor]?.value,
)
