package com.mipos.app.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mipos.app.databinding.BottomSheetReceiptPreviewBinding;
import com.mipos.app.models.CartItem;
import com.mipos.app.utils.CurrencyUtils;
import com.mipos.app.utils.PrinterManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptPreviewBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetReceiptPreviewBinding binding;
    private final List<CartItem> items;
    private final long total;
    private final String paymentMethod;

    public ReceiptPreviewBottomSheet(List<CartItem> items, long total, String paymentMethod) {
        this.items = items;
        this.total = total;
        this.paymentMethod = paymentMethod;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetReceiptPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvReceiptContent.setText(buildReceiptText());

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnSkipPrint.setOnClickListener(v -> dismiss());

        binding.btnCetak.setOnClickListener(v -> {
            binding.btnCetak.setEnabled(false);
            android.content.Context ctx = requireContext().getApplicationContext();
            PrinterManager.getInstance().print(ctx, items, total, paymentMethod, 0,
                new PrinterManager.PrintCallback() {
                    @Override public void onSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(ctx, "Struk berhasil dicetak", Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                        }
                    }
                    @Override public void onError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(ctx, "Gagal cetak: " + message, Toast.LENGTH_LONG).show();
                                if (binding != null) binding.btnCetak.setEnabled(true);
                            });
                        }
                    }
                });
        });
    }

    private String buildReceiptText() {
        PrinterManager pm = PrinterManager.getInstance();
        String storeName = pm.getStoreName(requireContext());
        StringBuilder sb = new StringBuilder();
        String line32 = "--------------------------------";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        sb.append(center(storeName, 32)).append("\n");
        sb.append(center(date, 32)).append("\n");
        sb.append(line32).append("\n");

        for (CartItem item : items) {
            String name = item.getMenuName();
            if (name.length() > 20) name = name.substring(0, 20);
            sb.append(name).append("\n");
            String qtyPrice = "  " + item.getQty() + " x " + CurrencyUtils.format(item.getPrice());
            String subtotal = CurrencyUtils.format(item.getSubtotal());
            sb.append(padRight(qtyPrice, 24)).append(padLeft(subtotal, 8)).append("\n");
        }

        sb.append(line32).append("\n");
        sb.append(padRight("TOTAL", 24)).append(padLeft(CurrencyUtils.format(total), 8)).append("\n");
        sb.append(padRight("Metode", 24)).append(padLeft(paymentMethod.toUpperCase(), 8)).append("\n");
        sb.append(line32).append("\n");
        sb.append(center("Terima kasih!", 32)).append("\n");

        return sb.toString();
    }

    private String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(text);
        return sb.toString();
    }

    private String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private String padLeft(String text, int width) {
        if (text.length() >= width) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width - text.length(); i++) sb.append(' ');
        sb.append(text);
        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
