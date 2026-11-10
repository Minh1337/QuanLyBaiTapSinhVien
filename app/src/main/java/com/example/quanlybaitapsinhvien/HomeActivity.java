package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Trang chủ chính (HomeActivity)
 * Thiết kế chuẩn theo giao diện Figma với Icon Q góc trên, tiêu đề "Trang chủ"
 * và thanh điều hướng dưới cùng gồm 3 ô trống + tab Profile
 */
public class HomeActivity extends AppCompatActivity {

    private View tabProfile;
    private String email = "";
    private String vaiTro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo và ánh xạ view
        khoiTaoViews();

        // Đọc dữ liệu tài khoản gửi từ LoginActivity
        docDuLieuTruyenVao();

        // Cài đặt sự kiện tương tác
        caiDatSuKien();
    }

    /**
     * Khởi tạo và liên kết các thành phần giao diện trên XML
     */
    private void khoiTaoViews() {
        tabProfile = findViewById(R.id.tabProfile);
    }

    /**
     * Đọc Email và Vai trò gửi từ màn hình Đăng nhập
     */
    private void docDuLieuTruyenVao() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("EMAIL")) {
                email = intent.getStringExtra("EMAIL");
            }
            if (intent.hasExtra("VAI_TRO")) {
                vaiTro = intent.getStringExtra("VAI_TRO");
            }
        }
    }

    /**
     * Cài đặt lắng nghe sự kiện click cho các tab điều hướng
     */
    private void caiDatSuKien() {
        // Sự kiện khi bấm vào tab Profile góc dưới bên phải
        tabProfile.setOnClickListener(v -> hienThongTinProfile());
    }

    /**
     * Hiển thị Hộp thoại thông tin Profile người dùng và nút Đăng xuất
     */
    private void hienThongTinProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin cá nhân (Profile)");
        builder.setMessage("Email: " + (email.isEmpty() ? "Chưa đăng nhập" : email) +
                "\nVai trò: " + (vaiTro.isEmpty() ? "N/A" : vaiTro));

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> dangXuatTaiKhoan());
        builder.setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Thực hiện đăng xuất tài khoản và quay về màn hình Chào mừng (WelcomeActivity)
     */
    private void dangXuatTaiKhoan() {
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
