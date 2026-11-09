package com.example.quanlybaitapsinhvien.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.db.DatabaseHelper;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.session.SessionManager;
import com.example.quanlybaitapsinhvien.utils.FileUtils;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static class QuizQuestion {
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer; // "A", "B", "C", "D"
        public String imageUri; // Optional image URI

        public QuizQuestion(String questionText, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
        }
    }

    private TextView tvQuestionNumber, tvScore, tvQuestionContent;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private Button btnSubmitAnswer, btnDownloadTemplate, btnImportFileFromDevice, btnEditImportText, btnAttachQuestionImage;
    private ImageButton btnBack;
    private ImageView ivQuestionImage;

    private final List<QuizQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentScore = 0;
    private int correctAnswersCount = 0;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private long taskId = -1;
    private double passingScore = 0.0;

    private String rawImportedText = "";

    private final ActivityResultLauncher<String> pickImportFileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    readTextFromUriAndShowDialog(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> pickQuestionImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && currentQuestionIndex < questions.size()) {
                    String savedUri = FileUtils.copyUriToInternalStorage(this, uri);
                    QuizQuestion q = questions.get(currentQuestionIndex);
                    q.imageUri = savedUri;
                    try {
                        ivQuestionImage.setImageURI(Uri.parse(savedUri));
                        ivQuestionImage.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        ivQuestionImage.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "✅ Đã đính kèm ảnh cho câu hỏi này!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        initViews();
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (getIntent().hasExtra("extra_task_id")) {
            taskId = getIntent().getLongExtra("extra_task_id", -1);
        }
        if (getIntent().hasExtra("passing_score")) {
            passingScore = getIntent().getDoubleExtra("passing_score", 0.0);
        }

        boolean isTeacherOrAdmin = "TEACHER".equalsIgnoreCase(sessionManager.getRole()) || "ADMIN".equalsIgnoreCase(sessionManager.getRole());

        if (getIntent().hasExtra("quiz_uri")) {
            String uriStr = getIntent().getStringExtra("quiz_uri");
            if (uriStr != null) {
                try {
                    readTextFromUriAndParseDirectly(Uri.parse(uriStr));
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể đọc file đính kèm!", Toast.LENGTH_SHORT).show();
                    loadSampleQuestionsFromAsset();
                }
            }
        } else {
            loadSampleQuestionsFromAsset();
        }

        if (!isTeacherOrAdmin) {
            btnDownloadTemplate.setVisibility(View.GONE);
            btnImportFileFromDevice.setVisibility(View.GONE);
            btnEditImportText.setVisibility(View.GONE);
            if (findViewById(R.id.tv_import_label) != null) findViewById(R.id.tv_import_label).setVisibility(View.GONE);
            if (findViewById(R.id.cv_import_section) != null) findViewById(R.id.cv_import_section).setVisibility(View.GONE);
            btnAttachQuestionImage.setVisibility(View.GONE);
        } else {
            btnAttachQuestionImage.setVisibility(View.VISIBLE);
        }
        
        displayCurrentQuestion();

        btnBack.setOnClickListener(v -> finish());

        btnDownloadTemplate.setOnClickListener(v -> {
            FileUtils.copyAssetToDownloads(this, "danh_sach_cau_hoi.txt", "Mau_Cau_Hoi_Trac_Nghiem_HNUE.docx");
        });

        btnImportFileFromDevice.setOnClickListener(v -> pickImportFileLauncher.launch("*/*"));

        btnEditImportText.setOnClickListener(v -> showEditImportDialog(rawImportedText));

        btnAttachQuestionImage.setOnClickListener(v -> pickQuestionImageLauncher.launch("image/*"));

        btnSubmitAnswer.setOnClickListener(v -> submitAnswer());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_quiz_back);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvScore = findViewById(R.id.tv_score);
        tvQuestionContent = findViewById(R.id.tv_question_content);

        rgOptions = findViewById(R.id.rg_options);
        rbA = findViewById(R.id.rb_option_a);
        rbB = findViewById(R.id.rb_option_b);
        rbC = findViewById(R.id.rb_option_c);
        rbD = findViewById(R.id.rb_option_d);

        ivQuestionImage = findViewById(R.id.iv_question_image);
        btnAttachQuestionImage = findViewById(R.id.btn_attach_question_image);

        btnSubmitAnswer = findViewById(R.id.btn_submit_answer);
        btnDownloadTemplate = findViewById(R.id.btn_download_quiz_template);
        btnImportFileFromDevice = findViewById(R.id.btn_import_file_from_device);
        btnEditImportText = findViewById(R.id.btn_edit_import_text);
    }

    private void readTextFromUriAndShowDialog(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                is.close();

                rawImportedText = sb.toString();
                showEditImportDialog(rawImportedText);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void readTextFromUriAndParseDirectly(Uri uri) throws Exception {
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

        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
            rawImportedText = sb.toString();
            parseQuestionsFromRawText(rawImportedText);
        }
    }

    private void showEditImportDialog(String defaultText) {
        final EditText etInput = new EditText(this);
        etInput.setLines(12);
        etInput.setGravity(Gravity.TOP);
        etInput.setText(defaultText != null && !defaultText.trim().isEmpty() ? defaultText : rawImportedText);
        etInput.setHint("Dán hoặc chỉnh sửa danh sách câu hỏi tại đây...\n\nCú pháp mẫu:\nCâu 1: ...\nA. ...\nB. ...\nC. ...\nD. ...\nĐáp án: B");

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa câu hỏi Import")
                .setView(etInput)
                .setPositiveButton("Xác nhận Import", (dialog, which) -> {
                    String editedText = etInput.getText().toString();
                    rawImportedText = editedText;
                    parseQuestionsFromRawText(editedText);
                    currentQuestionIndex = 0;
                    currentScore = 0;
                    displayCurrentQuestion();
                    Toast.makeText(this, "Đã import thành công " + questions.size() + " câu hỏi!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void parseQuestionsFromRawText(String rawText) {
        questions.clear();
        if (rawText == null || rawText.trim().isEmpty()) return;

        String[] lines = rawText.split("\n");
        String qText = null, optA = null, optB = null, optC = null, optD = null, ans = null;

        for (String l : lines) {
            String line = l.trim();
            if (line.isEmpty()) continue;

            String lower = line.toLowerCase();
            if (line.startsWith("Câu") || line.startsWith("Cau") || lower.startsWith("question") || line.startsWith("Q1") || line.startsWith("Q2") || line.startsWith("Q3") || line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.")) {
                if (qText != null && optA != null && optB != null) {
                    if (optC == null) optC = "C. Đáp án C";
                    if (optD == null) optD = "D. Đáp án D";
                    if (ans == null) ans = "A";
                    questions.add(new QuizQuestion(qText, optA, optB, optC, optD, ans));
                    optA = optB = optC = optD = ans = null;
                }
                qText = line;
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
            questions.add(new QuizQuestion(qText, optA, optB, optC, optD, ans));
        }
    }

    private void loadSampleQuestionsFromAsset() {
        try {
            InputStream is = getAssets().open("danh_sach_cau_hoi.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
            rawImportedText = sb.toString();
            parseQuestionsFromRawText(rawImportedText);
        } catch (Exception e) {
            questions.add(new QuizQuestion("Câu 1: Ngôn ngữ lập trình chính dùng để phát triển ứng dụng Android gốc là gì?",
                    "A. Swift", "B. Kotlin / Java", "C. C#", "D. PHP", "B"));
            questions.add(new QuizQuestion("Câu 2: SQLite trong Android dùng để làm gì?",
                    "A. Thiết kế giao diện", "B. Lưu trữ dữ liệu quan hệ", "C. Kết nối mạng", "D. Phát video", "B"));
        }
    }

    private void displayCurrentQuestion() {
        if (questions.isEmpty()) {
            tvQuestionContent.setText("Chưa có câu hỏi nào. Bấm 'Import file từ máy' hoặc 'Chỉnh sửa' để nhập câu hỏi.");
            ivQuestionImage.setVisibility(View.GONE);
            return;
        }

        if (currentQuestionIndex >= questions.size()) {
            showQuizCompletedDialog();
            return;
        }

        QuizQuestion q = questions.get(currentQuestionIndex);
        tvQuestionNumber.setText("Câu hỏi " + (currentQuestionIndex + 1) + " / " + questions.size());
        tvScore.setText("Điểm: " + currentScore);
        tvQuestionContent.setText(q.questionText);

        if (q.imageUri != null && !q.imageUri.isEmpty()) {
            try {
                ivQuestionImage.setImageURI(Uri.parse(q.imageUri));
                ivQuestionImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivQuestionImage.setVisibility(View.GONE);
            }
        } else {
            ivQuestionImage.setVisibility(View.GONE);
        }

        rbA.setText(q.optionA);
        rbB.setText(q.optionB);
        rbC.setText(q.optionC);
        rbD.setText(q.optionD);

        rgOptions.clearCheck();
    }

    private void submitAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) return;

        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn 1 đáp án!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userAns = "A";
        if (selectedId == R.id.rb_option_b) userAns = "B";
        else if (selectedId == R.id.rb_option_c) userAns = "C";
        else if (selectedId == R.id.rb_option_d) userAns = "D";

        QuizQuestion q = questions.get(currentQuestionIndex);
        if (userAns.equalsIgnoreCase(q.correctAnswer)) {
            correctAnswersCount++;
            currentScore += 10;
        }

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void showQuizCompletedDialog() {
        int totalQuestions = questions.isEmpty() ? 1 : questions.size();
        double finalScore = Math.round(((double) correctAnswersCount / totalQuestions * 10.0) * 10.0) / 10.0;
        boolean passed = (passingScore <= 0 || finalScore >= passingScore);
        String autoStatus = passed ? TaskProgress.STATUS_COMPLETED : TaskProgress.STATUS_IN_PROGRESS;

        long studentId = sessionManager != null ? sessionManager.getUserId() : -1;
        if (taskId != -1 && studentId != -1 && dbHelper != null) {
            dbHelper.updateStudentQuizResult(taskId, studentId, finalScore, autoStatus);
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Kết quả bài trắc nghiệm:\n");
        msg.append("• Số câu đúng: ").append(correctAnswersCount).append(" / ").append(totalQuestions).append("\n");
        msg.append("• Điểm số: ").append(String.format(Locale.getDefault(), "%.1f", finalScore)).append(" / 10.0\n");

        if (passingScore > 0) {
            msg.append("• Mức điểm cần đạt: ").append(String.format(Locale.getDefault(), "%.1f", passingScore)).append(" / 10.0\n");
            if (passed) {
                msg.append("• Trạng thái: ✅ ĐẠT (Đã tự động cập nhật Hoàn thành)");
            } else {
                msg.append("• Trạng thái: ❌ CHƯA ĐẠT (Hãy thử lại!)");
            }
        } else {
            msg.append("• Tiến độ: ✅ Tự động cập nhật HOÀN THÀNH!");
        }

        new AlertDialog.Builder(this)
                .setTitle("🎉 Hoàn thành bài trắc nghiệm!")
                .setMessage(msg.toString())
                .setPositiveButton("Làm lại", (dialog, which) -> {
                    currentQuestionIndex = 0;
                    currentScore = 0;
                    correctAnswersCount = 0;
                    displayCurrentQuestion();
                })
                .setNegativeButton("Thoát", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
