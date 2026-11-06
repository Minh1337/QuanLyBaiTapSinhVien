package com.example.quanlybaitapsinhvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quanlybaitapsinhvien.model.ChecklistItem;
import com.example.quanlybaitapsinhvien.model.ClassModel;
import com.example.quanlybaitapsinhvien.model.PersonalSubject;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;
import com.example.quanlybaitapsinhvien.model.User;
import com.example.quanlybaitapsinhvien.utils.PasswordUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quanlybaitap.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CLASSES = "classes";
    private static final String TABLE_CLASS_MEMBERS = "class_members";
    private static final String TABLE_PERSONAL_SUBJECTS = "personal_subjects";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_TASK_PROGRESS = "student_task_progress";
    private static final String TABLE_CHECKLIST = "checklist_items";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "role TEXT NOT NULL, " + // ADMIN, TEACHER, STUDENT
                "status TEXT NOT NULL" + // ACTIVE, LOCKED
                ")");

        // 2. Classes table
        db.execSQL("CREATE TABLE " + TABLE_CLASSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "class_code TEXT UNIQUE NOT NULL, " +
                "class_name TEXT NOT NULL, " +
                "teacher_id INTEGER, " +
                "is_archived INTEGER DEFAULT 0" +
                ")");

        // 3. Class Members table
        db.execSQL("CREATE TABLE " + TABLE_CLASS_MEMBERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "class_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "UNIQUE(class_id, student_id)" +
                ")");

        // 4. Personal Subjects table
        db.execSQL("CREATE TABLE " + TABLE_PERSONAL_SUBJECTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "subject_code TEXT NOT NULL, " +
                "subject_name TEXT NOT NULL, " +
                "student_id INTEGER NOT NULL" +
                ")");

        // 5. Tasks table
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "due_date TEXT NOT NULL, " +
                "source_type TEXT NOT NULL, " + // PERSONAL or TEACHER
                "class_id INTEGER, " +
                "personal_subject_id INTEGER, " +
                "created_by_user_id INTEGER NOT NULL" +
                ")");

        // 6. Task Progress table
        db.execSQL("CREATE TABLE " + TABLE_TASK_PROGRESS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " + // Chưa làm, Đang làm, Hoàn thành
                "priority TEXT NOT NULL, " + // Thấp, Trung bình, Cao
                "reminder_time TEXT, " +
                "UNIQUE(task_id, student_id)" +
                ")");

        // 7. Checklist Items table
        db.execSQL("CREATE TABLE " + TABLE_CHECKLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "progress_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "is_completed INTEGER DEFAULT 0" +
                ")");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONAL_SUBJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Seed Admin
        String saltAdmin = PasswordUtils.generateSalt();
        String hashAdmin = PasswordUtils.hashPassword("admin123", saltAdmin);
        ContentValues cvAdmin = new ContentValues();
        cvAdmin.put("username", "admin");
        cvAdmin.put("password_hash", hashAdmin);
        cvAdmin.put("salt", saltAdmin);
        cvAdmin.put("full_name", "Quản trị viên Hệ thống");
        cvAdmin.put("role", "ADMIN");
        cvAdmin.put("status", "ACTIVE");
        db.insert(TABLE_USERS, null, cvAdmin);

        // Seed Teacher
        String saltTeacher = PasswordUtils.generateSalt();
        String hashTeacher = PasswordUtils.hashPassword("gv123456", saltTeacher);
        ContentValues cvTeacher = new ContentValues();
        cvTeacher.put("username", "gv_nguyen");
        cvTeacher.put("password_hash", hashTeacher);
        cvTeacher.put("salt", saltTeacher);
        cvTeacher.put("full_name", "ThS. Nguyễn Văn Thầy");
        cvTeacher.put("role", "TEACHER");
        cvTeacher.put("status", "ACTIVE");
        long teacherId = db.insert(TABLE_USERS, null, cvTeacher);

        // Seed Student A
        String saltA = PasswordUtils.generateSalt();
        String hashA = PasswordUtils.hashPassword("sv123456", saltA);
        ContentValues cvA = new ContentValues();
        cvA.put("username", "sv_a");
        cvA.put("password_hash", hashA);
        cvA.put("salt", saltA);
        cvA.put("full_name", "Nguyễn Văn A");
        cvA.put("role", "STUDENT");
        cvA.put("status", "ACTIVE");
        long studentAId = db.insert(TABLE_USERS, null, cvA);

        // Seed Student B
        String saltB = PasswordUtils.generateSalt();
        String hashB = PasswordUtils.hashPassword("sv123456", saltB);
        ContentValues cvB = new ContentValues();
        cvB.put("username", "sv_b");
        cvB.put("password_hash", hashB);
        cvB.put("salt", saltB);
        cvB.put("full_name", "Trần Thị B");
        cvB.put("role", "STUDENT");
        cvB.put("status", "ACTIVE");
        long studentBId = db.insert(TABLE_USERS, null, cvB);

        // Seed Class MOB01
        ContentValues cvClass = new ContentValues();
        cvClass.put("class_code", "MOB01");
        cvClass.put("class_name", "Lập trình Android MOB01");
        cvClass.put("teacher_id", teacherId);
        cvClass.put("is_archived", 0);
        long classId = db.insert(TABLE_CLASSES, null, cvClass);

        // Seed Class Members
        ContentValues cvMemA = new ContentValues();
        cvMemA.put("class_id", classId);
        cvMemA.put("student_id", studentAId);
        db.insert(TABLE_CLASS_MEMBERS, null, cvMemA);

        ContentValues cvMemB = new ContentValues();
        cvMemB.put("class_id", classId);
        cvMemB.put("student_id", studentBId);
        db.insert(TABLE_CLASS_MEMBERS, null, cvMemB);

        // Seed Personal Subject for Student A
        ContentValues cvSubA = new ContentValues();
        cvSubA.put("subject_code", "CT202");
        cvSubA.put("subject_name", "Cơ sở dữ liệu");
        cvSubA.put("student_id", studentAId);
        long personalSubjectAId = db.insert(TABLE_PERSONAL_SUBJECTS, null, cvSubA);

        // Seed Personal Task for Student A
        ContentValues cvTaskA = new ContentValues();
        cvTaskA.put("title", "Làm bài tập lớn CSDL");
        cvTaskA.put("description", "Thiết kế ERD và viết các câu lệnh SQL truy vấn cho hệ thống bán hàng.");
        cvTaskA.put("due_date", "2025-05-30 23:59");
        cvTaskA.put("source_type", Task.SOURCE_PERSONAL);
        cvTaskA.put("personal_subject_id", personalSubjectAId);
        cvTaskA.put("created_by_user_id", studentAId);
        long taskPersonalId = db.insert(TABLE_TASKS, null, cvTaskA);

        // Seed Progress for Personal Task A
        ContentValues cvProgA1 = new ContentValues();
        cvProgA1.put("task_id", taskPersonalId);
        cvProgA1.put("student_id", studentAId);
        cvProgA1.put("status", TaskProgress.STATUS_IN_PROGRESS);
        cvProgA1.put("priority", TaskProgress.PRIORITY_HIGH);
        long progA1Id = db.insert(TABLE_TASK_PROGRESS, null, cvProgA1);

        // Seed Checklist for Personal Task A
        ContentValues cvCheck1 = new ContentValues();
        cvCheck1.put("progress_id", progA1Id);
        cvCheck1.put("content", "Vẽ sơ đồ ERD");
        cvCheck1.put("is_completed", 1);
        db.insert(TABLE_CHECKLIST, null, cvCheck1);

        ContentValues cvCheck2 = new ContentValues();
        cvCheck2.put("progress_id", progA1Id);
        cvCheck2.put("content", "Viết script SQL tao bang");
        cvCheck2.put("is_completed", 0);
        db.insert(TABLE_CHECKLIST, null, cvCheck2);

        // Seed Teacher Task in Class MOB01
        ContentValues cvTeacherTask = new ContentValues();
        cvTeacherTask.put("title", "Nộp đồ án Android Cuối kỳ");
        cvTeacherTask.put("description", "Xây dựng ứng dụng Sổ bài tập sinh viên đầy đủ chức năng, giao diện tiếng Việt.");
        cvTeacherTask.put("due_date", "2025-06-15 23:59");
        cvTeacherTask.put("source_type", Task.SOURCE_TEACHER);
        cvTeacherTask.put("class_id", classId);
        cvTeacherTask.put("created_by_user_id", teacherId);
        long teacherTaskId = db.insert(TABLE_TASKS, null, cvTeacherTask);

        // Seed Progress for Student A for Teacher Task
        ContentValues cvProgA2 = new ContentValues();
        cvProgA2.put("task_id", teacherTaskId);
        cvProgA2.put("student_id", studentAId);
        cvProgA2.put("status", TaskProgress.STATUS_NOT_STARTED);
        cvProgA2.put("priority", TaskProgress.PRIORITY_MEDIUM);
        db.insert(TABLE_TASK_PROGRESS, null, cvProgA2);

        // Seed Progress for Student B for Teacher Task
        ContentValues cvProgB1 = new ContentValues();
        cvProgB1.put("task_id", teacherTaskId);
        cvProgB1.put("student_id", studentBId);
        cvProgB1.put("status", TaskProgress.STATUS_NOT_STARTED);
        cvProgB1.put("priority", TaskProgress.PRIORITY_LOW);
        db.insert(TABLE_TASK_PROGRESS, null, cvProgB1);
    }

    // AUTHENTICATION
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        username = username.trim().toLowerCase(Locale.ROOT);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE LOWER(username) = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            String salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));
            String hash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
            if (PasswordUtils.verifyPassword(password, salt, hash)) {
                User user = new User(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        hash,
                        salt,
                        cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                cursor.close();
                return user;
            }
        }
        cursor.close();
        return null;
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                    cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                    cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status"))
            );
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    public long registerStudent(String username, String password, String fullName) {
        SQLiteDatabase db = getWritableDatabase();
        username = username.trim().toLowerCase(Locale.ROOT);
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(password, salt);

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password_hash", hash);
        cv.put("salt", salt);
        cv.put("full_name", fullName);
        cv.put("role", "STUDENT");
        cv.put("status", "ACTIVE");

        return db.insert(TABLE_USERS, null, cv);
    }

    // PERSONAL SUBJECTS
    public long insertPersonalSubject(PersonalSubject subject) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subject_code", subject.getSubjectCode());
        cv.put("subject_name", subject.getSubjectName());
        cv.put("student_id", subject.getStudentId());
        return db.insert(TABLE_PERSONAL_SUBJECTS, null, cv);
    }

    public List<PersonalSubject> getPersonalSubjectsForStudent(long studentId) {
        List<PersonalSubject> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PERSONAL_SUBJECTS + " WHERE student_id = ? ORDER BY subject_name ASC",
                new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new PersonalSubject(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("subject_code")),
                        cursor.getString(cursor.getColumnIndexOrThrow("subject_name")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("student_id"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // CLASSES FOR STUDENT
    public List<ClassModel> getClassesForStudent(long studentId) {
        List<ClassModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT c.* FROM " + TABLE_CLASSES + " c " +
                "INNER JOIN " + TABLE_CLASS_MEMBERS + " cm ON c.id = cm.class_id " +
                "WHERE cm.student_id = ? AND c.is_archived = 0 " +
                "ORDER BY c.class_name ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new ClassModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("class_code")),
                        cursor.getString(cursor.getColumnIndexOrThrow("class_name")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("teacher_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_archived")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // TASKS & PROGRESS QUERY FOR STUDENT
    public List<TaskWithProgress> getTasksForStudent(
            long studentId,
            String searchQuery,
            String sourceFilter,
            String statusFilter,
            String sortOption) {

        List<TaskWithProgress> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.*, ");
        sql.append("p.id AS prog_id, p.status AS prog_status, p.priority AS prog_priority, p.reminder_time AS prog_reminder, ");
        sql.append("c.class_code, c.class_name, ");
        sql.append("ps.subject_code, ps.subject_name ");
        sql.append("FROM ").append(TABLE_TASKS).append(" t ");
        sql.append("LEFT JOIN ").append(TABLE_TASK_PROGRESS).append(" p ON t.id = p.task_id AND p.student_id = ? ");
        sql.append("LEFT JOIN ").append(TABLE_CLASSES).append(" c ON t.class_id = c.id ");
        sql.append("LEFT JOIN ").append(TABLE_PERSONAL_SUBJECTS).append(" ps ON t.personal_subject_id = ps.id ");
        sql.append("WHERE (");
        // Personal tasks owned by this student OR Teacher tasks in classes student belongs to
        sql.append("(t.source_type = '").append(Task.SOURCE_PERSONAL).append("' AND t.created_by_user_id = ?) ");
        sql.append("OR (t.source_type = '").append(Task.SOURCE_TEACHER).append("' AND t.class_id IN (SELECT class_id FROM ").append(TABLE_CLASS_MEMBERS).append(" WHERE student_id = ?))");
        sql.append(") ");

        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(studentId));
        selectionArgs.add(String.valueOf(studentId));
        selectionArgs.add(String.valueOf(studentId));

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND LOWER(t.title) LIKE ? ");
            selectionArgs.add("%" + searchQuery.trim().toLowerCase(Locale.ROOT) + "%");
        }

        if (sourceFilter != null && !sourceFilter.isEmpty() && !"Tất cả nguồn".equalsIgnoreCase(sourceFilter)) {
            if ("Cá nhân".equalsIgnoreCase(sourceFilter) || Task.SOURCE_PERSONAL.equalsIgnoreCase(sourceFilter)) {
                sql.append("AND t.source_type = ? ");
                selectionArgs.add(Task.SOURCE_PERSONAL);
            } else if ("Giáo viên giao".equalsIgnoreCase(sourceFilter) || Task.SOURCE_TEACHER.equalsIgnoreCase(sourceFilter)) {
                sql.append("AND t.source_type = ? ");
                selectionArgs.add(Task.SOURCE_TEACHER);
            }
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !"Tất cả trạng thái".equalsIgnoreCase(statusFilter)) {
            if ("Chưa làm".equalsIgnoreCase(statusFilter)) {
                sql.append("AND (p.status IS NULL OR p.status = 'Chưa làm') ");
            } else if ("Đang làm".equalsIgnoreCase(statusFilter)) {
                sql.append("AND p.status = 'Đang làm' ");
            } else if ("Hoàn thành".equalsIgnoreCase(statusFilter)) {
                sql.append("AND p.status = 'Hoàn thành' ");
            } else if ("Quá hạn".equalsIgnoreCase(statusFilter)) {
                String currentDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                sql.append("AND t.due_date < '").append(currentDateStr).append("' ");
                sql.append("AND (p.status IS NULL OR p.status != 'Hoàn thành') ");
            }
        }

        if (sortOption != null) {
            if (sortOption.contains("Mức ưu tiên")) {
                sql.append("ORDER BY CASE ");
                sql.append("WHEN p.priority = 'Cao' THEN 1 ");
                sql.append("WHEN p.priority = 'Trung bình' THEN 2 ");
                sql.append("WHEN p.priority = 'Thấp' THEN 3 ");
                sql.append("ELSE 4 END ASC, t.due_date ASC ");
            } else if (sortOption.contains("Giảm dần")) {
                sql.append("ORDER BY t.due_date DESC ");
            } else {
                sql.append("ORDER BY t.due_date ASC ");
            }
        } else {
            sql.append("ORDER BY t.due_date ASC ");
        }

        Cursor cursor = db.rawQuery(sql.toString(), selectionArgs.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("due_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("source_type")),
                        cursor.isNull(cursor.getColumnIndexOrThrow("class_id")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("class_id")),
                        cursor.isNull(cursor.getColumnIndexOrThrow("personal_subject_id")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("personal_subject_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("created_by_user_id"))
                );

                long progId = cursor.isNull(cursor.getColumnIndexOrThrow("prog_id")) ? -1 : cursor.getLong(cursor.getColumnIndexOrThrow("prog_id"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("prog_status"));
                if (status == null) status = TaskProgress.STATUS_NOT_STARTED;

                String priority = cursor.getString(cursor.getColumnIndexOrThrow("prog_priority"));
                if (priority == null) priority = TaskProgress.PRIORITY_MEDIUM;

                String reminder = cursor.getString(cursor.getColumnIndexOrThrow("prog_reminder"));

                TaskProgress progress = new TaskProgress(progId, task.getId(), studentId, status, priority, reminder);

                String subjectOrClassName = "";
                if (task.isPersonal()) {
                    String subName = cursor.getString(cursor.getColumnIndexOrThrow("subject_name"));
                    String subCode = cursor.getString(cursor.getColumnIndexOrThrow("subject_code"));
                    subjectOrClassName = (subCode != null ? subCode + " - " : "") + (subName != null ? subName : "Môn cá nhân");
                } else {
                    String clsName = cursor.getString(cursor.getColumnIndexOrThrow("class_name"));
                    String clsCode = cursor.getString(cursor.getColumnIndexOrThrow("class_code"));
                    subjectOrClassName = (clsCode != null ? clsCode + " - " : "") + (clsName != null ? clsName : "Lớp học");
                }

                int completedChecklist = 0;
                int totalChecklist = 0;
                if (progId != -1) {
                    Cursor clCursor = db.rawQuery(
                            "SELECT COUNT(*) AS total, SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) AS done FROM " + TABLE_CHECKLIST + " WHERE progress_id = ?",
                            new String[]{String.valueOf(progId)});
                    if (clCursor.moveToFirst()) {
                        totalChecklist = clCursor.getInt(clCursor.getColumnIndexOrThrow("total"));
                        completedChecklist = clCursor.getInt(clCursor.getColumnIndexOrThrow("done"));
                    }
                    clCursor.close();
                }

                list.add(new TaskWithProgress(task, progress, subjectOrClassName, completedChecklist, totalChecklist));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public TaskWithProgress getTaskWithProgressById(long taskId, long studentId) {
        List<TaskWithProgress> tasks = getTasksForStudent(studentId, null, null, null, null);
        for (TaskWithProgress twp : tasks) {
            if (twp.getTask().getId() == taskId) {
                return twp;
            }
        }
        return null;
    }

    // CRUD FOR PERSONAL TASK
    public long insertPersonalTask(Task task, TaskProgress progress) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long taskId = -1;
        try {
            ContentValues cvTask = new ContentValues();
            cvTask.put("title", task.getTitle());
            cvTask.put("description", task.getDescription());
            cvTask.put("due_date", task.getDueDate());
            cvTask.put("source_type", Task.SOURCE_PERSONAL);
            cvTask.put("personal_subject_id", task.getPersonalSubjectId());
            cvTask.put("created_by_user_id", task.getCreatedByUserId());
            taskId = db.insert(TABLE_TASKS, null, cvTask);

            if (taskId != -1) {
                ContentValues cvProg = new ContentValues();
                cvProg.put("task_id", taskId);
                cvProg.put("student_id", progress.getStudentId());
                cvProg.put("status", progress.getStatus());
                cvProg.put("priority", progress.getPriority());
                cvProg.put("reminder_time", progress.getReminderTime());
                long progId = db.insert(TABLE_TASK_PROGRESS, null, cvProg);
                progress.setId(progId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return taskId;
    }

    public boolean updatePersonalTask(Task task, TaskProgress progress) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            // Verify task is personal and created by this student
            Cursor cursor = db.rawQuery("SELECT source_type, created_by_user_id FROM " + TABLE_TASKS + " WHERE id = ?",
                    new String[]{String.valueOf(task.getId())});
            if (cursor.moveToFirst()) {
                String sourceType = cursor.getString(cursor.getColumnIndexOrThrow("source_type"));
                long createdBy = cursor.getLong(cursor.getColumnIndexOrThrow("created_by_user_id"));
                cursor.close();

                if (!Task.SOURCE_PERSONAL.equalsIgnoreCase(sourceType) || createdBy != progress.getStudentId()) {
                    // Unauthorized to update requirement or title/due date for non-personal task
                    db.endTransaction();
                    return false;
                }

                ContentValues cvTask = new ContentValues();
                cvTask.put("title", task.getTitle());
                cvTask.put("description", task.getDescription());
                cvTask.put("due_date", task.getDueDate());
                cvTask.put("personal_subject_id", task.getPersonalSubjectId());
                db.update(TABLE_TASKS, cvTask, "id = ?", new String[]{String.valueOf(task.getId())});

                ContentValues cvProg = new ContentValues();
                cvProg.put("status", progress.getStatus());
                cvProg.put("priority", progress.getPriority());
                cvProg.put("reminder_time", progress.getReminderTime());
                int rows = db.update(TABLE_TASK_PROGRESS, cvProg, "task_id = ? AND student_id = ?",
                        new String[]{String.valueOf(task.getId()), String.valueOf(progress.getStudentId())});
                if (rows == 0) {
                    cvProg.put("task_id", task.getId());
                    cvProg.put("student_id", progress.getStudentId());
                    db.insert(TABLE_TASK_PROGRESS, null, cvProg);
                }
                success = true;
                db.setTransactionSuccessful();
            } else {
                cursor.close();
            }
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean updateTaskProgressOnly(long taskId, long studentId, String status, String priority) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cvProg = new ContentValues();
        cvProg.put("status", status);
        cvProg.put("priority", priority);

        int rows = db.update(TABLE_TASK_PROGRESS, cvProg, "task_id = ? AND student_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(studentId)});
        if (rows == 0) {
            cvProg.put("task_id", taskId);
            cvProg.put("student_id", studentId);
            long id = db.insert(TABLE_TASK_PROGRESS, null, cvProg);
            return id != -1;
        }
        return true;
    }

    public boolean deletePersonalTask(long taskId, long studentId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            Cursor cursor = db.rawQuery("SELECT source_type, created_by_user_id FROM " + TABLE_TASKS + " WHERE id = ?",
                    new String[]{String.valueOf(taskId)});
            if (cursor.moveToFirst()) {
                String sourceType = cursor.getString(cursor.getColumnIndexOrThrow("source_type"));
                long createdBy = cursor.getLong(cursor.getColumnIndexOrThrow("created_by_user_id"));
                cursor.close();

                if (!Task.SOURCE_PERSONAL.equalsIgnoreCase(sourceType) || createdBy != studentId) {
                    db.endTransaction();
                    return false; // Cannot delete teacher task or another student's task!
                }

                // Delete associated checklist & progress records
                Cursor progCursor = db.rawQuery("SELECT id FROM " + TABLE_TASK_PROGRESS + " WHERE task_id = ?",
                        new String[]{String.valueOf(taskId)});
                if (progCursor.moveToFirst()) {
                    do {
                        long pId = progCursor.getLong(0);
                        db.delete(TABLE_CHECKLIST, "progress_id = ?", new String[]{String.valueOf(pId)});
                    } while (progCursor.moveToNext());
                }
                progCursor.close();

                db.delete(TABLE_TASK_PROGRESS, "task_id = ?", new String[]{String.valueOf(taskId)});
                db.delete(TABLE_TASKS, "id = ?", new String[]{String.valueOf(taskId)});
                db.setTransactionSuccessful();
                success = true;
            } else {
                cursor.close();
            }
        } finally {
            db.endTransaction();
        }
        return success;
    }

    // CHECKLIST OPERATIONS
    public long ensureProgressRecordExists(long taskId, long studentId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_TASK_PROGRESS + " WHERE task_id = ? AND student_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            long pId = cursor.getLong(0);
            cursor.close();
            return pId;
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("task_id", taskId);
        cv.put("student_id", studentId);
        cv.put("status", TaskProgress.STATUS_NOT_STARTED);
        cv.put("priority", TaskProgress.PRIORITY_MEDIUM);
        return db.insert(TABLE_TASK_PROGRESS, null, cv);
    }

    public List<ChecklistItem> getChecklistItems(long progressId) {
        List<ChecklistItem> list = new ArrayList<>();
        if (progressId <= 0) return list;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CHECKLIST + " WHERE progress_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(progressId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new ChecklistItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("progress_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public long addChecklistItem(long progressId, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("progress_id", progressId);
        cv.put("content", content);
        cv.put("is_completed", 0);
        return db.insert(TABLE_CHECKLIST, null, cv);
    }

    public boolean toggleChecklistItem(long checklistItemId, boolean isCompleted) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_completed", isCompleted ? 1 : 0);
        int rows = db.update(TABLE_CHECKLIST, cv, "id = ?", new String[]{String.valueOf(checklistItemId)});
        return rows > 0;
    }

    public boolean deleteChecklistItem(long checklistItemId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_CHECKLIST, "id = ?", new String[]{String.valueOf(checklistItemId)});
        return rows > 0;
    }
}
