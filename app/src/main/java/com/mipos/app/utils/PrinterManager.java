package com.mipos.app.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import com.mipos.app.models.CartItem;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrinterManager {
    private static final String PREF_NAME = "printer_prefs";
    private static final String KEY_TYPE = "printer_type";
    private static final String KEY_BT_MAC = "bt_mac";
    private static final String KEY_WIFI_IP = "wifi_ip";
    private static final String KEY_WIFI_PORT = "wifi_port";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_KASIR_NAME = "kasir_name";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String TYPE_BLUETOOTH = "bluetooth";
    public static final String TYPE_WIFI = "wifi";

    private static PrinterManager instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrinterManager() {}

    public static PrinterManager getInstance() {
        if (instance == null) instance = new PrinterManager();
        return instance;
    }

    public interface PrintCallback {
        void onSuccess();
        void onError(String message);
    }

    public void saveBluetoothPrinter(Context ctx, String macAddress) {
        prefs(ctx).edit()
            .putString(KEY_TYPE, TYPE_BLUETOOTH)
            .putString(KEY_BT_MAC, macAddress)
            .apply();
    }

    public void saveWifiPrinter(Context ctx, String ip, int port) {
        prefs(ctx).edit()
            .putString(KEY_TYPE, TYPE_WIFI)
            .putString(KEY_WIFI_IP, ip)
            .putInt(KEY_WIFI_PORT, port)
            .apply();
    }

    public void saveStoreName(Context ctx, String name) {
        prefs(ctx).edit().putString(KEY_STORE_NAME, name).apply();
    }

    public String getStoreName(Context ctx) {
        return prefs(ctx).getString(KEY_STORE_NAME, "MiePos");
    }

    public String getPrinterType(Context ctx) {
        return prefs(ctx).getString(KEY_TYPE, null);
    }

    public String getBluetoothMac(Context ctx) {
        return prefs(ctx).getString(KEY_BT_MAC, null);
    }

    public String getWifiIp(Context ctx) {
        return prefs(ctx).getString(KEY_WIFI_IP, null);
    }

    public int getWifiPort(Context ctx) {
        return prefs(ctx).getInt(KEY_WIFI_PORT, 9100);
    }

    public void print(Context ctx, List<CartItem> items, long total,
                       String paymentMethod, long cashPaid, PrintCallback callback) {
        String type = getPrinterType(ctx);
        if (type == null) {
            callback.onError("Printer belum dikonfigurasi");
            return;
        }
        String storeName = getStoreName(ctx);
        executor.execute(() -> {
            try {
                OutputStream out;
                if (TYPE_BLUETOOTH.equals(type)) {
                    out = getBluetoothStream(ctx);
                } else {
                    out = getWifiStream(ctx);
                }
                byte[] data = ReceiptBuilder.build(storeName, items, total, paymentMethod, cashPaid, "Kasir");
                out.write(data);
                out.flush();
                out.close();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Gagal mencetak");
            }
        });
    }

    private OutputStream getBluetoothStream(Context ctx) throws Exception {
        String mac = getBluetoothMac(ctx);
        if (mac == null) throw new Exception("MAC address belum diset");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) throw new Exception("Bluetooth tidak tersedia");
        BluetoothDevice device = adapter.getRemoteDevice(mac);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
        adapter.cancelDiscovery();
        socket.connect();
        return socket.getOutputStream();
    }

    private OutputStream getWifiStream(Context ctx) throws Exception {
        String ip = getWifiIp(ctx);
        int port = getWifiPort(ctx);
        if (ip == null) throw new Exception("IP printer belum diset");
        Socket socket = new Socket(ip, port);
        socket.setSoTimeout(5000);
        return socket.getOutputStream();
    }

    private SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
