package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Chào mừng (Trang ngoài)
 * Tự động khôi phục phiên đăng nhập và điều hướng theo vai trò (ADMIN, TEACHER, STUDENT)
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button btnDangNhap;
    private Button btnDangKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khôi phục phiên đăng nhập cũ nếu có
        khoiPhucPhienDangNhap();

        setContentView(R.layout.activity_welcome);
        khoiTaoViews();
        caiDatSuKien();
    }

    private void khoiPhucPhienDangNhap() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.daDangNhap()) {
            int userId = sessionManager.layUserId();
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            TaiKhoan taiKhoan = dbHelper.layTaiKhoanTheoId(userId);

            if (taiKhoan != null && taiKhoan.getTrangThai() == 1) {
                Intent intent;
                switch (taiKhoan.getVaiTro()) {
                    case "ADMIN":
                        intent = new Intent(WelcomeActivity.this, AdminHomeActivity.class);
                        break;
                    case "TEACHER":
                        intent = new Intent(WelcomeActivity.this, TeacherHomeActivity.class);
                        break;
                    case "STUDENT":
                    default:
                        intent = new Intent(WelcomeActivity.this, StudentHomeActivity.class);
                        break;
                }
                intent.putExtra("USER_ID", taiKhoan.getId());
                intent.putExtra("HO_TEN", taiKhoan.getHoTen());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Phiên đăng nhập hết hạn hoặc tài khoản bị khóa", Toast.LENGTH_SHORT).show();
                sessionManager.xoaPhienDangNhap();
            }
        }
    }

    private void khoiTaoViews() {
        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnDangKy = findViewById(R.id.btnDangKy);
    }

    private void caiDatSuKien() {
        btnDangNhap.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnDangKy.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
