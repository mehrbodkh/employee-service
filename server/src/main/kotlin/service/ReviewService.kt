package com.mehrbod.service

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.repository.ReviewRepository
import java.util.*

class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val employeeService: EmployeeService,
) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {
        employeeService.getEmployee(id)
        reviewRepository.submitReview(id, review)
    }

    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int) = reviewRepository.fetchReviews(id, page, pageSize)

}