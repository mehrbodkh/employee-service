package com.mehrbod.controller

import com.mehrbod.controller.model.request.CreateEmployeeRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.Employee
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.employeeController() = route("/employees") {
    get {
        call.respond("Hello")
    }

    post {
        val request = call.receive<CreateEmployeeRequest>()
        val employeeRepository by closestDI().instance<EmployeeRepository>()
        val response = employeeRepository.createEmployee(request)
        call.respond(HttpStatusCode.Created, response.toString())
    }

    get("{id}") {
        val id = call.parameters["id"] ?: ""
        val employeeRepository by closestDI().instance<EmployeeRepository>()
        val response = employeeRepository.getById(id)
        call.respond(response!!)
    }

    get("/fetch-all") {
        val x: EmployeeRepository by closestDI().instance()
        call.respond<List<Employee>>(x.fetchAllEmployees())
    }
}
