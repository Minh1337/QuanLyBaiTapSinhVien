package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.adapter.ChecklistAdapter;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.ChecklistItem;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvSourceTag, tvSubject, tvTitle, tvDueDate, tvDesc, tvDetailBanner, tvChecklistPercent;
    private Spinner spPriority, spStatus;
    private Button btnUpdateProgress, btnAddChecklist;
    private EditText etAddChecklist;
    private ProgressBar pbChecklist;
    private RecyclerView rvChecklist;
    private ImageButton btnBack, btnEditTask, btnDeleteTask;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ChecklistAdapter checklistAdapter;

    private long taskId = -1;
    private long progressId = -1;
    private TaskWithProgress currentTaskWithProgress;
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTaskDetail();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

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
        setupRecyclerView();

        loadTaskDetail();

        btnBack.setOnClickListener(v -> finish());

        btnEditTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailActivity.this, AddEditTaskActivity.class);
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId);
            editLauncher.launch(intent);
        });

        btnDeleteTask.setOnClickListener(v -> confirmDeletePersonalTask());

        btnUpdateProgress.setOnClickListener(v -> saveStudentProgress());

        btnAddChecklist.setOnClickListener(v -> addChecklistItem());
    }

    private void initViews() {
        tvSourceTag = findViewById(R.id.tv_detail_source_tag);
        tvSubject = findViewById(R.id.tv_detail_subject);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDueDate = findViewById(R.id.tv_detail_due_date);
        tvDesc = findViewById(R.id.tv_detail_desc);
        tvDetailBanner = findViewById(R.id.tv_detail_banner);
        tvChecklistPercent = findViewById(R.id.tv_checklist_progress_text);

        spPriority = findViewById(R.id.sp_detail_priority);
        spStatus = findViewById(R.id.sp_detail_status);

        btnUpdateProgress = findViewById(R.id.btn_update_progress);
        btnAddChecklist = findViewById(R.id.btn_add_checklist);
        etAddChecklist = findViewById(R.id.et_add_checklist);
        pbChecklist = findViewById(R.id.pb_detail_checklist);
        rvChecklist = findViewById(R.id.rv_checklist);

        btnBack = findViewById(R.id.btn_back);
        btnEditTask = findViewById(R.id.btn_edit_task);
        btnDeleteTask = findViewById(R.id.btn_delete_task);
    }

    private void setupSpinners() {
        String[] priorities = new String[]{TaskProgress.PRIORITY_MEDIUM, TaskProgress.PRIORITY_HIGH, TaskProgress.PRIORITY_LOW};
        ArrayAdapter<String> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(pAdapter);

        String[] statuses = new String[]{TaskProgress.STATUS_NOT_STARTED, TaskProgress.STATUS_IN_PROGRESS, TaskProgress.STATUS_COMPLETED};
        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(sAdapter);
    }

    private void setupRecyclerView() {
        rvChecklist.setLayoutManager(new LinearLayoutManager(this));
        checklistAdapter = new ChecklistAdapter(checklistItems, new ChecklistAdapter.OnChecklistActionListener() {
            @Override
            public void onToggle(ChecklistItem item, boolean isChecked) {
                dbHelper.toggleChecklistItem(item.getId(), isChecked);
                loadChecklistData();
            }

            @Override
            public void onDelete(ChecklistItem item) {
                dbHelper.deleteChecklistItem(item.getId());
                loadChecklistData();
            }
        });
        rvChecklist.setAdapter(checklistAdapter);
    }

    private void loadTaskDetail() {
        if (taskId == -1) {
            Toast.makeText(this, "Không tìm thấy mã bài tập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long studentId = sessionManager.getUserId();
        currentTaskWithProgress = dbHelper.getTaskWithProgressById(taskId, studentId);

        if (currentTaskWithProgress == null) {
            Toast.makeText(this, "Không có quyền xem bài tập này!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Task task = currentTaskWithProgress.getTask();
        TaskProgress progress = currentTaskWithProgress.getProgress();

        tvTitle.setText(task.getTitle());
        tvSubject.setText(currentTaskWithProgress.getSubjectOrClassName());
        tvDueDate.setText("⏰ Hạn nộp: " + task.getDueDate());
        tvDesc.setText(task.getDescription() != null && !task.getDescription().trim().isEmpty() ? task.getDescription() : "(Không có mô tả)");

        // Source tag & Edit/Delete buttons visibility
        if (task.isPersonal()) {
            tvSourceTag.setText("Cá nhân");
            tvSourceTag.setBackgroundColor(Color.parseColor("#713E87"));
            tvDetailBanner.setVisibility(View.GONE);
            btnDeleteTask.setVisibility(View.VISIBLE);
        } else {
            tvSourceTag.setText("Giáo viên giao");
            tvSourceTag.setBackgroundColor(Color.parseColor("#0288D1"));
            tvDetailBanner.setVisibility(View.VISIBLE);
            btnDeleteTask.setVisibility(View.GONE);
        }

        // Set Priority & Status
        if (progress != null) {
            if (TaskProgress.PRIORITY_HIGH.equalsIgnoreCase(progress.getPriority())) spPriority.setSelection(1);
            else if (TaskProgress.PRIORITY_LOW.equalsIgnoreCase(progress.getPriority())) spPriority.setSelection(2);
            else spPriority.setSelection(0);

            if (TaskProgress.STATUS_IN_PROGRESS.equalsIgnoreCase(progress.getStatus())) spStatus.setSelection(1);
            else if (TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(progress.getStatus())) spStatus.setSelection(2);
            else spStatus.setSelection(0);
        }

        // Ensure Progress ID exists in DB for checklist
        progressId = dbHelper.ensureProgressRecordExists(taskId, studentId);
        loadChecklistData();
    }

    private void loadChecklistData() {
        if (progressId <= 0) return;
        checklistItems = dbHelper.getChecklistItems(progressId);
        checklistAdapter.updateData(checklistItems);

        int total = checklistItems.size();
        int done = 0;
        for (ChecklistItem item : checklistItems) {
            if (item.isCompleted()) done++;
        }

        int pct = (total == 0) ? 0 : (int) Math.round((double) done / total * 100.0);
        pbChecklist.setProgress(pct);
        tvChecklistPercent.setText(pct + "% (" + done + "/" + total + ")");
    }

    private void addChecklistItem() {
        String content = etAddChecklist.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung mục checklist!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressId <= 0) {
            progressId = dbHelper.ensureProgressRecordExists(taskId, sessionManager.getUserId());
        }

        long newId = dbHelper.addChecklistItem(progressId, content);
        if (newId != -1) {
            etAddChecklist.setText("");
            loadChecklistData();
        } else {
            Toast.makeText(this, "Không thể thêm mục checklist!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveStudentProgress() {
        String priority = spPriority.getSelectedItem().toString();
        String status = spStatus.getSelectedItem().toString();

        long studentId = sessionManager.getUserId();
        boolean success = dbHelper.updateTaskProgressOnly(taskId, studentId, status, priority);

        if (success) {
            Toast.makeText(this, "Cập nhật tiến độ thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Cập nhật tiến độ thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletePersonalTask() {
        if (currentTaskWithProgress == null || !currentTaskWithProgress.getTask().isPersonal()) {
            Toast.makeText(this, "Bài giáo viên giao không thể xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bài tập cá nhân này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    long studentId = sessionManager.getUserId();
                    boolean deleted = dbHelper.deletePersonalTask(taskId, studentId);
                    if (deleted) {
                        Toast.makeText(this, "Đã xóa bài tập cá nhân!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Xóa không thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
