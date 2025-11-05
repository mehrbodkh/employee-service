package com.mehrbod.notification.model

import com.mehrbod.common.UUIDSerializer
import com.mehrbod.model.ReviewDTO
import com.sksamuel.avro4k.AvroNamespace
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface Event {
    val time: LocalDateTime
}

@Serializable
@AvroNamespace("com.mehrbod.employee-service")
data class ReviewSubmittedEvent @OptIn(ExperimentalTime::class) constructor(
    override val time: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
    @Serializable(with = UUIDSerializer::class)
    val employeeID: UUID,
    val review: ReviewDTO,
) : Event

@Serializable
@AvroNamespace("com.mehrbod.employee-service")
data class ManagerChangedEvent @OptIn(ExperimentalTime::class) constructor(
    override val time: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
    @Serializable(with = UUIDSerializer::class)
    val employeeID: UUID,
    @Serializable(with = UUIDSerializer::class)
    val managerID: UUID,
) : Event

fun Event.getTopic() = when (this) {
    is ReviewSubmittedEvent -> "review"
    is ManagerChangedEvent -> "manager"
}
