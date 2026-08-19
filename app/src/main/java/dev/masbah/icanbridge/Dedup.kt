package dev.masbah.icanbridge

import android.content.Context

/**
 * iCan mengirim reading tiap ~3 menit tapi meng-UPDATE notifikasi yang sama
 * berkali-kali di antaranya. Tanpa dedup, Health Connect bisa terisi ratusan
 * duplikat per jam.
 */
class Dedup(context: Context) {
    private val prefs = context.getSharedPreferences("dedup", Context.MODE_PRIVATE)

    /** Minimal jarak antar tulisan, sedikit di bawah cadence 3 menit. */
    private val minGapMs = 150_000L

    @Synchronized
    fun shouldWrite(mgdl: Int, atMs: Long): Boolean {
        val lastValue = prefs.getInt("last_value", -1)
        val lastAt = prefs.getLong("last_at", 0L)

        if (mgdl == lastValue && atMs - lastAt < minGapMs) return false
        if (atMs - lastAt < 60_000L) return false // guard notifikasi yang di-repost cepat

        prefs.edit().putInt("last_value", mgdl).putLong("last_at", atMs).apply()
        return true
    }
}
