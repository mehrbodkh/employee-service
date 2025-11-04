package com.mehrbod.data.table

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

object PerformanceReviewsTable : UUIDTable("performance_reviews") {
    val employee = reference("employee_id", EmployeesTable, onDelete = ReferenceOption.CASCADE)
    val reviewDate = datetime("review_date").defaultExpression(CurrentDateTime)

    val performance = integer("performance").check { it.between(1, 10) }
    val softSkills = integer("soft_skills").check { it.between(1, 10) }
    val independence = integer("independence").check { it.between(1, 10) }
    val aspiration = integer("aspiration").check { it.between(1, 10) }

    init {
        runBlocking {
            SchemaUtils.create(PerformanceReviewsTable)
        }
    }
}