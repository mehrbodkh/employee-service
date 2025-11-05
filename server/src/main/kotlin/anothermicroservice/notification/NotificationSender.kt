package com.mehrbod.anothermicroservice.notification

interface NotificationSender {
    suspend fun send(notification: Notification)
}