package com.example.quanlybaitapsinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Class quản trị cơ sở dữ liệu SQLite cho toàn bộ ứng dụng Quản lý Bài tập Sinh viên
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TEN_DATABASE = "QuanLyHocTap.db";
    private static final int PHIEN_BAN = 2; // Nâng phiên bản để cập nhật cấu trúc bảng mới

    // Bảng 1: tai_khoan
    public static final String BANG_TAI_KHOAN = "tai_khoan";
    public static final String COT_TK_ID = "id";
    public static final String COT_TK_USERNAME = "username";
    public static final String COT_TK_EMAIL = "email";
    public static final String COT_TK_MAT_KHAU = "mat_khau";
    public static final String COT_TK_HO_TEN = "ho_ten";
    public static final String COT_TK_VAI_TRO = "vai_tro";
    public static final String COT_TK_TRANG_THAI = "trang_thai";

    // Bảng 2: lop_hoc
    public static final String BANG_LOP_HOC = "lop_hoc";
    public static final String COT_LH_ID = "id";
    public static final String COT_LH_MA_LOP = "ma_lop";
    public static final String COT_LH_TEN_LOP = "ten_lop";
    public static final String COT_LH_MA_GV = "ma_gv";
    public static final String COT_LH_MO_TA = "mo_ta";
    public static final String COT_LH_TRANG_THAI_LUU_TRU = "trang_thai_luu_tru";

    // Bảng 3: thanh_vien_lop
    public static final String BANG_THANH_VIEN_LOP = "thanh_vien_lop";
    public static final String COT_TV_ID = "id";
    public static final String COT_TV_MA_LOP_ID = "ma_lop_id";
    public static final String COT_TV_SINH_VIEN_ID = "sinh_vien_id";

    // Bảng 4: mon_hoc_ca_nhan
    public static final String BANG_MON_HOC_CA_NHAN = "mon_hoc_ca_nhan";
    public static final String COT_MH_ID = "id";
    public static final String COT_MH_SINH_VIEN_ID = "sinh_vien_id";
    public static final String COT_MH_TEN_MON = "ten_mon";
    public static final String COT_MH_MA_MON = "ma_mon";
    public static final String COT_MH_MO_TA = "mo_ta";

    // Bảng 5: bai_tap
    public static final String BANG_BAI_TAP = "bai_tap";
    public static final String COT_BT_ID = "id";
    public static final String COT_BT_MA_LOP_ID = "ma_lop_id";
    public static final String COT_BT_MON_HOC_CA_NHAN_ID = "mon_hoc_ca_nhan_id";
    public static final String COT_BT_NGUOI_TAO_ID = "nguoi_tao_id";
    public static final String COT_BT_TEN_BAI_TAP = "ten_bai_tap";
    public static final String COT_BT_MO_TA = "mo_ta";
    public static final String COT_BT_HAN_NOP = "han_nop";
    public static final String COT_BT_NGAY_TAO = "ngay_tao";
    public static final String COT_BT_NGUON_BAI = "nguon_bai";

    // Bảng 6: tien_do_sinh_vien
    public static final String BANG_TIEN_DO = "tien_do_sinh_vien";
    public static final String COT_TD_ID = "id";
    public static final String COT_TD_BAI_TAP_ID = "bai_tap_id";
    public static final String COT_TD_SINH_VIEN_ID = "sinh_vien_id";
    public static final String COT_TD_TRANG_THAI = "trang_thai";
    public static final String COT_TD_MUC_UU_TIEN = "muc_uu_tien";
    public static final String COT_TD_PERCENT = "percent_tien_do";
    public static final String COT_TD_NHAC_HAN = "nhac_han";

    // Bảng 7: checklist_item
    public static final String BANG_CHECKLIST = "checklist_item";
    public static final String COT_CL_ID = "id";
    public static final String COT_CL_TIEN_DO_ID = "tien_do_id";
    public static final String COT_CL_NOI_DUNG = "noi_dung";
    public static final String COT_CL_DA_HOAN_THANH = "da_hoan_thanh";

    public DatabaseHelper(Context context) {
        super(context, TEN_DATABASE, null, PHIEN_BAN);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        taoCacBang(db);
        chenDuLieuMauMacDinh(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BANG_CHECKLIST);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_TIEN_DO);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_BAI_TAP);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_MON_HOC_CA_NHAN);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_THANH_VIEN_LOP);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_LOP_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_TAI_KHOAN);
        onCreate(db);
    }

    /**
     * Tạo 7 bảng dữ liệu theo thiết kế
     */
    private void taoCacBang(SQLiteDatabase db) {
        // Bảng tài khoản
        db.execSQL("CREATE TABLE " + BANG_TAI_KHOAN + " (" +
                COT_TK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_TK_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COT_TK_EMAIL + " TEXT, " +
                COT_TK_MAT_KHAU + " TEXT NOT NULL, " +
                COT_TK_HO_TEN + " TEXT NOT NULL, " +
                COT_TK_VAI_TRO + " TEXT NOT NULL, " +
                COT_TK_TRANG_THAI + " INTEGER DEFAULT 1)");

        // Bảng lớp học
        db.execSQL("CREATE TABLE " + BANG_LOP_HOC + " (" +
                COT_LH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_LH_MA_LOP + " TEXT UNIQUE NOT NULL, " +
                COT_LH_TEN_LOP + " TEXT NOT NULL, " +
                COT_LH_MA_GV + " INTEGER NOT NULL, " +
                COT_LH_MO_TA + " TEXT, " +
                COT_LH_TRANG_THAI_LUU_TRU + " INTEGER DEFAULT 0)");

        // Bảng thành viên lớp
        db.execSQL("CREATE TABLE " + BANG_THANH_VIEN_LOP + " (" +
                COT_TV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_TV_MA_LOP_ID + " INTEGER NOT NULL, " +
                COT_TV_SINH_VIEN_ID + " INTEGER NOT NULL, " +
                "UNIQUE(" + COT_TV_MA_LOP_ID + ", " + COT_TV_SINH_VIEN_ID + "))");

        // Bảng môn học cá nhân
        db.execSQL("CREATE TABLE " + BANG_MON_HOC_CA_NHAN + " (" +
                COT_MH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_MH_SINH_VIEN_ID + " INTEGER NOT NULL, " +
                COT_MH_TEN_MON + " TEXT NOT NULL, " +
                COT_MH_MA_MON + " TEXT, " +
                COT_MH_MO_TA + " TEXT)");

        // Bảng bài tập
        db.execSQL("CREATE TABLE " + BANG_BAI_TAP + " (" +
                COT_BT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_BT_MA_LOP_ID + " INTEGER, " +
                COT_BT_MON_HOC_CA_NHAN_ID + " INTEGER, " +
                COT_BT_NGUOI_TAO_ID + " INTEGER NOT NULL, " +
                COT_BT_TEN_BAI_TAP + " TEXT NOT NULL, " +
                COT_BT_MO_TA + " TEXT, " +
                COT_BT_HAN_NOP + " TEXT NOT NULL, " +
                COT_BT_NGAY_TAO + " TEXT NOT NULL, " +
                COT_BT_NGUON_BAI + " TEXT NOT NULL)");

        // Bảng tiến độ sinh viên
        db.execSQL("CREATE TABLE " + BANG_TIEN_DO + " (" +
                COT_TD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_TD_BAI_TAP_ID + " INTEGER NOT NULL, " +
                COT_TD_SINH_VIEN_ID + " INTEGER NOT NULL, " +
                COT_TD_TRANG_THAI + " TEXT DEFAULT 'Chưa làm', " +
                COT_TD_MUC_UU_TIEN + " TEXT DEFAULT 'Bình thường', " +
                COT_TD_PERCENT + " INTEGER DEFAULT 0, " +
                COT_TD_NHAC_HAN + " INTEGER DEFAULT 1, " +
                "UNIQUE(" + COT_TD_BAI_TAP_ID + ", " + COT_TD_SINH_VIEN_ID + "))");

        // Bảng checklist item
        db.execSQL("CREATE TABLE " + BANG_CHECKLIST + " (" +
                COT_CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_CL_TIEN_DO_ID + " INTEGER NOT NULL, " +
                COT_CL_NOI_DUNG + " TEXT NOT NULL, " +
                COT_CL_DA_HOAN_THANH + " INTEGER DEFAULT 0)");
    }

    /**
     * Chèn các dữ liệu mẫu mặc định theo yêu cầu thiết kế
     */
    private void chenDuLieuMauMacDinh(SQLiteDatabase db) {
        // 1. Tài khoản Admin
        ContentValues cvAdmin = new ContentValues();
        cvAdmin.put(COT_TK_USERNAME, "admin");
        cvAdmin.put(COT_TK_EMAIL, "admin@hnue.edu.vn");
        cvAdmin.put(COT_TK_MAT_KHAU, "admin123");
        cvAdmin.put(COT_TK_HO_TEN, "Quản trị viên Hệ thống");
        cvAdmin.put(COT_TK_VAI_TRO, "ADMIN");
        cvAdmin.put(COT_TK_TRANG_THAI, 1);
        db.insert(BANG_TAI_KHOAN, null, cvAdmin);

        // 2. Giáo viên
        ContentValues cvGv = new ContentValues();
        cvGv.put(COT_TK_USERNAME, "gv_nguyen");
        cvGv.put(COT_TK_EMAIL, "gv_nguyen@hnue.edu.vn");
        cvGv.put(COT_TK_MAT_KHAU, "123456");
        cvGv.put(COT_TK_HO_TEN, "ThS. Nguyễn Văn Thầy");
        cvGv.put(COT_TK_VAI_TRO, "TEACHER");
        cvGv.put(COT_TK_TRANG_THAI, 1);
        long gvId = db.insert(BANG_TAI_KHOAN, null, cvGv);

        // 3. Sinh viên A
        ContentValues cvSvA = new ContentValues();
        cvSvA.put(COT_TK_USERNAME, "sv_a");
        cvSvA.put(COT_TK_EMAIL, "sv_a@hnue.edu.vn");
        cvSvA.put(COT_TK_MAT_KHAU, "123456");
        cvSvA.put(COT_TK_HO_TEN, "Nguyễn Văn A");
        cvSvA.put(COT_TK_VAI_TRO, "STUDENT");
        cvSvA.put(COT_TK_TRANG_THAI, 1);
        long svAId = db.insert(BANG_TAI_KHOAN, null, cvSvA);

        // 4. Sinh viên B
        ContentValues cvSvB = new ContentValues();
        cvSvB.put(COT_TK_USERNAME, "sv_b");
        cvSvB.put(COT_TK_EMAIL, "sv_b@hnue.edu.vn");
        cvSvB.put(COT_TK_MAT_KHAU, "123456");
        cvSvB.put(COT_TK_HO_TEN, "Trần Thị B");
        cvSvB.put(COT_TK_VAI_TRO, "STUDENT");
        cvSvB.put(COT_TK_TRANG_THAI, 1);
        long svBId = db.insert(BANG_TAI_KHOAN, null, cvSvB);

        // 5. Lớp học MOB01
        ContentValues cvLop = new ContentValues();
        cvLop.put(COT_LH_MA_LOP, "MOB01");
        cvLop.put(COT_LH_TEN_LOP, "Lập trình Android MOB01");
        cvLop.put(COT_LH_MA_GV, gvId);
        cvLop.put(COT_LH_MO_TA, "Lớp học phần Lập trình Android ứng dụng");
        cvLop.put(COT_LH_TRANG_THAI_LUU_TRU, 0);
        long lopId = db.insert(BANG_LOP_HOC, null, cvLop);

        // Gán thành viên cho lớp MOB01
        ContentValues cvTvA = new ContentValues();
        cvTvA.put(COT_TV_MA_LOP_ID, lopId);
        cvTvA.put(COT_TV_SINH_VIEN_ID, svAId);
        db.insert(BANG_THANH_VIEN_LOP, null, cvTvA);

        ContentValues cvTvB = new ContentValues();
        cvTvB.put(COT_TV_MA_LOP_ID, lopId);
        cvTvB.put(COT_TV_SINH_VIEN_ID, svBId);
        db.insert(BANG_THANH_VIEN_LOP, null, cvTvB);

        // 6. Môn học cá nhân của Sinh viên A
        ContentValues cvMhA = new ContentValues();
        cvMhA.put(COT_MH_SINH_VIEN_ID, svAId);
        cvMhA.put(COT_MH_TEN_MON, "Cơ sở dữ liệu");
        cvMhA.put(COT_MH_MA_MON, "CSDL01");
        cvMhA.put(COT_MH_MO_TA, "Môn học quản trị cơ sở dữ liệu");
        long monAId = db.insert(BANG_MON_HOC_CA_NHAN, null, cvMhA);

        // 7. Bài tập mẫu do Giáo viên giao
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String hanNopGv = sdf.format(cal.getTime());
        String ngayTaoNow = sdf.format(new Date());

        ContentValues cvBtGv = new ContentValues();
        cvBtGv.put(COT_BT_MA_LOP_ID, lopId);
        cvBtGv.put(COT_BT_NGUOI_TAO_ID, gvId);
        cvBtGv.put(COT_BT_TEN_BAI_TAP, "Nộp đồ án Android Cuối kỳ");
        cvBtGv.put(COT_BT_MO_TA, "Hoàn thiện mã nguồn ứng dụng Android và nộp file APK đính kèm.");
        cvBtGv.put(COT_BT_HAN_NOP, hanNopGv);
        cvBtGv.put(COT_BT_NGAY_TAO, ngayTaoNow);
        cvBtGv.put(COT_BT_NGUON_BAI, "TEACHER");
        long btGvId = db.insert(BANG_BAI_TAP, null, cvBtGv);

        // Tạo tiến độ cho Sinh viên A (33% progress)
        ContentValues cvTdA = new ContentValues();
        cvTdA.put(COT_TD_BAI_TAP_ID, btGvId);
        cvTdA.put(COT_TD_SINH_VIEN_ID, svAId);
        cvTdA.put(COT_TD_TRANG_THAI, "Đang làm");
        cvTdA.put(COT_TD_MUC_UU_TIEN, "Cao");
        cvTdA.put(COT_TD_PERCENT, 33);
        cvTdA.put(COT_TD_NHAC_HAN, 1);
        long tdAId = db.insert(BANG_TIEN_DO, null, cvTdA);

        // Checklist cho SV A
        chenChecklist(db, tdAId, "Thiết kế CSDL SQLite", 1);
        chenChecklist(db, tdAId, "Viết giao diện XML", 0);
        chenChecklist(db, tdAId, "Kiểm thử và hoàn thiện", 0);

        // Tạo tiến độ cho Sinh viên B (Chưa làm)
        ContentValues cvTdB = new ContentValues();
        cvTdB.put(COT_TD_BAI_TAP_ID, btGvId);
        cvTdB.put(COT_TD_SINH_VIEN_ID, svBId);
        cvTdB.put(COT_TD_TRANG_THAI, "Chưa làm");
        cvTdB.put(COT_TD_MUC_UU_TIEN, "Bình thường");
        cvTdB.put(COT_TD_PERCENT, 0);
        cvTdB.put(COT_TD_NHAC_HAN, 1);
        db.insert(BANG_TIEN_DO, null, cvTdB);

        // 8. Bài tập cá nhân của Sinh viên A
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 5);
        String hanNopCn = sdf.format(cal2.getTime());

        ContentValues cvBtCn = new ContentValues();
        cvBtCn.put(COT_BT_MON_HOC_CA_NHAN_ID, monAId);
        cvBtCn.put(COT_BT_NGUOI_TAO_ID, svAId);
        cvBtCn.put(COT_BT_TEN_BAI_TAP, "Làm bài tập lớn CSDL");
        cvBtCn.put(COT_BT_MO_TA, "Viết câu lệnh SQL và vẽ sơ đồ ERD cho bài tập lớn.");
        cvBtCn.put(COT_BT_HAN_NOP, hanNopCn);
        cvBtCn.put(COT_BT_NGAY_TAO, ngayTaoNow);
        cvBtCn.put(COT_BT_NGUON_BAI, "PERSONAL");
        long btCnId = db.insert(BANG_BAI_TAP, null, cvBtCn);

        ContentValues cvTdCnA = new ContentValues();
        cvTdCnA.put(COT_TD_BAI_TAP_ID, btCnId);
        cvTdCnA.put(COT_TD_SINH_VIEN_ID, svAId);
        cvTdCnA.put(COT_TD_TRANG_THAI, "Đang làm");
        cvTdCnA.put(COT_TD_MUC_UU_TIEN, "Bình thường");
        cvTdCnA.put(COT_TD_PERCENT, 50);
        cvTdCnA.put(COT_TD_NHAC_HAN, 1);
        long tdCnAId = db.insert(BANG_TIEN_DO, null, cvTdCnA);

        chenChecklist(db, tdCnAId, "Thu thập tài liệu sơ đồ ERD", 1);
        chenChecklist(db, tdCnAId, "Viết báo cáo bài tập lớn Word", 0);
    }

    private void chenChecklist(SQLiteDatabase db, long tienDoId, String noiDung, int daHoanThanh) {
        ContentValues cv = new ContentValues();
        cv.put(COT_CL_TIEN_DO_ID, tienDoId);
        cv.put(COT_CL_NOI_DUNG, noiDung);
        cv.put(COT_CL_DA_HOAN_THANH, daHoanThanh);
        db.insert(BANG_CHECKLIST, null, cv);
    }

    // ==========================================
    // CÁC HÀM XÁC THỰC VÀ QUẢN LÝ TÀI KHOẢN
    // ==========================================

    /**
     * Kiểm tra xem địa chỉ Email / Username đã tồn tại trong hệ thống chưa
     */
    public boolean kiemTraEmailTonTai(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_TK_EMAIL + ") = ? OR LOWER(" + COT_TK_USERNAME + ") = ?",
                new String[]{email.trim().toLowerCase(), email.trim().toLowerCase()});
        boolean tonTai = cursor.getCount() > 0;
        cursor.close();
        return tonTai;
    }

    /**
     * Đăng ký tài khoản Sinh viên mới (mặc định vai trò STUDENT)
     */
    public boolean dangKyTaiKhoan(String email, String matKhau) {
        String username = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        TaiKhoan tk = new TaiKhoan(0, username, email, matKhau, "Sinh viên " + username, "STUDENT", 1);
        return themTaiKhoan(tk);
    }

    /**
     * Kiểm tra đăng nhập theo Username/Email và Mật khẩu
     */
    public TaiKhoan kiemTraDangNhap(String accountInput, String matKhau) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + BANG_TAI_KHOAN +
                " WHERE (LOWER(" + COT_TK_USERNAME + ") = ? OR LOWER(" + COT_TK_EMAIL + ") = ?) AND " + COT_TK_MAT_KHAU + " = ?";
        String inputLower = accountInput.trim().toLowerCase();
        Cursor cursor = db.rawQuery(sql, new String[]{inputLower, inputLower, matKhau.trim()});

        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            taiKhoan = parseTaiKhoan(cursor);
        }
        cursor.close();
        return taiKhoan;
    }

    /**
     * Lấy tài khoản theo ID
     */
    public TaiKhoan layTaiKhoanTheoId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE " + COT_TK_ID + " = ?",
                new String[]{String.valueOf(userId)});
        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            taiKhoan = parseTaiKhoan(cursor);
        }
        cursor.close();
        return taiKhoan;
    }

    /**
     * Lấy tài khoản theo Username
     */
    public TaiKhoan layTaiKhoanTheoUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_TK_USERNAME + ") = ?",
                new String[]{username.trim().toLowerCase()});
        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            taiKhoan = parseTaiKhoan(cursor);
        }
        cursor.close();
        return taiKhoan;
    }

    /**
     * Lấy danh sách toàn bộ tài khoản trong hệ thống
     */
    public List<TaiKhoan> layTatCaTaiKhoan() {
        List<TaiKhoan> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " ORDER BY " + COT_TK_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                ds.add(parseTaiKhoan(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Tạo tài khoản mới (Admin tạo Giáo viên hoặc Sinh viên đăng ký)
     */
    public boolean themTaiKhoan(TaiKhoan tk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TK_USERNAME, tk.getUsername().trim().toLowerCase());
        cv.put(COT_TK_EMAIL, tk.getEmail() != null ? tk.getEmail().trim().toLowerCase() : tk.getUsername() + "@hnue.edu.vn");
        cv.put(COT_TK_MAT_KHAU, tk.getMatKhau());
        cv.put(COT_TK_HO_TEN, tk.getHoTen());
        cv.put(COT_TK_VAI_TRO, tk.getVaiTro());
        cv.put(COT_TK_TRANG_THAI, tk.getTrangThai());
        long res = db.insert(BANG_TAI_KHOAN, null, cv);
        return res != -1;
    }

    /**
     * Cập nhật trạng thái Khoá / Mở khoá tài khoản
     */
    public boolean capNhatTrangThaiTaiKhoan(int id, int trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TK_TRANG_THAI, trangThai);
        int rows = db.update(BANG_TAI_KHOAN, cv, COT_TK_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    private TaiKhoan parseTaiKhoan(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TK_ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(COT_TK_USERNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(COT_TK_EMAIL));
        String matKhau = cursor.getString(cursor.getColumnIndexOrThrow(COT_TK_MAT_KHAU));
        String hoTen = cursor.getString(cursor.getColumnIndexOrThrow(COT_TK_HO_TEN));
        String vaiTro = cursor.getString(cursor.getColumnIndexOrThrow(COT_TK_VAI_TRO));
        int trangThai = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TK_TRANG_THAI));
        return new TaiKhoan(id, username, email, matKhau, hoTen, vaiTro, trangThai);
    }

    // ==========================================
    // CÁC HÀM QUẢN LÝ LỚP HỌC & THÀNH VIÊN
    // ==========================================

    /**
     * Lấy tất cả lớp học trong hệ thống
     */
    public List<LopHoc> layDanhSachLopHoc() {
        List<LopHoc> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT l.*, t." + COT_TK_HO_TEN + " AS ten_gv FROM " + BANG_LOP_HOC + " l " +
                "LEFT JOIN " + BANG_TAI_KHOAN + " t ON l." + COT_LH_MA_GV + " = t." + COT_TK_ID +
                " ORDER BY l." + COT_LH_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                LopHoc lh = parseLopHoc(cursor);
                lh.setTenGiaoVien(cursor.getString(cursor.getColumnIndexOrThrow("ten_gv")));
                ds.add(lh);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy danh sách lớp học do Giáo viên phụ trách
     */
    public List<LopHoc> layDanhSachLopHocTheoGv(int maGv) {
        List<LopHoc> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT l.*, t." + COT_TK_HO_TEN + " AS ten_gv FROM " + BANG_LOP_HOC + " l " +
                "LEFT JOIN " + BANG_TAI_KHOAN + " t ON l." + COT_LH_MA_GV + " = t." + COT_TK_ID +
                " WHERE l." + COT_LH_MA_GV + " = ? AND l." + COT_LH_TRANG_THAI_LUU_TRU + " = 0" +
                " ORDER BY l." + COT_LH_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maGv)});
        if (cursor.moveToFirst()) {
            do {
                LopHoc lh = parseLopHoc(cursor);
                lh.setTenGiaoVien(cursor.getString(cursor.getColumnIndexOrThrow("ten_gv")));
                ds.add(lh);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy danh sách các lớp học sinh viên tham gia
     */
    public List<LopHoc> layDanhSachLopCuaSinhVien(int sinhVienId) {
        List<LopHoc> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT l.* FROM " + BANG_LOP_HOC + " l " +
                "INNER JOIN " + BANG_THANH_VIEN_LOP + " tv ON l." + COT_LH_ID + " = tv." + COT_TV_MA_LOP_ID +
                " WHERE tv." + COT_TV_SINH_VIEN_ID + " = ? AND l." + COT_LH_TRANG_THAI_LUU_TRU + " = 0";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(sinhVienId)});
        if (cursor.moveToFirst()) {
            do {
                ds.add(parseLopHoc(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Thêm lớp học mới
     */
    public boolean themLopHoc(LopHoc lh) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_LH_MA_LOP, lh.getMaLop().trim().toUpperCase());
        cv.put(COT_LH_TEN_LOP, lh.getTenLop().trim());
        cv.put(COT_LH_MA_GV, lh.getMaGv());
        cv.put(COT_LH_MO_TA, lh.getMoTa());
        cv.put(COT_LH_TRANG_THAI_LUU_TRU, 0);
        long res = db.insert(BANG_LOP_HOC, null, cv);
        return res != -1;
    }

    /**
     * Đổi Giáo viên phụ trách lớp học
     */
    public boolean capNhatGiaoVienLopHoc(int maLopId, int maGvMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_LH_MA_GV, maGvMoi);
        int rows = db.update(BANG_LOP_HOC, cv, COT_LH_ID + " = ?", new String[]{String.valueOf(maLopId)});
        return rows > 0;
    }

    /**
     * Kiểm tra Mã lớp đã tồn tại chưa
     */
    public boolean kiemTraMaLopTonTai(String maLop) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + BANG_LOP_HOC + " WHERE UPPER(" + COT_LH_MA_LOP + ") = ?",
                new String[]{maLop.trim().toUpperCase()});
        boolean tonTai = cursor.getCount() > 0;
        cursor.close();
        return tonTai;
    }

    /**
     * Thêm sinh viên vào lớp học
     */
    public boolean themThanhVienLop(int maLopId, int sinhVienId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TV_MA_LOP_ID, maLopId);
        cv.put(COT_TV_SINH_VIEN_ID, sinhVienId);
        long res = db.insertWithOnConflict(BANG_THANH_VIEN_LOP, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return res != -1;
    }

    /**
     * Bớt sinh viên khỏi lớp học
     */
    public boolean xoaThanhVienLop(int maLopId, int sinhVienId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(BANG_THANH_VIEN_LOP, COT_TV_MA_LOP_ID + " = ? AND " + COT_TV_SINH_VIEN_ID + " = ?",
                new String[]{String.valueOf(maLopId), String.valueOf(sinhVienId)});
        return rows > 0;
    }

    /**
     * Lấy danh sách sinh viên đã thuộc lớp
     */
    public List<TaiKhoan> layDanhSachSinhVienTrongLop(int maLopId) {
        List<TaiKhoan> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t.* FROM " + BANG_TAI_KHOAN + " t " +
                "INNER JOIN " + BANG_THANH_VIEN_LOP + " tv ON t." + COT_TK_ID + " = tv." + COT_TV_SINH_VIEN_ID +
                " WHERE tv." + COT_TV_MA_LOP_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maLopId)});
        if (cursor.moveToFirst()) {
            do {
                ds.add(parseTaiKhoan(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy danh sách sinh viên chưa thuộc lớp để thêm vào
     */
    public List<TaiKhoan> layDanhSachSinhVienChuaTrongLop(int maLopId) {
        List<TaiKhoan> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t.* FROM " + BANG_TAI_KHOAN + " t " +
                "WHERE t." + COT_TK_VAI_TRO + " = 'STUDENT' AND t." + COT_TK_ID + " NOT IN (" +
                "SELECT " + COT_TV_SINH_VIEN_ID + " FROM " + BANG_THANH_VIEN_LOP + " WHERE " + COT_TV_MA_LOP_ID + " = ?)";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maLopId)});
        if (cursor.moveToFirst()) {
            do {
                ds.add(parseTaiKhoan(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    private LopHoc parseLopHoc(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_LH_ID));
        String maLop = cursor.getString(cursor.getColumnIndexOrThrow(COT_LH_MA_LOP));
        String tenLop = cursor.getString(cursor.getColumnIndexOrThrow(COT_LH_TEN_LOP));
        int maGv = cursor.getInt(cursor.getColumnIndexOrThrow(COT_LH_MA_GV));
        String moTa = cursor.getString(cursor.getColumnIndexOrThrow(COT_LH_MO_TA));
        int trangThaiLuuTru = cursor.getInt(cursor.getColumnIndexOrThrow(COT_LH_TRANG_THAI_LUU_TRU));
        return new LopHoc(id, maLop, tenLop, maGv, moTa, trangThaiLuuTru);
    }

    // ==========================================
    // CÁC HÀM QUẢN LÝ MÔN HỌC CÁ NHÂN
    // ==========================================

    public List<MonHocCaNhan> layDanhSachMonHocCaNhan(int sinhVienId) {
        List<MonHocCaNhan> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_MON_HOC_CA_NHAN + " WHERE " + COT_MH_SINH_VIEN_ID + " = ?",
                new String[]{String.valueOf(sinhVienId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_MH_ID));
                String tenMon = cursor.getString(cursor.getColumnIndexOrThrow(COT_MH_TEN_MON));
                String maMon = cursor.getString(cursor.getColumnIndexOrThrow(COT_MH_MA_MON));
                String moTa = cursor.getString(cursor.getColumnIndexOrThrow(COT_MH_MO_TA));
                ds.add(new MonHocCaNhan(id, sinhVienId, tenMon, maMon, moTa));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    public long themMonHocCaNhan(MonHocCaNhan mon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_MH_SINH_VIEN_ID, mon.getSinhVienId());
        cv.put(COT_MH_TEN_MON, mon.getTenMon());
        cv.put(COT_MH_MA_MON, mon.getMaMon());
        cv.put(COT_MH_MO_TA, mon.getMoTa());
        return db.insert(BANG_MON_HOC_CA_NHAN, null, cv);
    }

    // ==========================================
    // CÁC HÀM QUẢN LÝ BÀI TẬP VÀ TIẾN ĐỘ SINH VIÊN
    // ==========================================

    /**
     * Lấy danh sách bài tập của sinh viên (gồm bài lớp + bài cá nhân), kèm bộ lọc & tìm kiếm
     */
    public List<BaiTap> layDanhSachBaiTapCuaSinhVien(int sinhVienId, String keyword, String filterLopMon, String filterTrangThai, String sortBy) {
        List<BaiTap> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder(
                "SELECT b.*, " +
                        "COALESCE(l." + COT_LH_TEN_LOP + ", m." + COT_MH_TEN_MON + ") AS ten_nguon, " +
                        "COALESCE(td." + COT_TD_PERCENT + ", 0) AS percent_tien_do, " +
                        "COALESCE(td." + COT_TD_TRANG_THAI + ", 'Chưa làm') AS trang_thai_sv, " +
                        "COALESCE(td." + COT_TD_MUC_UU_TIEN + ", 'Bình thường') AS muc_uu_tien, " +
                        "COALESCE(td." + COT_TD_NHAC_HAN + ", 1) AS nhac_han " +
                        "FROM " + BANG_BAI_TAP + " b " +
                        "LEFT JOIN " + BANG_LOP_HOC + " l ON b." + COT_BT_MA_LOP_ID + " = l." + COT_LH_ID + " " +
                        "LEFT JOIN " + BANG_MON_HOC_CA_NHAN + " m ON b." + COT_BT_MON_HOC_CA_NHAN_ID + " = m." + COT_MH_ID + " " +
                        "LEFT JOIN " + BANG_TIEN_DO + " td ON b." + COT_BT_ID + " = td." + COT_TD_BAI_TAP_ID + " AND td." + COT_TD_SINH_VIEN_ID + " = ? " +
                        "WHERE (b." + COT_BT_MA_LOP_ID + " IN (SELECT " + COT_TV_MA_LOP_ID + " FROM " + BANG_THANH_VIEN_LOP + " WHERE " + COT_TV_SINH_VIEN_ID + " = ?) " +
                        "OR m." + COT_MH_SINH_VIEN_ID + " = ?) "
        );

        List<String> params = new ArrayList<>();
        params.add(String.valueOf(sinhVienId));
        params.add(String.valueOf(sinhVienId));
        params.add(String.valueOf(sinhVienId));

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND LOWER(b.").append(COT_BT_TEN_BAI_TAP).append(") LIKE ? ");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }

        if (filterLopMon != null && !filterLopMon.equals("Tất cả")) {
            query.append("AND (l.").append(COT_LH_TEN_LOP).append(" = ? OR m.").append(COT_MH_TEN_MON).append(" = ?) ");
            params.add(filterLopMon);
            params.add(filterLopMon);
        }

        if (filterTrangThai != null && !filterTrangThai.equals("Tất cả")) {
            if (filterTrangThai.equals("Quá hạn")) {
                query.append("AND datetime(b.").append(COT_BT_HAN_NOP).append(") < datetime('now') AND COALESCE(td.").append(COT_TD_TRANG_THAI).append(", 'Chưa làm') != 'Hoàn thành' ");
            } else {
                query.append("AND COALESCE(td.").append(COT_TD_TRANG_THAI).append(", 'Chưa làm') = ? ");
                params.add(filterTrangThai);
            }
        }

        if ("Ưu tiên".equals(sortBy)) {
            query.append("ORDER BY CASE COALESCE(td.").append(COT_TD_MUC_UU_TIEN).append(", 'Bình thường') ")
                    .append("WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 ELSE 3 END, b.").append(COT_BT_HAN_NOP).append(" ASC");
        } else {
            query.append("ORDER BY b.").append(COT_BT_HAN_NOP).append(" ASC");
        }

        Cursor cursor = db.rawQuery(query.toString(), params.toArray(new String[0]));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date now = new Date();

        if (cursor.moveToFirst()) {
            do {
                BaiTap bt = parseBaiTap(cursor);
                bt.setTenNguon(cursor.getString(cursor.getColumnIndexOrThrow("ten_nguon")));
                bt.setProgressPercent(cursor.getInt(cursor.getColumnIndexOrThrow("percent_tien_do")));
                bt.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("trang_thai_sv")));
                bt.setMucUuTien(cursor.getString(cursor.getColumnIndexOrThrow("muc_uu_tien")));
                bt.setNhacHan(cursor.getInt(cursor.getColumnIndexOrThrow("nhac_han")));

                // Kiểm tra quá hạn
                try {
                    Date due = sdf.parse(bt.getHanNop());
                    if (due != null && due.before(now) && !"Hoàn thành".equals(bt.getTrangThai())) {
                        bt.setOverdue(true);
                    }
                } catch (Exception ignored) {
                }

                ds.add(bt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy bài tập theo ID kèm tiến độ của một sinh viên
     */
    public BaiTap layBaiTapTheoId(int baiTapId, int sinhVienId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT b.*, " +
                "COALESCE(l." + COT_LH_TEN_LOP + ", m." + COT_MH_TEN_MON + ") AS ten_nguon, " +
                "COALESCE(td." + COT_TD_PERCENT + ", 0) AS percent_tien_do, " +
                "COALESCE(td." + COT_TD_TRANG_THAI + ", 'Chưa làm') AS trang_thai_sv, " +
                "COALESCE(td." + COT_TD_MUC_UU_TIEN + ", 'Bình thường') AS muc_uu_tien, " +
                "COALESCE(td." + COT_TD_NHAC_HAN + ", 1) AS nhac_han " +
                "FROM " + BANG_BAI_TAP + " b " +
                "LEFT JOIN " + BANG_LOP_HOC + " l ON b." + COT_BT_MA_LOP_ID + " = l." + COT_LH_ID + " " +
                "LEFT JOIN " + BANG_MON_HOC_CA_NHAN + " m ON b." + COT_BT_MON_HOC_CA_NHAN_ID + " = m." + COT_MH_ID + " " +
                "LEFT JOIN " + BANG_TIEN_DO + " td ON b." + COT_BT_ID + " = td." + COT_TD_BAI_TAP_ID + " AND td." + COT_TD_SINH_VIEN_ID + " = ? " +
                "WHERE b." + COT_BT_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(sinhVienId), String.valueOf(baiTapId)});
        BaiTap bt = null;
        if (cursor.moveToFirst()) {
            bt = parseBaiTap(cursor);
            bt.setTenNguon(cursor.getString(cursor.getColumnIndexOrThrow("ten_nguon")));
            bt.setProgressPercent(cursor.getInt(cursor.getColumnIndexOrThrow("percent_tien_do")));
            bt.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("trang_thai_sv")));
            bt.setMucUuTien(cursor.getString(cursor.getColumnIndexOrThrow("muc_uu_tien")));
            bt.setNhacHan(cursor.getInt(cursor.getColumnIndexOrThrow("nhac_han")));
        }
        cursor.close();
        return bt;
    }

    /**
     * Thêm bài tập mới (Giáo viên giao hoặc Bài cá nhân)
     */
    public long themBaiTap(BaiTap bt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (bt.getMaLopId() != null) cv.put(COT_BT_MA_LOP_ID, bt.getMaLopId());
        if (bt.getMonHocCaNhanId() != null) cv.put(COT_BT_MON_HOC_CA_NHAN_ID, bt.getMonHocCaNhanId());
        cv.put(COT_BT_NGUOI_TAO_ID, bt.getNguoiTaoId());
        cv.put(COT_BT_TEN_BAI_TAP, bt.getTenBaiTap());
        cv.put(COT_BT_MO_TA, bt.getMoTa());
        cv.put(COT_BT_HAN_NOP, bt.getHanNop());
        cv.put(COT_BT_NGAY_TAO, bt.getNgayTao() != null ? bt.getNgayTao() : new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        cv.put(COT_BT_NGUON_BAI, bt.getNguonBai());
        return db.insert(BANG_BAI_TAP, null, cv);
    }

    /**
     * Cập nhật thông tin bài tập
     */
    public boolean capNhatBaiTap(BaiTap bt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_BT_TEN_BAI_TAP, bt.getTenBaiTap());
        cv.put(COT_BT_MO_TA, bt.getMoTa());
        cv.put(COT_BT_HAN_NOP, bt.getHanNop());
        int rows = db.update(BANG_BAI_TAP, cv, COT_BT_ID + " = ?", new String[]{String.valueOf(bt.getId())});
        return rows > 0;
    }

    /**
     * Xóa bài tập khỏi hệ thống
     */
    public boolean xoaBaiTap(int baiTapId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa các checklist liên quan
        db.execSQL("DELETE FROM " + BANG_CHECKLIST + " WHERE " + COT_CL_TIEN_DO_ID + " IN (SELECT id FROM " + BANG_TIEN_DO + " WHERE " + COT_TD_BAI_TAP_ID + " = ?)",
                new Object[]{baiTapId});
        // Xóa tiến độ
        db.delete(BANG_TIEN_DO, COT_TD_BAI_TAP_ID + " = ?", new String[]{String.valueOf(baiTapId)});
        // Xóa bài tập
        int rows = db.delete(BANG_BAI_TAP, COT_BT_ID + " = ?", new String[]{String.valueOf(baiTapId)});
        return rows > 0;
    }

    /**
     * Lấy bản ghi Tiến độ sinh viên (Tạo mới nếu chưa có)
     */
    public TienDoSinhVien layHoacTaoTienDo(int baiTapId, int sinhVienId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TIEN_DO + " WHERE " + COT_TD_BAI_TAP_ID + " = ? AND " + COT_TD_SINH_VIEN_ID + " = ?",
                new String[]{String.valueOf(baiTapId), String.valueOf(sinhVienId)});

        TienDoSinhVien td = null;
        if (cursor.moveToFirst()) {
            td = parseTienDo(cursor);
        } else {
            // Khởi tạo mới
            ContentValues cv = new ContentValues();
            cv.put(COT_TD_BAI_TAP_ID, baiTapId);
            cv.put(COT_TD_SINH_VIEN_ID, sinhVienId);
            cv.put(COT_TD_TRANG_THAI, "Chưa làm");
            cv.put(COT_TD_MUC_UU_TIEN, "Bình thường");
            cv.put(COT_TD_PERCENT, 0);
            cv.put(COT_TD_NHAC_HAN, 1);
            long id = db.insert(BANG_TIEN_DO, null, cv);
            td = new TienDoSinhVien((int) id, baiTapId, sinhVienId, "Chưa làm", "Bình thường", 0, 1);
        }
        cursor.close();
        return td;
    }

    /**
     * Cập nhật thông tin Tiến độ sinh viên (Ưu tiên, Trạng thái)
     */
    public boolean capNhatTienDo(TienDoSinhVien td) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TD_TRANG_THAI, td.getTrangThai());
        cv.put(COT_TD_MUC_UU_TIEN, td.getMucUuTien());
        cv.put(COT_TD_PERCENT, td.getPercentTienDo());
        cv.put(COT_TD_NHAC_HAN, td.getNhacHan());

        int rows = db.update(BANG_TIEN_DO, cv, COT_TD_ID + " = ?", new String[]{String.valueOf(td.getId())});
        return rows > 0;
    }

    /**
     * Lấy danh sách bài tập do Giáo viên giao cho 1 lớp
     */
    public List<BaiTap> layDanhSachBaiTapGiaoVien(int maLopId) {
        List<BaiTap> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_BAI_TAP + " WHERE " + COT_BT_MA_LOP_ID + " = ? ORDER BY " + COT_BT_ID + " DESC",
                new String[]{String.valueOf(maLopId)});
        if (cursor.moveToFirst()) {
            do {
                ds.add(parseBaiTap(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy báo cáo tiến độ các sinh viên trong lớp đối với 1 bài tập cụ thể
     */
    public List<TienDoSinhVien> layBaoCaoTiendoLop(int maLopId, int baiTapId) {
        List<TienDoSinhVien> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t." + COT_TK_ID + " AS sv_id, t." + COT_TK_HO_TEN + " AS sv_hoten, " +
                "COALESCE(td.id, -1) AS td_id, " +
                "COALESCE(td." + COT_TD_TRANG_THAI + ", 'Chưa làm') AS trang_thai, " +
                "COALESCE(td." + COT_TD_MUC_UU_TIEN + ", 'Bình thường') AS muc_uu_tien, " +
                "COALESCE(td." + COT_TD_PERCENT + ", 0) AS percent_tien_do, " +
                "COALESCE(td." + COT_TD_NHAC_HAN + ", 1) AS nhac_han " +
                "FROM " + BANG_THANH_VIEN_LOP + " tv " +
                "INNER JOIN " + BANG_TAI_KHOAN + " t ON tv." + COT_TV_SINH_VIEN_ID + " = t." + COT_TK_ID + " " +
                "LEFT JOIN " + BANG_TIEN_DO + " td ON td." + COT_TD_BAI_TAP_ID + " = ? AND td." + COT_TD_SINH_VIEN_ID + " = t." + COT_TK_ID + " " +
                "WHERE tv." + COT_TV_MA_LOP_ID + " = ? " +
                "ORDER BY t." + COT_TK_HO_TEN + " ASC";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(baiTapId), String.valueOf(maLopId)});
        if (cursor.moveToFirst()) {
            do {
                int svId = cursor.getInt(cursor.getColumnIndexOrThrow("sv_id"));
                String svHoTen = cursor.getString(cursor.getColumnIndexOrThrow("sv_hoten"));
                int tdId = cursor.getInt(cursor.getColumnIndexOrThrow("td_id"));
                String trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai"));
                String mucUuTien = cursor.getString(cursor.getColumnIndexOrThrow("muc_uu_tien"));
                int percent = cursor.getInt(cursor.getColumnIndexOrThrow("percent_tien_do"));
                int nhacHan = cursor.getInt(cursor.getColumnIndexOrThrow("nhac_han"));

                TienDoSinhVien td = new TienDoSinhVien(tdId, baiTapId, svId, trangThai, mucUuTien, percent, nhacHan);
                td.setTenSinhVien(svHoTen);
                ds.add(td);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Lấy tất cả bài tập trong hệ thống cho Admin giám sát
     */
    public List<BaiTap> layTatCaBaiTapHeThong() {
        List<BaiTap> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT b.*, " +
                "COALESCE(l." + COT_LH_TEN_LOP + ", m." + COT_MH_TEN_MON + ") AS ten_nguon " +
                "FROM " + BANG_BAI_TAP + " b " +
                "LEFT JOIN " + BANG_LOP_HOC + " l ON b." + COT_BT_MA_LOP_ID + " = l." + COT_LH_ID + " " +
                "LEFT JOIN " + BANG_MON_HOC_CA_NHAN + " m ON b." + COT_BT_MON_HOC_CA_NHAN_ID + " = m." + COT_MH_ID + " " +
                "ORDER BY b." + COT_BT_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                BaiTap bt = parseBaiTap(cursor);
                bt.setTenNguon(cursor.getString(cursor.getColumnIndexOrThrow("ten_nguon")));
                ds.add(bt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    private BaiTap parseBaiTap(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_BT_ID));
        Integer maLopId = cursor.isNull(cursor.getColumnIndexOrThrow(COT_BT_MA_LOP_ID)) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(COT_BT_MA_LOP_ID));
        Integer monHocId = cursor.isNull(cursor.getColumnIndexOrThrow(COT_BT_MON_HOC_CA_NHAN_ID)) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(COT_BT_MON_HOC_CA_NHAN_ID));
        int nguoiTaoId = cursor.getInt(cursor.getColumnIndexOrThrow(COT_BT_NGUOI_TAO_ID));
        String tenBt = cursor.getString(cursor.getColumnIndexOrThrow(COT_BT_TEN_BAI_TAP));
        String moTa = cursor.getString(cursor.getColumnIndexOrThrow(COT_BT_MO_TA));
        String hanNop = cursor.getString(cursor.getColumnIndexOrThrow(COT_BT_HAN_NOP));
        String ngayTao = cursor.getString(cursor.getColumnIndexOrThrow(COT_BT_NGAY_TAO));
        String nguonBai = cursor.getString(cursor.getColumnIndexOrThrow(COT_BT_NGUON_BAI));

        return new BaiTap(id, maLopId, monHocId, nguoiTaoId, tenBt, moTa, hanNop, ngayTao, nguonBai);
    }

    private TienDoSinhVien parseTienDo(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TD_ID));
        int btId = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TD_BAI_TAP_ID));
        int svId = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TD_SINH_VIEN_ID));
        String trangThai = cursor.getString(cursor.getColumnIndexOrThrow(COT_TD_TRANG_THAI));
        String mucUuTien = cursor.getString(cursor.getColumnIndexOrThrow(COT_TD_MUC_UU_TIEN));
        int percent = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TD_PERCENT));
        int nhacHan = cursor.getInt(cursor.getColumnIndexOrThrow(COT_TD_NHAC_HAN));
        return new TienDoSinhVien(id, btId, svId, trangThai, mucUuTien, percent, nhacHan);
    }

    // ==========================================
    // CÁC HÀM QUẢN LÝ CHECKLIST CÔNG VIỆC CON
    // ==========================================

    /**
     * Lấy danh sách các mục checklist công việc con của một bản ghi tiến độ
     */
    public List<ChecklistItem> layChecklist(int tienDoId) {
        List<ChecklistItem> ds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_CHECKLIST + " WHERE " + COT_CL_TIEN_DO_ID + " = ? ORDER BY " + COT_CL_ID + " ASC",
                new String[]{String.valueOf(tienDoId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COT_CL_ID));
                String noiDung = cursor.getString(cursor.getColumnIndexOrThrow(COT_CL_NOI_DUNG));
                int daHoanThanh = cursor.getInt(cursor.getColumnIndexOrThrow(COT_CL_DA_HOAN_THANH));
                ds.add(new ChecklistItem(id, tienDoId, noiDung, daHoanThanh));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ds;
    }

    /**
     * Thêm mục checklist công việc con mới
     */
    public boolean themChecklistItem(int tienDoId, String noiDung) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_CL_TIEN_DO_ID, tienDoId);
        cv.put(COT_CL_NOI_DUNG, noiDung.trim());
        cv.put(COT_CL_DA_HOAN_THANH, 0);
        long res = db.insert(BANG_CHECKLIST, null, cv);
        if (res != -1) {
            tinhLaiPhanTramTienDo(tienDoId);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật trạng thái checkbox hoàn thành mục checklist
     */
    public void capNhatChecklistItem(int checklistId, int daHoanThanh, int tienDoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_CL_DA_HOAN_THANH, daHoanThanh);
        db.update(BANG_CHECKLIST, cv, COT_CL_ID + " = ?", new String[]{String.valueOf(checklistId)});
        tinhLaiPhanTramTienDo(tienDoId);
    }

    /**
     * Xóa mục checklist công việc con
     */
    public boolean xoaChecklistItem(int checklistId, int tienDoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(BANG_CHECKLIST, COT_CL_ID + " = ?", new String[]{String.valueOf(checklistId)});
        if (rows > 0) {
            tinhLaiPhanTramTienDo(tienDoId);
            return true;
        }
        return false;
    }

    /**
     * Tính lại % tiến độ tự động dựa trên số mục checklist đã tích chọn
     */
    public void tinhLaiPhanTramTienDo(int tienDoId) {
        List<ChecklistItem> ds = layChecklist(tienDoId);
        if (ds.isEmpty()) return;

        int total = ds.size();
        int completed = 0;
        for (ChecklistItem item : ds) {
            if (item.getDaHoanThanh() == 1) {
                completed++;
            }
        }

        int percent = Math.round(((float) completed / total) * 100);
        String trangThai = percent == 100 ? "Hoàn thành" : (percent > 0 ? "Đang làm" : "Chưa làm");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TD_PERCENT, percent);
        cv.put(COT_TD_TRANG_THAI, trangThai);
        db.update(BANG_TIEN_DO, cv, COT_TD_ID + " = ?", new String[]{String.valueOf(tienDoId)});
    }

    // ==========================================
    // CÁC HÀM THỐNG KÊ VÀ BÁO CÁO CSV
    // ==========================================

    /**
     * Thống kê số lượng bài tập theo các trạng thái của sinh viên
     */
    public Map<String, Integer> layThongKeSinhVien(int sinhVienId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Chưa làm", 0);
        stats.put("Đang làm", 0);
        stats.put("Hoàn thành", 0);
        stats.put("Quá hạn", 0);

        List<BaiTap> ds = layDanhSachBaiTapCuaSinhVien(sinhVienId, null, "Tất cả", "Tất cả", "Hạn nộp");
        for (BaiTap bt : ds) {
            if (bt.isOverdue()) {
                stats.put("Quá hạn", stats.get("Quá hạn") + 1);
            }
            String st = bt.getTrangThai();
            if (stats.containsKey(st)) {
                stats.put(st, stats.get(st) + 1);
            } else {
                stats.put("Chưa làm", stats.get("Chưa làm") + 1);
            }
        }
        return stats;
    }

    /**
     * Cập nhật bật/tắt toàn bộ nhắc hạn cho sinh viên
     */
    public void capNhatNhacHanSinhVien(int sinhVienId, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COT_TD_NHAC_HAN, isEnabled ? 1 : 0);
        db.update(BANG_TIEN_DO, cv, COT_TD_SINH_VIEN_ID + " = ?", new String[]{String.valueOf(sinhVienId)});
    }
}
