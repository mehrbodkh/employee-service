package com.mehrbod.data.datasource

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.data.table.*
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityIDFunctionProvider
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

/**
 * `DatabaseEmployeeDataSource` is responsible for database interactions.
 * Database for employees include closure table to store organization hierarchy.
 */
class DatabaseEmployeeDataSource(
    private val db: R2dbcDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : EmployeeDataSource {

    override suspend fun save(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val insertedEmployee = EmployeesTable.insertAndGet(employee)
            val insertedId = insertedEmployee.id.getUuidOrThrow()

            EmployeeHierarchyTable.insert(insertedId, insertedId, 0)

            if (employee.supervisorId != null) {
                EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq employee.supervisorId.getUuidOrThrow() }
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
            val id = newEmployee.id.getUuidOrThrow()
            val currentEmployee = EmployeesTable.selectAll()
                .where { EmployeesTable.id eq id }
                .singleOrNull()
                ?.convertToEmployeeDTO()
                ?: throw EmployeeNotFoundException(id.toString())

            val currentSupervisorId = currentEmployee.supervisorId
            val newSupervisorId = newEmployee.supervisorId

            if (newSupervisorId != null && newSupervisorId != currentSupervisorId) {
                EmployeesTable.selectAll()
                    .where { EmployeesTable.id eq newSupervisorId.getUuidOrThrow() }
                    .singleOrNull() ?: throw EmployeeNotFoundException(newSupervisorId)

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
                    .where { EmployeeHierarchyTable.descendant eq newSupervisorId.getUuidOrThrow() }
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
                    .onEmpty {
                        throw EmployeeNotFoundException(id.toString())
                    }
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

    override suspend fun getSubordinates(managerId: UUID, depth: Int): List<EmployeeNodeDTO> =
        withContext(ioDispatcher) {
            suspendTransaction(db) {
                (EmployeeHierarchyTable.join(
                    EmployeesTable,
                    JoinType.INNER,
                    EmployeesTable.id,
                    otherColumn = EmployeeHierarchyTable.descendant
                ))
                    .selectAll()
                    .where {
                        EmployeeHierarchyTable.ancestor eq managerId and
                                EmployeeHierarchyTable.distance.between(1, depth)
                    }
                    .flowOn(ioDispatcher)
                    .map { it.convertToEmployeeNodeDTO() }
                    .flowOn(defaultDispatcher)
                    .toList()
            }
        }

    override suspend fun getRootSubordinates(depth: Int): List<EmployeeNodeDTO> {
        return suspendTransaction(db) {
            val rootEmployees = EmployeesTable.selectAll()
                .where { EmployeesTable.supervisor.isNull() }

            rootEmployees.map {
                val id = it[EmployeesTable.id].value
                getSubordinates(id, depth)
            }.toList().flatten()
        }
    }


    override suspend fun getSupervisors(employeeId: UUID, depth: Int): List<EmployeeNodeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            (EmployeeHierarchyTable.join(
                EmployeesTable,
                JoinType.INNER,
                EmployeesTable.id,
                otherColumn = EmployeeHierarchyTable.ancestor
            ))
                .selectAll()
                .where { (EmployeeHierarchyTable.descendant eq employeeId) and (EmployeeHierarchyTable.distance.between(1, depth)) }
                .flowOn(ioDispatcher)
                .map { it.convertToEmployeeNodeDTO() }
                .flowOn(defaultDispatcher)
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
