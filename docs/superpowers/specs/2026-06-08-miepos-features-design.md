# MiePos Feature Expansion — Design Spec
**Date:** 2026-06-08  
**Status:** Approved

---

## Overview

Pengembangan MiePos dari POS kasir sederhana menjadi sistem kasir lengkap dengan manajemen menu, laporan multi-periode, export data, cetak struk otomatis, QRIS static, dan UI yang dipercantik dengan tema warm & vibrant.

---

## 1. QRIS Static Generator

### Tujuan
Kasir dapat menampilkan QR code pembayaran QRIS full-screen saat pelanggan memilih metode QRIS.

### Alur
1. Owner upload gambar QR (dari bank/e-wallet) via halaman Pengaturan QRIS di ProfilFragment.
2. Gambar disimpan ke Firebase Storage, URL disimpan ke Firestore dokumen toko (`stores/{storeId}.qrisImageUrl`).
3. Saat kasir memilih "QRIS" di PaymentBottomSheet, app fetch URL dan tampilkan gambar QR full-screen dengan nominal transaksi di bawahnya.
4. Kasir tap "Konfirmasi Bayar" setelah pelanggan scan — transaksi dicatat seperti biasa.

### Data Model
```
Firestore: stores/{uid}
  - qrisImageUrl: String
  - storeName: String
```

### Komponen Baru
- `QrisFullscreenActivity` — tampilkan QR + nominal, tombol konfirmasi
- `PengaturanQrisFragment` — form upload QR image (owner only)
- `StoreRepository` — get/set data toko

---

## 2. Laporan Multi-Periode

### Tujuan
Laporan bisa dilihat per Hari Ini, Minggu Ini, dan Bulan Ini.

### Alur
- `LaporanFragment` menampilkan `ChipGroup` dengan 3 pilihan: Hari Ini / Minggu Ini / Bulan Ini.
- Saat chip dipilih, `LaporanViewModel` menghitung `startDate`/`endDate` lalu load ulang data.
- Summary cards: total omzet, jumlah transaksi, rata-rata per transaksi, item terlaris.
- Breakdown metode bayar: Cash / QRIS / Debit (nominal + persentase).
- List transaksi tetap ada di bawah summary.

### Perubahan Repository
- `TransactionRepository.getTodayTransactions()` di-refactor menjadi `getTransactionsByRange(Date start, Date end, Consumer<List<Transaction>> onSuccess, ...)`
- `LaporanViewModel` tambah `MutableLiveData<String> selectedPeriod` ("daily"/"weekly"/"monthly")

### Komponen Baru
- `SummaryCard` view component (reusable)
- `LaporanViewModel.loadReport(String period)`

---

## 3. Export Laporan (PDF + Excel)

### Tujuan
Owner/kasir bisa export laporan periode aktif ke PDF atau Excel, lalu share.

### Library
- **PDF**: Android native `android.graphics.pdf.PdfDocument` (tidak perlu dependency tambahan)
- **Excel**: `FastExcel` (lightweight, ~200KB) — lebih ringan dari Apache POI

### Alur
1. Tombol export (icon download) di toolbar `LaporanFragment`.
2. Muncul bottom sheet pilih format: PDF / Excel.
3. File di-generate di background thread, disimpan ke `Downloads/MiePos/laporan_YYYYMMDD_HHMMSS.pdf/.xlsx`.
4. Setelah selesai, `ShareIntent` otomatis terbuka — bisa kirim via WhatsApp, email, dll.

### Konten File
- Header: nama toko, periode laporan, tanggal export
- Summary: total omzet, jumlah transaksi
- Breakdown metode bayar
- Tabel transaksi: no, waktu, kasir, items, metode bayar, total

### Komponen Baru
- `ExportManager` (util class) — handle PDF dan Excel generation
- `ExportBottomSheet` — pilih format export

---

## 4. Manajemen Menu (Owner Only)

### Tujuan
Owner dapat tambah, edit, dan hapus menu dari dalam app.

### Role Management
- Field `role` ("owner" / "kasir") di Firestore `users/{uid}.role`
- `AuthRepository.fetchCurrentUser()` sudah fetch user data — tambah field `role` ke model `User`
- `SessionManager` simpan role saat login
- `MainActivity` cek role: tab "Menu" di bottom nav hanya muncul kalau `role == "owner"`

### Fitur
- `ManajemenMenuFragment`: RecyclerView list semua menu + FAB tambah
- Tiap item: nama, harga, kategori, badge "Tersedia/Habis", tombol edit & hapus
- `TambahEditMenuBottomSheet`: form nama, harga, kategori (dropdown), toggle tersedia
- Field `imageUrl` di `MenuItem` — opsional, upload via Firebase Storage + Glide untuk display
- Soft delete: ubah `available = false` daripada hapus dokumen (preserves transaction history)

