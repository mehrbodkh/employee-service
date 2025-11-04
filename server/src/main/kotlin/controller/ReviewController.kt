package com.mehrbod.controller

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.common.mapToPaginatedResponse
import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.service.ReviewService
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ReviewController(
    private val reviewService: ReviewService
) : BaseController {

    override fun RequestValidationConfig.validator() {
        validate<SubmitReviewRequest> {
            if (listOf(it.performance, it.softSkills, it.independence, it.aspiration).any { it !in 1..10 }) {
                ValidationResult.Invalid("Scores must be between 1 and 10")
            } else {
                ValidationResult.Valid
            }
        }
    }

    override fun Route.routes() = route("/review") {

        post("/{id}/submit") {
            val id = call.parameters["id"].getUuidOrThrow()
            val review = call.receive<SubmitReviewRequest>()
            reviewService.submitReview(id, review)
            call.respond(HttpStatusCode.Created)
        }

        get("{id}") {
            val id = call.parameters["id"].getUuidOrThrow()
            val page = (call.parameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
            val pageSize = (call.parameters["pageSize"]?.toIntOrNull() ?: 20).coerceIn(1..100)
            val response = reviewService.fetchReviews(id, page, pageSize)
            call.respond(response.mapToPaginatedResponse(page, pageSize))
        }
    }
}
