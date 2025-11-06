package com.mehrbod.module

import io.ktor.events.EventDefinition
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * For monitoring, the provided solution is enough for logging and some basic graph plotting
 * for next steps, using tools such as opentelemetery can help improve and send more data and different metrics
 * to our graphing solutions. That, for simplicity and size of the project, has been omitted from here.
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(ApplicationMonitoringPlugin)
}

val NotFoundEvent: EventDefinition<ApplicationCall> = EventDefinition()
val BadRequestEvent: EventDefinition<ApplicationCall> = EventDefinition()
val InternalServerErrorEvent: EventDefinition<ApplicationCall> = EventDefinition()
val ErrorEvent: EventDefinition<ApplicationCall> = EventDefinition()

val ApplicationMonitoringPlugin = createApplicationPlugin(name = "ApplicationMonitoringPlugin") {
    on(ResponseSent) { call ->
        when (call.response.status()) {
            HttpStatusCode.NotFound -> this@createApplicationPlugin.application.monitor.raise(NotFoundEvent, call)
            HttpStatusCode.BadRequest -> this@createApplicationPlugin.application.monitor.raise(BadRequestEvent, call)
            HttpStatusCode.InternalServerError -> this@createApplicationPlugin.application.monitor.raise(InternalServerErrorEvent, call)
            else -> this@createApplicationPlugin.application.monitor.raise(ErrorEvent, call)
        }
    }
}
