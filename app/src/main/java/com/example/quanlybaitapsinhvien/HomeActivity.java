package com.example.quanlybaitapsinhvien;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Các màn hình và thao tác theo vai trò tài khoản. */
public class HomeActivity extends AppCompatActivity {
    private static final int MAU_CHU = 0xFF38243D, MAU_PHU = 0xFF715D72, MAU_TIM = 0xFF603374;
    private SessionManager quanLyPhien;
    private DatabaseHelper coSoDuLieu;
    private ActivityResultLauncher<Intent> boChonTep, boChonTepBaiTap;
    private String vaiTro = "", email = "", uriTep = "", tenTep = "", cauTraLoiNhap = "";
    private String uriTepBaiTapNhap = "", tenTepBaiTapNhap = "";
    private int maTaiKhoan, maLopHienTai = -1, maBaiTapHienTai = -1;
    private int maBaiNopHienTai = -1;
    private String manHinh = "";
    private final ArrayList<ManHinhTruoc> lichSuManHinh = new ArrayList<>();
    private final ArrayList<MucDanhSach> danhSach = new ArrayList<>();
    private EditText oNhapCauTraLoi;
    private TextView nhanTepBaiTapNhap, nhanTepBaiNopNhap;
    private boolean hienLopLuuTru, dangChoChonTepBaiTap, dangChoChonTepBaiNop;

