# Build APK langsung dari HP (tanpa laptop)

Cara ini memakai GitHub Actions untuk mengompilasi source di server GitHub. Kamu perlu akun GitHub dan browser HP.

1. Buka GitHub di browser dan masuk akun.
2. Buat repository baru, misalnya `GuinevereAutoFree`. Pilih Private jika ingin source tidak publik.
3. Ekstrak ZIP source di HP menggunakan aplikasi pengelola arsip.
4. Unggah seluruh isi folder `GuinevereAutoFree` ke repository, termasuk folder tersembunyi `.github/workflows/build-apk.yml`. Pastikan `settings.gradle.kts`, `build.gradle.kts`, dan folder `app` berada di root repository (bukan di dalam folder bersarang lagi).
5. Buka tab **Actions** pada repository. Jika diminta, izinkan Actions.
6. Pilih workflow **Build Guinevere Auto Free APK**, tekan **Run workflow**, lalu **Run workflow** lagi.
7. Tunggu sampai job selesai dengan tanda centang hijau. Buka hasil run, cari bagian **Artifacts**, lalu unduh `GuinevereAutoFree-debug-apk`.
8. Ekstrak artifact ZIP, lalu instal `app-debug.apk` di HP. Android mungkin meminta izin instal aplikasi dari sumber browser/file manager.

Catatan: ini hanya jalur build dari HP; belum membuktikan APK berhasil dibangun atau berfungsi di MCGG. Jika build gagal, buka log job dan kirim teks errornya untuk diperbaiki. Prototipe saat ini masih eksperimental, perlu kalibrasi layar, dan sebaiknya diuji tanpa auto-tap dulu. Gunakan hanya jika diizinkan aturan game.
