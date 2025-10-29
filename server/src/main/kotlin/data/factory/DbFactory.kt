package com.mehrbod.data.factory

import io.ktor.server.config.ApplicationConfig
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.core.vendors.DatabaseDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import java.time.Duration


fun createDbConnection(dbConfig: ApplicationConfig, databaseDialect: DatabaseDialect): R2dbcDatabase {
    val connectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, dbConfig.property("driver").getString())
            .option(ConnectionFactoryOptions.PROTOCOL, dbConfig.property("protocol").getString())
            .option(ConnectionFactoryOptions.HOST, dbConfig.property("host").getString())
            .option(ConnectionFactoryOptions.USER, dbConfig.property("user").getString())
            .option(ConnectionFactoryOptions.PASSWORD, dbConfig.property("password").getString())
            .option(ConnectionFactoryOptions.DATABASE, dbConfig.property("database").getString())
            .build()
    )

    val config = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(Duration.ofMinutes(30))
        .initialSize(5)
        .maxSize(20)
        .build()

    return R2dbcDatabase.connect(
        connectionFactory = ConnectionPool(config),
        databaseConfig = R2dbcDatabaseConfig.Builder().apply {
            explicitDialect = databaseDialect
            defaultMaxAttempts = 1
            defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED
        }
    )
}