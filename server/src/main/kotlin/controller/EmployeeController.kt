package com.mehrbod.controller

import com.mehrbod.model.EmployeeDTO
import com.mehrbod.service.EmployeeService
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class EmployeeController(
    private val employeeService: EmployeeService,
) : BaseController {

    override fun RequestValidationConfig.validator() {
        validate<EmployeeDTO> {
            if (it.name.isBlank() || it.surname.isBlank() || it.email.isBlank() || it.position.isBlank()) {
                return@validate ValidationResult.Invalid("Mandatory fields cannot be empty.")
            }
            try {
                it.supervisorId?.let { supervisorId -> UUID.fromString(supervisorId) }
            } catch (_: Exception) {
                return@validate ValidationResult.Invalid("Invalid supervisorId: ${it.supervisorId}")
            }
            ValidationResult.Valid
        }
    }

    override fun Route.routes() = route("/employees") {

        post {
            val request = call.receive<EmployeeDTO>()
            val response = employeeService.createEmployee(request)
            call.respond(HttpStatusCode.Created, response)
        }

        put("{id}") {
            val request = call.receive<EmployeeDTO>()
            val id = call.parameters["id"]
            val response = employeeService.updateEmployee(request.copy(id = id))
            call.respond(HttpStatusCode.OK, response)
        }

        delete("{id}") {
            val id = call.parameters["id"]
            val response = employeeService.deleteEmployee(UUID.fromString(id))
            call.respond(HttpStatusCode.OK, response)
        }

        get("{id}") {
            val id = call.parameters["id"] ?: ""
            val response = employeeService.getEmployee(UUID.fromString(id))
            call.respond(response)
        }

        get("/{id}/subtree") {
            val id = call.parameters["id"] ?: return@get call.respond(mapOf("error" to "bad id"))
            val response = employeeService.getEmployeeSubordinates(UUID.fromString(id))
            call.respond(response)
        }

        get("/fetch-all") {
            val response = employeeService.getAllEmployees()
            call.respond(response)
        }
    }

}
