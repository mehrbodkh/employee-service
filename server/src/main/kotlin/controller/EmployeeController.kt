package com.mehrbod.controller

import com.mehrbod.common.getUuidOrThrow
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.service.EmployeeService
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class EmployeeController(
    private val employeeService: EmployeeService,
) : BaseController {

    override fun RequestValidationConfig.validator() {
        validate<EmployeeDTO> {
            if (it.name.isBlank() || it.surname.isBlank() || it.email.isBlank() || it.position.isBlank()) {
                return@validate ValidationResult.Invalid("Mandatory fields cannot be empty.")
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
            val id = call.parameters["id"].getUuidOrThrow()
            val response = employeeService.updateEmployee(request.copy(id = id))
            call.respond(HttpStatusCode.OK, response)
        }

        delete("{id}") {
            val id = call.parameters["id"].getUuidOrThrow()
            val response = employeeService.deleteEmployee(id)
            call.respond(HttpStatusCode.OK, response)
        }

        get("{id}") {
            val id = call.parameters["id"].getUuidOrThrow()
            val response = employeeService.getEmployee(id)
            call.respond(response)
        }

        get("/fetch-all") {
            val response = employeeService.getAllEmployees()
            call.respond(response)
        }
    }

}
