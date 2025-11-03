package com.mehrbod.common

import com.mehrbod.exception.InvalidIdException
import java.util.UUID

fun String?.getUuidOrThrow(): UUID = try {
    UUID.fromString(this)
} catch (_: Exception) {
    throw InvalidIdException()
}

fun String?.getCoercedDepth(range: IntRange = 1..10) = (this?.toInt() ?: 1).coerceIn(range)