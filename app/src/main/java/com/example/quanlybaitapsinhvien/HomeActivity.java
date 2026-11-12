package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int DARK = 0xFF38243D;
    private static final int MUTED = 0xFF715D72;
    private static final int PURPLE = 0xFF603374;

    private SessionManager session;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_home);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaiKhoan account = dbHelper.layTaiKhoanTheoId(session.layUserId());
        if (!session.daDangNhap() || account == null || account.getTrangThai() != 1) {
            session.xoaPhienDangNhap();
            openWelcome();
            return;
        }
        String role = account.getVaiTro();
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role) && !"STUDENT".equals(role)) {
            session.xoaPhienDangNhap();
            openWelcome();
            return;
        }
        renderHome(role, account.getEmail());
    }

    private void renderHome(String role, String email) {
        LinearLayout actions = findViewById(R.id.actionRow);
        LinearLayout list = findViewById(R.id.contentList);
        LinearLayout nav = findViewById(R.id.bottomNav);
        actions.removeAllViews();
        list.removeAllViews();
        nav.removeAllViews();

        String label = "ADMIN".equals(role) ? "Quản trị viên"
                : "TEACHER".equals(role) ? "Giáo viên" : "Sinh viên";
        ((TextView) findViewById(R.id.tvRole)).setText(label + " · " + email);
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        if ("ADMIN".equals(role)) {
            setText(R.id.tvHomeTitle, "Quản trị ứng dụng");
            setText(R.id.tvHeroLabel, "Học kỳ hiện tại");
            setText(R.id.tvHeroTitle, "Phân quyền & Lớp học");
            setText(R.id.tvHeroDescription, "Tạo giáo viên, tạo môn học và phân công giảng dạy");
            setText(R.id.tvSectionTitle, "Công cụ Quản trị");

            addActionCustom(actions, "Quản lý / Phân công", v -> startActivity(new Intent(HomeActivity.this, AdminManagementActivity.class)));
            addCard(list, "Quản lý Giáo viên & Môn học", "Tạo tài khoản Giáo viên, mở lớp và xếp Sinh viên", v -> startActivity(new Intent(HomeActivity.this, AdminManagementActivity.class)));

        } else if ("TEACHER".equals(role)) {
            setText(R.id.tvHomeTitle, "Lớp giảng dạy");
            setText(R.id.tvHeroLabel, "Lớp phụ trách");
            setText(R.id.tvHeroTitle, "Môn học phụ trách");
            setText(R.id.tvHeroDescription, "Tạo đề thi trắc nghiệm đếm giờ và quản lý bài tập");
            setText(R.id.tvSectionTitle, "Danh sách môn phụ trách");

            addActionCustom(actions, "+ Đề thi trắc nghiệm", v -> startActivity(new Intent(HomeActivity.this, CreateQuizActivity.class)));

            List<MonHoc> danhSach = dbHelper.layMonHocChoGiaoVien(session.layUserId());
            if (danhSach.isEmpty()) {
                danhSach = dbHelper.layTatCaMonHoc();
            }
            for (MonHoc mon : danhSach) {
                addCourseCard(list, mon, true);
            }
        } else {
            setText(R.id.tvHomeTitle, "Sổ bài tập sinh viên");
            setText(R.id.tvHeroLabel, "Lớp đang học");
            setText(R.id.tvHeroTitle, "Môn học của tôi");
            setText(R.id.tvHeroDescription, "Danh sách môn học, bài thi trắc nghiệm và bài tập");
            setText(R.id.tvSectionTitle, "Các môn học đang tham gia");

            List<MonHoc> danhSach = dbHelper.layDanhSachMonHocChoSinhVien(session.layUserId());
            for (MonHoc mon : danhSach) {
                addCourseCard(list, mon, false);
            }
        }
    }

    private void addCourseCard(LinearLayout parent, MonHoc mon, boolean isGiaoVien) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_course_card, parent, false);

        TextView tvNamHoc = cardView.findViewById(R.id.tvNamHoc);
        TextView tvTenMonHoc = cardView.findViewById(R.id.tvTenMonHoc);
        TextView tvTenGiaoVien = cardView.findViewById(R.id.tvTenGiaoVien);
        TextView btnVaoHoc = cardView.findViewById(R.id.btnVaoHoc);

        tvNamHoc.setText(mon.getNamHoc());
        tvTenMonHoc.setText(mon.getMaMon() + " - " + mon.getTenMon());
        tvTenGiaoVien.setText("Giảng viên phụ trách: " + (mon.getTenGiaoVien() != null ? mon.getTenGiaoVien() : "teacher@gmail.com"));

        if (isGiaoVien) {
            btnVaoHoc.setText("🏫 Quản lý môn học");
        } else {
            btnVaoHoc.setText("♥ Vào học");
        }

        btnVaoHoc.setOnClickListener(v -> moChiTietMonHoc(mon));
        cardView.setOnClickListener(v -> moChiTietMonHoc(mon));

        parent.addView(cardView);
    }

    private void moChiTietMonHoc(MonHoc mon) {
        Intent intent = new Intent(HomeActivity.this, CourseDetailActivity.class);
        intent.putExtra("MON_HOC_ID", mon.getId());
        intent.putExtra("MA_MON", mon.getMaMon());
        intent.putExtra("TEN_MON", mon.getTenMon());
        intent.putExtra("NAM_HOC", mon.getNamHoc());
        startActivity(intent);
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private void addActionCustom(LinearLayout parent, String title, View.OnClickListener listener) {
        TextView item = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
        if (parent.getChildCount() > 0) params.leftMargin = dp(8);
        item.setLayoutParams(params);
        item.setBackgroundResource(R.drawable.bg_home_pill);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), 0, dp(8), 0);
        item.setText(title);
        item.setTextColor(PURPLE);
        item.setTextSize(14);
        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(listener);
        parent.addView(item);
    }

    private void addCard(LinearLayout parent, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(70));
        params.bottomMargin = dp(9);
        card.setLayoutParams(params);
        card.setBackgroundResource(R.drawable.bg_home_card);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), 0, dp(14), 0);
        TextView name = new TextView(this);
        name.setText(title);
        name.setTextColor(DARK);
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        TextView description = new TextView(this);
        description.setText(subtitle);
        description.setTextColor(MUTED);
        description.setTextSize(13);
        description.setPadding(0, dp(4), 0, 0);
        card.addView(name);
        card.addView(description);
        card.setOnClickListener(listener);
        parent.addView(card);
    }

    private void logout() {
        session.xoaPhienDangNhap();
        openWelcome();
    }

    private void openWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
