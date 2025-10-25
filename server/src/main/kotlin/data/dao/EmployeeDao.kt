package com.mehrbod.data.dao

import com.mehrbod.model.Employee
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

object EmployeesTable: UUIDTable("employees") {
    val name = varchar("name", length = 50)
    val surname = varchar("surname", length = 50)
    val email = varchar("email", length = 150)
    val position = varchar("position", length = 150)
}

class EmployeeDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<EmployeeDao>(EmployeesTable)

    var name by EmployeesTable.name
    var surname by EmployeesTable.surname
    var email by EmployeesTable.email
    var position by EmployeesTable.position

    override fun toString(): String {
        return "$name, $surname: $email, $position"
    }

    fun convertToModel() = Employee(name, surname, email, position)
}