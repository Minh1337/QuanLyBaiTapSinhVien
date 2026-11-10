package com.example.quanlybaitapsinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class quản trị cơ sở dữ liệu SQLite cho ứng dụng Quản lý học tập
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên cơ sở dữ liệu và phiên bản
    private static final String TEN_DATABASE = "QuanLyHocTap.db";
    private static final int PHIEN_BAN = 1;

    // Tên bảng và các cột của bảng tài khoản
    public static final String BANG_TAI_KHOAN = "tai_khoan";
    public static final String COT_ID = "id";
    public static final String COT_EMAIL = "email";
    public static final String COT_MAT_KHAU = "mat_khau";
    public static final String COT_VAI_TRO = "vai_tro";
    public static final String COT_TRANG_THAI = "trang_thai";

    public DatabaseHelper(Context context) {
        super(context, TEN_DATABASE, null, PHIEN_BAN);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Câu lệnh tạo bảng tài khoản
        String taoBangTaiKhoan = "CREATE TABLE " + BANG_TAI_KHOAN + " (" +
                COT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COT_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COT_MAT_KHAU + " TEXT NOT NULL, " +
                COT_VAI_TRO + " TEXT NOT NULL, " +
                COT_TRANG_THAI + " INTEGER DEFAULT 1)";
        db.execSQL(taoBangTaiKhoan);

        // Tự động chèn tài khoản Admin mặc định
        themAdminMacDinh(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BANG_TAI_KHOAN);
        onCreate(db);
    }

    /**
     * Thêm tài khoản Admin mặc định khi khởi tạo cơ sở dữ liệu
     * Email: admin@gmail.com | Mật khẩu: admin123 | Vai trò: ADMIN
     */
    private void themAdminMacDinh(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COT_EMAIL, "admin@gmail.com");
        values.put(COT_MAT_KHAU, "admin123");
        values.put(COT_VAI_TRO, "ADMIN");
        values.put(COT_TRANG_THAI, 1);
        db.insert(BANG_TAI_KHOAN, null, values);
    }

    /**
     * Đăng ký tài khoản Sinh viên mới (vai trò mặc định là STUDENT)
     * @param email Email sinh viên đăng ký
     * @param matKhau Mật khẩu sinh viên tạo
     * @return true nếu đăng ký thành công, false nếu thất bại
     */
    public boolean dangKyTaiKhoan(String email, String matKhau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COT_EMAIL, email.trim().toLowerCase());
        values.put(COT_MAT_KHAU, matKhau.trim());
        values.put(COT_VAI_TRO, "STUDENT");
        values.put(COT_TRANG_THAI, 1);

        long ketQua = db.insert(BANG_TAI_KHOAN, null, values);
        return ketQua != -1;
    }


    //Kiểm tra xem địa chỉ Email đã tồn tại trong hệ thống hay chưa
    public boolean kiemTraEmailTonTai(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_EMAIL + ") = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{email.trim().toLowerCase()});
        boolean tonTai = cursor.getCount() > 0;
        cursor.close();
        return tonTai;
    }

    // Kiểm tra đăng nhập với Email và Mật khẩu
    public TaiKhoan kiemTraDangNhap(String email, String matKhau) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + BANG_TAI_KHOAN + " WHERE LOWER(" + COT_EMAIL + ") = ? AND " + COT_MAT_KHAU + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{email.trim().toLowerCase(), matKhau.trim()});

        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COT_ID);
            int emailIndex = cursor.getColumnIndexOrThrow(COT_EMAIL);
            int matKhauIndex = cursor.getColumnIndexOrThrow(COT_MAT_KHAU);
            int vaiTroIndex = cursor.getColumnIndexOrThrow(COT_VAI_TRO);
            int trangThaiIndex = cursor.getColumnIndexOrThrow(COT_TRANG_THAI);

            taiKhoan = new TaiKhoan(
                    cursor.getInt(idIndex),
                    cursor.getString(emailIndex),
                    cursor.getString(matKhauIndex),
                    cursor.getString(vaiTroIndex),
                    cursor.getInt(trangThaiIndex)
            );
        }
        cursor.close();
        return taiKhoan;
    }
}
