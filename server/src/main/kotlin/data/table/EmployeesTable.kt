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
import org.jetbrains.exposed.v1.core.dao.id.EntityIDFunctionProvider
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

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

fun ResultRow.convertToEmployeeDTO() = EmployeeDTO(
    id = this[EmployeesTable.id].value.toString(),
    name = this[name],
    surname = this[surname],
    email = this[email],
    position = this[position],
    supervisorId = this[supervisor]?.value?.toString(),
    subordinatesCount = 0,
)

fun ResultRow.convertToEmployeeNodeDTO() = EmployeeNodeDTO(
    id = this[EmployeesTable.id].value.toString(),
    name = this[name],
    surname = this[surname],
    email = this[email],
    position = this[position],
    supervisorId = this[supervisor]?.value?.toString(),
)

/**
 * Due to lack of support for R2DBC on exposed DAO, some basic DAO like functions was needed
 */
suspend fun EmployeesTable.insertAndGet(employee: EmployeeDTO) = insertAndGetId {
    it[name] = employee.name
    it[surname] = employee.surname
    it[email] = employee.email
    it[position] = employee.position
    it[supervisor] = employee.supervisorId?.let {
        EntityIDFunctionProvider.createEntityID(
            UUID.fromString(employee.supervisorId),
            EmployeesTable
        )
    }
}.let {
    employee.copy(id = it.value.toString())
}

suspend fun EmployeesTable.update(employee: EmployeeDTO) = update(
    where = { EmployeesTable.id eq UUID.fromString(employee.id) },
) {
    it[name] = employee.name
    it[surname] = employee.surname
    it[email] = employee.email
    it[position] = employee.position
    it[supervisor] = employee.supervisorId?.let {
        EntityIDFunctionProvider.createEntityID(
            UUID.fromString(employee.supervisorId),
            EmployeesTable
        )
    }
}
