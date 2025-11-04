package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.datasource.review.PerformanceReviewDataSource
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.coroutines.CoroutineContext

class ReviewRepository(
    private val reviewDataSource: PerformanceReviewDataSource,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {
        reviewDataSource.submitReview(id, review)
    }

    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int) = reviewDataSource.fetchReviews(id, page, pageSize)
}