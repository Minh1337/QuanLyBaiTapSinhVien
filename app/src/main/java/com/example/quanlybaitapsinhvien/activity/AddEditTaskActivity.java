package com.example.quanlybaitapsinhvien.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.ClassModel;
import com.example.quanlybaitapsinhvien.model.PersonalSubject;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvHeaderTitle, tvReadOnlyBanner;
    private EditText etTaskTitle, etTaskDesc, etDueDate;
    private Spinner spSubject, spPriority, spStatus;
    private Button btnPickDate, btnSaveTask;
    private ImageButton btnBack, btnDeleteTask;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private long taskId = -1;
    private TaskWithProgress currentTaskWithProgress;
    private List<PersonalSubject> personalSubjects = new ArrayList<>();
    private List<ClassModel> studentClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        if (getIntent().hasExtra(EXTRA_TASK_ID)) {
            taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        }

        initViews();
        setupSpinners();
        loadSubjectsAndClasses();

        if (taskId != -1) {
            loadTaskData();
        } else {
            tvHeaderTitle.setText("THÊM BÀI TẬP CÁ NHÂN");
            btnDeleteTask.setVisibility(View.GONE);
        }

        btnPickDate.setOnClickListener(v -> showDateTimePicker());
        btnSaveTask.setOnClickListener(v -> saveTask());
        btnDeleteTask.setOnClickListener(v -> confirmDeleteTask());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvReadOnlyBanner = findViewById(R.id.tv_read_only_banner);
        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDesc = findViewById(R.id.et_task_desc);
        etDueDate = findViewById(R.id.et_due_date);
        spSubject = findViewById(R.id.sp_subject);
        spPriority = findViewById(R.id.sp_priority);
        spStatus = findViewById(R.id.sp_status);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSaveTask = findViewById(R.id.btn_save_task);
        btnBack = findViewById(R.id.btn_back);
        btnDeleteTask = findViewById(R.id.btn_delete_task);
    }

    private void setupSpinners() {
        // Priority Spinner
        String[] priorities = new String[]{TaskProgress.PRIORITY_MEDIUM, TaskProgress.PRIORITY_HIGH, TaskProgress.PRIORITY_LOW};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);

        // Status Spinner
        String[] statuses = new String[]{TaskProgress.STATUS_NOT_STARTED, TaskProgress.STATUS_IN_PROGRESS, TaskProgress.STATUS_COMPLETED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);
    }

    private void loadSubjectsAndClasses() {
        long studentId = sessionManager.getUserId();
        personalSubjects = dbHelper.getPersonalSubjectsForStudent(studentId);
        studentClasses = dbHelper.getClassesForStudent(studentId);

        List<String> subjectOptions = new ArrayList<>();
        for (PersonalSubject ps : personalSubjects) {
            subjectOptions.add("[Môn cá nhân] " + ps.getSubjectCode() + " - " + ps.getSubjectName());
        }

        if (subjectOptions.isEmpty()) {
            subjectOptions.add("(Chưa có môn cá nhân - Hãy thêm môn trước)");
        }

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectOptions);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubject.setAdapter(subjectAdapter);
    }

    private void loadTaskData() {
        long studentId = sessionManager.getUserId();
        currentTaskWithProgress = dbHelper.getTaskWithProgressById(taskId, studentId);

        if (currentTaskWithProgress == null) {
            Toast.makeText(this, "Không tìm thấy thông tin bài tập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Task task = currentTaskWithProgress.getTask();
        TaskProgress progress = currentTaskWithProgress.getProgress();

        tvHeaderTitle.setText("SỬA BÀI TẬP");
        etTaskTitle.setText(task.getTitle());
        etTaskDesc.setText(task.getDescription() != null ? task.getDescription() : "");
        etDueDate.setText(task.getDueDate());

        // Set Priority Spinner
        if (progress != null && progress.getPriority() != null) {
            if (TaskProgress.PRIORITY_HIGH.equalsIgnoreCase(progress.getPriority())) spPriority.setSelection(1);
            else if (TaskProgress.PRIORITY_LOW.equalsIgnoreCase(progress.getPriority())) spPriority.setSelection(2);
            else spPriority.setSelection(0);
        }

        // Set Status Spinner
        if (progress != null && progress.getStatus() != null) {
            if (TaskProgress.STATUS_IN_PROGRESS.equalsIgnoreCase(progress.getStatus())) spStatus.setSelection(1);
            else if (TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(progress.getStatus())) spStatus.setSelection(2);
            else spStatus.setSelection(0);
        }

        if (!task.isPersonal()) {
            // Teacher task -> READ-ONLY for requirement and due date!
            tvReadOnlyBanner.setVisibility(View.VISIBLE);
            etTaskTitle.setEnabled(false);
            etTaskDesc.setEnabled(false);
            etDueDate.setEnabled(false);
            btnPickDate.setEnabled(false);
            spSubject.setEnabled(false);
            btnDeleteTask.setVisibility(View.GONE);

            List<String> teacherSubjectOptions = new ArrayList<>();
            teacherSubjectOptions.add("[Lớp học phần] " + currentTaskWithProgress.getSubjectOrClassName());
            ArrayAdapter<String> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherSubjectOptions);
            tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSubject.setAdapter(tAdapter);
            spSubject.setSelection(0);
        } else {
            // Personal task -> fully editable and deletable!
            tvReadOnlyBanner.setVisibility(View.GONE);
            btnDeleteTask.setVisibility(View.VISIBLE);

            if (task.getPersonalSubjectId() != null) {
                for (int i = 0; i < personalSubjects.size(); i++) {
                    if (personalSubjects.get(i).getId() == task.getPersonalSubjectId()) {
                        spSubject.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute1) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                        year1, month1 + 1, dayOfMonth, hourOfDay, minute1);
                etDueDate.setText(formattedDate);
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String desc = etTaskDesc.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        String priority = spPriority.getSelectedItem().toString();
        String status = spStatus.getSelectedItem().toString();

        long studentId = sessionManager.getUserId();

        if (taskId != -1 && currentTaskWithProgress != null && !currentTaskWithProgress.getTask().isPersonal()) {
            // For Teacher-assigned tasks: Only progress & priority are updated!
            boolean updated = dbHelper.updateTaskProgressOnly(taskId, studentId, status, priority);
            if (updated) {
                Toast.makeText(this, "Đã cập nhật tiến độ cho bài giáo viên giao!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // For Personal Tasks: Validate title and due date
        if (title.isEmpty()) {
            etTaskTitle.setError("Vui lòng nhập tên bài tập cá nhân");
            etTaskTitle.requestFocus();
            return;
        }

        if (dueDate.isEmpty()) {
            etDueDate.setError("Vui lòng chọn hạn nộp");
            etDueDate.requestFocus();
            return;
        }

        Long personalSubjectId = null;
        if (!personalSubjects.isEmpty() && spSubject.getSelectedItemPosition() < personalSubjects.size()) {
            personalSubjectId = personalSubjects.get(spSubject.getSelectedItemPosition()).getId();
        }

        Task task = new Task(
                taskId != -1 ? taskId : 0,
                title,
                desc,
                dueDate,
                Task.SOURCE_PERSONAL,
                null,
                personalSubjectId,
                studentId
        );

        TaskProgress progress = new TaskProgress(
                0,
                taskId != -1 ? taskId : 0,
                studentId,
                status,
                priority,
                null
        );

        if (taskId == -1) {
            long newId = dbHelper.insertPersonalTask(task, progress);
            if (newId != -1) {
                Toast.makeText(this, "Thêm bài tập cá nhân thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Tạo bài tập thất bại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.updatePersonalTask(task, progress);
            if (success) {
                Toast.makeText(this, "Cập nhật bài tập cá nhân thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Bạn không có quyền sửa nội dung bài tập này!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDeleteTask() {
        if (currentTaskWithProgress == null || !currentTaskWithProgress.getTask().isPersonal()) {
            Toast.makeText(this, "Không thể xóa bài tập do giáo viên giao!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa bài tập")
                .setMessage("Bạn có chắc chắn muốn xóa bài tập cá nhân này? Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    long studentId = sessionManager.getUserId();
                    boolean deleted = dbHelper.deletePersonalTask(taskId, studentId);
                    if (deleted) {
                        Toast.makeText(this, "Đã xóa bài tập cá nhân!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Xóa bài tập thất bại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
