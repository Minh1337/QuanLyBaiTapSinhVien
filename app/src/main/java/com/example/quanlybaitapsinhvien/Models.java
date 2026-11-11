package com.example.quanlybaitapsinhvien;

/**
 * Tổng hợp các Data Model cơ bản trong hệ thống Quản lý học tập
 */
public class Models {
}

/**
 * Model biểu diễn thông tin Tài khoản người dùng trong hệ thống
 */
class TaiKhoan {
    private int id;
    private String username;
    private String email;
    private String matKhau;
    private String hoTen;
    private String vaiTro; // "ADMIN", "TEACHER", "STUDENT"
    private int trangThai; // 1: Hoạt động, 0: Bị khóa

    public TaiKhoan() {
    }

    public TaiKhoan(int id, String username, String email, String matKhau, String hoTen, String vaiTro, int trangThai) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

/**
 * Model biểu diễn Lớp học phần
 */
class LopHoc {
    private int id;
    private String maLop;
    private String tenLop;
    private int maGv;
    private String moTa;
    private int trangThaiLuuTru; // 0: Đang mở, 1: Đã lưu trữ
    private String tenGiaoVien; // Tên hiển thị giáo viên (helper)

    public LopHoc() {
    }

    public LopHoc(int id, String maLop, String tenLop, int maGv, String moTa, int trangThaiLuuTru) {
        this.id = id;
        this.maLop = maLop;
        this.tenLop = tenLop;
        this.maGv = maGv;
        this.moTa = moTa;
        this.trangThaiLuuTru = trangThaiLuuTru;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public int getMaGv() {
        return maGv;
    }

    public void setMaGv(int maGv) {
        this.maGv = maGv;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getTrangThaiLuuTru() {
        return trangThaiLuuTru;
    }

    public void setTrangThaiLuuTru(int trangThaiLuuTru) {
        this.trangThaiLuuTru = trangThaiLuuTru;
    }

    public String getTenGiaoVien() {
        return tenGiaoVien;
    }

    public void setTenGiaoVien(String tenGiaoVien) {
        this.tenGiaoVien = tenGiaoVien;
    }
}

/**
 * Model biểu diễn Môn học cá nhân của Sinh viên
 */
class MonHocCaNhan {
    private int id;
    private int sinhVienId;
    private String tenMon;
    private String maMon;
    private String moTa;

    public MonHocCaNhan() {
    }

    public MonHocCaNhan(int id, int sinhVienId, String tenMon, String maMon, String moTa) {
        this.id = id;
        this.sinhVienId = sinhVienId;
        this.tenMon = tenMon;
        this.maMon = maMon;
        this.moTa = moTa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSinhVienId() {
        return sinhVienId;
    }

    public void setSinhVienId(int sinhVienId) {
        this.sinhVienId = sinhVienId;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

/**
 * Model biểu diễn Bài tập (Bao gồm bài Giáo viên giao và Bài cá nhân)
 */
class BaiTap {
    private int id;
    private Integer maLopId; // nullable
    private Integer monHocCaNhanId; // nullable
    private int nguoiTaoId;
    private String tenBaiTap;
    private String moTa;
    private String hanNop;
    private String ngayTao;
    private String nguonBai; // "TEACHER" hoặc "PERSONAL"

    // Thuộc tính helper khi truy vấn hiển thị
    private String tenNguon; // Tên lớp học hoặc tên môn cá nhân
    private int progressPercent; // % Tiến độ sinh viên
    private String trangThai; // "Chưa làm", "Đang làm", "Hoàn thành"
    private String mucUuTien; // "Bình thường", "Cao", "Khẩn cấp"
    private boolean overdue; // Bài đã quá hạn hay chưa
    private int nhacHan; // 1: Bật nhắc hạn, 0: Tắt

    public BaiTap() {
    }

    public BaiTap(int id, Integer maLopId, Integer monHocCaNhanId, int nguoiTaoId, String tenBaiTap, String moTa, String hanNop, String ngayTao, String nguonBai) {
        this.id = id;
        this.maLopId = maLopId;
        this.monHocCaNhanId = monHocCaNhanId;
        this.nguoiTaoId = nguoiTaoId;
        this.tenBaiTap = tenBaiTap;
        this.moTa = moTa;
        this.hanNop = hanNop;
        this.ngayTao = ngayTao;
        this.nguonBai = nguonBai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMaLopId() {
        return maLopId;
    }

    public void setMaLopId(Integer maLopId) {
        this.maLopId = maLopId;
    }

    public Integer getMonHocCaNhanId() {
        return monHocCaNhanId;
    }

    public void setMonHocCaNhanId(Integer monHocCaNhanId) {
        this.monHocCaNhanId = monHocCaNhanId;
    }

    public int getNguoiTaoId() {
        return nguoiTaoId;
    }

    public void setNguoiTaoId(int nguoiTaoId) {
        this.nguoiTaoId = nguoiTaoId;
    }

    public String getTenBaiTap() {
        return tenBaiTap;
    }

    public void setTenBaiTap(String tenBaiTap) {
        this.tenBaiTap = tenBaiTap;
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

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNguonBai() {
        return nguonBai;
    }

    public void setNguonBai(String nguonBai) {
        this.nguonBai = nguonBai;
    }

    public String getTenNguon() {
        return tenNguon;
    }

    public void setTenNguon(String tenNguon) {
        this.tenNguon = tenNguon;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMucUuTien() {
        return mucUuTien;
    }

    public void setMucUuTien(String mucUuTien) {
        this.mucUuTien = mucUuTien;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public int getNhacHan() {
        return nhacHan;
    }

    public void setNhacHan(int nhacHan) {
        this.nhacHan = nhacHan;
    }
}

/**
 * Model biểu diễn Tiến độ sinh viên làm bài tập
 */
class TienDoSinhVien {
    private int id;
    private int baiTapId;
    private int sinhVienId;
    private String trangThai; // "Chưa làm", "Đang làm", "Hoàn thành"
    private String mucUuTien; // "Bình thường", "Cao", "Khẩn cấp"
    private int percentTienDo; // 0 -> 100
    private int nhacHan; // 1: Bật, 0: Tắt

    // Helper
    private String tenSinhVien;

    public TienDoSinhVien() {
    }

    public TienDoSinhVien(int id, int baiTapId, int sinhVienId, String trangThai, String mucUuTien, int percentTienDo, int nhacHan) {
        this.id = id;
        this.baiTapId = baiTapId;
        this.sinhVienId = sinhVienId;
        this.trangThai = trangThai;
        this.mucUuTien = mucUuTien;
        this.percentTienDo = percentTienDo;
        this.nhacHan = nhacHan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaiTapId() {
        return baiTapId;
    }

    public void setBaiTapId(int baiTapId) {
        this.baiTapId = baiTapId;
    }

    public int getSinhVienId() {
        return sinhVienId;
    }

    public void setSinhVienId(int sinhVienId) {
        this.sinhVienId = sinhVienId;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMucUuTien() {
        return mucUuTien;
    }

    public void setMucUuTien(String mucUuTien) {
        this.mucUuTien = mucUuTien;
    }

    public int getPercentTienDo() {
        return percentTienDo;
    }

    public void setPercentTienDo(int percentTienDo) {
        this.percentTienDo = percentTienDo;
    }

    public int getNhacHan() {
        return nhacHan;
    }

    public void setNhacHan(int nhacHan) {
        this.nhacHan = nhacHan;
    }

    public String getTenSinhVien() {
        return tenSinhVien;
    }

    public void setTenSinhVien(String tenSinhVien) {
        this.tenSinhVien = tenSinhVien;
    }
}

/**
 * Model biểu diễn Mục Checklist công việc con của bài tập
 */
class ChecklistItem {
    private int id;
    private int tienDoId;
    private String noiDung;
    private int daHoanThanh; // 0: Chưa xong, 1: Đã xong

    public ChecklistItem() {
    }

    public ChecklistItem(int id, int tienDoId, String noiDung, int daHoanThanh) {
        this.id = id;
        this.tienDoId = tienDoId;
        this.noiDung = noiDung;
        this.daHoanThanh = daHoanThanh;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTienDoId() {
        return tienDoId;
    }

    public void setTienDoId(int tienDoId) {
        this.tienDoId = tienDoId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public int getDaHoanThanh() {
        return daHoanThanh;
    }

    public void setDaHoanThanh(int daHoanThanh) {
        this.daHoanThanh = daHoanThanh;
    }

    public boolean isChecked() {
        return daHoanThanh == 1;
    }
}
