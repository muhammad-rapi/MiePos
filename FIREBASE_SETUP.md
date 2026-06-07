# Firebase Setup (Manual Steps)

Lakukan langkah ini di Firebase Console sebelum menjalankan app.

## 1. Buat Project Firebase

1. Buka https://console.firebase.google.com
2. Klik "Add project" → nama: **MiePos**
3. Disable Google Analytics (tidak perlu untuk MVP)
4. Klik "Create project"

## 2. Tambah Android App

1. Di project dashboard, klik ikon Android
2. Package name: `com.mipos.app`
3. App nickname: MiePos
4. Download `google-services.json`
5. Taruh file tersebut di folder: `MiePos/app/google-services.json`

## 3. Enable Authentication

1. Build → Authentication → Get started
2. Sign-in method → Email/Password → Enable → Save

## 4. Buat Akun Test

1. Authentication → Users → Add user
2. Tambah dua akun:
   - **Owner:** `owner@mipos.com` / `mipos123`
   - **Kasir:** `kasir@mipos.com` / `mipos123`
3. Catat UID masing-masing (akan dipakai untuk langkah selanjutnya)

## 5. Setup Firestore

1. Build → Firestore Database → Create database
2. Pilih "Start in production mode" → pilih region asia-southeast2 (Jakarta)
3. Klik "Enable"

## 6. Set Security Rules

Firestore → Rules → Edit → paste rules berikut → Publish:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /menus/{menuId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'owner';
    }
    match /transactions/{transId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 7. Tambah Dokumen Users

Firestore → Data → Start collection → collection ID: `users`

**Document 1** — Document ID = UID dari owner:
```
name: "Pak Budi (Owner)"
role: "owner"
email: "owner@mipos.com"
```

**Document 2** — Document ID = UID dari kasir:
```
name: "Siti (Kasir)"
role: "kasir"
email: "kasir@mipos.com"
```

## 8. Tambah Seed Menu

Firestore → Data → Add collection → collection ID: `menus`

Tambah 9 dokumen (auto-ID):

| name | price | category | available |
|---|---|---|---|
| Mie Ayam Biasa | 12000 | mie_ayam | true |
| Mie Ayam Bakso | 15000 | mie_ayam | true |
| Mie Ayam Komplit | 18000 | mie_ayam | true |
| Mie Ayam Pangsit | 16000 | mie_ayam | true |
| Es Teh Manis | 5000 | minuman | true |
| Es Jeruk | 7000 | minuman | true |
| Air Mineral | 4000 | minuman | true |
| Bakso Tambah | 5000 | tambahan | true |
| Pangsit Tambah | 4000 | tambahan | true |

## 9. Composite Index (jika diperlukan)

Saat pertama kali buka Laporan, mungkin ada error di Logcat tentang "requires an index".
Klik link di error tersebut untuk membuat index otomatis. Tunggu beberapa menit.
