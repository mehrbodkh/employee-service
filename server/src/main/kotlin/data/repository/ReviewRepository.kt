package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.datasource.review.CacheReviewDataSource
import com.mehrbod.data.datasource.review.PerformanceReviewDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

class ReviewRepository(
    private val reviewDataSource: PerformanceReviewDataSource,
    private val cacheReviewDataSource: CacheReviewDataSource,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) = withContext(ioDispatcher) {
        reviewDataSource.submitReview(id, review)
    }

    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int) = withContext(ioDispatcher) {
        cacheReviewDataSource.fetchReviews(id, page, page)?.let {
            println("From cache")
            return@let 1 to it
        }

        val result = reviewDataSource.fetchReviews(id, page, pageSize)

        cacheReviewDataSource.setReviews(id, page, pageSize, result.second)

        return@withContext result
    }
}