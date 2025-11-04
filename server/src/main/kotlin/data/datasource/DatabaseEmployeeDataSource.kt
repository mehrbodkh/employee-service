package com.mehrbod.data.datasource

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
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
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
            val insertedId = EmployeesTable.insertAndGetId {
                it[name] = employee.name
                it[surname] = employee.surname
                it[email] = employee.email
                it[position] = employee.position
                it[supervisor] = employee.supervisorId?.let {
                    EntityIDFunctionProvider.createEntityID(employee.supervisorId, EmployeesTable)
                }
            }.value

            EmployeeHierarchyTable.insert(insertedId, insertedId, 0)

            if (employee.supervisorId != null) {
                EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq employee.supervisorId }
                    .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }
                    .toList()
                    .forEach { (ancId, dist) ->
                        EmployeeHierarchyTable.insert(ancId, insertedId, dist + 1)
                    }
            }

            employee.copy(id = insertedId)
        }
    }

    override suspend fun update(newEmployee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val id = newEmployee.id ?: throw Exception("Employee id cannot be empty")
            val currentEmployee = EmployeesTable.selectAll()
                .where { EmployeesTable.id eq id }
                .singleOrNull()
                ?.convertToEmployeeDTO()
                ?: throw EmployeeNotFoundException(id)

            val currentSupervisorId = currentEmployee.supervisorId
            val newSupervisorId = newEmployee.supervisorId

            if (newSupervisorId != null && newSupervisorId != currentSupervisorId) {
                EmployeesTable.selectAll()
                    .where { EmployeesTable.id eq newSupervisorId }
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
                    .where { EmployeeHierarchyTable.descendant eq newSupervisorId }
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

                EmployeesTable.update(
                    where = { EmployeesTable.id eq newEmployee.id },
                ) {
                    it[EmployeesTable.name] = newEmployee.name
                    it[surname] = newEmployee.surname
                    it[email] = newEmployee.email
                    it[position] = newEmployee.position
                    it[supervisor] = EntityIDFunctionProvider.createEntityID(newEmployee.supervisorId, EmployeesTable)
                }
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
                        throw EmployeeNotFoundException(id)
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

    override suspend fun getSubordinatesCount(managerId: UUID): Long = suspendTransaction(db) {
        withContext(ioDispatcher) {
            EmployeeHierarchyTable
                .selectAll()
                .where { (EmployeeHierarchyTable.ancestor eq managerId) and (EmployeeHierarchyTable.distance greater 0) }
                .count()
        }
    }

    override suspend fun getSubordinates(managerId: UUID, depth: Int): List<EmployeeNodeDTO> = suspendTransaction(db) {
        (EmployeeHierarchyTable.join(
            EmployeesTable,
            JoinType.INNER,
            EmployeesTable.id,
            otherColumn = EmployeeHierarchyTable.descendant
        ))
            .selectAll()
            .where {
                EmployeeHierarchyTable.ancestor eq managerId and EmployeeHierarchyTable.distance.between(0, depth)
            }
            .flowOn(ioDispatcher)
            .map { it.convertToEmployeeNodeDTO() }
            .flowOn(defaultDispatcher)
            .toList()
    }

    override suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>> = suspendTransaction(db) {
        EmployeesTable.selectAll()
            .where { EmployeesTable.supervisor.isNull() }
            .map {
                val id = it[EmployeesTable.id].value
                getSubordinates(id, depth)
            }
            .flowOn(ioDispatcher)
            .toList()
    }


    override suspend fun getSupervisors(employeeId: UUID, depth: Int): List<EmployeeNodeDTO> =
        withContext(ioDispatcher) {
            suspendTransaction(db) {
                (EmployeeHierarchyTable.join(
                    EmployeesTable,
                    JoinType.INNER,
                    EmployeesTable.id,
                    otherColumn = EmployeeHierarchyTable.ancestor
                ))
                    .selectAll()
                    .where {
                        (EmployeeHierarchyTable.descendant eq employeeId) and (EmployeeHierarchyTable.distance.between(
                            0,
                            depth
                        ))
                    }
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

    override suspend fun getByEmail(email: String): EmployeeDTO? = suspendTransaction(db) {
        EmployeesTable
            .selectAll()
            .where { EmployeesTable.email eq email }
            .singleOrNull()
            ?.convertToEmployeeDTO()
    }

    override suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            EmployeesTable.selectAll().map { it.convertToEmployeeDTO() }.toList()
        }
    }
}