    private static class ManHinhTruoc {
        String manHinh; int maLop, maBaiTap, maBaiNop;
        ManHinhTruoc(String manHinh, int maLop, int maBaiTap, int maBaiNop) {
            this.manHinh = manHinh; this.maLop = maLop; this.maBaiTap = maBaiTap; this.maBaiNop = maBaiNop;
        }
    }
    private static class LuaChon {
        int ma; String nhan;
        LuaChon(int ma, String nhan) { this.ma = ma; this.nhan = nhan; }
        @Override public String toString() { return nhan; }
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        quanLyPhien = new SessionManager(this); coSoDuLieu = new DatabaseHelper(this);
        boChonTep = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), ketQua -> {
            Intent data = ketQua.getData();
            if (ketQua.getResultCode() == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                uriTep = uri.toString(); tenTep = layTenTep(uri);
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (Exception ignored) { }
                hienThongBao("Đã chọn tệp: " + tenTep);
                capNhatNhanTepBaiNopNhap();
            }
        });
        boChonTepBaiTap = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), ketQua -> {
            Intent data = ketQua.getData();
            if (ketQua.getResultCode() == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                uriTepBaiTapNhap = uri.toString(); tenTepBaiTapNhap = layTenTep(uri);
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (Exception ignored) { }
                capNhatNhanTepBaiTapNhap();
                hienThongBao("Đã chọn tệp bài tập: " + tenTepBaiTapNhap);
            }
        });
        setContentView(R.layout.activity_home);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!quanLyPhien.daDangNhap()) { moManHinhChao(); return; }
        maTaiKhoan = quanLyPhien.layUserId();
        TaiKhoan taiKhoan = coSoDuLieu.layTaiKhoanTheoId(maTaiKhoan);
        if (taiKhoan == null || taiKhoan.getTrangThai() != 1) { quanLyPhien.xoaPhienDangNhap(); moManHinhChao(); return; }
        vaiTro = taiKhoan.getVaiTro(); email = taiKhoan.getEmail();
        if (!"ADMIN".equals(vaiTro) && !"TEACHER".equals(vaiTro) && !"STUDENT".equals(vaiTro)) { quanLyPhien.xoaPhienDangNhap(); moManHinhChao(); return; }
        if(dangChoChonTepBaiTap||dangChoChonTepBaiNop){dangChoChonTepBaiTap=false;dangChoChonTepBaiNop=false;return;}
        if (manHinh.isEmpty()) manHinh = layManHinhTrangChu();
        hienThiManHinh();
    }

    private String layManHinhTrangChu() { return "ADMIN".equals(vaiTro) ? "quan-tri-tong-quan" : "TEACHER".equals(vaiTro) ? "giao-vien-tong-quan" : "sinh-vien-tong-quan"; }
    private void moManHinh(String trangCanMo) { lichSuManHinh.clear(); manHinh = trangCanMo; hienThiManHinh(); }
    private void moChiTiet(String trangCanMo, int maLop, int maBaiTap, int maBaiNop) {
        lichSuManHinh.add(new ManHinhTruoc(manHinh, maLopHienTai, maBaiTapHienTai, maBaiNopHienTai));
        if ("sinh-vien-chi-tiet-bai-tap".equals(trangCanMo) && maBaiTap != maBaiTapHienTai) {
            uriTep = ""; tenTep = ""; cauTraLoiNhap = "";
        }
        manHinh = trangCanMo; maLopHienTai = maLop; maBaiTapHienTai = maBaiTap; maBaiNopHienTai = maBaiNop; hienThiManHinh();
    }
    private void quayLai() {
        if (lichSuManHinh.isEmpty()) { moManHinh(layManHinhTrangChu()); return; }
        ManHinhTruoc previous = lichSuManHinh.remove(lichSuManHinh.size() - 1);
        manHinh = previous.manHinh; maLopHienTai = previous.maLop; maBaiTapHienTai = previous.maBaiTap; maBaiNopHienTai = previous.maBaiNop; hienThiManHinh();
    }
    @Override public void onBackPressed() { if (!lichSuManHinh.isEmpty()) quayLai(); else super.onBackPressed(); }

    private LinearLayout hangNut, thanhDieuHuong, hangLoc, vungNhapThem, actionBottom;
    private ListView oDanhSach;
    private MucDanhSachAdapter danhSachAdapter;
    private TextView tieuDeTrang, nhanAnhBia, tieuDeAnhBia, moTaAnhBia, tieuDeMuc;
    private void hienThiManHinh() {
        hangNut = findViewById(R.id.actionRow); oDanhSach = findViewById(R.id.contentList); thanhDieuHuong = findViewById(R.id.bottomNav);
        hangLoc = findViewById(R.id.hangLoc); vungNhapThem = findViewById(R.id.vungNhapThem); actionBottom = findViewById(R.id.actionBottom);
        tieuDeTrang = findViewById(R.id.tvHomeTitle); nhanAnhBia = findViewById(R.id.nhanAnhBia);
        tieuDeAnhBia = findViewById(R.id.tieuDeAnhBia); moTaAnhBia = findViewById(R.id.moTaAnhBia);
        tieuDeMuc = findViewById(R.id.tvSectionTitle);
        hangNut.removeAllViews(); thanhDieuHuong.removeAllViews(); hangLoc.removeAllViews(); vungNhapThem.removeAllViews(); actionBottom.removeAllViews();
        actionBottom.setVisibility(View.GONE); oNhapCauTraLoi=null; nhanTepBaiNopNhap=null;
        hangLoc.setVisibility(View.GONE); danhSach.clear();
        danhSachAdapter = new MucDanhSachAdapter(this, danhSach); oDanhSach.setAdapter(danhSachAdapter);
        oDanhSach.setOnItemClickListener((khungCha, row, position, id) -> {
            Runnable action = danhSach.get(position).getHanhDong();
            if (action != null) action.run();
        });
        String tenVaiTro = "ADMIN".equals(vaiTro) ? "Quản trị viên" : "TEACHER".equals(vaiTro) ? "Giáo viên" : "Sinh viên";
        ((TextView) findViewById(R.id.tvRole)).setText(tenVaiTro + " · " + email);
        findViewById(R.id.btnLogout).setOnClickListener(v -> dangXuat());
        findViewById(R.id.btnLogout).setContentDescription("Đăng xuất");
        switch (manHinh) {
            case "quan-tri-tai-khoan": hienThiTaiKhoan(); break;
            case "quan-tri-lop": hienThiLopQuanTri(); break;
            case "quan-tri-chi-tiet-lop": hienThiChiTietLopQuanTri(); break;
            case "giao-vien-lop": hienThiLopGiangDay(); break;
            case "giao-vien-chi-tiet-lop": hienThiChiTietLopGiangDay(); break;
            case "giao-vien-theo-doi-bai": hienThiTheoDoiBaiTap(); break;
            case "giao-vien-chi-tiet-bai-nop": hienThiBaiNop(); break;
            case "sinh-vien-lop": hienThiLopSinhVien(); break;
            case "sinh-vien-chi-tiet-lop": hienThiChiTietLopSinhVien(); break;
            case "sinh-vien-chi-tiet-bai-tap": hienThiChiTietBaiTapSinhVien(); break;
            case "sinh-vien-viec-ca-nhan": hienThiViecCaNhan(); break;
            case "sinh-vien-lich-tuan": hienThiLichTuan(); break;
            case "sinh-vien-thong-ke": hienThiThongKe(); break;
            default: hienThiTongQuanTheoVaiTro(); break;
        }
    }

    private void hienThiTongQuanTheoVaiTro() {
        if ("ADMIN".equals(vaiTro)) hienThiTongQuanQuanTri();
        else if ("TEACHER".equals(vaiTro)) hienThiTongQuanGiaoVien();
        else hienThiTongQuanSinhVien();
    }

    private void hienThiTongQuanQuanTri() {
        datTieuDe("Quản trị ứng dụng", "TỔNG QUAN HỆ THỐNG", "Quản lý lớp học phần", "Tạo lớp, cấp tài khoản và theo dõi quy mô hệ thống.", "Điều hành ứng dụng");
        SQLiteDatabase d = coSoDuLieu.getReadableDatabase();
        themMucDanhSach("Tài khoản", demBanGhi(d, "SELECT COUNT(*) FROM tai_khoan WHERE vai_tro<>'ADMIN'") + " tài khoản", "Giáo viên và sinh viên", () -> moManHinh("quan-tri-tai-khoan"));
        themMucDanhSach("Lớp đang hoạt động", demBanGhi(d, "SELECT COUNT(*) FROM lop_hoc_phan WHERE trang_thai='ACTIVE'") + " lớp", "Quản lý lớp, giáo viên và sinh viên", () -> moManHinh("quan-tri-lop"));
        themMucDanhSach("Lớp đã lưu trữ", demBanGhi(d, "SELECT COUNT(*) FROM lop_hoc_phan WHERE trang_thai='ARCHIVED'") + " lớp", "Lịch sử lớp được giữ lại", () -> { hienLopLuuTru = true; moManHinh("quan-tri-lop"); });
        themNutThaoTac(hangNut, "Xếp lớp / quản lý lớp", () -> moManHinh("quan-tri-lop"));
        themNutThaoTac(hangNut, "Cấp tài khoản", this::hienTaoTaiKhoan);
        themNutDieuHuong("Tổng quan", () -> moManHinh("quan-tri-tong-quan")); themNutDieuHuong("Tài khoản", () -> moManHinh("quan-tri-tai-khoan")); themNutDieuHuong("Lớp & xếp lớp", () -> moManHinh("quan-tri-lop"));
    }

    private void hienThiTaiKhoan() {
        datTieuDe("Tài khoản", "QUẢN LÝ NGƯỜI DÙNG", "Cấp và quản lý quyền truy cập", "Chỉ quản trị viên mới tạo tài khoản giáo viên hoặc sinh viên.", "Danh sách tài khoản");
        themNutThaoTac(hangNut, "+ Tạo tài khoản", this::hienTaoTaiKhoan);
        Cursor c = coSoDuLieu.layDanhSachTaiKhoan();
        while (c.moveToNext()) {
            int id = c.getInt(0); String address = c.getString(1), r = c.getString(2); boolean active = c.getInt(3) == 1;
            String ten = c.getString(4), code = c.getString(5);
            if ("ADMIN".equals(r)) continue;
            themMucDanhSach(tenHienThi(ten, address), ("TEACHER".equals(r) ? "Giáo viên" : "Sinh viên") + (code.isEmpty() ? "" : " · " + code), active ? "Đang hoạt động" : "Đã khóa", () -> hienThiThaoTacTaiKhoan(id, tenHienThi(ten, address), active));
        }
        c.close(); if (danhSach.isEmpty()) themDanhSachRong("Chưa có tài khoản được cấp.");
        themNutDieuHuong("Tổng quan", () -> moManHinh("quan-tri-tong-quan")); themNutDieuHuong("Tài khoản", () -> moManHinh("quan-tri-tai-khoan")); themNutDieuHuong("Lớp & xếp lớp", () -> moManHinh("quan-tri-lop"));
    }

    private void hienThiLopQuanTri() {
        datTieuDe(hienLopLuuTru ? "Lớp đã lưu trữ" : "Lớp học phần", "QUẢN LÝ LỚP", "Xếp sinh viên và phân công giáo viên", "Tạo lớp, chọn giáo viên phụ trách, mở lớp để thêm hoặc gỡ sinh viên.", "Danh sách lớp");
        themNutThaoTac(hangNut, hienLopLuuTru ? "Xem lớp đang mở" : "+ Tạo lớp", hienLopLuuTru ? () -> { hienLopLuuTru = false; hienThiManHinh(); } : this::hienTaoLop);
        themNutThaoTac(hangNut, hienLopLuuTru ? "Quay lại" : "Lớp lưu trữ", hienLopLuuTru ? () -> { hienLopLuuTru = false; hienThiManHinh(); } : () -> { hienLopLuuTru = true; hienThiManHinh(); });
        String status = hienLopLuuTru ? "ARCHIVED" : "ACTIVE";
        Cursor c = coSoDuLieu.getReadableDatabase().rawQuery("SELECT l.id,l.ma_lop,l.ten_hoc_phan,l.hoc_ky,l.nam_hoc,a.ho_ten,a.email,(SELECT COUNT(*) FROM thanh_vien_lop m WHERE m.lop_id=l.id) FROM lop_hoc_phan l JOIN tai_khoan a ON a.id=l.giao_vien_id WHERE l.trang_thai=? ORDER BY l.ten_hoc_phan", new String[]{status});
        while (c.moveToNext()) {
            int id = c.getInt(0); String code = c.getString(1), subject = c.getString(2), term = c.getString(3), year = c.getString(4);
            String giaoVien = tenHienThi(c.getString(5), c.getString(6)); int members = c.getInt(7);
            themMucDanhSach(subject + " · " + code, giaoVien + " · " + members + " sinh viên", term + (year.isEmpty() ? "" : " · " + year), () -> moChiTiet("quan-tri-chi-tiet-lop", id, -1, -1));
        }
        c.close();
        if (danhSach.isEmpty()) {
            if (hienLopLuuTru) themDanhSachRong("Chưa có lớp đã lưu trữ.");
            else themMucDanhSach("Chưa có lớp học phần", "Tạo lớp trước, sau đó mở lớp để chọn giáo viên và xếp sinh viên.", "Chạm để tạo lớp", this::hienTaoLop);
        }
        themNutDieuHuong("Tổng quan", () -> moManHinh("quan-tri-tong-quan")); themNutDieuHuong("Tài khoản", () -> moManHinh("quan-tri-tai-khoan")); themNutDieuHuong("Lớp & xếp lớp", () -> moManHinh("quan-tri-lop"));
    }

    private void hienThiChiTietLopQuanTri() {
        Cursor c = coSoDuLieu.getReadableDatabase().rawQuery("SELECT l.ma_lop,l.ten_hoc_phan,l.hoc_ky,l.nam_hoc,l.trang_thai,a.id,a.ho_ten,a.email FROM lop_hoc_phan l JOIN tai_khoan a ON a.id=l.giao_vien_id WHERE l.id=?", new String[]{String.valueOf(maLopHienTai)});
        if (!c.moveToFirst()) { c.close(); quayLai(); return; }
        String code=c.getString(0), subject=c.getString(1), term=c.getString(2), year=c.getString(3), status=c.getString(4); int teacherId=c.getInt(5); String giaoVien=tenHienThi(c.getString(6),c.getString(7)); c.close();
        datTieuDe(subject, "CHI TIẾT LỚP · " + code, "Lớp " + code, "Giáo viên phụ trách: " + giaoVien + "\n" + term + (year.isEmpty()?"":" · "+year), "Thành viên lớp");
        if ("ACTIVE".equals(status)) {
            themNutThaoTac(hangNut, "+ Xếp sinh viên", this::hienThemSinhVien);
            themNutThaoTac(hangNut, "Đổi giáo viên", this::hienChuyenGiaoVien);
        }
        Cursor members = coSoDuLieu.getReadableDatabase().rawQuery("SELECT a.id,a.ho_ten,a.email,a.ma_so FROM thanh_vien_lop m JOIN tai_khoan a ON a.id=m.sinh_vien_id WHERE m.lop_id=? ORDER BY a.ho_ten", new String[]{String.valueOf(maLopHienTai)});
        while (members.moveToNext()) {
            int id=members.getInt(0); String ten=tenHienThi(members.getString(1),members.getString(2)), codeStudent=members.getString(3);
            themMucDanhSach(ten, codeStudent + " · " + members.getString(2), "Đã đăng ký", "ACTIVE".equals(status) ? () -> new AlertDialog.Builder(this).setTitle(ten).setItems(new String[]{"Xóa khỏi lớp"}, (d,w)->xacNhanGoSinhVien(id,ten)).show() : null);
        }
        members.close(); if (danhSach.isEmpty()) themDanhSachRong("Lớp chưa có sinh viên.");
        if ("ACTIVE".equals(status)) themNutThaoTac(hangNut, "Lưu trữ lớp", this::xacNhanLuuTruLop);
        themNutDieuHuong("Tổng quan", () -> moManHinh("quan-tri-tong-quan")); themNutDieuHuong("Lớp & xếp lớp", () -> moManHinh("quan-tri-lop"));
    }

    private void hienThiTongQuanGiaoVien() {
        datTieuDe("Không gian giảng dạy", "GIÁO VIÊN", "Lớp và bài tập của bạn", "Bài tập, tiến độ và bài nộp chỉ thuộc các lớp bạn đang phụ trách.", "Lớp phụ trách");
        Cursor c = layLopCuaGiaoVien(); int n=0;
        while(c.moveToNext()) { n++; int id=c.getInt(0); String code=c.getString(1), subject=c.getString(2); int students=c.getInt(3), assignments=c.getInt(4);
            themMucDanhSach(subject + " · " + code, students + " sinh viên", assignments + " bài tập đã giao", () -> moChiTiet("giao-vien-chi-tiet-lop", id, -1, -1)); }
        c.close(); if(n==0) themDanhSachRong("Chưa có lớp được phân công. Quản trị viên sẽ cấp lớp cho tài khoản này.");
        themNutDieuHuong("Tổng quan", () -> moManHinh("giao-vien-tong-quan")); themNutDieuHuong("Lớp của tôi", () -> moManHinh("giao-vien-lop")); themNutDieuHuong("Tiến độ", () -> moManHinh("giao-vien-lop"));
    }

    private void hienThiLopGiangDay() {
        datTieuDe("Lớp của tôi", "GIẢNG DẠY", "Lớp đang phụ trách", "Mở một lớp để giao bài và xem tiến độ nộp.", "Danh sách lớp");
        Cursor c=layLopCuaGiaoVien(); int n=0;
        while(c.moveToNext()) { n++; int id=c.getInt(0); String code=c.getString(1),subject=c.getString(2); int students=c.getInt(3),assignments=c.getInt(4);
            themMucDanhSach(subject+" · "+code, students+" sinh viên", assignments+" bài tập", () -> moChiTiet("giao-vien-chi-tiet-lop",id,-1,-1)); }
        c.close(); if(n==0) themDanhSachRong("Quản trị viên chưa phân công lớp cho bạn.");
        themNutDieuHuong("Tổng quan", () -> moManHinh("giao-vien-tong-quan")); themNutDieuHuong("Lớp của tôi", () -> moManHinh("giao-vien-lop"));
    }

    private void hienThiChiTietLopGiangDay() {
        Cursor cl=coSoDuLieu.getReadableDatabase().rawQuery("SELECT ma_lop,ten_hoc_phan FROM lop_hoc_phan WHERE id=? AND giao_vien_id=? AND trang_thai='ACTIVE'",new String[]{String.valueOf(maLopHienTai),String.valueOf(maTaiKhoan)});
        if(!cl.moveToFirst()){cl.close();hienThongBao("Bạn không còn được phân công lớp này.");quayLai();return;}
        String code=cl.getString(0),subject=cl.getString(1);cl.close();
        datTieuDe(subject,"LỚP · "+code,"Bài tập và tiến độ","Tạo bài cho lớp, sau đó theo dõi trạng thái từng sinh viên.","Bài tập đã giao");
        themNutThaoTac(hangNut,"+ Giao bài tập",this::hienTaoBaiTap);
        Cursor c=layBaiTapCuaLop(maLopHienTai); int n=0;
        while(c.moveToNext()){n++;int id=c.getInt(0);String datTieuDe=c.getString(1);long due=c.getLong(2);int total=c.getInt(4),submitted=c.getInt(5);
            themMucDanhSach(datTieuDe,submitted+" / "+total+" đã nộp","Hạn "+dinhDangNgay(due),()->moChiTiet("giao-vien-theo-doi-bai",maLopHienTai,id,-1));}
        c.close(); if(n==0)themDanhSachRong("Lớp chưa có bài tập. Dùng “Giao bài tập” để tạo bài đầu tiên.");
        themNutDieuHuong("Tổng quan",()->moManHinh("giao-vien-tong-quan"));themNutDieuHuong("Lớp của tôi",()->moManHinh("giao-vien-lop"));
    }

    private void hienThiTheoDoiBaiTap() {
        Cursor a=layBaiTapGiaoVien(maBaiTapHienTai);
        if(!a.moveToFirst()){a.close();quayLai();return;}
        String titleText=a.getString(0),instructions=a.getString(1);long due=a.getLong(2);int maLop=a.getInt(3);String classCode=a.getString(4),tepUri=a.getString(5),tepTen=a.getString(6);a.close();
        datTieuDe("Theo dõi bài tập","LỚP · "+classCode,titleText,"Hạn nộp: "+dinhDangNgay(due)+"\n"+instructions,"Tiến độ sinh viên");
        if(!tepUri.isEmpty())themMucDanhSach("Tệp đính kèm: "+tepTen,"Chạm để mở tệp bài tập","",()->moTep(tepUri));
        themNutThaoTac(hangNut,"Sửa bài / hạn",this::hienSuaBaiTap);
        themNutThaoTac(hangNut,"Thu hồi bài",this::xacNhanThuHoiBaiTap);
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT s.id,a.ho_ten,a.email,a.ma_so,s.trang_thai,s.nop_luc,s.diem FROM thanh_vien_lop m JOIN tai_khoan a ON a.id=m.sinh_vien_id LEFT JOIN bai_nop s ON s.sinh_vien_id=m.sinh_vien_id AND s.bai_tap_id=? WHERE m.lop_id=? ORDER BY a.ho_ten",new String[]{String.valueOf(maBaiTapHienTai),String.valueOf(maLop)});
        int n=0,submitted=0;
        while(c.moveToNext()){n++;String state=c.isNull(4)?"NOT_SUBMITTED":c.getString(4);if("SUBMITTED".equals(state)||"LATE".equals(state)||"GRADED".equals(state))submitted++;
            String laSinhVien=tenHienThi(c.getString(1),c.getString(2)),code=c.getString(3);int maBaiNop=c.getInt(0);String sub=laSinhVien+" · "+code;
            if("NOT_SUBMITTED".equals(state)) themMucDanhSach(laSinhVien,code,"Chưa nộp",null);
            else {String detail=tenTrangThai(state)+(c.isNull(6)?"":" · "+c.getDouble(6)+" điểm")+" · "+dinhDangNgayGio(c.getLong(5));themMucDanhSach(sub,detail,"Mở bài nộp",()->moChiTiet("giao-vien-chi-tiet-bai-nop",maLopHienTai,maBaiTapHienTai,maBaiNop));}
        }
        c.close();themMucDanhSach("Tiến độ chung",n==0?"Chưa có sinh viên":submitted+" / "+n+" sinh viên đã nộp",n==0?"":(int)(100L*submitted/n)+"% hoàn thành",null);
        themNutDieuHuong("Tổng quan",()->moManHinh("giao-vien-tong-quan"));themNutDieuHuong("Lớp của tôi",()->moChiTiet("giao-vien-chi-tiet-lop",maLop,-1,-1));
    }

    private void hienThiBaiNop() {
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT s.noi_dung,s.tep_uri,s.tep_ten,s.nop_luc,s.trang_thai,s.diem,s.nhan_xet,a.ho_ten,a.email,b.tieu_de FROM bai_nop s JOIN tai_khoan a ON a.id=s.sinh_vien_id JOIN bai_tap b ON b.id=s.bai_tap_id JOIN lop_hoc_phan l ON l.id=b.lop_id WHERE s.id=? AND l.giao_vien_id=? AND l.trang_thai='ACTIVE' AND b.trang_thai='ACTIVE'",new String[]{String.valueOf(layMaBaiNopHienTai()),String.valueOf(maTaiKhoan)});
        if(!c.moveToFirst()){c.close();quayLai();return;}
        String answer=c.getString(0),uri=c.getString(1),filename=c.getString(2);long submittedAt=c.getLong(3);String status=c.getString(4);boolean hasGrade=!c.isNull(5);double grade=hasGrade?c.getDouble(5):0;String feedback=c.getString(6);String laSinhVien=tenHienThi(c.getString(7),c.getString(8)),assignmentTitle=c.getString(9);c.close();
        datTieuDe("Bài nộp của sinh viên","CHẤM VÀ PHẢN HỒI",laSinhVien,assignmentTitle+"\nNộp lúc "+dinhDangNgayGio(submittedAt)+" · "+tenTrangThai(status),"Nội dung bài nộp");
        themMucDanhSach("Phần trả lời",answer.isEmpty()?"Sinh viên không gửi nội dung văn bản.":answer,"",null);
        if(!filename.isEmpty())themMucDanhSach("Tệp đính kèm",filename,"Chạm để mở",()->moTep(uri));
        if(hasGrade)themMucDanhSach("Kết quả",grade+" điểm",feedback,null);else if(!feedback.isEmpty())themMucDanhSach("Phản hồi",feedback,"Yêu cầu nộp lại",null);
        themNutThaoTac(hangNut,"Chấm / phản hồi",()->hienThiHopThoaiChamBai(layMaBaiNopHienTai(),hasGrade,grade,feedback));
        themNutDieuHuong("Tổng quan",()->moManHinh("giao-vien-tong-quan"));themNutDieuHuong("Quay lại bài",()->quayLai());
    }

    private int layMaBaiNopHienTai() { return maBaiNopHienTai; }

    private void hienThiTongQuanSinhVien() {
        datTieuDe("Việc học của tôi","SINH VIÊN","Bài tập cần hoàn thành","Bài giáo viên giao và việc cá nhân được tách riêng để bạn dễ theo dõi.","Sắp đến hạn");
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.id,b.tieu_de,b.han_nop,l.ma_lop,s.trang_thai FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id JOIN thanh_vien_lop m ON m.lop_id=l.id LEFT JOIN bai_nop s ON s.bai_tap_id=b.id AND s.sinh_vien_id=m.sinh_vien_id WHERE m.sinh_vien_id=? AND l.trang_thai='ACTIVE' AND b.trang_thai='ACTIVE' ORDER BY b.han_nop LIMIT 6",new String[]{String.valueOf(maTaiKhoan)});
        int n=0;while(c.moveToNext()){n++;int id=c.getInt(0);String t=c.getString(1),classCode=c.getString(3),state=c.getString(4);long due=c.getLong(2);
            themMucDanhSach("Giáo viên giao · "+t,"Lớp "+classCode+" · Hạn "+dinhDangNgay(due),state==null?"Chưa nộp":tenTrangThai(state),()->moChiTiet("sinh-vien-chi-tiet-bai-tap",-1,id,-1));}
        c.close();if(n==0)themDanhSachRong("Chưa có bài tập được giao trong các lớp của bạn.");
        Cursor p=coSoDuLieu.getReadableDatabase().rawQuery("SELECT id,tieu_de,ten_mon,han_nop,trang_thai FROM bai_ca_nhan WHERE sinh_vien_id=? AND trang_thai='TODO' ORDER BY han_nop LIMIT 4",new String[]{String.valueOf(maTaiKhoan)});
        while(p.moveToNext()){int id=p.getInt(0);themMucDanhSach("Cá nhân · "+p.getString(1),p.getString(2)+" · Hạn "+dinhDangNgay(p.getLong(3)),"Ưu tiên việc của bạn",()->hienThiBieuMauViecCaNhan(id));}p.close();
        themNutThaoTac(hangNut,"+ Việc cá nhân",()->hienThiBieuMauViecCaNhan(-1));themNutThaoTac(hangNut,"Xem lịch tuần",()->moManHinh("sinh-vien-lich-tuan"));
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));themNutDieuHuong("Cá nhân",()->moManHinh("sinh-vien-viec-ca-nhan"));themNutDieuHuong("Thống kê",()->moManHinh("sinh-vien-thong-ke"));
    }

    private void hienThiLopSinhVien() {
        datTieuDe("Lớp học của tôi","SINH VIÊN","Các lớp đã tham gia","Bài tập trong lớp do giáo viên giao; nội dung và hạn nộp chỉ đọc.","Danh sách lớp");
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT l.id,l.ma_lop,l.ten_hoc_phan,l.hoc_ky,a.ho_ten,a.email FROM thanh_vien_lop m JOIN lop_hoc_phan l ON l.id=m.lop_id JOIN tai_khoan a ON a.id=l.giao_vien_id WHERE m.sinh_vien_id=? AND l.trang_thai='ACTIVE' ORDER BY l.ten_hoc_phan",new String[]{String.valueOf(maTaiKhoan)});
        int n=0;while(c.moveToNext()){n++;int id=c.getInt(0);String code=c.getString(1),subject=c.getString(2),term=c.getString(3),giaoVien=tenHienThi(c.getString(4),c.getString(5));themMucDanhSach(subject+" · "+code,"Giáo viên: "+giaoVien,term,()->moChiTiet("sinh-vien-chi-tiet-lop",id,-1,-1));}
        c.close();if(n==0)themDanhSachRong("Bạn chưa được thêm vào lớp học phần nào. Liên hệ quản trị viên để được xếp lớp.");
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));themNutDieuHuong("Cá nhân",()->moManHinh("sinh-vien-viec-ca-nhan"));
    }

    private void hienThiChiTietLopSinhVien() {
        Cursor cl=coSoDuLieu.getReadableDatabase().rawQuery("SELECT ma_lop,ten_hoc_phan FROM lop_hoc_phan l JOIN thanh_vien_lop m ON m.lop_id=l.id WHERE l.id=? AND m.sinh_vien_id=? AND l.trang_thai='ACTIVE'",new String[]{String.valueOf(maLopHienTai),String.valueOf(maTaiKhoan)});
        if(!cl.moveToFirst()){cl.close();quayLai();return;}String code=cl.getString(0),subject=cl.getString(1);cl.close();
        datTieuDe(subject,"LỚP · "+code,"Bài tập được giao","Mở bài để đọc yêu cầu, hạn nộp và nộp câu trả lời.","Bài tập");
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.id,b.tieu_de,b.han_nop,s.trang_thai FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id AND l.trang_thai='ACTIVE' LEFT JOIN bai_nop s ON s.bai_tap_id=b.id AND s.sinh_vien_id=? WHERE b.lop_id=? AND b.trang_thai='ACTIVE' ORDER BY b.han_nop",new String[]{String.valueOf(maTaiKhoan),String.valueOf(maLopHienTai)});
        int n=0;while(c.moveToNext()){n++;int id=c.getInt(0);String t=c.getString(1),state=c.getString(3);themMucDanhSach(t,"Hạn "+dinhDangNgay(c.getLong(2)),state==null?"Chưa nộp":tenTrangThai(state),()->moChiTiet("sinh-vien-chi-tiet-bai-tap",-1,id,-1));}c.close();if(n==0)themDanhSachRong("Lớp chưa có bài tập được giao.");
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));
    }

    private void hienThiChiTietBaiTapSinhVien() {
        Cursor c=layBaiTapChoSinhVien(maBaiTapHienTai);
        if(!c.moveToFirst()){c.close();hienThongBao("Bài tập không thuộc lớp của bạn hoặc đã được thu hồi.");quayLai();return;}
        String t=c.getString(0),instructions=c.getString(1),classCode=c.getString(3),state=c.getString(4),answer=c.getString(5),uri=c.getString(6),filename=c.getString(7),feedback=c.getString(8),tepBaiTapUri=c.getString(11),tepBaiTapTen=c.getString(12);long due=c.getLong(2);boolean hasGrade=!c.isNull(9);double grade=hasGrade?c.getDouble(9):0;long submitted=c.isNull(10)?0:c.getLong(10);c.close();
        datTieuDe("Chi tiết bài tập","GIÁO VIÊN GIAO · "+classCode,t,"Đề bài chỉ đọc. Bạn được sửa bài nộp trước hạn hoặc khi giáo viên yêu cầu nộp lại.","Yêu cầu");
        themMucDanhSach("Hạn nộp",dinhDangNgayGio(due),due<System.currentTimeMillis()?"Đã quá hạn":"",null);
        themMucDanhSach("Yêu cầu",instructions,"",null);
        if(!tepBaiTapUri.isEmpty())themMucDanhSach("Tệp bài tập: "+tepBaiTapTen,"Chạm để mở tệp giáo viên đính kèm","",()->moTep(tepBaiTapUri));

        themMucDanhSach("Tình trạng nộp",state==null?"Chưa nộp":tenTrangThai(state),"",null);
        themMucDanhSach("Chấm điểm",hasGrade?grade+" / 10":"Chưa có điểm",feedback==null?"":feedback,null);
        themMucDanhSach("Thời gian còn lại",thoiGianConLai(due),"",null);
        themMucDanhSach("Lần chỉnh sửa cuối",submitted==0?"-":dinhDangNgayGio(submitted),"",null);
        if(answer!=null&&!answer.isEmpty())themMucDanhSach("Bài làm đã nộp",answer,"",null);
        if(filename!=null&&!filename.isEmpty())themMucDanhSach("Tệp đã nộp: "+filename,"Chạm để mở tệp của bạn","",()->moTep(uri));
        if("RESUBMIT".equals(state))themMucDanhSach("Yêu cầu sửa bài",feedback==null?"Giáo viên yêu cầu cập nhật bài.":feedback,"",null);

        boolean dangChoPhepSua="SUBMITTED".equals(state)||"LATE".equals(state)||"GRADED".equals(state);
        boolean canSubmit=state==null||"RESUBMIT".equals(state)||(dangChoPhepSua&&due>System.currentTimeMillis());
        if(state!=null&&!canSubmit)themMucDanhSach("Bài nộp đã khóa","Muốn sửa bài sau hạn, hãy nhờ giáo viên yêu cầu nộp lại.","",null);
        if(canSubmit){
            if(uriTep.isEmpty()&&uri!=null&&!uri.isEmpty()){uriTep=uri;tenTep=filename;}
            String nutBai=state==null?(due<System.currentTimeMillis()?"Nộp bài trễ":"Nộp bài"):"Sửa bài tập";
            themNutCuoiManHinh(nutBai,()->hienThiHopThoaiNopBai(state!=null,answer==null?"":answer));
        }
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));
    }

    private String thoiGianConLai(long hanNop){
        long conLai=hanNop-System.currentTimeMillis();
        if(conLai<=0)return "Đã quá hạn";
        long soPhut=(conLai+59999)/60000;
        long ngay=soPhut/1440,gio=(soPhut%1440)/60,phut=soPhut%60;
        if(ngay>0)return "Còn "+ngay+" ngày "+gio+" giờ";
        if(gio>0)return "Còn "+gio+" giờ "+phut+" phút";
        return "Còn "+Math.max(1,phut)+" phút";
    }

    private void hienThiViecCaNhan() {
        datTieuDe("Việc cá nhân","SINH VIÊN","Tự quản lý bài tập riêng","Việc cá nhân chỉ xem và chỉnh sửa được bởi tài khoản đang đăng nhập.","Danh sách việc");
        themNutThaoTac(hangNut,"+ Thêm việc",()->hienThiBieuMauViecCaNhan(-1));themNutThaoTac(hangNut,"Xuất CSV",this::xuatDuLieuCsv);
        hangLoc.setVisibility(View.VISIBLE);
        SearchView search=new SearchView(this);search.setIconifiedByDefault(false);search.setQueryHint("Tìm theo tên bài hoặc môn học");search.setBackgroundResource(R.drawable.bg_home_card);hangLoc.addView(search,new LinearLayout.LayoutParams(-1,doiDonViDp(46)));
        LinearLayout filters=new LinearLayout(this);filters.setOrientation(LinearLayout.HORIZONTAL);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,doiDonViDp(42));fp.topMargin=doiDonViDp(4);hangLoc.addView(filters,fp);
        Spinner status=new Spinner(this);status.setAdapter(taoBoChonChu(new String[]{"Tất cả trạng thái","Chưa xong","Hoàn thành"}));filters.addView(status,new LinearLayout.LayoutParams(0,-1,1));
        Spinner sort=new Spinner(this);sort.setAdapter(taoBoChonChu(new String[]{"Hạn gần nhất","Ưu tiên cao"}));filters.addView(sort,new LinearLayout.LayoutParams(0,-1,1));
        Runnable refresh=()->napDanhSachViecCaNhan(search.getQuery().toString(),status.getSelectedItemPosition(),sort.getSelectedItemPosition());
        status.setOnItemSelectedListener(xuLyChonDanhSach(refresh));sort.setOnItemSelectedListener(xuLyChonDanhSach(refresh));refresh.run();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override public boolean onQueryTextSubmit(String query){napDanhSachViecCaNhan(query,status.getSelectedItemPosition(),sort.getSelectedItemPosition());return true;}
            @Override public boolean onQueryTextChange(String query){napDanhSachViecCaNhan(query,status.getSelectedItemPosition(),sort.getSelectedItemPosition());return true;}
        });
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));themNutDieuHuong("Cá nhân",()->moManHinh("sinh-vien-viec-ca-nhan"));themNutDieuHuong("Thống kê",()->moManHinh("sinh-vien-thong-ke"));
    }

    private void napDanhSachViecCaNhan(String query,int filter,int sort) {
        danhSach.clear();String state=filter==1?"TODO":filter==2?"DONE":"%";String order=sort==1?"CASE uu_tien WHEN 'Cao' THEN 0 WHEN 'Vừa' THEN 1 ELSE 2 END, han_nop":"han_nop";
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT id,ten_mon,tieu_de,ghi_chu,han_nop,uu_tien,trang_thai FROM bai_ca_nhan WHERE sinh_vien_id=? AND trang_thai LIKE ? AND (LOWER(tieu_de) LIKE ? OR LOWER(ten_mon) LIKE ?) ORDER BY "+order,new String[]{String.valueOf(maTaiKhoan),state,"%"+query.toLowerCase(Locale.ROOT)+"%","%"+query.toLowerCase(Locale.ROOT)+"%"});
        int n=0;while(c.moveToNext()){n++;int id=c.getInt(0);String subject=c.getString(1),datTieuDe=c.getString(2),note=c.getString(3),priority=c.getString(5),status=c.getString(6);long due=c.getLong(4);
            themMucDanhSach("Cá nhân · "+datTieuDe,subject+" · Hạn "+dinhDangNgay(due),priority+" ưu tiên · "+("DONE".equals(status)?"Hoàn thành":"Chưa xong"),()->hienThiThaoTacViecCaNhan(id,datTieuDe,subject,note,due,priority,status));}
        c.close();if(n==0)themMucDanhSach("Không có việc phù hợp","Thử thay đổi từ khóa hoặc bộ lọc.","",null);danhSachAdapter.notifyDataSetChanged();
    }

    private void hienThiLichTuan() {
        datTieuDe("Lịch tuần","SINH VIÊN","7 ngày sắp tới","Lịch được tổng hợp từ bài giáo viên giao và việc cá nhân của bạn.","Theo hạn nộp");
        long now=System.currentTimeMillis(),end=now+7L*24*60*60*1000;
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.id,b.tieu_de,b.han_nop,l.ma_lop,s.trang_thai FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id JOIN thanh_vien_lop m ON m.lop_id=l.id LEFT JOIN bai_nop s ON s.bai_tap_id=b.id AND s.sinh_vien_id=m.sinh_vien_id WHERE m.sinh_vien_id=? AND l.trang_thai='ACTIVE' AND b.trang_thai='ACTIVE' AND b.han_nop BETWEEN ? AND ? ORDER BY b.han_nop",new String[]{String.valueOf(maTaiKhoan),String.valueOf(now),String.valueOf(end)});
        int n=0;while(c.moveToNext()){n++;int id=c.getInt(0);String datTieuDe=c.getString(1),code=c.getString(3),state=c.getString(4);themMucDanhSach("Giáo viên giao · "+datTieuDe,"Lớp "+code+" · "+dinhDangNgayGio(c.getLong(2)),state==null?"Chưa nộp":tenTrangThai(state),()->moChiTiet("sinh-vien-chi-tiet-bai-tap",-1,id,-1));}c.close();
        Cursor p=coSoDuLieu.getReadableDatabase().rawQuery("SELECT id,tieu_de,ten_mon,han_nop,uu_tien FROM bai_ca_nhan WHERE sinh_vien_id=? AND trang_thai='TODO' AND han_nop BETWEEN ? AND ? ORDER BY han_nop",new String[]{String.valueOf(maTaiKhoan),String.valueOf(now),String.valueOf(end)});
        while(p.moveToNext()){n++;int id=p.getInt(0);String datTieuDe=p.getString(1),subject=p.getString(2),priority=p.getString(4);long due=p.getLong(3);themMucDanhSach("Cá nhân · "+datTieuDe,subject+" · "+dinhDangNgayGio(due),priority+" ưu tiên",()->hienThiBieuMauViecCaNhan(id));}p.close();if(n==0)themDanhSachRong("Tuần tới chưa có hạn nộp nào.");
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lịch tuần",()->moManHinh("sinh-vien-lich-tuan"));themNutDieuHuong("Cá nhân",()->moManHinh("sinh-vien-viec-ca-nhan"));
    }

    private void hienThiThongKe() {
        datTieuDe("Thống kê học tập","CHỈ DỮ LIỆU CỦA BẠN","Tiến độ cá nhân","Thống kê không hiển thị bài riêng của bạn cho giáo viên hoặc sinh viên khác.","Tổng hợp");
        SQLiteDatabase d=coSoDuLieu.getReadableDatabase();
        int assigned=demBanGhi(d,"SELECT COUNT(*) FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id AND l.trang_thai='ACTIVE' JOIN thanh_vien_lop m ON m.lop_id=b.lop_id WHERE m.sinh_vien_id="+maTaiKhoan+" AND b.trang_thai='ACTIVE'");
        int turnedIn=demBanGhi(d,"SELECT COUNT(*) FROM bai_nop s JOIN bai_tap b ON b.id=s.bai_tap_id AND b.trang_thai='ACTIVE' JOIN lop_hoc_phan l ON l.id=b.lop_id AND l.trang_thai='ACTIVE' JOIN thanh_vien_lop m ON m.lop_id=l.id AND m.sinh_vien_id=s.sinh_vien_id WHERE s.sinh_vien_id="+maTaiKhoan+" AND s.trang_thai<>'RESUBMIT'");
        int personal=demBanGhi(d,"SELECT COUNT(*) FROM bai_ca_nhan WHERE sinh_vien_id="+maTaiKhoan+" AND trang_thai='TODO'");
        int done=demBanGhi(d,"SELECT COUNT(*) FROM bai_ca_nhan WHERE sinh_vien_id="+maTaiKhoan+" AND trang_thai='DONE'");
        themMucDanhSach("Bài giáo viên giao",turnedIn+" / "+assigned+" bài đã nộp",assigned==0?"Chưa có bài":(int)(100L*turnedIn/assigned)+"% tiến độ",null);
        themMucDanhSach("Việc cá nhân",done+" hoàn thành",personal+" việc còn lại",null);
        themMucDanhSach("Lịch và báo cáo","Xem hạn trong 7 ngày","Xuất CSV dữ liệu của tôi",()->moManHinh("sinh-vien-lich-tuan"));
        themNutThaoTac(hangNut,"Xuất CSV của tôi",this::xuatDuLieuCsv);
        themNutDieuHuong("Bài tập",()->moManHinh("sinh-vien-tong-quan"));themNutDieuHuong("Lớp học",()->moManHinh("sinh-vien-lop"));themNutDieuHuong("Cá nhân",()->moManHinh("sinh-vien-viec-ca-nhan"));themNutDieuHuong("Thống kê",()->moManHinh("sinh-vien-thong-ke"));
    }

    private Cursor layLopCuaGiaoVien(){return coSoDuLieu.getReadableDatabase().rawQuery("SELECT l.id,l.ma_lop,l.ten_hoc_phan,(SELECT COUNT(*) FROM thanh_vien_lop m WHERE m.lop_id=l.id),(SELECT COUNT(*) FROM bai_tap b WHERE b.lop_id=l.id AND b.trang_thai='ACTIVE') FROM lop_hoc_phan l WHERE l.giao_vien_id=? AND l.trang_thai='ACTIVE' ORDER BY l.ten_hoc_phan",new String[]{String.valueOf(maTaiKhoan)});}
    private Cursor layBaiTapCuaLop(int maLop){return coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.id,b.tieu_de,b.han_nop,b.trang_thai,(SELECT COUNT(*) FROM thanh_vien_lop m WHERE m.lop_id=b.lop_id),(SELECT COUNT(*) FROM bai_nop s JOIN thanh_vien_lop m ON m.sinh_vien_id=s.sinh_vien_id AND m.lop_id=b.lop_id WHERE s.bai_tap_id=b.id AND s.trang_thai<>'RESUBMIT') FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id WHERE b.lop_id=? AND l.giao_vien_id=? AND l.trang_thai='ACTIVE' AND b.trang_thai='ACTIVE' ORDER BY b.han_nop DESC",new String[]{String.valueOf(maLop),String.valueOf(maTaiKhoan)});}
    private Cursor layBaiTapGiaoVien(int id){return coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.tieu_de,b.yeu_cau,b.han_nop,b.lop_id,l.ma_lop,b.tep_uri,b.tep_ten FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id WHERE b.id=? AND l.giao_vien_id=? AND l.trang_thai='ACTIVE' AND b.trang_thai='ACTIVE'",new String[]{String.valueOf(id),String.valueOf(maTaiKhoan)});}
    private Cursor layBaiTapChoSinhVien(int id){return coSoDuLieu.getReadableDatabase().rawQuery("SELECT b.tieu_de,b.yeu_cau,b.han_nop,l.ma_lop,s.trang_thai,s.noi_dung,s.tep_uri,s.tep_ten,s.nhan_xet,s.diem,s.nop_luc,b.tep_uri,b.tep_ten FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id AND l.trang_thai='ACTIVE' JOIN thanh_vien_lop m ON m.lop_id=l.id LEFT JOIN bai_nop s ON s.bai_tap_id=b.id AND s.sinh_vien_id=m.sinh_vien_id WHERE b.id=? AND m.sinh_vien_id=? AND b.trang_thai='ACTIVE'",new String[]{String.valueOf(id),String.valueOf(maTaiKhoan)});}

    private void hienTaoTaiKhoan(){
        LinearLayout form=taoKhungBieuMau();EditText ten=taoONhap(form,"Họ và tên","Nguyễn Văn A",false),code=taoONhap(form,"Mã tài khoản (nếu có)","SV001 hoặc GV001",false),emailField=taoONhap(form,"Email đăng nhập","ten@school.edu.vn",false),password=taoONhap(form,"Mật khẩu khởi tạo","Tối thiểu 6 ký tự",false);password.setInputType(129);
        Spinner roleChoice=new Spinner(this);roleChoice.setAdapter(taoBoChonChu(new String[]{"Sinh viên","Giáo viên"}));form.addView(roleChoice);
        AlertDialog hopThoai=new AlertDialog.Builder(this).setTitle("Cấp tài khoản").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Tạo tài khoản",null).create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
            String address=emailField.getText().toString().trim(),pass=password.getText().toString();
            if(ten.getText().toString().trim().isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(address).matches()||pass.trim().length()<6){hienThongBao("Nhập họ tên, email hợp lệ và mật khẩu từ 6 ký tự.");return;}
            String r=roleChoice.getSelectedItemPosition()==0?"STUDENT":"TEACHER";
            boolean ok=coSoDuLieu.taoTaiKhoanBangQuanTri(maTaiKhoan,address,pass,r,ten.getText().toString(),code.getText().toString());
            hienThongBao(ok?"Đã tạo tài khoản. Hãy gửi thông tin đăng nhập cho người dùng.":"Email hoặc mã tài khoản đã tồn tại.");
            if(ok){hopThoai.dismiss();hienThiManHinh();}
        }));
        hopThoai.show();
    }

    private void hienThiThaoTacTaiKhoan(int id,String ten,boolean active){
        String[] options=active?new String[]{"Khóa tài khoản","Đặt lại mật khẩu"}:new String[]{"Mở tài khoản","Đặt lại mật khẩu"};
        new AlertDialog.Builder(this).setTitle(ten).setItems(options,(d,which)->{
            if(which==0){boolean ok=coSoDuLieu.doiTrangThaiTaiKhoan(maTaiKhoan,id,!active);hienThongBao(ok?(active?"Đã khóa tài khoản.":"Đã mở tài khoản."):"Không thể cập nhật tài khoản.");hienThiManHinh();}
            else {
                EditText pass=new EditText(this);pass.setHint("Mật khẩu mới (từ 6 ký tự)");pass.setInputType(129);
                AlertDialog hopThoai=new AlertDialog.Builder(this).setTitle("Đặt lại mật khẩu").setView(pass).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create();
                hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
                    String p=pass.getText().toString();
                    if(p.trim().length()<6){hienThongBao("Mật khẩu cần ít nhất 6 ký tự.");return;}
                    boolean ok=coSoDuLieu.datLaiMatKhau(maTaiKhoan,id,p);
                    hienThongBao(ok?"Đã đặt lại mật khẩu.":"Không thể cập nhật mật khẩu.");
                    if(ok)hopThoai.dismiss();
                }));
                hopThoai.show();
            }
        }).show();
    }

    private void hienTaoLop(){
        ArrayList<LuaChon> teachers=layLuaChon(coSoDuLieu.layGiaoVienHoatDong(),false);if(teachers.isEmpty()){hienThongBao("Hãy cấp ít nhất một tài khoản giáo viên trước.");return;}
        LinearLayout f=taoKhungBieuMau();EditText code=taoONhap(f,"Mã lớp","VD: MOB01",false),subject=taoONhap(f,"Tên học phần","Lập trình di động",false),term=taoONhap(f,"Học kỳ","Học kỳ 1",false),year=taoONhap(f,"Năm học","2026-2027",false);Spinner sp=new Spinner(this);sp.setAdapter(taoBoChonTaiKhoan(teachers));f.addView(sp);
        AlertDialog hopThoai=new AlertDialog.Builder(this).setTitle("Tạo lớp học phần").setView(f).setNegativeButton("Hủy",null).setPositiveButton("Tạo lớp",null).create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
            if(code.getText().toString().trim().isEmpty()||subject.getText().toString().trim().isEmpty()){hienThongBao("Nhập mã lớp và tên học phần.");return;}
            LuaChon giaoVien=(LuaChon)sp.getSelectedItem();
            boolean ok=coSoDuLieu.taoLop(maTaiKhoan,code.getText().toString(),subject.getText().toString(),term.getText().toString(),year.getText().toString(),giaoVien.ma);
            hienThongBao(ok?"Đã tạo lớp. Mở lớp để xếp sinh viên.":"Mã lớp đã tồn tại hoặc giáo viên không hợp lệ.");
            if(ok){hopThoai.dismiss();moManHinh("quan-tri-lop");}
        }));
        hopThoai.show();
    }

    private void hienThemSinhVien(){
        ArrayList<LuaChon> sinhVienCoTheXep=new ArrayList<>();
        Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT a.id,a.ho_ten,a.email,a.ma_so FROM tai_khoan a WHERE a.vai_tro='STUDENT' AND a.trang_thai=1 AND NOT EXISTS(SELECT 1 FROM thanh_vien_lop m WHERE m.sinh_vien_id=a.id AND m.lop_id=?) ORDER BY a.ho_ten",new String[]{String.valueOf(maLopHienTai)});
        while(c.moveToNext())sinhVienCoTheXep.add(new LuaChon(c.getInt(0),tenHienThi(c.getString(1),c.getString(2))+" · "+c.getString(3)));
        c.close();
        if(sinhVienCoTheXep.isEmpty()){hienThongBao("Không còn sinh viên hoạt động nào để xếp vào lớp này.");return;}

        String[] tenSinhVien=new String[sinhVienCoTheXep.size()];
        boolean[] daChon=new boolean[sinhVienCoTheXep.size()];
        for(int i=0;i<sinhVienCoTheXep.size();i++)tenSinhVien[i]=sinhVienCoTheXep.get(i).nhan;

        AlertDialog hopThoai=new AlertDialog.Builder(this)
                .setTitle("Xếp sinh viên (0/"+sinhVienCoTheXep.size()+")")
                .setMultiChoiceItems(tenSinhVien,daChon,(hopThoaiLuaChon,viTri,duocChon)->{
                    daChon[viTri]=duocChon;
                    AlertDialog hopThoaiHienTai=(AlertDialog)hopThoaiLuaChon;
                    int soDaChon=demSinhVienDaChon(daChon);
                    hopThoaiHienTai.setTitle("Xếp sinh viên ("+soDaChon+"/"+sinhVienCoTheXep.size()+")");
                    hopThoaiHienTai.getButton(DialogInterface.BUTTON_NEUTRAL).setText(soDaChon==daChon.length?"Bỏ chọn tất cả":"Chọn tất cả");
                })
                .setPositiveButton("Xếp vào lớp",null)
                .setNeutralButton("Chọn tất cả",null)
                .setNegativeButton("Hủy",null)
                .create();

        hopThoai.setOnShowListener(v->{
            Button nutXep=hopThoai.getButton(DialogInterface.BUTTON_POSITIVE);
            nutXep.setOnClickListener(nut->{
                ArrayList<Integer> maSinhVienDaChon=new ArrayList<>();
                for(int i=0;i<daChon.length;i++)if(daChon[i])maSinhVienDaChon.add(sinhVienCoTheXep.get(i).ma);
                if(maSinhVienDaChon.isEmpty()){hienThongBao("Hãy chọn ít nhất một sinh viên.");return;}
                int soLuongDaXep=coSoDuLieu.themNhieuSinhVienVaoLop(maTaiKhoan,maLopHienTai,maSinhVienDaChon);
                hienThongBao("Đã xếp "+soLuongDaXep+"/"+maSinhVienDaChon.size()+" sinh viên vào lớp.");
                hopThoai.dismiss();hienThiManHinh();
            });

            Button nutChonTatCa=hopThoai.getButton(DialogInterface.BUTTON_NEUTRAL);
            nutChonTatCa.setOnClickListener(nut->{
                boolean chonHet=demSinhVienDaChon(daChon)<daChon.length;
                for(int i=0;i<daChon.length;i++){
                    daChon[i]=chonHet;
                    hopThoai.getListView().setItemChecked(i,chonHet);
                }
                nutChonTatCa.setText(chonHet?"Bỏ chọn tất cả":"Chọn tất cả");
                hopThoai.setTitle("Xếp sinh viên ("+demSinhVienDaChon(daChon)+"/"+sinhVienCoTheXep.size()+")");
            });
        });
        hopThoai.show();
    }

    private int demSinhVienDaChon(boolean[] danhSachDaChon){int dem=0;for(boolean daChon:danhSachDaChon)if(daChon)dem++;return dem;}

    private void hienChuyenGiaoVien(){
        ArrayList<LuaChon> teachers=new ArrayList<>();Cursor c=coSoDuLieu.layGiaoVienHoatDong();while(c.moveToNext())if(c.getInt(0)!=layMaGiaoVienPhuTrach())teachers.add(new LuaChon(c.getInt(0),tenHienThi(c.getString(1),c.getString(2))));c.close();
        if(teachers.isEmpty()){hienThongBao("Không có giáo viên khác đang hoạt động.");return;}Spinner sp=new Spinner(this);sp.setAdapter(taoBoChonTaiKhoan(teachers));
        new AlertDialog.Builder(this).setTitle("Chuyển giáo viên phụ trách").setMessage("Quyền xem lớp và giao bài sẽ chuyển sang giáo viên mới.").setView(sp).setNegativeButton("Hủy",null).setPositiveButton("Chuyển",(d,w)->{LuaChon t=(LuaChon)sp.getSelectedItem();hienThongBao(coSoDuLieu.doiGiaoVien(maTaiKhoan,maLopHienTai,t.ma)?"Đã chuyển giáo viên phụ trách.":"Không thể đổi giáo viên.");hienThiManHinh();}).show();
    }
    private int layMaGiaoVienPhuTrach(){Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT giao_vien_id FROM lop_hoc_phan WHERE id=?",new String[]{String.valueOf(maLopHienTai)});int id=c.moveToFirst()?c.getInt(0):-1;c.close();return id;}
    private void xacNhanGoSinhVien(int id,String ten){new AlertDialog.Builder(this).setTitle("Xóa sinh viên khỏi lớp?").setMessage("Gỡ "+ten+" khỏi danh sách thành viên lớp.").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->{hienThongBao(coSoDuLieu.xoaSinhVienKhoiLop(maTaiKhoan,maLopHienTai,id)?"Đã gỡ sinh viên khỏi lớp.":"Không thể gỡ sinh viên.");hienThiManHinh();}).show();}
    private void xacNhanLuuTruLop(){new AlertDialog.Builder(this).setTitle("Lưu trữ lớp học?").setMessage("Lớp sẽ ngừng hoạt động nhưng dữ liệu và lịch sử vẫn được giữ lại.").setNegativeButton("Hủy",null).setPositiveButton("Lưu trữ",(d,w)->{hienThongBao(coSoDuLieu.luuTruLop(maTaiKhoan,maLopHienTai)?"Đã lưu trữ lớp.":"Không thể lưu trữ lớp.");moManHinh("quan-tri-lop");}).show();}

    private void hienTaoBaiTap(){hienThiBieuMauBaiTap(false);}
    private void hienSuaBaiTap(){hienThiBieuMauBaiTap(true);}
    private void hienThiBieuMauBaiTap(boolean editing){
        String tieuDeCu="",yeuCauCu="",tepUriCu="",tepTenCu="";
        long hanCu=System.currentTimeMillis()+7L*86400000;
        if(editing){
            Cursor c=layBaiTapGiaoVien(maBaiTapHienTai);
            if(c.moveToFirst()){tieuDeCu=c.getString(0);yeuCauCu=c.getString(1);hanCu=c.getLong(2);tepUriCu=c.getString(5);tepTenCu=c.getString(6);}
            c.close();
        }

        uriTepBaiTapNhap=tepUriCu;tenTepBaiTapNhap=tepTenCu;
        final long[] hanNop={hanCu};
        LinearLayout f=taoKhungBieuMau();
        EditText tieuDe=taoONhap(f,"Tên bài tập","VD: Bài thực hành 1",false);tieuDe.setText(tieuDeCu);
        EditText yeuCau=taoONhap(f,"Yêu cầu","Mô tả nội dung và tiêu chí nộp",true);yeuCau.setMinLines(3);yeuCau.setText(yeuCauCu);

        TextView hanNopView=new TextView(this);
        hanNopView.setPadding(doiDonViDp(12),doiDonViDp(14),doiDonViDp(12),doiDonViDp(14));
        hanNopView.setBackgroundResource(R.drawable.bg_home_card);
        hanNopView.setText("Hạn nộp: "+dinhDangNgayGio(hanNop[0])+" · Chạm để đổi");
        f.addView(hanNopView);
        hanNopView.setOnClickListener(v->chonNgayGio(hanNop[0],giaTri->{hanNop[0]=giaTri;hanNopView.setText("Hạn nộp: "+dinhDangNgayGio(giaTri)+" · Chạm để đổi");}));

        nhanTepBaiTapNhap=new TextView(this);
        nhanTepBaiTapNhap.setTextColor(MAU_CHU);nhanTepBaiTapNhap.setTextSize(14);
        nhanTepBaiTapNhap.setPadding(doiDonViDp(12),doiDonViDp(12),doiDonViDp(12),doiDonViDp(12));
        nhanTepBaiTapNhap.setBackgroundResource(R.drawable.bg_home_card);
        f.addView(nhanTepBaiTapNhap);capNhatNhanTepBaiTapNhap();

        LinearLayout hangNutTep=new LinearLayout(this);hangNutTep.setOrientation(LinearLayout.HORIZONTAL);f.addView(hangNutTep);
        themNutThaoTac(hangNutTep,"Chọn tệp",this::chonTepBaiTapGiao);
        themNutThaoTac(hangNutTep,"Gỡ tệp",this::goTepBaiTapNhap);

        AlertDialog hopThoai=new AlertDialog.Builder(this)
                .setTitle(editing?"Sửa bài tập":"Giao bài tập")
                .setMessage("Lớp đang giao: "+tenLop(maLopHienTai))
                .setView(f)
                .setNegativeButton("Hủy",null)
                .setPositiveButton("Lưu",null)
                .create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
                    if(tieuDe.getText().toString().trim().isEmpty()||yeuCau.getText().toString().trim().isEmpty()){hienThongBao("Nhập tên và yêu cầu bài tập.");return;}
                    boolean ok=editing
                            ?coSoDuLieu.suaBaiTap(maTaiKhoan,maBaiTapHienTai,tieuDe.getText().toString(),yeuCau.getText().toString(),uriTepBaiTapNhap,tenTepBaiTapNhap,hanNop[0])
                            :coSoDuLieu.taoBaiTap(maTaiKhoan,maLopHienTai,tieuDe.getText().toString(),yeuCau.getText().toString(),uriTepBaiTapNhap,tenTepBaiTapNhap,hanNop[0]);
                    String thongBao=editing?"Đã cập nhật bài tập.":"Đã giao bài tập.";
                    if(!tenTepBaiTapNhap.isEmpty())thongBao+=" Đã đính kèm "+tenTepBaiTapNhap+".";
                    hienThongBao(ok?thongBao:"Không thể lưu bài tập.");
                    if(ok){hopThoai.dismiss();uriTepBaiTapNhap="";tenTepBaiTapNhap="";hienThiManHinh();}
                }));
        hopThoai.setOnDismissListener(v->nhanTepBaiTapNhap=null);
        hopThoai.show();
    }

    private void chonTepBaiTapGiao(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        dangChoChonTepBaiTap=true;
        try{boChonTepBaiTap.launch(intent);}catch(ActivityNotFoundException e){dangChoChonTepBaiTap=false;hienThongBao("Thiết bị chưa có ứng dụng chọn tệp.");}
    }
    private void goTepBaiTapNhap(){uriTepBaiTapNhap="";tenTepBaiTapNhap="";capNhatNhanTepBaiTapNhap();}
    private void capNhatNhanTepBaiTapNhap(){if(nhanTepBaiTapNhap!=null)nhanTepBaiTapNhap.setText(tenTepBaiTapNhap.isEmpty()?"Tệp bài tập: Chưa đính kèm":"Tệp bài tập: "+tenTepBaiTapNhap);}
    private void xacNhanThuHoiBaiTap(){new AlertDialog.Builder(this).setTitle("Thu hồi bài tập?").setMessage("Bài sẽ không còn xuất hiện trong danh sách của sinh viên. Thao tác này không xóa bài nộp đã lưu.").setNegativeButton("Hủy",null).setPositiveButton("Thu hồi",(d,w)->{hienThongBao(coSoDuLieu.thuHoiBaiTap(maTaiKhoan,maBaiTapHienTai)?"Đã thu hồi bài tập.":"Không thể thu hồi bài.");quayLai();}).show();}

    private void hienThiHopThoaiChamBai(int maBaiNop,boolean daCoDiem,double oldGrade,String oldFeedback){
        LinearLayout f=taoKhungBieuMau();EditText grade=taoONhap(f,"Điểm (0–10)","8.5",false);grade.setInputType(8194);if(daCoDiem)grade.setText(String.valueOf(oldGrade));EditText feedback=taoONhap(f,"Nhận xét","Nhận xét bài làm",true);feedback.setMinLines(3);feedback.setText(oldFeedback);
        AlertDialog hopThoai=new AlertDialog.Builder(this).setTitle("Chấm và phản hồi").setView(f).setNeutralButton("Yêu cầu nộp lại",(d,w)->{hienThongBao(coSoDuLieu.danhGiaBaiNop(maTaiKhoan,maBaiNop,0,feedback.getText().toString(),true)?"Đã gửi yêu cầu nộp lại.":"Không thể cập nhật bài nộp.");hienThiManHinh();}).setNegativeButton("Hủy",null).setPositiveButton("Lưu điểm",null).create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
            double score;try{score=Double.parseDouble(grade.getText().toString());}catch(Exception e){hienThongBao("Nhập điểm từ 0 đến 10.");return;}
            if(Double.isNaN(score)||Double.isInfinite(score)||score<0||score>10){hienThongBao("Điểm phải nằm trong khoảng 0–10.");return;}
            boolean ok=coSoDuLieu.danhGiaBaiNop(maTaiKhoan,maBaiNop,score,feedback.getText().toString(),false);
            hienThongBao(ok?"Đã lưu điểm và nhận xét.":"Không thể cập nhật bài nộp.");
            if(ok){hopThoai.dismiss();hienThiManHinh();}
        }));
        hopThoai.show();
    }

    private void hienThiHopThoaiNopBai(boolean daNop,String cauTraLoiCu){
        String uriCu=uriTep,tenTepCu=tenTep,cauTraLoiNhapCu=cauTraLoiNhap;
        boolean[] daLuu={false};
        LinearLayout f=taoKhungBieuMau();
        oNhapCauTraLoi=taoONhap(f,"Nội dung bài làm","Nhập câu trả lời hoặc mô tả tệp đã đính kèm",true);
        oNhapCauTraLoi.setMinLines(4);
        oNhapCauTraLoi.setText(cauTraLoiNhap.isEmpty()?cauTraLoiCu:cauTraLoiNhap);

        nhanTepBaiNopNhap=new TextView(this);
        nhanTepBaiNopNhap.setTextColor(MAU_CHU);nhanTepBaiNopNhap.setTextSize(14);
        nhanTepBaiNopNhap.setPadding(doiDonViDp(12),doiDonViDp(12),doiDonViDp(12),doiDonViDp(12));
        nhanTepBaiNopNhap.setBackgroundResource(R.drawable.bg_home_card);
        f.addView(nhanTepBaiNopNhap);capNhatNhanTepBaiNopNhap();

        LinearLayout hangNutTep=new LinearLayout(this);hangNutTep.setOrientation(LinearLayout.HORIZONTAL);f.addView(hangNutTep);
        themNutThaoTac(hangNutTep,"Đính kèm tệp",this::chonTepBaiNop);
        themNutThaoTac(hangNutTep,"Gỡ tệp",this::goTepBaiNopNhap);

        AlertDialog hopThoai=new AlertDialog.Builder(this)
                .setTitle(daNop?"Sửa bài nộp":"Nộp bài tập")
                .setMessage("Có thể gửi nội dung, tệp hoặc cả hai.")
                .setView(f)
                .setNegativeButton("Hủy",null)
                .setPositiveButton(daNop?"Lưu sửa bài":"Nộp bài",null)
                .create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
            String cauTraLoi=oNhapCauTraLoi.getText().toString().trim();
            if(cauTraLoi.isEmpty()&&uriTep.isEmpty()){hienThongBao("Nhập nội dung hoặc đính kèm tệp trước khi nộp.");return;}
            boolean ok=coSoDuLieu.nopBai(maTaiKhoan,maBaiTapHienTai,cauTraLoi,uriTep,tenTep);
            if(!ok){hienThongBao("Không thể lưu bài nộp. Hãy kiểm tra bạn có thuộc lớp này không.");return;}
            hienThongBao(daNop?"Đã cập nhật bài nộp.":"Đã nộp bài thành công.");
            daLuu[0]=true;
            cauTraLoiNhap="";uriTep="";tenTep="";oNhapCauTraLoi=null;nhanTepBaiNopNhap=null;
            hopThoai.dismiss();hienThiManHinh();
        }));
        hopThoai.setOnDismissListener(v->{
            if(!daLuu[0]){uriTep=uriCu;tenTep=tenTepCu;cauTraLoiNhap=cauTraLoiNhapCu;}
            oNhapCauTraLoi=null;
            nhanTepBaiNopNhap=null;
        });
        hopThoai.show();
    }

    private void goTepBaiNopNhap(){uriTep="";tenTep="";capNhatNhanTepBaiNopNhap();}
    private void capNhatNhanTepBaiNopNhap(){if(nhanTepBaiNopNhap!=null)nhanTepBaiNopNhap.setText(tenTep.isEmpty()?"Tệp nộp: Chưa đính kèm":"Tệp nộp: "+tenTep);}
    private void chonTepBaiNop(){if(oNhapCauTraLoi!=null)cauTraLoiNhap=oNhapCauTraLoi.getText().toString();Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);dangChoChonTepBaiNop=true;try{boChonTep.launch(intent);}catch(ActivityNotFoundException e){dangChoChonTepBaiNop=false;hienThongBao("Thiết bị chưa có ứng dụng chọn tệp.");}}

    private void hienThiThaoTacViecCaNhan(int id,String datTieuDe,String subject,String note,long due,String priority,String status){
        String[] options={"Sửa việc","Đánh dấu "+("DONE".equals(status)?"chưa xong":"hoàn thành"),"Xóa việc"};new AlertDialog.Builder(this).setTitle(datTieuDe).setItems(options,(d,which)->{if(which==0)hienThiBieuMauViecCaNhan(id);else if(which==1){coSoDuLieu.suaViecCaNhan(maTaiKhoan,id,subject,datTieuDe,note,due,priority,"DONE".equals(status)?"TODO":"DONE");hienThiManHinh();}else new AlertDialog.Builder(this).setTitle("Xóa việc cá nhân?").setMessage("Việc đã xóa không thể khôi phục.").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(x,y)->{coSoDuLieu.xoaViecCaNhan(maTaiKhoan,id);hienThiManHinh();}).show();}).show();
    }
    private void hienThiBieuMauViecCaNhan(int taskId){
        String subjectText="",titleText="",noteText="",priorityText="Vừa",statusText="TODO";long due=System.currentTimeMillis()+3L*86400000;
        if(taskId>=0){Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT ten_mon,tieu_de,ghi_chu,han_nop,uu_tien,trang_thai FROM bai_ca_nhan WHERE id=? AND sinh_vien_id=?",new String[]{String.valueOf(taskId),String.valueOf(maTaiKhoan)});if(c.moveToFirst()){subjectText=c.getString(0);titleText=c.getString(1);noteText=c.getString(2);due=c.getLong(3);priorityText=c.getString(4);statusText=c.getString(5);}c.close();}
        final long[] dueAt={due};LinearLayout f=taoKhungBieuMau();EditText subject=taoONhap(f,"Môn học","VD: Cơ sở dữ liệu",false);subject.setText(subjectText);EditText datTieuDe=taoONhap(f,"Tên việc","Tên bài tập cá nhân",false);datTieuDe.setText(titleText);EditText note=taoONhap(f,"Ghi chú","Nội dung cần làm",true);note.setText(noteText);Spinner priority=new Spinner(this);priority.setAdapter(taoBoChonChu(new String[]{"Thấp","Vừa","Cao"}));priority.setSelection(priorityText.equals("Cao")?2:priorityText.equals("Thấp")?0:1);f.addView(priority);Spinner status=new Spinner(this);status.setAdapter(taoBoChonChu(new String[]{"Chưa xong","Hoàn thành"}));status.setSelection("DONE".equals(statusText)?1:0);if(taskId>=0)f.addView(status);
        TextView dueView=new TextView(this);dueView.setPadding(doiDonViDp(12),doiDonViDp(14),doiDonViDp(12),doiDonViDp(14));dueView.setBackgroundResource(R.drawable.bg_home_card);dueView.setText("Hạn: "+dinhDangNgayGio(dueAt[0])+" · Chạm để đổi");f.addView(dueView);dueView.setOnClickListener(v->chonNgayGio(dueAt[0],value->{dueAt[0]=value;dueView.setText("Hạn: "+dinhDangNgayGio(value)+" · Chạm để đổi");}));
        AlertDialog hopThoai=new AlertDialog.Builder(this).setTitle(taskId<0?"Thêm việc cá nhân":"Sửa việc cá nhân").setView(f).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create();
        hopThoai.setOnShowListener(v->hopThoai.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(nut->{
            if(subject.getText().toString().trim().isEmpty()||datTieuDe.getText().toString().trim().isEmpty()){hienThongBao("Nhập tên môn và tên việc.");return;}
            String st=status.getSelectedItemPosition()==1?"DONE":"TODO";
            boolean ok=taskId<0
                    ?coSoDuLieu.taoViecCaNhan(maTaiKhoan,subject.getText().toString(),datTieuDe.getText().toString(),note.getText().toString(),dueAt[0],priority.getSelectedItem().toString())!=-1
                    :coSoDuLieu.suaViecCaNhan(maTaiKhoan,taskId,subject.getText().toString(),datTieuDe.getText().toString(),note.getText().toString(),dueAt[0],priority.getSelectedItem().toString(),st);
            hienThongBao(ok?"Đã lưu việc cá nhân.":"Không thể lưu việc.");
            if(ok){hopThoai.dismiss();hienThiManHinh();}
        }));
        hopThoai.show();
    }

    private void xuatDuLieuCsv(){
        StringBuilder csv=new StringBuilder("Nguồn,Môn hoặc lớp,Bài tập,Hạn nộp,Trạng thái,Điểm,Ưu tiên\n");Cursor p=coSoDuLieu.getReadableDatabase().rawQuery("SELECT ten_mon,tieu_de,han_nop,trang_thai,uu_tien FROM bai_ca_nhan WHERE sinh_vien_id=? ORDER BY han_nop",new String[]{String.valueOf(maTaiKhoan)});
        while(p.moveToNext())csv.append("Cá nhân,").append(oDuLieuCsv(p.getString(0))).append(',').append(oDuLieuCsv(p.getString(1))).append(',').append(oDuLieuCsv(dinhDangNgay(p.getLong(2)))).append(',').append(oDuLieuCsv("DONE".equals(p.getString(3))?"Hoàn thành":"Chưa xong")).append(",,").append(oDuLieuCsv(p.getString(4))).append('\n');p.close();
        Cursor a=coSoDuLieu.getReadableDatabase().rawQuery("SELECT l.ma_lop,b.tieu_de,b.han_nop,s.trang_thai,s.diem FROM bai_tap b JOIN lop_hoc_phan l ON l.id=b.lop_id AND l.trang_thai='ACTIVE' JOIN thanh_vien_lop m ON m.lop_id=l.id LEFT JOIN bai_nop s ON s.bai_tap_id=b.id AND s.sinh_vien_id=m.sinh_vien_id WHERE m.sinh_vien_id=? AND b.trang_thai='ACTIVE' ORDER BY b.han_nop",new String[]{String.valueOf(maTaiKhoan)});
        while(a.moveToNext())csv.append("Giáo viên giao,").append(oDuLieuCsv(a.getString(0))).append(',').append(oDuLieuCsv(a.getString(1))).append(',').append(oDuLieuCsv(dinhDangNgay(a.getLong(2)))).append(',').append(oDuLieuCsv(a.isNull(3)?"Chưa nộp":tenTrangThai(a.getString(3)))).append(',').append(a.isNull(4)?"":a.getDouble(4)).append(',').append('\n');a.close();
        Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/csv");share.putExtra(Intent.EXTRA_SUBJECT,"Báo cáo bài tập cá nhân");share.putExtra(Intent.EXTRA_TEXT,csv.toString());startActivity(Intent.createChooser(share,"Xuất dữ liệu CSV"));
    }

    private void chonNgayGio(long initial,java.util.function.LongConsumer consumer){Calendar calendar=Calendar.getInstance();calendar.setTimeInMillis(initial);new DatePickerDialog(this,(view,y,m,day)->{Calendar chosen=Calendar.getInstance();chosen.set(y,m,day);new TimePickerDialog(this,(v,h,min)->{chosen.set(Calendar.HOUR_OF_DAY,h);chosen.set(Calendar.MINUTE,min);chosen.set(Calendar.SECOND,0);consumer.accept(chosen.getTimeInMillis());},calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();}

    private void datTieuDe(String datTieuDe,String nhanPhu,String anhBia,String moTaBia,String section){tieuDeTrang.setText(datTieuDe);nhanAnhBia.setText(nhanPhu);tieuDeAnhBia.setText(anhBia);moTaAnhBia.setText(moTaBia);tieuDeMuc.setText(section);}
    private void themNutCuoiManHinh(String nhan,Runnable hanhDong){actionBottom.setVisibility(View.VISIBLE);themNutThaoTac(actionBottom,nhan,hanhDong);}
    private void themNutThaoTac(LinearLayout khungCha,String nhan,Runnable hanhDong){TextView v=new TextView(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,doiDonViDp(42),1);if(khungCha.getChildCount()>0)p.leftMargin=doiDonViDp(8);v.setLayoutParams(p);v.setBackgroundResource(R.drawable.bg_home_pill);v.setGravity(Gravity.CENTER);v.setPadding(doiDonViDp(8),0,doiDonViDp(8),0);v.setText(nhan);v.setTextColor(MAU_TIM);v.setTextSize(13);v.setTypeface(null,1);v.setClickable(true);v.setFocusable(true);v.setOnClickListener(view->hanhDong.run());khungCha.addView(v);}
    private void themNutDieuHuong(String nhan,Runnable hanhDong){TextView v=new TextView(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,doiDonViDp(40),1);v.setLayoutParams(p);v.setGravity(Gravity.CENTER);v.setText(nhan);v.setTextSize(12);v.setTextColor(MAU_TIM);v.setOnClickListener(view->hanhDong.run());thanhDieuHuong.addView(v);}
    private void themMucDanhSach(String datTieuDe,String moTa,String trangThaiMuc,Runnable hanhDong){
        danhSach.add(new MucDanhSach(datTieuDe,moTa,trangThaiMuc,hanhDong));
        if(danhSachAdapter!=null)danhSachAdapter.notifyDataSetChanged();
    }
    private void themDanhSachRong(String message){themMucDanhSach(message,"","",null);}
    private LinearLayout taoKhungBieuMau(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(doiDonViDp(20),doiDonViDp(8),doiDonViDp(20),doiDonViDp(4));return f;}
    private EditText taoONhap(LinearLayout khungCha,String nhan,String hint,boolean multiline){TextView t=new TextView(this);t.setText(nhan);t.setTextColor(MAU_CHU);t.setTextSize(13);t.setTypeface(null,1);LinearLayout.LayoutParams tl=new LinearLayout.LayoutParams(-1,-2);tl.topMargin=doiDonViDp(8);khungCha.addView(t,tl);EditText e=new EditText(this);e.setHint(hint);e.setTextSize(14);e.setSingleLine(!multiline);if(multiline){e.setGravity(Gravity.TOP);e.setMinLines(2);}e.setPadding(doiDonViDp(12),doiDonViDp(8),doiDonViDp(12),doiDonViDp(8));e.setBackgroundResource(R.drawable.bg_home_card);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.topMargin=doiDonViDp(4);khungCha.addView(e,ep);return e;}
    private ArrayAdapter<String> taoBoChonChu(String[] items){ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,items);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return a;}
    private ArrayAdapter<LuaChon> taoBoChonTaiKhoan(ArrayList<LuaChon> values){ArrayAdapter<LuaChon>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,values);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return a;}
    private ArrayList<LuaChon> layLuaChon(Cursor c,boolean laSinhVien){ArrayList<LuaChon> out=new ArrayList<>();while(c.moveToNext()){String nhan=tenHienThi(c.getString(1),c.getString(2));if(laSinhVien&&c.getColumnCount()>3&&!c.isNull(3))nhan+=" · "+c.getString(3);out.add(new LuaChon(c.getInt(0),nhan));}c.close();return out;}
    private android.widget.AdapterView.OnItemSelectedListener xuLyChonDanhSach(Runnable action){return new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){action.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};}
    private String tenHienThi(String ten,String email){return ten==null||ten.trim().isEmpty()?email:ten;}
    private String dinhDangNgay(long value){return new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date(value));}
    private String dinhDangNgayGio(long value){return new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(value));}
    private String tenTrangThai(String s){if(s==null)return "Chưa nộp";switch(s){case "SUBMITTED":return "Đã nộp";case "LATE":return "Nộp trễ";case "GRADED":return "Đã chấm";case "RESUBMIT":return "Cần nộp lại";case "WITHDRAWN":return "Đã thu hồi";default:return s;}}
    private int demBanGhi(SQLiteDatabase d,String sql){Cursor c=d.rawQuery(sql,null);int demBanGhi=c.moveToFirst()?c.getInt(0):0;c.close();return demBanGhi;}
    private String tenLop(int id){Cursor c=coSoDuLieu.getReadableDatabase().rawQuery("SELECT ma_lop||' · '||ten_hoc_phan FROM lop_hoc_phan WHERE id=?",new String[]{String.valueOf(id)});String s=c.moveToFirst()?c.getString(0):"";c.close();return s;}
    private String layTenTep(Uri uri){String ten="Tệp đính kèm";Cursor c=null;try{c=getContentResolver().query(uri,null,null,null,null);if(c!=null&&c.moveToFirst()){int i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)ten=c.getString(i);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return ten;}
    private void moTep(String value){if(value==null||value.isEmpty())return;try{Uri uri=Uri.parse(value);Intent intent=new Intent(Intent.ACTION_VIEW);String mime=getContentResolver().getType(uri);if(mime==null)intent.setData(uri);else intent.setDataAndType(uri,mime);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(intent);}catch(Exception e){hienThongBao("Không mở được tệp đính kèm trên thiết bị.");}}
    private String oDuLieuCsv(String value){if(value==null)return "\"\"";return "\""+value.replace("\"","\"\"").replace("\n"," ")+"\"";}
    private int doiDonViDp(int px){return Math.round(px*getResources().getDisplayMetrics().density);}
    private void hienThongBao(String text){Toast.makeText(this,text,Toast.LENGTH_SHORT).show();}
    private void dangXuat(){quanLyPhien.xoaPhienDangNhap();moManHinhChao();}
    private void moManHinhChao(){Intent i=new Intent(this,WelcomeActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivity(i);finish();}
}
