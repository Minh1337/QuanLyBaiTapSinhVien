package com.example.quanlybaitapsinhvien;

/**
 * Model biểu diễn thông tin Tài khoản người dùng
 */
public class TaiKhoan {
    private int id;
    private String email;
    private String matKhau;
    private String vaiTro; // "ADMIN", "TEACHER", "STUDENT"
    private int trangThai; // 1: Hoạt động, 0: Bị khóa

    public TaiKhoan() {
    }

    public TaiKhoan(int id, String email, String matKhau, String vaiTro, int trangThai) {
        this.id = id;
        this.email = email;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public int getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(int trangThai) {
        this.trangThai = trangThai;
    }
}
