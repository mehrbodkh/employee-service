package com.mehrbod.controller

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.service.OrganizationService
import io.ktor.server.response.*
import io.ktor.server.routing.*

class OrganizationController(
    private val organizationService: OrganizationService,
) : BaseController {

    override fun Route.routes() = route("/org") {

        get("/root") {
            val depth = (call.queryParameters["depth"]?.toInt() ?: 1).coerceIn(1..10)
            val result = organizationService.getRootsSubordinates(depth)
            call.respond(result)
        }

        get("/{id}/supervisors") {
            val depth = (call.queryParameters["depth"]?.toInt() ?: 1).coerceIn(1..10)
            val id = call.parameters["id"].getUuidOrThrow()
            val result = organizationService.getSupervisors(id, depth)
            call.respond(result)
        }

        get("/{id}/hierarchy") {
            val depth = (call.queryParameters["depth"]?.toInt() ?: 1).coerceIn(1..10)
            val id = call.parameters["id"].getUuidOrThrow()
            val result = organizationService.getSubordinates(id, depth)
            call.respond(result)
        }
    }

}
