package com.mehrbod.data.di

import com.mehrbod.common.Environment
import com.mehrbod.data.datasource.DatabaseDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.core.vendors.DatabaseDialect
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.VendorDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.Duration

val dataModule = DI.Module("dbModule") {
    bind<R2dbcDatabase>() with singleton {
        createDbConnection(instance("dbConfig"), instance())
    }

    bind<ApplicationConfig>("dbConfig") with singleton {
        val app: Application = instance()
        when (val environment: Environment = instance()) {
            Environment.PROD -> app.environment.config.config("database.postgres")
            Environment.DEV -> app.environment.config.config("database.h2")
        }
    }

    bind<VendorDialect>() with singleton {
        when (val environment: Environment = instance()) {
            Environment.PROD -> PostgreSQLDialect()
            Environment.DEV -> H2Dialect()
        }
    }

    bind<DatabaseDataSource>() with singleton {
        DatabaseDataSource(instance(), instance("io"))
    }
}

private fun createDbConnection(dbConfig: ApplicationConfig, databaseDialect: DatabaseDialect): R2dbcDatabase {
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
