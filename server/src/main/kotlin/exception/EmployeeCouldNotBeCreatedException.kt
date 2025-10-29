package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

class EmployeeCouldNotBeCreatedException :
    ServerException(HttpStatusCode.InternalServerError, "Employee could not be created")