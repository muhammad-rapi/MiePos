package com.mipos.app.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mipos.app.activities.QrisFullscreenActivity;
import com.mipos.app.databinding.BottomSheetPaymentBinding;
import com.mipos.app.repository.StoreRepository;
import com.mipos.app.utils.CurrencyUtils;
import com.mipos.app.viewmodel.PosViewModel;

public class PaymentBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetPaymentBinding binding;
    private final PosViewModel viewModel;
    private long total;
    private String pendingQrisUrl;

    private ActivityResultLauncher<Intent> qrisLauncher;

    public PaymentBottomSheet(PosViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qrisLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == QrisFullscreenActivity.RESULT_CONFIRMED) {
                    viewModel.processPayment("qris");
                }
            });
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

        total = viewModel.totalLiveData.getValue() != null ? viewModel.totalLiveData.getValue() : 0L;
        binding.tvPaymentTotal.setText(CurrencyUtils.format(total));

        binding.tilCashInput.setVisibility(View.VISIBLE);
        binding.layoutKembalian.setVisibility(View.VISIBLE);

        // Prefetch QRIS URL
        MutableLiveData<String> urlLiveData = new MutableLiveData<>();
        new StoreRepository().getQrisImageUrl(urlLiveData, new MutableLiveData<>());
        urlLiveData.observe(getViewLifecycleOwner(), url -> pendingQrisUrl = url);

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
                            ? requireContext().getColor(com.mipos.app.R.color.success)
                            : requireContext().getColor(com.mipos.app.R.color.error));
                } catch (NumberFormatException e) {
                    binding.tvKembalian.setText("-");
                }
            }
            public void afterTextChanged(Editable s) {}
        });

        binding.btnConfirmPayment.setOnClickListener(v -> {
            int checked = binding.rgPaymentMethod.getCheckedRadioButtonId();
            if (checked == binding.rbQris.getId()) {
                Intent intent = new Intent(requireContext(), QrisFullscreenActivity.class);
                intent.putExtra(QrisFullscreenActivity.EXTRA_QRIS_URL, pendingQrisUrl);
                intent.putExtra(QrisFullscreenActivity.EXTRA_TOTAL, finalTotal);
                qrisLauncher.launch(intent);
                return;
            }

            String method;
            if (checked == binding.rbCash.getId()) method = "cash";
            else method = "debit";
            viewModel.processPayment(method);
        });

        viewModel.transactionSuccessLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Transaksi Berhasil!", Toast.LENGTH_SHORT).show();
                viewModel.clearCart();
                viewModel.transactionSuccessLiveData.setValue(null);
                dismiss();
            }
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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
