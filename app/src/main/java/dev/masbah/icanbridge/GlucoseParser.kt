package dev.masbah.icanbridge

import kotlin.math.roundToInt

/**
 * Parser teks notifikasi iCan -> nilai glukosa mg/dL.
 *
 * Mendukung:
 *  - Satuan mg/dL (rentang 36-450)
 *  - Satuan mmol/L (rentang 2.0-25.0 mmol/L, dikonversi otomatis ke mg/dL)
 *  - Format koma desimal Eropa ("7,2 mmol/L")
 *  - Model iCan i3 maupun iCan i6
 *  - Deteksi dan isolasi dari notifikasi multi-metrik baru Sinocare (Asam Urat / Uric Acid & Keton)
 */
object GlucoseParser {

    const val MIN_MGDL = 36
    const val MAX_MGDL = 450
    const val MIN_MMOL = 2.0
    const val MAX_MMOL = 25.0
    const val MMOL_TO_MGDL = 18.0182

    private val SIGNED = Regex("""[+\-\u2212]\s*\d+(?:[.,]\d+)?""")
    private val TIME = Regex("""\b\d{1,2}:\d{2}(:\d{2})?\s*(?:AM|PM|am|pm)?\b""")
    private val DATE = Regex("""\b\d{1,4}[/\-]\d{1,2}[/\-]\d{1,4}\b""")
    private val SENSOR_AGE = Regex("""\b\d+\s*d\s*\d+\s*h\b""", RegexOption.IGNORE_CASE)
    private val ALNUM_TOKEN = Regex("""\b(?=\w*\d)(?=\w*[A-Za-z])\w{4,}\b""") // mis. serial / token
    private val CANDIDATE_MGDL = Regex("""(?<![\d.,])(\d{2,3})(?![\d.,])""")
    private val CANDIDATE_MMOL = Regex("""(?<![\d.,])(\d{1,2}(?:[.,]\d{1,2})?)(?![\d.,])""")

    // Kata kunci metrik lain dari update Sinocare yang bukan glukosa darah
    private val NON_GLUCOSE_METRICS = listOf("asam urat", "uric acid", "keton", "ketone")

    data class Reading(val mgdl: Int, val trend: String?)

    fun parse(raw: String?): Reading? {
        if (raw.isNullOrBlank()) return null

        val isMgdl = raw.contains("mg/dL", ignoreCase = true) || raw.contains("mg/dl", ignoreCase = true)
        val isMmol = raw.contains("mmol/L", ignoreCase = true) || raw.contains("mmol/l", ignoreCase = true)

        // Notifikasi harus benar-benar soal glukosa darah.
        if (!isMgdl && !isMmol) return null

        // Proteksi multi-metrik: Jika notifikasi hanya mengenai Asam Urat atau Keton tanpa Glukosa, abaikan
        val lower = raw.lowercase()
        val hasNonGlucose = NON_GLUCOSE_METRICS.any { lower.contains(it) }
        val hasGlucoseExplicit = lower.contains("glukosa") || lower.contains("glucose") || lower.contains("cgm") || lower.contains("gula")
        if (hasNonGlucose && !hasGlucoseExplicit) {
            return null
        }

        val cleaned = raw
            .replace(SENSOR_AGE, " ")
            .replace(DATE, " ")
            .replace(TIME, " ")
            .replace(ALNUM_TOKEN, " ")
            .replace(SIGNED, " ")

        val mgdl = if (isMgdl) {
            CANDIDATE_MGDL.findAll(cleaned)
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .firstOrNull { it in MIN_MGDL..MAX_MGDL }
        } else {
            CANDIDATE_MMOL.findAll(cleaned)
                .mapNotNull {
                    val normalized = it.groupValues[1].replace(',', '.')
                    normalized.toDoubleOrNull()
                }
                .firstOrNull { it in MIN_MMOL..MAX_MMOL }
                ?.let { (it * MMOL_TO_MGDL).roundToInt() }
        } ?: return null

        return Reading(mgdl, extractTrend(raw))
    }

    private fun extractTrend(raw: String): String? = when {
        raw.contains("↑↑") || raw.contains("\u21c8") || raw.contains("⬆⬆") -> "RISING_RAPIDLY"
        raw.contains("↓↓") || raw.contains("\u21ca") || raw.contains("⬇⬇") -> "FALLING_RAPIDLY"
        raw.contains('\u2191') || raw.contains("\u2b06") || raw.contains("▲") -> "RISING"
        raw.contains('\u2193') || raw.contains("\u2b07") || raw.contains("▼") -> "FALLING"
        raw.contains('\u2197') || raw.contains("⬈") -> "RISING_SLOWLY"
        raw.contains('\u2198') || raw.contains("⬊") -> "FALLING_SLOWLY"
        raw.contains('\u2192') || raw.contains("\u27a1") || raw.contains("▶") || raw.contains("➔") -> "FLAT"
        else -> null
    }
}
