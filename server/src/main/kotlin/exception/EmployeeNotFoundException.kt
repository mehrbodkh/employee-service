package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

data class EmployeeNotFoundException(val id: String) :
    ServerException(HttpStatusCode.NotFound, "Employee with id $id not found")