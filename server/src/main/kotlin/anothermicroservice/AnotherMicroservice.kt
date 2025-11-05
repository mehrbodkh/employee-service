package com.mehrbod.anothermicroservice

import com.mehrbod.anothermicroservice.event.EventConsumer
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

/**
 * This module represents another microservice.
 * It has been implemented here in the same project for easier implementation
 */
fun Application.configureAnotherMicroservice() {
    val consumer by closestDI().instance<EventConsumer>()
    launch {
        consumer.start()
    }
}