package com.mehrbod

import com.mehrbod.controller.BaseController
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.configureRouting() {
    install(Resources)
    install(AutoHeadResponse)
    val controllers by closestDI().instance<List<BaseController>>()
    install(RequestValidation) {
        controllers.forEach { controller ->
            with(controller) {
                validator()
            }
        }
    }
    routing {
        route("/api/v1") {
            controllers.forEach { controller ->
                with(controller) {
                    routes()
                }
            }
        }
    }
}
