package com.mehrbod.di.application

import com.mehrbod.common.Environment
import com.mehrbod.notification.NotificationProducer
import com.mehrbod.notification.KafkaNotificationProducer
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import io.ktor.server.application.*
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

    bindSingleton<NotificationProducer> { KafkaNotificationProducer(instance<Application>().kafkaProducer!!) }
}