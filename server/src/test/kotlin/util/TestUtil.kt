package com.mehrbod.util

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.initApplication() {
    environment {
        config = ApplicationConfig("application.yaml")
    }
    client = createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }
}