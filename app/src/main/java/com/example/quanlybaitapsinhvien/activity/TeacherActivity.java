package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.ClassModel;
import com.example.quanlybaitapsinhvien.model.StudentReportItem;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TeacherActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextView tvTeacherInfo;
    private Spinner spTeacherClass, spReportTask;
    private LinearLayout layoutTaskList, layoutReportList;
    private Button btnCreateTask;
    private ImageButton btnLogout;

    private List<ClassModel> teacherClasses = new ArrayList<>();
    private List<Task> currentClassTasks = new ArrayList<>();
    private ClassModel selectedClass;

    private final ActivityResultLauncher<Intent> refreshLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshClassTasks();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        tvTeacherInfo = findViewById(R.id.tv_teacher_info);
        spTeacherClass = findViewById(R.id.sp_teacher_class);
        spReportTask = findViewById(R.id.sp_report_task);
        layoutTaskList = findViewById(R.id.layout_teacher_task_list);
        layoutReportList = findViewById(R.id.layout_student_report_list);
        btnCreateTask = findViewById(R.id.btn_create_teacher_task);
        btnLogout = findViewById(R.id.btn_teacher_logout);

        tvTeacherInfo.setText("Xin chào Giáo viên: " + sessionManager.getFullName() + " (" + sessionManager.getUsername() + ")");

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCreateTask.setOnClickListener(v -> {
            if (selectedClass == null) {
                Toast.makeText(this, "Vui lòng chọn lớp học phần!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(TeacherActivity.this, AddEditTaskActivity.class);
            intent.putExtra(AddEditTaskActivity.EXTRA_CLASS_ID, selectedClass.getId());
            refreshLauncher.launch(intent);
        });

        loadTeacherClasses();
    }

    private void loadTeacherClasses() {
        long teacherId = sessionManager.getUserId();
        teacherClasses = dbHelper.getClassesForTeacher(teacherId);

        if (teacherClasses.isEmpty()) {
            Toast.makeText(this, "Bạn chưa được phân công quản lý lớp học phần nào!", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> classNames = new ArrayList<>();
        for (ClassModel c : teacherClasses) {
            classNames.add(c.getClassCode() + " - " + c.getClassName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTeacherClass.setAdapter(adapter);

        spTeacherClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass = teacherClasses.get(position);
                refreshClassTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshClassTasks() {
        if (selectedClass == null) return;

        currentClassTasks = dbHelper.getTasksForClass(selectedClass.getId());
        renderTaskList();
        setupReportTaskSpinner();
    }

    private void renderTaskList() {
        layoutTaskList.removeAllViews();
        if (currentClassTasks.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Lớp chưa có bài tập nào được giao.");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(0, 16, 0, 16);
            layoutTaskList.addView(emptyText);
            return;
        }

        for (Task task : currentClassTasks) {
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setBackgroundColor(Color.parseColor("#FAFAFA"));
            container.setPadding(16, 16, 16, 16);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 12, 0, 0);
            container.setLayoutParams(clp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(task.getTitle());
            tvTitle.setTextSize(15);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextColor(Color.parseColor("#382841"));

            TextView tvDesc = new TextView(this);
            tvDesc.setText(task.getDescription() != null ? task.getDescription() : "");
            tvDesc.setTextSize(13);
            tvDesc.setTextColor(Color.parseColor("#555555"));

            TextView tvDue = new TextView(this);
            tvDue.setText("⏰ Hạn nộp: " + task.getDueDate());
            tvDue.setTextSize(12);
            tvDue.setTextColor(Color.parseColor("#D32F2F"));

            LinearLayout btnLayout = new LinearLayout(this);
            btnLayout.setOrientation(LinearLayout.HORIZONTAL);
            btnLayout.setPadding(0, 8, 0, 0);

            Button btnEdit = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnEdit.setText("✏️ Sửa bài");
            btnEdit.setTextSize(11);

            Button btnDelete = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnDelete.setText("🗑️ Thu hồi");
            btnDelete.setTextSize(11);
            btnDelete.setTextColor(Color.RED);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherActivity.this, AddEditTaskActivity.class);
                intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.getId());
                intent.putExtra(AddEditTaskActivity.EXTRA_CLASS_ID, selectedClass.getId());
                refreshLauncher.launch(intent);
            });
            btnDelete.setOnClickListener(v -> confirmDeleteTask(task));

            btnLayout.addView(btnEdit);
            btnLayout.addView(btnDelete);

            container.addView(tvTitle);
            container.addView(tvDesc);
            container.addView(tvDue);
            container.addView(btnLayout);

            layoutTaskList.addView(container);
        }
    }

    private void setupReportTaskSpinner() {
        if (currentClassTasks.isEmpty()) {
            spReportTask.setAdapter(null);
            layoutReportList.removeAllViews();
            return;
        }

        List<String> taskTitles = new ArrayList<>();
        for (Task t : currentClassTasks) {
            taskTitles.add(t.getTitle());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReportTask.setAdapter(adapter);

        spReportTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < currentClassTasks.size()) {
                    renderStudentReportList(currentClassTasks.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void renderStudentReportList(Task task) {
        layoutReportList.removeAllViews();
        if (selectedClass == null || task == null) return;

        List<StudentReportItem> reports = dbHelper.getStudentsProgressReportForTask(task.getId(), selectedClass.getId());

        if (reports.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Lớp chưa có sinh viên nào.");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(0, 16, 0, 16);
            layoutReportList.addView(emptyText);
            return;
        }

        for (StudentReportItem item : reports) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(12, 12, 12, 12);
            row.setBackgroundColor(Color.parseColor("#F5F5F5"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 0);
            row.setLayoutParams(lp);

            TextView tvName = new TextView(this);
            tvName.setText("👨‍🎓 " + item.getStudentName() + " (" + item.getUsername() + ")");
            tvName.setTextSize(14);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(Color.parseColor("#382841"));

            TextView tvStatus = new TextView(this);
            String subText = item.getSubmittedFilePath() != null ? " [📄 File: " + item.getSubmittedFilePath() + "]" : "";
            tvStatus.setText("Trạng thái: " + item.getStatus() + " (" + item.getProgressPercentage() + "% completed)" + subText);
            tvStatus.setTextSize(12);

            if ("Hoàn thành".equalsIgnoreCase(item.getStatus())) tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            else if ("Đang làm".equalsIgnoreCase(item.getStatus())) tvStatus.setTextColor(Color.parseColor("#E65100"));
            else tvStatus.setTextColor(Color.GRAY);

            row.addView(tvName);
            row.addView(tvStatus);

            layoutReportList.addView(row);
        }
    }

    private void confirmDeleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thu hồi bài tập")
                .setMessage("Bạn có chắc chắn muốn thu hồi bài tập '" + task.getTitle() + "' khỏi lớp?")
                .setPositiveButton("Thu hồi", (dialog, which) -> {
                    dbHelper.deleteTeacherTask(task.getId());
                    Toast.makeText(this, "Đã thu hồi bài tập!", Toast.LENGTH_SHORT).show();
                    refreshClassTasks();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
