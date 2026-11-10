package com.example.quanlybaitapsinhvien;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lớp Quản lý Phiên đăng nhập (SessionManager)
 * Lưu trữ và khôi phục trạng thái tài khoản người dùng bằng SharedPreferences
 */
public class SessionManager {

    private static final String TEN_PREF = "QuanLyHocTapSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(TEN_PREF, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Tạo và lưu phiên đăng nhập của người dùng vào SharedPreferences
     * @param userId ID tài khoản
     * @param email Email tài khoản
     * @param vaiTro Vai trò ("ADMIN", "TEACHER", "STUDENT")
     */
    public void taoPhienDangNhap(int userId, String email, String vaiTro) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, vaiTro);
        editor.apply();
    }

    /**
     * Kiểm tra xem hiện tại có phiên đăng nhập nào đang hoạt động không
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    public boolean daDangNhap() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy ID tài khoản người dùng đang đăng nhập
     * @return ID người dùng (-1 nếu chưa đăng nhập)
     */
    public int layUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    /**
     * Lấy Email người dùng đang đăng nhập
     * @return Chuỗi email người dùng
     */
    public String layUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Lấy Vai trò (Role) của người dùng đang đăng nhập
     * @return Chuỗi vai trò ("ADMIN", "TEACHER", "STUDENT")
     */
    public String layUserVaiTro() {
        return pref.getString(KEY_USER_ROLE, "STUDENT");
    }

    /**
     * Xóa sạch phiên đăng nhập khi người dùng bấm Đăng xuất
     */
    public void xoaPhienDangNhap() {
        editor.clear();
        editor.apply();
    }
}
