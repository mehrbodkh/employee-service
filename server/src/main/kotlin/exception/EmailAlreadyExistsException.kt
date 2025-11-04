package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

class EmailAlreadyExistsException : ServerException(HttpStatusCode.Companion.BadRequest, "Email already exists")