package com.mehrbod.client

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisClientWrapper(
    private val client: RedisClient,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val connection = client.connect().coroutines()
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend inline fun <reified T> set(key: String, value: T, ttlSeconds: Long? = 10.seconds.inWholeMilliseconds) = withContext(ioDispatcher) {
        val str = json.encodeToString(value)
        if (ttlSeconds != null) {
            connection.setex(key, ttlSeconds, str)
        } else {
            connection.set(key, str)
        }
    }

    suspend inline fun <reified T> get(key: String): T? = withContext(Dispatchers.IO) {
        val str = connection.get(key) ?: return@withContext null
        json.decodeFromString(str)
    }

    suspend fun close() = withContext(ioDispatcher) {
        client.shutdown()
    }
}
