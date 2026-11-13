package com.example.quanlybaitapsinhvien;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateQuizActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int monHocId;
    private int deThiId = -1;
    private int noiDungId = -1;

    private EditText etQuizTitle;
    private EditText etPhanCum;
    private EditText etQuizTimer;
    private EditText etStartTime;
    private EditText etEndTime;
    private TextView btnSaveQuiz;
    private LinearLayout questionsPreviewContainer;

    private EditText etDialogTxtInput;
    private ActivityResultLauncher<String> pickTxtLauncher;

    private ActivityResultLauncher<String> pickImageLauncher;
    private String currentPickedImageUri = "";
    private ImageView currentDialogImgPreview;
    private TextView currentDialogBtnRemoveImage;

    private final List<CauHoiItem> danhSachCauHoi = new ArrayList<>();

    public static class CauHoiItem {
        public String noiDung;
        public String anhPath;
        public String loaiCauHoi; // SINGLE hoặc MULTI
        public String dapAnA;
        public String dapAnB;
        public String dapAnC;
        public String dapAnD;
        public String dapAnDung;

        public CauHoiItem(String noiDung, String anhPath, String loaiCauHoi, String dapAnA, String dapAnB, String dapAnC, String dapAnD, String dapAnDung) {
            this.noiDung = noiDung;
            this.anhPath = anhPath;
            this.loaiCauHoi = loaiCauHoi;
            this.dapAnA = dapAnA;
            this.dapAnB = dapAnB;
            this.dapAnC = dapAnC;
            this.dapAnD = dapAnD;
            this.dapAnDung = dapAnDung;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_create_quiz);

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
        deThiId = getIntent().getIntExtra("DE_THI_ID", -1);
        noiDungId = getIntent().getIntExtra("NOI_DUNG_ID", -1);

        dangKyLauncherPickTxt();
        dangKyLauncherPickImage();

        khoiTaoViews();
        caiDatSuKien();

        if (deThiId != -1) {
            taiThongTinDeThiDeSua();
        } else {
            napCauHoiMau();
        }
    }

    private void dangKyLauncherPickTxt() {
        pickTxtLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    reader.close();
                    if (inputStream != null) inputStream.close();

                    if (etDialogTxtInput != null) {
                        etDialogTxtInput.setText(builder.toString());
                    }
                    Toast.makeText(this, "Đã tải dữ liệu tệp TXT!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể đọc tệp TXT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dangKyLauncherPickImage() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                currentPickedImageUri = saoChepAnhVaoBoNhoTrong(uri);
                if (currentDialogImgPreview != null) {
                    hienThiAnhLenImageView(currentDialogImgPreview, currentPickedImageUri);
                }
                if (currentDialogBtnRemoveImage != null) {
                    currentDialogBtnRemoveImage.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Đã lưu ảnh thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saoChepAnhVaoBoNhoTrong(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File imgFile = new File(getFilesDir(), "img_cauhoi_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream os = new FileOutputStream(imgFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            if (is != null) is.close();
            return Uri.fromFile(imgFile).toString();
        } catch (Exception e) {
            return uri.toString();
        }
    }

    private void khoiTaoViews() {
        etQuizTitle = findViewById(R.id.etQuizTitle);
        etPhanCum = findViewById(R.id.etPhanCum);
        etQuizTimer = findViewById(R.id.etQuizTimer);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnSaveQuiz = findViewById(R.id.btnSaveQuiz);
        questionsPreviewContainer = findViewById(R.id.questionsPreviewContainer);

        if (etStartTime != null) {
            etStartTime.setFocusable(false);
            etStartTime.setClickable(true);
            etStartTime.setOnClickListener(v -> chonNgayGio(etStartTime));
        }
        if (etEndTime != null) {
            etEndTime.setFocusable(false);
            etEndTime.setClickable(true);
            etEndTime.setOnClickListener(v -> chonNgayGio(etEndTime));
        }
    }

    private void caiDatSuKien() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnImportTxt).setOnClickListener(v -> hienThiDialogImportTxt());
        findViewById(R.id.btnAddQuestionManual).setOnClickListener(v -> hienThiDialogThemCauHoi(-1));
        if (btnSaveQuiz != null) {
            btnSaveQuiz.setOnClickListener(v -> luuDeThiVaCauHoi());
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

    private void taiThongTinDeThiDeSua() {
        if (btnSaveQuiz != null) {
            btnSaveQuiz.setText("💾 Cập nhật đề thi trắc nghiệm");
        }

        DeThi deThi = dbHelper.layDeThiTheoId(deThiId);
        if (deThi != null) {
            etQuizTitle.setText(deThi.getTieuDe());
            etQuizTimer.setText(String.valueOf(deThi.getThoiGianPhut()));
            etStartTime.setText(deThi.getThoiGianBatDau());
            etEndTime.setText(deThi.getThoiGianKetThuc());
        }

        if (noiDungId != -1) {
            NoiDungMonHoc nd = dbHelper.layNoiDungTheoId(noiDungId);
            if (nd != null && !TextUtils.isEmpty(nd.getPhanCum())) {
                etPhanCum.setText(nd.getPhanCum());
            }
        }

        List<CauHoi> list = dbHelper.layDanhSachCauHoi(deThiId);
        danhSachCauHoi.clear();
        if (list != null && !list.isEmpty()) {
            for (CauHoi ch : list) {
                danhSachCauHoi.add(new CauHoiItem(
                        ch.getNoiDung(),
                        ch.getAnhPath(),
                        ch.getLoaiCauHoi(),
                        ch.getDapAnA(),
                        ch.getDapAnB(),
                        ch.getDapAnC(),
                        ch.getDapAnD(),
                        ch.getDapAnDung()
                ));
            }
        }
        capNhatGiaoDienPreview();
    }

    private void napCauHoiMau() {
        docVaParseContentTxt(layMau20CauHoiCSDL());
        capNhatGiaoDienPreview();
    }

    private void capNhatGiaoDienPreview() {
        questionsPreviewContainer.removeAllViews();

        if (danhSachCauHoi.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có câu hỏi nào. Bấm 'Import TXT' hoặc 'Thêm thủ công'.");
            tvEmpty.setTextColor(0xFF715D72);
            tvEmpty.setTextSize(13);
            tvEmpty.setPadding(10, 20, 10, 20);
            questionsPreviewContainer.addView(tvEmpty);
            return;
        }

        int dpDensity = Math.round(getResources().getDisplayMetrics().density);

        for (int i = 0; i < danhSachCauHoi.size(); i++) {
            final int index = i;
            CauHoiItem ch = danhSachCauHoi.get(i);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setBackgroundResource(R.drawable.bg_home_card);
            itemLayout.setPadding(24, 24, 24, 24);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 16;
            itemLayout.setLayoutParams(params);

            TextView tvTitle = new TextView(this);
            tvTitle.setText("Câu " + (i + 1) + ": " + ch.noiDung);
            tvTitle.setTextColor(0xFF38243D);
            tvTitle.setTextSize(15);
            tvTitle.setTypeface(null, Typeface.BOLD);
            itemLayout.addView(tvTitle);

            TextView tvBadge = new TextView(this);
            tvBadge.setText(CauHoi.LOAI_SINGLE.equals(ch.loaiCauHoi) ? "[Radio - Chọn 1 đáp án]" : "[Checkbox - Chọn nhiều đáp án]");
            tvBadge.setTextColor(0xFF603374);
            tvBadge.setTextSize(12);
            itemLayout.addView(tvBadge);

            if (!TextUtils.isEmpty(ch.anhPath)) {
                ImageView imgView = new ImageView(this);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 150 * dpDensity);
                imgParams.topMargin = 8 * dpDensity;
                imgParams.bottomMargin = 8 * dpDensity;
                imgView.setLayoutParams(imgParams);
                imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                hienThiAnhLenImageView(imgView, ch.anhPath);
                itemLayout.addView(imgView);
            }

            if (CauHoi.LOAI_SINGLE.equals(ch.loaiCauHoi)) {
                RadioGroup rg = new RadioGroup(this);
                rg.setEnabled(false);

                RadioButton rbA = new RadioButton(this); rbA.setText("A. " + ch.dapAnA); rbA.setEnabled(false);
                RadioButton rbB = new RadioButton(this); rbB.setText("B. " + ch.dapAnB); rbB.setEnabled(false);
                RadioButton rbC = new RadioButton(this); rbC.setText("C. " + ch.dapAnC); rbC.setEnabled(false);
                RadioButton rbD = new RadioButton(this); rbD.setText("D. " + ch.dapAnD); rbD.setEnabled(false);

                if ("A".equalsIgnoreCase(ch.dapAnDung)) rbA.setChecked(true);
                else if ("B".equalsIgnoreCase(ch.dapAnDung)) rbB.setChecked(true);
                else if ("C".equalsIgnoreCase(ch.dapAnDung)) rbC.setChecked(true);
                else if ("D".equalsIgnoreCase(ch.dapAnDung)) rbD.setChecked(true);

                rg.addView(rbA); rg.addView(rbB); rg.addView(rbC); rg.addView(rbD);
                itemLayout.addView(rg);
            } else {
                LinearLayout cbBox = new LinearLayout(this);
                cbBox.setOrientation(LinearLayout.VERTICAL);

                CheckBox cbA = new CheckBox(this); cbA.setText("A. " + ch.dapAnA); cbA.setEnabled(false);
                CheckBox cbB = new CheckBox(this); cbB.setText("B. " + ch.dapAnB); cbB.setEnabled(false);
                CheckBox cbC = new CheckBox(this); cbC.setText("C. " + ch.dapAnC); cbC.setEnabled(false);
                CheckBox cbD = new CheckBox(this); cbD.setText("D. " + ch.dapAnD); cbD.setEnabled(false);

                if (ch.dapAnDung != null) {
                    if (ch.dapAnDung.contains("A")) cbA.setChecked(true);
                    if (ch.dapAnDung.contains("B")) cbB.setChecked(true);
                    if (ch.dapAnDung.contains("C")) cbC.setChecked(true);
                    if (ch.dapAnDung.contains("D")) cbD.setChecked(true);
                }

                cbBox.addView(cbA); cbBox.addView(cbB); cbBox.addView(cbC); cbBox.addView(cbD);
                itemLayout.addView(cbBox);
            }

            LinearLayout rowBtn = new LinearLayout(this);
            rowBtn.setOrientation(LinearLayout.HORIZONTAL);
            rowBtn.setPadding(0, 10, 0, 0);

            TextView btnEdit = new TextView(this);
            btnEdit.setText("✏ Chỉnh sửa");
            btnEdit.setTextColor(0xFF603374);
            btnEdit.setTextSize(13);
            btnEdit.setPadding(0, 0, 20, 0);
            btnEdit.setOnClickListener(v -> hienThiDialogThemCauHoi(index));

            TextView btnDelete = new TextView(this);
            btnDelete.setText("🗑 Xóa");
            btnDelete.setTextColor(0xFFEF4444);
            btnDelete.setTextSize(13);
            btnDelete.setOnClickListener(v -> {
                danhSachCauHoi.remove(index);
                capNhatGiaoDienPreview();
            });

            rowBtn.addView(btnEdit);
            rowBtn.addView(btnDelete);
            itemLayout.addView(rowBtn);

            questionsPreviewContainer.addView(itemLayout);
        }
    }

    private void hienThiDialogImportTxt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import danh sách câu hỏi TXT");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_txt, null);

        TextView btnLoadSample = dialogView.findViewById(R.id.btnLoadSample);
        TextView btnPickFile = dialogView.findViewById(R.id.btnPickFile);
        etDialogTxtInput = dialogView.findViewById(R.id.etTxtInput);

        btnLoadSample.setOnClickListener(v -> {
            etDialogTxtInput.setText(layMau20CauHoiCSDL());
            Toast.makeText(this, "Đã nạp bộ mẫu 20 câu hỏi CSDL!", Toast.LENGTH_SHORT).show();
        });

        btnPickFile.setOnClickListener(v -> {
            try {
                pickTxtLauncher.launch("*/*");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Import", (dialog, which) -> {
            String content = etDialogTxtInput.getText().toString();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "Nội dung rỗng", Toast.LENGTH_SHORT).show();
                return;
            }
            int count = docVaParseContentTxt(content);
            capNhatGiaoDienPreview();
            Toast.makeText(this, "Đã import " + count + " câu hỏi!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private int docVaParseContentTxt(String text) {
        if (TextUtils.isEmpty(text)) return 0;

        text = text.replace("\r", "");
        String[] lines = text.split("\n");

        String currentNoiDung = "";
        String a = "", b = "", c = "", d = "", dung = "A";
        String loai = CauHoi.LOAI_SINGLE;
        String currentAnswerKey = "";
        int count = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String lower = line.toLowerCase(Locale.ROOT);

            if (lower.matches("^(câu|cau)\\s*\\d+.*") || lower.matches("^câu\\s*\\d+.*") || lower.matches("^cau\\s*\\d+.*")) {
                if (!currentNoiDung.isEmpty()) {
                    luuCauHoiVaoDanhSach(currentNoiDung, a, b, c, d, dung, loai);
                    count++;
                }
                a = ""; b = ""; c = ""; d = ""; dung = "A"; loai = CauHoi.LOAI_SINGLE;
                currentAnswerKey = "";

                int idxColon = line.indexOf(":");
                if (idxColon == -1) idxColon = line.indexOf(".");
                if (idxColon == -1) idxColon = line.indexOf("-");

                currentNoiDung = idxColon != -1 ? line.substring(idxColon + 1).trim() : line;

            } else if (lower.matches("^(đáp án|dap an|đáp án đúng|dap an dung).*")) {
                int idxColon = line.indexOf(":");
                if (idxColon == -1) idxColon = line.indexOf("=");
                if (idxColon == -1) idxColon = line.indexOf("-");

                if (idxColon != -1) {
                    dung = line.substring(idxColon + 1).replaceAll("[^a-zA-DA-Z,]", "").toUpperCase(Locale.ROOT);
                } else {
                    dung = line.replaceAll("[^a-zA-DA-Z,]", "").toUpperCase(Locale.ROOT);
                }

                if (dung.contains(",") || dung.length() > 1) {
                    loai = CauHoi.LOAI_MULTI;
                }
                currentAnswerKey = "";

            } else if (lower.matches("^[aA][\\.\\:\\)\\-].*") || lower.matches("^[aA]\\s+.*")) {
                a = catTienToDapAn(line);
                currentAnswerKey = "A";
            } else if (lower.matches("^[bB][\\.\\:\\)\\-].*") || lower.matches("^[bB]\\s+.*")) {
                b = catTienToDapAn(line);
                currentAnswerKey = "B";
            } else if (lower.matches("^[cC][\\.\\:\\)\\-].*") || lower.matches("^[cC]\\s+.*")) {
                c = catTienToDapAn(line);
                currentAnswerKey = "C";
            } else if (lower.matches("^[dD][\\.\\:\\)\\-].*") || lower.matches("^[dD]\\s+.*")) {
                d = catTienToDapAn(line);
                currentAnswerKey = "D";
            } else {
                if (!currentAnswerKey.isEmpty()) {
                    if ("A".equals(currentAnswerKey)) a += " " + line;
                    else if ("B".equals(currentAnswerKey)) b += " " + line;
                    else if ("C".equals(currentAnswerKey)) c += " " + line;
                    else if ("D".equals(currentAnswerKey)) d += " " + line;
                } else if (!currentNoiDung.isEmpty()) {
                    currentNoiDung += " " + line;
                }
            }
        }

        if (!currentNoiDung.isEmpty()) {
            luuCauHoiVaoDanhSach(currentNoiDung, a, b, c, d, dung, loai);
            count++;
        }
        return count;
    }

    private String catTienToDapAn(String line) {
        return line.replaceFirst("^[a-dA-D][\\.\\:\\)\\-\\s]+\\s*", "").trim();
    }

    private void luuCauHoiVaoDanhSach(String nd, String a, String b, String c, String d, String dung, String loai) {
        if (TextUtils.isEmpty(a)) a = "Phương án A";
        if (TextUtils.isEmpty(b)) b = "Phương án B";
        if (TextUtils.isEmpty(c)) c = "Phương án C";
        if (TextUtils.isEmpty(d)) d = "Phương án D";
        if (TextUtils.isEmpty(dung)) dung = "A";

        danhSachCauHoi.add(new CauHoiItem(nd.trim(), "", loai, a.trim(), b.trim(), c.trim(), d.trim(), dung.trim()));
    }

    private void hienThiDialogThemCauHoi(int indexEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(indexEdit == -1 ? "Thêm câu hỏi mới" : "Chỉnh sửa câu hỏi");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_question, null);

        final EditText etNoiDung = dialogView.findViewById(R.id.etNoiDung);
        final TextView btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        final TextView btnRemoveImage = dialogView.findViewById(R.id.btnRemoveImage);
        final ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
        final EditText etA = dialogView.findViewById(R.id.etA);
        final EditText etB = dialogView.findViewById(R.id.etB);
        final EditText etC = dialogView.findViewById(R.id.etC);
        final EditText etD = dialogView.findViewById(R.id.etD);
        final EditText etDung = dialogView.findViewById(R.id.etDung);

        currentPickedImageUri = "";
        currentDialogImgPreview = imgPreview;
        currentDialogBtnRemoveImage = btnRemoveImage;

        if (indexEdit != -1 && indexEdit < danhSachCauHoi.size()) {
            CauHoiItem item = danhSachCauHoi.get(indexEdit);
            etNoiDung.setText(item.noiDung);
            etA.setText(item.dapAnA);
            etB.setText(item.dapAnB);
            etC.setText(item.dapAnC);
            etD.setText(item.dapAnD);
            etDung.setText(item.dapAnDung);

            if (!TextUtils.isEmpty(item.anhPath)) {
                currentPickedImageUri = item.anhPath;
                hienThiAnhLenImageView(imgPreview, item.anhPath);
                btnRemoveImage.setVisibility(View.VISIBLE);
            }
        }

        btnPickImage.setOnClickListener(v -> {
            try {
                pickImageLauncher.launch("image/*");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoveImage.setOnClickListener(v -> {
            currentPickedImageUri = "";
            imgPreview.setVisibility(View.GONE);
            btnRemoveImage.setVisibility(View.GONE);
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String nd = etNoiDung.getText().toString().trim();
            String anh = currentPickedImageUri.trim();
            String a = etA.getText().toString().trim();
            String b = etB.getText().toString().trim();
            String c = etC.getText().toString().trim();
            String d = etD.getText().toString().trim();
            String dung = etDung.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(nd)) {
                Toast.makeText(this, "Nội dung câu hỏi không được trống", Toast.LENGTH_SHORT).show();
                return;
            }

            String loai = (dung.contains(",") || dung.contains("&")) ? CauHoi.LOAI_MULTI : CauHoi.LOAI_SINGLE;
            CauHoiItem newItem = new CauHoiItem(nd, anh, loai, a, b, c, d, dung);

            if (indexEdit == -1) {
                danhSachCauHoi.add(newItem);
            } else {
                danhSachCauHoi.set(indexEdit, newItem);
            }
            capNhatGiaoDienPreview();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void hienThiAnhLenImageView(ImageView imgView, String anhPath) {
        if (TextUtils.isEmpty(anhPath)) {
            imgView.setVisibility(View.GONE);
            return;
        }
        try {
            Uri uri = Uri.parse(anhPath);
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap != null) {
                imgView.setImageBitmap(bitmap);
                imgView.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception ignored) {}

        try {
            imgView.setImageURI(Uri.parse(anhPath));
            imgView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
            imgView.setVisibility(View.GONE);
        }
    }

    private void luuDeThiVaCauHoi() {
        String tieuDe = etQuizTitle.getText().toString().trim();
        String phanCumInput = etPhanCum != null ? etPhanCum.getText().toString().trim() : "";
        String timerStr = etQuizTimer.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();

        if (TextUtils.isEmpty(tieuDe)) {
            Toast.makeText(this, "Vui lòng nhập tên đề thi", Toast.LENGTH_SHORT).show();
            return;
        }

        String phanCum = !TextUtils.isEmpty(phanCumInput) ? phanCumInput : "BÀI THI TRẮC NGHIỆM";

        int thoiGian = 15;
        try {
            if (!TextUtils.isEmpty(timerStr)) {
                thoiGian = Integer.parseInt(timerStr);
            }
        } catch (Exception ignored) {}

        if (deThiId != -1) {
            dbHelper.capNhatDeThi(deThiId, tieuDe, thoiGian, startTime, endTime);
            dbHelper.xoaCauHoiCuaDeThi(deThiId);

            for (CauHoiItem ch : danhSachCauHoi) {
                dbHelper.themCauHoi(deThiId, ch.noiDung, ch.anhPath, ch.loaiCauHoi, ch.dapAnA, ch.dapAnB, ch.dapAnC, ch.dapAnD, ch.dapAnDung);
            }

            if (noiDungId != -1) {
                dbHelper.capNhatNoiDungMonHoc(noiDungId, tieuDe, phanCum, "Bài thi trắc nghiệm (" + danhSachCauHoi.size() + " câu).", null, endTime);
            }

            Toast.makeText(this, "Cập nhật bài thi thành công với " + danhSachCauHoi.size() + " câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            long idDeThi = dbHelper.taoDeThi(monHocId, tieuDe, thoiGian, startTime, endTime);
            if (idDeThi != -1) {
                for (CauHoiItem ch : danhSachCauHoi) {
                    dbHelper.themCauHoi(idDeThi, ch.noiDung, ch.anhPath, ch.loaiCauHoi, ch.dapAnA, ch.dapAnB, ch.dapAnC, ch.dapAnD, ch.dapAnDung);
                }
                dbHelper.themNoiDungMonHoc(monHocId, tieuDe, NoiDungMonHoc.LOAI_TRAC_NGHIEM, "", phanCum, (int) idDeThi, "Bài thi trắc nghiệm (" + danhSachCauHoi.size() + " câu).", endTime);
                Toast.makeText(this, "Tạo bài thi thành công với " + danhSachCauHoi.size() + " câu hỏi!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Không thể tạo bài thi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String layMau20CauHoiCSDL() {
        return "Câu 1: Khóa chính (Primary Key) trong CSDL có đặc điểm gì?\n" +
                "A. Phải duy nhất và không được chứa giá trị NULL\n" +
                "B. Có thể chứa giá trị NULL nhưng phải duy nhất\n" +
                "C. Được trùng lặp giá trị giữa các dòng\n" +
                "D. Không tự động tạo chỉ mục Index\n" +
                "Đáp án: A\n\n" +
                "Câu 2: Những câu lệnh nào thuộc nhóm DDL (Data Definition Language)?\n" +
                "A. CREATE TABLE\n" +
                "B. ALTER TABLE\n" +
                "C. SELECT * FROM\n" +
                "D. DROP TABLE\n" +
                "Đáp án: A,B,D\n\n" +
                "Câu 3: Khóa ngoại (Foreign Key) dùng để làm gì trong CSDL quan hệ?\n" +
                "A. Ràng buộc toàn vẹn tham chiếu giữa hai bảng\n" +
                "B. Tăng tốc độ tìm kiếm bản ghi\n" +
                "C. Đảm bảo dữ liệu không bị trùng\n" +
                "D. Mã hóa mật khẩu người dùng\n" +
                "Đáp án: A\n\n" +
                "Câu 4: Mệnh đề nào trong SQL dùng để lọc nhóm sau khi gom nhóm GROUP BY?\n" +
                "A. WHERE\n" +
                "B. HAVING\n" +
                "C. ORDER BY\n" +
                "D. LIKE\n" +
                "Đáp án: B\n\n" +
                "Câu 5: Những hàm co cụm (Aggregate Functions) nào phổ biến trong SQL?\n" +
                "A. COUNT()\n" +
                "B. SUM()\n" +
                "C. AVG()\n" +
                "D. MAX()\n" +
                "Đáp án: A,B,C,D\n\n" +
                "Câu 6: Câu lệnh SQL nào dùng để thêm dòng dữ liệu mới vào bảng?\n" +
                "A. INSERT INTO\n" +
                "B. UPDATE\n" +
                "C. ADD ROW\n" +
                "D. ALTER TABLE\n" +
                "Đáp án: A\n\n" +
                "Câu 7: Tác động của Chỉ mục (Index) trong CSDL là gì?\n" +
                "A. Tăng tốc độ truy vấn SELECT\n" +
                "B. Giảm tốc độ thao tác INSERT/UPDATE/DELETE\n" +
                "C. Chiếm thêm dung lượng bộ nhớ\n" +
                "D. Tự động xóa dữ liệu cũ\n" +
                "Đáp án: A,B,C\n\n" +
                "Câu 8: Ràng buộc UNIQUE trong CSDL có điểm khác biệt gì so với PRIMARY KEY?\n" +
                "A. Cho phép chứa tối đa một giá trị NULL\n" +
                "B. Một bảng có thể có nhiều ràng buộc UNIQUE\n" +
                "C. Không bắt buộc dữ liệu phải duy nhất\n" +
                "D. Giống hệt PRIMARY KEY không khác gì\n" +
                "Đáp án: A,B\n\n" +
                "Câu 9: Từ khóa DISTINCT trong câu lệnh SELECT dùng để làm gì?\n" +
                "A. Loại bỏ các dòng dữ liệu trùng lặp trong kết quả\n" +
                "B. Sắp xếp dữ liệu tăng dần\n" +
                "C. Giới hạn số lượng bản ghi trả về\n" +
                "D. Đếm số lượng cột trong bảng\n" +
                "Đáp án: A\n\n" +
                "Câu 10: Phép nối LEFT JOIN trả về kết quả như thế nào?\n" +
                "A. Tất cả các bản ghi bảng bên trái và các bản ghi khớp ở bảng bên phải\n" +
                "B. Chỉ các bản ghi khớp nhau ở cả hai bảng\n" +
                "C. Tất cả các bản ghi bảng bên phải\n" +
                "D. Loại bỏ tất cả dữ liệu trùng\n" +
                "Đáp án: A\n\n" +
                "Câu 11: Những phép toán tập hợp nào có sẵn trong chuẩn SQL?\n" +
                "A. UNION\n" +
                "B. INTERSECT\n" +
                "C. EXCEPT\n" +
                "D. JOIN\n" +
                "Đáp án: A,B,C\n\n" +
                "Câu 12: Câu lệnh DELETE khác câu lệnh TRUNCATE ở điểm nào?\n" +
                "A. DELETE có thể đi kèm điều kiện WHERE để xóa từng dòng\n" +
                "B. TRUNCATE xóa toàn bộ dữ liệu bảng nhanh hơn và không có WHERE\n" +
                "C. TRUNCATE không ghi log từng dòng dữ liệu\n" +
                "D. DELETE không thể hoàn tác (Rollback)\n" +
                "Đáp án: A,B,C\n\n" +
                "Câu 13: Giao dịch (Transaction) trong CSDL phải đảm bảo 4 tính chất nào (ACID)?\n" +
                "A. Atomicity (Tính nguyên tố)\n" +
                "B. Consistency (Tính nhất quán)\n" +
                "C. Isolation (Tính cô lập)\n" +
                "D. Durability (Tính bền vững)\n" +
                "Đáp án: A,B,C,D\n\n" +
                "Câu 14: Mục đích chính của việc Chuẩn hóa CSDL (Normalization) là gì?\n" +
                "A. Giảm thiểu sự dư thừa dữ liệu và tránh tranh chấp đồng thời\n" +
                "B. Tăng dung lượng đĩa cứng sử dụng\n" +
                "C. Tạo giao diện đẹp cho người dùng\n" +
                "D. Mã hóa tệp tin CSDL\n" +
                "Đáp án: A\n\n" +
                "Câu 15: Dạng chuẩn 1NF yêu cầu bảng CSDL phải đạt điều kiện nào?\n" +
                "A. Mỗi ô chứa một giá trị đơn (nguyên tố)\n" +
                "B. Không chứa các thuộc tính đa trị\n" +
                "C. Đã đạt dạng chuẩn 3NF\n" +
                "D. Có ít nhất 5 bảng quan hệ\n" +
                "Đáp án: A,B\n\n" +
                "Câu 16: Phép nối FULL OUTER JOIN trả về những dòng dữ liệu nào?\n" +
                "A. Tất cả các dòng khi có sự phù hợp ở bảng trái hoặc bảng phải\n" +
                "B. Chỉ các dòng khớp 100% ở cả hai bảng\n" +
                "C. Chỉ bảng trái\n" +
                "D. Không trả về dữ liệu nào\n" +
                "Đáp án: A\n\n" +
                "Câu 17: Cột nào thích hợp nhất để làm Khóa chính cho bảng SinhVien?\n" +
                "A. ma_sinh_vien\n" +
                "B. ho_ten\n" +
                "C. ngay_sinh\n" +
                "D. dia_chi\n" +
                "Đáp án: A\n\n" +
                "Câu 18: Những kiểu dữ liệu nào thường dùng lưu trữ chuỗi ký tự trong SQL Server?\n" +
                "A. VARCHAR\n" +
                "B. NVARCHAR\n" +
                "C. TEXT\n" +
                "D. INT\n" +
                "Đáp án: A,B,C\n\n" +
                "Câu 19: Để sắp xếp kết quả giảm dần trong câu lệnh ORDER BY, ta dùng từ khóa nào?\n" +
                "A. DESC\n" +
                "B. ASC\n" +
                "C. DOWN\n" +
                "D. DECREASE\n" +
                "Đáp án: A\n\n" +
                "Câu 20: Ràng buộc CHECK trong SQL được dùng để làm gì?\n" +
                "A. Giới hạn khoảng giá trị hợp lệ được nhập vào một cột\n" +
                "B. Kiểm tra kết nối mạng CSDL\n" +
                "C. Đảm bảo tên bảng không trùng nhau\n" +
                "D. Kiểm tra quyền truy cập tài khoản\n" +
                "Đáp án: A";
    }
}
