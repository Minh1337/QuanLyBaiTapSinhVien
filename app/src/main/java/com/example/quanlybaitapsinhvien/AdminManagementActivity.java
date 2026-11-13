package com.example.quanlybaitapsinhvien;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminManagementActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_admin_management);

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            final int initialPaddingLeft = mainLayout.getPaddingLeft();
            final int initialPaddingTop = mainLayout.getPaddingTop();
            final int initialPaddingRight = mainLayout.getPaddingRight();
            final int initialPaddingBottom = mainLayout.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        initialPaddingLeft,
                        systemBars.top + initialPaddingTop,
                        initialPaddingRight,
                        systemBars.bottom + initialPaddingBottom
                );
                return insets;
            });
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        EditText etTeacherEmail = findViewById(R.id.etTeacherEmail);
        EditText etTeacherPassword = findViewById(R.id.etTeacherPassword);
        TextView btnCreateTeacher = findViewById(R.id.btnCreateTeacher);

        if (btnCreateTeacher != null) {
            btnCreateTeacher.setOnClickListener(v -> {
                String email = etTeacherEmail.getText().toString().trim();
                String pass = etTeacherPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                    Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ok = dbHelper.taoTaiKhoanGiaoVien(email, pass);
                if (ok) {
                    Toast.makeText(this, "Tạo tài khoản Giáo viên thành công!", Toast.LENGTH_SHORT).show();
                    etTeacherEmail.setText("");
                    etTeacherPassword.setText("");
                } else {
                    Toast.makeText(this, "Tài khoản đã tồn tại hoặc lỗi", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
