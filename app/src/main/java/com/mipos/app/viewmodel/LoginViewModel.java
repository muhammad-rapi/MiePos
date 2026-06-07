package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.User;
import com.mipos.app.repository.AuthRepository;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository = new AuthRepository();
    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password, userLiveData, errorLiveData);
        userLiveData.observeForever(u -> loadingLiveData.setValue(false));
        errorLiveData.observeForever(e -> loadingLiveData.setValue(false));
    }
}
