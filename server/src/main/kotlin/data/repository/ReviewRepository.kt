package com.mehrbod.data.repository

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.datasource.DatabaseEmployeeDataSource
import java.util.UUID

class ReviewRepository(

) {

    suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {

    }
}