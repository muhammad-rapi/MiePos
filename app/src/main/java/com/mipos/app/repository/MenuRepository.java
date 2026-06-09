package com.mipos.app.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mipos.app.models.MenuItem;
import java.util.ArrayList;
import java.util.List;

public class MenuRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getMenus(MutableLiveData<List<MenuItem>> menusLiveData,
                         MutableLiveData<String> errorLiveData) {
        db.collection("menus")
            .whereEqualTo("available", true)
            .get()
            .addOnSuccessListener(snapshots -> {
                List<MenuItem> list = new ArrayList<>();
                for (var doc : snapshots) {
                    MenuItem m = doc.toObject(MenuItem.class);
                    m.setId(doc.getId());
                    list.add(m);
                }
                menusLiveData.postValue(list);
            })
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }

    public void getAllMenus(MutableLiveData<List<MenuItem>> menusLiveData,
                            MutableLiveData<String> errorLiveData) {
        db.collection("menus")
            .get()
            .addOnSuccessListener(snapshots -> {
                List<MenuItem> list = new ArrayList<>();
                for (var doc : snapshots) {
                    MenuItem m = doc.toObject(MenuItem.class);
                    m.setId(doc.getId());
                    list.add(m);
                }
                menusLiveData.postValue(list);
            })
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }

    public void addMenu(MenuItem menu, MutableLiveData<Boolean> successLiveData,
                        MutableLiveData<String> errorLiveData) {
        db.collection("menus").add(menu)
            .addOnSuccessListener(ref -> successLiveData.postValue(true))
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }

    public void updateMenu(MenuItem menu, MutableLiveData<Boolean> successLiveData,
                           MutableLiveData<String> errorLiveData) {
        db.collection("menus").document(menu.getId()).set(menu)
            .addOnSuccessListener(v -> successLiveData.postValue(true))
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }

    public void setAvailable(String menuId, boolean available,
                             MutableLiveData<Boolean> successLiveData,
                             MutableLiveData<String> errorLiveData) {
        db.collection("menus").document(menuId)
            .update("available", available)
            .addOnSuccessListener(v -> successLiveData.postValue(true))
            .addOnFailureListener(e -> errorLiveData.postValue(e.getMessage()));
    }
}
