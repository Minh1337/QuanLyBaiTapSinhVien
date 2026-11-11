package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private static final int DARK = 0xFF38243D;
    private static final int MUTED = 0xFF715D72;
    private static final int PURPLE = 0xFF603374;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaiKhoan account = new DatabaseHelper(this).layTaiKhoanTheoId(session.layUserId());
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
            setText(R.id.tvHeroTitle, "Lớp học phần");
            setText(R.id.tvHeroDescription, "Quản lý lớp và phân công giảng dạy");
            setText(R.id.tvSectionTitle, "Quản lý học kỳ");
            addAction(actions, "Tạo giáo viên");
            addAction(actions, "Tạo lớp");
            addCard(list, "Tài khoản", "Tạo giáo viên, khóa hoặc mở tài khoản");
            addCard(list, "Lớp học phần", "Phân công giáo viên và xếp sinh viên");
            addCard(list, "Lớp đã lưu trữ", "Xem lại lịch sử lớp");
        } else if ("TEACHER".equals(role)) {
            setText(R.id.tvHomeTitle, "Lớp giảng dạy");
            setText(R.id.tvHeroLabel, "Lớp phụ trách");
            setText(R.id.tvHeroTitle, "Chưa có lớp");
            setText(R.id.tvHeroDescription, "Lớp được phân công sẽ xuất hiện tại đây");
            setText(R.id.tvSectionTitle, "Công việc của giáo viên");
            addAction(actions, "Giao bài");
            addAction(actions, "Xem tiến độ");
            addCard(list, "Lớp của tôi", "Chưa có lớp học phần được phân công");
            addCard(list, "Bài đã giao", "Bài tập của lớp sẽ hiển thị tại đây");
            addCard(list, "Tiến độ sinh viên", "Theo dõi từng bài sau khi giao");
        } else {
            setText(R.id.tvHomeTitle, "Việc của tôi");
            setText(R.id.tvHeroLabel, "Bài tập sắp đến hạn");
            setText(R.id.tvHeroTitle, "Chưa có bài tập");
            setText(R.id.tvHeroDescription, "Bài của lớp và bài cá nhân sẽ xuất hiện tại đây");
            setText(R.id.tvSectionTitle, "Bài tập và lớp học");
            addAction(actions, "+ Bài cá nhân");
            addAction(actions, "Môn cá nhân");
            addCard(list, "Giáo viên giao", "Chưa có bài tập được giao");
            addCard(list, "Cá nhân", "Bài tập bạn tạo sẽ hiển thị tại đây");
            addCard(list, "Lớp của tôi", "Chưa tham gia lớp học phần nào");
            addAction(nav, "Bài tập");
            addAction(nav, "Lịch tuần");
            addAction(nav, "Thống kê");
        }
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private void addAction(LinearLayout parent, String title) {
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
        item.setOnClickListener(v -> showPending(title));
        parent.addView(item);
    }

    private void addCard(LinearLayout parent, String title, String subtitle) {
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
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView description = new TextView(this);
        description.setText(subtitle);
        description.setTextColor(MUTED);
        description.setTextSize(13);
        description.setPadding(0, dp(4), 0, 0);
        card.addView(name);
        card.addView(description);
        card.setOnClickListener(v -> showPending(title));
        parent.addView(card);
    }

    private void showPending(String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Chức năng này đang được phát triển.")
                .setPositiveButton("Đóng", null)
                .show();
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
