package com.mehrbod.service

import com.mehrbod.model.ReviewDTO
import com.mehrbod.event.EventProducer
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class NotificationServiceTest {

    @MockK
    private lateinit var eventProducer: EventProducer

    @InjectMockKs
    private lateinit var service: NotificationService

    @Test
    fun `should send submit review event`() = runTest {
        val id = UUID.randomUUID()
        val review = mockk<ReviewDTO>()
        val slot = CapturingSlot<ReviewSubmittedEvent>()
        coEvery { eventProducer.sendEvent(capture(slot)) } returns Unit

        service.sendSubmitReviewNotification(id, review)

        coVerify { eventProducer.sendEvent(any()) }
        assertEquals(review, slot.captured.review)
        assertEquals(id, slot.captured.employeeID)
    }

    @Test
    fun `should send manager change event`() = runTest {
        val id = UUID.randomUUID()
        val managerId = UUID.randomUUID()
        val slot = CapturingSlot<ManagerChangedEvent>()
        coEvery { eventProducer.sendEvent(capture(slot)) } returns Unit

        service.sendManagerChangedNotification(id, managerId)

        coVerify { eventProducer.sendEvent(any()) }
        assertEquals(managerId, slot.captured.managerID)
        assertEquals(id, slot.captured.employeeID)
    }
}
