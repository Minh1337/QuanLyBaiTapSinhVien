package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.session.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword, etPasswordConfirm;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etPasswordConfirm = findViewById(R.id.et_reg_password_confirm);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);

        btnRegister.setOnClickListener(v -> performRegister());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etPasswordConfirm.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            etPasswordConfirm.setError("Mật khẩu xác nhận không trùng khớp");
            etPasswordConfirm.requestFocus();
            return;
        }

        long newUserId = dbHelper.registerStudent(username, password, fullName);
        if (newUserId == -1) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác!", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.createLoginSession(newUserId, username, fullName, "STUDENT");

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.class.getName());
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
