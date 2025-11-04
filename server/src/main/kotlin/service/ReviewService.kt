package com.mehrbod.service

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.data.repository.ReviewRepository
import com.mehrbod.exception.EmployeeNotFoundException
import com.mehrbod.model.ReviewDTO
import java.util.*

class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val employeeRepository: EmployeeRepository,
) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {
        employeeRepository.getById(id) ?: throw EmployeeNotFoundException(id)

        reviewRepository.submitReview(id, review)
    }

    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int): Pair<Long, List<ReviewDTO>> {
        return reviewRepository.fetchReviews(id, page, pageSize)
    }
}
