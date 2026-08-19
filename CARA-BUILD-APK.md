# Build APK lewat GitHub Actions

Tidak butuh Android SDK di laptop sama sekali.

## 1. Buat repo dan push

Dari folder hasil ekstrak:

    cd ~/Downloads/ican-bridge
    git init -b main
    git add .
    git commit -m "iCan Bridge"
    gh repo create ican-bridge --private --source=. --push

Kalau `gh` belum ada di Fedora: `sudo dnf install gh` lalu `gh auth login`.

Tanpa `gh`: buat repo kosong di github.com, lalu

    git remote add origin git@github.com:USERNAME/ican-bridge.git
    git push -u origin main

## 2. Ambil APK-nya

Buka repo di GitHub -> tab **Actions** -> klik run paling atas.
Tunggu sampai centang hijau (sekitar 4-7 menit untuk run pertama).
Scroll ke bawah ke bagian **Artifacts** -> unduh `ican-bridge-apk`.

Hasilnya file zip berisi `app-debug.apk`.

## 3. Pasang di Galaxy S25 Ultra

Cara termudah: unduh artifact-nya langsung dari browser HP, ekstrak,
lalu ketuk APK-nya. Saat muncul peringatan, izinkan browser/file manager
untuk memasang dari sumber tidak dikenal.

Lewat kabel dari Fedora juga bisa:

    sudo dnf install android-tools
    adb install app-debug.apk

APK ini ditandatangani dengan debug key bawaan. Aman untuk pemakaian
pribadi, tapi tidak bisa diunggah ke Play Store dan tidak akan
menerima update otomatis.

## Kalau build gagal

Buka run yang merah di tab Actions, klik langkah "Build debug APK",
dan salin baris error paling bawah. Tersangka utama adalah pemanggilan
`Metadata.autoRecorded()` di HealthConnectWriter.kt jika versi API
Health Connect berubah lagi.
