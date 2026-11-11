package com.example.quanlybaitapsinhvien;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
 * Màn hình Chi tiết bài tập và Cập nhật Tiến độ / Checklist công việc con
 */
public class AssignmentDetailActivity extends AppCompatActivity {

    private View btnQuayLaiDetail;
    private TextView tvDetailTagNguon, tvDetailHanNop, tvDetailTenBaiTap, tvDetailNguon, tvDetailMoTa;
    private View layoutThaoTacCaNhan;
    private Button btnSuaBaiCaNhan, btnXoaBaiCaNhan;

    private Spinner spnMucUuTien, spnTrangThai;
    private Button btnLuuTienDo;

    private TextView tvChecklistPercent;
    private ProgressBar progressChecklist;
    private EditText etNoiDungChecklist;
    private Button btnThemChecklist;
    private RecyclerView rvChecklist;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private int baiTapId = -1;
    private int sinhVienId = -1;
    private BaiTap curBaiTap;
    private TienDoSinhVien curTienDo;

    private ChecklistAdapter checklistAdapter;
    private final List<ChecklistItem> dsChecklist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        docDuLieuIntent();
        khoiTaoViews();
        caiDatSpinners();
        loadThongTinBaiTap();
        caiDatSuKien();
    }

    private void docDuLieuIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            baiTapId = intent.getIntExtra("BAI_TAP_ID", -1);
        }
        sinhVienId = sessionManager.layUserId();
    }

    private void khoiTaoViews() {
        btnQuayLaiDetail = findViewById(R.id.btnQuayLaiDetail);
        tvDetailTagNguon = findViewById(R.id.tvDetailTagNguon);
        tvDetailHanNop = findViewById(R.id.tvDetailHanNop);
        tvDetailTenBaiTap = findViewById(R.id.tvDetailTenBaiTap);
        tvDetailNguon = findViewById(R.id.tvDetailNguon);
        tvDetailMoTa = findViewById(R.id.tvDetailMoTa);

        layoutThaoTacCaNhan = findViewById(R.id.layoutThaoTacCaNhan);
        btnSuaBaiCaNhan = findViewById(R.id.btnSuaBaiCaNhan);
        btnXoaBaiCaNhan = findViewById(R.id.btnXoaBaiCaNhan);

        spnMucUuTien = findViewById(R.id.spnMucUuTien);
        spnTrangThai = findViewById(R.id.spnTrangThai);
        btnLuuTienDo = findViewById(R.id.btnLuuTienDo);

        tvChecklistPercent = findViewById(R.id.tvChecklistPercent);
        progressChecklist = findViewById(R.id.progressChecklist);
        etNoiDungChecklist = findViewById(R.id.etNoiDungChecklist);
        btnThemChecklist = findViewById(R.id.btnThemChecklist);
        rvChecklist = findViewById(R.id.rvChecklist);

        rvChecklist.setLayoutManager(new LinearLayoutManager(this));
    }

    private void caiDatSpinners() {
        ArrayAdapter<String> adapterUuTien = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Bình thường", "Cao", "Khẩn cấp"});
        adapterUuTien.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMucUuTien.setAdapter(adapterUuTien);

        ArrayAdapter<String> adapterTrangThai = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Chưa làm", "Đang làm", "Hoàn thành"});
        adapterTrangThai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTrangThai.setAdapter(adapterTrangThai);
    }

    private void loadThongTinBaiTap() {
        if (baiTapId == -1 || sinhVienId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin bài tập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        curBaiTap = dbHelper.layBaiTapTheoId(baiTapId, sinhVienId);
        if (curBaiTap == null) {
            Toast.makeText(this, "Bài tập không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        curTienDo = dbHelper.layHoacTaoTienDo(baiTapId, sinhVienId);

        tvDetailTenBaiTap.setText(curBaiTap.getTenBaiTap());
        tvDetailNguon.setText("Môn/Lớp: " + (curBaiTap.getTenNguon() != null ? curBaiTap.getTenNguon() : "Cá nhân"));
        tvDetailHanNop.setText("Hạn: " + curBaiTap.getHanNop());
        tvDetailMoTa.setText(curBaiTap.getMoTa() != null && !curBaiTap.getMoTa().isEmpty() ? curBaiTap.getMoTa() : "Không có mô tả.");

        if ("TEACHER".equals(curBaiTap.getNguonBai())) {
            tvDetailTagNguon.setText("Giáo viên giao");
            tvDetailTagNguon.setBackgroundResource(R.drawable.bg_badge_teacher);
            tvDetailTagNguon.setTextColor(Color.parseColor("#713E87"));
            layoutThaoTacCaNhan.setVisibility(View.GONE);
        } else {
            tvDetailTagNguon.setText("Cá nhân");
            tvDetailTagNguon.setBackgroundResource(R.drawable.bg_badge_personal);
            tvDetailTagNguon.setTextColor(Color.parseColor("#0284C7"));
            layoutThaoTacCaNhan.setVisibility(View.VISIBLE);
        }

        // Set Spinners
        if (curTienDo.getMucUuTien() != null) {
            switch (curTienDo.getMucUuTien()) {
                case "Cao":
                    spnMucUuTien.setSelection(1);
                    break;
                case "Khẩn cấp":
                    spnMucUuTien.setSelection(2);
                    break;
                default:
                    spnMucUuTien.setSelection(0);
                    break;
            }
        }

        if (curTienDo.getTrangThai() != null) {
            switch (curTienDo.getTrangThai()) {
                case "Đang làm":
                    spnTrangThai.setSelection(1);
                    break;
                case "Hoàn thành":
                    spnTrangThai.setSelection(2);
                    break;
                default:
                    spnTrangThai.setSelection(0);
                    break;
            }
        }

        loadChecklist();
    }

    private void loadChecklist() {
        if (curTienDo == null) return;
        dsChecklist.clear();
        dsChecklist.addAll(dbHelper.layChecklist(curTienDo.getId()));

        if (checklistAdapter == null) {
            checklistAdapter = new ChecklistAdapter(this, dsChecklist, new ChecklistAdapter.OnChecklistChangeListener() {
                @Override
                public void onCheckChanged(ChecklistItem item, boolean isChecked) {
                    dbHelper.capNhatChecklistItem(item.getId(), isChecked ? 1 : 0, curTienDo.getId());
                    loadThongTinTienDoSauCapNhat();
                }

                @Override
                public void onDeleteItem(ChecklistItem item) {
                    dbHelper.xoaChecklistItem(item.getId(), curTienDo.getId());
                    loadThongTinTienDoSauCapNhat();
                }
            });
            rvChecklist.setAdapter(checklistAdapter);
        } else {
            checklistAdapter.notifyDataSetChanged();
        }

        capNhatGiaoDienProgress();
    }

    private void loadThongTinTienDoSauCapNhat() {
        curTienDo = dbHelper.layHoacTaoTienDo(baiTapId, sinhVienId);
        loadChecklist();
        if (curTienDo.getTrangThai() != null) {
            switch (curTienDo.getTrangThai()) {
                case "Đang làm":
                    spnTrangThai.setSelection(1);
                    break;
                case "Hoàn thành":
                    spnTrangThai.setSelection(2);
                    break;
                default:
                    spnTrangThai.setSelection(0);
                    break;
            }
        }
    }

    private void capNhatGiaoDienProgress() {
        int percent = curTienDo.getPercentTienDo();
        tvChecklistPercent.setText(percent + "%");
        progressChecklist.setProgress(percent);
    }

    private void caiDatSuKien() {
        btnQuayLaiDetail.setOnClickListener(v -> finish());

        // Nút Sửa bài cá nhân
        btnSuaBaiCaNhan.setOnClickListener(v -> hienDialogSuaBaiCaNhan());

        // Nút Xóa bài cá nhân
        btnXoaBaiCaNhan.setOnClickListener(v -> xacNhanXoaBaiCaNhan());

        // Nút Lưu tiến độ
        btnLuuTienDo.setOnClickListener(v -> {
            String uuTien = spnMucUuTien.getSelectedItem().toString();
            String trangThai = spnTrangThai.getSelectedItem().toString();

            curTienDo.setMucUuTien(uuTien);
            curTienDo.setTrangThai(trangThai);
            dbHelper.capNhatTienDo(curTienDo);

            Toast.makeText(this, "Đã lưu cập nhật tiến độ cá nhân!", Toast.LENGTH_SHORT).show();
        });

        // Nút Thêm mục Checklist
        btnThemChecklist.setOnClickListener(v -> {
            String noiDung = etNoiDungChecklist.getText().toString().trim();
            if (TextUtils.isEmpty(noiDung)) {
                etNoiDungChecklist.setError("Vui lòng nhập nội dung công việc con");
                return;
            }

            boolean res = dbHelper.themChecklistItem(curTienDo.getId(), noiDung);
            if (res) {
                etNoiDungChecklist.setText("");
                loadThongTinTienDoSauCapNhat();
                Toast.makeText(this, "Đã thêm mục công việc con!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thêm thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Dialog sửa thông tin bài tập cá nhân
     */
    private void hienDialogSuaBaiCaNhan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa bài tập cá nhân");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_bai_tap_ca_nhan, null);
        EditText etTenBai = dialogView.findViewById(R.id.etTenBaiTapDialog);
        EditText etMoTa = dialogView.findViewById(R.id.etMoTaDialog);
        EditText etHanNop = dialogView.findViewById(R.id.etHanNopDialog);
        Spinner spnMonHoc = dialogView.findViewById(R.id.spnMonHocDialog);
        spnMonHoc.setVisibility(View.GONE);

        etTenBai.setText(curBaiTap.getTenBaiTap());
        etMoTa.setText(curBaiTap.getMoTa());
        etHanNop.setText(curBaiTap.getHanNop());

        builder.setView(dialogView);
        builder.setPositiveButton("Lưu thay đổi", (dialog, which) -> {
            String ten = etTenBai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String hanNop = etHanNop.getText().toString().trim();

            if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(hanNop)) {
                Toast.makeText(this, "Vui lòng nhập tên bài và hạn nộp!", Toast.LENGTH_SHORT).show();
                return;
            }

            curBaiTap.setTenBaiTap(ten);
            curBaiTap.setMoTa(moTa);
            curBaiTap.setHanNop(hanNop);

            dbHelper.capNhatBaiTap(curBaiTap);
            loadThongTinBaiTap();
            Toast.makeText(this, "Đã cập nhật bài tập cá nhân!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Dialog xác nhận xóa bài tập cá nhân
     */
    private void xacNhanXoaBaiCaNhan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa bài tập");
        builder.setMessage("Bạn có chắc chắn muốn xóa bài tập cá nhân này không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            dbHelper.xoaBaiTap(curBaiTap.getId());
            Toast.makeText(this, "Đã xóa bài tập!", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
