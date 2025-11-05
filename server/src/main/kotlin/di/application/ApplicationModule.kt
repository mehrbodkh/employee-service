package com.mehrbod.di.application

import com.mehrbod.client.RedisClientWrapper
import com.mehrbod.common.Environment
import com.mehrbod.event.EventProducer
import com.mehrbod.notification.KafkaEventProducer
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import io.ktor.server.application.*
import io.lettuce.core.RedisClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.apache.kafka.clients.producer.KafkaProducer
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

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

    bindSingleton<KafkaProducer<*,*>> {
        val app: Application = instance()
        app.kafkaProducer!!
    }

    bindSingleton<EventProducer> { KafkaEventProducer(instance<Application>().kafkaProducer) }

    bindSingleton {
        val url = instance<Application>().environment.config.property("redis.url").getString()
        RedisClientWrapper(RedisClient.create(url))
    }
}
