package com.mehrbod.model

import com.mehrbod.data.dao.EmployeesTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow

@Serializable
data class Employee(
    val name: String,
    val surname: String,
    val email: String,
    val position: String,
)

fun ResultRow.convertToEmployee() = Employee(
    name = this[EmployeesTable.name],
    surname = this[EmployeesTable.surname],
    email = this[EmployeesTable.email],
    position = this[EmployeesTable.position],
)
