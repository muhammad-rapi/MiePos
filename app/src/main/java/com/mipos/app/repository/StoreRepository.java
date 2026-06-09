package com.mipos.app.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mipos.app.utils.SessionManager;
import java.util.HashMap;
import java.util.Map;

public class StoreRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getQrisImageUrl(MutableLiveData<String> urlLiveData,
                                 MutableLiveData<String> errorLiveData) {
        String uid = SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { errorLiveData.postValue("User tidak valid"); return; }
        db.collection("stores").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    urlLiveData.postValue(doc.getString("qrisImageUrl"));
                } else {
                    urlLiveData.postValue(null);
                }
            })
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }

    public void saveQrisImageUrl(String imageUrl, MutableLiveData<Boolean> successLiveData,
                                  MutableLiveData<String> errorLiveData) {
        String uid = SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { errorLiveData.postValue("User tidak valid"); return; }
        Map<String, Object> data = new HashMap<>();
        data.put("qrisImageUrl", imageUrl);
        db.collection("stores").document(uid).set(data)
            .addOnSuccessListener(v -> successLiveData.postValue(true))
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }
}
