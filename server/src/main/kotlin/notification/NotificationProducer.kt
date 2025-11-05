package com.mehrbod.notification


import com.mehrbod.notification.model.Event

interface NotificationProducer {
    suspend fun sendEvent(event: Event)
}