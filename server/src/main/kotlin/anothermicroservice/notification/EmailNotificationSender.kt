package com.mehrbod.anothermicroservice.notification

class EmailNotificationSender : NotificationSender {
    override suspend fun send(notification: Notification) {
        println("Sending email $notification with retry")
    }
}