package com.shuttl.location_pings.util

object BatchCounter {
    private var batchEvent = 0

    fun increment() {
        batchEvent++
    }

    fun getBatchCount() = batchEvent

    fun reset() {
        batchEvent = 0
    }
}