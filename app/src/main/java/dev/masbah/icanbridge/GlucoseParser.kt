package dev.masbah.icanbridge

/**
 * Parser teks notifikasi iCan -> nilai glukosa mg/dL.
 *
 * Kenapa tidak sekadar Regex("\\d+"):
 *  1. Layar iCan menampilkan nilai DAN delta berdampingan ("174" lalu "-1 mg/dL").
 *     Angka bertanda harus dibuang dulu, kalau tidak delta bisa terbaca sebagai nilai.
 *  2. Teks bisa mengandung angka lain: sisa umur sensor (14d16h), jam (05:27),
 *     tanggal (08/14/2026), potongan serial (82C0024E2594A).
 *  3. Rentang alat iCan i3 adalah 36-450 mg/dL. Apa pun di luar itu bukan reading.
 */
object GlucoseParser {

    const val MIN_MGDL = 36
    const val MAX_MGDL = 450

    private val SIGNED = Regex("""[+\-\u2212]\s*\d+(?:[.,]\d+)?""")
    private val TIME = Regex("""\b\d{1,2}:\d{2}(:\d{2})?\b""")
    private val DATE = Regex("""\b\d{1,4}[/\-]\d{1,2}[/\-]\d{1,4}\b""")
    private val SENSOR_AGE = Regex("""\b\d+\s*d\s*\d+\s*h\b""", RegexOption.IGNORE_CASE)
    private val ALNUM_TOKEN = Regex("""\b(?=\w*\d)(?=\w*[A-Za-z])\w{5,}\b""") // mis. serial
    private val CANDIDATE = Regex("""(?<![\d.,])(\d{2,3})(?![\d.,])""")

    data class Reading(val mgdl: Int, val trend: String?)

    fun parse(raw: String?): Reading? {
        if (raw.isNullOrBlank()) return null
        // Notifikasi harus benar-benar soal glukosa. Kalau tidak ada satuan,
        // lebih baik tolak daripada menulis angka acak ke Health Connect.
        if (!raw.contains("mg/dL", ignoreCase = true) &&
            !raw.contains("mg/dl", ignoreCase = true)
        ) return null

        val cleaned = raw
            .replace(SENSOR_AGE, " ")
            .replace(DATE, " ")
            .replace(TIME, " ")
            .replace(ALNUM_TOKEN, " ")
            .replace(SIGNED, " ")

        val mgdl = CANDIDATE.findAll(cleaned)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .firstOrNull { it in MIN_MGDL..MAX_MGDL }
            ?: return null

        return Reading(mgdl, extractTrend(raw))
    }

    private fun extractTrend(raw: String): String? = when {
        raw.contains('\u2191') || raw.contains("\u2b06") -> "RISING"
        raw.contains('\u2193') || raw.contains("\u2b07") -> "FALLING"
        raw.contains('\u2197') -> "RISING_SLOWLY"
        raw.contains('\u2198') -> "FALLING_SLOWLY"
        raw.contains('\u2192') || raw.contains("\u27a1") -> "FLAT"
        else -> null
    }
}
