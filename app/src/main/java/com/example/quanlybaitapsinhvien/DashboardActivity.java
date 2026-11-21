package com.example.quanlybaitapsinhvien;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Dashboard Thống kê (DashboardActivity)
 * Hiển thị các khối thông số tổng quan (dành sẵn khu vực để bổ sung biểu đồ/thống kê sau)
 */
public class DashboardActivity extends AppCompatActivity {

    private View btnQuayLai;
    private TextView tvStatMonHoc;
    private TextView tvStatBaiTap;
    private TextView tvStatTienDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo và ánh xạ view
        khoiTaoViews();

        // Cài đặt lắng nghe sự kiện
        caiDatSuKien();
    }

    /**
     * Khởi tạo và ánh xạ các phần tử trên layout XML
     */
    private void khoiTaoViews() {
        btnQuayLai = findViewById(R.id.btnQuayLai);
        tvStatMonHoc = findViewById(R.id.tvStatMonHoc);
        tvStatBaiTap = findViewById(R.id.tvStatBaiTap);
        tvStatTienDo = findViewById(R.id.tvStatTienDo);
    }

    /**
     * Cài đặt các lắng nghe sự kiện bấm nút
     */
    private void caiDatSuKien() {
        // Bấm nút Quay lại
        btnQuayLai.setOnClickListener(v -> quayLaiTrangTruoc());
    }

    /**
     * Quay lại màn hình Trang chủ trước đó
     */
    private void quayLaiTrangTruoc() {
        finish();
    }
}
