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
                List<MenuItem> menus = new ArrayList<>();
                for (var doc : snapshots) {
                    MenuItem menu = doc.toObject(MenuItem.class);
                    menu.setId(doc.getId());
                    menus.add(menu);
                }
                menusLiveData.setValue(menus);
            })
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
