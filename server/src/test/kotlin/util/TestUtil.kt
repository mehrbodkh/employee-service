package com.mehrbod.util

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.coroutines.EmptyCoroutineContext

suspend fun measureRPS(requestCount: Int, block: suspend () -> Unit) {
    val startTime = System.currentTimeMillis()
    block()
    val endTime = System.currentTimeMillis()
    val totalTimeInSeconds = (endTime - startTime) / 1000.0
    println("RPS: ${requestCount / totalTimeInSeconds}")
}

fun initializedTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication(
    EmptyCoroutineContext,
    {
        initializeApplication()
        block()
    }
)

private fun ApplicationTestBuilder.initializeApplication() {
    environment {
        config = ApplicationConfig("application-test.yaml")
    }
    client = createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }
}

