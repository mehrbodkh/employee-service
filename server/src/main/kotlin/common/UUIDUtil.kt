package com.mehrbod.common

import com.mehrbod.exception.InvalidIdException
import java.util.UUID

fun String?.getUuidOrThrow(): UUID = try {
    UUID.fromString(this)
} catch (_: Exception) {
    throw InvalidIdException()
}

fun String?.getUuidOrNull(): UUID? = try {
    UUID.fromString(this)
} catch (_: Exception) {
    null
}