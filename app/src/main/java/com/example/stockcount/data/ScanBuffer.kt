package com.example.stockcount.data

/**
 * In-memory buffer that stores the most recent scan. The buffer allows undoing the
 * last scan within a configurable time window. Once the window has passed, the
 * scan can no longer be undone.
 */
class ScanBuffer(private val windowMillis: Long = DEFAULT_WINDOW_MS) {

    private var lastScan: Scan? = null

    /**
     * Records a scan with the current time.
     */
    fun record(code: String, timeMillis: Long = System.currentTimeMillis()) {
        lastScan = Scan(code, timeMillis)
    }

    /**
     * Returns true if the last scan is still within the undo window.
     */
    fun canUndo(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val scan = lastScan ?: return false
        return currentTimeMillis - scan.timestamp <= windowMillis
    }

    /**
     * Returns the last scanned code if undo is allowed. The scan is cleared once
     * undone. If the window has expired or no scan exists, returns null.
     */
    fun undo(currentTimeMillis: Long = System.currentTimeMillis()): String? {
        return if (canUndo(currentTimeMillis)) {
            val code = lastScan!!.code
            lastScan = null
            code
        } else {
            null
        }
    }

    private data class Scan(val code: String, val timestamp: Long)

    companion object {
        const val DEFAULT_WINDOW_MS: Long = 5_000L
    }
}
