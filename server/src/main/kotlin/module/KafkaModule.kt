package com.mehrbod.module

import com.mehrbod.anothermicroservice.eventconsumer.EventReceiver
import com.mehrbod.notification.model.ManagerChangedEvent
import com.mehrbod.notification.model.ReviewSubmittedEvent
import io.github.flaxoos.ktor.server.plugins.kafka.*
import io.github.flaxoos.ktor.server.plugins.kafka.components.fromRecord
import io.ktor.server.application.*
import kotlinx.coroutines.launch

const val REVIEW_TOPIC_NAME = "review"
const val MANAGER_TOPIC_NAME = "manager"

fun Application.configureKafka() {
    val config = environment.config.config("kafka")

    val schemaRegistryUrlConfig = config.property("schema.registry.url").getString()
    val bootstrapServersConfig = config.property("common.bootstrap.servers").getString()

    installKafka {
        val reviewEvents = TopicName.named(REVIEW_TOPIC_NAME)
        val managerEvents = TopicName.named(MANAGER_TOPIC_NAME)
        schemaRegistryUrl = schemaRegistryUrlConfig
        topic(reviewEvents) {
            partitions = 1
            replicas = 1
        }
        topic(managerEvents) {
            partitions = 1
            replicas = 1
        }
        producer {
            bootstrapServers = bootstrapServersConfig
            retries = 1
            clientId = "producer"
        }
        registerSchemas {
            ReviewSubmittedEvent::class at reviewEvents
            ManagerChangedEvent::class at managerEvents
        }
        consumer {
            maxPollRecords = 50
            bootstrapServers = bootstrapServersConfig
            groupId = "consumer-group"
            clientId = "consumer"
            maxPollIntervalMs = 2000
        }
        /**
         * This has been done due to the projects being close together.
         * In real life, this section also should be handled by Kafka clients themselves.
         */
        consumerConfig {
            consumerRecordHandler(reviewEvents) { record ->
                launch {
                    val event = fromRecord<ReviewSubmittedEvent>(record.value())
                    EventReceiver.eventReceived(event)
                }
            }

            consumerRecordHandler(managerEvents) { record ->
                launch {
                    val event = fromRecord<ManagerChangedEvent>(record.value())
                    EventReceiver.eventReceived(event)
                }
            }
        }
    }
}
