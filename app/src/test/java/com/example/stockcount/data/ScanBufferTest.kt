package com.example.stockcount.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScanBufferTest {

    @Test
    fun undoWithinWindowReturnsCode() {
        val buffer = ScanBuffer(windowMillis = 1_000L)
        buffer.record("123", timeMillis = 0L)

        val result = buffer.undo(currentTimeMillis = 500L)

        assertEquals("123", result)
    }

    @Test
    fun undoAfterWindowReturnsNull() {
        val buffer = ScanBuffer(windowMillis = 1_000L)
        buffer.record("123", timeMillis = 0L)

        val result = buffer.undo(currentTimeMillis = 1_500L)

        assertNull(result)
    }
}
