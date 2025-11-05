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
    private val notificationService: NotificationService,
) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {
        val employee = employeeRepository.getById(id) ?: throw EmployeeNotFoundException(id)

        val result = reviewRepository.submitReview(id, review)

        notificationService.sendSubmitReviewNotification(employee, result)
    }

    suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int): Pair<Long, List<ReviewDTO>> {
        return reviewRepository.fetchReviews(id, page, pageSize)
    }
}
