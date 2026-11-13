package com.example.quanlybaitapsinhvien;

/**
 * Model biểu diễn Đề thi
 */
public class DeThi {
    private int id;
    private int idMonHoc;
    private String tieuDe;
    private int thoiGianPhut;
    private String thoiGianBatDau;
    private String thoiGianKetThuc;

    public DeThi() {
    }

    public DeThi(int id, int idMonHoc, String tieuDe, int thoiGianPhut, String thoiGianBatDau, String thoiGianKetThuc) {
        this.id = id;
        this.idMonHoc = idMonHoc;
        this.tieuDe = tieuDe;
        this.thoiGianPhut = thoiGianPhut;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdMonHoc() {
        return idMonHoc;
    }

    public void setIdMonHoc(int idMonHoc) {
        this.idMonHoc = idMonHoc;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public int getThoiGianPhut() {
        return thoiGianPhut;
    }

    public void setThoiGianPhut(int thoiGianPhut) {
        this.thoiGianPhut = thoiGianPhut;
    }

    public String getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(String thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public String getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(String thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }
}
