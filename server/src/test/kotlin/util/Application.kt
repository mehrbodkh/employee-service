package com.mehrbod.util

import com.mehrbod.anothermicroservice.configureAnotherMicroservice
import com.mehrbod.anothermicroservice.di.anotherMicroServiceModule
import com.mehrbod.client.RedisClientWrapper
import com.mehrbod.common.Environment
import com.mehrbod.common.configureSerialization
import com.mehrbod.configureHTTP
import com.mehrbod.configureMonitoring
import com.mehrbod.controller.EmployeeControllerTest
import com.mehrbod.controller.di.controllerModule
import com.mehrbod.data.di.dataModule
import com.mehrbod.data.di.repositoryModule
import com.mehrbod.di.application.applicationModule
import com.mehrbod.di.configureDI
import com.mehrbod.event.EventProducer
import com.mehrbod.exception.configureGlobalExceptionHandling
import com.mehrbod.module.configureH2
import com.mehrbod.module.configureRouting
import com.mehrbod.notification.KafkaEventProducer
import com.mehrbod.service.di.serviceModule
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.lettuce.core.RedisClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.apache.kafka.clients.producer.KafkaProducer
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.testcontainers.containers.GenericContainer

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("UNUSED_PARAMETER")
fun Application.module() {
    configureTestDI()
    configureHTTP()
    configureSerialization()
    configureH2()
    configureMonitoring()
    configureRouting()
    configureGlobalExceptionHandling()
    configureAnotherMicroservice()
}

fun Application.configureTestDI() = di {
    import(testApplicationModule)
    import(dataModule)
    import(repositoryModule)
    import(serviceModule)
    import(controllerModule)
    import(anotherMicroServiceModule)
}

val testApplicationModule = DI.Module("applicationModule") {
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

    bindSingleton<KafkaProducer<*,*>> {
        val app: Application = instance()
        app.kafkaProducer!!
    }

    bindSingleton<EventProducer> { KafkaEventProducer(instance<Application>().kafkaProducer) }

    bindSingleton {
        val redisContainer = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
        redisContainer.start()
        RedisClientWrapper(RedisClient.create("redis://${redisContainer.host}:${redisContainer.getMappedPort(6379)}"))
    }
}
