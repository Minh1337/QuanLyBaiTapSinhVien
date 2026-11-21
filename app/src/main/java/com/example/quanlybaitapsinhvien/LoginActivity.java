package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText oEmail;
    private EditText oMatKhau;
    private DatabaseHelper coSoDuLieu;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);
        coSoDuLieu = new DatabaseHelper(this);
        oEmail = findViewById(R.id.etTenDangNhap);
        oMatKhau = findViewById(R.id.etMatKhau);
        Button nutDangNhap = findViewById(R.id.btnThucHienDangNhap);
        View nutQuayLai = findViewById(R.id.btnQuayLai);
        nutDangNhap.setOnClickListener(v -> dangNhap());
        nutQuayLai.setOnClickListener(v -> finish());
    }

    private void dangNhap() {
        String email = oEmail.getText().toString().trim();
        String matKhau = oMatKhau.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            oEmail.setError("Vui lòng nhập email");
            oEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(matKhau)) {
            oMatKhau.setError("Vui lòng nhập mật khẩu");
            oMatKhau.requestFocus();
            return;
        }

        TaiKhoan taiKhoan = coSoDuLieu.kiemTraDangNhap(email, matKhau);
        if (taiKhoan == null) {
            Toast.makeText(this, "Email hoặc mật khẩu không đúng. Tài khoản do quản trị viên cấp.", Toast.LENGTH_LONG).show();
            return;
        }
        if (taiKhoan.getTrangThai() == 0) {
            Toast.makeText(this, "Tài khoản đã bị khóa. Hãy liên hệ quản trị viên.", Toast.LENGTH_LONG).show();
            return;
        }

        new SessionManager(this).taoPhienDangNhap(taiKhoan.getId(), taiKhoan.getEmail(), taiKhoan.getVaiTro());
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
