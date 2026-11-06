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
import com.example.quanlybaitapsinhvien.model.User;
import com.example.quanlybaitapsinhvien.session.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        if (sessionManager.isLoggedIn()) {
            navigateByRole(sessionManager.getRole());
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);

        btnLogin.setOnClickListener(v -> performLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        User user = dbHelper.authenticateUser(username, password);
        if (user == null) {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            Toast.makeText(this, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!", Toast.LENGTH_LONG).show();
            return;
        }

        sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getFullName(), user.getRole());

        Toast.makeText(this, "Đăng nhập thành công! Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
        navigateByRole(user.getRole());
    }

    private void navigateByRole(String role) {
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, TeacherActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
