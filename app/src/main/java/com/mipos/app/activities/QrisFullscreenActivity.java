package com.mipos.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.mipos.app.R;
import com.mipos.app.utils.CurrencyUtils;

public class QrisFullscreenActivity extends AppCompatActivity {
    public static final String EXTRA_QRIS_URL = "qris_url";
    public static final String EXTRA_TOTAL = "total";
    public static final int RESULT_CONFIRMED = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qris_fullscreen);

        String qrisUrl = getIntent().getStringExtra(EXTRA_QRIS_URL);
        long total = getIntent().getLongExtra(EXTRA_TOTAL, 0L);

        ImageView ivQris = findViewById(R.id.ivQrisCode);
        TextView tvTotal = findViewById(R.id.tvQrisTotal);
        MaterialButton btnKonfirmasi = findViewById(R.id.btnKonfirmasiQris);
        MaterialButton btnBatal = findViewById(R.id.btnBatalQris);

        tvTotal.setText(CurrencyUtils.format(total));

        if (qrisUrl != null && !qrisUrl.isEmpty()) {
            Glide.with(this).load(qrisUrl).into(ivQris);
        } else {
            ivQris.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        btnKonfirmasi.setOnClickListener(v -> {
            setResult(RESULT_CONFIRMED);
            finish();
        });

        btnBatal.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
