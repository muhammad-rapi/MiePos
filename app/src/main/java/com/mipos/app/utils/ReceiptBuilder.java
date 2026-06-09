package com.mipos.app.utils;

import com.mipos.app.models.CartItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptBuilder {

    private static final byte[] ESC_INIT       = {0x1B, 0x40};
    private static final byte[] ALIGN_CENTER   = {0x1B, 0x61, 0x01};
    private static final byte[] ALIGN_LEFT     = {0x1B, 0x61, 0x00};
    private static final byte[] BOLD_ON        = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF       = {0x1B, 0x45, 0x00};
    private static final byte[] DOUBLE_SIZE_ON = {0x1D, 0x21, 0x11};
    private static final byte[] DOUBLE_SIZE_OFF= {0x1D, 0x21, 0x00};
    private static final byte[] FEED_4         = {0x1B, 0x64, 0x04};
    private static final byte[] CUT_FULL       = {0x1D, 0x56, 0x00};

    public static byte[] build(String storeName, List<CartItem> items,
                                long total, String paymentMethod,
                                long cashPaid, String kasirName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String name = (storeName == null || storeName.isEmpty()) ? "MiePos" : storeName;
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String method = (paymentMethod != null) ? paymentMethod.toUpperCase(Locale.getDefault()) : "-";

        baos.write(ESC_INIT);

        // Store name — centered, double size, bold
        baos.write(ALIGN_CENTER);
        baos.write(DOUBLE_SIZE_ON);
        baos.write(BOLD_ON);
        writeLine(baos, name);
        baos.write(BOLD_OFF);
        baos.write(DOUBLE_SIZE_OFF);

        baos.write(ALIGN_CENTER);
        writeLine(baos, "================================");

        baos.write(ALIGN_LEFT);
        writeLine(baos, "Tanggal : " + now);
        writeLine(baos, "Kasir   : " + kasirName);
        writeLine(baos, "--------------------------------");

        // Items
        for (CartItem item : items) {
            String itemName = item.getMenuName();
            if (itemName.length() > 20) itemName = itemName.substring(0, 20);
            writeLine(baos, itemName);
            String qtyPrice = "  " + item.getQty() + " x " + CurrencyUtils.format(item.getPrice());
            String subtotal = CurrencyUtils.format(item.getSubtotal());
            int pad = 32 - qtyPrice.length() - subtotal.length();
            StringBuilder sb = new StringBuilder(qtyPrice);
            for (int i = 0; i < Math.max(pad, 1); i++) sb.append(' ');
            sb.append(subtotal);
            writeLine(baos, sb.toString());
        }

        writeLine(baos, "--------------------------------");

        baos.write(BOLD_ON);
        writeLine(baos, String.format("%-16s%s", "TOTAL", CurrencyUtils.format(total)));
        baos.write(BOLD_OFF);

        writeLine(baos, String.format("%-16s%s", "Metode", method));

        if ("CASH".equals(method) && cashPaid > 0) {
            writeLine(baos, String.format("%-16s%s", "Bayar", CurrencyUtils.format(cashPaid)));
            long kembalian = cashPaid - total;
            writeLine(baos, String.format("%-16s%s", "Kembalian", CurrencyUtils.format(Math.max(kembalian, 0))));
        }

        baos.write(ALIGN_CENTER);
        writeLine(baos, "================================");
        writeLine(baos, "Terima kasih!");
        writeLine(baos, "Selamat makan :)");

        baos.write(FEED_4);
        baos.write(CUT_FULL);

        return baos.toByteArray();
    }

    private static void writeLine(ByteArrayOutputStream baos, String text) throws IOException {
        baos.write((text + "\n").getBytes("UTF-8"));
    }
}
