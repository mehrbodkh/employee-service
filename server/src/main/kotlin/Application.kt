package com.mehrbod

import com.mehrbod.di.configureDI
import io.ktor.server.application.*
import org.h2.tools.Server

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start()
    configureDI()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureRouting()
}
