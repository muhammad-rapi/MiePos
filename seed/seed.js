const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

// Ganti dengan path ke service account key kamu
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({ credential: cert(serviceAccount) });
const db = getFirestore();

const menus = [
  { name: 'Mie Ayam Biasa',   price: 12000, category: 'mie_ayam',  available: true },
  { name: 'Mie Ayam Bakso',   price: 15000, category: 'mie_ayam',  available: true },
  { name: 'Mie Ayam Komplit', price: 18000, category: 'mie_ayam',  available: true },
  { name: 'Mie Ayam Pangsit', price: 16000, category: 'mie_ayam',  available: true },
  { name: 'Es Teh Manis',     price: 5000,  category: 'minuman',   available: true },
  { name: 'Es Jeruk',         price: 7000,  category: 'minuman',   available: true },
  { name: 'Air Mineral',      price: 4000,  category: 'minuman',   available: true },
  { name: 'Bakso Tambah',     price: 5000,  category: 'tambahan',  available: true },
  { name: 'Pangsit Tambah',   price: 4000,  category: 'tambahan',  available: true },
];

async function seed() {
  const batch = db.batch();
  menus.forEach(menu => {
    const ref = db.collection('menus').doc();
    batch.set(ref, menu);
  });
  await batch.commit();
  console.log(`✅ ${menus.length} menu berhasil ditambahkan ke Firestore`);
}

seed().catch(console.error);
