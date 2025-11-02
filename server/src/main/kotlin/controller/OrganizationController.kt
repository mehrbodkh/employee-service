package com.mehrbod.controller

import com.mehrbod.service.OrganizationService
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class OrganizationController(
    private val organizationService: OrganizationService,
) : BaseController {

    override fun RequestValidationConfig.validator() {
    }

    override fun Route.routes() = route("/org") {

        get("/{id}/supervisors") {
            val depth = call.queryParameters["depth"]?.toInt() ?: 1
            val id = call.parameters["id"] ?: ""
            val result = organizationService.getSupervisors(UUID.fromString(id), depth)
            call.respond(result)
        }

        get("/{id}/hierarchy") {
            val depth = call.queryParameters["depth"]?.toInt() ?: 1
            val id = call.parameters["id"] ?: ""
            val result = organizationService.getSubordinates(UUID.fromString(id), depth)
            call.respond(result)
        }
    }

}
