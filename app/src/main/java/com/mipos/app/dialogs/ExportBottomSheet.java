package com.mipos.app.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mipos.app.databinding.BottomSheetExportBinding;
import com.mipos.app.models.Transaction;
import com.mipos.app.utils.ExportManager;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetExportBinding binding;
    private final List<Transaction> transactions;
    private final long totalPemasukan;
    private final Map<String, Long> byMethod;
    private final String period;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExportBottomSheet(List<Transaction> transactions, long totalPemasukan,
                              Map<String, Long> byMethod, String period) {
        this.transactions = transactions;
        this.totalPemasukan = totalPemasukan;
        this.byMethod = byMethod;
        this.period = period;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnExportPdf.setOnClickListener(v -> exportAndShare("pdf"));
        binding.btnExportCsv.setOnClickListener(v -> exportAndShare("csv"));
    }

    private void exportAndShare(String type) {
        binding.btnExportPdf.setEnabled(false);
        binding.btnExportCsv.setEnabled(false);
        executor.execute(() -> {
            try {
                File file = type.equals("pdf")
                    ? ExportManager.exportPdf(requireContext(), "MiePos", period, transactions, totalPemasukan, byMethod)
                    : ExportManager.exportCsv(requireContext(), period, transactions, totalPemasukan, byMethod);

                requireActivity().runOnUiThread(() -> {
                    shareFile(file, type.equals("pdf") ? "application/pdf" : "text/csv");
                    dismiss();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Gagal export: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnExportPdf.setEnabled(true);
                    binding.btnExportCsv.setEnabled(true);
                });
            }
        });
    }

    private void shareFile(File file, String mimeType) {
        android.net.Uri uri = FileProvider.getUriForFile(requireContext(),
            requireContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Bagikan Laporan"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
