package com.example.quanlybaitapsinhvien.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.PersonalSubject;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

public class AddPersonalSubjectActivity extends AppCompatActivity {

    private EditText etSubjectCode, etSubjectName;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_subject);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        etSubjectCode = findViewById(R.id.et_subject_code);
        etSubjectName = findViewById(R.id.et_subject_name);
        Button btnSave = findViewById(R.id.btn_save_subject);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSubject());
    }

    private void saveSubject() {
        String code = etSubjectCode.getText().toString().trim();
        String name = etSubjectName.getText().toString().trim();

        if (code.isEmpty()) {
            etSubjectCode.setError("Vui lòng nhập mã môn học");
            etSubjectCode.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            etSubjectName.setError("Vui lòng nhập tên môn học");
            etSubjectName.requestFocus();
            return;
        }

        long studentId = sessionManager.getUserId();
        PersonalSubject subject = new PersonalSubject(0, code, name, studentId);
        long result = dbHelper.insertPersonalSubject(subject);

        if (result != -1) {
            Toast.makeText(this, "Thêm môn cá nhân thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi thêm môn học!", Toast.LENGTH_SHORT).show();
        }
    }
}