### Perubahan Model
```java
// MenuItem tambah:
private String imageUrl; // nullable
```

### Komponen Baru
- `ManajemenMenuFragment`
- `ManajemenMenuViewModel`
- `TambahEditMenuBottomSheet`
- `MenuRepository.addMenu()`, `updateMenu()`, `deleteMenu()` (set available=false)
- `StorageRepository` — upload image ke Firebase Storage

---

## 5. Cetak Struk Otomatis

### Tujuan
Setelah transaksi berhasil, struk otomatis dikirim ke thermal printer (Bluetooth atau WiFi/LAN).

### Library
- **`escpos-coffee`** (v3.x) — support ESC/POS, Bluetooth, dan TCP/IP (WiFi)

### Pengaturan Printer (Owner)
- Di ProfilFragment > "Pengaturan Printer"
- Bluetooth: scan device, pilih printer, pair → simpan MAC address ke `SharedPreferences`
- WiFi/LAN: input IP address + port (default 9100) → simpan ke `SharedPreferences`
- Test print tombol

### Alur Cetak
1. Setelah `processPayment()` sukses di `PosViewModel`, emit event ke `PosFragment`
2. `PosFragment` panggil `PrinterManager.print(receiptData)`
3. `PrinterManager` cek mode koneksi (BT/WiFi), buka koneksi, kirim data ESC/POS, tutup koneksi
4. Jika gagal: snackbar "Printer tidak terhubung" + opsi "Coba Lagi" / "Lewati"

### Konten Struk
```
[Nama Toko]
[Tanggal] [Jam]
--------------------------------
[Nama Item]     [Qty]  [Subtotal]
...
--------------------------------
TOTAL           Rp [total]
Bayar           [metode]
Kembalian       Rp [kembalian]  (jika cash)
--------------------------------
      Terima kasih!
```

### Komponen Baru
- `PrinterManager` (singleton util) — handle BT + WiFi connection & print
- `ReceiptBuilder` — build ESC/POS byte array dari data transaksi
- `PrinterSettingsFragment` — UI pengaturan printer

---

## 6. UI Beautifikasi — Warm & Vibrant

### Color Palette
```xml
colorPrimary     = #FF6B35  (oranye)
colorSecondary   = #FFB800  (kuning)
colorBackground  = #FFF8F0  (krem hangat)
colorSurface     = #FFFFFF
colorOnPrimary   = #FFFFFF
colorAccent      = #E84545  (merah aksen)
```

### Per Halaman

**LoginActivity**
- Background krem dengan ilustrasi sederhana (bowl mie / icon POS)
- Logo/nama app besar di atas
- Card putih dengan shadow untuk form login
- Tombol gradient oranye→kuning

**PosFragment**
- Header gradient oranye dengan nama kasir + total cart badge
- Menu card: gambar/placeholder icon + nama bold + harga + tombol tambah
- Cart redesign: background krem, item card dengan qty badge, subtotal per item
- Tombol "Bayar" sticky di bawah, gradient oranye, disabled jika cart kosong

**LaporanFragment**
- Summary cards berwarna: oranye (omzet), hijau (jumlah transaksi), biru (rata-rata)
- ChipGroup periode dengan style filled
- List transaksi dengan card dan separator

**ProfilFragment**
- Avatar circle dengan inisial nama
- Info cards terstruktur (nama, email, role)
- Tombol logout merah

**PaymentBottomSheet**
- Ikon per metode bayar (cash: 💵, QRIS: QR icon, debit: kartu)
- Warna highlight untuk metode terpilih
- Tampilan kembalian lebih prominent

**ManajemenMenuFragment (baru)**
- FAB oranye
- Menu card dengan gambar, badge kategori berwarna, toggle available

### Library Tambahan
- `MPAndroidChart` — bar chart breakdown metode bayar di laporan

---

## Dependency Tambahan (build.gradle)

```groovy
// Export
implementation 'com.github.indi-engine:fastexcel:0.18.0'

// Thermal Printer
implementation 'com.github.anastaciocintra:escpos-coffee:3.3.0'

// Chart
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

---

## Urutan Implementasi (Prioritas)

1. **Role management** — fondasi untuk fitur owner-only
2. **UI Beautifikasi** — dikerjakan bersamaan dengan fitur lain
3. **Manajemen Menu** — CRUD menu dengan role guard
4. **Laporan multi-periode** — refactor repository + summary cards
5. **Export Laporan** — PDF + Excel + share
6. **QRIS Static** — upload + display full-screen
7. **Cetak Struk** — PrinterManager + settings
