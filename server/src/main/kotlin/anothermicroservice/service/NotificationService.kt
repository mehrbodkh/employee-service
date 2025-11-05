package com.mehrbod.anothermicroservice.service

import com.mehrbod.anothermicroservice.notification.Notification
import com.mehrbod.anothermicroservice.notification.NotificationSender
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent

class NotificationService(
    private val emailSender: NotificationSender
) {

    suspend fun sendManagerChangedNotification(event: ManagerChangedEvent) {
        emailSender.send(Notification(event.employee.email, event.employee.supervisorId.toString()))
    }

    suspend fun sendReviewSubmittedNotification(event: ReviewSubmittedEvent) {
        emailSender.send(Notification(event.employee.email, event.review.toString()))
    }

}