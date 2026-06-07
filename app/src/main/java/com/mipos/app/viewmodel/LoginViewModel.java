package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.User;
import com.mipos.app.repository.AuthRepository;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository = new AuthRepository();
    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    private Observer<User> userObserver;
    private Observer<String> errorObserver;

    public void login(String email, String password) {
        loadingLiveData.setValue(true);

        if (userObserver != null) userLiveData.removeObserver(userObserver);
        if (errorObserver != null) errorLiveData.removeObserver(errorObserver);

        userObserver = u -> loadingLiveData.setValue(false);
        errorObserver = e -> loadingLiveData.setValue(false);

        userLiveData.observeForever(userObserver);
        errorLiveData.observeForever(errorObserver);

        authRepository.login(email, password, userLiveData, errorLiveData);
    }

    @Override
    protected void onCleared() {
        if (userObserver != null) userLiveData.removeObserver(userObserver);
        if (errorObserver != null) errorLiveData.removeObserver(errorObserver);
        super.onCleared();
    }
}
