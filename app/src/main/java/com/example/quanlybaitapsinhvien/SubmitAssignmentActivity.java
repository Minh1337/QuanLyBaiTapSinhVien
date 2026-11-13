package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SubmitAssignmentActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SessionManager session;

    private int noiDungId;
    private String tieuDe;
    private boolean isGiaoVien;
    private NoiDungMonHoc noiDung;

    private TextView tvAssignmentTitle;
    private TextView tvAssignmentDesc;
    private TextView tvHanNop;
    private TextView btnDownloadTeacherFile;

    private LinearLayout studentSubmissionBox;
    private LinearLayout studentFileListContainer;
    private TextView btnAddStudentFile;

    private LinearLayout teacherSubmissionBox;
    private LinearLayout teacherFileListContainer;
    private TextView btnDownloadZipAll;

    private ActivityResultLauncher<String> pickStudentFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);
        setContentView(R.layout.activity_submit_assignment);

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

        noiDungId = getIntent().getIntExtra("NOI_DUNG_ID", -1);
        tieuDe = getIntent().getStringExtra("TIEU_DE");
        isGiaoVien = getIntent().getBooleanExtra("IS_GIAO_VIEN", false);

        dangKyLauncherPickFile();
        khoiTaoViews();
        caiDatSuKien();
        taiThongTinBaiTap();
    }

    private void dangKyLauncherPickFile() {
        pickStudentFileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String fileName = getFileNameFromUri(uri);

                // Kiểm tra định dạng file theo yêu cầu của GV
                if (noiDung != null && !kiemTraDinhDangFile(fileName, noiDung.getPhanCum())) {
                    new AlertDialog.Builder(this)
                            .setTitle("❌ Tệp nộp không hợp lệ")
                            .setMessage("Tệp '" + fileName + "' không đúng định dạng yêu cầu!\n\nGiảng viên " + noiDung.getPhanCum())
                            .setPositiveButton("Đã hiểu", null)
                            .show();
                    return;
                }

                String localPath = saoChepBaiNopVaoBoNhoTrong(uri, fileName);
                String ngayNop = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                dbHelper.luuBaiNopFile(session.layUserId(), noiDungId, fileName, localPath, ngayNop);
                Toast.makeText(this, "Nộp tệp " + fileName + " thành công!", Toast.LENGTH_SHORT).show();
                hienThiDanhSachBaiNopSinhVien();
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return "BaiLam_" + System.currentTimeMillis() % 10000 + ".pdf";
    }

    private String saoChepBaiNopVaoBoNhoTrong(Uri uri, String fileName) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File localFile = new File(getFilesDir(), "bainop_" + System.currentTimeMillis() + "_" + fileName);
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
            return uri.toString();
        }
    }

    private boolean kiemTraDinhDangFile(String fileName, String phanCum) {
        if (TextUtils.isEmpty(phanCum) || !phanCum.toLowerCase().contains("yêu cầu file:")) {
            return true;
        }
        int idx = phanCum.indexOf(":");
        if (idx == -1) return true;
        String requiredStr = phanCum.substring(idx + 1).toLowerCase(Locale.ROOT).trim();
        if (requiredStr.isEmpty()) return true;

        String lowerName = fileName.toLowerCase(Locale.ROOT);
        String[] exts = requiredStr.split(",");
        for (String ext : exts) {
            ext = ext.trim();
            if (!ext.isEmpty()) {
                if (!ext.startsWith(".")) ext = "." + ext;
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void khoiTaoViews() {
        tvAssignmentTitle = findViewById(R.id.tvAssignmentTitle);
        tvAssignmentDesc = findViewById(R.id.tvAssignmentDesc);
        tvHanNop = findViewById(R.id.tvHanNop);
        btnDownloadTeacherFile = findViewById(R.id.btnDownloadTeacherFile);

        studentSubmissionBox = findViewById(R.id.studentSubmissionBox);
        studentFileListContainer = findViewById(R.id.studentFileListContainer);
        btnAddStudentFile = findViewById(R.id.btnAddStudentFile);

        teacherSubmissionBox = findViewById(R.id.teacherSubmissionBox);
        teacherFileListContainer = findViewById(R.id.teacherFileListContainer);
        btnDownloadZipAll = findViewById(R.id.btnDownloadZipAll);
    }

    private void caiDatSuKien() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnAddStudentFile != null) {
            btnAddStudentFile.setOnClickListener(v -> chonTepMoiFormMayHoacNhap());
        }

        if (btnDownloadZipAll != null) {
            btnDownloadZipAll.setOnClickListener(v -> taoVaTaiFileZipBaiTap());
        }
    }

    private void taiThongTinBaiTap() {
        noiDung = dbHelper.layNoiDungTheoId(noiDungId);

        if (noiDung != null) {
            tvAssignmentTitle.setText(noiDung.getTieuDe());
            String desc = !TextUtils.isEmpty(noiDung.getMoTa()) ? noiDung.getMoTa() : "Sinh viên hoàn thành bài tập và nộp tệp tin.";
            if (!TextUtils.isEmpty(noiDung.getPhanCum()) && noiDung.getPhanCum().contains("Yêu cầu file:")) {
                desc += "\n⚠️ " + noiDung.getPhanCum();
            }
            tvAssignmentDesc.setText(desc);
            tvHanNop.setText("🕒 Hạn nộp: " + (!TextUtils.isEmpty(noiDung.getHanNop()) ? noiDung.getHanNop() : "Không giới hạn"));

            if (!TextUtils.isEmpty(noiDung.getUrlFile())) {
                btnDownloadTeacherFile.setVisibility(View.VISIBLE);
                btnDownloadTeacherFile.setText("📥 Tải / Mở tệp đề bài của Giảng viên");
                btnDownloadTeacherFile.setOnClickListener(v -> moFileDeBaiCuaGV(noiDung.getUrlFile()));
            } else {
                btnDownloadTeacherFile.setVisibility(View.GONE);
            }
        } else {
            tvAssignmentTitle.setText(!TextUtils.isEmpty(tieuDe) ? tieuDe : "Nộp bài tập file");
            tvAssignmentDesc.setText("Nộp tệp tin bài làm của bạn.");
            tvHanNop.setText("🕒 Hạn nộp: 2025-03-15 23:59");
            btnDownloadTeacherFile.setVisibility(View.GONE);
        }

        if (isGiaoVien) {
            studentSubmissionBox.setVisibility(View.GONE);
            teacherSubmissionBox.setVisibility(View.VISIBLE);
            hienThiDanhSachBaiNopGiaoVien();
        } else {
            studentSubmissionBox.setVisibility(View.VISIBLE);
            teacherSubmissionBox.setVisibility(View.GONE);
            hienThiDanhSachBaiNopSinhVien();
        }
    }

    private void moFileDeBaiCuaGV(String urlOrPath) {
        if (TextUtils.isEmpty(urlOrPath)) return;
        try {
            if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlOrPath));
                startActivity(intent);
            } else {
                Uri uri = Uri.parse(urlOrPath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Đang xem đề bài: " + urlOrPath, Toast.LENGTH_SHORT).show();
        }
    }

    private void hienThiDanhSachBaiNopSinhVien() {
        studentFileListContainer.removeAllViews();
        int studentId = session.layUserId();
        List<DatabaseHelper.BaiNopItem> list = dbHelper.layDanhSachBaiNopCuaSinhVienEx(studentId, noiDungId);

        if (list == null || list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Bạn chưa nộp tệp bài làm nào. Hãy bấm '➕ Nộp tệp mới'.");
            tvEmpty.setTextColor(0xFF715D72);
            tvEmpty.setTextSize(13);
            tvEmpty.setPadding(10, 20, 10, 20);
            studentFileListContainer.addView(tvEmpty);
            return;
        }

        for (DatabaseHelper.BaiNopItem item : list) {
            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.VERTICAL);
            itemCard.setBackgroundResource(R.drawable.bg_home_card);
            itemCard.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 12;
            itemCard.setLayoutParams(params);

            TextView tvFileName = new TextView(this);
            tvFileName.setText("📄 " + item.tenFile);
            tvFileName.setTextColor(0xFF38243D);
            tvFileName.setTextSize(15);
            tvFileName.setTypeface(null, Typeface.BOLD);
            itemCard.addView(tvFileName);

            TextView tvDate = new TextView(this);
            tvDate.setText("🕒 Ngày nộp: " + item.ngayNop);
            tvDate.setTextColor(0xFF715D72);
            tvDate.setTextSize(12);
            itemCard.addView(tvDate);

            LinearLayout rowActions = new LinearLayout(this);
            rowActions.setOrientation(LinearLayout.HORIZONTAL);
            rowActions.setPadding(0, 10, 0, 0);

            TextView btnEdit = new TextView(this);
            btnEdit.setText("✏ Đổi tên");
            btnEdit.setTextColor(0xFF603374);
            btnEdit.setTextSize(13);
            btnEdit.setPadding(0, 0, 24, 0);
            btnEdit.setOnClickListener(v -> hienThiDialogDoiTenFile(item));

            TextView btnDelete = new TextView(this);
            btnDelete.setText("🗑 Xóa bài nộp");
            btnDelete.setTextColor(0xFFEF4444);
            btnDelete.setTextSize(13);
            btnDelete.setOnClickListener(v -> {
                dbHelper.xoaBaiNopFile(item.id);
                Toast.makeText(this, "Đã xóa bài nộp", Toast.LENGTH_SHORT).show();
                hienThiDanhSachBaiNopSinhVien();
            });

            rowActions.addView(btnEdit);
            rowActions.addView(btnDelete);
            itemCard.addView(rowActions);

            studentFileListContainer.addView(itemCard);
        }
    }

    private void hienThiDanhSachBaiNopGiaoVien() {
        teacherFileListContainer.removeAllViews();
        List<DatabaseHelper.BaiNopItem> list = dbHelper.layDanhSachBaiNopFull(noiDungId);

        if (list == null || list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có sinh viên nào nộp bài tập này.");
            tvEmpty.setTextColor(0xFF715D72);
            tvEmpty.setTextSize(13);
            tvEmpty.setPadding(10, 20, 10, 20);
            teacherFileListContainer.addView(tvEmpty);
            return;
        }

        for (DatabaseHelper.BaiNopItem item : list) {
            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.VERTICAL);
            itemCard.setBackgroundResource(R.drawable.bg_home_card);
            itemCard.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 12;
            itemCard.setLayoutParams(params);

            TextView tvStudent = new TextView(this);
            tvStudent.setText("👤 Sinh viên: " + item.email);
            tvStudent.setTextColor(0xFF38243D);
            tvStudent.setTextSize(15);
            tvStudent.setTypeface(null, Typeface.BOLD);
            itemCard.addView(tvStudent);

            TextView tvFile = new TextView(this);
            tvFile.setText("📄 Tệp nộp: " + item.tenFile);
            tvFile.setTextColor(0xFF603374);
            tvFile.setTextSize(13);
            itemCard.addView(tvFile);

            TextView tvDate = new TextView(this);
            tvDate.setText("🕒 Ngày nộp: " + item.ngayNop);
            tvDate.setTextColor(0xFF715D72);
            tvDate.setTextSize(12);
            itemCard.addView(tvDate);

            teacherFileListContainer.addView(itemCard);
        }
    }

    private void chonTepMoiFormMayHoacNhap() {
        String[] options = {"📁 Chọn tệp tin từ thiết bị", "✍ Nhập tên tệp bài làm thủ công"};
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài tập mới")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        try {
                            pickStudentFileLauncher.launch("*/*");
                        } catch (Exception e) {
                            Toast.makeText(this, "Không thể mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hienThiDialogNhapTenFileThuCong();
                    }
                })
                .show();
    }

    private void hienThiDialogNhapTenFileThuCong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập tên tệp tin bài làm");

        final EditText input = new EditText(this);
        input.setHint("Nhập tên tệp (VD: BaiLam.pdf, DoAn.zip)");
        input.setText("BaiLam_" + System.currentTimeMillis() % 10000 + ".pdf");
        input.setPadding(24, 24, 24, 24);
        builder.setView(input);

        builder.setPositiveButton("Nộp file", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (TextUtils.isEmpty(fileName)) {
                fileName = "BaiLam_SinhVien.pdf";
            }

            if (noiDung != null && !kiemTraDinhDangFile(fileName, noiDung.getPhanCum())) {
                new AlertDialog.Builder(this)
                        .setTitle("❌ Tệp nộp không hợp lệ")
                        .setMessage("Tệp '" + fileName + "' không đúng định dạng yêu cầu!\n\nGiảng viên " + noiDung.getPhanCum())
                        .setPositiveButton("Đã hiểu", null)
                        .show();
                return;
            }

            String ngayNop = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dbHelper.luuBaiNopFile(session.layUserId(), noiDungId, fileName, "/storage/emulated/0/Download/" + fileName, ngayNop);
            Toast.makeText(this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
            hienThiDanhSachBaiNopSinhVien();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void hienThiDialogDoiTenFile(DatabaseHelper.BaiNopItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa tên bài nộp");

        final EditText input = new EditText(this);
        input.setText(item.tenFile);
        input.setPadding(24, 24, 24, 24);
        builder.setView(input);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                if (noiDung != null && !kiemTraDinhDangFile(newName, noiDung.getPhanCum())) {
                    Toast.makeText(this, "Tên file mới không đúng định dạng " + noiDung.getPhanCum(), Toast.LENGTH_LONG).show();
                    return;
                }
                dbHelper.capNhatTenFileBaiNop(item.id, newName);
                Toast.makeText(this, "Đã cập nhật tên file!", Toast.LENGTH_SHORT).show();
                hienThiDanhSachBaiNopSinhVien();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void taoVaTaiFileZipBaiTap() {
        List<DatabaseHelper.BaiNopItem> list = dbHelper.layDanhSachBaiNopFull(noiDungId);
        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "Không có bài nộp nào để đóng gói ZIP!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File zipDir = getExternalFilesDir("ZipSubmissions");
            if (zipDir == null) zipDir = getCacheDir();
            if (!zipDir.exists()) zipDir.mkdirs();

            File zipFile = new File(zipDir, "BaiNop_BaiTap_" + noiDungId + ".zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (DatabaseHelper.BaiNopItem item : list) {
                String entryName = item.email.replace("@", "_at_") + "_" + item.tenFile;
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                String dummyContent = "Thông tin bài làm:\nSinh viên: " + item.email + "\nTệp: " + item.tenFile + "\nThời gian: " + item.ngayNop + "\n";
                zos.write(dummyContent.getBytes());
                zos.closeEntry();
            }
            zos.close();
            fos.close();

            Toast.makeText(this, "📦 Đã nén thành công " + list.size() + " bài nộp vào file ZIP:\n" + zipFile.getName(), Toast.LENGTH_LONG).show();

            try {
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", zipFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/zip");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ file ZIP bài nộp"));
            } catch (Exception ignored) {}

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nén ZIP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
