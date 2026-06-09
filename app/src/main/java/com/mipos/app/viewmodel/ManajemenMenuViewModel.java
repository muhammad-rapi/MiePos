package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.MenuItem;
import com.mipos.app.repository.MenuRepository;
import java.util.List;

public class ManajemenMenuViewModel extends ViewModel {
    private final MenuRepository repo = new MenuRepository();

    public final MutableLiveData<List<MenuItem>> menusLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadAllMenus() {
        repo.getAllMenus(menusLiveData, errorLiveData);
    }

    public void addMenu(MenuItem menu) {
        repo.addMenu(menu, successLiveData, errorLiveData);
    }

    public void updateMenu(MenuItem menu) {
        repo.updateMenu(menu, successLiveData, errorLiveData);
    }

    public void toggleAvailable(MenuItem menu) {
        repo.setAvailable(menu.getId(), !menu.isAvailable(), successLiveData, errorLiveData);
    }
}
