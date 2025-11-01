package com.mehrbod.exception

import io.ktor.http.HttpStatusCode

class OwnManagerException :
    ServerException(HttpStatusCode.BadRequest, "Employee cannot be their own manager")