package com.mehrbod.anothermicroservice.di

import com.mehrbod.anothermicroservice.event.EventConsumer
import com.mehrbod.anothermicroservice.notification.EmailNotificationSender
import com.mehrbod.anothermicroservice.notification.NotificationSender
import com.mehrbod.anothermicroservice.notification.PushNotificationSender
import com.mehrbod.anothermicroservice.service.NotificationService
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val anotherMicroServiceModule = DI.Module("AnotherMS") {
    bindSingleton { EventConsumer(instance()) }
    bindSingleton<NotificationSender>("email") { EmailNotificationSender() }
    bindSingleton<NotificationSender>("push") { PushNotificationSender() }
    bindSingleton { NotificationService(instance("email")) }
}