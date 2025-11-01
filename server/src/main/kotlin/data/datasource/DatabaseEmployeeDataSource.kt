package com.mehrbod.data.datasource

import com.mehrbod.data.table.*
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityIDFunctionProvider
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

/**
 * `DatabaseEmployeeDataSource` is responsible for database interactions.
 * Database for employees include closure table to store organization hierarchy.
 */
class DatabaseEmployeeDataSource(
    private val db: R2dbcDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EmployeeDataSource {

    override suspend fun save(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val insertedEmployee = EmployeesTable.insertAndGet(employee)
            val insertedId = UUID.fromString(insertedEmployee.id)

            EmployeeHierarchyTable.insert(insertedId, insertedId, 0)

            if (employee.supervisorId != null) {
                EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq UUID.fromString(employee.supervisorId) }
                    .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }
                    .toList()
                    .forEach { (ancId, dist) ->
                        EmployeeHierarchyTable.insert(ancId, insertedId, dist + 1)
                    }
            }

            insertedEmployee
        }
    }

    override suspend fun update(newEmployee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val id = UUID.fromString(newEmployee.id)
            val currentEmployee = EmployeesTable.selectAll()
                .where { EmployeesTable.id eq id }
                .singleOrNull()
                ?.convertToEmployeeDTO()

            currentEmployee ?: run {
                throw EmployeeNotFoundException(id.toString())
            }

            EmployeesTable.update(newEmployee)

            val currentSupervisorId = currentEmployee.supervisorId
            val newSupervisorId = newEmployee.supervisorId

            if (newSupervisorId != null && newSupervisorId != currentSupervisorId) {
                val subtree = EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.ancestor eq id }
                    .map { it[EmployeeHierarchyTable.descendant] }
                    .toList()

                val oldAncestors = EmployeeHierarchyTable.selectAll()
                    .where { (EmployeeHierarchyTable.descendant eq id) and (EmployeeHierarchyTable.ancestor neq id) }
                    .map { it[EmployeeHierarchyTable.ancestor] }
                    .toList()

                EmployeeHierarchyTable.deleteWhere {
                    (EmployeeHierarchyTable.ancestor inList oldAncestors) and (EmployeeHierarchyTable.descendant inList subtree)
                }

                EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq UUID.fromString(newSupervisorId) }
                    .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }
                    .toList()
                    .forEach { (ancestor, depth) ->
                        subtree.forEach { descendant ->
                            val subDepth = EmployeeHierarchyTable.selectAll()
                                .where { (EmployeeHierarchyTable.ancestor eq id) and (EmployeeHierarchyTable.descendant eq descendant) }
                                .single()[EmployeeHierarchyTable.distance]

                            try {
                                EmployeeHierarchyTable.insert(ancestor, descendant, depth + subDepth + 1)
                            } catch (_: Exception) {
                                /**
                                 * Empty catch body, due to lack of support for insertIgnore on H2 postgres mode
                                 */
                            }
                        }
                    }

                EmployeesTable.update(newEmployee)
            }

            newEmployee
        }
    }

    override suspend fun delete(id: UUID) {
        withContext(ioDispatcher) {
            suspendTransaction(db) {
                val supervisorId = EmployeesTable.selectAll()
                    .where { EmployeesTable.id eq id }
                    .map { it[EmployeesTable.supervisor] }
                    .singleOrNull()?.value

                val subordinates = EmployeesTable.selectAll()
                    .where { EmployeesTable.supervisor eq id }
                    .map { it[EmployeesTable.id].value }
                    .toList()

                subordinates.forEach { subId ->
                    EmployeesTable.update({ EmployeesTable.id eq subId }) {
                        it[supervisor] = supervisorId?.let { sid ->
                            EntityIDFunctionProvider.createEntityID(
                                sid,
                                EmployeesTable
                            )
                        }
                    }
                }

                subordinates.forEach { subId ->
                    val descendantSubtree = EmployeeHierarchyTable.selectAll()
                        .where { EmployeeHierarchyTable.ancestor eq subId }
                        .map { it[EmployeeHierarchyTable.descendant] }
                        .toList()

                    EmployeeHierarchyTable.deleteWhere {
                        (EmployeeHierarchyTable.ancestor eq id) and (EmployeeHierarchyTable.descendant inList descendantSubtree)
                    }

                    if (supervisorId != null) {
                        EmployeeHierarchyTable.selectAll()
                            .where { EmployeeHierarchyTable.descendant eq supervisorId }
                            .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }
                            .toList()
                            .forEach { (ancestor, depth) ->
                                descendantSubtree.forEach { descendant ->
                                    val subDepth = EmployeeHierarchyTable.selectAll()
                                        .where { (EmployeeHierarchyTable.ancestor eq subId) and (EmployeeHierarchyTable.descendant eq descendant) }
                                        .single()[EmployeeHierarchyTable.distance]

                                    try {
                                        EmployeeHierarchyTable.insert(ancestor, descendant, depth + subDepth + 1)
                                    } catch (_: Exception) {
                                        /**
                                         * Empty catch body, due to lack of support for insertIgnore on H2 postgres mode
                                         */
                                    }
                                }
                            }
                    }
                }

                EmployeeHierarchyTable.deleteWhere {
                    (EmployeeHierarchyTable.ancestor eq id) or (EmployeeHierarchyTable.descendant eq id)
                }

                EmployeesTable.deleteWhere { EmployeesTable.id eq id }
            }
        }
    }

    override suspend fun getSubordinates(managerId: UUID): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            (EmployeeHierarchyTable.join(
                EmployeesTable,
                JoinType.INNER,
                EmployeesTable.id,
                otherColumn = EmployeeHierarchyTable.descendant
            ))
                .selectAll()
                .where { (EmployeeHierarchyTable.ancestor eq managerId) and (EmployeeHierarchyTable.distance greater 0) }
                .map { it.convertToEmployeeDTO() }
                .toList()
        }
    }


    override suspend fun getSupervisors(employeeId: UUID): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            (EmployeeHierarchyTable.join(
                EmployeesTable,
                JoinType.INNER,
                EmployeesTable.id,
                otherColumn = EmployeeHierarchyTable.ancestor
            ))
                .selectAll()
                .where { (EmployeeHierarchyTable.descendant eq employeeId) and (EmployeeHierarchyTable.distance greater 0) }
                .map { it.convertToEmployeeDTO() }
                .toList()
        }
    }

    override suspend fun getById(id: UUID): EmployeeDTO? = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable
                .selectAll()
                .where { EmployeesTable.id eq id }
                .singleOrNull()
                ?.convertToEmployeeDTO()
        }
    }

    override suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable.selectAll().map { it.convertToEmployeeDTO() }.toList()
        }
    }
}
