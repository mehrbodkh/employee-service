package com.mehrbod.controller

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.util.UUID

fun Route.employeeController() = route("/employees") {

    post {
        val request = call.receive<CreateEmployeeRequest>()
        val employeeRepository by closestDI().instance<EmployeeRepository>()
        val response = employeeRepository.createEmployee(request)
        call.respond(HttpStatusCode.Created, response.toString())
    }

    get("{id}") {
        val id = call.parameters["id"] ?: ""
        val employeeRepository by closestDI().instance<EmployeeRepository>()
        val response = employeeRepository.getById(UUID.fromString(id))
        response?.let {
            call.respond(response)
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "Employee not found")
        }
    }

    get("/{id}/subtree") {
        val id = call.parameters["id"] ?: return@get call.respond(mapOf("error" to "bad id"))
        val depth = call.request.queryParameters["depth"]?.toIntOrNull()
        val employeeRepository by closestDI().instance<EmployeeRepository>()
        val nodes = employeeRepository.getSubordinates(id)
        call.respond(nodes)
    }

    get("/fetch-all") {
        val x: EmployeeRepository by closestDI().instance()
        call.respond<List<EmployeeDTO>>(x.fetchAllEmployees())
    }
}
