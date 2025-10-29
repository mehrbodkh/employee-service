package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

abstract class ServerException(val statusCode: HttpStatusCode, val errorMessage: String) :
    RuntimeException(errorMessage)