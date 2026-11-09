package com.example.quanlybaitapsinhvien.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.quanlybaitapsinhvien.model.PersonalSubject;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.FileUtils;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;
import com.google.android.material.card.MaterialCardView;
import android.view.Gravity;
import android.widget.ImageView;
import java.io.FileOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvHeaderTitle, tvReadOnlyBanner, tvAttachedFile, tvAttachedQuiz;
    private EditText etTaskTitle, etTaskDesc, etDueDate, etPassingScore;
    private Spinner spSubject, spPriority, spStatus, spTaskType;
    private Button btnPickDate, btnSaveTask, btnAttachFile, btnAttachQuiz, btnDownloadQuizTemplate;
    private ImageButton btnBack, btnDeleteTask;
    private CheckBox cbRequiresSubmission;
    private View layoutAttachQuiz;
    private LinearLayout layoutImportedQuizPreview;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    public static final String EXTRA_CLASS_ID = "extra_class_id";
    private long taskId = -1;
    private long preselectedClassId = -1;
    private TaskWithProgress currentTaskWithProgress;
    private List<PersonalSubject> personalSubjects = new ArrayList<>();
    private List<ClassModel> studentClasses = new ArrayList<>();
    private String currentAttachmentUri = null;
    private String currentQuizUri = null;

    private final ActivityResultLauncher<String> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentAttachmentUri = FileUtils.copyUriToInternalStorage(this, uri);
                    String fileName = FileUtils.getFileNameFromUri(this, Uri.parse(currentAttachmentUri));
                    tvAttachedFile.setText(fileName);
                    Toast.makeText(this, "Đã đính kèm tài liệu: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
    );
    
    private final ActivityResultLauncher<String> pickQuizLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentQuizUri = FileUtils.copyUriToInternalStorage(this, uri);
                    String fileName = FileUtils.getFileNameFromUri(this, Uri.parse(currentQuizUri));
                    tvAttachedQuiz.setText(fileName);
                    Toast.makeText(this, "✅ Đã import file câu hỏi trắc nghiệm thành công!", Toast.LENGTH_SHORT).show();
                    renderImportedQuizPreview(currentQuizUri);
                }
            }
    );

    private int selectedQuestionImageIndex = -1;
    private List<QuizActivity.QuizQuestion> currentParsedQuestions = new ArrayList<>();
    private String pendingNewQuestionImageUri = null;
    private TextView tvDialogImageStatus = null;

    private final ActivityResultLauncher<String> pickQuestionImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && selectedQuestionImageIndex != -1 && selectedQuestionImageIndex < currentParsedQuestions.size()) {
                    String savedUri = FileUtils.copyUriToInternalStorage(this, uri);
                    QuizActivity.QuizQuestion q = currentParsedQuestions.get(selectedQuestionImageIndex);
                    q.imageUri = savedUri;
                    saveQuestionsToQuizFile(currentParsedQuestions);
                    renderImportedQuizPreview(currentQuizUri);
                    Toast.makeText(this, "✅ Đã đính kèm ảnh cho câu hỏi " + (selectedQuestionImageIndex + 1), Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> pickDialogQuestionImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    pendingNewQuestionImageUri = FileUtils.copyUriToInternalStorage(this, uri);
                    String fileName = FileUtils.getFileNameFromUri(this, uri);
                    if (tvDialogImageStatus != null) {
                        tvDialogImageStatus.setText("✅ Ảnh: " + fileName);
                        tvDialogImageStatus.setTextColor(Color.parseColor("#2E7D32"));
                    }
                    Toast.makeText(this, "✅ Đã chọn ảnh cho câu hỏi!", Toast.LENGTH_SHORT).show();
                }
            }
    );



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!"TEACHER".equalsIgnoreCase(sessionManager.getRole())) {
            Toast.makeText(this, "Chỉ Giảng viên mới có quyền tạo và giao bài tập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_add_edit_task);

        dbHelper = new DatabaseHelper(this);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        if (getIntent().hasExtra(EXTRA_TASK_ID)) {
            taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        }
        if (getIntent().hasExtra(EXTRA_CLASS_ID)) {
            preselectedClassId = getIntent().getLongExtra(EXTRA_CLASS_ID, -1);
        }

        initViews();
        setupSpinners();
        loadSubjectsAndClasses();

        if (taskId != -1) {
            loadTaskData();
        } else {
            tvHeaderTitle.setText("GIAO BÀI TẬP LỚP");
            btnDeleteTask.setVisibility(View.GONE);
        }

        btnPickDate.setOnClickListener(v -> showDateTimePicker());
        btnSaveTask.setOnClickListener(v -> saveTask());
        btnDeleteTask.setOnClickListener(v -> confirmDeleteTask());
        btnBack.setOnClickListener(v -> finish());
        btnAttachFile.setOnClickListener(v -> pickFileLauncher.launch("*/*"));
        btnAttachQuiz.setOnClickListener(v -> pickQuizLauncher.launch("text/plain"));
        btnDownloadQuizTemplate.setOnClickListener(v -> {
            FileUtils.copyAssetToDownloads(this, "danh_sach_cau_hoi.txt", "Mau_Cau_Hoi_Trac_Nghiem_HNUE.txt");
        });
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvReadOnlyBanner = findViewById(R.id.tv_read_only_banner);
        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDesc = findViewById(R.id.et_task_desc);
        etDueDate = findViewById(R.id.et_due_date);
        etPassingScore = findViewById(R.id.et_passing_score);
        spSubject = findViewById(R.id.sp_subject);
        spPriority = findViewById(R.id.sp_priority);
        spStatus = findViewById(R.id.sp_status);
        spTaskType = findViewById(R.id.sp_task_type);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSaveTask = findViewById(R.id.btn_save_task);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        tvAttachedFile = findViewById(R.id.tv_attached_file);
        btnAttachQuiz = findViewById(R.id.btn_attach_quiz);
        tvAttachedQuiz = findViewById(R.id.tv_attached_quiz);
        btnDownloadQuizTemplate = findViewById(R.id.btn_download_quiz_template);
        cbRequiresSubmission = findViewById(R.id.cb_requires_submission);
        layoutAttachQuiz = findViewById(R.id.layout_attach_quiz);
        layoutImportedQuizPreview = findViewById(R.id.layout_imported_quiz_preview);
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

        // Task Type Spinner
        String[] types = new String[]{Task.TYPE_NORMAL, Task.TYPE_QUIZ};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTaskType.setAdapter(typeAdapter);
        
        spTaskType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) { // Quiz
                    layoutAttachQuiz.setVisibility(View.VISIBLE);
                    btnDownloadQuizTemplate.setVisibility(View.VISIBLE);
                    if (cbRequiresSubmission != null && taskId == -1) cbRequiresSubmission.setChecked(false);
                } else { // Normal
                    layoutAttachQuiz.setVisibility(View.GONE);
                    btnDownloadQuizTemplate.setVisibility(View.GONE);
                    if (cbRequiresSubmission != null && taskId == -1) cbRequiresSubmission.setChecked(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSubjectsAndClasses() {
        long userId = sessionManager.getUserId();
        String role = sessionManager.getRole();
        studentClasses = new ArrayList<>();

        List<String> subjectOptions = new ArrayList<>();
        if ("ADMIN".equalsIgnoreCase(role)) {
            studentClasses = dbHelper.getAllActiveClasses();
            for (ClassModel c : studentClasses) {
                subjectOptions.add("[Lớp học phần] " + c.getClassCode() + " - " + c.getClassName());
            }
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            studentClasses = dbHelper.getClassesForTeacher(userId);
            for (ClassModel c : studentClasses) {
                subjectOptions.add("[Lớp học phần] " + c.getClassCode() + " - " + c.getClassName());
            }
        }

        if (subjectOptions.isEmpty()) {
            subjectOptions.add("(Chưa có lớp học phần nào - Admin cần tạo lớp trước)");
        }

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectOptions);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubject.setAdapter(subjectAdapter);
        
        if (preselectedClassId != -1) {
            for (int i = 0; i < studentClasses.size(); i++) {
                if (studentClasses.get(i).getId() == preselectedClassId) {
                    spSubject.setSelection(i);
                    break;
                }
            }
        }
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
        if (etPassingScore != null) {
            etPassingScore.setText(task.getPassingScore() > 0 ? String.valueOf(task.getPassingScore()) : "");
        }
        if (cbRequiresSubmission != null) {
            cbRequiresSubmission.setChecked(task.isRequiresSubmission());
        }

        currentAttachmentUri = task.getAttachmentPath();
        if (currentAttachmentUri != null && !currentAttachmentUri.trim().isEmpty()) {
            tvAttachedFile.setText(FileUtils.getFileNameFromUri(this, Uri.parse(currentAttachmentUri)));
        } else {
            tvAttachedFile.setText("Chưa đính kèm");
        }
        
        currentQuizUri = task.getQuizFilePath();
        if (currentQuizUri != null && !currentQuizUri.trim().isEmpty()) {
            tvAttachedQuiz.setText(FileUtils.getFileNameFromUri(this, Uri.parse(currentQuizUri)));
            renderImportedQuizPreview(currentQuizUri);
        } else {
            tvAttachedQuiz.setText("Chưa import (.txt / .docx)");
            if (layoutImportedQuizPreview != null) layoutImportedQuizPreview.setVisibility(View.GONE);
        }

        if (Task.TYPE_QUIZ.equalsIgnoreCase(task.getTaskType())) {
            spTaskType.setSelection(1);
            layoutAttachQuiz.setVisibility(View.VISIBLE);
            btnDownloadQuizTemplate.setVisibility(View.VISIBLE);
        } else {
            spTaskType.setSelection(0);
            layoutAttachQuiz.setVisibility(View.GONE);
            btnDownloadQuizTemplate.setVisibility(View.GONE);
        }

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

        tvReadOnlyBanner.setVisibility(View.GONE);
        btnDeleteTask.setVisibility(View.VISIBLE);

        if (task.getClassId() != null) {
            for (int i = 0; i < studentClasses.size(); i++) {
                if (studentClasses.get(i).getId() == task.getClassId().longValue()) {
                    spSubject.setSelection(i);
                    break;
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

        if (title.isEmpty()) {
            etTaskTitle.setError("Vui lòng nhập tên bài tập");
            etTaskTitle.requestFocus();
            return;
        }

        if (dueDate.isEmpty()) {
            etDueDate.setError("Vui lòng chọn hạn nộp");
            etDueDate.requestFocus();
            return;
        }

        if (studentClasses == null || studentClasses.isEmpty()) {
            Toast.makeText(this, "Chưa có lớp học phần nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        Long classId = studentClasses.get(spSubject.getSelectedItemPosition()).getId();
        String taskTypeStr = spTaskType.getSelectedItem() != null ? spTaskType.getSelectedItem().toString() : Task.TYPE_NORMAL;

        double passingScore = 0.0;
        if (etPassingScore != null) {
            String pStr = etPassingScore.getText().toString().trim();
            if (!pStr.isEmpty()) {
                try {
                    passingScore = Double.parseDouble(pStr);
                } catch (Exception ignored) {}
            }
        }

        boolean reqSub = cbRequiresSubmission != null && cbRequiresSubmission.isChecked();

        Task task = new Task(
                taskId != -1 ? taskId : 0,
                title,
                desc,
                dueDate,
                Task.SOURCE_TEACHER,
                classId,
                null,
                sessionManager.getUserId(),
                currentAttachmentUri,
                currentQuizUri,
                taskTypeStr,
                passingScore,
                reqSub
        );

        if (taskId == -1) {
            long newId = dbHelper.insertTeacherTask(task);
            if (newId != -1) {
                Toast.makeText(this, "Giao bài tập thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Tạo bài tập thất bại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.updateTeacherTask(task);
            if (success) {
                Toast.makeText(this, "Đã cập nhật bài tập!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDeleteTask() {
        if (taskId == -1) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa bài tập")
                .setMessage("Bạn có chắc chắn muốn xóa bài tập này? Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteTeacherTask(taskId);
                    if (deleted) {
                        Toast.makeText(this, "Đã xóa bài tập!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Xóa bài tập thất bại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }



    private void saveQuestionsToQuizFile(List<QuizActivity.QuizQuestion> questions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            QuizActivity.QuizQuestion q = questions.get(i);
            sb.append(q.questionText).append("\n");
            if (q.imageUri != null && !q.imageUri.isEmpty()) {
                sb.append("[Ảnh: ").append(q.imageUri).append("]\n");
            }
            sb.append(q.optionA).append("\n");
            sb.append(q.optionB).append("\n");
            sb.append(q.optionC).append("\n");
            sb.append(q.optionD).append("\n");
            sb.append("Đáp án: ").append(q.correctAnswer).append("\n\n");
        }

        try {
            File dir = new File(getFilesDir(), "attachments");
            if (!dir.exists()) dir.mkdirs();
            File quizFile = new File(dir, "quiz_" + System.currentTimeMillis() + ".txt");
            try (FileOutputStream fos = new FileOutputStream(quizFile)) {
                fos.write(sb.toString().getBytes("UTF-8"));
                fos.flush();
            }
            currentQuizUri = Uri.fromFile(quizFile).toString();
            tvAttachedQuiz.setText(FileUtils.getFileNameFromUri(this, Uri.parse(currentQuizUri)));
        } catch (Exception e) {
            Log.e("AddEditTaskActivity", "Error saving quiz questions file: " + e.getMessage(), e);
        }
    }

    private void showAddQuestionDialog() {
        pendingNewQuestionImageUri = null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etQuestion = new EditText(this);
        etQuestion.setHint("Tên câu hỏi (ví dụ: Câu 1: Thủ đô VN là gì?)");

        // Image Attachment Layout inside dialog
        LinearLayout imgLayout = new LinearLayout(this);
        imgLayout.setOrientation(LinearLayout.HORIZONTAL);
        imgLayout.setGravity(Gravity.CENTER_VERTICAL);
        imgLayout.setPadding(0, 12, 0, 8);

        Button btnChooseImg = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnChooseImg.setText("📷 Upload ảnh câu hỏi");
        btnChooseImg.setTextSize(11);
        btnChooseImg.setTextColor(Color.parseColor("#713E87"));

        tvDialogImageStatus = new TextView(this);
        tvDialogImageStatus.setText(" Chưa chọn ảnh");
        tvDialogImageStatus.setTextSize(11);
        tvDialogImageStatus.setTextColor(Color.GRAY);

        btnChooseImg.setOnClickListener(v -> pickDialogQuestionImageLauncher.launch("image/*"));

        imgLayout.addView(btnChooseImg);
        imgLayout.addView(tvDialogImageStatus);

        final EditText etOptA = new EditText(this);
        etOptA.setHint("A. TP. Hồ Chí Minh");

        final EditText etOptB = new EditText(this);
        etOptB.setHint("B. Hà Nội");

        final EditText etOptC = new EditText(this);
        etOptC.setHint("C. Đà Nẵng");

        final EditText etOptD = new EditText(this);
        etOptD.setHint("D. Cần Thơ");

        final EditText etAns = new EditText(this);
        etAns.setHint("Đáp án đúng (A, B, C hoặc D)");

        layout.addView(etQuestion);
        layout.addView(imgLayout);
        layout.addView(etOptA);
        layout.addView(etOptB);
        layout.addView(etOptC);
        layout.addView(etOptD);
        layout.addView(etAns);

        new AlertDialog.Builder(this)
                .setTitle("Thêm câu hỏi trắc nghiệm mới")
                .setView(layout)
                .setPositiveButton("Thêm câu hỏi", (dialog, which) -> {
                    String qText = etQuestion.getText().toString().trim();
                    String aText = etOptA.getText().toString().trim();
                    String bText = etOptB.getText().toString().trim();
                    String cText = etOptC.getText().toString().trim();
                    String dText = etOptD.getText().toString().trim();
                    String ansText = etAns.getText().toString().trim().toUpperCase();

                    if (qText.isEmpty() || aText.isEmpty() || bText.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên câu hỏi và các phương án!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!aText.startsWith("A.")) aText = "A. " + aText;
                    if (!bText.startsWith("B.")) bText = "B. " + bText;
                    if (cText.isEmpty()) cText = "C. Phương án C";
                    else if (!cText.startsWith("C.")) cText = "C. " + cText;
                    if (dText.isEmpty()) dText = "D. Phương án D";
                    else if (!dText.startsWith("D.")) dText = "D. " + dText;

                    if (!ansText.equals("A") && !ansText.equals("B") && !ansText.equals("C") && !ansText.equals("D")) {
                        ansText = "A";
                    }

                    QuizActivity.QuizQuestion newQuest = new QuizActivity.QuizQuestion(qText, aText, bText, cText, dText, ansText);
                    newQuest.imageUri = pendingNewQuestionImageUri;
                    currentParsedQuestions.add(newQuest);
                    saveQuestionsToQuizFile(currentParsedQuestions);
                    renderImportedQuizPreview(currentQuizUri);
                    Toast.makeText(this, "✅ Đã thêm câu hỏi trắc nghiệm mới!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void renderImportedQuizPreview(String uriStr) {
        if (layoutImportedQuizPreview == null) return;
        layoutImportedQuizPreview.removeAllViews();

        if (uriStr == null || uriStr.trim().isEmpty()) {
            layoutImportedQuizPreview.setVisibility(View.GONE);
            return;
        }

        try {
            Uri uri = Uri.parse(uriStr);
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                String path = uri.getPath();
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        is = new FileInputStream(file);
                    }
                }
            }

            if (is == null) {
                layoutImportedQuizPreview.setVisibility(View.GONE);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();

            String rawText = sb.toString();
            currentParsedQuestions = parseQuestionsFromRawText(rawText);

            if (currentParsedQuestions.isEmpty()) {
                layoutImportedQuizPreview.setVisibility(View.GONE);
                return;
            }

            layoutImportedQuizPreview.setVisibility(View.VISIBLE);

            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setGravity(Gravity.CENTER_VERTICAL);
            headerLayout.setPadding(0, 12, 0, 8);

            TextView tvHeader = new TextView(this);
            tvHeader.setText("📋 CÂU HỎI TRẮC NGHIỆM (" + currentParsedQuestions.size() + " câu):");
            tvHeader.setTextSize(13);
            tvHeader.setTypeface(null, Typeface.BOLD);
            tvHeader.setTextColor(Color.parseColor("#713E87"));
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvHeader.setLayoutParams(hLp);

            Button btnAddQuestion = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnAddQuestion.setText("➕ Thêm câu hỏi");
            btnAddQuestion.setTextSize(11);
            btnAddQuestion.setOnClickListener(v -> showAddQuestionDialog());

            headerLayout.addView(tvHeader);
            headerLayout.addView(btnAddQuestion);
            layoutImportedQuizPreview.addView(headerLayout);

            for (int i = 0; i < currentParsedQuestions.size(); i++) {
                final int qIndex = i;
                QuizActivity.QuizQuestion q = currentParsedQuestions.get(i);

                MaterialCardView card = new MaterialCardView(this);
                card.setRadius(12f);
                card.setCardElevation(2f);
                card.setCardBackgroundColor(Color.WHITE);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.setMargins(0, 8, 0, 8);
                card.setLayoutParams(clp);

                LinearLayout cardContent = new LinearLayout(this);
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setPadding(16, 16, 16, 16);

                TextView tvQuestion = new TextView(this);
                tvQuestion.setText(q.questionText);
                tvQuestion.setTextSize(14);
                tvQuestion.setTypeface(null, Typeface.BOLD);
                tvQuestion.setTextColor(Color.parseColor("#382841"));
                cardContent.addView(tvQuestion);

                if (q.imageUri != null && !q.imageUri.isEmpty()) {
                    ImageView ivQuestionImg = new ImageView(this);
                    LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320);
                    imgLp.setMargins(0, 8, 0, 8);
                    ivQuestionImg.setLayoutParams(imgLp);
                    ivQuestionImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    try {
                        ivQuestionImg.setImageURI(Uri.parse(q.imageUri));
                        cardContent.addView(ivQuestionImg);
                    } catch (Exception ignored) {}
                }

                RadioGroup rg = new RadioGroup(this);
                rg.setPadding(0, 8, 0, 0);

                RadioButton rbA = new RadioButton(this);
                rbA.setText(q.optionA);
                rbA.setTextSize(13);

                RadioButton rbB = new RadioButton(this);
                rbB.setText(q.optionB);
                rbB.setTextSize(13);

                RadioButton rbC = new RadioButton(this);
                rbC.setText(q.optionC);
                rbC.setTextSize(13);

                RadioButton rbD = new RadioButton(this);
                rbD.setText(q.optionD);
                rbD.setTextSize(13);

                // Pre-select correct answer for Teacher/Admin when importing
                if ("A".equalsIgnoreCase(q.correctAnswer)) rbA.setChecked(true);
                else if ("B".equalsIgnoreCase(q.correctAnswer)) rbB.setChecked(true);
                else if ("C".equalsIgnoreCase(q.correctAnswer)) rbC.setChecked(true);
                else if ("D".equalsIgnoreCase(q.correctAnswer)) rbD.setChecked(true);

                rg.addView(rbA);
                rg.addView(rbB);
                rg.addView(rbC);
                rg.addView(rbD);

                TextView tvAnsLabel = new TextView(this);
                tvAnsLabel.setText("✅ Đáp án đúng: Phương án " + q.correctAnswer);
                tvAnsLabel.setTextSize(12);
                tvAnsLabel.setTextColor(Color.parseColor("#2E7D32"));
                tvAnsLabel.setTypeface(null, Typeface.BOLD);
                tvAnsLabel.setPadding(0, 4, 0, 8);

                Button btnUploadImg = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                btnUploadImg.setText(q.imageUri != null && !q.imageUri.isEmpty() ? "📷 Đổi ảnh câu hỏi" : "📷 Up ảnh câu hỏi");
                btnUploadImg.setTextSize(11);
                btnUploadImg.setOnClickListener(v -> {
                    selectedQuestionImageIndex = qIndex;
                    pickQuestionImageLauncher.launch("image/*");
                });

                cardContent.addView(rg);
                cardContent.addView(tvAnsLabel);
                cardContent.addView(btnUploadImg);
                card.addView(cardContent);

                layoutImportedQuizPreview.addView(card);
            }

        } catch (Exception e) {
            Log.e("AddEditTaskActivity", "Error parsing imported quiz preview: " + e.getMessage(), e);
            layoutImportedQuizPreview.setVisibility(View.GONE);
        }
    }

    private List<QuizActivity.QuizQuestion> parseQuestionsFromRawText(String rawText) {
        List<QuizActivity.QuizQuestion> list = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) return list;

        String[] lines = rawText.split("\n");
        String qText = null, optA = null, optB = null, optC = null, optD = null, ans = null, imgUri = null;

        for (String l : lines) {
            String line = l.trim();
            if (line.isEmpty()) continue;

            String lower = line.toLowerCase();
            if (line.startsWith("Câu") || line.startsWith("Cau") || lower.startsWith("question") || line.startsWith("Q1") || line.startsWith("Q2") || line.startsWith("Q3") || line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.")) {
                if (qText != null && optA != null && optB != null) {
                    if (optC == null) optC = "C. Đáp án C";
                    if (optD == null) optD = "D. Đáp án D";
                    if (ans == null) ans = "A";
                    QuizActivity.QuizQuestion quest = new QuizActivity.QuizQuestion(qText, optA, optB, optC, optD, ans);
                    quest.imageUri = imgUri;
                    list.add(quest);
                    optA = optB = optC = optD = ans = imgUri = null;
                }
                qText = line;
            } else if (line.startsWith("[Ảnh:") || line.startsWith("[Image:") || line.startsWith("Ảnh:") || line.startsWith("Image:")) {
                int start = line.indexOf(":") + 1;
                int end = line.endsWith("]") ? line.length() - 1 : line.length();
                if (start < end) {
                    imgUri = line.substring(start, end).trim();
                }
            } else if (line.startsWith("A.") || line.startsWith("A:") || line.startsWith("A)") || line.startsWith("A -") || line.startsWith("a.")) {
                optA = line;
            } else if (line.startsWith("B.") || line.startsWith("B:") || line.startsWith("B)") || line.startsWith("B -") || line.startsWith("b.")) {
                optB = line;
            } else if (line.startsWith("C.") || line.startsWith("C:") || line.startsWith("C)") || line.startsWith("C -") || line.startsWith("c.")) {
                optC = line;
            } else if (line.startsWith("D.") || line.startsWith("D:") || line.startsWith("D)") || line.startsWith("D -") || line.startsWith("d.")) {
                optD = line;
            } else if (line.contains("Đáp án") || line.contains("Dap an") || line.contains("Answer") || line.startsWith("Ans:")) {
                String upper = line.toUpperCase();
                if (upper.contains("A")) ans = "A";
                else if (upper.contains("B")) ans = "B";
                else if (upper.contains("C")) ans = "C";
                else if (upper.contains("D")) ans = "D";
            }
        }

        if (qText != null && optA != null && optB != null) {
            if (optC == null) optC = "C. Đáp án C";
            if (optD == null) optD = "D. Đáp án D";
            if (ans == null) ans = "A";
            QuizActivity.QuizQuestion quest = new QuizActivity.QuizQuestion(qText, optA, optB, optC, optD, ans);
            quest.imageUri = imgUri;
            list.add(quest);
        }

        return list;
    }
}
