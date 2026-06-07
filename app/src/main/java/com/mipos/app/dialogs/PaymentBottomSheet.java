package com.mipos.app.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mipos.app.R;
import com.mipos.app.databinding.BottomSheetPaymentBinding;
import com.mipos.app.utils.CurrencyUtils;
import com.mipos.app.viewmodel.PosViewModel;

public class PaymentBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetPaymentBinding binding;
    private final PosViewModel viewModel;

    public PaymentBottomSheet(PosViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long total = viewModel.totalLiveData.getValue() != null
            ? viewModel.totalLiveData.getValue() : 0L;
        binding.tvPaymentTotal.setText(CurrencyUtils.format(total));

        binding.rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isCash = checkedId == binding.rbCash.getId();
            boolean isQris = checkedId == binding.rbQris.getId();
            binding.tilCashInput.setVisibility(isCash ? View.VISIBLE : View.GONE);
            binding.layoutKembalian.setVisibility(isCash ? View.VISIBLE : View.GONE);
            binding.tvQrisPlaceholder.setVisibility(isQris ? View.VISIBLE : View.GONE);
        });

        final long finalTotal = total;
        binding.etCashInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    long nominal = Long.parseLong(s.toString().trim());
                    long kembalian = nominal - finalTotal;
                    binding.tvKembalian.setText(CurrencyUtils.format(Math.max(kembalian, 0)));
                    binding.tvKembalian.setTextColor(
                        kembalian >= 0
                            ? requireContext().getColor(R.color.success)
                            : requireContext().getColor(R.color.error));
                } catch (NumberFormatException e) {
                    binding.tvKembalian.setText("-");
                }
            }
            public void afterTextChanged(Editable s) {}
        });

        binding.btnConfirmPayment.setOnClickListener(v -> {
            String method;
            int checked = binding.rgPaymentMethod.getCheckedRadioButtonId();
            if (checked == binding.rbCash.getId()) method = "cash";
            else if (checked == binding.rbQris.getId()) method = "qris";
            else method = "debit";

            viewModel.processPayment(method);
        });

        viewModel.transactionSuccessLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "✅ Transaksi Berhasil!", Toast.LENGTH_SHORT).show();
                viewModel.clearCart();
                viewModel.transactionSuccessLiveData.setValue(null);
                dismiss();
            }
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
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
