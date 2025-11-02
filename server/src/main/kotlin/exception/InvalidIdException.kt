package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

class InvalidIdException : ServerException(HttpStatusCode.BadRequest, "Invalid input id")