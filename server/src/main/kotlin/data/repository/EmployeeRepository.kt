package com.mehrbod.data.repository

import com.mehrbod.data.datasource.employee.EmployeeCacheDataSource
import com.mehrbod.data.datasource.employee.EmployeeDataSource
import com.mehrbod.exception.EmployeeCouldNotBeCreatedException
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EmployeeRepository(
    private val dbDataSource: EmployeeDataSource,
    /**
     * Using redis as cache on repository level, mostly due to the fact that it is being used as a read through
     * mechanism, which repository can be a suitable place for.
     * It also, for time saving purposes, is only being used for read heavy requests.
     */
    private val cacheDataSource: EmployeeCacheDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun createEmployee(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        try {
            dbDataSource.save(employee)
        } catch (_: Exception) {
            throw EmployeeCouldNotBeCreatedException()
        }
    }

    suspend fun updateEmployee(updatedEmployee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        dbDataSource.update(updatedEmployee)
    }

    suspend fun deleteEmployee(id: UUID) {
        dbDataSource.delete(id)
    }

    suspend fun getSubordinatesCount(id: UUID): Long = withContext(ioDispatcher) {
        dbDataSource.getSubordinatesCount(id)
    }

    suspend fun getSubordinates(id: UUID, depth: Int = 1): List<EmployeeNodeDTO> = withContext(ioDispatcher) {
        cacheDataSource.getSubordinates(id, depth) ?: run {
            dbDataSource.getSubordinates(id, depth).also {
                cacheDataSource.setSubordinates(id, depth, it)
            }
        }
    }

    suspend fun getSupervisors(id: UUID, depth: Int): List<EmployeeNodeDTO> = withContext(ioDispatcher) {
        cacheDataSource.getSupervisors(id, depth) ?: run {
            dbDataSource.getSupervisors(id, depth).also {
                cacheDataSource.setSupervisors(id, depth, it)
            }
        }
    }

    suspend fun getById(id: UUID): EmployeeDTO? = withContext(ioDispatcher) {
        dbDataSource.getById(id)
    }

    suspend fun getByEmail(email: String): EmployeeDTO? = withContext(ioDispatcher) {
        dbDataSource.getByEmail(email)
    }

    suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>> = withContext(ioDispatcher) {
        cacheDataSource.getRootSubordinates(depth) ?: run {
            dbDataSource.getRootSubordinates(depth).also {
                cacheDataSource.setRootSubordinates(depth, it)
            }
        }
    }

    suspend fun fetchAllEmployees(): List<EmployeeDTO> = withContext(ioDispatcher) {
        dbDataSource.fetchAllEmployees()
    }
}
