package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Đăng nhập ứng dụng Sổ Bài Tập Sinh Viên
 * Hỗ trợ Đăng nhập chuẩn và nút ĐĂNG NHẬP NHANH DEMO cho 4 vai trò
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etTenDangNhap;
    private EditText etMatKhau;
    private ImageView ivAnHienMatKhau;
    private TextView tvDangKyNgay;
    private Button btnThucHienDangNhap;

    // Nút Đăng nhập nhanh Demo
    private Button btnDemoSvA;
    private Button btnDemoSvB;
    private Button btnDemoGiaoVien;
    private Button btnDemoAdmin;

    private boolean isPasswordVisible = false;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        khoiTaoViews();
        caiDatSuKien();
    }

    /**
     * Khởi tạo và ánh xạ các view giao diện
     */
    private void khoiTaoViews() {
        etTenDangNhap = findViewById(R.id.etTenDangNhap);
        etMatKhau = findViewById(R.id.etMatKhau);
        ivAnHienMatKhau = findViewById(R.id.ivAnHienMatKhau);
        tvDangKyNgay = findViewById(R.id.tvDangKyNgay);
        btnThucHienDangNhap = findViewById(R.id.btnThucHienDangNhap);

        btnDemoSvA = findViewById(R.id.btnDemoSvA);
        btnDemoSvB = findViewById(R.id.btnDemoSvB);
        btnDemoGiaoVien = findViewById(R.id.btnDemoGiaoVien);
        btnDemoAdmin = findViewById(R.id.btnDemoAdmin);
    }

    /**
     * Cài đặt các sự kiện click nút
     */
    private void caiDatSuKien() {
        // Sự kiện ẩn/hiện mật khẩu
        ivAnHienMatKhau.setOnClickListener(v -> toggleMatKhau());

        // Chuyển sang màn hình Đăng ký
        tvDangKyNgay.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Nút Đăng nhập chính
        btnThucHienDangNhap.setOnClickListener(v -> xuLyDangNhap());

        // Nút Đăng nhập nhanh Demo
        btnDemoSvA.setOnClickListener(v -> dangNhapNhanhDemo("sv_a", "123456"));
        btnDemoSvB.setOnClickListener(v -> dangNhapNhanhDemo("sv_b", "123456"));
        btnDemoGiaoVien.setOnClickListener(v -> dangNhapNhanhDemo("gv_nguyen", "123456"));
        btnDemoAdmin.setOnClickListener(v -> dangNhapNhanhDemo("admin", "admin123"));
    }

    /**
     * Xử lý ẩn/hiện mật khẩu trong ô nhập
     */
    private void toggleMatKhau() {
        if (isPasswordVisible) {
            etMatKhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivAnHienMatKhau.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            etMatKhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivAnHienMatKhau.setImageResource(R.drawable.ic_eye);
            isPasswordVisible = true;
        }
        etMatKhau.setSelection(etMatKhau.getText().length());
    }

    /**
     * Xử lý đăng nhập thông thường
     */
    private void xuLyDangNhap() {
        String username = etTenDangNhap.getText().toString().trim();
        String matKhau = etMatKhau.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etTenDangNhap.setError("Vui lòng nhập tên đăng nhập");
            etTenDangNhap.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(matKhau)) {
            etMatKhau.setError("Vui lòng nhập mật khẩu");
            etMatKhau.requestFocus();
            return;
        }

        TaiKhoan taiKhoan = dbHelper.kiemTraDangNhap(username, matKhau);
        if (taiKhoan == null) {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taiKhoan.getTrangThai() == 0) {
            Toast.makeText(this, "Tài khoản này hiện đã bị khóa!", Toast.LENGTH_LONG).show();
            return;
        }

        chuyenManHinhTheoVaiTro(taiKhoan);
    }

    /**
     * Đăng nhập nhanh cho các tài khoản Demo
     */
    private void dangNhapNhanhDemo(String username, String matKhau) {
        TaiKhoan taiKhoan = dbHelper.kiemTraDangNhap(username, matKhau);
        if (taiKhoan != null && taiKhoan.getTrangThai() == 1) {
            Toast.makeText(this, "⚡ Đăng nhập Demo thành công: " + taiKhoan.getHoTen(), Toast.LENGTH_SHORT).show();
            chuyenManHinhTheoVaiTro(taiKhoan);
        } else {
            Toast.makeText(this, "Lỗi tài khoản demo!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lưu phiên đăng nhập và mở màn hình theo vai trò (ADMIN, TEACHER, STUDENT)
     */
    private void chuyenManHinhTheoVaiTro(TaiKhoan taiKhoan) {
        sessionManager.taoPhienDangNhap(taiKhoan.getId(), taiKhoan.getEmail(), taiKhoan.getVaiTro());

        Intent intent;
        switch (taiKhoan.getVaiTro()) {
            case "ADMIN":
                intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                break;
            case "TEACHER":
                intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
                break;
            case "STUDENT":
            default:
                intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
                break;
        }
        intent.putExtra("USER_ID", taiKhoan.getId());
        intent.putExtra("HO_TEN", taiKhoan.getHoTen());
        startActivity(intent);
        finish();
    }
}
