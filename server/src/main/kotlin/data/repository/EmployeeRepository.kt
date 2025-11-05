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
    private val cacheDataSource: EmployeeCacheDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun createEmployee(employee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        try {
            dbDataSource.save(employee).also {
                cacheDataSource.save(it)
            }
        } catch (_: Exception) {
            throw EmployeeCouldNotBeCreatedException()
        }
    }

    suspend fun updateEmployee(updatedEmployee: EmployeeDTO): EmployeeDTO = withContext(ioDispatcher) {
        dbDataSource.update(updatedEmployee).also {
            cacheDataSource.save(it)
        }
    }

    suspend fun deleteEmployee(id: UUID) {
        dbDataSource.delete(id).also {
            cacheDataSource.delete(id)
        }
    }

    suspend fun getSubordinatesCount(id: UUID): Long = withContext(ioDispatcher) {
        cacheDataSource.getSubordinatesCount(id) ?: run {
            dbDataSource.getSubordinatesCount(id).also {
                cacheDataSource.setSubordinatesCount(id, it)
            }
        }
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
        cacheDataSource.getById(id) ?: run {
            dbDataSource.getById(id)?.also {
                cacheDataSource.save(it)
            }
        }
    }

    suspend fun getByEmail(email: String): EmployeeDTO? = withContext(ioDispatcher) {
        cacheDataSource.getByEmail(email) ?: run {
            dbDataSource.getByEmail(email)?.also {
                cacheDataSource.setByEmail(email, it)
            }
        }
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
