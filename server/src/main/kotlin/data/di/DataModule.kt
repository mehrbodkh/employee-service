package com.mehrbod.data.di

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val dataModule = DI.Module("dbModule") {
    bind<Database>() with singleton {
        val app: Application = instance()
        val dbConfig = app.environment.config.config("database.postgres")

        val url = dbConfig.property("devUrl").getString()

        Database.connect(
            url = url,
            user = dbConfig.propertyOrNull("user")?.getString() ?: "",
            password = dbConfig.propertyOrNull("password")?.getString() ?: "",
            databaseConfig = DatabaseConfig {
                defaultMaxAttempts = 1
            }
        )
    }
}
