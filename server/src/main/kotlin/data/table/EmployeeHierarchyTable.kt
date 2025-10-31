package com.mehrbod.data.table

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

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

suspend fun EmployeeHierarchyTable.insert(ancestor: UUID, descendant: UUID, distance: Int) = insert {
    it[EmployeeHierarchyTable.ancestor] = ancestor
    it[EmployeeHierarchyTable.descendant] = descendant
    it[EmployeeHierarchyTable.distance] = distance
}