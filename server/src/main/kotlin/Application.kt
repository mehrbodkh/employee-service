package com.mehrbod

import com.mehrbod.anothermicroservice.configureAnotherMicroservice
import com.mehrbod.common.configureSerialization
import com.mehrbod.di.configureDI
import com.mehrbod.exception.configureGlobalExceptionHandling
import com.mehrbod.module.configureH2
import com.mehrbod.module.configureKafka
import com.mehrbod.module.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("UNUSED_PARAMETER")
fun Application.module() {
    configureDI()
    configureHTTP()
    configureSerialization()
    configureH2()
    configureKafka()
    configureMonitoring()
    configureRouting()
    configureGlobalExceptionHandling()
    configureAnotherMicroservice()
}
