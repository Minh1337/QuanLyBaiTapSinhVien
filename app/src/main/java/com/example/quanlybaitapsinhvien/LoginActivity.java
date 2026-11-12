package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Màn hình Đăng nhập
 * Kiểm tra tài khoản từ CSDL SQLite và phân quyền chuyển trang theo vai trò (ADMIN, TEACHER, STUDENT)
 */
public class LoginActivity extends AppCompatActivity {

    private View btnQuayLai;
    private EditText etTenDangNhap;
    private EditText etMatKhau;
    private TextView tvDangKyNgay;
    private Button btnThucHienDangNhap;

    // Đối tượng quản lý CSDL SQLite
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Xử lý WindowInsets để tránh bị che khuất bởi thanh trạng thái (status bar) và notch/camera
        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            final int initialPaddingLeft = mainLayout.getPaddingLeft();
            final int initialPaddingTop = mainLayout.getPaddingTop();
            final int initialPaddingRight = mainLayout.getPaddingRight();
            final int initialPaddingBottom = mainLayout.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        initialPaddingLeft,
                        systemBars.top + initialPaddingTop,
                        initialPaddingRight,
                        systemBars.bottom + initialPaddingBottom
                );
                return insets;
            });
        }

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo và ánh xạ các view trong giao diện
        khoiTaoViews();

        // Gán sự kiện tương tác cho các nút
        caiDatSuKien();
    }

    /**
     * Khởi tạo và liên kết các view với thành phần trên layout XML
     */
    private void khoiTaoViews() {
        btnQuayLai = findViewById(R.id.btnQuayLai);
        etTenDangNhap = findViewById(R.id.etTenDangNhap);
        etMatKhau = findViewById(R.id.etMatKhau);
        tvDangKyNgay = findViewById(R.id.tvDangKyNgay);
        btnThucHienDangNhap = findViewById(R.id.btnThucHienDangNhap);
    }

    /**
     * Cài đặt lắng nghe sự kiện click cho nút Quay lại, chữ Đăng ký và nút Đăng nhập
     */
    private void caiDatSuKien() {
        // Sự kiện khi bấm nút Quay lại góc trên bên trái
        btnQuayLai.setOnClickListener(v -> quayLaiTrangTruoc());

        // Sự kiện khi bấm vào chữ "Đăng ký" góc dưới bên phải phần mật khẩu
        tvDangKyNgay.setOnClickListener(v -> chuyenDenDangKy());

        // Sự kiện khi bấm nút "Đăng nhập"
        btnThucHienDangNhap.setOnClickListener(v -> xuLyDangNhap());

        // Đăng nhập nhanh bằng tài khoản Demo (1 tap)
        View btnDemoStudent = findViewById(R.id.btnDemoStudent);
        View btnDemoTeacher = findViewById(R.id.btnDemoTeacher);
        View btnDemoAdmin = findViewById(R.id.btnDemoAdmin);

        if (btnDemoStudent != null) {
            btnDemoStudent.setOnClickListener(v -> dangNhapNhanh("student@gmail.com", "student123"));
        }
        if (btnDemoTeacher != null) {
            btnDemoTeacher.setOnClickListener(v -> dangNhapNhanh("teacher@gmail.com", "teacher123"));
        }
        if (btnDemoAdmin != null) {
            btnDemoAdmin.setOnClickListener(v -> dangNhapNhanh("admin@gmail.com", "admin123"));
        }
    }

    private void dangNhapNhanh(String email, String matKhau) {
        etTenDangNhap.setText(email);
        etMatKhau.setText(matKhau);
        xuLyDangNhap();
    }

    /**
     * Kiểm tra thông tin nhập vào từ SQLite và phân quyền chuyển trang tương ứng
     */
    private void xuLyDangNhap() {
        String tenDangNhap = etTenDangNhap.getText().toString().trim();
        String matKhau = etMatKhau.getText().toString().trim();

        if (!kiemTraNhapLieu(tenDangNhap, matKhau)) {
            return;
        }

        // Kiểm tra tài khoản trong SQLite Database
        TaiKhoan taiKhoan = dbHelper.kiemTraDangNhap(tenDangNhap, matKhau);

        if (taiKhoan == null) {
            Toast.makeText(this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra trạng thái hoạt động của tài khoản
        if (taiKhoan.getTrangThai() == 0) {
            Toast.makeText(this, "Tài khoản của bạn hiện đã bị khóa!", Toast.LENGTH_LONG).show();
            return;
        }

        // Phân quyền điều hướng theo vai trò (Role) đọc từ SQLite
        phanQuyenDieuHuong(taiKhoan);
    }

    /**
     * Lưu phiên đăng nhập và mở màn hình Trang chủ tương ứng với vai trò
     * @param taiKhoan Đối tượng tài khoản vừa đăng nhập thành công
     */
    private void phanQuyenDieuHuong(TaiKhoan taiKhoan) {
        String vaiTro = taiKhoan.getVaiTro();
        Toast.makeText(this, "Đăng nhập thành công với vai trò " + vaiTro + "!", Toast.LENGTH_SHORT).show();

        // Lưu phiên đăng nhập vào SharedPreferences
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.taoPhienDangNhap(taiKhoan.getId(), taiKhoan.getEmail(), vaiTro);

        // Mở Trang chủ phân quyền HomeActivity
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("EMAIL", taiKhoan.getEmail());
        intent.putExtra("VAI_TRO", vaiTro);
        startActivity(intent);
        finish();
    }

    /**
     * Kiểm tra tính hợp lệ của thông tin đầu vào (không được bỏ trống)
     * @param tenDangNhap Tên đăng nhập hoặc Email người dùng đã nhập
     * @param matKhau Mật khẩu người dùng đã nhập
     * @return true nếu hợp lệ, false nếu thiếu thông tin
     */
    private boolean kiemTraNhapLieu(String tenDangNhap, String matKhau) {
        if (TextUtils.isEmpty(tenDangNhap)) {
            etTenDangNhap.setError("Vui lòng nhập tên đăng nhập hoặc email");
            etTenDangNhap.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(matKhau)) {
            etMatKhau.setError("Vui lòng nhập mật khẩu");
            etMatKhau.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Chuyển sang màn hình Đăng ký (RegisterActivity)
     */
    private void chuyenDenDangKy() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Quay lại màn hình trước đó
     */
    private void quayLaiTrangTruoc() {
        finish();
    }
}
