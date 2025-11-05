package com.mehrbod.anothermicroservice.notification

class PushNotificationSender : NotificationSender {
    override suspend fun send(notification: Notification) {
        println("Sending push $notification with retry")
    }
}