package com.mehrbod.exception

import io.ktor.http.HttpStatusCode
import java.util.UUID

data class EmployeeNotFoundException(val id: UUID) :
    ServerException(HttpStatusCode.NotFound, "Employee with id $id not found")