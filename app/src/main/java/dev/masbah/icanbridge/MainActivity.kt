package dev.masbah.icanbridge

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var logView: TextView
    private lateinit var writer: HealthConnectWriter

    private val requestPerms =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) {
            refresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writer = HealthConnectWriter(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }
        status = TextView(this).apply { textSize = 15f }
        logView = TextView(this).apply { textSize = 11f; setPadding(0, 30, 0, 0) }

        root.addView(status)
        root.addView(button("1. Izin baca notifikasi") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })
        root.addView(button("2. Izin tulis Health Connect") {
            requestPerms.launch(HealthConnectWriter.PERMISSIONS)
        })
        root.addView(button("Muat ulang log") { refresh() })
        root.addView(button("Bersihkan log") { DebugLog.clear(this); refresh() })
        root.addView(logView)

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setOnClickListener { onClick() }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val listenerOn = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true

        lifecycleScope.launch {
            val hcState = when (writer.availability()) {
                HealthConnectClient.SDK_AVAILABLE ->
                    if (writer.hasPermission()) "siap" else "terpasang, izin BELUM diberikan"
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "perlu update"
                else -> "tidak tersedia di perangkat ini"
            }
            status.text = buildString {
                append("Notification access: ${if (listenerOn) "AKTIF" else "MATI"}\n")
                append("Health Connect: $hcState\n\n")
                append("Kalau ada notifikasi iCan yang tidak terbaca, salin barisnya dari log di bawah.")
            }
            logView.text = DebugLog.read(this@MainActivity).joinToString("\n\n")
                .ifBlank { "(belum ada notifikasi iCan tertangkap)" }
        }
    }
}
