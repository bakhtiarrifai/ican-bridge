# iCan Bridge

Jembatan satu arah: notifikasi app **iCan CGM** (Sinocare) -> **Health Connect**.
Dari Health Connect, Samsung Health / Fitbit / Home Assistant bisa ikut membaca.

Tidak menyentuh BLE transmitter sama sekali, jadi app iCan resmi tetap jalan normal.

## Urutan pemasangan

1. Buka project ini di Android Studio, `Run` ke HP (bukan emulator).
2. Tekan tombol **1** -> aktifkan "iCan Bridge" di daftar Notification access.
3. Tekan tombol **2** -> beri izin *Write blood glucose*.
4. Pastikan app iCan menampilkan notifikasi persisten berisi angka glukosa,
   dan notifikasinya tidak di-silence.
5. Buang iCan Bridge **dan** app iCan dari optimasi baterai
   (Settings -> Battery -> Unrestricted). Ini penyebab nomor satu data bolong.

## Kalau angka tidak masuk

Layar utama menyimpan dump mentah 30 notifikasi terakhir. Buka, lihat baris
yang berakhir `TIDAK TERBACA`, lalu sesuaikan `GlucoseParser.kt`.

Kalau log kosong sama sekali, paket app-nya mungkin beda. Set
`ICanNotificationListener.ACCEPT_ALL = true` sementara, picu satu reading,
lalu baca nama paket yang muncul di log dan masukkan ke `TARGET_PACKAGES`.

## Batasan yang perlu disadari

- **Lag.** Screenshot menunjukkan jeda ~3 menit antara waktu reading dan waktu
  tampil. Timestamp yang ditulis adalah waktu notifikasi muncul, bukan waktu
  sensor mengukur. Untuk grafik harian ini tidak masalah; untuk apa pun yang
  time-critical, ini masalah.
- **Tidak ada backfill.** Bridge ini hanya menangkap data ke depan. Riwayat lama
  harus lewat tombol share/export di tab Data app iCan.
- **Notifikasi mati = data hilang diam-diam.** Tidak ada mekanisme rekonsiliasi.
- Bukan perangkat medis. Jangan jadikan dasar keputusan dosis.
