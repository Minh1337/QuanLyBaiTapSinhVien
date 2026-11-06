package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.ClassModel;
import com.example.quanlybaitapsinhvien.model.User;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextView tvAdminInfo;
    private LinearLayout layoutUserList, layoutClassList;
    private Button btnAddTeacher, btnCreateClass;
    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        tvAdminInfo = findViewById(R.id.tv_admin_info);
        layoutUserList = findViewById(R.id.layout_user_list);
        layoutClassList = findViewById(R.id.layout_class_list);
        btnAddTeacher = findViewById(R.id.btn_add_teacher);
        btnCreateClass = findViewById(R.id.btn_create_class);
        btnLogout = findViewById(R.id.btn_admin_logout);

        tvAdminInfo.setText("Xin chào Admin: " + sessionManager.getFullName() + " (" + sessionManager.getUsername() + ")");

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnAddTeacher.setOnClickListener(v -> showCreateTeacherDialog());
        btnCreateClass.setOnClickListener(v -> showCreateClassDialog());

        refreshAdminData();
    }

    private void refreshAdminData() {
        renderUsersList();
        renderClassesList();
    }

    private void renderUsersList() {
        layoutUserList.removeAllViews();
        List<User> users = dbHelper.getAllUsers();

        for (User user : users) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvInfo = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvInfo.setLayoutParams(lp);

            String roleText = " [Sinh viên]";
            if ("TEACHER".equalsIgnoreCase(user.getRole())) roleText = " [Giáo viên]";
            else if ("ADMIN".equalsIgnoreCase(user.getRole())) roleText = " [Quản trị]";

            String statusText = "LOCKED".equalsIgnoreCase(user.getStatus()) ? " (🔒 Đã khóa)" : " (Hoạt động)";
            tvInfo.setText(user.getFullName() + roleText + statusText);
            tvInfo.setTextColor("LOCKED".equalsIgnoreCase(user.getStatus()) ? Color.RED : Color.parseColor("#382841"));
            tvInfo.setTextSize(13);

            Button btnToggleLock = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnToggleLock.setText("ADMIN".equalsIgnoreCase(user.getRole()) ? "Bảo vệ" : ("LOCKED".equalsIgnoreCase(user.getStatus()) ? "🔓 Mở" : "🔒 Khóa"));
            btnToggleLock.setTextSize(11);
            btnToggleLock.setPadding(12, 0, 12, 0);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                btnToggleLock.setEnabled(false);
            } else {
                btnToggleLock.setOnClickListener(v -> {
                    dbHelper.toggleUserLockStatus(user.getId());
                    refreshAdminData();
                });
            }

            row.addView(tvInfo);
            row.addView(btnToggleLock);
            layoutUserList.addView(row);
        }
    }

    private void renderClassesList() {
        layoutClassList.removeAllViews();
        List<ClassModel> classes = dbHelper.getAllClassesIncludeArchived();

        for (ClassModel cls : classes) {
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setBackgroundColor(Color.parseColor("#FAFAFA"));
            container.setPadding(16, 16, 16, 16);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 12, 0, 0);
            container.setLayoutParams(clp);

            User teacher = dbHelper.getUserById(cls.getTeacherId());
            String teacherName = teacher != null ? teacher.getFullName() : "Chưa gán";

            TextView tvClassTitle = new TextView(this);
            String archivedStatus = cls.isArchived() ? " [📦 Đã lưu trữ]" : "";
            tvClassTitle.setText("Lớp: " + cls.getClassCode() + " - " + cls.getClassName() + archivedStatus);
            tvClassTitle.setTextSize(14);
            tvClassTitle.setTypeface(null, Typeface.BOLD);
            tvClassTitle.setTextColor(Color.parseColor("#713E87"));

            TextView tvTeacherInfo = new TextView(this);
            tvTeacherInfo.setText("👨‍🏫 GV phụ trách: " + teacherName);
            tvTeacherInfo.setTextSize(12);
            tvTeacherInfo.setTextColor(Color.parseColor("#555555"));

            // Actions layout
            LinearLayout actionLayout = new LinearLayout(this);
            actionLayout.setOrientation(LinearLayout.HORIZONTAL);
            actionLayout.setPadding(0, 8, 0, 0);

            Button btnChangeTeacher = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnChangeTeacher.setText("👨‍🏫 Đổi GV");
            btnChangeTeacher.setTextSize(10);

            Button btnAddStudent = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnAddStudent.setText("➕ Thêm SV");
            btnAddStudent.setTextSize(10);

            Button btnRemoveStudent = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnRemoveStudent.setText("🗑️ Bớt SV");
            btnRemoveStudent.setTextSize(10);

            Button btnArchive = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnArchive.setText(cls.isArchived() ? "🔄 Mở" : "📦 Lưu trữ");
            btnArchive.setTextSize(10);

            btnChangeTeacher.setOnClickListener(v -> showChangeTeacherDialog(cls));
            btnAddStudent.setOnClickListener(v -> showAddStudentToClassDialog(cls));
            btnRemoveStudent.setOnClickListener(v -> showRemoveStudentFromClassDialog(cls));
            btnArchive.setOnClickListener(v -> {
                dbHelper.toggleArchiveClass(cls.getId());
                refreshAdminData();
            });

            actionLayout.addView(btnChangeTeacher);
            actionLayout.addView(btnAddStudent);
            actionLayout.addView(btnRemoveStudent);
            actionLayout.addView(btnArchive);

            container.addView(tvClassTitle);
            container.addView(tvTeacherInfo);
            container.addView(actionLayout);

            layoutClassList.addView(container);
        }
    }

    private void showCreateTeacherDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etUsername = new EditText(this);
        etUsername.setHint("Tên đăng nhập giáo viên (ví dụ: gv_tran)");

        final EditText etFullName = new EditText(this);
        etFullName.setHint("Họ và tên giáo viên (ví dụ: ThS. Trần Văn B)");

        final EditText etPassword = new EditText(this);
        etPassword.setHint("Mật khẩu (mặc định: gv123456)");
        etPassword.setText("gv123456");

        layout.addView(etUsername);
        layout.addView(etFullName);
        layout.addView(etPassword);

        new AlertDialog.Builder(this)
                .setTitle("Tạo tài khoản Giáo viên")
                .setView(layout)
                .setPositiveButton("Tạo tài khoản", (dialog, which) -> {
                    String username = etUsername.getText().toString().trim();
                    String fullName = etFullName.getText().toString().trim();
                    String pass = etPassword.getText().toString().trim();

                    if (username.isEmpty() || fullName.isEmpty() || pass.length() < 8) {
                        Toast.makeText(this, "Thông tin không hợp lệ hoặc mật khẩu < 8 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long id = dbHelper.createTeacherAccount(username, pass, fullName);
                    if (id != -1) {
                        Toast.makeText(this, "Tạo tài khoản giáo viên thành công!", Toast.LENGTH_SHORT).show();
                        refreshAdminData();
                    } else {
                        Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreateClassDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etClassCode = new EditText(this);
        etClassCode.setHint("Mã lớp (ví dụ: MOB02)");

        final EditText etClassName = new EditText(this);
        etClassName.setHint("Tên lớp học phần (ví dụ: Lập trình Android MOB02)");

        TextView tvTeacherLabel = new TextView(this);
        tvTeacherLabel.setText("Chọn giáo viên phụ trách:");
        tvTeacherLabel.setPadding(0, 16, 0, 8);

        final Spinner spTeacher = new Spinner(this);
        List<User> teachers = dbHelper.getUsersByRole("TEACHER");
        List<String> teacherNames = new ArrayList<>();
        for (User t : teachers) {
            teacherNames.add(t.getFullName() + " (" + t.getUsername() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTeacher.setAdapter(adapter);

        layout.addView(etClassCode);
        layout.addView(etClassName);
        layout.addView(tvTeacherLabel);
        layout.addView(spTeacher);

        new AlertDialog.Builder(this)
                .setTitle("Tạo Lớp học phần")
                .setView(layout)
                .setPositiveButton("Tạo Lớp", (dialog, which) -> {
                    String code = etClassCode.getText().toString().trim();
                    String name = etClassName.getText().toString().trim();

                    if (code.isEmpty() || name.isEmpty() || teachers.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin mã lớp và tên lớp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long teacherId = teachers.get(spTeacher.getSelectedItemPosition()).getId();
                    long id = dbHelper.createClass(code, name, teacherId);
                    if (id != -1) {
                        Toast.makeText(this, "Tạo lớp học phần thành công!", Toast.LENGTH_SHORT).show();
                        refreshAdminData();
                    } else {
                        Toast.makeText(this, "Mã lớp đã tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangeTeacherDialog(ClassModel cls) {
        List<User> teachers = dbHelper.getUsersByRole("TEACHER");
        if (teachers.isEmpty()) {
            Toast.makeText(this, "Chưa có giáo viên nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        int currentSelection = 0;
        for (int i = 0; i < teachers.size(); i++) {
            User t = teachers.get(i);
            names.add(t.getFullName() + " (" + t.getUsername() + ")");
            if (t.getId() == cls.getTeacherId()) {
                currentSelection = i;
            }
        }

        final Spinner spTeacher = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTeacher.setAdapter(adapter);
        spTeacher.setSelection(currentSelection);

        new AlertDialog.Builder(this)
                .setTitle("Đổi giáo viên phụ trách lớp " + cls.getClassCode())
                .setView(spTeacher)
                .setPositiveButton("Xác nhận đổi", (dialog, which) -> {
                    long newTeacherId = teachers.get(spTeacher.getSelectedItemPosition()).getId();
                    dbHelper.updateClassTeacher(cls.getId(), newTeacherId);
                    Toast.makeText(this, "Đã chuyển quyền xem và giao bài sang giáo viên mới!", Toast.LENGTH_SHORT).show();
                    refreshAdminData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddStudentToClassDialog(ClassModel cls) {
        List<User> students = dbHelper.getUsersByRole("STUDENT");
        if (students.isEmpty()) {
            Toast.makeText(this, "Chưa có sinh viên nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        for (User s : students) {
            names.add(s.getFullName() + " (" + s.getUsername() + ")");
        }

        final Spinner spStudent = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStudent.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Thêm sinh viên vào lớp " + cls.getClassCode())
                .setView(spStudent)
                .setPositiveButton("Thêm vào lớp", (dialog, which) -> {
                    long studentId = students.get(spStudent.getSelectedItemPosition()).getId();
                    boolean added = dbHelper.addStudentToClass(cls.getId(), studentId);
                    if (added) {
                        Toast.makeText(this, "Đã thêm sinh viên vào lớp!", Toast.LENGTH_SHORT).show();
                        refreshAdminData();
                    } else {
                        Toast.makeText(this, "Sinh viên đã thuộc lớp này!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRemoveStudentFromClassDialog(ClassModel cls) {
        List<User> studentsInClass = dbHelper.getStudentsInClass(cls.getId());
        if (studentsInClass.isEmpty()) {
            Toast.makeText(this, "Lớp chưa có sinh viên nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        for (User s : studentsInClass) {
            names.add(s.getFullName() + " (" + s.getUsername() + ")");
        }

        final Spinner spStudent = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStudent.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Bớt sinh viên khỏi lớp " + cls.getClassCode())
                .setView(spStudent)
                .setPositiveButton("Bớt khỏi lớp", (dialog, which) -> {
                    long studentId = studentsInClass.get(spStudent.getSelectedItemPosition()).getId();
                    dbHelper.removeStudentFromClass(cls.getId(), studentId);
                    Toast.makeText(this, "Đã bớt sinh viên khỏi lớp!", Toast.LENGTH_SHORT).show();
                    refreshAdminData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
