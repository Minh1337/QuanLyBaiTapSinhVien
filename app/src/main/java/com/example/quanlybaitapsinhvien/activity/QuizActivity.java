package com.example.quanlybaitapsinhvien.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.utils.FileUtils;
import com.example.quanlybaitapsinhvien.utils.WindowInsetUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static class QuizQuestion {
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer; // "A", "B", "C", "D"

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
    private Button btnSubmitAnswer, btnDownloadTemplate, btnImportQuiz;
    private ImageButton btnBack;

    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        View headerBar = findViewById(R.id.header_bar);
        View rootView = findViewById(R.id.root_view);
        WindowInsetUtils.applyHeaderAndRootInsets(headerBar, rootView);

        initViews();
        loadSampleQuestionsFromAsset();
        displayCurrentQuestion();

        btnBack.setOnClickListener(v -> finish());

        btnDownloadTemplate.setOnClickListener(v -> {
            File file = FileUtils.copyAssetToDownloads(this, "mau_cau_hoi_trac_nghiem.docx", "Mau_Cau_Hoi_Trac_Nghiem_HNUE.docx");
            if (file != null) {
                FileUtils.openFile(this, file);
            }
        });

        btnImportQuiz.setOnClickListener(v -> {
            loadSampleQuestionsFromAsset();
            currentQuestionIndex = 0;
            currentScore = 0;
            displayCurrentQuestion();
            Toast.makeText(this, "Đã import thành công " + questions.size() + " câu hỏi trắc nghiệm!", Toast.LENGTH_SHORT).show();
        });

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

        btnSubmitAnswer = findViewById(R.id.btn_submit_answer);
        btnDownloadTemplate = findViewById(R.id.btn_download_quiz_template);
        btnImportQuiz = findViewById(R.id.btn_import_quiz);
    }

    private void loadSampleQuestionsFromAsset() {
        questions.clear();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("danh_sach_cau_hoi.txt"), "UTF-8"));
            String line;
            String qText = null, optA = null, optB = null, optC = null, optD = null, ans = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Câu") || line.startsWith("Cau")) {
                    if (qText != null && optA != null && optB != null && optC != null && optD != null && ans != null) {
                        questions.add(new QuizQuestion(qText, optA, optB, optC, optD, ans));
                        optA = optB = optC = optD = ans = null;
                    }
                    qText = line;
                } else if (line.startsWith("A.") || line.startsWith("A :")) {
                    optA = line;
                } else if (line.startsWith("B.") || line.startsWith("B :")) {
                    optB = line;
                } else if (line.startsWith("C.") || line.startsWith("C :")) {
                    optC = line;
                } else if (line.startsWith("D.") || line.startsWith("D :")) {
                    optD = line;
                } else if (line.contains("Đáp án") || line.contains("Dap an") || line.startsWith("Ans:")) {
                    if (line.contains("A")) ans = "A";
                    else if (line.contains("B")) ans = "B";
                    else if (line.contains("C")) ans = "C";
                    else if (line.contains("D")) ans = "D";
                }
            }

            if (qText != null && optA != null && optB != null && optC != null && optD != null && ans != null) {
                questions.add(new QuizQuestion(qText, optA, optB, optC, optD, ans));
            }
            reader.close();
        } catch (Exception e) {
            // Fallback default questions if asset read fails
            questions.add(new QuizQuestion("Câu 1: Ngôn ngữ lập trình chính dùng để phát triển ứng dụng Android gốc là gì?",
                    "A. Swift", "B. Kotlin / Java", "C. C#", "D. PHP", "B"));
            questions.add(new QuizQuestion("Câu 2: SQLite trong Android dùng để làm gì?",
                    "A. Thiết kế giao diện", "B. Lưu trữ dữ liệu quan hệ", "C. Kết nối mạng", "D. Phát video", "B"));
        }
    }

    private void displayCurrentQuestion() {
        if (questions.isEmpty()) {
            tvQuestionContent.setText("Không có câu hỏi nào!");
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
            currentScore += 10;
            Toast.makeText(this, "✅ Chính xác! +10 điểm", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Chế đáp án chưa đúng. Đáp án đúng là: " + q.correctAnswer, Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void showQuizCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành ôn tập!")
                .setMessage("Chúc mừng bạn đã hoàn thành bài trắc nghiệm!\nTổng số điểm đạt được: " + currentScore + " điểm.")
                .setPositiveButton("Làm lại", (dialog, which) -> {
                    currentQuestionIndex = 0;
                    currentScore = 0;
                    displayCurrentQuestion();
                })
                .setNegativeButton("Thoát", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
