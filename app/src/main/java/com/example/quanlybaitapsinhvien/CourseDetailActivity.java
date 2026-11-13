package com.example.quanlybaitapsinhvien;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CourseDetailActivity extends AppCompatActivity {
    private SessionManager session;
    private DatabaseHelper dbHelper;

    private int monHocId;
    private String maMon;
    private String tenMon;
    private String namHoc;

    private ImageView btnBack;
    private TextView tvCourseTitle;
    private TextView tvCourseSubtitle;
    private TextView tvHeroNamHoc;
    private TextView tvHeroTenMon;
    private TextView tvHeroGiaoVien;
    private LinearLayout actionTeacherRow;

    private EditText etSearch;
    private TextView btnSort;
    private LinearLayout filterChipContainer;
    private LinearLayout contentListContainer;

    private ActivityResultLauncher<String> pickCourseFileLauncher;
    private String currentPickedCourseFilePath = "";
    private String currentPickedCourseFileName = "";

    private TextView dialogTvFileInfo;
    private TextView dialogBtnRemoveFile;

    private final List<NoiDungMonHoc> danhSachGoc = new ArrayList<>();
    private String tuKhoaTimKiem = "";
    private String loaiLocHienTai = "ALL";
    private String cheDoSapXep = "NEWEST"; // NEWEST, OLDEST, NAME_ASC, NAME_DESC

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_course_detail);

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

        Intent intent = getIntent();
        monHocId = intent.getIntExtra("MON_HOC_ID", -1);
        maMon = intent.getStringExtra("MA_MON");
        tenMon = intent.getStringExtra("TEN_MON");
        namHoc = intent.getStringExtra("NAM_HOC");

        dangKyLauncherPickCourseFile();
        khoiTaoViews();
        caiDatSuKien();
    }

    private void dangKyLauncherPickCourseFile() {
        pickCourseFileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                currentPickedCourseFileName = getFileNameFromUri(uri);
                currentPickedCourseFilePath = saoChepCourseFileVaoBoNhoTrong(uri, currentPickedCourseFileName);

                if (dialogTvFileInfo != null) {
                    dialogTvFileInfo.setVisibility(View.VISIBLE);
                    dialogTvFileInfo.setText("📎 Tệp đã chọn từ máy: " + currentPickedCourseFileName);
                }
                if (dialogBtnRemoveFile != null) {
                    dialogBtnRemoveFile.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Đã chọn tệp thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return "TaiLieu_" + System.currentTimeMillis() % 10000 + ".pdf";
    }

    private String saoChepCourseFileVaoBoNhoTrong(Uri uri, String fileName) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File localFile = new File(getFilesDir(), "coursefile_" + System.currentTimeMillis() + "_" + fileName);
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

    @Override
    protected void onResume() {
        super.onResume();
        hienThiThongTinMonHoc();
    }

    private void khoiTaoViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseSubtitle = findViewById(R.id.tvCourseSubtitle);
        tvHeroNamHoc = findViewById(R.id.tvHeroNamHoc);
        tvHeroTenMon = findViewById(R.id.tvHeroTenMon);
        tvHeroGiaoVien = findViewById(R.id.tvHeroGiaoVien);
        actionTeacherRow = findViewById(R.id.actionTeacherRow);

        etSearch = findViewById(R.id.etSearch);
        btnSort = findViewById(R.id.btnSort);
        filterChipContainer = findViewById(R.id.filterChipContainer);
        contentListContainer = findViewById(R.id.contentListContainer);
    }

    private void caiDatSuKien() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tuKhoaTimKiem = s.toString().trim().toLowerCase(Locale.ROOT);
                    apDungLocVaSapXep();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> hienThiDialogSapXep());
        }

        khoiTaoDanhSachChipsLoc();
    }

    private void khoiTaoDanhSachChipsLoc() {
        filterChipContainer.removeAllViews();
        String[][] chips = {
                {"ALL", "Tất cả"},
                {"PDF", "📄 PDF"},
                {"VIDEO", "🎬 Video"},
                {"LINK", "🔗 Link"},
                {"TRAC_NGHIEM", "📝 Bài thi"},
                {"NOP_BAI", "📤 Bài tập"},
                {"COMPLETED", "✓ Đã xong"},
                {"INCOMPLETE", "⏳ Chưa xong"}
        };

        for (String[] chipData : chips) {
            final String code = chipData[0];
            final String label = chipData[1];

            TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
            params.rightMargin = dp(8);
            chip.setLayoutParams(params);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(12), 0, dp(12), 0);
            chip.setText(label);
            chip.setTextSize(13);
            chip.setClickable(true);
            chip.setFocusable(true);

            capNhatGiaoDienChip(chip, code.equals(loaiLocHienTai));

            chip.setOnClickListener(v -> {
                loaiLocHienTai = code;
                khoiTaoDanhSachChipsLoc();
                apDungLocVaSapXep();
            });

            filterChipContainer.addView(chip);
        }
    }

    private void capNhatGiaoDienChip(TextView chip, boolean isActive) {
        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_blue_button);
            chip.setTextColor(0xFFFFFFFF);
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_home_pill);
            chip.setTextColor(0xFF603374);
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void hienThiDialogSapXep() {
        String[] options = {
                "Mới nhất đến cũ nhất",
                "Cũ nhất đến mới nhất",
                "Tên A - Z",
                "Tên Z - A"
        };
        int selectedIndex = "OLDEST".equals(cheDoSapXep) ? 1 :
                "NAME_ASC".equals(cheDoSapXep) ? 2 :
                        "NAME_DESC".equals(cheDoSapXep) ? 3 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp danh sách")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    switch (which) {
                        case 0: cheDoSapXep = "NEWEST"; break;
                        case 1: cheDoSapXep = "OLDEST"; break;
                        case 2: cheDoSapXep = "NAME_ASC"; break;
                        case 3: cheDoSapXep = "NAME_DESC"; break;
                    }
                    apDungLocVaSapXep();
                    dialog.dismiss();
                })
                .show();
    }

    private void hienThiThongTinMonHoc() {
        if (maMon != null && tenMon != null) {
            tvCourseTitle.setText(maMon + " - " + tenMon);
            tvHeroTenMon.setText(maMon + " - " + tenMon);
        } else {
            tvCourseTitle.setText("Chi tiết môn học");
            tvHeroTenMon.setText("Hệ quản trị CSDL");
        }

        if (namHoc != null) {
            tvCourseSubtitle.setText(namHoc);
            tvHeroNamHoc.setText(namHoc);
        } else {
            tvCourseSubtitle.setText("NĂM HỌC 2025-2026");
            tvHeroNamHoc.setText("NĂM HỌC 2025-2026");
        }

        TaiKhoan account = dbHelper.layTaiKhoanTheoId(session.layUserId());
        String role = account != null ? account.getVaiTro() : "STUDENT";
        boolean isGiaoVien = "TEACHER".equals(role) || "ADMIN".equals(role);

        tvHeroGiaoVien.setText("Vai trò: " + (isGiaoVien ? "Giảng viên phụ trách" : "Sinh viên lớp học"));

        if (isGiaoVien) {
            actionTeacherRow.setVisibility(View.VISIBLE);
            actionTeacherRow.removeAllViews();
            themNutHanhDongGV(actionTeacherRow, "+ Đề thi", v -> {
                Intent intent = new Intent(CourseDetailActivity.this, CreateQuizActivity.class);
                intent.putExtra("MON_HOC_ID", monHocId);
                startActivity(intent);
            });
            themNutHanhDongGV(actionTeacherRow, "+ Bài tập file", v -> {
                Intent intent = new Intent(CourseDetailActivity.this, CreateAssignmentActivity.class);
                intent.putExtra("MON_HOC_ID", monHocId);
                startActivity(intent);
            });
            themNutHanhDongGV(actionTeacherRow, "+ Bài giảng/Link", v -> hienThiDialogThemBaiHocHoacLink());
        } else {
            actionTeacherRow.setVisibility(View.GONE);
        }

        int studentId = session.layUserId();
        List<NoiDungMonHoc> data = dbHelper.layNoiDungMonHoc(monHocId, studentId);
        danhSachGoc.clear();
        if (data != null) {
            danhSachGoc.addAll(data);
        }

        apDungLocVaSapXep();
    }

    private void apDungLocVaSapXep() {
        TaiKhoan account = dbHelper.layTaiKhoanTheoId(session.layUserId());
        String role = account != null ? account.getVaiTro() : "STUDENT";
        boolean isGiaoVien = "TEACHER".equals(role) || "ADMIN".equals(role);
        int studentId = session.layUserId();

        List<NoiDungMonHoc> listFiltered = new ArrayList<>();

        // 1. Lọc theo từ khóa tìm kiếm và loại/trạng thái
        for (NoiDungMonHoc item : danhSachGoc) {
            boolean matchSearch = TextUtils.isEmpty(tuKhoaTimKiem)
                    || (item.getTieuDe() != null && item.getTieuDe().toLowerCase(Locale.ROOT).contains(tuKhoaTimKiem))
                    || (item.getMoTa() != null && item.getMoTa().toLowerCase(Locale.ROOT).contains(tuKhoaTimKiem))
                    || (item.getPhanCum() != null && item.getPhanCum().toLowerCase(Locale.ROOT).contains(tuKhoaTimKiem));

            if (!matchSearch) continue;

            boolean matchFilter = false;
            switch (loaiLocHienTai) {
                case "ALL":
                    matchFilter = true;
                    break;
                case "PDF":
                    matchFilter = NoiDungMonHoc.LOAI_PDF.equals(item.getLoaiNoiDung());
                    break;
                case "VIDEO":
                    matchFilter = NoiDungMonHoc.LOAI_VIDEO.equals(item.getLoaiNoiDung());
                    break;
                case "LINK":
                    matchFilter = NoiDungMonHoc.LOAI_LINK.equals(item.getLoaiNoiDung());
                    break;
                case "TRAC_NGHIEM":
                    matchFilter = NoiDungMonHoc.LOAI_TRAC_NGHIEM.equals(item.getLoaiNoiDung());
                    break;
                case "NOP_BAI":
                    matchFilter = NoiDungMonHoc.LOAI_NOP_BAI.equals(item.getLoaiNoiDung());
                    break;
                case "COMPLETED":
                    matchFilter = item.isHoanThanh();
                    break;
                case "INCOMPLETE":
                    matchFilter = !item.isHoanThanh();
                    break;
            }

            if (matchFilter) {
                listFiltered.add(item);
            }
        }

        // 2. Sắp xếp danh sách
        Collections.sort(listFiltered, (o1, o2) -> {
            switch (cheDoSapXep) {
                case "OLDEST":
                    return Integer.compare(o1.getId(), o2.getId());
                case "NAME_ASC":
                    return o1.getTieuDe().compareToIgnoreCase(o2.getTieuDe());
                case "NAME_DESC":
                    return o2.getTieuDe().compareToIgnoreCase(o1.getTieuDe());
                case "NEWEST":
                default:
                    return Integer.compare(o2.getId(), o1.getId());
            }
        });

        // 3. Hiển thị danh sách lên giao diện
        contentListContainer.removeAllViews();
        if (!listFiltered.isEmpty()) {
            String lastPhanCum = "";
            for (NoiDungMonHoc item : listFiltered) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_course_content, contentListContainer, false);

                TextView tvPhanCum = itemView.findViewById(R.id.tvPhanCum);
                TextView tvTypeIcon = itemView.findViewById(R.id.tvTypeIcon);
                TextView tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
                TextView tvItemDesc = itemView.findViewById(R.id.tvItemDesc);
                TextView tvStatus = itemView.findViewById(R.id.tvStatus);
                TextView btnTeacherEdit = itemView.findViewById(R.id.btnTeacherEdit);
                TextView btnTeacherDelete = itemView.findViewById(R.id.btnTeacherDelete);
                TextView btnItemAction = itemView.findViewById(R.id.btnItemAction);

                if (!TextUtils.isEmpty(item.getPhanCum()) && !item.getPhanCum().equals(lastPhanCum)) {
                    tvPhanCum.setVisibility(View.VISIBLE);
                    tvPhanCum.setText(item.getPhanCum());
                    lastPhanCum = item.getPhanCum();
                } else {
                    tvPhanCum.setVisibility(View.GONE);
                }

                tvItemTitle.setText(item.getTieuDe());
                if (!TextUtils.isEmpty(item.getMoTa())) {
                    tvItemDesc.setVisibility(View.VISIBLE);
                    tvItemDesc.setText(item.getMoTa());
                } else {
                    tvItemDesc.setVisibility(View.GONE);
                }

                if (isGiaoVien) {
                    btnTeacherEdit.setVisibility(View.VISIBLE);
                    btnTeacherDelete.setVisibility(View.VISIBLE);

                    btnTeacherDelete.setOnClickListener(v -> xacNhanXoaNoiDung(item));
                    btnTeacherEdit.setOnClickListener(v -> {
                        if (NoiDungMonHoc.LOAI_TRAC_NGHIEM.equals(item.getLoaiNoiDung())) {
                            Intent intentEdit = new Intent(CourseDetailActivity.this, CreateQuizActivity.class);
                            intentEdit.putExtra("MON_HOC_ID", monHocId);
                            intentEdit.putExtra("DE_THI_ID", item.getIdThamChieu());
                            intentEdit.putExtra("NOI_DUNG_ID", item.getId());
                            startActivity(intentEdit);
                        } else {
                            hienThiDialogSuaNoiDung(item);
                        }
                    });
                } else {
                    btnTeacherEdit.setVisibility(View.GONE);
                    btnTeacherDelete.setVisibility(View.GONE);
                }

                String loai = item.getLoaiNoiDung();
                if (NoiDungMonHoc.LOAI_PDF.equals(loai)) {
                    tvTypeIcon.setText("📄");
                    tvStatus.setText(item.isHoanThanh() ? "✓ Đã xem" : "PDF · Tài liệu");
                    btnItemAction.setText("Xem PDF");
                    btnItemAction.setOnClickListener(v -> {
                        dbHelper.capNhatHoanThanh(studentId, item.getId(), true);
                        hienThiFileHoacUrl(item.getUrlFile(), item.getTieuDe());
                    });
                } else if (NoiDungMonHoc.LOAI_VIDEO.equals(loai)) {
                    tvTypeIcon.setText("🎬");
                    tvStatus.setText(item.isHoanThanh() ? "✓ Đã xem" : "Video · Bài giảng");
                    btnItemAction.setText("Xem Video");
                    btnItemAction.setOnClickListener(v -> {
                        dbHelper.capNhatHoanThanh(studentId, item.getId(), true);
                        moLinkWeb(item.getUrlFile());
                    });
                } else if (NoiDungMonHoc.LOAI_LINK.equals(loai)) {
                    tvTypeIcon.setText("🔗");
                    tvStatus.setText(item.isHoanThanh() ? "✓ Đã mở" : "Liên kết ngoài");
                    btnItemAction.setText("Mở Link");
                    btnItemAction.setOnClickListener(v -> {
                        dbHelper.capNhatHoanThanh(studentId, item.getId(), true);
                        moLinkWeb(item.getUrlFile());
                    });
                } else if (NoiDungMonHoc.LOAI_TRAC_NGHIEM.equals(loai)) {
                    tvTypeIcon.setText("📝");
                    DeThi deThiInfo = dbHelper.layDeThiTheoId(item.getIdThamChieu());
                    String hanText;
                    if (deThiInfo != null) {
                        if (!TextUtils.isEmpty(deThiInfo.getThoiGianBatDau()) && !TextUtils.isEmpty(deThiInfo.getThoiGianKetThuc())) {
                            hanText = "Hạn: " + deThiInfo.getThoiGianBatDau() + " - " + deThiInfo.getThoiGianKetThuc();
                        } else if (!TextUtils.isEmpty(deThiInfo.getThoiGianKetThuc())) {
                            hanText = "Hạn nộp: " + deThiInfo.getThoiGianKetThuc();
                        } else if (!TextUtils.isEmpty(deThiInfo.getThoiGianBatDau())) {
                            hanText = "Mở từ: " + deThiInfo.getThoiGianBatDau();
                        } else if (!TextUtils.isEmpty(item.getHanNop())) {
                            hanText = "Hạn: " + item.getHanNop();
                        } else {
                            hanText = "Trắc nghiệm (" + deThiInfo.getThoiGianPhut() + " phút)";
                        }
                    } else {
                        hanText = !TextUtils.isEmpty(item.getHanNop()) ? "Hạn: " + item.getHanNop() : "Trắc nghiệm đếm giờ";
                    }

                    if (isGiaoVien) {
                        tvStatus.setText("⏱ " + (deThiInfo != null ? deThiInfo.getThoiGianPhut() : 15) + " phút · " + hanText);
                        btnItemAction.setText("✏ Sửa đề");
                        btnItemAction.setOnClickListener(v -> {
                            Intent intentEdit = new Intent(CourseDetailActivity.this, CreateQuizActivity.class);
                            intentEdit.putExtra("MON_HOC_ID", monHocId);
                            intentEdit.putExtra("DE_THI_ID", item.getIdThamChieu());
                            intentEdit.putExtra("NOI_DUNG_ID", item.getId());
                            startActivity(intentEdit);
                        });
                    } else {
                        double diemThi = dbHelper.layDiemThiCuaSinhVien(studentId, item.getIdThamChieu());
                        if (diemThi >= 0.0) {
                            tvStatus.setText("✓ Điểm đạt: " + diemThi + "/10.0 · " + hanText);
                        } else {
                            tvStatus.setText(item.isHoanThanh() ? "✓ Đã làm bài · " + hanText : hanText);
                        }
                        btnItemAction.setText("Vào thi");
                        btnItemAction.setOnClickListener(v -> {
                            Intent quizIntent = new Intent(CourseDetailActivity.this, QuizActivity.class);
                            quizIntent.putExtra("DE_THI_ID", item.getIdThamChieu());
                            quizIntent.putExtra("NOI_DUNG_ID", item.getId());
                            startActivity(quizIntent);
                        });
                    }
                } else if (NoiDungMonHoc.LOAI_NOP_BAI.equals(loai)) {
                    tvTypeIcon.setText("📤");
                    String hanNopText = !TextUtils.isEmpty(item.getHanNop()) ? "Hạn: " + item.getHanNop() : "Bài tập nộp file";
                    tvStatus.setText(item.isHoanThanh() ? "✓ Đã nộp bài (" + hanNopText + ")" : hanNopText);
                    btnItemAction.setText(isGiaoVien ? "Xem bài nộp" : "Nộp bài");
                    btnItemAction.setOnClickListener(v -> {
                        Intent submitIntent = new Intent(CourseDetailActivity.this, SubmitAssignmentActivity.class);
                        submitIntent.putExtra("NOI_DUNG_ID", item.getId());
                        submitIntent.putExtra("TIEU_DE", item.getTieuDe());
                        submitIntent.putExtra("IS_GIAO_VIEN", isGiaoVien);
                        startActivity(submitIntent);
                    });
                } else {
                    tvTypeIcon.setText("📌");
                    tvStatus.setText("");
                    btnItemAction.setText("Chi tiết");
                    btnItemAction.setOnClickListener(v -> Toast.makeText(this, item.getTieuDe(), Toast.LENGTH_SHORT).show());
                }

                contentListContainer.addView(itemView);
            }
        } else {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không tìm thấy bài học / bài tập nào phù hợp.");
            tvEmpty.setTextColor(0xFF715D72);
            tvEmpty.setTextSize(14);
            tvEmpty.setPadding(0, 30, 0, 30);
            tvEmpty.setGravity(Gravity.CENTER);
            contentListContainer.addView(tvEmpty);
        }
    }

    private void xacNhanXoaNoiDung(NoiDungMonHoc item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa mục này?")
                .setMessage("Bạn có chắc chắn muốn xóa mục '" + item.getTieuDe() + "' khỏi môn học?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (NoiDungMonHoc.LOAI_TRAC_NGHIEM.equals(item.getLoaiNoiDung())) {
                        dbHelper.xoaDeThi(item.getIdThamChieu());
                    }
                    dbHelper.xoaNoiDungMonHoc(item.getId());
                    Toast.makeText(this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                    hienThiThongTinMonHoc();
                })
                .setNegativeButton("Hủy", null)
                .show();
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

    private void hienThiDialogSuaNoiDung(NoiDungMonHoc item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa bài học / Mục");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_course_item, null);

        final RadioGroup rgLoaiNoiDung = dialogView.findViewById(R.id.rgLoaiNoiDung);
        final RadioButton rbPdf = dialogView.findViewById(R.id.rbPdf);
        final RadioButton rbVideo = dialogView.findViewById(R.id.rbVideo);
        final RadioButton rbLink = dialogView.findViewById(R.id.rbLink);

        final EditText etTieuDe = dialogView.findViewById(R.id.etTieuDe);
        final EditText etPhanCum = dialogView.findViewById(R.id.etPhanCum);
        final TextView btnPickFile = dialogView.findViewById(R.id.btnPickFile);
        final TextView btnRemoveFile = dialogView.findViewById(R.id.btnRemoveFile);
        final TextView tvFileInfo = dialogView.findViewById(R.id.tvFileInfo);
        final EditText etUrlInput = dialogView.findViewById(R.id.etUrlInput);
        final EditText etMoTa = dialogView.findViewById(R.id.etMoTa);
        final EditText etHanNop = dialogView.findViewById(R.id.etHanNop);

        currentPickedCourseFilePath = "";
        currentPickedCourseFileName = "";
        dialogTvFileInfo = tvFileInfo;
        dialogBtnRemoveFile = btnRemoveFile;

        etTieuDe.setText(item.getTieuDe());
        etPhanCum.setText(item.getPhanCum());
        etMoTa.setText(item.getMoTa());
        etHanNop.setText(item.getHanNop());
        etUrlInput.setText(item.getUrlFile());

        if (NoiDungMonHoc.LOAI_VIDEO.equals(item.getLoaiNoiDung())) {
            rbVideo.setChecked(true);
        } else if (NoiDungMonHoc.LOAI_LINK.equals(item.getLoaiNoiDung())) {
            rbLink.setChecked(true);
        } else {
            rbPdf.setChecked(true);
        }

        if (etHanNop != null) {
            etHanNop.setFocusable(false);
            etHanNop.setClickable(true);
            etHanNop.setOnClickListener(v -> chonNgayGio(etHanNop));
        }

        if (!TextUtils.isEmpty(item.getUrlFile())) {
            if (item.getUrlFile().startsWith("content://") || item.getUrlFile().startsWith("file://")) {
                tvFileInfo.setVisibility(View.VISIBLE);
                btnRemoveFile.setVisibility(View.VISIBLE);
                tvFileInfo.setText("📎 Tệp đính kèm hiện tại: " + item.getUrlFile());
            }
        }

        btnPickFile.setOnClickListener(v -> {
            try {
                pickCourseFileLauncher.launch("*/*");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoveFile.setOnClickListener(v -> {
            currentPickedCourseFilePath = "";
            etUrlInput.setText("");
            tvFileInfo.setVisibility(View.GONE);
            btnRemoveFile.setVisibility(View.GONE);
        });

        TextView btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        TextView btnDialogSave = dialogView.findViewById(R.id.btnDialogSave);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (btnDialogCancel != null) btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnDialogSave != null) {
            btnDialogSave.setText("Cập nhật");
            btnDialogSave.setOnClickListener(v -> {
                String title = etTieuDe.getText().toString().trim();
                String phanCum = etPhanCum.getText().toString().trim();
                String moTa = etMoTa.getText().toString().trim();
                String han = etHanNop.getText().toString().trim();
                String urlOrPath = !TextUtils.isEmpty(currentPickedCourseFilePath) ? currentPickedCourseFilePath : etUrlInput.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(this, "Tiêu đề không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                String loai = NoiDungMonHoc.LOAI_PDF;
                if (rbVideo.isChecked()) loai = NoiDungMonHoc.LOAI_VIDEO;
                else if (rbLink.isChecked()) loai = NoiDungMonHoc.LOAI_LINK;

                if (urlOrPath.contains("youtube.com") || urlOrPath.contains("youtu.be") || urlOrPath.contains("vimeo.com") || urlOrPath.toLowerCase().endsWith(".mp4")) {
                    loai = NoiDungMonHoc.LOAI_VIDEO;
                } else if (rbPdf.isChecked() && (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://"))) {
                    loai = NoiDungMonHoc.LOAI_LINK;
                }

                dbHelper.capNhatNoiDungMonHoc(item.getId(), title, loai, phanCum, moTa, urlOrPath, han);
                Toast.makeText(this, "Đã cập nhật bài học!", Toast.LENGTH_SHORT).show();
                hienThiThongTinMonHoc();
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void hienThiDialogThemBaiHocHoacLink() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm bài giảng / Link / Tài liệu mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_course_item, null);

        final RadioGroup rgLoaiNoiDung = dialogView.findViewById(R.id.rgLoaiNoiDung);
        final RadioButton rbPdf = dialogView.findViewById(R.id.rbPdf);
        final RadioButton rbVideo = dialogView.findViewById(R.id.rbVideo);
        final RadioButton rbLink = dialogView.findViewById(R.id.rbLink);

        final EditText etTieuDe = dialogView.findViewById(R.id.etTieuDe);
        final EditText etPhanCum = dialogView.findViewById(R.id.etPhanCum);
        final TextView btnPickFile = dialogView.findViewById(R.id.btnPickFile);
        final TextView btnRemoveFile = dialogView.findViewById(R.id.btnRemoveFile);
        final TextView tvFileInfo = dialogView.findViewById(R.id.tvFileInfo);
        final EditText etUrlInput = dialogView.findViewById(R.id.etUrlInput);
        final EditText etMoTa = dialogView.findViewById(R.id.etMoTa);
        final EditText etHanNop = dialogView.findViewById(R.id.etHanNop);
        final TextView btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        final TextView btnDialogSave = dialogView.findViewById(R.id.btnDialogSave);

        currentPickedCourseFilePath = "";
        currentPickedCourseFileName = "";
        dialogTvFileInfo = tvFileInfo;
        dialogBtnRemoveFile = btnRemoveFile;

        if (etHanNop != null) {
            etHanNop.setFocusable(false);
            etHanNop.setClickable(true);
            etHanNop.setOnClickListener(v -> chonNgayGio(etHanNop));
        }

        btnPickFile.setOnClickListener(v -> {
            try {
                pickCourseFileLauncher.launch("*/*");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoveFile.setOnClickListener(v -> {
            currentPickedCourseFilePath = "";
            etUrlInput.setText("");
            tvFileInfo.setVisibility(View.GONE);
            btnRemoveFile.setVisibility(View.GONE);
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (btnDialogCancel != null) btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnDialogSave != null) {
            btnDialogSave.setText("Tạo bài học");
            btnDialogSave.setOnClickListener(v -> {
                String title = etTieuDe.getText().toString().trim();
                String phanCum = etPhanCum.getText().toString().trim();
                String moTa = etMoTa.getText().toString().trim();
                String han = etHanNop.getText().toString().trim();
                String urlOrPath = !TextUtils.isEmpty(currentPickedCourseFilePath) ? currentPickedCourseFilePath : etUrlInput.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                    return;
                }

                String loai = NoiDungMonHoc.LOAI_PDF;
                if (rbVideo != null && rbVideo.isChecked()) loai = NoiDungMonHoc.LOAI_VIDEO;
                else if (rbLink != null && rbLink.isChecked()) loai = NoiDungMonHoc.LOAI_LINK;

                if (urlOrPath.contains("youtube.com") || urlOrPath.contains("youtu.be") || urlOrPath.contains("vimeo.com") || urlOrPath.toLowerCase().endsWith(".mp4")) {
                    loai = NoiDungMonHoc.LOAI_VIDEO;
                } else if (rbPdf != null && rbPdf.isChecked() && (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://"))) {
                    loai = NoiDungMonHoc.LOAI_LINK;
                }

                if (TextUtils.isEmpty(phanCum)) phanCum = "BÀI GIẢNG & TÀI LIỆU";

                dbHelper.themNoiDungMonHoc(monHocId, title, loai, urlOrPath, phanCum, 0, moTa, han);
                Toast.makeText(this, "Tạo bài học thành công!", Toast.LENGTH_SHORT).show();
                hienThiThongTinMonHoc();
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void themNutHanhDongGV(LinearLayout parent, String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
        if (parent.getChildCount() > 0) params.leftMargin = dp(8);
        btn.setLayoutParams(params);
        btn.setBackgroundResource(R.drawable.bg_home_pill);
        btn.setGravity(Gravity.CENTER);
        btn.setText(text);
        btn.setTextColor(0xFF603374);
        btn.setTextSize(13);
        btn.setClickable(true);
        btn.setFocusable(true);
        btn.setOnClickListener(listener);
        parent.addView(btn);
    }

    private void moLinkWeb(String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Liên kết không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở liên kết: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    private void hienThiFileHoacUrl(String urlOrFile, String tieuDe) {
        if (TextUtils.isEmpty(urlOrFile)) {
            Toast.makeText(this, "Tệp tin/đường dẫn không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (urlOrFile.startsWith("http://") || urlOrFile.startsWith("https://")) {
            moLinkWeb(urlOrFile);
            return;
        }

        try {
            File file = null;
            if (urlOrFile.startsWith("file://")) {
                Uri parsedUri = Uri.parse(urlOrFile);
                if (parsedUri.getPath() != null) {
                    file = new File(parsedUri.getPath());
                }
            } else if (urlOrFile.startsWith("content://")) {
                Uri uri = Uri.parse(urlOrFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, getMimeTypeFromFileName(urlOrFile));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Mở tài liệu: " + tieuDe));
                return;
            } else {
                file = new File(urlOrFile);
            }

            if (file != null && file.exists()) {
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(contentUri, getMimeTypeFromFileName(file.getName()));
                openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(openIntent, "Mở tài liệu: " + tieuDe));
            } else {
                Toast.makeText(this, "Đang mở tài liệu: " + tieuDe, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Không có ứng dụng phù hợp để xem tệp (" + tieuDe + ")", Toast.LENGTH_LONG).show();
        }
    }

    private String getMimeTypeFromFileName(String fileName) {
        if (fileName == null) return "*/*";
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) return "image/*";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "application/msword";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) return "application/zip";
        if (lower.endsWith(".mp4")) return "video/mp4";
        return "*/*";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
