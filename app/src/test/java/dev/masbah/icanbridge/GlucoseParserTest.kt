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
    }

    @Test fun tanpa_satuan_ditolak() {
        assertNull(GlucoseParser.parse("Sensor akan berakhir dalam 6 jam"))
    }

    @Test fun di_luar_rentang_alat_ditolak() {
        assertNull(GlucoseParser.parse("999 mg/dL"))
    }
}
