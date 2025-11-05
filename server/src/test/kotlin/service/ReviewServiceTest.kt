package com.mehrbod.service

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.data.repository.ReviewRepository
import com.mehrbod.exception.EmployeeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@ExtendWith(MockKExtension::class)
class ReviewServiceTest {

    @MockK
    private lateinit var reviewRepository: ReviewRepository

    @MockK
    private lateinit var employeeRepository: EmployeeRepository

    @RelaxedMockK
    private lateinit var notificationService: NotificationService

    @InjectMockKs
    private lateinit var service: ReviewService

    @Nested
    inner class Submit {

        @Test
        fun `should throw exception - employee doesn't exist`() = runTest {
            val id = UUID.randomUUID()

            coEvery { employeeRepository.getById(id) } returns null

            assertThrows<EmployeeNotFoundException> { service.submitReview(id, mockk()) }
            coVerify { employeeRepository.getById(id) }
            coVerify(inverse = true) { reviewRepository.submitReview(any(), any()) }
        }

        @Test
        fun `should submit review`() = runTest {
            val id = UUID.randomUUID()
            val review = mockk<SubmitReviewRequest>()

            coEvery { employeeRepository.getById(id) } returns mockk()
            coEvery { reviewRepository.submitReview(any(), any()) } returns mockk()

            service.submitReview(id, review)

            coVerify { employeeRepository.getById(id) }
            coVerify { reviewRepository.submitReview(any(), any()) }
            coVerify { notificationService.sendSubmitReviewNotification(any(), any()) }
        }
    }

    @Nested
    inner class Fetch {

        @ParameterizedTest
        @ValueSource(ints = [1, 10, 100])
        fun `should get paginated review list`(value: Int) = runTest {
            val id = UUID.randomUUID()
            coEvery { reviewRepository.fetchReviews(any(), any(), any()) } returns mockk()

            service.fetchReviews(id, value, value * 2)

            coVerify { reviewRepository.fetchReviews(id, value, value * 2) }
        }
    }
}
