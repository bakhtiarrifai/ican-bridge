package dev.masbah.icanbridge

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ICanNotificationListener : NotificationListenerService() {

    companion object {
        // Paket app iCan versi CE. Kalau region kamu memakai varian lain,
        // cek daftar paket lewat DebugLog dengan ACCEPT_ALL = true.
        val TARGET_PACKAGES = setOf(
            "com.sinocare.ican.health.ce",
            "com.sinocare.ican.health"
        )

        /** Set true sementara kalau notifikasi iCan tidak pernah tertangkap. */
        const val ACCEPT_ALL = false
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dedup: Dedup
    private lateinit var writer: HealthConnectWriter

    override fun onCreate() {
        super.onCreate()
        dedup = Dedup(this)
        writer = HealthConnectWriter(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!ACCEPT_ALL && sbn.packageName !in TARGET_PACKAGES) return

        val ex = sbn.notification.extras
        val parts = listOfNotNull(
            ex.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            ex.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            ex.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            ex.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            ex.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
        )
        val raw = parts.joinToString(" | ")

        val reading = GlucoseParser.parse(raw)
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(sbn.postTime))

        DebugLog.add(
            this,
            "[$stamp] ${sbn.packageName}\n$raw\n-> ${reading?.mgdl ?: "TIDAK TERBACA"} mg/dL" +
                (reading?.trend?.let { " ($it)" } ?: "")
        )

        if (reading == null) return
        if (!dedup.shouldWrite(reading.mgdl, sbn.postTime)) return

        scope.launch {
            runCatching { writer.write(reading.mgdl, sbn.postTime) }
                .onFailure { DebugLog.add(this@ICanNotificationListener, "[$stamp] GAGAL TULIS: ${it.message}") }
        }
    }
}
