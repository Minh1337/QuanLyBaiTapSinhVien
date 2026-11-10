package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Đăng ký tài khoản
 * Cho phép người dùng tạo tài khoản mới bằng Email, Mật khẩu và Xác nhận mật khẩu, lưu dữ liệu vào SQLite
 */
public class RegisterActivity extends AppCompatActivity {

    private View btnQuayLai;
    private EditText etEmail;
    private EditText etMatKhau;
    private EditText etXacNhanMatKhau;
    private Button btnThucHienDangKy;

    // Đối tượng quản lý CSDL SQLite
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo và ánh xạ giao diện
        khoiTaoViews();

        // Cài đặt sự kiện tương tác
        caiDatSuKien();
    }

    /**
     * Khởi tạo và liên kết các view trên layout XML
     */
    private void khoiTaoViews() {
        btnQuayLai = findViewById(R.id.btnQuayLai);
        etEmail = findViewById(R.id.etEmail);
        etMatKhau = findViewById(R.id.etMatKhau);
        etXacNhanMatKhau = findViewById(R.id.etXacNhanMatKhau);
        btnThucHienDangKy = findViewById(R.id.btnThucHienDangKy);
    }

    /**
     * Cài đặt các lắng nghe sự kiện click cho nút Quay lại và nút Đăng ký
     */
    private void caiDatSuKien() {
        // Sự kiện nút Quay lại góc trên bên trái
        btnQuayLai.setOnClickListener(v -> quayLaiTrangTruoc());

        // Sự kiện khi bấm nút Đăng ký
        btnThucHienDangKy.setOnClickListener(v -> xuLyDangKy());
    }

    /**
     * Xử lý kiểm tra dữ liệu, lưu tài khoản vào SQLite và hiển thị thông báo
     */
    private void xuLyDangKy() {
        String email = etEmail.getText().toString().trim();
        String matKhau = etMatKhau.getText().toString().trim();
        String xacNhanMatKhau = etXacNhanMatKhau.getText().toString().trim();

        if (!kiemTraDauVao(email, matKhau, xacNhanMatKhau)) {
            return;
        }

        // Kiểm tra xem email đã được đăng ký chưa
        if (dbHelper.kiemTraEmailTonTai(email)) {
            etEmail.setError("Email này đã được sử dụng!");
            etEmail.requestFocus();
            return;
        }

        // Thực hiện lưu tài khoản mới vào CSDL SQLite (mặc định vai trò STUDENT)
        boolean ketQua = dbHelper.dangKyTaiKhoan(email, matKhau);
        if (ketQua) {
            // Hiển thị AlertDialog thông báo đăng ký thành công
            hienDialogThanhCong();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào trong các ô nhập
     * @param email Địa chỉ email
     * @param matKhau Mật khẩu
     * @param xacNhanMatKhau Mật khẩu xác nhận
     * @return true nếu dữ liệu hợp lệ, false nếu không hợp lệ
     */
    private boolean kiemTraDauVao(String email, String matKhau, String xacNhanMatKhau) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập địa chỉ email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không đúng định dạng");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(matKhau)) {
            etMatKhau.setError("Vui lòng nhập mật khẩu");
            etMatKhau.requestFocus();
            return false;
        }

        if (matKhau.length() < 8) {
            etMatKhau.setError("Mật khẩu phải chứa ít nhất 8 ký tự");
            etMatKhau.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(xacNhanMatKhau)) {
            etXacNhanMatKhau.setError("Vui lòng xác nhận lại mật khẩu");
            etXacNhanMatKhau.requestFocus();
            return false;
        }

        if (!matKhau.equals(xacNhanMatKhau)) {
            etXacNhanMatKhau.setError("Mật khẩu xác nhận không trùng khớp");
            etXacNhanMatKhau.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Hiển thị AlertDialog thông báo đăng ký thành công ngay trên màn hình hiện tại
     */
    private void hienDialogThanhCong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_success, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Đặt nền Dialog trong suốt để hiển thị bo góc CardView chuẩn thiết kế
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Bấm nút Đồng ý để đóng Dialog và chuyển về trang Đăng nhập
        Button btnDongY = dialogView.findViewById(R.id.btnDongY);
        btnDongY.setOnClickListener(v -> {
            dialog.dismiss();
            chuyenVeDangNhap();
        });

        // Không cho phép đóng Dialog bằng cách bấm ra ngoài
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Chuyển về màn hình Đăng nhập (LoginActivity) sau khi đăng ký thành công
     */
    private void chuyenVeDangNhap() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void quayLaiTrangTruoc() {
        finish();
    }
}
