package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

data class EmployeeEmailNotFoundException(val email: String) :
    ServerException(HttpStatusCode.Companion.NotFound, "Employee with email $email not found")