package com.mehrbod.data.di

import com.mehrbod.common.Environment
import com.mehrbod.data.datasource.DatabaseEmployeeDataSource
import com.mehrbod.data.datasource.EmployeeDataSource
import com.mehrbod.data.datasource.review.DatabaseReviewDataSource
import com.mehrbod.data.datasource.review.PerformanceReviewDataSource
import com.mehrbod.data.factory.createDbConnection
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.VendorDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val dataModule = DI.Module("dbModule") {
    bindSingleton<R2dbcDatabase> {
        createDbConnection(instance("dbConfig"), instance())
    }

    bindSingleton("dbConfig") {
        val app: Application = instance()
        when (instance<Environment>()) {
            Environment.PROD -> app.environment.config.config("database.postgres")
            Environment.DEV -> app.environment.config.config("database.h2")
        }
    }

    bindSingleton<VendorDialect> {
        when (instance<Environment>()) {
            Environment.PROD -> PostgreSQLDialect()
            Environment.DEV -> H2Dialect()
        }
    }

    bindSingleton<EmployeeDataSource>("database") {
        DatabaseEmployeeDataSource(instance(), instance("io"), instance("default"))
    }

    bindSingleton<PerformanceReviewDataSource>("database") { DatabaseReviewDataSource(instance(), instance("io")) }
}
