package com.mehrbod.anothermicroservice.event

import com.mehrbod.anothermicroservice.eventconsumer.EventReceiver
import com.mehrbod.anothermicroservice.service.NotificationService
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch


class EventConsumer(
    private val notificationService: NotificationService
) {

    suspend fun start() {
        coroutineScope {
            launch {
                EventReceiver
                    .eventStream
                    .buffer()
                    .collect {
                        when (it) {
                            is ManagerChangedEvent -> notificationService.sendManagerChangedNotification(it)
                            is ReviewSubmittedEvent -> notificationService.sendReviewSubmittedNotification(it)
                        }
                    }
            }
        }
    }
}