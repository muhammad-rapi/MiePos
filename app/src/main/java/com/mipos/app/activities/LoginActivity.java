package com.mipos.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.mipos.app.databinding.ActivityLoginBinding;
import com.mipos.app.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.userLiveData.observe(this, user -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        viewModel.errorLiveData.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(error);
            }
        });

        viewModel.loadingLiveData.observe(this, loading -> {
            binding.btnLogin.setEnabled(!loading);
            binding.btnLogin.setText(loading ? "Memuat..." : "Masuk");
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            binding.tvError.setVisibility(View.GONE);
            if (email.isEmpty() || password.isEmpty()) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText("Email dan password harus diisi");
                return;
            }
            viewModel.login(email, password);
        });
    }
}
