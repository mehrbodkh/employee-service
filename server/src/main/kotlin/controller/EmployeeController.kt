package com.mehrbod.controller

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeDTO
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.util.*

class EmployeeController(
    private val employeeRepository: EmployeeRepository
) : BaseController {
    override fun RequestValidationConfig.validator() {
        validate<CreateEmployeeRequest> {
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
            val request = call.receive<CreateEmployeeRequest>()
            val response = employeeRepository.createEmployee(request)
            call.respond(HttpStatusCode.Created, response.toString())
        }

        get("{id}") {
            val id = call.parameters["id"] ?: ""
            val response = employeeRepository.getById(UUID.fromString(id))
            response?.let {
                call.respond(response)
            } ?: run {
                call.respond(HttpStatusCode.NotFound, "Employee not found")
            }
        }

        get("/{id}/subtree") {
            val id = call.parameters["id"] ?: return@get call.respond(mapOf("error" to "bad id"))
            val nodes = employeeRepository.getSubordinates(id)
            call.respond(nodes)
        }

        get("/fetch-all") {
            val x: EmployeeRepository by closestDI().instance()
            call.respond<List<EmployeeDTO>>(x.fetchAllEmployees())
        }
    }

}
