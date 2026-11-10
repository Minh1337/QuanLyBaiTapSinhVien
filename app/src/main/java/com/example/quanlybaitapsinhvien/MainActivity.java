package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Chính của ứng dụng (điều hướng khởi đầu đến màn hình Chào mừng)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển trực tiếp sang màn hình Welcome (Chào mừng)
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
