package com.mehrbod

import com.mehrbod.common.Environment
import io.ktor.server.application.*
import org.h2.tools.Server
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.configureDatabases() {
    val environment by closestDI().instance<Environment>()
    if (environment == Environment.DEV) {
        try {
            Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start()
        } catch (e: Exception) {
            log.error(e.message)
        }
    }
//    install(Kafka) {
//        schemaRegistryUrl = "my.schemaRegistryUrl"
//        val myTopic = TopicName.named("my-topic")
//        topic(myTopic) {
//            partitions = 1
//            replicas = 1
//            configs {
//                messageTimestampType = MessageTimestampType.CreateTime
//            }
//        }
//        common { // <-- Define common properties
//            bootstrapServers = listOf("my-kafka")
//            retries = 1
//            clientId = "my-client-id"
//        }
//        admin { } // <-- Creates an admin client
//        producer { // <-- Creates a producer
//            clientId = "my-client-id"
//        }
//        consumer { // <-- Creates a consumer
//            groupId = "my-group-id"
//            clientId = "my-client-id-override" //<-- Override common properties
//        }
//        consumerConfig {
//            consumerRecordHandler(myTopic) { record ->
//                // Do something with record
//            }
//        }
//        registerSchemas {
//            using { // <-- optionally provide a client, by default CIO is used
//                HttpClient()
//            }
//            // MyRecord::class at myTopic // <-- Will register schema upon startup
//        }
//    }
}
