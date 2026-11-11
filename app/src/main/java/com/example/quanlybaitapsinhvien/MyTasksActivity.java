package com.example.quanlybaitapsinhvien;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyTasksActivity extends AppCompatActivity {

    private TextView tvStudentInfo;
    private TextView btnLogout;
    private TextView tabStatistics;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);

        sessionManager = new SessionManager(this);
        tvStudentInfo = findViewById(R.id.tvStudentInfo);
        btnLogout = findViewById(R.id.btnLogout);
        tabStatistics = findViewById(R.id.tabStatistics);

        String email = sessionManager.layUserEmail();
        if (!email.isEmpty()){
            tvStudentInfo.setText("Sinh viên - " + email);
        }

        btnLogout.setOnClickListener(v -> {
            hienDialogDangXuat();
        });

        tabStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MyTasksActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
    }

    private void hienDialogDangXuat(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");
        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            sessionManager.xoaPhienDangNhap();
            Intent intent = new Intent(MyTasksActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }
}
