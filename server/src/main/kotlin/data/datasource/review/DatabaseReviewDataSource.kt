package com.mehrbod.data.datasource.review

import com.mehrbod.controller.model.request.SubmitReviewRequest
import com.mehrbod.data.table.EmployeesTable
import com.mehrbod.data.table.PerformanceReviewsTable
import com.mehrbod.model.Page
import com.mehrbod.model.ReviewDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityIDFunctionProvider
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DatabaseReviewDataSource(
    private val db: R2dbcDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PerformanceReviewDataSource {

    override suspend fun submitReview(id: UUID, review: SubmitReviewRequest) {
        withContext(ioDispatcher) {
            suspendTransaction(db) {
                PerformanceReviewsTable.insert {
                    it[employee] = EntityIDFunctionProvider.createEntityID(id, EmployeesTable)
                    it[reviewDate] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    it[performance] = review.performance
                    it[softSkills] = review.softSkills
                    it[independence] = review.independence
                    it[aspiration] = review.aspiration
                }
            }
        }
    }

    override suspend fun fetchReviews(id: UUID, page: Int, pageSize: Int): Pair<Long, List<ReviewDTO>> =
        suspendTransaction(db) {
            val totalCount = PerformanceReviewsTable.selectAll()
                .where { PerformanceReviewsTable.employee eq id }
                .count()
            val reviews = PerformanceReviewsTable
                .selectAll()
                .where { PerformanceReviewsTable.employee eq id }
                .orderBy(PerformanceReviewsTable.reviewDate, SortOrder.DESC)
                .limit(pageSize).offset(((page - 1) * pageSize).toLong())
                .map {
                    ReviewDTO(
                        it[PerformanceReviewsTable.reviewDate],
                        it[PerformanceReviewsTable.performance],
                        it[PerformanceReviewsTable.softSkills],
                        it[PerformanceReviewsTable.independence],
                        it[PerformanceReviewsTable.aspiration],
                    )
                }
                .flowOn(ioDispatcher)
                .toList()

            totalCount to reviews
        }
}
