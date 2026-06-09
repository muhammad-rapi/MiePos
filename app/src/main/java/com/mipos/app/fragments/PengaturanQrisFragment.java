package com.mipos.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import com.bumptech.glide.Glide;
import com.mipos.app.databinding.FragmentPengaturanQrisBinding;
import com.mipos.app.repository.StoreRepository;

public class PengaturanQrisFragment extends Fragment {
    private FragmentPengaturanQrisBinding binding;
    private final StoreRepository storeRepo = new StoreRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPengaturanQrisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MutableLiveData<String> urlLiveData = new MutableLiveData<>();
        MutableLiveData<String> errLiveData = new MutableLiveData<>();
        storeRepo.getQrisImageUrl(urlLiveData, errLiveData);
        urlLiveData.observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                binding.etQrisUrl.setText(url);
                Glide.with(this).load(url).into(binding.ivQrisPreview);
            }
        });

        binding.btnPreviewQris.setOnClickListener(v -> {
            String url = binding.etQrisUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                Glide.with(this).load(url).into(binding.ivQrisPreview);
            }
        });

        binding.btnSimpanQris.setOnClickListener(v -> {
            String url = binding.etQrisUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(getContext(), "URL tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            MutableLiveData<Boolean> successLd = new MutableLiveData<>();
            MutableLiveData<String> errLd = new MutableLiveData<>();
            storeRepo.saveQrisImageUrl(url, successLd, errLd);
            successLd.observe(getViewLifecycleOwner(), ok -> {
                if (Boolean.TRUE.equals(ok)) {
                    binding.tvQrisStatus.setVisibility(View.VISIBLE);
                    binding.tvQrisStatus.setText("QRIS berhasil disimpan!");
                }
            });
            errLd.observe(getViewLifecycleOwner(), err -> {
                if (err != null) Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
