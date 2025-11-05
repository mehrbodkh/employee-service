package com.mehrbod.data.datasource.employee

import com.mehrbod.client.RedisClientWrapper
import com.mehrbod.model.EmployeeNodeDTO
import java.util.*

interface EmployeeCacheDataSource {
    suspend fun setSubordinates(managerId: UUID, depth: Int, data: List<EmployeeNodeDTO>)
    suspend fun getSubordinates(managerId: UUID, depth: Int): List<EmployeeNodeDTO>?
    suspend fun setSupervisors(managerId: UUID, depth: Int, data: List<EmployeeNodeDTO>)
    suspend fun getSupervisors(employeeId: UUID, depth: Int): List<EmployeeNodeDTO>?
    suspend fun setRootSubordinates(depth: Int, data: List<List<EmployeeNodeDTO>>)
    suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>>?
}

class EmployeeCacheDataSourceImpl(
    private val redis: RedisClientWrapper
) : EmployeeCacheDataSource {

    private fun rootCacheKey(depth: Int) = "employee:root:$depth"
    private fun subCacheKey(id: UUID, depth: Int) = "employee:subordinate:$id:$depth"
    private fun superCacheKey(id: UUID, depth: Int) = "employee:supervisor:$id:$depth"

    override suspend fun setSubordinates(
        managerId: UUID,
        depth: Int,
        data: List<EmployeeNodeDTO>
    ) {
        redis.set(subCacheKey(managerId, depth), data)
    }

    override suspend fun getSubordinates(
        managerId: UUID,
        depth: Int
    ): List<EmployeeNodeDTO>? {
        return redis.get(subCacheKey(managerId, depth))
    }

    override suspend fun setSupervisors(
        managerId: UUID,
        depth: Int,
        data: List<EmployeeNodeDTO>
    ) {
        redis.set(superCacheKey(managerId, depth), data)
    }

    override suspend fun getSupervisors(
        employeeId: UUID,
        depth: Int
    ): List<EmployeeNodeDTO>? {
        return redis.get(superCacheKey(employeeId, depth))
    }

    override suspend fun setRootSubordinates(depth: Int, data: List<List<EmployeeNodeDTO>>) {
        redis.set(rootCacheKey(depth), data)
    }

    override suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>>? {
        return redis.get(rootCacheKey(depth))
    }
}
