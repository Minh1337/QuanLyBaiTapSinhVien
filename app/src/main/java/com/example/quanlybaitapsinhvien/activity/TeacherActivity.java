package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

public class TeacherActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        sessionManager = new SessionManager(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        TextView tvInfo = findViewById(R.id.tv_teacher_info);
        ImageButton btnLogout = findViewById(R.id.btn_teacher_logout);

        tvInfo.setText("Xin chào Giáo viên: " + sessionManager.getFullName() + " (" + sessionManager.getUsername() + ")");

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
