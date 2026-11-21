package com.example.quanlybaitapsinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Locale;

/** Lưu tài khoản, lớp học phần, bài tập và bài nộp bằng SQLite. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TEN_CSDL = "QuanLyHocTap.db";
    private static final int PHIEN_BAN_CSDL = 7;

    public static final String BANG_TAI_KHOAN = "tai_khoan";
    public static final String BANG_LOP_HOC_PHAN = "lop_hoc_phan";
    public static final String BANG_THANH_VIEN_LOP = "thanh_vien_lop";
    public static final String BANG_BAI_TAP = "bai_tap";
    public static final String BANG_BAI_NOP = "bai_nop";
    public static final String BANG_VIEC_CA_NHAN = "bai_ca_nhan";

    public DatabaseHelper(Context boiCanh) { super(boiCanh, TEN_CSDL, null, PHIEN_BAN_CSDL); }

    @Override public void onConfigure(SQLiteDatabase coSoDuLieu) {
        super.onConfigure(coSoDuLieu);
        coSoDuLieu.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase coSoDuLieu) {
        coSoDuLieu.execSQL("CREATE TABLE " + BANG_TAI_KHOAN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE NOT NULL, mat_khau TEXT NOT NULL, vai_tro TEXT NOT NULL, trang_thai INTEGER NOT NULL DEFAULT 1, ho_ten TEXT NOT NULL DEFAULT '', ma_so TEXT NOT NULL DEFAULT '')");
        taoCacBangDuLieu(coSoDuLieu);
        taoQuanTriVienMacDinh(coSoDuLieu);
    }

    @Override public void onUpgrade(SQLiteDatabase coSoDuLieu, int phienBanCu, int phienBanMoi) {
        // Thêm cấu trúc còn thiếu và giữ nguyên các bảng dữ liệu cũ.
        if (bangTonTai(coSoDuLieu, BANG_TAI_KHOAN)) {
            themCotNeuChuaCo(coSoDuLieu, BANG_TAI_KHOAN, "ho_ten", "TEXT NOT NULL DEFAULT ''");
            themCotNeuChuaCo(coSoDuLieu, BANG_TAI_KHOAN, "ma_so", "TEXT NOT NULL DEFAULT ''");
        }
        if (bangTonTai(coSoDuLieu, BANG_BAI_TAP)) {
            themCotNeuChuaCo(coSoDuLieu, BANG_BAI_TAP, "tep_uri", "TEXT NOT NULL DEFAULT ''");
            themCotNeuChuaCo(coSoDuLieu, BANG_BAI_TAP, "tep_ten", "TEXT NOT NULL DEFAULT ''");
        }
        taoCacBangDuLieu(coSoDuLieu);
        taoQuanTriVienMacDinh(coSoDuLieu);
    }

    private void taoCacBangDuLieu(SQLiteDatabase coSoDuLieu) {
        coSoDuLieu.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_account_code ON " + BANG_TAI_KHOAN + "(ma_so) WHERE ma_so <> ''");
        coSoDuLieu.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_LOP_HOC_PHAN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, ma_lop TEXT NOT NULL UNIQUE, ten_hoc_phan TEXT NOT NULL, hoc_ky TEXT NOT NULL DEFAULT '', nam_hoc TEXT NOT NULL DEFAULT '', giao_vien_id INTEGER NOT NULL, trang_thai TEXT NOT NULL DEFAULT 'ACTIVE', tao_luc INTEGER NOT NULL, luu_tru_luc INTEGER, FOREIGN KEY(giao_vien_id) REFERENCES " + BANG_TAI_KHOAN + "(id))");
        coSoDuLieu.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_THANH_VIEN_LOP + " (id INTEGER PRIMARY KEY AUTOINCREMENT, lop_id INTEGER NOT NULL, sinh_vien_id INTEGER NOT NULL, tham_gia_luc INTEGER NOT NULL, UNIQUE(lop_id, sinh_vien_id), FOREIGN KEY(lop_id) REFERENCES " + BANG_LOP_HOC_PHAN + "(id) ON DELETE CASCADE, FOREIGN KEY(sinh_vien_id) REFERENCES " + BANG_TAI_KHOAN + "(id))");
        coSoDuLieu.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_BAI_TAP + " (id INTEGER PRIMARY KEY AUTOINCREMENT, lop_id INTEGER NOT NULL, tieu_de TEXT NOT NULL, yeu_cau TEXT NOT NULL, tep_uri TEXT NOT NULL DEFAULT '', tep_ten TEXT NOT NULL DEFAULT '', han_nop INTEGER NOT NULL, nguoi_tao_id INTEGER NOT NULL, tao_luc INTEGER NOT NULL, cap_nhat_luc INTEGER NOT NULL, trang_thai TEXT NOT NULL DEFAULT 'ACTIVE', FOREIGN KEY(lop_id) REFERENCES " + BANG_LOP_HOC_PHAN + "(id), FOREIGN KEY(nguoi_tao_id) REFERENCES " + BANG_TAI_KHOAN + "(id))");
        coSoDuLieu.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_BAI_NOP + " (id INTEGER PRIMARY KEY AUTOINCREMENT, bai_tap_id INTEGER NOT NULL, sinh_vien_id INTEGER NOT NULL, noi_dung TEXT NOT NULL DEFAULT '', tep_uri TEXT NOT NULL DEFAULT '', tep_ten TEXT NOT NULL DEFAULT '', nop_luc INTEGER NOT NULL, trang_thai TEXT NOT NULL DEFAULT 'SUBMITTED', diem REAL, nhan_xet TEXT NOT NULL DEFAULT '', danh_gia_luc INTEGER, UNIQUE(bai_tap_id, sinh_vien_id), FOREIGN KEY(bai_tap_id) REFERENCES " + BANG_BAI_TAP + "(id), FOREIGN KEY(sinh_vien_id) REFERENCES " + BANG_TAI_KHOAN + "(id))");
        coSoDuLieu.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_VIEC_CA_NHAN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, sinh_vien_id INTEGER NOT NULL, ten_mon TEXT NOT NULL DEFAULT '', tieu_de TEXT NOT NULL, ghi_chu TEXT NOT NULL DEFAULT '', han_nop INTEGER NOT NULL, uu_tien TEXT NOT NULL DEFAULT 'Vừa', trang_thai TEXT NOT NULL DEFAULT 'TODO', tao_luc INTEGER NOT NULL, FOREIGN KEY(sinh_vien_id) REFERENCES " + BANG_TAI_KHOAN + "(id))");
        coSoDuLieu.execSQL("CREATE INDEX IF NOT EXISTS idx_class_teacher ON " + BANG_LOP_HOC_PHAN + "(giao_vien_id, trang_thai)");
        coSoDuLieu.execSQL("CREATE INDEX IF NOT EXISTS idx_member_student ON " + BANG_THANH_VIEN_LOP + "(sinh_vien_id)");
        coSoDuLieu.execSQL("CREATE INDEX IF NOT EXISTS idx_assignment_class ON " + BANG_BAI_TAP + "(lop_id, trang_thai, han_nop)");
        coSoDuLieu.execSQL("CREATE INDEX IF NOT EXISTS idx_submission_student ON " + BANG_BAI_NOP + "(sinh_vien_id, bai_tap_id)");
        coSoDuLieu.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_student ON " + BANG_VIEC_CA_NHAN + "(sinh_vien_id, han_nop)");
    }

    private boolean bangTonTai(SQLiteDatabase coSoDuLieu, String tenBang) {
        Cursor c = coSoDuLieu.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tenBang});
        boolean tonTai = c.moveToFirst();
        c.close();
        return tonTai;
    }
    private void themCotNeuChuaCo(SQLiteDatabase coSoDuLieu, String table, String column, String definition) {
        Cursor c = coSoDuLieu.rawQuery("PRAGMA table_info(" + table + ")", null);
        boolean exists = false;
        while (c.moveToNext()) if (column.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) exists = true;
        c.close();
        if (!exists) coSoDuLieu.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private void taoQuanTriVienMacDinh(SQLiteDatabase coSoDuLieu) {
        Cursor c = coSoDuLieu.rawQuery("SELECT id FROM " + BANG_TAI_KHOAN + " WHERE LOWER(email)=?", new String[]{"admin@gmail.com"});
        boolean exists = c.moveToFirst(); c.close();
        if (!exists) {
            ContentValues v = new ContentValues();
            v.put("email", "admin@gmail.com"); v.put("mat_khau", "admin123");
            v.put("vai_tro", "ADMIN"); v.put("trang_thai", 1); v.put("ho_ten", "Quản trị viên");
            coSoDuLieu.insert(BANG_TAI_KHOAN, null, v);
        }
    }

    public boolean taoTaiKhoanBangQuanTri(int maTaiKhoanThucHien, String email, String matKhau, String vaiTro, String hoTen, String maSo) {
        if (!laQuanTriVien(maTaiKhoanThucHien) || (!"TEACHER".equals(vaiTro) && !"STUDENT".equals(vaiTro))) return false;
        ContentValues v = new ContentValues(); v.put("email", email.trim().toLowerCase(Locale.ROOT)); v.put("mat_khau", matKhau.trim());
        v.put("vai_tro", vaiTro); v.put("trang_thai", 1); v.put("ho_ten", hoTen.trim()); v.put("ma_so", maSo.trim().toUpperCase(Locale.ROOT));
        return getWritableDatabase().insert(BANG_TAI_KHOAN, null, v) != -1;
    }

    public boolean doiTrangThaiTaiKhoan(int maTaiKhoanThucHien, int maTaiKhoanDich, boolean dangHoatDong) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laQuanTriVien(coSoDuLieu, maTaiKhoanThucHien) || maTaiKhoanThucHien == maTaiKhoanDich && !dangHoatDong) return false;
        Cursor c = coSoDuLieu.rawQuery("SELECT vai_tro FROM " + BANG_TAI_KHOAN + " WHERE id=?", new String[]{String.valueOf(maTaiKhoanDich)});
        if (!c.moveToFirst()) { c.close(); return false; }
        String vaiTro = c.getString(0); c.close();
        if ("ADMIN".equals(vaiTro) && !dangHoatDong) {
            c = coSoDuLieu.rawQuery("SELECT COUNT(*) FROM " + BANG_TAI_KHOAN + " WHERE vai_tro='ADMIN' AND trang_thai=1", null);
            boolean lastAdmin = c.moveToFirst() && c.getInt(0) <= 1; c.close();
            if (lastAdmin) return false;
        }
        ContentValues v = new ContentValues(); v.put("trang_thai", dangHoatDong ? 1 : 0);
        return coSoDuLieu.update(BANG_TAI_KHOAN, v, "id=?", new String[]{String.valueOf(maTaiKhoanDich)}) > 0;
    }

    public boolean datLaiMatKhau(int maTaiKhoanThucHien, int maTaiKhoanDich, String matKhau) {
        if (!laQuanTriVien(maTaiKhoanThucHien)) return false;
        ContentValues v = new ContentValues(); v.put("mat_khau", matKhau.trim());
        return getWritableDatabase().update(BANG_TAI_KHOAN, v, "id=? AND vai_tro<>'ADMIN'", new String[]{String.valueOf(maTaiKhoanDich)}) > 0;
    }

    public boolean taoLop(int maTaiKhoanThucHien, String maSo, String tenMon, String hocKy, String namHoc, int maGiaoVien) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laQuanTriVien(coSoDuLieu, maTaiKhoanThucHien) || !laTaiKhoanHoatDong(coSoDuLieu, maGiaoVien, "TEACHER")) return false;
        ContentValues v = new ContentValues(); v.put("ma_lop", maSo.trim().toUpperCase(Locale.ROOT)); v.put("ten_hoc_phan", tenMon.trim());
        v.put("hoc_ky", hocKy.trim()); v.put("nam_hoc", namHoc.trim()); v.put("giao_vien_id", maGiaoVien);
        v.put("tao_luc", System.currentTimeMillis());
        return coSoDuLieu.insert(BANG_LOP_HOC_PHAN, null, v) != -1;
    }

    public boolean doiGiaoVien(int maTaiKhoanThucHien, int maLop, int maGiaoVien) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laQuanTriVien(coSoDuLieu, maTaiKhoanThucHien) || !laTaiKhoanHoatDong(coSoDuLieu, maGiaoVien, "TEACHER")) return false;
        ContentValues v = new ContentValues(); v.put("giao_vien_id", maGiaoVien);
        return coSoDuLieu.update(BANG_LOP_HOC_PHAN, v, "id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(maLop)}) > 0;
    }

    public boolean themSinhVienVaoLop(int maTaiKhoanThucHien, int maLop, int maSinhVien) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laQuanTriVien(coSoDuLieu, maTaiKhoanThucHien) || !laTaiKhoanHoatDong(coSoDuLieu, maSinhVien, "STUDENT") || !lopDangHoatDong(coSoDuLieu, maLop)) return false;
        ContentValues v = new ContentValues(); v.put("lop_id", maLop); v.put("sinh_vien_id", maSinhVien); v.put("tham_gia_luc", System.currentTimeMillis());
        return coSoDuLieu.insert(BANG_THANH_VIEN_LOP, null, v) != -1;
    }

    public int themNhieuSinhVienVaoLop(int maTaiKhoanThucHien, int maLop, ArrayList<Integer> danhSachSinhVien) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laQuanTriVien(coSoDuLieu, maTaiKhoanThucHien) || !lopDangHoatDong(coSoDuLieu, maLop)) return 0;
        int soSinhVienDaThem = 0;
        coSoDuLieu.beginTransaction();
        try {
            for (Integer maSinhVien : danhSachSinhVien) {
                if (maSinhVien == null || !laTaiKhoanHoatDong(coSoDuLieu, maSinhVien, "STUDENT")) continue;
                ContentValues v = new ContentValues();
                v.put("lop_id", maLop); v.put("sinh_vien_id", maSinhVien); v.put("tham_gia_luc", System.currentTimeMillis());
                if (coSoDuLieu.insertWithOnConflict(BANG_THANH_VIEN_LOP, null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1) soSinhVienDaThem++;
            }
            coSoDuLieu.setTransactionSuccessful();
        } finally { coSoDuLieu.endTransaction(); }
        return soSinhVienDaThem;
    }

    public boolean xoaSinhVienKhoiLop(int maTaiKhoanThucHien, int maLop, int maSinhVien) {
        if (!laQuanTriVien(maTaiKhoanThucHien) || !lopDangHoatDong(getReadableDatabase(), maLop)) return false;
        return getWritableDatabase().delete(BANG_THANH_VIEN_LOP, "lop_id=? AND sinh_vien_id=?", new String[]{String.valueOf(maLop), String.valueOf(maSinhVien)}) > 0;
    }

    public boolean luuTruLop(int maTaiKhoanThucHien, int maLop) {
        if (!laQuanTriVien(maTaiKhoanThucHien)) return false;
        ContentValues v = new ContentValues(); v.put("trang_thai", "ARCHIVED"); v.put("luu_tru_luc", System.currentTimeMillis());
        return getWritableDatabase().update(BANG_LOP_HOC_PHAN, v, "id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(maLop)}) > 0;
    }

    public boolean taoBaiTap(int maGiaoVien, int maLop, String tieuDe, String yeuCau, String tepUri, String tepTen, long hanNop) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!giaoVienPhuTrachLop(coSoDuLieu, maGiaoVien, maLop)) return false;
        ContentValues v = new ContentValues(); v.put("lop_id", maLop); v.put("tieu_de", tieuDe.trim());
        v.put("yeu_cau", yeuCau.trim()); v.put("tep_uri", tepUri); v.put("tep_ten", tepTen); v.put("han_nop", hanNop); v.put("nguoi_tao_id", maGiaoVien);
        v.put("tao_luc", System.currentTimeMillis()); v.put("cap_nhat_luc", System.currentTimeMillis());
        return coSoDuLieu.insert(BANG_BAI_TAP, null, v) != -1;
    }

    public boolean suaBaiTap(int maGiaoVien, int maBaiTap, String tieuDe, String yeuCau, String tepUri, String tepTen, long hanNop) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!giaoVienPhuTrachBaiTap(coSoDuLieu, maGiaoVien, maBaiTap)) return false;
        ContentValues v = new ContentValues(); v.put("tieu_de", tieuDe.trim()); v.put("yeu_cau", yeuCau.trim());
        v.put("tep_uri", tepUri); v.put("tep_ten", tepTen);
        v.put("han_nop", hanNop); v.put("cap_nhat_luc", System.currentTimeMillis());
        return coSoDuLieu.update(BANG_BAI_TAP, v, "id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(maBaiTap)}) > 0;
    }

    public boolean thuHoiBaiTap(int maGiaoVien, int maBaiTap) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!giaoVienPhuTrachBaiTap(coSoDuLieu, maGiaoVien, maBaiTap)) return false;
        ContentValues v = new ContentValues(); v.put("trang_thai", "WITHDRAWN"); v.put("cap_nhat_luc", System.currentTimeMillis());
        return coSoDuLieu.update(BANG_BAI_TAP, v, "id=?", new String[]{String.valueOf(maBaiTap)}) > 0;
    }

    public boolean nopBai(int maSinhVien, int maBaiTap, String cauTraLoi, String uriTep, String tenTep) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        Cursor c = coSoDuLieu.rawQuery("SELECT a.han_nop,s.trang_thai FROM " + BANG_BAI_TAP + " a JOIN " + BANG_LOP_HOC_PHAN + " l ON l.id=a.lop_id AND l.trang_thai='ACTIVE' JOIN " + BANG_THANH_VIEN_LOP + " m ON m.lop_id=a.lop_id JOIN " + BANG_TAI_KHOAN + " u ON u.id=m.sinh_vien_id AND u.vai_tro='STUDENT' AND u.trang_thai=1 LEFT JOIN " + BANG_BAI_NOP + " s ON s.bai_tap_id=a.id AND s.sinh_vien_id=m.sinh_vien_id WHERE a.id=? AND m.sinh_vien_id=? AND a.trang_thai='ACTIVE'", new String[]{String.valueOf(maBaiTap), String.valueOf(maSinhVien)});
        if (!c.moveToFirst()) { c.close(); return false; }
        long due = c.getLong(0); String trangThaiCu = c.getString(1); c.close();
        long now = System.currentTimeMillis();
        boolean duocNop = trangThaiCu == null || "RESUBMIT".equals(trangThaiCu)
                || now < due && ("SUBMITTED".equals(trangThaiCu) || "LATE".equals(trangThaiCu) || "GRADED".equals(trangThaiCu));
        if (!duocNop || cauTraLoi.trim().isEmpty() && uriTep.isEmpty()) return false;
        ContentValues v = new ContentValues(); v.put("bai_tap_id", maBaiTap); v.put("sinh_vien_id", maSinhVien);
        v.put("noi_dung", cauTraLoi); v.put("tep_uri", uriTep); v.put("tep_ten", tenTep); v.put("nop_luc", now);
        v.put("trang_thai", now > due ? "LATE" : "SUBMITTED"); v.putNull("diem"); v.put("nhan_xet", ""); v.putNull("danh_gia_luc");
        return coSoDuLieu.insertWithOnConflict(BANG_BAI_NOP, null, v, SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    public boolean danhGiaBaiNop(int maGiaoVien, int maBaiNop, double diem, String nhanXet, boolean yeuCauNopLai) {
        SQLiteDatabase coSoDuLieu = getWritableDatabase();
        if (!laTaiKhoanHoatDong(coSoDuLieu, maGiaoVien, "TEACHER")) return false;
        if (!yeuCauNopLai && (Double.isNaN(diem) || Double.isInfinite(diem) || diem < 0 || diem > 10)) return false;
        Cursor c = coSoDuLieu.rawQuery("SELECT s.id FROM " + BANG_BAI_NOP + " s JOIN " + BANG_BAI_TAP + " a ON a.id=s.bai_tap_id AND a.trang_thai='ACTIVE' JOIN " + BANG_LOP_HOC_PHAN + " l ON l.id=a.lop_id AND l.trang_thai='ACTIVE' WHERE s.id=? AND l.giao_vien_id=?", new String[]{String.valueOf(maBaiNop), String.valueOf(maGiaoVien)});
        boolean allowed = c.moveToFirst(); c.close(); if (!allowed) return false;
        ContentValues v = new ContentValues(); v.put("trang_thai", yeuCauNopLai ? "RESUBMIT" : "GRADED"); v.put("nhan_xet", nhanXet);
        if (yeuCauNopLai) v.putNull("diem"); else v.put("diem", diem);
        v.put("danh_gia_luc", System.currentTimeMillis());
        return coSoDuLieu.update(BANG_BAI_NOP, v, "id=?", new String[]{String.valueOf(maBaiNop)}) > 0;
    }

    public long taoViecCaNhan(int maSinhVien, String tenMon, String tieuDe, String note, long due, String mucUuTien) {
        if (!laTaiKhoanHoatDong(getReadableDatabase(), maSinhVien, "STUDENT")) return -1;
        ContentValues v = new ContentValues(); v.put("sinh_vien_id", maSinhVien); v.put("ten_mon", tenMon); v.put("tieu_de", tieuDe);
        v.put("ghi_chu", note); v.put("han_nop", due); v.put("uu_tien", mucUuTien); v.put("tao_luc", System.currentTimeMillis());
        return getWritableDatabase().insert(BANG_VIEC_CA_NHAN, null, v);
    }

    public boolean suaViecCaNhan(int maSinhVien, int maViecCaNhan, String tenMon, String tieuDe, String note, long due, String mucUuTien, String trangThai) {
        if (!laTaiKhoanHoatDong(getReadableDatabase(), maSinhVien, "STUDENT")) return false;
        ContentValues v = new ContentValues(); v.put("ten_mon", tenMon); v.put("tieu_de", tieuDe); v.put("ghi_chu", note);
        v.put("han_nop", due); v.put("uu_tien", mucUuTien); v.put("trang_thai", trangThai);
        return getWritableDatabase().update(BANG_VIEC_CA_NHAN, v, "id=? AND sinh_vien_id=?", new String[]{String.valueOf(maViecCaNhan), String.valueOf(maSinhVien)}) > 0;
    }

    public boolean xoaViecCaNhan(int maSinhVien, int maViecCaNhan) {
        if (!laTaiKhoanHoatDong(getReadableDatabase(), maSinhVien, "STUDENT")) return false;
        return getWritableDatabase().delete(BANG_VIEC_CA_NHAN, "id=? AND sinh_vien_id=?", new String[]{String.valueOf(maViecCaNhan), String.valueOf(maSinhVien)}) > 0;
    }

    public TaiKhoan kiemTraDangNhap(String email, String matKhau) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,email,mat_khau,vai_tro,trang_thai FROM " + BANG_TAI_KHOAN + " WHERE LOWER(email)=? AND mat_khau=?", new String[]{email.trim().toLowerCase(), matKhau.trim()});
        TaiKhoan account = docTaiKhoan(c); c.close(); return account;
    }

    public TaiKhoan layTaiKhoanTheoId(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,email,mat_khau,vai_tro,trang_thai FROM " + BANG_TAI_KHOAN + " WHERE id=?", new String[]{String.valueOf(id)});
        TaiKhoan account = docTaiKhoan(c); c.close(); return account;
    }

    private TaiKhoan docTaiKhoan(Cursor c) {
        TaiKhoan a = null;
        if (c.moveToFirst()) a = new TaiKhoan(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4));
        return a;
    }

    public Cursor layDanhSachTaiKhoan() { return getReadableDatabase().rawQuery("SELECT id,email,vai_tro,trang_thai,ho_ten,ma_so FROM " + BANG_TAI_KHOAN + " ORDER BY vai_tro,ho_ten,email", null); }
    public Cursor layGiaoVienHoatDong() { return getReadableDatabase().rawQuery("SELECT id,ho_ten,email FROM " + BANG_TAI_KHOAN + " WHERE vai_tro='TEACHER' AND trang_thai=1 ORDER BY ho_ten,email", null); }
    public Cursor laySinhVienHoatDong() { return getReadableDatabase().rawQuery("SELECT id,ho_ten,email,ma_so FROM " + BANG_TAI_KHOAN + " WHERE vai_tro='STUDENT' AND trang_thai=1 ORDER BY ho_ten,email", null); }

    private boolean laQuanTriVien(int id) { return laQuanTriVien(getReadableDatabase(), id); }
    private boolean laQuanTriVien(SQLiteDatabase coSoDuLieu, int id) { return laTaiKhoanHoatDong(coSoDuLieu, id, "ADMIN"); }
    private boolean laTaiKhoanHoatDong(SQLiteDatabase coSoDuLieu, int id, String vaiTro) {
        Cursor c = coSoDuLieu.rawQuery("SELECT id FROM " + BANG_TAI_KHOAN + " WHERE id=? AND vai_tro=? AND trang_thai=1", new String[]{String.valueOf(id), vaiTro});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    private boolean lopDangHoatDong(SQLiteDatabase coSoDuLieu, int maLop) {
        Cursor c = coSoDuLieu.rawQuery("SELECT id FROM " + BANG_LOP_HOC_PHAN + " WHERE id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(maLop)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    private boolean giaoVienPhuTrachLop(SQLiteDatabase coSoDuLieu, int maGiaoVien, int maLop) {
        if (!laTaiKhoanHoatDong(coSoDuLieu, maGiaoVien, "TEACHER")) return false;
        Cursor c = coSoDuLieu.rawQuery("SELECT id FROM " + BANG_LOP_HOC_PHAN + " WHERE id=? AND giao_vien_id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(maLop), String.valueOf(maGiaoVien)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    private boolean giaoVienPhuTrachBaiTap(SQLiteDatabase coSoDuLieu, int maGiaoVien, int maBaiTap) {
        if (!laTaiKhoanHoatDong(coSoDuLieu, maGiaoVien, "TEACHER")) return false;
        Cursor c = coSoDuLieu.rawQuery("SELECT a.id FROM " + BANG_BAI_TAP + " a JOIN " + BANG_LOP_HOC_PHAN + " l ON l.id=a.lop_id WHERE a.id=? AND l.giao_vien_id=? AND l.trang_thai='ACTIVE'", new String[]{String.valueOf(maBaiTap), String.valueOf(maGiaoVien)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
}
