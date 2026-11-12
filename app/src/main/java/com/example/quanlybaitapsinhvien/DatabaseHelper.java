package com.example.quanlybaitapsinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Class quản trị cơ sở dữ liệu SQLite nâng cấp phiên bản 2
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TEN_DATABASE = "QuanLyHocTap.db";
    private static final int PHIEN_BAN = 5;

    // Bảng Tài khoản
    public static final String BANG_TAI_KHOAN = "tai_khoan";
    public static final String COT_ID = "id";
    public static final String COT_EMAIL = "email";
    public static final String COT_MAT_KHAU = "mat_khau";
    public static final String COT_VAI_TRO = "vai_tro";
    public static final String COT_TRANG_THAI = "trang_thai";

    // Bảng Môn học
    public static final String BANG_MON_HOC = "mon_hoc";
    public static final String COT_MA_MON = "ma_mon";
    public static final String COT_TEN_MON = "ten_mon";
    public static final String COT_NAM_HOC = "nam_hoc";
    public static final String COT_ID_GIAO_VIEN = "id_giao_vien";

    // Bảng Sinh viên - Môn học
    public static final String BANG_SV_MON_HOC = "sinh_vien_mon_hoc";
    public static final String COT_ID_SINH_VIEN = "id_sinh_vien";
    public static final String COT_ID_MON_HOC = "id_mon_hoc";

    // Bảng Nội dung môn học / Bài học
    public static final String BANG_NOI_DUNG = "noi_dung_mon_hoc";
    public static final String COT_TIEU_DE = "tieu_de";
    public static final String COT_LOAI_NOI_DUNG = "loai_noi_dung";
    public static final String COT_URL_FILE = "url_file";
    public static final String COT_PHAN_CUM = "phan_cum";
    public static final String COT_ID_THAM_CHIEU = "id_tham_chieu";
    public static final String COT_MO_TA = "mo_ta";
    public static final String COT_HAN_NOP = "han_nop";

    // Bảng Trạng thái hoàn thành bài học
    public static final String BANG_HOAN_THANH = "hoan_thanh_bai_hoc";
    public static final String COT_ID_NOI_DUNG = "id_noi_dung";
    public static final String COT_DA_HOAN_THANH = "da_hoan_thanh";

    // Bảng Đề thi trắc nghiệm
    public static final String BANG_DE_THI = "de_thi_trac_nghiem";
    public static final String COT_THOI_GIAN_PHUT = "thoi_gian_phut";
    public static final String COT_THOI_GIAN_BAT_DAU = "thoi_gian_bat_dau";
    public static final String COT_THOI_GIAN_KET_THUC = "thoi_gian_ket_thuc";

    // Bảng Câu hỏi
    public static final String BANG_CAU_HOI = "cau_hoi";
    public static final String COT_ID_DE_THI = "id_de_thi";
    public static final String COT_NOI_DUNG_CH = "noi_dung";
    public static final String COT_ANH_PATH = "anh_path";
    public static final String COT_LOAI_CAU_HOI = "loai_cau_hoi";
    public static final String COT_DAP_AN_A = "dap_an_a";
    public static final String COT_DAP_AN_B = "dap_an_b";
    public static final String COT_DAP_AN_C = "dap_an_c";
    public static final String COT_DAP_AN_D = "dap_an_d";
    public static final String COT_DAP_AN_DUNG = "dap_an_dung";

    // Bảng Kết quả thi
    public static final String BANG_KET_QUA = "ket_qua_thi";
    public static final String COT_DIEM_SO = "diem_so";
    public static final String COT_NGAY_THI = "ngay_thi";

    // Bảng Bài nộp file
    public static final String BANG_BAI_NOP_FILE = "bai_nop_file";
    public static final String COT_TEN_FILE = "ten_file";
    public static final String COT_DUONG_DAN_FILE = "duong_dan_file";
    public static final String COT_NGAY_NOP = "ngay_nop";

    public DatabaseHelper(Context context) {
        super(context, TEN_DATABASE, null, PHIEN_BAN);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        taoCacBang(db);
        themDuLieuMacDinh(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BANG_BAI_NOP_FILE);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_KET_QUA);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_CAU_HOI);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_DE_THI);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_HOAN_THANH);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_NOI_DUNG);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_SV_MON_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_MON_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + BANG_TAI_KHOAN);
        onCreate(db);
    }

    private void taoCacBang(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + BANG_TAI_KHOAN + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COT_MAT_KHAU + " TEXT NOT NULL, " +
                COT_VAI_TRO + " TEXT NOT NULL, " +
                COT_TRANG_THAI + " INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + BANG_MON_HOC + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_MA_MON + " TEXT NOT NULL, " +
                COT_TEN_MON + " TEXT NOT NULL, " +
                COT_NAM_HOC + " TEXT NOT NULL, " +
                COT_ID_GIAO_VIEN + " INTEGER)");

        db.execSQL("CREATE TABLE " + BANG_SV_MON_HOC + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_SINH_VIEN + " INTEGER NOT NULL, " +
                COT_ID_MON_HOC + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + BANG_NOI_DUNG + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_MON_HOC + " INTEGER NOT NULL, " +
                COT_TIEU_DE + " TEXT NOT NULL, " +
                COT_LOAI_NOI_DUNG + " TEXT NOT NULL, " +
                COT_URL_FILE + " TEXT, " +
                COT_PHAN_CUM + " TEXT, " +
                COT_ID_THAM_CHIEU + " INTEGER DEFAULT 0, " +
                COT_MO_TA + " TEXT, " +
                COT_HAN_NOP + " TEXT)");

        db.execSQL("CREATE TABLE " + BANG_HOAN_THANH + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_SINH_VIEN + " INTEGER NOT NULL, " +
                COT_ID_NOI_DUNG + " INTEGER NOT NULL, " +
                COT_DA_HOAN_THANH + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + BANG_DE_THI + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_MON_HOC + " INTEGER NOT NULL, " +
                COT_TIEU_DE + " TEXT NOT NULL, " +
                COT_THOI_GIAN_PHUT + " INTEGER DEFAULT 15, " +
                COT_THOI_GIAN_BAT_DAU + " TEXT, " +
                COT_THOI_GIAN_KET_THUC + " TEXT)");

        db.execSQL("CREATE TABLE " + BANG_CAU_HOI + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_DE_THI + " INTEGER NOT NULL, " +
                COT_NOI_DUNG_CH + " TEXT NOT NULL, " +
                COT_ANH_PATH + " TEXT, " +
                COT_LOAI_CAU_HOI + " TEXT NOT NULL, " +
                COT_DAP_AN_A + " TEXT, " +
                COT_DAP_AN_B + " TEXT, " +
                COT_DAP_AN_C + " TEXT, " +
                COT_DAP_AN_D + " TEXT, " +
                COT_DAP_AN_DUNG + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + BANG_KET_QUA + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_SINH_VIEN + " INTEGER NOT NULL, " +
                COT_ID_DE_THI + " INTEGER NOT NULL, " +
                COT_DIEM_SO + " REAL NOT NULL, " +
                COT_NGAY_THI + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + BANG_BAI_NOP_FILE + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_ID_SINH_VIEN + " INTEGER NOT NULL, " +
                COT_ID_NOI_DUNG + " INTEGER NOT NULL, " +
                COT_TEN_FILE + " TEXT NOT NULL, " +
                COT_DUONG_DAN_FILE + " TEXT NOT NULL, " +
                "ngay_nop TEXT NOT NULL)");
    }

    private void themDuLieuMacDinh(SQLiteDatabase db) {
        // 1. Chèn tài khoản Admin và Giáo viên
        ContentValues admin = new ContentValues();
        admin.put(COT_EMAIL, "admin@gmail.com");
        admin.put(COT_MAT_KHAU, "admin123");
        admin.put(COT_VAI_TRO, "ADMIN");
        admin.put(COT_TRANG_THAI, 1);
        db.insert(BANG_TAI_KHOAN, null, admin);

        ContentValues gv = new ContentValues();
        gv.put(COT_EMAIL, "teacher@gmail.com");
        gv.put(COT_MAT_KHAU, "teacher123");
        gv.put(COT_VAI_TRO, "TEACHER");
        gv.put(COT_TRANG_THAI, 1);
        long gvId = db.insert(BANG_TAI_KHOAN, null, gv);

        ContentValues sv = new ContentValues();
        sv.put(COT_EMAIL, "student@gmail.com");
        sv.put(COT_MAT_KHAU, "student123");
        sv.put(COT_VAI_TRO, "STUDENT");
        sv.put(COT_TRANG_THAI, 1);
        long svId = db.insert(BANG_TAI_KHOAN, null, sv);

        // 2. Chèn 2 Môn học mẫu theo đúng thiết kế (@design)
        ContentValues mon1 = new ContentValues();
        mon1.put(COT_MA_MON, "COMP 261");
        mon1.put(COT_TEN_MON, "Trí tuệ nhân tạo");
        mon1.put(COT_NAM_HOC, "NĂM HỌC 2025-2026");
        mon1.put(COT_ID_GIAO_VIEN, gvId);
        long idMon1 = db.insert(BANG_MON_HOC, null, mon1);

        ContentValues mon2 = new ContentValues();
        mon2.put(COT_MA_MON, "COMP 270 - 292");
        mon2.put(COT_TEN_MON, "Hệ quản trị CSDL");
        mon2.put(COT_NAM_HOC, "NĂM HỌC 2024-2025");
        mon2.put(COT_ID_GIAO_VIEN, gvId);
        long idMon2 = db.insert(BANG_MON_HOC, null, mon2);

        // Xếp Sinh viên vào môn học
        ContentValues svMon1 = new ContentValues();
        svMon1.put(COT_ID_SINH_VIEN, svId);
        svMon1.put(COT_ID_MON_HOC, idMon1);
        db.insert(BANG_SV_MON_HOC, null, svMon1);

        ContentValues svMon2 = new ContentValues();
        svMon2.put(COT_ID_SINH_VIEN, svId);
        svMon2.put(COT_ID_MON_HOC, idMon2);
        db.insert(BANG_SV_MON_HOC, null, svMon2);

        // 3. Chèn Đề thi trắc nghiệm mẫu cho môn Hệ quản trị CSDL
        ContentValues deThi = new ContentValues();
        deThi.put(COT_ID_MON_HOC, idMon2);
        deThi.put(COT_TIEU_DE, "Trắc nghiệm chương 2 - Hệ quản trị CSDL");
        deThi.put(COT_THOI_GIAN_PHUT, 10);
        long idDeThi = db.insert(BANG_DE_THI, null, deThi);

        // Câu hỏi 1 (Radio - Chọn 1)
        ContentValues ch1 = new ContentValues();
        ch1.put(COT_ID_DE_THI, idDeThi);
        ch1.put(COT_NOI_DUNG_CH, "Câu 1: Khóa chính (Primary Key) trong CSDL dùng để làm gì?");
        ch1.put(COT_LOAI_CAU_HOI, CauHoi.LOAI_SINGLE);
        ch1.put(COT_DAP_AN_A, "Xác định duy nhất mỗi bản ghi trong bảng");
        ch1.put(COT_DAP_AN_B, "Liên kết bảng với bảng khác");
        ch1.put(COT_DAP_AN_C, "Cho phép lưu giá trị NULL");
        ch1.put(COT_DAP_AN_D, "Tạo bản sao lưu dữ liệu");
        ch1.put(COT_DAP_AN_DUNG, "A");
        db.insert(BANG_CAU_HOI, null, ch1);

        // Câu hỏi 2 (Checkbox - Chọn nhiều)
        ContentValues ch2 = new ContentValues();
        ch2.put(COT_ID_DE_THI, idDeThi);
        ch2.put(COT_NOI_DUNG_CH, "Câu 2: Những câu lệnh nào thuộc nhóm DDL (Data Definition Language)?");
        ch2.put(COT_LOAI_CAU_HOI, CauHoi.LOAI_MULTI);
        ch2.put(COT_DAP_AN_A, "CREATE TABLE");
        ch2.put(COT_DAP_AN_B, "ALTER TABLE");
        ch2.put(COT_DAP_AN_C, "SELECT * FROM");
        ch2.put(COT_DAP_AN_D, "DROP TABLE");
        ch2.put(COT_DAP_AN_DUNG, "A,B,D");
        db.insert(BANG_CAU_HOI, null, ch2);

        // 4. Chèn Nội dung môn học (Bài học, Video, Link, Trắc nghiệm, Nộp bài tập) theo đúng Ảnh thiết kế
        chenNoiDung(db, idMon2, "Giới thiệu môn học", NoiDungMonHoc.LOAI_PDF, "pdf_gioi_thieu.pdf", null, 0, "Slide giới thiệu tổng quan về môn Hệ quản trị CSDL");
        chenNoiDung(db, idMon2, "Video Giới thiệu môn học", NoiDungMonHoc.LOAI_VIDEO, "https://youtube.com", null, 0, "Video hướng dẫn cài đặt SQL Server");
        chenNoiDung(db, idMon2, "Điểm danh", NoiDungMonHoc.LOAI_LINK, "https://diemdanh.edu.vn", null, 0, "Link điểm danh đầu giờ học");
        chenNoiDung(db, idMon2, "sqliteonline.com", NoiDungMonHoc.LOAI_LINK, "https://sqliteonline.com", null, 0, "Website thực hành truy vấn SQL trực tuyến");
        chenNoiDung(db, idMon2, "Full script database AP", NoiDungMonHoc.LOAI_PDF, "script_database.sql", null, 0, "Tệp script khởi tạo CSDL AP");

        chenNoiDung(db, idMon2, "Bài giảng Chương 3 - Phần 3", NoiDungMonHoc.LOAI_PDF, "chuong3.pdf", "PHẦN 3 - ÔN TẬP THIẾT KẾ CSDL QUAN HỆ (Tham khảo)", 0, "Nội dung bài giảng chuẩn bị cho bài tập");
        chenNoiDung(db, idMon2, "Video C4-P4-ThietKeCSDL", NoiDungMonHoc.LOAI_VIDEO, "https://youtube.com", "PHẦN 3 - ÔN TẬP THIẾT KẾ CSDL QUAN HỆ (Tham khảo)", 0, "Video giảng bài thiết kế CSDL");
        chenNoiDung(db, idMon2, "Nộp bài tập tuần 4", NoiDungMonHoc.LOAI_NOP_BAI, "", "PHẦN 3 - ÔN TẬP THIẾT KẾ CSDL QUAN HỆ (Tham khảo)", 1, "Sinh viên hoàn thiện sơ đồ ERD và nộp tệp PDF bài làm.");

        chenNoiDung(db, idMon2, "Trắc nghiệm chương 2", NoiDungMonHoc.LOAI_TRAC_NGHIEM, "", "CHƯƠNG 2 - BÀI THI & BÀI TẬP", (int) idDeThi, "Bài thi trắc nghiệm đếm giờ 10 phút.");
        chenNoiDung(db, idMon2, "Nộp bài tập 1 - Thiết kế biểu đồ thực thể liên kết", NoiDungMonHoc.LOAI_NOP_BAI, "", "CHƯƠNG 2 - BÀI THI & BÀI TẬP", 2, "Vẽ sơ đồ ERD cho hệ thống quản lý thư viện và gửi file.");
    }

    private void chenNoiDung(SQLiteDatabase db, long idMon, String tieuDe, String loai, String url, String phanCum, int idThamChieu, String moTa) {
        ContentValues val = new ContentValues();
        val.put(COT_ID_MON_HOC, idMon);
        val.put(COT_TIEU_DE, tieuDe);
        val.put(COT_LOAI_NOI_DUNG, loai);
        val.put(COT_URL_FILE, url);
        val.put(COT_PHAN_CUM, phanCum);
        val.put(COT_ID_THAM_CHIEU, idThamChieu);
        val.put(COT_MO_TA, moTa);
        db.insert(BANG_NOI_DUNG, null, val);
    }

    // --- CÁC HÀM XÁC THỰC VÀ TÀI KHOẢN ---

    public boolean dangKyTaiKhoan(String email, String matKhau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_EMAIL, email.trim().toLowerCase());
        values.put(COT_MAT_KHAU, matKhau.trim());
        values.put(COT_VAI_TRO, "STUDENT");
        values.put(COT_TRANG_THAI, 1);
        long result = db.insert(BANG_TAI_KHOAN, null, values);
        return result != -1;
    }

    public boolean taoTaiKhoanGiaoVien(String email, String matKhau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_EMAIL, email.trim().toLowerCase());
        values.put(COT_MAT_KHAU, matKhau.trim());
        values.put(COT_VAI_TRO, "TEACHER");
        values.put(COT_TRANG_THAI, 1);
        long result = db.insert(BANG_TAI_KHOAN, null, values);
        return result != -1;
    }

    public boolean kiemTraEmailTonTai(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_EMAIL + ") = ?", new String[]{email.trim().toLowerCase()});
        boolean tonTai = cursor.getCount() > 0;
        cursor.close();
        return tonTai;
    }

    public TaiKhoan kiemTraDangNhap(String email, String matKhau) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_EMAIL + ") = ? AND " + COT_MAT_KHAU + " = ?", new String[]{email.trim().toLowerCase(), matKhau.trim()});

        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            taiKhoan = new TaiKhoan(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MAT_KHAU)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_VAI_TRO)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_TRANG_THAI))
            );
        }
        cursor.close();
        return taiKhoan;
    }

    public TaiKhoan layTaiKhoanTheoId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE " + COT_ID + " = ?", new String[]{String.valueOf(userId)});
        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            taiKhoan = new TaiKhoan(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MAT_KHAU)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_VAI_TRO)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_TRANG_THAI))
            );
        }
        cursor.close();
        return taiKhoan;
    }

    public List<TaiKhoan> layDanhSachGiaoVien() {
        List<TaiKhoan> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_TAI_KHOAN + " WHERE " + COT_VAI_TRO + " = 'TEACHER'", null);
        while (cursor.moveToNext()) {
            list.add(new TaiKhoan(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MAT_KHAU)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_VAI_TRO)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_TRANG_THAI))
            ));
        }
        cursor.close();
        return list;
    }

    // --- CÁC HÀM QUẢN LÝ MÔN HỌC ---

    public boolean taoMonHoc(String maMon, String tenMon, String namHoc, int idGiaoVien) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_MA_MON, maMon.trim());
        values.put(COT_TEN_MON, tenMon.trim());
        values.put(COT_NAM_HOC, namHoc.trim());
        values.put(COT_ID_GIAO_VIEN, idGiaoVien);
        return db.insert(BANG_MON_HOC, null, values) != -1;
    }

    public List<MonHoc> layDanhSachMonHocChoSinhVien(int idSinhVien) {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT m.*, t." + COT_EMAIL + " AS ten_gv FROM " + BANG_MON_HOC + " m " +
                "INNER JOIN " + BANG_SV_MON_HOC + " sm ON m." + COT_ID + " = sm." + COT_ID_MON_HOC + " " +
                "LEFT JOIN " + BANG_TAI_KHOAN + " t ON m." + COT_ID_GIAO_VIEN + " = t." + COT_ID + " " +
                "WHERE sm." + COT_ID_SINH_VIEN + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idSinhVien)});
        while (cursor.moveToNext()) {
            list.add(new MonHoc(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MA_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_TEN_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_NAM_HOC)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_GIAO_VIEN)),
                    cursor.getString(cursor.getColumnIndexOrThrow("ten_gv"))
            ));
        }
        cursor.close();
        if (list.isEmpty()) {
            return layTatCaMonHoc();
        }
        return list;
    }

    public List<MonHoc> layMonHocChoGiaoVien(int idGiaoVien) {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT m.*, t." + COT_EMAIL + " AS ten_gv FROM " + BANG_MON_HOC + " m " +
                "LEFT JOIN " + BANG_TAI_KHOAN + " t ON m." + COT_ID_GIAO_VIEN + " = t." + COT_ID + " " +
                "WHERE m." + COT_ID_GIAO_VIEN + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idGiaoVien)});
        while (cursor.moveToNext()) {
            list.add(new MonHoc(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MA_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_TEN_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_NAM_HOC)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_GIAO_VIEN)),
                    cursor.getString(cursor.getColumnIndexOrThrow("ten_gv"))
            ));
        }
        cursor.close();
        return list;
    }

    public List<MonHoc> layTatCaMonHoc() {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT m.*, t." + COT_EMAIL + " AS ten_gv FROM " + BANG_MON_HOC + " m " +
                "LEFT JOIN " + BANG_TAI_KHOAN + " t ON m." + COT_ID_GIAO_VIEN + " = t." + COT_ID;
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new MonHoc(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_MA_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_TEN_MON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_NAM_HOC)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_GIAO_VIEN)),
                    cursor.getString(cursor.getColumnIndexOrThrow("ten_gv"))
            ));
        }
        cursor.close();
        return list;
    }

    // --- CÁC HÀM XỬ LÝ NỘI DUNG VÀ TRẠNG THÁI HOÀN THÀNH ---

    public List<NoiDungMonHoc> layNoiDungMonHoc(int idMonHoc, int idSinhVien) {
        List<NoiDungMonHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT n.*, CASE WHEN h." + COT_DA_HOAN_THANH + " = 1 THEN 1 ELSE 0 END AS hoan_thanh " +
                "FROM " + BANG_NOI_DUNG + " n " +
                "LEFT JOIN " + BANG_HOAN_THANH + " h ON n." + COT_ID + " = h." + COT_ID_NOI_DUNG + " AND h." + COT_ID_SINH_VIEN + " = ? " +
                "WHERE n." + COT_ID_MON_HOC + " = ? " +
                "ORDER BY n." + COT_ID + " ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idSinhVien), String.valueOf(idMonHoc)});
        while (cursor.moveToNext()) {
            int moTaIndex = cursor.getColumnIndex(COT_MO_TA);
            String moTa = moTaIndex != -1 ? cursor.getString(moTaIndex) : "";

            int hanNopIndex = cursor.getColumnIndex(COT_HAN_NOP);
            String hanNop = hanNopIndex != -1 ? cursor.getString(hanNopIndex) : "";

            list.add(new NoiDungMonHoc(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_MON_HOC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_TIEU_DE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_LOAI_NOI_DUNG)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_URL_FILE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_PHAN_CUM)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_THAM_CHIEU)),
                    cursor.getInt(cursor.getColumnIndexOrThrow("hoan_thanh")) == 1,
                    moTa != null ? moTa : "",
                    hanNop != null ? hanNop : ""
            ));
        }
        cursor.close();
        return list;
    }

    public NoiDungMonHoc layNoiDungTheoId(int idNoiDung) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_NOI_DUNG + " WHERE " + COT_ID + " = ?", new String[]{String.valueOf(idNoiDung)});
        NoiDungMonHoc nd = null;
        if (cursor.moveToFirst()) {
            int moTaIndex = cursor.getColumnIndex(COT_MO_TA);
            String moTa = moTaIndex != -1 ? cursor.getString(moTaIndex) : "";

            int hanNopIndex = cursor.getColumnIndex(COT_HAN_NOP);
            String hanNop = hanNopIndex != -1 ? cursor.getString(hanNopIndex) : "";

            nd = new NoiDungMonHoc(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_MON_HOC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_TIEU_DE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_LOAI_NOI_DUNG)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_URL_FILE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COT_PHAN_CUM)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COT_ID_THAM_CHIEU)),
                    false,
                    moTa != null ? moTa : "",
                    hanNop != null ? hanNop : ""
            );
        }
        cursor.close();
        return nd;
    }

    public void capNhatHoanThanh(int idSinhVien, int idNoiDung, boolean hoanThanh) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_SINH_VIEN, idSinhVien);
        values.put(COT_ID_NOI_DUNG, idNoiDung);
        values.put(COT_DA_HOAN_THANH, hoanThanh ? 1 : 0);

        db.delete(BANG_HOAN_THANH, COT_ID_SINH_VIEN + " = ? AND " + COT_ID_NOI_DUNG + " = ?",
                new String[]{String.valueOf(idSinhVien), String.valueOf(idNoiDung)});
        db.insert(BANG_HOAN_THANH, null, values);
    }

    public boolean themNoiDungMonHoc(int idMonHoc, String tieuDe, String loaiNoiDung, String urlFile, String phanCum, int idThamChieu, String moTa, String hanNop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_MON_HOC, idMonHoc);
        values.put(COT_TIEU_DE, tieuDe);
        values.put(COT_LOAI_NOI_DUNG, loaiNoiDung);
        values.put(COT_URL_FILE, urlFile);
        values.put(COT_PHAN_CUM, phanCum);
        values.put(COT_ID_THAM_CHIEU, idThamChieu);
        values.put(COT_MO_TA, moTa);
        values.put(COT_HAN_NOP, hanNop);
        return db.insert(BANG_NOI_DUNG, null, values) != -1;
    }

    public boolean themNoiDungMonHoc(int idMonHoc, String tieuDe, String loaiNoiDung, String urlFile, String phanCum, int idThamChieu, String moTa) {
        return themNoiDungMonHoc(idMonHoc, tieuDe, loaiNoiDung, urlFile, phanCum, idThamChieu, moTa, "");
    }

    public boolean themNoiDungMonHoc(int idMonHoc, String tieuDe, String loaiNoiDung, String urlFile, String phanCum, int idThamChieu) {
        return themNoiDungMonHoc(idMonHoc, tieuDe, loaiNoiDung, urlFile, phanCum, idThamChieu, "", "");
    }

    public boolean xoaNoiDungMonHoc(int idNoiDung) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(BANG_NOI_DUNG, COT_ID + " = ?", new String[]{String.valueOf(idNoiDung)}) > 0;
    }

    public boolean capNhatNoiDungMonHoc(int idNoiDung, String tieuDe, String phanCum, String moTa, String urlFile, String hanNop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_TIEU_DE, tieuDe.trim());
        values.put(COT_PHAN_CUM, phanCum.trim());
        values.put(COT_MO_TA, moTa.trim());
        if (urlFile != null) {
            values.put(COT_URL_FILE, urlFile.trim());
        }
        if (hanNop != null) {
            values.put(COT_HAN_NOP, hanNop.trim());
        }
        return db.update(BANG_NOI_DUNG, values, COT_ID + " = ?", new String[]{String.valueOf(idNoiDung)}) > 0;
    }

    public boolean capNhatNoiDungMonHoc(int idNoiDung, String tieuDe, String phanCum, String moTa, String urlFile) {
        return capNhatNoiDungMonHoc(idNoiDung, tieuDe, phanCum, moTa, urlFile, null);
    }

    public boolean capNhatNoiDungMonHoc(int idNoiDung, String tieuDe, String phanCum, String moTa) {
        return capNhatNoiDungMonHoc(idNoiDung, tieuDe, phanCum, moTa, null, null);
    }

    public boolean capNhatThamChieuNoiDung(int idNoiDung, int idThamChieu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_THAM_CHIEU, idThamChieu);
        return db.update(BANG_NOI_DUNG, values, COT_ID + " = ?", new String[]{String.valueOf(idNoiDung)}) > 0;
    }

    // --- CÁC HÀM XỬ LÝ ĐỀ THI VÀ CÂU HỎI TRẮC NGHIỆM ---

    public long taoDeThi(int idMonHoc, String tieuDe, int thoiGianPhut, String thoiGianBatDau, String thoiGianKetThuc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_MON_HOC, idMonHoc);
        values.put(COT_TIEU_DE, tieuDe);
        values.put(COT_THOI_GIAN_PHUT, thoiGianPhut);
        values.put(COT_THOI_GIAN_BAT_DAU, thoiGianBatDau);
        values.put(COT_THOI_GIAN_KET_THUC, thoiGianKetThuc);
        return db.insert(BANG_DE_THI, null, values);
    }

    public long taoDeThi(int idMonHoc, String tieuDe, int thoiGianPhut) {
        return taoDeThi(idMonHoc, tieuDe, thoiGianPhut, "", "");
    }

    public boolean capNhatDeThi(int idDeThi, String tieuDe, int thoiGianPhut, String thoiGianBatDau, String thoiGianKetThuc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_TIEU_DE, tieuDe.trim());
        values.put(COT_THOI_GIAN_PHUT, thoiGianPhut);
        values.put(COT_THOI_GIAN_BAT_DAU, thoiGianBatDau);
        values.put(COT_THOI_GIAN_KET_THUC, thoiGianKetThuc);
        return db.update(BANG_DE_THI, values, COT_ID + " = ?", new String[]{String.valueOf(idDeThi)}) > 0;
    }

    public boolean capNhatDeThi(int idDeThi, String tieuDe, int thoiGianPhut) {
        return capNhatDeThi(idDeThi, tieuDe, thoiGianPhut, "", "");
    }

    public void xoaCauHoiCuaDeThi(int idDeThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BANG_CAU_HOI, COT_ID_DE_THI + " = ?", new String[]{String.valueOf(idDeThi)});
    }

    public boolean themCauHoi(long idDeThi, String noiDung, String anhPath, String loai, String a, String b, String c, String d, String dapAnDung) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_DE_THI, idDeThi);
        values.put(COT_NOI_DUNG_CH, noiDung);
        values.put(COT_ANH_PATH, anhPath);
        values.put(COT_LOAI_CAU_HOI, loai);
        values.put(COT_DAP_AN_A, a);
        values.put(COT_DAP_AN_B, b);
        values.put(COT_DAP_AN_C, c);
        values.put(COT_DAP_AN_D, d);
        values.put(COT_DAP_AN_DUNG, dapAnDung);
        return db.insert(BANG_CAU_HOI, null, values) != -1;
    }

    public DeThi layDeThiTheoId(int idDeThi) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_DE_THI + " WHERE " + COT_ID + " = ?", new String[]{String.valueOf(idDeThi)});
        DeThi deThi = null;
        if (cursor != null && cursor.moveToFirst()) {
            int idIdx = cursor.getColumnIndex(COT_ID);
            int monHocIdx = cursor.getColumnIndex(COT_ID_MON_HOC);
            int tieuDeIdx = cursor.getColumnIndex(COT_TIEU_DE);
            int tgPhutIdx = cursor.getColumnIndex(COT_THOI_GIAN_PHUT);
            int bdIdx = cursor.getColumnIndex(COT_THOI_GIAN_BAT_DAU);
            int ktIdx = cursor.getColumnIndex(COT_THOI_GIAN_KET_THUC);

            int id = idIdx != -1 ? cursor.getInt(idIdx) : idDeThi;
            int monHoc = monHocIdx != -1 ? cursor.getInt(monHocIdx) : 1;
            String tieuDe = tieuDeIdx != -1 ? cursor.getString(tieuDeIdx) : "Đề thi";
            int tgPhut = tgPhutIdx != -1 ? cursor.getInt(tgPhutIdx) : 15;
            String bd = bdIdx != -1 ? cursor.getString(bdIdx) : "";
            String kt = ktIdx != -1 ? cursor.getString(ktIdx) : "";

            deThi = new DeThi(id, monHoc, tieuDe, tgPhut, bd != null ? bd : "", kt != null ? kt : "");
        }
        if (cursor != null) cursor.close();
        return deThi;
    }

    public List<CauHoi> layDanhSachCauHoi(int idDeThi) {
        List<CauHoi> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BANG_CAU_HOI + " WHERE " + COT_ID_DE_THI + " = ?", new String[]{String.valueOf(idDeThi)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIdx = cursor.getColumnIndex(COT_ID);
                int deThiIdx = cursor.getColumnIndex(COT_ID_DE_THI);
                int ndIdx = cursor.getColumnIndex(COT_NOI_DUNG_CH);
                int anhIdx = cursor.getColumnIndex(COT_ANH_PATH);
                int loaiIdx = cursor.getColumnIndex(COT_LOAI_CAU_HOI);
                int aIdx = cursor.getColumnIndex(COT_DAP_AN_A);
                int bIdx = cursor.getColumnIndex(COT_DAP_AN_B);
                int cIdx = cursor.getColumnIndex(COT_DAP_AN_C);
                int dIdx = cursor.getColumnIndex(COT_DAP_AN_D);
                int dungIdx = cursor.getColumnIndex(COT_DAP_AN_DUNG);

                int id = idIdx != -1 ? cursor.getInt(idIdx) : 0;
                int dtId = deThiIdx != -1 ? cursor.getInt(deThiIdx) : idDeThi;
                String nd = ndIdx != -1 ? cursor.getString(ndIdx) : "";
                String anh = anhIdx != -1 ? cursor.getString(anhIdx) : "";
                String loai = loaiIdx != -1 ? cursor.getString(loaiIdx) : CauHoi.LOAI_SINGLE;
                String a = aIdx != -1 ? cursor.getString(aIdx) : "";
                String b = bIdx != -1 ? cursor.getString(bIdx) : "";
                String c = cIdx != -1 ? cursor.getString(cIdx) : "";
                String d = dIdx != -1 ? cursor.getString(dIdx) : "";
                String dung = dungIdx != -1 ? cursor.getString(dungIdx) : "A";

                list.add(new CauHoi(id, dtId, nd, anh != null ? anh : "", loai, a, b, c, d, dung));
            }
            cursor.close();
        }
        return list;
    }

    public void luuKetQuaThi(int idSinhVien, int idDeThi, double diemSo, String ngayThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_SINH_VIEN, idSinhVien);
        values.put(COT_ID_DE_THI, idDeThi);
        values.put(COT_DIEM_SO, diemSo);
        values.put(COT_NGAY_THI, ngayThi);
        db.insert(BANG_KET_QUA, null, values);
    }

    // --- CÁC HÀM NỘP FILE BÀI TẬP ---

    public boolean luuBaiNopFile(int idSinhVien, int idNoiDung, String tenFile, String duongDanFile, String ngayNop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_ID_SINH_VIEN, idSinhVien);
        values.put(COT_ID_NOI_DUNG, idNoiDung);
        values.put(COT_TEN_FILE, tenFile);
        values.put(COT_DUONG_DAN_FILE, duongDanFile);
        values.put(COT_NGAY_NOP, ngayNop);
        long result = db.insert(BANG_BAI_NOP_FILE, null, values);

        // Đánh dấu hoàn thành bài tập nộp file
        if (result != -1) {
            capNhatHoanThanh(idSinhVien, idNoiDung, true);
        }
        return result != -1;
    }

    public static class BaiNopItem {
        public String email;
        public String tenFile;
        public String duongDanFile;
        public String ngayNop;

        public BaiNopItem(String email, String tenFile, String duongDanFile, String ngayNop) {
            this.email = email;
            this.tenFile = tenFile;
            this.duongDanFile = duongDanFile;
            this.ngayNop = ngayNop;
        }
    }

    public List<BaiNopItem> layDanhSachBaiNopFull(int idNoiDung) {
        List<BaiNopItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t." + COT_EMAIL + ", b." + COT_TEN_FILE + ", b." + COT_DUONG_DAN_FILE + ", b." + COT_NGAY_NOP + " " +
                "FROM " + BANG_BAI_NOP_FILE + " b " +
                "INNER JOIN " + BANG_TAI_KHOAN + " t ON b." + COT_ID_SINH_VIEN + " = t." + COT_ID + " " +
                "WHERE b." + COT_ID_NOI_DUNG + " = ? ORDER BY b." + COT_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idNoiDung)});
        while (cursor.moveToNext()) {
            String email = cursor.getString(0);
            String tenFile = cursor.getString(1);
            String duongDan = cursor.getString(2);
            String ngayNop = cursor.getString(3);
            list.add(new BaiNopItem(email, tenFile, duongDan, ngayNop));
        }
        cursor.close();
        return list;
    }

    public List<String> layDanhSachBaiNopCuaSinhVien(int idNoiDung) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t." + COT_EMAIL + ", b." + COT_TEN_FILE + ", b." + COT_NGAY_NOP + " " +
                "FROM " + BANG_BAI_NOP_FILE + " b " +
                "INNER JOIN " + BANG_TAI_KHOAN + " t ON b." + COT_ID_SINH_VIEN + " = t." + COT_ID + " " +
                "WHERE b." + COT_ID_NOI_DUNG + " = ? ORDER BY b." + COT_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idNoiDung)});
        while (cursor.moveToNext()) {
            String email = cursor.getString(0);
            String tenFile = cursor.getString(1);
            String ngayNop = cursor.getString(2);
            list.add("👤 Sinh viên: " + email + "\n📄 File nộp: " + tenFile + "\n🕒 Thời gian nộp: " + ngayNop);
        }
        cursor.close();
        return list;
    }

    public List<String> layDanhSachKetQuaThiCuaSinhVien(int idDeThi) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT t." + COT_EMAIL + ", k." + COT_DIEM_SO + ", k." + COT_NGAY_THI + " " +
                "FROM " + BANG_KET_QUA + " k " +
                "INNER JOIN " + BANG_TAI_KHOAN + " t ON k." + COT_ID_SINH_VIEN + " = t." + COT_ID + " " +
                "WHERE k." + COT_ID_DE_THI + " = ? ORDER BY k." + COT_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idDeThi)});
        while (cursor.moveToNext()) {
            String email = cursor.getString(0);
            double diem = cursor.getDouble(1);
            String ngayThi = cursor.getString(2);
            list.add("👤 Sinh viên: " + email + "\n📊 Điểm số: " + diem + "/10.0\n🕒 Ngày làm: " + ngayThi);
        }
        cursor.close();
        return list;
    }
}
