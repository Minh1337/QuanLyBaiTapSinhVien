package com.example.quanlybaitapsinhvien;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

public class CreateAssignmentActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int monHocId;

    private EditText etTitle;
    private EditText etPhanCum;
    private EditText etDesc;
    private EditText etHanNop;
    private EditText etAllowedExt;
    private TextView btnPickAttachment;
    private TextView btnRemoveAttachment;
    private TextView tvAttachmentInfo;
    private TextView btnSaveAssignment;

    private ActivityResultLauncher<String> pickAttachmentLauncher;
    private String pickedAttachmentPath = "";
    private String pickedAttachmentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_create_assignment);

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

        monHocId = getIntent().getIntExtra("MON_HOC_ID", 1);

        dangKyLauncherPickAttachment();
        khoiTaoViews();
        caiDatSuKien();
    }

    private void dangKyLauncherPickAttachment() {
        pickAttachmentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pickedAttachmentPath = saoChepDeBaiVaoBoNhoTrong(uri);
                if (tvAttachmentInfo != null) {
                    tvAttachmentInfo.setVisibility(View.VISIBLE);
                    tvAttachmentInfo.setText("📎 Tệp đính kèm đã chọn: " + pickedAttachmentName);
                }
                if (btnRemoveAttachment != null) {
                    btnRemoveAttachment.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Đã lưu tệp đề bài thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saoChepDeBaiVaoBoNhoTrong(Uri uri) {
        try {
            String fileName = "debai_" + System.currentTimeMillis() + ".pdf";
            String pathStr = uri.getPath();
            if (pathStr != null && pathStr.contains("/")) {
                fileName = pathStr.substring(pathStr.lastIndexOf('/') + 1);
            }
            pickedAttachmentName = fileName;

            InputStream is = getContentResolver().openInputStream(uri);
            File localFile = new File(getFilesDir(), "debai_" + System.currentTimeMillis() + "_" + fileName);
            FileOutputStream os = new FileOutputStream(localFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            if (is != null) is.close();
            return Uri.fromFile(localFile).toString();
        } catch (Exception e) {
            pickedAttachmentName = "tệp_đề_bài.pdf";
            return uri.toString();
        }
    }

    private void khoiTaoViews() {
        etTitle = findViewById(R.id.etTitle);
        etPhanCum = findViewById(R.id.etPhanCum);
        etDesc = findViewById(R.id.etDesc);
        etHanNop = findViewById(R.id.etHanNop);
        etAllowedExt = findViewById(R.id.etAllowedExt);
        btnPickAttachment = findViewById(R.id.btnPickAttachment);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);
        tvAttachmentInfo = findViewById(R.id.tvAttachmentInfo);
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment);

        if (etHanNop != null) {
            etHanNop.setFocusable(false);
            etHanNop.setClickable(true);
            etHanNop.setOnClickListener(v -> chonNgayGio(etHanNop));
        }
    }

    private void caiDatSuKien() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnPickAttachment != null) {
            btnPickAttachment.setOnClickListener(v -> {
                try {
                    pickAttachmentLauncher.launch("*/*");
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnRemoveAttachment != null) {
            btnRemoveAttachment.setOnClickListener(v -> {
                pickedAttachmentPath = "";
                pickedAttachmentName = "";
                tvAttachmentInfo.setVisibility(View.GONE);
                btnRemoveAttachment.setVisibility(View.GONE);
            });
        }

        if (btnSaveAssignment != null) {
            btnSaveAssignment.setOnClickListener(v -> luuBaiTapNopFile());
        }
    }

    private void chonNgayGio(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                        year, month + 1, dayOfMonth, hourOfDay, minute);
                target.setText(formatted);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void luuBaiTapNopFile() {
        String tieuDe = etTitle.getText().toString().trim();
        String phanCumInput = etPhanCum != null ? etPhanCum.getText().toString().trim() : "";
        String moTa = etDesc.getText().toString().trim();
        String hanNop = etHanNop.getText().toString().trim();
        String allowedExt = etAllowedExt.getText().toString().trim();

        if (TextUtils.isEmpty(tieuDe)) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề bài tập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(hanNop)) {
            hanNop = "2025-03-15 23:59";
        }

        String phanCum = !TextUtils.isEmpty(phanCumInput) ? phanCumInput : "BÀI TẬP NỘP FILE";
        if (!TextUtils.isEmpty(allowedExt)) {
            phanCum += " (Yêu cầu file: " + allowedExt + ")";
        }

        boolean ok = dbHelper.themNoiDungMonHoc(monHocId, tieuDe, NoiDungMonHoc.LOAI_NOP_BAI, pickedAttachmentPath, phanCum, 0, moTa, hanNop);
        if (ok) {
            Toast.makeText(this, "Tạo bài tập nộp file thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi tạo bài tập", Toast.LENGTH_SHORT).show();
        }
    }
}
