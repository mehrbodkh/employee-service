package com.mehrbod.data.datasource.review

import com.mehrbod.client.RedisClientWrapper
import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.model.ReviewDTO
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import java.util.*

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class CacheReviewDataSource(
    private val redisClient: RedisClientWrapper
) {

    private fun cacheKey(employeeId: String, page: Int, pageSize: Int) = "employee_review:$employeeId:$page:$pageSize"

    suspend fun setReviews(
        id: UUID,
        page: Int,
        pageSize: Int,
        reviews: List<ReviewDTO>
    ) {
        redisClient.set(cacheKey(id.toString(), page, pageSize), reviews)
    }

    suspend fun fetchReviews(
        id: UUID,
        page: Int,
        pageSize: Int
    ): List<ReviewDTO>? {
        return redisClient.get<List<ReviewDTO>>(cacheKey(id.toString(), page, pageSize))
    }
}