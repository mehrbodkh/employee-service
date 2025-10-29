package com.mehrbod.data.table

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object EmployeeHierarchyTable : Table("employee_hierarchy") {
    val ancestor = uuid("ancestor").references(EmployeesTable.id)
    val descendant = uuid("descendant").references(EmployeesTable.id)
    val distance = integer("distance")
    override val primaryKey = PrimaryKey(ancestor, descendant, name = "pk_employee_hierarchy")

    init {
        runBlocking {
            suspendTransaction {
                SchemaUtils.create(EmployeeHierarchyTable)
            }
        }
    }
}