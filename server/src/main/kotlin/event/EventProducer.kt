package com.mehrbod.notification


import com.mehrbod.notification.model.Event

interface EventProducer {
    suspend fun sendEvent(event: Event)
}