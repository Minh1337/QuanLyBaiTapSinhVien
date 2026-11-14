package com.example.quanlybaitapsinhvien;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản trị hệ thống (AdminManagementActivity)
 * Chức năng dành cho Quản trị viên:
 * 1. Quản lý Tài khoản (Duyệt sinh viên đăng ký, Tạo tài khoản GV/SV, Khóa/Mở khóa tài khoản)
 * 2. Quản lý Lớp học / Môn học (Tạo lớp, Chặn trùng mã, Đổi giáo viên phụ trách)
 * 3. Xếp Sinh viên vào Lớp học (Thêm/Bớt sinh viên, Chặn trùng thành viên)
 */
public class AdminManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // Tabs navigation
    private TextView btnTabAccounts, btnTabClasses, btnTabAssignment;
    private View layoutTabAccounts, layoutTabClasses, layoutTabAssignment;

    // Tab 1: Accounts
    private RadioGroup rgNewUserRole;
    private RadioButton rbRoleTeacher, rbRoleStudent;
    private EditText etNewUserEmail, etNewUserPassword;
    private TextView btnCreateUser;
    private TextView btnFilterAllAccounts, btnFilterPendingAccounts, btnFilterTeacherAccounts, btnFilterStudentAccounts;
    private LinearLayout llAccountList;
    private String currentAccountFilter = "ALL"; // ALL, PENDING, TEACHER, STUDENT

    // Tab 2: Classes
    private EditText etClassCode, etClassName, etSchoolYear;
    private Spinner spTeachers;
    private TextView btnCreateClass;
    private LinearLayout llClassList;
    private List<TaiKhoan> teacherList = new ArrayList<>();

    // Tab 3: Assignment
    private Spinner spAssignClasses;
    private TextView btnAddStudentToSelectedClass;
    private TextView tvClassStudentListHeader;
    private LinearLayout llEnrolledStudentsList;
    private List<MonHoc> classList = new ArrayList<>();
    private MonHoc selectedClassForAssignment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_admin_management);

        // System Bar Insets
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

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        khoiTaoViews();
        caiDatSuKien();

        // Mặc định nạp dữ liệu tab 1
        moTab("ACCOUNTS");
    }

    private void khoiTaoViews() {
        // Navigation Tabs
        btnTabAccounts = findViewById(R.id.btnTabAccounts);
        btnTabClasses = findViewById(R.id.btnTabClasses);
        btnTabAssignment = findViewById(R.id.btnTabAssignment);

        layoutTabAccounts = findViewById(R.id.layoutTabAccounts);
        layoutTabClasses = findViewById(R.id.layoutTabClasses);
        layoutTabAssignment = findViewById(R.id.layoutTabAssignment);

        // Tab 1 Views
        rgNewUserRole = findViewById(R.id.rgNewUserRole);
        rbRoleTeacher = findViewById(R.id.rbRoleTeacher);
        rbRoleStudent = findViewById(R.id.rbRoleStudent);
        etNewUserEmail = findViewById(R.id.etNewUserEmail);
        etNewUserPassword = findViewById(R.id.etNewUserPassword);
        btnCreateUser = findViewById(R.id.btnCreateUser);

        btnFilterAllAccounts = findViewById(R.id.btnFilterAllAccounts);
        btnFilterPendingAccounts = findViewById(R.id.btnFilterPendingAccounts);
        btnFilterTeacherAccounts = findViewById(R.id.btnFilterTeacherAccounts);
        btnFilterStudentAccounts = findViewById(R.id.btnFilterStudentAccounts);
        llAccountList = findViewById(R.id.llAccountList);

        // Tab 2 Views
        etClassCode = findViewById(R.id.etClassCode);
        etClassName = findViewById(R.id.etClassName);
        etSchoolYear = findViewById(R.id.etSchoolYear);
        spTeachers = findViewById(R.id.spTeachers);
        btnCreateClass = findViewById(R.id.btnCreateClass);
        llClassList = findViewById(R.id.llClassList);

        // Tab 3 Views
        spAssignClasses = findViewById(R.id.spAssignClasses);
        btnAddStudentToSelectedClass = findViewById(R.id.btnAddStudentToSelectedClass);
        tvClassStudentListHeader = findViewById(R.id.tvClassStudentListHeader);
        llEnrolledStudentsList = findViewById(R.id.llEnrolledStudentsList);
    }

    private void caiDatSuKien() {
        // Swapping Tabs
        btnTabAccounts.setOnClickListener(v -> moTab("ACCOUNTS"));
        btnTabClasses.setOnClickListener(v -> moTab("CLASSES"));
        btnTabAssignment.setOnClickListener(v -> moTab("ASSIGNMENT"));

        // Tab 1: Admin Creates Account
        btnCreateUser.setOnClickListener(v -> xuLyTaoTaiKhoanBoiAdmin());

        // Account Filter Buttons
        btnFilterAllAccounts.setOnClickListener(v -> locDanhSachTaiKhoan("ALL"));
        btnFilterPendingAccounts.setOnClickListener(v -> locDanhSachTaiKhoan("PENDING"));
        btnFilterTeacherAccounts.setOnClickListener(v -> locDanhSachTaiKhoan("TEACHER"));
        btnFilterStudentAccounts.setOnClickListener(v -> locDanhSachTaiKhoan("STUDENT"));

        // Tab 2: Create Class
        btnCreateClass.setOnClickListener(v -> xuLyTaoMonHoc());

        // Tab 3: Add Student Button
        btnAddStudentToSelectedClass.setOnClickListener(v -> hienDialogThemSinhVienVaoLop());
    }

    private void moTab(String tabName) {
        layoutTabAccounts.setVisibility(View.GONE);
        layoutTabClasses.setVisibility(View.GONE);
        layoutTabAssignment.setVisibility(View.GONE);

        btnTabAccounts.setBackgroundResource(R.drawable.bg_home_pill);
        btnTabAccounts.setTextColor(getResources().getColor(R.color.blue_primary));

        btnTabClasses.setBackgroundResource(R.drawable.bg_home_pill);
        btnTabClasses.setTextColor(getResources().getColor(R.color.blue_primary));

        btnTabAssignment.setBackgroundResource(R.drawable.bg_home_pill);
        btnTabAssignment.setTextColor(getResources().getColor(R.color.blue_primary));

        if ("CLASSES".equals(tabName)) {
            layoutTabClasses.setVisibility(View.VISIBLE);
            btnTabClasses.setBackgroundResource(R.drawable.bg_blue_button);
            btnTabClasses.setTextColor(Color.WHITE);
            napDanhSachGiaoVienChoSpinner();
            napDanhSachMonHoc();
        } else if ("ASSIGNMENT".equals(tabName)) {
            layoutTabAssignment.setVisibility(View.VISIBLE);
            btnTabAssignment.setBackgroundResource(R.drawable.bg_blue_button);
            btnTabAssignment.setTextColor(Color.WHITE);
            napDanhSachLopChoAssignment();
        } else {
            // Default: ACCOUNTS
            layoutTabAccounts.setVisibility(View.VISIBLE);
            btnTabAccounts.setBackgroundResource(R.drawable.bg_blue_button);
            btnTabAccounts.setTextColor(Color.WHITE);
            napDanhSachTaiKhoan();
        }
    }

    // ==========================================
    // TAB 1: QUẢN LÝ TÀI KHOẢN & DUYỆT
    // ==========================================

    private void xuLyTaoTaiKhoanBoiAdmin() {
        String email = etNewUserEmail.getText().toString().trim();
        String pass = etNewUserPassword.getText().toString().trim();
        String vaiTro = rbRoleTeacher.isChecked() ? "TEACHER" : "STUDENT";

        if (TextUtils.isEmpty(email)) {
            etNewUserEmail.setError("Vui lòng nhập Email!");
            etNewUserEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etNewUserEmail.setError("Email không hợp lệ!");
            etNewUserEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 8) {
            etNewUserPassword.setError("Mật khẩu tối thiểu 8 ký tự!");
            etNewUserPassword.requestFocus();
            return;
        }

        if (dbHelper.kiemTraEmailTonTai(email)) {
            Toast.makeText(this, "Email này đã tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbHelper.taoTaiKhoanBoiAdmin(email, pass, vaiTro);
        if (ok) {
            Toast.makeText(this, "Tạo tài khoản " + (vaiTro.equals("TEACHER") ? "Giáo viên" : "Sinh viên") + " thành công!", Toast.LENGTH_SHORT).show();
            etNewUserEmail.setText("");
            etNewUserPassword.setText("");
            napDanhSachTaiKhoan();
        } else {
            Toast.makeText(this, "Lỗi tạo tài khoản, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void locDanhSachTaiKhoan(String filter) {
        currentAccountFilter = filter;

        btnFilterAllAccounts.setBackgroundResource(R.drawable.bg_home_pill);
        btnFilterAllAccounts.setTextColor(getResources().getColor(R.color.blue_primary));
        btnFilterPendingAccounts.setBackgroundResource(R.drawable.bg_home_pill);
        btnFilterPendingAccounts.setTextColor(getResources().getColor(R.color.blue_primary));
        btnFilterTeacherAccounts.setBackgroundResource(R.drawable.bg_home_pill);
        btnFilterTeacherAccounts.setTextColor(getResources().getColor(R.color.blue_primary));
        btnFilterStudentAccounts.setBackgroundResource(R.drawable.bg_home_pill);
        btnFilterStudentAccounts.setTextColor(getResources().getColor(R.color.blue_primary));

        if ("PENDING".equals(filter)) {
            btnFilterPendingAccounts.setBackgroundResource(R.drawable.bg_blue_button);
            btnFilterPendingAccounts.setTextColor(Color.WHITE);
        } else if ("TEACHER".equals(filter)) {
            btnFilterTeacherAccounts.setBackgroundResource(R.drawable.bg_blue_button);
            btnFilterTeacherAccounts.setTextColor(Color.WHITE);
        } else if ("STUDENT".equals(filter)) {
            btnFilterStudentAccounts.setBackgroundResource(R.drawable.bg_blue_button);
            btnFilterStudentAccounts.setTextColor(Color.WHITE);
        } else {
            btnFilterAllAccounts.setBackgroundResource(R.drawable.bg_blue_button);
            btnFilterAllAccounts.setTextColor(Color.WHITE);
        }

        napDanhSachTaiKhoan();
    }

    private void napDanhSachTaiKhoan() {
        llAccountList.removeAllViews();
        List<TaiKhoan> tatCa = dbHelper.layTatCaTaiKhoan();

        List<TaiKhoan> listHienThi = new ArrayList<>();
        for (TaiKhoan tk : tatCa) {
            if ("PENDING".equals(currentAccountFilter)) {
                if (tk.getTrangThai() == 0) listHienThi.add(tk);
            } else if ("TEACHER".equals(currentAccountFilter)) {
                if ("TEACHER".equalsIgnoreCase(tk.getVaiTro())) listHienThi.add(tk);
            } else if ("STUDENT".equals(currentAccountFilter)) {
                if ("STUDENT".equalsIgnoreCase(tk.getVaiTro())) listHienThi.add(tk);
            } else {
                listHienThi.add(tk);
            }
        }

        if (listHienThi.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không tìm thấy tài khoản nào.");
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setPadding(0, 20, 0, 20);
            llAccountList.addView(tvEmpty);
            return;
        }

        for (TaiKhoan tk : listHienThi) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_account, llAccountList, false);

            TextView tvEmail = itemView.findViewById(R.id.tvEmail);
            TextView tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            TextView tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            TextView btnApprove = itemView.findViewById(R.id.btnApprove);
            TextView btnToggleLock = itemView.findViewById(R.id.btnToggleLock);

            tvEmail.setText(tk.getEmail());

            String roleText = "STUDENT".equalsIgnoreCase(tk.getVaiTro()) ? "Sinh viên" :
                    ("TEACHER".equalsIgnoreCase(tk.getVaiTro()) ? "Giáo viên" : "Quản trị viên");
            tvRoleBadge.setText(roleText);

            if (tk.getTrangThai() == 1) {
                tvStatusBadge.setText("🟢 Đã duyệt / Hoạt động");
                tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                btnApprove.setVisibility(View.GONE);
                btnToggleLock.setText("🔒 Khóa");
            } else {
                tvStatusBadge.setText("🟠 Chờ Admin duyệt / Bị khóa");
                tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                btnApprove.setVisibility(View.VISIBLE);
                btnToggleLock.setText("🔓 Mở khóa");
            }

            // Bảo vệ tài khoản Admin duy nhất không bị tự khóa
            if ("admin@gmail.com".equalsIgnoreCase(tk.getEmail()) || "ADMIN".equalsIgnoreCase(tk.getVaiTro())) {
                btnToggleLock.setVisibility(View.GONE);
                btnApprove.setVisibility(View.GONE);
            } else {
                btnToggleLock.setVisibility(View.VISIBLE);
            }

            // Sự kiện Duyệt tài khoản
            btnApprove.setOnClickListener(v -> {
                boolean ok = dbHelper.duyetTaiKhoan(tk.getId());
                if (ok) {
                    Toast.makeText(this, "Đã duyệt tài khoản: " + tk.getEmail(), Toast.LENGTH_SHORT).show();
                    napDanhSachTaiKhoan();
                } else {
                    Toast.makeText(this, "Lỗi khi duyệt tài khoản!", Toast.LENGTH_SHORT).show();
                }
            });

            // Sự kiện Khóa / Mở khóa
            btnToggleLock.setOnClickListener(v -> {
                int trangThaiMoi = (tk.getTrangThai() == 1) ? 0 : 1;
                String hanhDong = (trangThaiMoi == 1) ? "Mở khóa" : "Khóa";

                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận " + hanhDong)
                        .setMessage("Bạn có chắc chắn muốn " + hanhDong.toLowerCase() + " tài khoản " + tk.getEmail() + "?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            boolean ok = dbHelper.khoaMoTaiKhoan(tk.getId(), trangThaiMoi);
                            if (ok) {
                                Toast.makeText(this, "Đã " + hanhDong.toLowerCase() + " tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                napDanhSachTaiKhoan();
                            } else {
                                Toast.makeText(this, "Lỗi thao tác!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            llAccountList.addView(itemView);
        }
    }

    // ==========================================
    // TAB 2: QUẢN LÝ MÔN HỌC / LỚP HỌC
    // ==========================================

    private void napDanhSachGiaoVienChoSpinner() {
        teacherList = dbHelper.layDanhSachGiaoVien();
        List<String> spinnerItems = new ArrayList<>();
        for (TaiKhoan gv : teacherList) {
            spinnerItems.add(gv.getEmail() + " (ID: " + gv.getId() + ")");
        }

        if (spinnerItems.isEmpty()) {
            spinnerItems.add("Chưa có giáo viên nào");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        spTeachers.setAdapter(adapter);
    }

    private void xuLyTaoMonHoc() {
        String maMon = etClassCode.getText().toString().trim();
        String tenMon = etClassName.getText().toString().trim();
        String namHoc = etSchoolYear.getText().toString().trim();

        if (TextUtils.isEmpty(maMon)) {
            etClassCode.setError("Vui lòng nhập Mã môn / Mã lớp!");
            etClassCode.requestFocus();
            return;
        }

        if (dbHelper.kiemTraMaMonTonTai(maMon)) {
            etClassCode.setError("Mã môn / Mã lớp này đã tồn tại!");
            etClassCode.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(tenMon)) {
            etClassName.setError("Vui lòng nhập Tên môn học!");
            etClassName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(namHoc)) {
            namHoc = "NĂM HỌC 2025-2026";
        }

        int idGiaoVien = 0;
        if (!teacherList.isEmpty() && spTeachers.getSelectedItemPosition() >= 0) {
            idGiaoVien = teacherList.get(spTeachers.getSelectedItemPosition()).getId();
        }

        boolean ok = dbHelper.taoMonHoc(maMon, tenMon, namHoc, idGiaoVien);
        if (ok) {
            Toast.makeText(this, "Tạo lớp học thành công!", Toast.LENGTH_SHORT).show();
            etClassCode.setText("");
            etClassName.setText("");
            etSchoolYear.setText("");
            napDanhSachMonHoc();
        } else {
            Toast.makeText(this, "Tạo lớp thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void napDanhSachMonHoc() {
        llClassList.removeAllViews();
        List<MonHoc> dsMon = dbHelper.layTatCaMonHoc();

        if (dsMon.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có môn học / lớp học phần nào.");
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setPadding(0, 20, 0, 20);
            llClassList.addView(tvEmpty);
            return;
        }

        for (MonHoc mon : dsMon) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_class, llClassList, false);

            TextView tvSchoolYear = itemView.findViewById(R.id.tvSchoolYear);
            TextView tvClassTitle = itemView.findViewById(R.id.tvClassTitle);
            TextView tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            TextView btnEditClass = itemView.findViewById(R.id.btnEditClass);
            TextView btnDeleteClass = itemView.findViewById(R.id.btnDeleteClass);

            tvSchoolYear.setText(mon.getNamHoc());
            tvClassTitle.setText(mon.getMaMon() + " - " + mon.getTenMon());

            String tenGv = mon.getTenGiaoVien() != null && !mon.getTenGiaoVien().isEmpty() ? mon.getTenGiaoVien() : "Chưa phân công";
            tvTeacherName.setText("👨‍🏫 Giảng viên: " + tenGv);

            // Sự kiện Sửa thông tin / Đổi Giáo viên
            btnEditClass.setOnClickListener(v -> hienDialogSuaMonHoc(mon));

            // Sự kiện Xóa Lớp
            btnDeleteClass.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa lớp học")
                        .setMessage("Bạn có chắc chắn muốn xóa lớp " + mon.getMaMon() + " - " + mon.getTenMon() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = dbHelper.xoaMonHoc(mon.getId());
                            if (ok) {
                                Toast.makeText(this, "Đã xóa lớp học thành công!", Toast.LENGTH_SHORT).show();
                                napDanhSachMonHoc();
                            } else {
                                Toast.makeText(this, "Lỗi khi xóa lớp!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            llClassList.addView(itemView);
        }
    }

    private void hienDialogSuaMonHoc(MonHoc mon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_class, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etEditMaMon = dialogView.findViewById(R.id.etEditMaMon);
        EditText etEditTenMon = dialogView.findViewById(R.id.etEditTenMon);
        EditText etEditNamHoc = dialogView.findViewById(R.id.etEditNamHoc);
        Spinner spEditGiaoVien = dialogView.findViewById(R.id.spEditGiaoVien);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancelEditClass);
        TextView btnSave = dialogView.findViewById(R.id.btnSaveEditClass);

        etEditMaMon.setText(mon.getMaMon());
        etEditTenMon.setText(mon.getTenMon());
        etEditNamHoc.setText(mon.getNamHoc());

        List<TaiKhoan> dsGiaoVien = dbHelper.layDanhSachGiaoVien();
        List<String> teacherNames = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < dsGiaoVien.size(); i++) {
            TaiKhoan gv = dsGiaoVien.get(i);
            teacherNames.add(gv.getEmail());
            if (gv.getId() == mon.getIdGiaoVien()) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teacherNames);
        spEditGiaoVien.setAdapter(adapter);
        if (selectedIndex < teacherNames.size()) {
            spEditGiaoVien.setSelection(selectedIndex);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String maMoi = etEditMaMon.getText().toString().trim();
            String tenMoi = etEditTenMon.getText().toString().trim();
            String namMoi = etEditNamHoc.getText().toString().trim();

            if (TextUtils.isEmpty(maMoi) || TextUtils.isEmpty(tenMoi)) {
                Toast.makeText(this, "Mã môn và Tên môn không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            int idGvMoi = 0;
            if (!dsGiaoVien.isEmpty() && spEditGiaoVien.getSelectedItemPosition() >= 0) {
                idGvMoi = dsGiaoVien.get(spEditGiaoVien.getSelectedItemPosition()).getId();
            }

            boolean ok = dbHelper.capNhatMonHoc(mon.getId(), maMoi, tenMoi, namMoi, idGvMoi);
            if (ok) {
                Toast.makeText(this, "Cập nhật lớp học và Đổi giáo viên thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                napDanhSachMonHoc();
            } else {
                Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // ==========================================
    // TAB 3: XẾP SINH VIÊN VÀO LỚP HỌC
    // ==========================================

    private void napDanhSachLopChoAssignment() {
        classList = dbHelper.layTatCaMonHoc();
        List<String> classNames = new ArrayList<>();
        for (MonHoc mon : classList) {
            classNames.add(mon.getMaMon() + " - " + mon.getTenMon());
        }

        if (classNames.isEmpty()) {
            classNames.add("Chưa có lớp học nào");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classNames);
        spAssignClasses.setAdapter(adapter);

        spAssignClasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!classList.isEmpty() && position < classList.size()) {
                    selectedClassForAssignment = classList.get(position);
                    napDanhSachSinhVienThuocLop(selectedClassForAssignment.getId());
                } else {
                    selectedClassForAssignment = null;
                    llEnrolledStudentsList.removeAllViews();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void napDanhSachSinhVienThuocLop(int idMonHoc) {
        llEnrolledStudentsList.removeAllViews();
        List<TaiKhoan> dsSinhVien = dbHelper.layDanhSachSinhVienCuaMonHoc(idMonHoc);

        tvClassStudentListHeader.setText("Danh sách Sinh viên thuộc Lớp (" + dsSinhVien.size() + " sinh viên)");

        if (dsSinhVien.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có sinh viên nào trong lớp này.");
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setPadding(0, 20, 0, 20);
            llEnrolledStudentsList.addView(tvEmpty);
            return;
        }

        for (TaiKhoan sv : dsSinhVien) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_enrolled_student, llEnrolledStudentsList, false);

            TextView tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            TextView btnRemoveStudent = itemView.findViewById(R.id.btnRemoveStudent);

            tvStudentEmail.setText(sv.getEmail());

            btnRemoveStudent.setOnClickListener(v -> {
                boolean ok = dbHelper.xoaSinhVienKhoiMonHoc(sv.getId(), idMonHoc);
                if (ok) {
                    Toast.makeText(this, "Đã xóa sinh viên khỏi lớp!", Toast.LENGTH_SHORT).show();
                    napDanhSachSinhVienThuocLop(idMonHoc);
                } else {
                    Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
                }
            });

            llEnrolledStudentsList.addView(itemView);
        }
    }

    private void hienDialogThemSinhVienVaoLop() {
        if (selectedClassForAssignment == null) {
            Toast.makeText(this, "Vui lòng chọn Lớp học trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTargetClassName = dialogView.findViewById(R.id.tvTargetClassName);
        LinearLayout llAvailableStudents = dialogView.findViewById(R.id.llAvailableStudents);
        TextView btnClose = dialogView.findViewById(R.id.btnCloseAddStudentDialog);

        tvTargetClassName.setText("Lớp: " + selectedClassForAssignment.getMaMon() + " - " + selectedClassForAssignment.getTenMon());

        List<TaiKhoan> dsKhadung = dbHelper.layDanhSachSinhVienChuaVaoMonHoc(selectedClassForAssignment.getId());

        if (dsKhadung.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có sinh viên đã duyệt nào khả dụng để thêm.");
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setPadding(0, 20, 0, 20);
            llAvailableStudents.addView(tvEmpty);
        } else {
            for (TaiKhoan sv : dsKhadung) {
                View rowView = LayoutInflater.from(this).inflate(R.layout.item_admin_enrolled_student, llAvailableStudents, false);

                TextView tvStudentEmail = rowView.findViewById(R.id.tvStudentEmail);
                TextView btnAdd = rowView.findViewById(R.id.btnRemoveStudent);

                tvStudentEmail.setText(sv.getEmail());
                btnAdd.setText("+ Thêm vào lớp");
                btnAdd.setTextColor(getResources().getColor(R.color.blue_primary));
                btnAdd.setBackgroundResource(R.drawable.bg_home_pill);

                btnAdd.setOnClickListener(v -> {
                    boolean ok = dbHelper.themSinhVienVaoMonHoc(sv.getId(), selectedClassForAssignment.getId());
                    if (ok) {
                        Toast.makeText(this, "Đã thêm " + sv.getEmail() + " vào lớp!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        napDanhSachSinhVienThuocLop(selectedClassForAssignment.getId());
                    } else {
                        Toast.makeText(this, "Sinh viên này đã có trong lớp!", Toast.LENGTH_SHORT).show();
                    }
                });

                llAvailableStudents.addView(rowView);
            }
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
