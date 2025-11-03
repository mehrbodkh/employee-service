package com.mehrbod.data.table

import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object PerformanceReviewsTable : UUIDTable("performance_reviews") {
    val performance = integer("performance").check { it.between(1, 10) }
    val softSkills = integer("soft_skills").check { it.between(1, 10) }
    val independence = integer("independence").check { it.between(1, 10) }
    val aspiration = integer("aspiration").check { it.between(1, 10) }
}