package com.example.bluetooth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

class GattOperationQueue(private val defaultTimeoutMillis: Long = 4000L) {
    private val mutex = Mutex()
    private var activeDeferred: CompletableDeferred<Any?>? = null

    suspend fun <T> execute(
        description: String,
        timeoutMillis: Long = defaultTimeoutMillis,
        block: () -> Boolean
    ): Result<T> = mutex.withLock {
        val deferred = CompletableDeferred<Any?>()
        activeDeferred = deferred

        try {
            val initiated = block()
            if (!initiated) {
                return@withLock Result.failure(IllegalStateException("Failed to initiate GATT operation: $description"))
            }

            val result = withTimeoutOrNull(timeoutMillis) {
                @Suppress("UNCHECKED_CAST")
                deferred.await() as T
            }

            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(IllegalStateException("GATT operation '$description' timed out after ${timeoutMillis}ms"))
            }
        } catch (e: CancellationException) {
            deferred.cancel(e)
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            activeDeferred = null
        }
    }

    fun completeActive(result: Any?) {
        activeDeferred?.complete(result)
    }

    fun failActive(throwable: Throwable) {
        activeDeferred?.completeExceptionally(throwable)
    }

    fun cancelAll() {
        activeDeferred?.cancel()
        activeDeferred = null
    }
}
