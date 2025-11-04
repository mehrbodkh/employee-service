package com.mehrbod.data.datasource.review

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.model.Page
import com.mehrbod.model.ReviewDTO
import java.util.UUID

interface PerformanceReviewDataSource {
    suspend fun submitReview(id: UUID, review: SubmitReviewRequest)
    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int): Pair<Long, List<ReviewDTO>>
}