package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager phien = new SessionManager(this);
        if (phien.daDangNhap()) {
            TaiKhoan taiKhoan = new DatabaseHelper(this).layTaiKhoanTheoId(phien.layUserId());
            if (taiKhoan != null && taiKhoan.getTrangThai() == 1) {
                moTrangChu();
                return;
            }
            phien.xoaPhienDangNhap();
        }

        setContentView(R.layout.activity_welcome);
        Button nutDangNhap = findViewById(R.id.btnDangNhap);
        nutDangNhap.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void moTrangChu() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
