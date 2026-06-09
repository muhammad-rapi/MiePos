package com.mipos.app.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mipos.app.R;
import com.mipos.app.databinding.BottomSheetTambahEditMenuBinding;
import com.mipos.app.models.MenuItem;
import com.mipos.app.viewmodel.ManajemenMenuViewModel;

public class TambahEditMenuBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetTambahEditMenuBinding binding;
    private final ManajemenMenuViewModel viewModel;
    private final MenuItem existingMenu; // null = tambah baru

    public TambahEditMenuBottomSheet(ManajemenMenuViewModel viewModel, @Nullable MenuItem existingMenu) {
        this.viewModel = viewModel;
        this.existingMenu = existingMenu;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetTambahEditMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] kategoriList = {"mie_ayam", "minuman", "tambahan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, kategoriList);
        binding.actvKategori.setAdapter(adapter);

        if (existingMenu != null) {
            binding.tvJudul.setText("Edit Menu");
            binding.etNama.setText(existingMenu.getName());
            binding.etHarga.setText(String.valueOf(existingMenu.getPrice()));
            binding.actvKategori.setText(existingMenu.getCategory(), false);
            binding.switchAvailable.setChecked(existingMenu.isAvailable());
        }

        binding.btnSimpan.setOnClickListener(v -> {
            String nama = binding.etNama.getText().toString().trim();
            String hargaStr = binding.etHarga.getText().toString().trim();
            String kategori = binding.actvKategori.getText().toString().trim();

            if (nama.isEmpty()) { binding.tilNama.setError("Nama wajib diisi"); return; }
            if (hargaStr.isEmpty()) { binding.tilHarga.setError("Harga wajib diisi"); return; }

            long harga;
            try { harga = Long.parseLong(hargaStr); } catch (NumberFormatException e) {
                binding.tilHarga.setError("Harga tidak valid"); return;
            }

            MenuItem menu = existingMenu != null ? existingMenu : new MenuItem();
            menu.setName(nama);
            menu.setPrice(harga);
            menu.setCategory(kategori.isEmpty() ? "mie_ayam" : kategori);
            menu.setAvailable(binding.switchAvailable.isChecked());

            if (existingMenu == null) {
                viewModel.addMenu(menu);
            } else {
                viewModel.updateMenu(menu);
            }
        });

        viewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.successLiveData.setValue(null);
                viewModel.loadAllMenus();
                dismiss();
            }
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                viewModel.errorLiveData.setValue(null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
