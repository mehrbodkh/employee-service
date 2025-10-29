package com.mehrbod

import com.mehrbod.common.configureSerialization
import com.mehrbod.di.configureDI
import com.mehrbod.exception.configureGlobalExceptionHandling
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("UNUSED_PARAMETER")
fun Application.module() {
    configureDI()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureRouting()
    configureGlobalExceptionHandling()
}
