package com.mipos.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import com.mipos.app.models.Transaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExportManager {

    public static File exportPdf(Context context, String storeName, String period,
                                  List<Transaction> transactions,
                                  long totalPemasukan, Map<String, Long> byMethod) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.parseColor("#FF6B35"));

        Paint normalPaint = new Paint();
        normalPaint.setTextSize(11f);
        normalPaint.setColor(Color.BLACK);

        Paint boldPaint = new Paint();
        boldPaint.setTextSize(11f);
        boldPaint.setFakeBoldText(true);
        boldPaint.setColor(Color.BLACK);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1f);

        int y = 50;
        canvas.drawText(storeName.isEmpty() ? "MiePos" : storeName, 40, y, titlePaint);
        y += 25;
        canvas.drawText("Laporan " + period, 40, y, normalPaint);
        y += 15;
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Dicetak: " + now, 40, y, normalPaint);
        y += 10;
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 20;

        canvas.drawText("Total Pemasukan: " + CurrencyUtils.format(totalPemasukan), 40, y, boldPaint);
        y += 18;
        canvas.drawText("Total Transaksi: " + transactions.size(), 40, y, normalPaint);
        y += 18;
        long cash = byMethod.getOrDefault("cash", 0L);
        long qris = byMethod.getOrDefault("qris", 0L);
        long debit = byMethod.getOrDefault("debit", 0L);
        canvas.drawText("Cash: " + CurrencyUtils.format(cash) +
            "   QRIS: " + CurrencyUtils.format(qris) +
            "   Debit: " + CurrencyUtils.format(debit), 40, y, normalPaint);
        y += 10;
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 20;

        // Table header
        canvas.drawText("No", 40, y, boldPaint);
        canvas.drawText("Waktu", 70, y, boldPaint);
        canvas.drawText("Kasir", 180, y, boldPaint);
        canvas.drawText("Metode", 320, y, boldPaint);
        canvas.drawText("Total", 440, y, boldPaint);
        y += 5;
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 14;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        int no = 1;
        for (Transaction t : transactions) {
            if (y > 800) break; // simple overflow guard
            canvas.drawText(String.valueOf(no++), 40, y, normalPaint);
            String time = t.getTimestamp() != null ? sdf.format(t.getTimestamp().toDate()) : "-";
            canvas.drawText(time, 70, y, normalPaint);
            String kasir = t.getKasirName() != null ? t.getKasirName() : "-";
            if (kasir.length() > 14) kasir = kasir.substring(0, 14);
            canvas.drawText(kasir, 180, y, normalPaint);
            canvas.drawText(t.getPaymentMethod() != null ? t.getPaymentMethod() : "-", 320, y, normalPaint);
            canvas.drawText(CurrencyUtils.format(t.getTotal()), 440, y, normalPaint);
            y += 16;
        }

        document.finishPage(page);

        File dir = getExportDir(context);
        String filename = "laporan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File file = new File(dir, filename);
        document.writeTo(new FileOutputStream(file));
        document.close();
        return file;
    }

    public static File exportCsv(Context context, String period,
                                  List<Transaction> transactions,
                                  long totalPemasukan, Map<String, Long> byMethod) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Laporan MiePos - ").append(period).append("\n");
        sb.append("Total Pemasukan,").append(totalPemasukan).append("\n");
        sb.append("Total Transaksi,").append(transactions.size()).append("\n");
        sb.append("Cash,").append(byMethod.getOrDefault("cash", 0L)).append("\n");
        sb.append("QRIS,").append(byMethod.getOrDefault("qris", 0L)).append("\n");
        sb.append("Debit,").append(byMethod.getOrDefault("debit", 0L)).append("\n\n");
        sb.append("No,Waktu,Kasir,Metode Bayar,Total\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        int no = 1;
        for (Transaction t : transactions) {
            String time = t.getTimestamp() != null ? sdf.format(t.getTimestamp().toDate()) : "-";
            String kasir = t.getKasirName() != null ? t.getKasirName().replace(",", " ") : "-";
            sb.append(no++).append(",")
              .append(time).append(",")
              .append(kasir).append(",")
              .append(t.getPaymentMethod() != null ? t.getPaymentMethod() : "-").append(",")
              .append(t.getTotal()).append("\n");
        }

        File dir = getExportDir(context);
        String filename = "laporan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes("UTF-8"));
        }
        return file;
    }

    private static File getExportDir(Context context) {
        File base = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (base == null) base = context.getFilesDir(); // fallback ke internal storage
        File dir = new File(base, "MiePos");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
