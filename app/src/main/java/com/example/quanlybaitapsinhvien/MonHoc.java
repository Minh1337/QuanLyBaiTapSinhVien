package com.example.quanlybaitapsinhvien;

/**
 * Model biểu diễn thông tin Môn học
 */
public class MonHoc {
    private int id;
    private String maMon;
    private String tenMon;
    private String namHoc;
    private int idGiaoVien;
    private String tenGiaoVien;

    public MonHoc() {
    }

    public MonHoc(int id, String maMon, String tenMon, String namHoc, int idGiaoVien, String tenGiaoVien) {
        this.id = id;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.namHoc = namHoc;
        this.idGiaoVien = idGiaoVien;
        this.tenGiaoVien = tenGiaoVien;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public int getIdGiaoVien() {
        return idGiaoVien;
    }

    public void setIdGiaoVien(int idGiaoVien) {
        this.idGiaoVien = idGiaoVien;
    }

    public String getTenGiaoVien() {
        return tenGiaoVien;
    }

    public void setTenGiaoVien(String tenGiaoVien) {
        this.tenGiaoVien = tenGiaoVien;
    }
}
