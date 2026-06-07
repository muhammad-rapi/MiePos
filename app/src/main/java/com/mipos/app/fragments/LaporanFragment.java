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
import com.mipos.app.adapters.TransactionAdapter;
import com.mipos.app.databinding.FragmentLaporanBinding;
import com.mipos.app.utils.CurrencyUtils;
import com.mipos.app.viewmodel.LaporanViewModel;

public class LaporanFragment extends Fragment {
    private FragmentLaporanBinding binding;
    private LaporanViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLaporanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LaporanViewModel.class);

        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTransactions.setAdapter(adapter);

        viewModel.totalTransaksi.observe(getViewLifecycleOwner(), count ->
            binding.tvTotalTransaksi.setText(String.valueOf(count)));

        viewModel.totalPemasukan.observe(getViewLifecycleOwner(), total ->
            binding.tvTotalPemasukan.setText(CurrencyUtils.format(total)));

        viewModel.byPaymentMethod.observe(getViewLifecycleOwner(), map -> {
            binding.tvCashTotal.setText(CurrencyUtils.format(map.getOrDefault("cash", 0L)));
            binding.tvQrisTotal.setText(CurrencyUtils.format(map.getOrDefault("qris", 0L)));
            binding.tvDebitTotal.setText(CurrencyUtils.format(map.getOrDefault("debit", 0L)));
        });

        viewModel.transactionsLiveData.observe(getViewLifecycleOwner(), transactions ->
            adapter.setTransactions(transactions));

        viewModel.loadTodayReport();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
