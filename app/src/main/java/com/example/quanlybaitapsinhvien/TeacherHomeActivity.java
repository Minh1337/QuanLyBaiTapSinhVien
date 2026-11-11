package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Trang chủ Giáo viên: Giao bài tập và Giám sát tiến độ từng sinh viên
 */
public class TeacherHomeActivity extends AppCompatActivity {

    private TextView tvTeacherGreeting;
    private ImageView btnDangXuatTeacher;
    private Spinner spnLopGiangDay;

    private Button btnGiaoBaiMoi;
    private RecyclerView rvBaiTapDaGiao;

    private Spinner spnBaiTapBaoCao;
    private RecyclerView rvBaoCaoSinhVien;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private int teacherId = -1;
    private List<LopHoc> dsLopGiangDay = new ArrayList<>();
    private LopHoc curLopHoc = null;

    private List<BaiTap> dsBaiTapClass = new ArrayList<>();
    private BaiTap curBaiTapBaoCao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        teacherId = sessionManager.layUserId();

        TaiKhoan tk = dbHelper.layTaiKhoanTheoId(teacherId);
        String hoTenGv = tk != null ? tk.getHoTen() : "Giáo viên";

        khoiTaoViews();
        tvTeacherGreeting.setText(hoTenGv);

        caiDatSuKien();
        loadDanhSachLopGiangDay();
    }

    private void khoiTaoViews() {
        tvTeacherGreeting = findViewById(R.id.tvTeacherGreeting);
        btnDangXuatTeacher = findViewById(R.id.btnDangXuatTeacher);
        spnLopGiangDay = findViewById(R.id.spnLopGiangDay);

        btnGiaoBaiMoi = findViewById(R.id.btnGiaoBaiMoi);
        rvBaiTapDaGiao = findViewById(R.id.rvBaiTapDaGiao);
        rvBaiTapDaGiao.setLayoutManager(new LinearLayoutManager(this));

        spnBaiTapBaoCao = findViewById(R.id.spnBaiTapBaoCao);
        rvBaoCaoSinhVien = findViewById(R.id.rvBaoCaoSinhVien);
        rvBaoCaoSinhVien.setLayoutManager(new LinearLayoutManager(this));
    }

    private void caiDatSuKien() {
        btnDangXuatTeacher.setOnClickListener(v -> dangXuat());

        btnGiaoBaiMoi.setOnClickListener(v -> hienDialogGiaoBaiMoi());

        spnLopGiangDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < dsLopGiangDay.size()) {
                    curLopHoc = dsLopGiangDay.get(position);
                    loadBaiTapLopHoc();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spnBaiTapBaoCao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < dsBaiTapClass.size()) {
                    curBaiTapBaoCao = dsBaiTapClass.get(position);
                    loadBaoCaoTienDo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadDanhSachLopGiangDay() {
        dsLopGiangDay = dbHelper.layDanhSachLopHocTheoGv(teacherId);
        if (dsLopGiangDay.isEmpty()) {
            Toast.makeText(this, "Hiện bạn chưa có lớp giảng dạy nào được phân công!", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> tenLopList = new ArrayList<>();
        for (LopHoc l : dsLopGiangDay) {
            tenLopList.add(l.getMaLop() + " - " + l.getTenLop());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenLopList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLopGiangDay.setAdapter(adapter);
    }

    private void loadBaiTapLopHoc() {
        if (curLopHoc == null) return;

        dsBaiTapClass = dbHelper.layDanhSachBaiTapGiaoVien(curLopHoc.getId());

        BaiTapTeacherAdapter adapter = new BaiTapTeacherAdapter(this, dsBaiTapClass, new BaiTapTeacherAdapter.OnTeacherAssignmentActionListener() {
            @Override
            public void onEdit(BaiTap baiTap) {
                hienDialogSuaBai(baiTap);
            }

            @Override
            public void onDelete(BaiTap baiTap) {
                hienDialogThuHoiBai(baiTap);
            }
        });
        rvBaiTapDaGiao.setAdapter(adapter);

        // Populate Spinner Bài tập Báo cáo
        List<String> tenBaiList = new ArrayList<>();
        for (BaiTap b : dsBaiTapClass) {
            tenBaiList.add(b.getTenBaiTap());
        }

        if (tenBaiList.isEmpty()) {
            tenBaiList.add("Chưa có bài tập nào");
        }

        ArrayAdapter<String> adapterBaoCao = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tenBaiList);
        adapterBaoCao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnBaiTapBaoCao.setAdapter(adapterBaoCao);

        if (!dsBaiTapClass.isEmpty()) {
            curBaiTapBaoCao = dsBaiTapClass.get(0);
            loadBaoCaoTienDo();
        } else {
            rvBaoCaoSinhVien.setAdapter(null);
        }
    }

    private void loadBaoCaoTienDo() {
        if (curLopHoc == null || curBaiTapBaoCao == null) return;
        List<TienDoSinhVien> dsBaoCao = dbHelper.layBaoCaoTiendoLop(curLopHoc.getId(), curBaiTapBaoCao.getId());
        BaoCaoTienDoAdapter adapter = new BaoCaoTienDoAdapter(this, dsBaoCao);
        rvBaoCaoSinhVien.setAdapter(adapter);
    }

    /**
     * Dialog Giao bài tập mới cho lớp
     */
    private void hienDialogGiaoBaiMoi() {
        if (curLopHoc == null) {
            Toast.makeText(this, "Vui lòng chọn lớp giảng dạy trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Giao bài tập mới cho lớp " + curLopHoc.getMaLop());

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_bai_tap_ca_nhan, null);
        EditText etTenBai = dialogView.findViewById(R.id.etTenBaiTapDialog);
        EditText etMoTa = dialogView.findViewById(R.id.etMoTaDialog);
        EditText etHanNop = dialogView.findViewById(R.id.etHanNopDialog);
        Spinner spnMonHoc = dialogView.findViewById(R.id.spnMonHocDialog);
        spnMonHoc.setVisibility(View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59", Locale.getDefault());
        etHanNop.setText(sdf.format(new Date()));

        builder.setView(dialogView);
        builder.setPositiveButton("Giao bài", (dialog, which) -> {
            String ten = etTenBai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String hanNop = etHanNop.getText().toString().trim();

            if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(hanNop)) {
                Toast.makeText(this, "Vui lòng nhập tên bài tập và hạn nộp!", Toast.LENGTH_SHORT).show();
                return;
            }

            BaiTap bt = new BaiTap(0, curLopHoc.getId(), null, teacherId, ten, moTa, hanNop,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), "TEACHER");

            long res = dbHelper.themBaiTap(bt);
            if (res != -1) {
                Toast.makeText(this, "Đã giao bài tập thành công!", Toast.LENGTH_SHORT).show();
                loadBaiTapLopHoc();
            } else {
                Toast.makeText(this, "Giao bài tập thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Dialog sửa bài tập đã giao
     */
    private void hienDialogSuaBai(BaiTap bt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa bài tập đã giao");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_bai_tap_ca_nhan, null);
        EditText etTenBai = dialogView.findViewById(R.id.etTenBaiTapDialog);
        EditText etMoTa = dialogView.findViewById(R.id.etMoTaDialog);
        EditText etHanNop = dialogView.findViewById(R.id.etHanNopDialog);
        Spinner spnMonHoc = dialogView.findViewById(R.id.spnMonHocDialog);
        spnMonHoc.setVisibility(View.GONE);

        etTenBai.setText(bt.getTenBaiTap());
        etMoTa.setText(bt.getMoTa());
        etHanNop.setText(bt.getHanNop());

        builder.setView(dialogView);
        builder.setPositiveButton("Lưu thay đổi", (dialog, which) -> {
            String ten = etTenBai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String hanNop = etHanNop.getText().toString().trim();

            if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(hanNop)) {
                Toast.makeText(this, "Vui lòng nhập tên bài tập và hạn nộp!", Toast.LENGTH_SHORT).show();
                return;
            }

            bt.setTenBaiTap(ten);
            bt.setMoTa(moTa);
            bt.setHanNop(hanNop);

            dbHelper.capNhatBaiTap(bt);
            Toast.makeText(this, "Đã cập nhật bài tập!", Toast.LENGTH_SHORT).show();
            loadBaiTapLopHoc();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Dialog thu hồi / xóa bài tập đã giao
     */
    private void hienDialogThuHoiBai(BaiTap bt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thu hồi bài tập");
        builder.setMessage("Bạn có chắc chắn muốn thu hồi bài tập '" + bt.getTenBaiTap() + "' khỏi lớp không?");
        builder.setPositiveButton("Thu hồi", (dialog, which) -> {
            dbHelper.xoaBaiTap(bt.getId());
            Toast.makeText(this, "Đã thu hồi bài tập!", Toast.LENGTH_SHORT).show();
            loadBaiTapLopHoc();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void dangXuat() {
        sessionManager.xoaPhienDangNhap();
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(TeacherHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
