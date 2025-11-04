package com.mehrbod.data.table

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

object EmployeeHierarchyTable : Table("employee_hierarchy") {
    val ancestor = uuid("ancestor").references(EmployeesTable.id)
    val descendant = uuid("descendant").references(EmployeesTable.id)
    val distance = integer("distance")
    override val primaryKey = PrimaryKey(ancestor, descendant, name = "pk_employee_hierarchy")

    init {
        runBlocking {
            suspendTransaction {
                SchemaUtils.create(EmployeeHierarchyTable)
                index(false, ancestor)
                index(false, descendant)
                index(false, ancestor, descendant)
            }
        }
    }
}

/**
 * Due to lack of support for R2DBC on exposed DAO, some basic DAO like functions was needed
 */
suspend fun EmployeeHierarchyTable.insert(ancestor: UUID, descendant: UUID, distance: Int) = insert {
    it[EmployeeHierarchyTable.ancestor] = ancestor
    it[EmployeeHierarchyTable.descendant] = descendant
    it[EmployeeHierarchyTable.distance] = distance
}
