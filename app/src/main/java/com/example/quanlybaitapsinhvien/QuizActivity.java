package com.example.quanlybaitapsinhvien;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SessionManager session;

    private int deThiId;
    private int noiDungId;
    private DeThi deThi;
    private List<CauHoi> danhSachCauHoi;

    private TextView tvQuizTitle;
    private TextView tvQuizTimer;
    private LinearLayout questionsLayout;
    private TextView btnSubmitQuiz;

    private CountDownTimer countDownTimer;
    private final List<View> questionViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);
        setContentView(R.layout.activity_quiz);

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

        deThiId = getIntent().getIntExtra("DE_THI_ID", 1);
        noiDungId = getIntent().getIntExtra("NOI_DUNG_ID", 0);

        khoiTaoViews();
        caiDatSuKien();
        taiDuLieuDeThi();
    }

    private void khoiTaoViews() {
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvQuizTimer = findViewById(R.id.tvQuizTimer);
        questionsLayout = findViewById(R.id.questionsLayout);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);
    }

    private void caiDatSuKien() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSubmitQuiz != null) {
            btnSubmitQuiz.setOnClickListener(v -> xacNhanNopBai());
        }
    }

    private void taiDuLieuDeThi() {
        deThi = dbHelper.layDeThiTheoId(deThiId);
        danhSachCauHoi = dbHelper.layDanhSachCauHoi(deThiId);

        if (deThi != null) {
            tvQuizTitle.setText(deThi.getTieuDe());
        } else {
            tvQuizTitle.setText("Bài thi trắc nghiệm");
        }

        if (danhSachCauHoi == null || danhSachCauHoi.isEmpty()) {
            Toast.makeText(this, "Chưa có câu hỏi nào cho bài thi này!", Toast.LENGTH_SHORT).show();
            return;
        }

        hienThiDanhSachCauHoi();
        batDauDemGio();
    }

    private void hienThiDanhSachCauHoi() {
        questionsLayout.removeAllViews();
        questionViews.clear();
        int dpDensity = Math.round(getResources().getDisplayMetrics().density);

        for (int i = 0; i < danhSachCauHoi.size(); i++) {
            CauHoi ch = danhSachCauHoi.get(i);

            LinearLayout itemBox = new LinearLayout(this);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setBackgroundResource(R.drawable.bg_home_card);
            itemBox.setPadding(24, 24, 24, 24);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 16;
            itemBox.setLayoutParams(params);

            // Tiêu đề câu hỏi
            TextView tvQuestion = new TextView(this);
            tvQuestion.setText("Câu " + (i + 1) + ": " + ch.getNoiDung());
            tvQuestion.setTextColor(0xFF38243D);
            tvQuestion.setTextSize(16);
            tvQuestion.setTypeface(null, Typeface.BOLD);
            itemBox.addView(tvQuestion);

            // Ghi chú hình thức chọn
            TextView tvHint = new TextView(this);
            tvHint.setText(CauHoi.LOAI_SINGLE.equals(ch.getLoaiCauHoi()) ? "(Chọn 1 đáp án)" : "(Chọn nhiều đáp án)");
            tvHint.setTextColor(0xFF715D72);
            tvHint.setTextSize(12);
            itemBox.addView(tvHint);

            // Hiển thị ảnh (nếu có)
            if (!TextUtils.isEmpty(ch.getAnhPath())) {
                ImageView imgView = new ImageView(this);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 200 * dpDensity);
                imgParams.topMargin = 8 * dpDensity;
                imgParams.bottomMargin = 8 * dpDensity;
                imgView.setLayoutParams(imgParams);
                imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                hienThiAnhLenImageView(imgView, ch.getAnhPath());
                itemBox.addView(imgView);
            }

            // Phương án đáp án: RadioGroup (Radio) hoặc LinearLayout Checkbox
            if (CauHoi.LOAI_SINGLE.equals(ch.getLoaiCauHoi())) {
                RadioGroup rg = new RadioGroup(this);
                rg.setTag("RADIO_GROUP");

                RadioButton rbA = new RadioButton(this); rbA.setText("A. " + ch.getDapAnA()); rbA.setTag("A");
                RadioButton rbB = new RadioButton(this); rbB.setText("B. " + ch.getDapAnB()); rbB.setTag("B");
                RadioButton rbC = new RadioButton(this); rbC.setText("C. " + ch.getDapAnC()); rbC.setTag("C");
                RadioButton rbD = new RadioButton(this); rbD.setText("D. " + ch.getDapAnD()); rbD.setTag("D");

                rg.addView(rbA); rg.addView(rbB); rg.addView(rbC); rg.addView(rbD);
                itemBox.addView(rg);
                questionViews.add(rg);
            } else {
                LinearLayout cbLayout = new LinearLayout(this);
                cbLayout.setOrientation(LinearLayout.VERTICAL);
                cbLayout.setTag("CHECKBOX_LAYOUT");

                CheckBox cbA = new CheckBox(this); cbA.setText("A. " + ch.getDapAnA()); cbA.setTag("A");
                CheckBox cbB = new CheckBox(this); cbB.setText("B. " + ch.getDapAnB()); cbB.setTag("B");
                CheckBox cbC = new CheckBox(this); cbC.setText("C. " + ch.getDapAnC()); cbC.setTag("C");
                CheckBox cbD = new CheckBox(this); cbD.setText("D. " + ch.getDapAnD()); cbD.setTag("D");

                cbLayout.addView(cbA); cbLayout.addView(cbB); cbLayout.addView(cbC); cbLayout.addView(cbD);
                itemBox.addView(cbLayout);
                questionViews.add(cbLayout);
            }

            questionsLayout.addView(itemBox);
        }
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

    private void batDauDemGio() {
        int thoiGianPhut = deThi != null ? deThi.getThoiGianPhut() : 15;
        long totalMillis = (long) thoiGianPhut * 60 * 1000;

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long m = seconds / 60;
                long s = seconds % 60;
                tvQuizTimer.setText(String.format(Locale.getDefault(), "⏱ Thời gian còn lại: %02d:%02d", m, s));
            }

            @Override
            public void onFinish() {
                tvQuizTimer.setText("⏱ Hết giờ làm bài!");
                Toast.makeText(QuizActivity.this, "Đã hết thời gian làm bài!", Toast.LENGTH_LONG).show();
                chamDiemVaNopBai();
            }
        }.start();
    }

    private void xacNhanNopBai() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài thi")
                .setMessage("Bạn có chắc chắn muốn nộp bài thi ngay bây giờ?")
                .setPositiveButton("Nộp bài", (dialog, which) -> chamDiemVaNopBai())
                .setNegativeButton("Làm tiếp", null)
                .show();
    }

    private void chamDiemVaNopBai() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int soCauDung = 0;
        int tongSoCau = danhSachCauHoi.size();

        for (int i = 0; i < tongSoCau; i++) {
            CauHoi ch = danhSachCauHoi.get(i);
            View view = questionViews.get(i);
            String dapAnSinhVien = "";

            if (view instanceof RadioGroup) {
                RadioGroup rg = (RadioGroup) view;
                int selectedId = rg.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton rb = rg.findViewById(selectedId);
                    if (rb != null && rb.getTag() != null) {
                        dapAnSinhVien = rb.getTag().toString();
                    }
                }
            } else if (view instanceof LinearLayout) {
                LinearLayout cbLayout = (LinearLayout) view;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < cbLayout.getChildCount(); j++) {
                    View child = cbLayout.getChildAt(j);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked() && cb.getTag() != null) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(cb.getTag().toString());
                        }
                    }
                }
                dapAnSinhVien = sb.toString();
            }

            if (kieuTraVeDung(dapAnSinhVien, ch.getDapAnDung())) {
                soCauDung++;
            }
        }

        double diemSo = tongSoCau > 0 ? (double) Math.round((soCauDung * 10.0 / tongSoCau) * 10) / 10.0 : 0.0;
        String ngayThi = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        // Lưu kết quả thi vào SQLite
        int studentId = session.layUserId();
        dbHelper.luuKetQuaThi(studentId, deThiId, diemSo, ngayThi);
        if (noiDungId > 0) {
            dbHelper.capNhatHoanThanh(studentId, noiDungId, true);
        }

        // Hiển thị kết quả điểm số
        new AlertDialog.Builder(this)
                .setTitle("🎉 Kết quả bài thi")
                .setMessage("Số câu đúng: " + soCauDung + "/" + tongSoCau + "\nĐiểm số đạt được: " + diemSo + "/10.0")
                .setCancelable(false)
                .setPositiveButton("Hoàn tất", (dialog, which) -> finish())
                .show();
    }

    private boolean kieuTraVeDung(String svAns, String keyAns) {
        if (TextUtils.isEmpty(svAns) || TextUtils.isEmpty(keyAns)) return false;
        svAns = svAns.replaceAll("\\s+", "").toUpperCase();
        keyAns = keyAns.replaceAll("\\s+", "").toUpperCase();
        return svAns.equals(keyAns);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
