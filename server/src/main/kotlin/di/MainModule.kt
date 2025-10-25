package com.mehrbod.di

import io.ktor.server.application.*
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton


fun Application.configureDI() {
    di {
        bind<String> { singleton {  "Hello" } }
    }
}