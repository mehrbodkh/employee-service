package com.mehrbod.data.datasource

import com.mehrbod.data.table.EmployeeHierarchyTable
import com.mehrbod.data.table.EmployeesTable
import com.mehrbod.data.table.convertToEmployeeDTO
import com.mehrbod.model.EmployeeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityIDFunctionProvider
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

class DatabaseEmployeeDataSource(
    private val db: R2dbcDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EmployeeDataSource {
    override suspend fun createEmployee(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val id = EmployeesTable.insertAndGetId {
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
            }.value

            EmployeeHierarchyTable.insert {
                it[ancestor] = id
                it[descendant] = id
                it[distance] = 0
            }

            if (employee.supervisorId != null) {
                val ancestors = EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq UUID.fromString(employee.supervisorId) }
                    .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }

                ancestors.collect { (ancId, dist) ->
                    EmployeeHierarchyTable.insert {
                        it[ancestor] = ancId
                        it[descendant] = id
                        it[distance] = dist + 1
                    }
                }
            }

            EmployeeDTO(
                id.toString(),
                employee.name,
                employee.surname,
                employee.email,
                employee.position,
                employee.supervisorId
            )
        }
    }

    override suspend fun updateEmployee(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        suspendTransaction(db) {
            val id = UUID.fromString(employee.id)
            EmployeesTable.update(
                where = { EmployeesTable.id eq id }
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

            if (employee.supervisorId != null) {
                val ancestors = EmployeeHierarchyTable.selectAll()
                    .where { EmployeeHierarchyTable.descendant eq UUID.fromString(employee.supervisorId) }
                    .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }

                ancestors.collect { (ancId, dist) ->
                    EmployeeHierarchyTable.insert {
                        it[ancestor] = ancId
                        it[descendant] = id
                        it[distance] = dist + 1
                    }
                }
            }

            EmployeeDTO(
                id.toString(),
                employee.name,
                employee.surname,
                employee.email,
                employee.position,
                employee.supervisorId
            )
        }
    }

    override suspend fun deleteEmployee(id: UUID) {
        withContext(ioDispatcher) {
            suspendTransaction(db) {
                val supervisorId = EmployeesTable.selectAll()
                    .where { EmployeesTable.id eq id }
                    .map { it.convertToEmployeeDTO() }
                    .singleOrNull()?.supervisorId?.let {
                        UUID.fromString(it)
                    }

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
                        val newAncestors = EmployeeHierarchyTable
                            .selectAll()
                            .where { EmployeeHierarchyTable.descendant eq supervisorId }
                            .map { it[EmployeeHierarchyTable.ancestor] to it[EmployeeHierarchyTable.distance] }
                            .toList()

                        for ((ancestor, depth) in newAncestors) {
                            descendantSubtree.forEach { descendant ->
                                val subDepth = EmployeeHierarchyTable
                                    .selectAll()
                                    .where { (EmployeeHierarchyTable.ancestor eq subId) and (EmployeeHierarchyTable.descendant eq descendant) }
                                    .single()[EmployeeHierarchyTable.distance]

                                try {
                                    EmployeeHierarchyTable.insert {
                                        it[EmployeeHierarchyTable.ancestor] = ancestor
                                        it[EmployeeHierarchyTable.descendant] = descendant
                                        it[distance] = depth + subDepth + 1
                                    }
                                } catch (_: Exception) {
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

    override suspend fun getSubordinates(managerId: String): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            (EmployeeHierarchyTable.join(
                EmployeesTable,
                JoinType.INNER,
                EmployeesTable.id,
                otherColumn = EmployeeHierarchyTable.descendant
            ))
                .selectAll()
                .where { (EmployeeHierarchyTable.ancestor eq UUID.fromString(managerId)) and (EmployeeHierarchyTable.distance greater 0) }
                .map { it.convertToEmployeeDTO() }
                .toList()
        }
    }


    override suspend fun getSupervisors(employeeId: String): List<EmployeeDTO> = withContext(ioDispatcher) {
        suspendTransaction(db) {
            (EmployeeHierarchyTable.join(
                EmployeesTable,
                JoinType.INNER,
                EmployeesTable.id,
                otherColumn = EmployeeHierarchyTable.ancestor
            ))
                .selectAll()
                .where { (EmployeeHierarchyTable.descendant eq UUID.fromString(employeeId)) and (EmployeeHierarchyTable.distance greater 0) }
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
