package dev.masbah.icanbridge

import android.content.Context

/**
 * Ini bagian terpenting saat pertama kali dipasang.
 *
 * Saya belum pernah melihat teks notifikasi iCan yang sebenarnya, jadi parser
 * di atas dibangun dari tebakan terdidik. DebugLog menyimpan dump mentah setiap
 * notifikasi iCan supaya kamu bisa membuka layar utama, melihat teks aslinya,
 * dan menyesuaikan GlucoseParser kalau ternyata polanya lain.
 */
object DebugLog {
    private const val MAX = 30

    fun add(context: Context, line: String) {
        val prefs = context.getSharedPreferences("debug", Context.MODE_PRIVATE)
        val existing = prefs.getString("lines", "").orEmpty()
        val merged = (listOf(line) + existing.split("\n\u0000\n").filter { it.isNotBlank() })
            .take(MAX)
            .joinToString("\n\u0000\n")
        prefs.edit().putString("lines", merged).apply()
    }

    fun read(context: Context): List<String> =
        context.getSharedPreferences("debug", Context.MODE_PRIVATE)
            .getString("lines", "").orEmpty()
            .split("\n\u0000\n")
            .filter { it.isNotBlank() }

    fun clear(context: Context) =
        context.getSharedPreferences("debug", Context.MODE_PRIVATE).edit().clear().apply()
}
