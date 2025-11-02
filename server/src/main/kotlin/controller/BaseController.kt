package com.mehrbod.controller

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.routing.Route

interface BaseController {
    fun RequestValidationConfig.validator() {}
    fun Route.routes(): Route
}
