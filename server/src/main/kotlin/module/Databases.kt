package com.mehrbod.module

import com.mehrbod.common.Environment
import io.ktor.server.application.*
import org.h2.tools.Server
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.configureH2() {
    val environment by closestDI().instance<Environment>()
    if (environment == Environment.DEV) {
        try {
            Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start()
        } catch (e: Exception) {
            log.error(e.message)
        }
    }
}
