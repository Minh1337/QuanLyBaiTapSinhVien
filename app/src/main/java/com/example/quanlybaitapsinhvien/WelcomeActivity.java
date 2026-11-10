package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Chào mừng (Trang ngoài)
 * Hiển thị logo Q, câu chào mừng, tự động khôi phục phiên đăng nhập cũ nếu có
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button btnDangNhap;
    private Button btnDangKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra và khôi phục phiên đăng nhập cũ (nếu đã đăng nhập trước đó)
        khoiPhucPhienDangNhap();

        setContentView(R.layout.activity_welcome);

        // Khởi tạo và ánh xạ giao diện
        khoiTaoViews();

        // Cài đặt các sự kiện tương tác
        caiDatSuKien();
    }

    /**
     * Kiểm tra và khôi phục phiên đăng nhập cũ từ SharedPreferences và SQLite
     */
    private void khoiPhucPhienDangNhap() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.daDangNhap()) {
            int userId = sessionManager.layUserId();
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            TaiKhoan taiKhoan = dbHelper.layTaiKhoanTheoId(userId);

            // Nếu tài khoản tồn tại và đang ở trạng thái Hoạt động (1)
            if (taiKhoan != null && taiKhoan.getTrangThai() == 1) {
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                intent.putExtra("EMAIL", taiKhoan.getEmail());
                intent.putExtra("VAI_TRO", taiKhoan.getVaiTro());
                startActivity(intent);
                finish();
            } else {
                // Nếu tài khoản bị khóa hoặc không hợp lệ, xóa phiên đăng nhập cũ
                Toast.makeText(this, "Phiên đăng nhập hết hạn hoặc tài khoản đã bị khóa", Toast.LENGTH_LONG).show();
                sessionManager.xoaPhienDangNhap();
            }
        }
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
