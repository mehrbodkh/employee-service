package com.mehrbod.util

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.coroutines.EmptyCoroutineContext

fun initializedTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication(
    EmptyCoroutineContext,
    {
        initializeApplication()
        block()
    }
)


private fun ApplicationTestBuilder.initializeApplication() {
    environment {
        config = ApplicationConfig("application.yaml")
    }
    client = createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }
}