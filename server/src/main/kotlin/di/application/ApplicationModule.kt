package com.mehrbod.di.application

import com.mehrbod.common.Environment
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton

val applicationModule = DI.Module("applicationModule") {
    bindSingleton {
        val app: Application = instance()
        val config = app.environment.config.propertyOrNull("ktor.environment")?.getString() ?: "DEV"

        Environment.valueOf(config)
    }

    bindSingleton<CoroutineDispatcher>("io") {
        Dispatchers.IO
    }

    bindSingleton<CoroutineDispatcher>("default") {
        Dispatchers.Default
    }
}