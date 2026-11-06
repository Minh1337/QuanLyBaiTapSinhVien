package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.adapter.TaskAdapter;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvStudentName, tvClassSubjectCount, tvEmptyView;
    private ImageButton btnLogout;
    private EditText etSearch;
    private Spinner spSourceFilter, spStatusFilter, spSortFilter;
    private Button btnAddPersonalSubject;
    private RecyclerView rvTasks;
    private ExtendedFloatingActionButton fabAddTask;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;
    private List<TaskWithProgress> taskList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> refreshLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTasks();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String role = sessionManager.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, TeacherActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.main);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        initViews();
        setupUserHeader();
        setupFilterSpinners();
        setupSearchAndListeners();
        setupRecyclerView();

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void initViews() {
        tvStudentName = findViewById(R.id.tv_student_name);
        tvClassSubjectCount = findViewById(R.id.tv_class_subject_count);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        btnLogout = findViewById(R.id.btn_logout);
        etSearch = findViewById(R.id.et_search);

        spSourceFilter = findViewById(R.id.sp_source_filter);
        spStatusFilter = findViewById(R.id.sp_status_filter);
        spSortFilter = findViewById(R.id.sp_sort_filter);

        btnAddPersonalSubject = findViewById(R.id.btn_add_personal_subject);
        rvTasks = findViewById(R.id.rv_tasks);
        fabAddTask = findViewById(R.id.fab_add_task);
    }

    private void setupUserHeader() {
        tvStudentName.setText("Xin chào: " + sessionManager.getFullName());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupFilterSpinners() {
        // Source Filter
        String[] sources = new String[]{"Tất cả nguồn", "Cá nhân", "Giáo viên giao"};
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSourceFilter.setAdapter(sourceAdapter);

        // Status Filter
        String[] statuses = new String[]{"Tất cả trạng thái", "Chưa làm", "Đang làm", "Hoàn thành", "Quá hạn"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(statusAdapter);

        // Sort Filter
        String[] sortOptions = new String[]{"Hạn nộp (Tăng dần)", "Hạn nộp (Giảm dần)", "Mức ưu tiên"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSortFilter.setAdapter(sortAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spSourceFilter.setOnItemSelectedListener(filterListener);
        spStatusFilter.setOnItemSelectedListener(filterListener);
        spSortFilter.setOnItemSelectedListener(filterListener);
    }

    private void setupSearchAndListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddPersonalSubject.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPersonalSubjectActivity.class);
            refreshLauncher.launch(intent);
        });

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            refreshLauncher.launch(intent);
        });
    }

    private void setupRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, taskWithProgress -> {
            Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskWithProgress.getTask().getId());
            refreshLauncher.launch(intent);
        });
        rvTasks.setAdapter(taskAdapter);
    }

    private void loadTasks() {
        long studentId = sessionManager.getUserId();
        String searchQuery = etSearch.getText().toString().trim();
        String sourceFilter = spSourceFilter.getSelectedItem() != null ? spSourceFilter.getSelectedItem().toString() : "Tất cả nguồn";
        String statusFilter = spStatusFilter.getSelectedItem() != null ? spStatusFilter.getSelectedItem().toString() : "Tất cả trạng thái";
        String sortOption = spSortFilter.getSelectedItem() != null ? spSortFilter.getSelectedItem().toString() : "Hạn nộp (Tăng dần)";

        taskList = dbHelper.getTasksForStudent(studentId, searchQuery, sourceFilter, statusFilter, sortOption);
        taskAdapter.updateData(taskList);

        if (taskList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }

        int classCount = dbHelper.getClassesForStudent(studentId).size();
        int subjectCount = dbHelper.getPersonalSubjectsForStudent(studentId).size();
        tvClassSubjectCount.setText("Môn học & Lớp (" + classCount + " Lớp, " + subjectCount + " Môn cá nhân)");
    }
}
