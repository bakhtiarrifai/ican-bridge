package dev.masbah.icanbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GlucoseParserTest {

    @Test fun ambil_nilai_bukan_delta() {
        assertEquals(174, GlucoseParser.parse("iCan | 174 \u2192 -1 mg/dL")?.mgdl)
    }

    @Test fun delta_positif_juga_diabaikan() {
        assertEquals(112, GlucoseParser.parse("112 \u2197 +7 mg/dL")?.mgdl)
    }

    @Test fun jam_dan_tanggal_diabaikan() {
        assertEquals(175, GlucoseParser.parse("08/14 05:24 AM 175 mg/dL")?.mgdl)
    }

    @Test fun umur_sensor_diabaikan() {
        assertEquals(88, GlucoseParser.parse("SN 82C0024E2594A (14d16h) | 88 mg/dL")?.mgdl)
    }

    @Test fun trend_terbaca() {
        assertEquals("FLAT", GlucoseParser.parse("174 \u2192 -1 mg/dL")?.trend)
        assertEquals("RISING_RAPIDLY", GlucoseParser.parse("210 ↑↑ +15 mg/dL")?.trend)
        assertEquals("FALLING_RAPIDLY", GlucoseParser.parse("70 ↓↓ -12 mg/dL")?.trend)
        assertEquals("RISING", GlucoseParser.parse("140 ▲ +4 mg/dL")?.trend)
        assertEquals("FALLING", GlucoseParser.parse("85 ▼ -3 mg/dL")?.trend)
    }

    @Test fun tanpa_satuan_ditolak() {
        assertNull(GlucoseParser.parse("Sensor akan berakhir dalam 6 jam"))
    }

    @Test fun di_luar_rentang_alat_ditolak() {
        assertNull(GlucoseParser.parse("999 mg/dL"))
    }

    @Test fun mmol_terbaca_dan_dikonversi() {
        // 7.0 mmol/L * 18.0182 = 126.1 -> 126 mg/dL
        assertEquals(126, GlucoseParser.parse("iCan CGM | 7.0 mmol/L")?.mgdl)
    }

    @Test fun mmol_dengan_koma_desimal_terbaca() {
        // 5,5 mmol/L * 18.0182 = 99.1 -> 99 mg/dL
        assertEquals(99, GlucoseParser.parse("iCan | 5,5 mmol/L \u2192 -0,1")?.mgdl)
    }

    @Test fun notifikasi_asam_urat_ditolak() {
        assertNull(GlucoseParser.parse("iCan | Asam Urat: 6.5 mg/dL"))
        assertNull(GlucoseParser.parse("Uric Acid alert: 7.2 mg/dL"))
    }

    @Test fun notifikasi_keton_ditolak() {
        assertNull(GlucoseParser.parse("iCan | Keton Darah: 0.4 mmol/L"))
    }

    @Test fun format_ican_i6() {
        assertEquals(135, GlucoseParser.parse("iCan i6 | 135 mg/dL \u2192")?.mgdl)
    }
}
