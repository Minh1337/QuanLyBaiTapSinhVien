package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Trang chủ Quản trị viên: Quản lý tài khoản, Quản lý lớp học phần, Giám sát hệ thống
 */
public class AdminHomeActivity extends AppCompatActivity {

    private TextView tvAdminGreeting;
    private ImageView btnDangXuatAdmin;

    // Section 1: Tài khoản
    private Button btnTaoGiaoVien;
    private RecyclerView rvTaiKhoanAdmin;

    // Section 2: Lớp học phần
    private Button btnTaoLopMoi;
    private RecyclerView rvLopHocAdmin;

    // Section 3: Giám sát bài tập
    private RecyclerView rvBaiTapAdmin;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        khoiTaoViews();
        caiDatSuKien();

        loadDanhSachTaiKhoan();
        loadDanhSachLopHoc();
        loadDanhSachBaiTapHeThong();
    }

    private void khoiTaoViews() {
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        btnDangXuatAdmin = findViewById(R.id.btnDangXuatAdmin);

        btnTaoGiaoVien = findViewById(R.id.btnTaoGiaoVien);
        rvTaiKhoanAdmin = findViewById(R.id.rvTaiKhoanAdmin);
        rvTaiKhoanAdmin.setLayoutManager(new LinearLayoutManager(this));

        btnTaoLopMoi = findViewById(R.id.btnTaoLopMoi);
        rvLopHocAdmin = findViewById(R.id.rvLopHocAdmin);
        rvLopHocAdmin.setLayoutManager(new LinearLayoutManager(this));

        rvBaiTapAdmin = findViewById(R.id.rvBaiTapAdmin);
        rvBaiTapAdmin.setLayoutManager(new LinearLayoutManager(this));
    }

    private void caiDatSuKien() {
        btnDangXuatAdmin.setOnClickListener(v -> dangXuat());
        btnTaoGiaoVien.setOnClickListener(v -> hienDialogTaoGiaoVien());
        btnTaoLopMoi.setOnClickListener(v -> hienDialogTaoLopMoi());
    }

    // ==========================================
    // SECTION 1: QUẢN LÝ TÀI KHOẢN
    // ==========================================
    private void loadDanhSachTaiKhoan() {
        List<TaiKhoan> ds = dbHelper.layTatCaTaiKhoan();
        TaiKhoanAdapter adapter = new TaiKhoanAdapter(this, ds, taiKhoan -> {
            if ("ADMIN".equals(taiKhoan.getVaiTro())) {
                Toast.makeText(this, "Không thể khóa tài khoản Admin duy nhất!", Toast.LENGTH_SHORT).show();
                return;
            }
            int newStatus = taiKhoan.getTrangThai() == 1 ? 0 : 1;
            dbHelper.capNhatTrangThaiTaiKhoan(taiKhoan.getId(), newStatus);
            Toast.makeText(this, newStatus == 0 ? "Đã khóa tài khoản " + taiKhoan.getUsername() : "Đã mở khóa tài khoản " + taiKhoan.getUsername(), Toast.LENGTH_SHORT).show();
            loadDanhSachTaiKhoan();
        });
        rvTaiKhoanAdmin.setAdapter(adapter);
    }

    private void hienDialogTaoGiaoVien() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo tài khoản Giáo viên mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tao_giao_vien, null);
        EditText etUsername = dialogView.findViewById(R.id.etUsernameGv);
        EditText etMatKhau = dialogView.findViewById(R.id.etMatKhauGv);
        EditText etHoTen = dialogView.findViewById(R.id.etHoTenGv);

        builder.setView(dialogView);
        builder.setPositiveButton("Tạo tài khoản", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String matKhau = etMatKhau.getText().toString().trim();
            String hoTen = etHoTen.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(matKhau) || TextUtils.isEmpty(hoTen)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.layTaiKhoanTheoUsername(username) != null) {
                Toast.makeText(this, "Tên đăng nhập này đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            TaiKhoan gv = new TaiKhoan(0, username, username + "@hnue.edu.vn", matKhau, hoTen, "TEACHER", 1);
            boolean res = dbHelper.themTaiKhoan(gv);
            if (res) {
                Toast.makeText(this, "Đã tạo tài khoản Giáo viên thành công!", Toast.LENGTH_SHORT).show();
                loadDanhSachTaiKhoan();
            } else {
                Toast.makeText(this, "Tạo tài khoản thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ==========================================
    // SECTION 2: QUẢN LÝ LỚP HỌC PHẦN
    // ==========================================
    private void loadDanhSachLopHoc() {
        List<LopHoc> ds = dbHelper.layDanhSachLopHoc();
        LopHocAdapter adapter = new LopHocAdapter(this, ds, new LopHocAdapter.OnLopHocActionListener() {
            @Override
            public void onDoiGv(LopHoc lopHoc) {
                hienDialogDoiGiaoVien(lopHoc);
            }

            @Override
            public void onThemSv(LopHoc lopHoc) {
                hienDialogThemSinhVienVaoLop(lopHoc);
            }

            @Override
            public void onBotSv(LopHoc lopHoc) {
                hienDialogBotSinhVienKhoiLop(lopHoc);
            }
        });
        rvLopHocAdmin.setAdapter(adapter);
    }

    private void hienDialogTaoLopMoi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo Lớp Học Phần Mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tao_lop_hoc, null);
        EditText etMaLop = dialogView.findViewById(R.id.etMaLopDialog);
        EditText etTenLop = dialogView.findViewById(R.id.etTenLopDialog);
        EditText etMoTa = dialogView.findViewById(R.id.etMoTaLopDialog);
        Spinner spnGiaoVien = dialogView.findViewById(R.id.spnGiaoVienDialog);

        List<TaiKhoan> dsGv = new ArrayList<>();
        List<TaiKhoan> allAccounts = dbHelper.layTatCaTaiKhoan();
        for (TaiKhoan tk : allAccounts) {
            if ("TEACHER".equals(tk.getVaiTro()) && tk.getTrangThai() == 1) {
                dsGv.add(tk);
            }
        }

        if (dsGv.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo tài khoản Giáo viên trước!", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> tenGvList = new ArrayList<>();
        for (TaiKhoan gv : dsGv) {
            tenGvList.add(gv.getHoTen() + " (@" + gv.getUsername() + ")");
        }

        ArrayAdapter<String> adapterGv = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenGvList);
        adapterGv.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGiaoVien.setAdapter(adapterGv);

        builder.setView(dialogView);
        builder.setPositiveButton("Tạo lớp", (dialog, which) -> {
            String maLop = etMaLop.getText().toString().trim();
            String tenLop = etTenLop.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();

            if (TextUtils.isEmpty(maLop) || TextUtils.isEmpty(tenLop)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã lớp và tên lớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.kiemTraMaLopTonTai(maLop)) {
                Toast.makeText(this, "Mã lớp này đã tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGvId = dsGv.get(spnGiaoVien.getSelectedItemPosition()).getId();
            LopHoc lh = new LopHoc(0, maLop, tenLop, selectedGvId, moTa, 0);

            boolean res = dbHelper.themLopHoc(lh);
            if (res) {
                Toast.makeText(this, "Đã tạo lớp học phần thành công!", Toast.LENGTH_SHORT).show();
                loadDanhSachLopHoc();
            } else {
                Toast.makeText(this, "Tạo lớp thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void hienDialogDoiGiaoVien(LopHoc lopHoc) {
        List<TaiKhoan> dsGv = new ArrayList<>();
        for (TaiKhoan tk : dbHelper.layTatCaTaiKhoan()) {
            if ("TEACHER".equals(tk.getVaiTro()) && tk.getTrangThai() == 1) {
                dsGv.add(tk);
            }
        }

        if (dsGv.isEmpty()) {
            Toast.makeText(this, "Không có giáo viên khả dụng!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tenGvList = new ArrayList<>();
        for (TaiKhoan gv : dsGv) {
            tenGvList.add(gv.getHoTen() + " (@" + gv.getUsername() + ")");
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapterGv = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenGvList);
        adapterGv.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterGv);
        spinner.setPadding(40, 20, 40, 20);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi Giáo viên cho lớp " + lopHoc.getMaLop());
        builder.setView(spinner);
        builder.setPositiveButton("Đổi GV", (dialog, which) -> {
            int newGvId = dsGv.get(spinner.getSelectedItemPosition()).getId();
            dbHelper.capNhatGiaoVienLopHoc(lopHoc.getId(), newGvId);
            Toast.makeText(this, "Đã đổi giáo viên phụ trách lớp!", Toast.LENGTH_SHORT).show();
            loadDanhSachLopHoc();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void hienDialogThemSinhVienVaoLop(LopHoc lopHoc) {
        List<TaiKhoan> dsSvChuaTrongLop = dbHelper.layDanhSachSinhVienChuaTrongLop(lopHoc.getId());
        if (dsSvChuaTrongLop.isEmpty()) {
            Toast.makeText(this, "Tất cả sinh viên trong hệ thống đã thuộc lớp này!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tenSvList = new ArrayList<>();
        for (TaiKhoan sv : dsSvChuaTrongLop) {
            tenSvList.add(sv.getHoTen() + " (@" + sv.getUsername() + ")");
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapterSv = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenSvList);
        adapterSv.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSv);
        spinner.setPadding(40, 20, 40, 20);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Sinh viên vào lớp " + lopHoc.getMaLop());
        builder.setView(spinner);
        builder.setPositiveButton("Thêm SV", (dialog, which) -> {
            int svId = dsSvChuaTrongLop.get(spinner.getSelectedItemPosition()).getId();
            dbHelper.themThanhVienLop(lopHoc.getId(), svId);
            Toast.makeText(this, "Đã thêm sinh viên vào lớp!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void hienDialogBotSinhVienKhoiLop(LopHoc lopHoc) {
        List<TaiKhoan> dsSvTrongLop = dbHelper.layDanhSachSinhVienTrongLop(lopHoc.getId());
        if (dsSvTrongLop.isEmpty()) {
            Toast.makeText(this, "Lớp này hiện chưa có sinh viên nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tenSvList = new ArrayList<>();
        for (TaiKhoan sv : dsSvTrongLop) {
            tenSvList.add(sv.getHoTen() + " (@" + sv.getUsername() + ")");
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapterSv = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenSvList);
        adapterSv.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSv);
        spinner.setPadding(40, 20, 40, 20);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bớt Sinh viên khỏi lớp " + lopHoc.getMaLop());
        builder.setView(spinner);
        builder.setPositiveButton("Bớt SV", (dialog, which) -> {
            int svId = dsSvTrongLop.get(spinner.getSelectedItemPosition()).getId();
            dbHelper.xoaThanhVienLop(lopHoc.getId(), svId);
            Toast.makeText(this, "Đã xóa sinh viên khỏi lớp!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ==========================================
    // SECTION 3: GIÁM SÁT BÀI TẬP HỆ THỐNG
    // ==========================================
    private void loadDanhSachBaiTapHeThong() {
        List<BaiTap> ds = dbHelper.layTatCaBaiTapHeThong();
        BaiTapAdminAdapter adapter = new BaiTapAdminAdapter(this, ds, baiTap -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xóa bài tập hệ thống");
            builder.setMessage("Xác nhận xóa bài tập '" + baiTap.getTenBaiTap() + "' khỏi hệ thống?");
            builder.setPositiveButton("Xóa", (dialog, which) -> {
                dbHelper.xoaBaiTap(baiTap.getId());
                Toast.makeText(this, "Đã xóa bài tập hệ thống!", Toast.LENGTH_SHORT).show();
                loadDanhSachBaiTapHeThong();
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
        rvBaiTapAdmin.setAdapter(adapter);
    }

    private void dangXuat() {
        sessionManager.xoaPhienDangNhap();
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
