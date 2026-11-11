package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {

    private TextView btnExportCsv;
    private TextView tabHome;
    private TextView tabAssignments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        btnExportCsv = findViewById(R.id.btnExportCsv);
        tabHome = findViewById(R.id.tabHome);
        tabAssignments = findViewById(R.id.tabAssignments);

        btnExportCsv.setOnClickListener(v -> {
            hienDialogXuatCsvThanhCong();
        });

        tabHome.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, MyTasksActivity.class);
            startActivity(intent);
            finish();
        });

        tabAssignments.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, MyTasksActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void hienDialogXuatCsvThanhCong(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xuất báo cáo CSV");
        builder.setMessage("Đã xuất danh sách bài tập ra file CSV thành công!");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }
}
