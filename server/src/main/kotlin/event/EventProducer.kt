package com.mehrbod.event


import com.mehrbod.notification.model.Event

interface EventProducer {
    suspend fun sendEvent(event: Event)
}