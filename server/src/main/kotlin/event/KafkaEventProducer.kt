package com.mehrbod.notification

import com.mehrbod.event.EventProducer
import com.mehrbod.notification.model.Event
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent
import com.mehrbod.notification.model.getTopic
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import io.github.flaxoos.ktor.server.plugins.kafka.components.toRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * This specific kafka, breaks down the events into two different sets of topics as demonstration on this project.
 * This works just fine for this small of a project, however, we can also send user change event (Generic one)
 * on one topic, and delegate the task of event type parsing to the consumers.
 */
class KafkaEventProducer(
    // This is marked as nullable due to some testing issues
    private val producer: KafkaProducer<KafkaRecordKey, GenericRecord>?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : EventProducer {

    override suspend fun sendEvent(event: Event) {
        withContext(ioDispatcher) {
            when (event) {
                is ManagerChangedEvent -> producer?.sendEvent(
                    ProducerRecord(
                        event.getTopic(),
                        event.time.toString(),
                        event.toRecord()
                    )
                )

                is ReviewSubmittedEvent -> producer?.sendEvent(
                    ProducerRecord(
                        event.getTopic(),
                        event.time.toString(),
                        event.toRecord()
                    )
                )
            }
        }
    }

}

private suspend fun <K, V> KafkaProducer<K, V>.sendEvent(record: ProducerRecord<K, V>) =
    suspendCancellableCoroutine { continuation ->
        send(record) { metadata, exception ->
            if (exception != null) {
                continuation.resumeWithException(exception)
            } else if (metadata != null) {
                continuation.resume(metadata)
            }
        }
    }
