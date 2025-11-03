package com.mehrbod.controller

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.service.ReviewService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

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
    }
}