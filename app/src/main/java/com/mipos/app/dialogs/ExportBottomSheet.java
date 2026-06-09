package com.mipos.app.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "ExportBottomSheet";
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
        if (binding == null) return;
        binding.btnExportPdf.setEnabled(false);
        binding.btnExportCsv.setEnabled(false);

        // Capture semua resource SEBELUM masuk background thread
        // Jangan pernah panggil requireContext()/requireActivity() di dalam executor!
        final android.content.Context ctx = requireContext().getApplicationContext();
        final android.app.Activity activity = requireActivity();
        final String authority = ctx.getPackageName() + ".provider";
        final String mimeType = type.equals("pdf") ? "application/pdf" : "text/csv";

        executor.execute(() -> {
            File file = null;
            try {
                file = type.equals("pdf")
                    ? ExportManager.exportPdf(ctx, "MiePos", period, transactions, totalPemasukan, byMethod)
                    : ExportManager.exportCsv(ctx, period, transactions, totalPemasukan, byMethod);

                if (file == null || !file.exists() || file.length() == 0) {
                    throw new IOException("File export kosong atau gagal dibuat");
                }

                final File finalFile = file;
                activity.runOnUiThread(() -> {
                    try {
                        android.net.Uri uri = FileProvider.getUriForFile(ctx, authority, finalFile);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType(mimeType);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Pakai activity.startActivity bukan fragment.startActivity
                        // agar tidak bergantung pada fragment lifecycle
                        activity.startActivity(Intent.createChooser(intent, "Bagikan Laporan"));
                        if (isAdded()) dismiss();
                    } catch (Exception shareEx) {
                        Log.e(TAG, "Gagal share file", shareEx);
                        Toast.makeText(ctx,
                            "Export OK tapi gagal share: " + shareEx.getMessage(),
                            Toast.LENGTH_LONG).show();
                        if (isAdded() && binding != null) {
                            binding.btnExportPdf.setEnabled(true);
                            binding.btnExportCsv.setEnabled(true);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Gagal buat file export", e);
                activity.runOnUiThread(() -> {
                    Toast.makeText(ctx,
                        "Gagal export: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                    if (isAdded() && binding != null) {
                        binding.btnExportPdf.setEnabled(true);
                        binding.btnExportCsv.setEnabled(true);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
        binding = null;
    }
}
