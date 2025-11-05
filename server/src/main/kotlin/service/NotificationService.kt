package com.mehrbod.service

import com.mehrbod.model.ReviewDTO
import com.mehrbod.event.EventProducer
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent
import java.util.UUID

class NotificationService(
    private val eventProducer: EventProducer
) {
    suspend fun sendSubmitReviewNotification(id: UUID, review: ReviewDTO) {
        eventProducer.sendEvent(ReviewSubmittedEvent(employeeID = id, review = review))
    }

    suspend fun sendManagerChangedNotification(employeeId: UUID, newManagerId: UUID) {
        eventProducer.sendEvent(ManagerChangedEvent(employeeID = employeeId, managerID = newManagerId))
    }
}
