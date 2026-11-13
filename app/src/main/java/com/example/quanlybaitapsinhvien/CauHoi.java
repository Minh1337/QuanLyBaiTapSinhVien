package com.example.quanlybaitapsinhvien;

/**
 * Model biểu diễn Câu hỏi trắc nghiệm
 */
public class CauHoi {
    public static final String LOAI_SINGLE = "SINGLE";
    public static final String LOAI_MULTI = "MULTI";

    private int id;
    private int idDeThi;
    private String noiDung;
    private String anhPath;
    private String loaiCauHoi;
    private String dapAnA;
    private String dapAnB;
    private String dapAnC;
    private String dapAnD;
    private String dapAnDung;

    public CauHoi() {
    }

    public CauHoi(int id, int idDeThi, String noiDung, String anhPath, String loaiCauHoi, String dapAnA, String dapAnB, String dapAnC, String dapAnD, String dapAnDung) {
        this.id = id;
        this.idDeThi = idDeThi;
        this.noiDung = noiDung;
        this.anhPath = anhPath;
        this.loaiCauHoi = loaiCauHoi;
        this.dapAnA = dapAnA;
        this.dapAnB = dapAnB;
        this.dapAnC = dapAnC;
        this.dapAnD = dapAnD;
        this.dapAnDung = dapAnDung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDeThi() {
        return idDeThi;
    }

    public void setIdDeThi(int idDeThi) {
        this.idDeThi = idDeThi;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getAnhPath() {
        return anhPath;
    }

    public void setAnhPath(String anhPath) {
        this.anhPath = anhPath;
    }

    public String getLoaiCauHoi() {
        return loaiCauHoi;
    }

    public void setLoaiCauHoi(String loaiCauHoi) {
        this.loaiCauHoi = loaiCauHoi;
    }

    public String getDapAnA() {
        return dapAnA;
    }

    public void setDapAnA(String dapAnA) {
        this.dapAnA = dapAnA;
    }

    public String getDapAnB() {
        return dapAnB;
    }

    public void setDapAnB(String dapAnB) {
        this.dapAnB = dapAnB;
    }

    public String getDapAnC() {
        return dapAnC;
    }

    public void setDapAnC(String dapAnC) {
        this.dapAnC = dapAnC;
    }

    public String getDapAnD() {
        return dapAnD;
    }

    public void setDapAnD(String dapAnD) {
        this.dapAnD = dapAnD;
    }

    public String getDapAnDung() {
        return dapAnDung;
    }

    public void setDapAnDung(String dapAnDung) {
        this.dapAnDung = dapAnDung;
    }
}
