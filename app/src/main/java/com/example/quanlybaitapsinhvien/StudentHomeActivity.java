package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Màn hình Trang chủ Sinh viên với 4 Tab Navigation: Trang chủ, Bài tập, Lịch, Thống kê
 */
public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvStudentGreeting;
    private ImageView btnDangXuat;
    private FrameLayout layoutTabContentContainer;

    // Bottom Tab views
    private View tabTrangChu, tabBaiTap, tabLich, tabThongKe;
    private ImageView ivTabHome, ivTabAssignment, ivTabCalendar, ivTabStats;
    private TextView tvTabHome, tvTabAssignment, tvTabCalendar, tvTabStats;

    // References to active tab view contents
    private View viewTabHome, viewTabAssignments, viewTabCalendar, viewTabStats;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int sinhVienId = -1;
    private String hoTen = "Sinh viên";

    private int activeTab = 1; // 1: Trang chủ, 2: Bài tập, 3: Lịch, 4: Thống kê

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        sinhVienId = sessionManager.layUserId();

        TaiKhoan tk = dbHelper.layTaiKhoanTheoId(sinhVienId);
        if (tk != null) {
            hoTen = tk.getHoTen();
        }

        khoiTaoViews();
        caiDatSuKien();
        hienThiTab(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data for current tab when returning from detail screen
        hienThiTab(activeTab);
    }

    private void khoiTaoViews() {
        tvStudentGreeting = findViewById(R.id.tvStudentGreeting);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        layoutTabContentContainer = findViewById(R.id.layoutTabContentContainer);

        tvStudentGreeting.setText("Xin chào, " + hoTen);

        tabTrangChu = findViewById(R.id.tabTrangChu);
        tabBaiTap = findViewById(R.id.tabBaiTap);
        tabLich = findViewById(R.id.tabLich);
        tabThongKe = findViewById(R.id.tabThongKe);

        ivTabHome = findViewById(R.id.ivTabHome);
        ivTabAssignment = findViewById(R.id.ivTabAssignment);
        ivTabCalendar = findViewById(R.id.ivTabCalendar);
        ivTabStats = findViewById(R.id.ivTabStats);

        tvTabHome = findViewById(R.id.tvTabHome);
        tvTabAssignment = findViewById(R.id.tvTabAssignment);
        tvTabCalendar = findViewById(R.id.tvTabCalendar);
        tvTabStats = findViewById(R.id.tvTabStats);
    }

    private void caiDatSuKien() {
        btnDangXuat.setOnClickListener(v -> dangXuat());

        tabTrangChu.setOnClickListener(v -> hienThiTab(1));
        tabBaiTap.setOnClickListener(v -> hienThiTab(2));
        tabLich.setOnClickListener(v -> hienThiTab(3));
        tabThongKe.setOnClickListener(v -> hienThiTab(4));
    }

    private void capNhatMauTabNavigation(int tabIndex) {
        int colorActive = Color.parseColor("#713E87");
        int colorInactive = Color.parseColor("#6B5E74");

        tvTabHome.setTextColor(tabIndex == 1 ? colorActive : colorInactive);
        tvTabAssignment.setTextColor(tabIndex == 2 ? colorActive : colorInactive);
        tvTabCalendar.setTextColor(tabIndex == 3 ? colorActive : colorInactive);
        tvTabStats.setTextColor(tabIndex == 4 ? colorActive : colorInactive);

        ivTabHome.setColorFilter(tabIndex == 1 ? colorActive : colorInactive);
        ivTabAssignment.setColorFilter(tabIndex == 2 ? colorActive : colorInactive);
        ivTabCalendar.setColorFilter(tabIndex == 3 ? colorActive : colorInactive);
        ivTabStats.setColorFilter(tabIndex == 4 ? colorActive : colorInactive);
    }

    private void hienThiTab(int tabIndex) {
        activeTab = tabIndex;
        capNhatMauTabNavigation(tabIndex);
        layoutTabContentContainer.removeAllViews();

        switch (tabIndex) {
            case 1:
                layoutTabContentContainer.addView(layViewTabHome());
                break;
            case 2:
                layoutTabContentContainer.addView(layViewTabAssignments());
                break;
            case 3:
                layoutTabContentContainer.addView(layViewTabCalendar());
                break;
            case 4:
                layoutTabContentContainer.addView(layViewTabStats());
                break;
        }
    }

    // ==========================================
    // TAB 1: TRANG CHỦ
    // ==========================================
    private View layViewTabHome() {
        viewTabHome = LayoutInflater.from(this).inflate(R.layout.tab_student_home, null);

        View cardHanGanNhat = viewTabHome.findViewById(R.id.cardHanGanNhat);
        TextView tvTagHanGanNhatNguon = viewTabHome.findViewById(R.id.tvTagHanGanNhatNguon);
        TextView tvTenBaiGanNhat = viewTabHome.findViewById(R.id.tvTenBaiGanNhat);
        TextView tvNguonBaiGanNhat = viewTabHome.findViewById(R.id.tvNguonBaiGanNhat);
        TextView tvHanNopGanNhat = viewTabHome.findViewById(R.id.tvHanNopGanNhat);
        TextView tvPercentGanNhat = viewTabHome.findViewById(R.id.tvPercentGanNhat);
        ProgressBar progressGanNhat = viewTabHome.findViewById(R.id.progressGanNhat);
        RecyclerView rvVieccanLam = viewTabHome.findViewById(R.id.rvVieccanLam);

        rvVieccanLam.setLayoutManager(new LinearLayoutManager(this));

        List<BaiTap> ds = dbHelper.layDanhSachBaiTapCuaSinhVien(sinhVienId, null, "Tất cả", "Tất cả", "Hạn nộp");

        if (!ds.isEmpty()) {
            BaiTap ganNhat = ds.get(0);
            tvTenBaiGanNhat.setText(ganNhat.getTenBaiTap());
            tvNguonBaiGanNhat.setText(ganNhat.getTenNguon() != null ? ganNhat.getTenNguon() : "Bài tập");
            tvHanNopGanNhat.setText("Hạn: " + ganNhat.getHanNop());
            tvPercentGanNhat.setText(ganNhat.getProgressPercent() + "%");
            progressGanNhat.setProgress(ganNhat.getProgressPercent());

            if ("TEACHER".equals(ganNhat.getNguonBai())) {
                tvTagHanGanNhatNguon.setText("Giáo viên giao");
                tvTagHanGanNhatNguon.setBackgroundResource(R.drawable.bg_badge_teacher);
                tvTagHanGanNhatNguon.setTextColor(Color.parseColor("#713E87"));
            } else {
                tvTagHanGanNhatNguon.setText("Cá nhân");
                tvTagHanGanNhatNguon.setBackgroundResource(R.drawable.bg_badge_personal);
                tvTagHanGanNhatNguon.setTextColor(Color.parseColor("#0284C7"));
            }

            cardHanGanNhat.setOnClickListener(v -> moChiTietBaiTap(ganNhat.getId()));
        } else {
            tvTenBaiGanNhat.setText("Không có bài tập nào gần hạn");
            tvNguonBaiGanNhat.setText("Bạn đã hoàn thành hết các bài tập!");
            tvHanNopGanNhat.setText("");
            tvPercentGanNhat.setText("0%");
            progressGanNhat.setProgress(0);
        }

        BaiTapAdapter adapter = new BaiTapAdapter(this, ds, this::moChiTietBaiTapObject);
        rvVieccanLam.setAdapter(adapter);

        return viewTabHome;
    }

    // ==========================================
    // TAB 2: BÀI TẬP
    // ==========================================
    private View layViewTabAssignments() {
        viewTabAssignments = LayoutInflater.from(this).inflate(R.layout.tab_student_assignments, null);

        Button btnThemBaiTapCaNhan = viewTabAssignments.findViewById(R.id.btnThemBaiTapCaNhan);
        EditText etTimKiemBaiTap = viewTabAssignments.findViewById(R.id.etTimKiemBaiTap);
        Spinner spnLocMon = viewTabAssignments.findViewById(R.id.spnLocMon);
        Spinner spnLocTrangThai = viewTabAssignments.findViewById(R.id.spnLocTrangThai);
        Spinner spnSapXep = viewTabAssignments.findViewById(R.id.spnSapXep);
        RecyclerView rvDanhSachBaiTap = viewTabAssignments.findViewById(R.id.rvDanhSachBaiTap);

        rvDanhSachBaiTap.setLayoutManager(new LinearLayoutManager(this));

        // Load danh sách môn/lớp cho Spinner lọc
        List<String> listMon = new ArrayList<>();
        listMon.add("Tất cả");
        List<LopHoc> dsLop = dbHelper.layDanhSachLopCuaSinhVien(sinhVienId);
        for (LopHoc l : dsLop) {
            listMon.add(l.getTenLop());
        }
        List<MonHocCaNhan> dsMonCn = dbHelper.layDanhSachMonHocCaNhan(sinhVienId);
        for (MonHocCaNhan m : dsMonCn) {
            listMon.add(m.getTenMon());
        }

        ArrayAdapter<String> adapterMon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMon);
        adapterMon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocMon.setAdapter(adapterMon);

        ArrayAdapter<String> adapterTT = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Chưa làm", "Đang làm", "Hoàn thành", "Quá hạn"});
        adapterTT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocTrangThai.setAdapter(adapterTT);

        ArrayAdapter<String> adapterSX = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Hạn nộp", "Ưu tiên"});
        adapterSX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSapXep.setAdapter(adapterSX);

        Runnable loadData = () -> {
            String kw = etTimKiemBaiTap.getText().toString().trim();
            String filterMon = spnLocMon.getSelectedItem() != null ? spnLocMon.getSelectedItem().toString() : "Tất cả";
            String filterTT = spnLocTrangThai.getSelectedItem() != null ? spnLocTrangThai.getSelectedItem().toString() : "Tất cả";
            String sortBy = spnSapXep.getSelectedItem() != null ? spnSapXep.getSelectedItem().toString() : "Hạn nộp";

            List<BaiTap> ds = dbHelper.layDanhSachBaiTapCuaSinhVien(sinhVienId, kw, filterMon, filterTT, sortBy);
            BaiTapAdapter adapter = new BaiTapAdapter(this, ds, this::moChiTietBaiTapObject);
            rvDanhSachBaiTap.setAdapter(adapter);
        };

        etTimKiemBaiTap.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadData.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        AdapterView.OnItemSelectedListener itemListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { loadData.run(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spnLocMon.setOnItemSelectedListener(itemListener);
        spnLocTrangThai.setOnItemSelectedListener(itemListener);
        spnSapXep.setOnItemSelectedListener(itemListener);

        btnThemBaiTapCaNhan.setOnClickListener(v -> hienDialogThemBaiTapCaNhan(loadData));

        loadData.run();
        return viewTabAssignments;
    }

    // ==========================================
    // TAB 3: LỊCH HẠN NỘP
    // ==========================================
    private View layViewTabCalendar() {
        viewTabCalendar = LayoutInflater.from(this).inflate(R.layout.tab_student_calendar, null);

        Switch swNhacHan = viewTabCalendar.findViewById(R.id.swNhacHan);
        RecyclerView rvLichTuan = viewTabCalendar.findViewById(R.id.rvLichTuan);

        rvLichTuan.setLayoutManager(new LinearLayoutManager(this));

        swNhacHan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.capNhatNhacHanSinhVien(sinhVienId, isChecked);
            Toast.makeText(this, isChecked ? "Đã bật nhắc hạn bài tập 🔔" : "Đã tắt nhắc hạn bài tập 🔕", Toast.LENGTH_SHORT).show();
        });

        List<BaiTap> ds = dbHelper.layDanhSachBaiTapCuaSinhVien(sinhVienId, null, "Tất cả", "Tất cả", "Hạn nộp");
        BaiTapAdapter adapter = new BaiTapAdapter(this, ds, this::moChiTietBaiTapObject);
        rvLichTuan.setAdapter(adapter);

        return viewTabCalendar;
    }

    // ==========================================
    // TAB 4: THỐNG KÊ & XUẤT CSV
    // ==========================================
    private View layViewTabStats() {
        viewTabStats = LayoutInflater.from(this).inflate(R.layout.tab_student_stats, null);

        TextView tvStatChuaLam = viewTabStats.findViewById(R.id.tvStatChuaLam);
        TextView tvStatDangLam = viewTabStats.findViewById(R.id.tvStatDangLam);
        TextView tvStatHoanThanh = viewTabStats.findViewById(R.id.tvStatHoanThanh);
        TextView tvStatQuaHan = viewTabStats.findViewById(R.id.tvStatQuaHan);
        Button btnXuatBaoCaoCSV = viewTabStats.findViewById(R.id.btnXuatBaoCaoCSV);

        Map<String, Integer> stats = dbHelper.layThongKeSinhVien(sinhVienId);
        tvStatChuaLam.setText(String.valueOf(stats.get("Chưa làm")));
        tvStatDangLam.setText(String.valueOf(stats.get("Đang làm")));
        tvStatHoanThanh.setText(String.valueOf(stats.get("Hoàn thành")));
        tvStatQuaHan.setText(String.valueOf(stats.get("Quá hạn")));

        btnXuatBaoCaoCSV.setOnClickListener(v -> xuatBaoCaoCSV());

        return viewTabStats;
    }

    /**
     * Xuất file CSV báo cáo tiến độ học tập và chia sẻ
     */
    private void xuatBaoCaoCSV() {
        List<BaiTap> ds = dbHelper.layDanhSachBaiTapCuaSinhVien(sinhVienId, null, "Tất cả", "Tất cả", "Hạn nộp");

        StringBuilder csvText = new StringBuilder();
        // BOM UTF-8 để Excel hiển thị đúng tiếng Việt
        csvText.append("\uFEFF");
        csvText.append("STT,Tên Bài Tập,Nguồn Bài,Lớp / Môn Học,Trạng Thái,Mức Ưu Tiên,Tiến Độ (%),Hạn Nộp\n");

        int stt = 1;
        for (BaiTap bt : ds) {
            csvText.append(stt++).append(",");
            csvText.append("\"").append(bt.getTenBaiTap().replace("\"", "\"\"")).append("\",");
            csvText.append("\"").append("TEACHER".equals(bt.getNguonBai()) ? "Giáo viên giao" : "Cá nhân").append("\",");
            csvText.append("\"").append((bt.getTenNguon() != null ? bt.getTenNguon() : "").replace("\"", "\"\"")).append("\",");
            csvText.append("\"").append(bt.getTrangThai()).append("\",");
            csvText.append("\"").append(bt.getMucUuTien()).append("\",");
            csvText.append(bt.getProgressPercent()).append(",");
            csvText.append("\"").append(bt.getHanNop()).append("\"\n");
        }

        try {
            File cacheDir = getExternalCacheDir();
            if (cacheDir == null) cacheDir = getCacheDir();
            File csvFile = new File(cacheDir, "BaoCao_TienDo_SinhVien.csv");

            FileOutputStream out = new FileOutputStream(csvFile);
            out.write(csvText.toString().getBytes("UTF-8"));
            out.close();

            Toast.makeText(this, "Đã xuất file CSV thành công!\n" + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Mở Intent chia sẻ file CSV
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", csvFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ Báo cáo CSV"));

        } catch (Exception e) {
            Toast.makeText(this, "Đã xuất file CSV thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dialog tạo Bài tập cá nhân mới
     */
    private void hienDialogThemBaiTapCaNhan(Runnable onDone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm bài tập cá nhân");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_bai_tap_ca_nhan, null);
        EditText etTenBai = dialogView.findViewById(R.id.etTenBaiTapDialog);
        EditText etMoTa = dialogView.findViewById(R.id.etMoTaDialog);
        EditText etHanNop = dialogView.findViewById(R.id.etHanNopDialog);
        Spinner spnMonHoc = dialogView.findViewById(R.id.spnMonHocDialog);

        List<MonHocCaNhan> dsMon = dbHelper.layDanhSachMonHocCaNhan(sinhVienId);
        if (dsMon.isEmpty()) {
            dbHelper.themMonHocCaNhan(new MonHocCaNhan(0, sinhVienId, "Môn cá nhân", "CN01", "Môn cá nhân mặc định"));
            dsMon = dbHelper.layDanhSachMonHocCaNhan(sinhVienId);
        }

        List<String> tenMonList = new ArrayList<>();
        for (MonHocCaNhan m : dsMon) {
            tenMonList.add(m.getTenMon());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenMonList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonHoc.setAdapter(adapter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59", Locale.getDefault());
        etHanNop.setText(sdf.format(new Date()));

        builder.setView(dialogView);
        final List<MonHocCaNhan> finalDsMon = dsMon;
        builder.setPositiveButton("Tạo bài tập", (dialog, which) -> {
            String ten = etTenBai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String hanNop = etHanNop.getText().toString().trim();

            if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(hanNop)) {
                Toast.makeText(this, "Vui lòng nhập tên bài tập và hạn nộp!", Toast.LENGTH_SHORT).show();
                return;
            }

            int monId = finalDsMon.get(spnMonHoc.getSelectedItemPosition()).getId();
            BaiTap bt = new BaiTap(0, null, monId, sinhVienId, ten, moTa, hanNop,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), "PERSONAL");

            long id = dbHelper.themBaiTap(bt);
            if (id != -1) {
                dbHelper.layHoacTaoTienDo((int) id, sinhVienId);
                Toast.makeText(this, "Đã thêm bài tập cá nhân thành công!", Toast.LENGTH_SHORT).show();
                if (onDone != null) onDone.run();
            } else {
                Toast.makeText(this, "Tạo bài tập thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void moChiTietBaiTapObject(BaiTap bt) {
        moChiTietBaiTap(bt.getId());
    }

    private void moChiTietBaiTap(int baiTapId) {
        Intent intent = new Intent(StudentHomeActivity.this, AssignmentDetailActivity.class);
        intent.putExtra("BAI_TAP_ID", baiTapId);
        startActivity(intent);
    }

    private void dangXuat() {
        sessionManager.xoaPhienDangNhap();
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StudentHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
