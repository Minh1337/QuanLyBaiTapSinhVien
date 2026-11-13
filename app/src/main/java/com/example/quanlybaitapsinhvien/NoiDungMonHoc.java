package com.example.quanlybaitapsinhvien;

/**
 * Model biểu diễn Nội dung môn học (Bài học, Video, Link, Trắc nghiệm, Nộp bài tập)
 */
public class NoiDungMonHoc {
    public static final String LOAI_PDF = "PDF";
    public static final String LOAI_VIDEO = "VIDEO";
    public static final String LOAI_LINK = "LINK";
    public static final String LOAI_NOP_BAI = "NOP_BAI";
    public static final String LOAI_TRAC_NGHIEM = "TRAC_NGHIEM";

    private int id;
    private int idMonHoc;
    private String tieuDe;
    private String loaiNoiDung;
    private String urlFile;
    private String phanCum;
    private int idThamChieu;
    private boolean hoanThanh;
    private String moTa;
    private String hanNop;

    public NoiDungMonHoc() {
    }

    public NoiDungMonHoc(int id, int idMonHoc, String tieuDe, String loaiNoiDung, String urlFile, String phanCum, int idThamChieu, boolean hoanThanh, String moTa, String hanNop) {
        this.id = id;
        this.idMonHoc = idMonHoc;
        this.tieuDe = tieuDe;
        this.loaiNoiDung = loaiNoiDung;
        this.urlFile = urlFile;
        this.phanCum = phanCum;
        this.idThamChieu = idThamChieu;
        this.hoanThanh = hoanThanh;
        this.moTa = moTa;
        this.hanNop = hanNop;
    }

    public NoiDungMonHoc(int id, int idMonHoc, String tieuDe, String loaiNoiDung, String urlFile, String phanCum, int idThamChieu, boolean hoanThanh) {
        this(id, idMonHoc, tieuDe, loaiNoiDung, urlFile, phanCum, idThamChieu, hoanThanh, "", "");
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

    public String getLoaiNoiDung() {
        return loaiNoiDung;
    }

    public void setLoaiNoiDung(String loaiNoiDung) {
        this.loaiNoiDung = loaiNoiDung;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public String getPhanCum() {
        return phanCum;
    }

    public void setPhanCum(String phanCum) {
        this.phanCum = phanCum;
    }

    public int getIdThamChieu() {
        return idThamChieu;
    }

    public void setIdThamChieu(int idThamChieu) {
        this.idThamChieu = idThamChieu;
    }

    public boolean isHoanThanh() {
        return hoanThanh;
    }

    public void setHoanThanh(boolean hoanThanh) {
        this.hoanThanh = hoanThanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getHanNop() {
        return hanNop;
    }

    public void setHanNop(String hanNop) {
        this.hanNop = hanNop;
    }
}
