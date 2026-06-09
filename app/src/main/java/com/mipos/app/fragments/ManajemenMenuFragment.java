package com.mipos.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.mipos.app.adapters.ManajemenMenuAdapter;
import com.mipos.app.databinding.FragmentManajemenMenuBinding;
import com.mipos.app.dialogs.TambahEditMenuBottomSheet;
import com.mipos.app.models.MenuItem;
import com.mipos.app.viewmodel.ManajemenMenuViewModel;

public class ManajemenMenuFragment extends Fragment {
    private FragmentManajemenMenuBinding binding;
    private ManajemenMenuViewModel viewModel;
    private ManajemenMenuAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManajemenMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManajemenMenuViewModel.class);

        adapter = new ManajemenMenuAdapter(new ManajemenMenuAdapter.OnMenuActionListener() {
            @Override
            public void onEdit(MenuItem menu) {
                new TambahEditMenuBottomSheet(viewModel, menu)
                    .show(getChildFragmentManager(), "edit_menu");
            }
            @Override
            public void onToggleAvailable(MenuItem menu) {
                viewModel.toggleAvailable(menu);
            }
        });

        binding.rvMenuManajemen.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMenuManajemen.setAdapter(adapter);

        binding.fabTambahMenu.setOnClickListener(v ->
            new TambahEditMenuBottomSheet(viewModel, null)
                .show(getChildFragmentManager(), "tambah_menu"));

        viewModel.menusLiveData.observe(getViewLifecycleOwner(), menus ->
            adapter.setMenus(menus));

        viewModel.loadAllMenus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
