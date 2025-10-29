package com.mehrbod

import com.mehrbod.controller.BaseController
import com.mehrbod.controller.EmployeeController
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

val controllers = listOf<BaseController>(
    EmployeeController
)

fun Application.configureRouting() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
    install(Resources)
    install(AutoHeadResponse)
    install(RequestValidation) {
        controllers.forEach { controller ->
            controller.apply {
                validator()
            }
        }
    }
    routing {
        route("/api/v1") {
            controllers.forEach { controller ->
                controller.apply {
                    routes()
                }
            }
        }
    }
}
