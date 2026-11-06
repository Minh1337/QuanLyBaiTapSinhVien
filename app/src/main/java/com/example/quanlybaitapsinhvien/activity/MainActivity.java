package com.example.quanlybaitapsinhvien.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.adapter.TaskAdapter;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.FileUtils;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvStudentName, tvEmptyView;
    private ImageButton btnLogout;
    private EditText etSearch;
    private Spinner spSourceFilter, spStatusFilter, spSortFilter;
    private Button btnAddPersonalSubject, btnOpenQuiz, btnHomeAddTask, btnExportCsv, btnToggleReminder;
    private RecyclerView rvTasks, rvHomeTasks, rvCalendarTasks;
    private ExtendedFloatingActionButton fabAddTask;

    private View containerTabHome, containerTabTasks, containerTabCalendar, containerTabStats;
    private TextView tvTabHome, tvTabTasks, tvTabCalendar, tvTabStats;
    private View tabHome, tabTasks, tabCalendar, tabStats;

    private TextView tvNearestTitle, tvNearestSub;
    private ProgressBar pbNearest;

    private TextView tvStatsNotStarted, tvStatsInProgress, tvStatsCompleted, tvStatsOverdue;
    private ProgressBar pbStatsNotStarted, pbStatsInProgress, pbStatsCompleted;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter, homeTaskAdapter, calendarTaskAdapter;
    private List<TaskWithProgress> taskList = new ArrayList<>();

    private boolean isReminderEnabled = true;

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
        setupRecyclerViews();
        setupBottomNavigation();

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void initViews() {
        tvStudentName = findViewById(R.id.tv_student_name);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        btnLogout = findViewById(R.id.btn_logout);
        etSearch = findViewById(R.id.et_search);

        spSourceFilter = findViewById(R.id.sp_source_filter);
        spStatusFilter = findViewById(R.id.sp_status_filter);
        spSortFilter = findViewById(R.id.sp_sort_filter);

        btnAddPersonalSubject = findViewById(R.id.btn_add_personal_subject);
        btnOpenQuiz = findViewById(R.id.btn_open_quiz);
        btnHomeAddTask = findViewById(R.id.btn_home_add_task);
        btnExportCsv = findViewById(R.id.btn_export_csv);
        btnToggleReminder = findViewById(R.id.btn_toggle_reminder);

        rvTasks = findViewById(R.id.rv_tasks);
        rvHomeTasks = findViewById(R.id.rv_home_tasks);
        rvCalendarTasks = findViewById(R.id.rv_calendar_tasks);
        fabAddTask = findViewById(R.id.fab_add_task);

        containerTabHome = findViewById(R.id.container_tab_home);
        containerTabTasks = findViewById(R.id.container_tab_tasks);
        containerTabCalendar = findViewById(R.id.container_tab_calendar);
        containerTabStats = findViewById(R.id.container_tab_stats);

        tabHome = findViewById(R.id.tab_home);
        tabTasks = findViewById(R.id.tab_tasks);
        tabCalendar = findViewById(R.id.tab_calendar);
        tabStats = findViewById(R.id.tab_stats);

        tvTabHome = findViewById(R.id.tv_tab_home);
        tvTabTasks = findViewById(R.id.tv_tab_tasks);
        tvTabCalendar = findViewById(R.id.tv_tab_calendar);
        tvTabStats = findViewById(R.id.tv_tab_stats);

        tvNearestTitle = findViewById(R.id.tv_nearest_task_title);
        tvNearestSub = findViewById(R.id.tv_nearest_task_sub);
        pbNearest = findViewById(R.id.pb_nearest_task);

        tvStatsNotStarted = findViewById(R.id.tv_stats_not_started_count);
        tvStatsInProgress = findViewById(R.id.tv_stats_in_progress_count);
        tvStatsCompleted = findViewById(R.id.tv_stats_completed_count);
        tvStatsOverdue = findViewById(R.id.tv_stats_overdue_count);

        pbStatsNotStarted = findViewById(R.id.pb_stats_not_started);
        pbStatsInProgress = findViewById(R.id.pb_stats_in_progress);
        pbStatsCompleted = findViewById(R.id.pb_stats_completed);
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
        String[] sources = new String[]{"Tất cả nguồn", "Cá nhân", "Giáo viên giao"};
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSourceFilter.setAdapter(sourceAdapter);

        String[] statuses = new String[]{"Tất cả trạng thái", "Chưa làm", "Đang làm", "Hoàn thành", "Quá hạn"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(statusAdapter);

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

        if (btnOpenQuiz != null) {
            btnOpenQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
            });
        }

        View.OnClickListener openAddEdit = v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            refreshLauncher.launch(intent);
        };

        fabAddTask.setOnClickListener(openAddEdit);
        if (btnHomeAddTask != null) btnHomeAddTask.setOnClickListener(openAddEdit);

        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> {
                long studentId = sessionManager.getUserId();
                File csv = dbHelper.exportStudentDataToCSV(this, studentId);
                if (csv != null) {
                    Toast.makeText(this, "📊 Đã xuất file CSV: " + csv.getName(), Toast.LENGTH_LONG).show();
                    FileUtils.openFile(this, csv);
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra khi xuất CSV!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnToggleReminder != null) {
            btnToggleReminder.setOnClickListener(v -> {
                isReminderEnabled = !isReminderEnabled;
                btnToggleReminder.setText(isReminderEnabled ? "🔔 Đã bật nhắc hạn (Trước 30 phút)" : "🔕 Đã tắt nhắc hạn");
            });
        }
    }

    private void setupRecyclerViews() {
        TaskAdapter.OnTaskClickListener taskClickListener = taskWithProgress -> {
            Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskWithProgress.getTask().getId());
            refreshLauncher.launch(intent);
        };

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, taskClickListener);
        rvTasks.setAdapter(taskAdapter);

        if (rvHomeTasks != null) {
            rvHomeTasks.setLayoutManager(new LinearLayoutManager(this));
            homeTaskAdapter = new TaskAdapter(new ArrayList<>(), taskClickListener);
            rvHomeTasks.setAdapter(homeTaskAdapter);
        }

        if (rvCalendarTasks != null) {
            rvCalendarTasks.setLayoutManager(new LinearLayoutManager(this));
            calendarTaskAdapter = new TaskAdapter(new ArrayList<>(), taskClickListener);
            rvCalendarTasks.setAdapter(calendarTaskAdapter);
        }
    }

    private void setupBottomNavigation() {
        View.OnClickListener tabClickListener = v -> {
            int id = v.getId();
            resetBottomNavStyles();

            if (id == R.id.tab_home) {
                containerTabHome.setVisibility(View.VISIBLE);
                containerTabTasks.setVisibility(View.GONE);
                containerTabCalendar.setVisibility(View.GONE);
                containerTabStats.setVisibility(View.GONE);

                tvTabHome.setBackgroundColor(Color.parseColor("#EFDEF5"));
                tvTabHome.setTextColor(Color.parseColor("#713E87"));
                tvTabHome.setTypeface(null, Typeface.BOLD);

            } else if (id == R.id.tab_tasks) {
                containerTabHome.setVisibility(View.GONE);
                containerTabTasks.setVisibility(View.VISIBLE);
                containerTabCalendar.setVisibility(View.GONE);
                containerTabStats.setVisibility(View.GONE);

                tvTabTasks.setBackgroundColor(Color.parseColor("#EFDEF5"));
                tvTabTasks.setTextColor(Color.parseColor("#713E87"));
                tvTabTasks.setTypeface(null, Typeface.BOLD);

            } else if (id == R.id.tab_calendar) {
                containerTabHome.setVisibility(View.GONE);
                containerTabTasks.setVisibility(View.GONE);
                containerTabCalendar.setVisibility(View.VISIBLE);
                containerTabStats.setVisibility(View.GONE);

                tvTabCalendar.setBackgroundColor(Color.parseColor("#EFDEF5"));
                tvTabCalendar.setTextColor(Color.parseColor("#713E87"));
                tvTabCalendar.setTypeface(null, Typeface.BOLD);

            } else if (id == R.id.tab_stats) {
                containerTabHome.setVisibility(View.GONE);
                containerTabTasks.setVisibility(View.GONE);
                containerTabCalendar.setVisibility(View.GONE);
                containerTabStats.setVisibility(View.VISIBLE);

                tvTabStats.setBackgroundColor(Color.parseColor("#EFDEF5"));
                tvTabStats.setTextColor(Color.parseColor("#713E87"));
                tvTabStats.setTypeface(null, Typeface.BOLD);
            }
        };

        tabHome.setOnClickListener(tabClickListener);
        tabTasks.setOnClickListener(tabClickListener);
        tabCalendar.setOnClickListener(tabClickListener);
        tabStats.setOnClickListener(tabClickListener);
    }

    private void resetBottomNavStyles() {
        tvTabHome.setBackgroundColor(Color.TRANSPARENT);
        tvTabHome.setTextColor(Color.parseColor("#715F75"));
        tvTabHome.setTypeface(null, Typeface.NORMAL);

        tvTabTasks.setBackgroundColor(Color.TRANSPARENT);
        tvTabTasks.setTextColor(Color.parseColor("#715F75"));
        tvTabTasks.setTypeface(null, Typeface.NORMAL);

        tvTabCalendar.setBackgroundColor(Color.TRANSPARENT);
        tvTabCalendar.setTextColor(Color.parseColor("#715F75"));
        tvTabCalendar.setTypeface(null, Typeface.NORMAL);

        tvTabStats.setBackgroundColor(Color.TRANSPARENT);
        tvTabStats.setTextColor(Color.parseColor("#715F75"));
        tvTabStats.setTypeface(null, Typeface.NORMAL);
    }

    private void loadTasks() {
        long studentId = sessionManager.getUserId();
        String searchQuery = etSearch.getText().toString().trim();
        String sourceFilter = spSourceFilter.getSelectedItem() != null ? spSourceFilter.getSelectedItem().toString() : "Tất cả nguồn";
        String statusFilter = spStatusFilter.getSelectedItem() != null ? spStatusFilter.getSelectedItem().toString() : "Tất cả trạng thái";
        String sortOption = spSortFilter.getSelectedItem() != null ? spSortFilter.getSelectedItem().toString() : "Hạn nộp (Tăng dần)";

        taskList = dbHelper.getTasksForStudent(studentId, searchQuery, sourceFilter, statusFilter, sortOption);
        taskAdapter.updateData(taskList);

        if (homeTaskAdapter != null) homeTaskAdapter.updateData(taskList);
        if (calendarTaskAdapter != null) calendarTaskAdapter.updateData(taskList);

        if (taskList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);

            tvNearestTitle.setText("Không có bài tập cận hạn");
            tvNearestSub.setText("Hãy tạo bài tập mới để theo dõi");
            pbNearest.setProgress(0);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);

            // Populates Nearest Deadline Card
            TaskWithProgress nearest = taskList.get(0);
            Task t = nearest.getTask();
            tvNearestTitle.setText(t.getTitle());
            tvNearestSub.setText(nearest.getSubjectOrClassName() + "  ·  Hạn: " + t.getDueDate());
            pbNearest.setProgress(nearest.getProgressPercentage());
        }

        updateStats(studentId, taskList);
    }

    private void updateStats(long studentId, List<TaskWithProgress> tasks) {
        int notStarted = 0;
        int inProgress = 0;
        int completed = 0;
        int overdue = 0;

        String currentDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        for (TaskWithProgress twp : tasks) {
            Task t = twp.getTask();
            TaskProgress p = twp.getProgress();
            String st = p != null ? p.getStatus() : TaskProgress.STATUS_NOT_STARTED;

            if (TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(st)) {
                completed++;
            } else if (TaskProgress.STATUS_IN_PROGRESS.equalsIgnoreCase(st)) {
                inProgress++;
            } else {
                notStarted++;
            }

            if (!TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(st) && t.getDueDate() != null && t.getDueDate().compareTo(currentDateStr) < 0) {
                overdue++;
            }
        }

        int total = tasks.size();
        tvStatsNotStarted.setText(notStarted + " bài");
        tvStatsInProgress.setText(inProgress + " bài");
        tvStatsCompleted.setText(completed + " bài");
        tvStatsOverdue.setText(overdue + " bài");

        pbStatsNotStarted.setProgress(total > 0 ? (int) Math.round((double) notStarted / total * 100.0) : 0);
        pbStatsInProgress.setProgress(total > 0 ? (int) Math.round((double) inProgress / total * 100.0) : 0);
        pbStatsCompleted.setProgress(total > 0 ? (int) Math.round((double) completed / total * 100.0) : 0);
    }
}
