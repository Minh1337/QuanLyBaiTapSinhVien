package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TienDoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tien_do);

        // Xử lý WindowInsets để giao diện không đè lên Status Bar (cột pin, sóng)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTenBai = findViewById(R.id.tvTenBai);
        TextView tvTrangThai = findViewById(R.id.tvTrangThai);

        Intent intent = getIntent();
        if (intent != null) {
            String tenBai = intent.getStringExtra("TEN_BAI");
            if (tenBai != null) {
                tvTenBai.setText(tenBai);
            }
        }
    }
}
