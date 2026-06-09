package com.mipos.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mipos.app.databinding.FragmentPrinterSettingsBinding;
import com.mipos.app.utils.PrinterManager;
import java.util.ArrayList;

public class PrinterSettingsFragment extends Fragment {
    private FragmentPrinterSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrinterSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PrinterManager pm = PrinterManager.getInstance();

        String mac = pm.getBluetoothMac(requireContext());
        String ip = pm.getWifiIp(requireContext());
        int port = pm.getWifiPort(requireContext());
        String storeName = pm.getStoreName(requireContext());

        if (mac != null) binding.etBtMac.setText(mac);
        if (ip != null) binding.etWifiIp.setText(ip);
        binding.etWifiPort.setText(String.valueOf(port));
        binding.etStoreName.setText(storeName);

        binding.btnSaveBluetooth.setOnClickListener(v -> {
            String macVal = binding.etBtMac.getText().toString().trim();
            String name = binding.etStoreName.getText().toString().trim();
            if (macVal.isEmpty()) {
                binding.tilBtMac.setError("MAC tidak boleh kosong");
                return;
            }
            binding.tilBtMac.setError(null);
            pm.saveBluetoothPrinter(requireContext(), macVal);
            if (!name.isEmpty()) pm.saveStoreName(requireContext(), name);
            Toast.makeText(getContext(), "Bluetooth printer disimpan", Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveWifi.setOnClickListener(v -> {
            String ipVal = binding.etWifiIp.getText().toString().trim();
            String portStr = binding.etWifiPort.getText().toString().trim();
            String name = binding.etStoreName.getText().toString().trim();
            if (ipVal.isEmpty()) {
                binding.tilWifiIp.setError("IP tidak boleh kosong");
                return;
            }
            binding.tilWifiIp.setError(null);
            int portVal = portStr.isEmpty() ? 9100 : Integer.parseInt(portStr);
            pm.saveWifiPrinter(requireContext(), ipVal, portVal);
            if (!name.isEmpty()) pm.saveStoreName(requireContext(), name);
            Toast.makeText(getContext(), "WiFi printer disimpan", Toast.LENGTH_SHORT).show();
        });

        binding.btnTestPrint.setOnClickListener(v -> {
            binding.tvPrinterStatus.setVisibility(View.VISIBLE);
            binding.tvPrinterStatus.setText("Mencetak test...");
            pm.print(requireContext(), new ArrayList<>(), 0, "test", 0,
                new PrinterManager.PrintCallback() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() ->
                            binding.tvPrinterStatus.setText("Test print berhasil!"));
                    }

                    @Override
                    public void onError(String message) {
                        requireActivity().runOnUiThread(() ->
                            binding.tvPrinterStatus.setText("Gagal: " + message));
                    }
                });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
