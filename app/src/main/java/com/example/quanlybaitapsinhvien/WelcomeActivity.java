package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Chào mừng (Trang ngoài)
 * Hiển thị logo Q, câu chào mừng và 2 nút lựa chọn Đăng nhập / Đăng ký
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button btnDangNhap;
    private Button btnDangKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Khởi tạo và ánh xạ giao diện
        khoiTaoViews();

        // Cài đặt các sự kiện tương tác
        caiDatSuKien();
    }

    /**
     * Khởi tạo và ánh xạ các phần tử giao diện từ layout XML
     */
    private void khoiTaoViews() {
        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnDangKy = findViewById(R.id.btnDangKy);
    }

    /**
     * Cài đặt các lắng nghe sự kiện click cho nút Đăng nhập và Đăng ký
     */
    private void caiDatSuKien() {
        // Sự kiện khi bấm nút Đăng nhập
        btnDangNhap.setOnClickListener(v -> chuyenDenDangNhap());

        // Sự kiện khi bấm nút Đăng ký
        btnDangKy.setOnClickListener(v -> chuyenDenDangKy());
    }

    /**
     * Chuyển sang màn hình Đăng nhập (LoginActivity)
     */
    private void chuyenDenDangNhap() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Chuyển sang màn hình Đăng ký (RegisterActivity)
     */
    private void chuyenDenDangKy() {
        Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
