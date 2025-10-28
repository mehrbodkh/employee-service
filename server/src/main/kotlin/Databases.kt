package com.mehrbod

import com.mehrbod.common.Environment
import com.mehrbod.data.repository.EmployeeRepository
import com.mehrbod.model.EmployeeDTO
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
//    val dbConnection: Connection = connectToPostgres(embedded = true)
//    val cityService = CityService(dbConnection)
    
//    routing {
//
//        // Create city
//        post("/cities") {
//            val city = call.receive<City>()
//            val id = cityService.create(city)
//            call.respond(HttpStatusCode.Created, id)
//        }
//
//        // Read city
//        get("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            try {
//                val city = cityService.read(id)
//                call.respond(HttpStatusCode.OK, city)
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.NotFound)
//            }
//        }
//
//        // Update city
//        put("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = call.receive<City>()
//            cityService.update(id, user)
//            call.respond(HttpStatusCode.OK)
//        }
//
//        // Delete city
//        delete("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            cityService.delete(id)
//            call.respond(HttpStatusCode.OK)
//        }
//    }
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

    routing {
        get("/fetch-all") {
            val x: EmployeeRepository by closestDI().instance()
            call.respond<List<EmployeeDTO>>(x.fetchAllEmployees())
        }
    }
}
