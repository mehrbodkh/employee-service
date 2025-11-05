package com.mehrbod.service

import com.mehrbod.event.EventProducer
import com.mehrbod.model.EmployeeDTO
import com.mehrbod.model.ReviewDTO
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent

class NotificationService(
    private val eventProducer: EventProducer
) {
    suspend fun sendSubmitReviewNotification(employee: EmployeeDTO, review: ReviewDTO) {
        eventProducer.sendEvent(ReviewSubmittedEvent(employee= employee, review = review))
    }

    suspend fun sendManagerChangedNotification(employee: EmployeeDTO) {
        eventProducer.sendEvent(ManagerChangedEvent(employee = employee))
    }
}
