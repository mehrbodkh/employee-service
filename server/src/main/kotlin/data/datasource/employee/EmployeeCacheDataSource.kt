package com.mehrbod.data.datasource.employee

import com.mehrbod.client.RedisClientWrapper
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.EmployeeNodeDTO
import java.util.UUID

class EmployeeCacheDataSource(
    private val redis: RedisClientWrapper
) {

    private fun idCacheKey(id: UUID) = "employee:$id"
    private fun rootCacheKey(depth: Int) = "employee:root:$depth"
    private fun subCountCacheKey(id: UUID) = "employee:subcount:$id"
    private fun subCacheKey(id: UUID, depth: Int) = "employee:sub:$id:$depth"
    private fun superCacheKey(id: UUID, depth: Int) = "employee:super:$id:$depth"
    private fun emailCacheKey(email: String) = "employee:email:$email"

    suspend fun save(employee: EmployeeDTO): EmployeeDTO {
        employee.id?.let {
            deleteCache(it, employee.email)
            redis.set(idCacheKey(employee.id), employee)
            redis.set(emailCacheKey(employee.email), employee)
        }
        return employee
    }

    suspend fun delete(id: UUID) {
        redis.delete(idCacheKey(id))
        deleteCache(id, null)
    }

    suspend fun setSubordinatesCount(managerId: UUID, count: Long) {
        redis.set(subCountCacheKey(managerId), count)
    }

    suspend fun getSubordinatesCount(managerId: UUID): Long? {
        return redis.get<Long>(subCountCacheKey(managerId))
    }

    suspend fun setSubordinates(
        managerId: UUID,
        depth: Int,
        data: List<EmployeeNodeDTO>
    ) {
        redis.set(subCacheKey(managerId, depth), data)
    }

    suspend fun getSubordinates(
        managerId: UUID,
        depth: Int
    ): List<EmployeeNodeDTO>? {
        return redis.get(subCacheKey(managerId, depth))
    }

    suspend fun setSupervisors(
        managerId: UUID,
        depth: Int,
        data: List<EmployeeNodeDTO>
    ) {
        redis.set(superCacheKey(managerId, depth), data)
    }

    suspend fun getSupervisors(
        employeeId: UUID,
        depth: Int
    ): List<EmployeeNodeDTO>? {
        return redis.get(superCacheKey(employeeId, depth))
    }

    suspend fun setRootSubordinates(depth: Int, data: List<List<EmployeeNodeDTO>>) {
        redis.set(rootCacheKey(depth), data)
    }

    suspend fun getRootSubordinates(depth: Int): List<List<EmployeeNodeDTO>>? {
        return redis.get(rootCacheKey(depth))
    }

    suspend fun getById(id: UUID): EmployeeDTO? {
        return redis.get<EmployeeDTO>(id.toString())
    }

    suspend fun setByEmail(email: String, employee: EmployeeDTO) {
        redis.set(emailCacheKey(email), employee)
    }

    suspend fun getByEmail(email: String): EmployeeDTO? {
        return redis.get(emailCacheKey(email))
    }

    private suspend fun deleteCache(id: UUID, email: String?) {
        redis.delete(idCacheKey(id))
        redis.delete(subCountCacheKey(id))
        email?.let { redis.delete(emailCacheKey(email)) }
    }
}
