package com.example.quanlybaitapsinhvien;

/** Một hàng thông tin dùng trong các danh sách của ứng dụng. */
public class MucDanhSach {
    private final String tieuDe;
    private final String moTa;
    private final String trangThai;
    private final Runnable hanhDong;

    public MucDanhSach(String tieuDe, String moTa, String trangThai, Runnable hanhDong) {
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.hanhDong = hanhDong;
    }

    public String getTieuDe() { return tieuDe; }
    public String getMoTa() { return moTa; }
    public String getTrangThai() { return trangThai; }
    public Runnable getHanhDong() { return hanhDong; }
}
