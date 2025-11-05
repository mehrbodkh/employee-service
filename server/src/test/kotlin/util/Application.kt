package com.mehrbod.util

import com.mehrbod.anothermicroservice.configureAnotherMicroservice
import com.mehrbod.common.configureSerialization
import com.mehrbod.configureHTTP
import com.mehrbod.configureMonitoring
import com.mehrbod.di.configureDI
import com.mehrbod.exception.configureGlobalExceptionHandling
import com.mehrbod.module.configureH2
import com.mehrbod.module.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("UNUSED_PARAMETER")
fun Application.module() {
    configureDI()
    configureHTTP()
    configureSerialization()
    configureH2()
    configureMonitoring()
    configureRouting()
    configureGlobalExceptionHandling()
    configureAnotherMicroservice()
}
