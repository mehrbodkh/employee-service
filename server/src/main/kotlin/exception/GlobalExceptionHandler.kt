package com.mehrbod.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureGlobalExceptionHandling() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            this@configureGlobalExceptionHandling.log.error(cause.message, cause)
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
        exception<ServerException> { call, cause ->
            this@configureGlobalExceptionHandling.log.error(cause.errorMessage, cause)
            call.respond(status = cause.statusCode, message = ServerErrorMessage(cause.errorMessage))
        }
        exception<RuntimeException> { call, cause ->
            this@configureGlobalExceptionHandling.log.error(cause.message, cause)
            call.respond(status = HttpStatusCode.InternalServerError, message = cause.localizedMessage)
        }
    }
}