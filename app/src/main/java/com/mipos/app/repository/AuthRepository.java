package com.mipos.app.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mipos.app.models.User;
import com.mipos.app.utils.SessionManager;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void login(String email, String password,
                      MutableLiveData<User> userLiveData,
                      MutableLiveData<String> errorLiveData) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();
                db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(uid);
                            SessionManager.getInstance().setCurrentUser(user);
                            userLiveData.setValue(user);
                        } else {
                            errorLiveData.setValue("Data pengguna tidak ditemukan");
                        }
                    })
                    .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
            })
            .addOnFailureListener(e -> {
                String msg = e.getMessage();
                if (msg != null && msg.contains("password")) {
                    errorLiveData.setValue("Password salah");
                } else if (msg != null && msg.contains("no user")) {
                    errorLiveData.setValue("Email tidak terdaftar");
                } else {
                    errorLiveData.setValue("Login gagal. Coba lagi.");
                }
            });
    }

    public void logout() {
        auth.signOut();
        SessionManager.getInstance().clear();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void fetchCurrentUser(MutableLiveData<User> userLiveData,
                                  MutableLiveData<String> errorLiveData) {
        String uid = getCurrentUid();
        if (uid == null) { errorLiveData.setValue("Tidak ada sesi aktif"); return; }
        db.collection("users").document(uid).get()
            .addOnSuccessListener(doc -> {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setUid(uid);
                    SessionManager.getInstance().setCurrentUser(user);
                    userLiveData.setValue(user);
                }
            })
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
