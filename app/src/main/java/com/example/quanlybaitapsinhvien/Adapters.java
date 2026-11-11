package com.example.quanlybaitapsinhvien;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Container tổng hợp tất cả các RecyclerView Adapters của ứng dụng
 */
public class Adapters {
}

/**
 * Adapter hiển thị danh sách bài tập cho Sinh viên
 */
class BaiTapAdapter extends RecyclerView.Adapter<BaiTapAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(BaiTap baiTap);
    }

    private final Context context;
    private final List<BaiTap> list;
    private final OnItemClickListener listener;

    public BaiTapAdapter(Context context, List<BaiTap> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bai_tap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaiTap bt = list.get(position);

        holder.tvTenBaiTap.setText(bt.getTenBaiTap());
        holder.tvNguon.setText(bt.getTenNguon() != null ? bt.getTenNguon() : "Bài tập");
        holder.tvHanNop.setText("Hạn nộp: " + bt.getHanNop());
        holder.tvPercent.setText(bt.getProgressPercent() + "%");
        holder.progressBar.setProgress(bt.getProgressPercent());

        if ("TEACHER".equals(bt.getNguonBai())) {
            holder.tvTagNguon.setText("Giáo viên giao");
            holder.tvTagNguon.setBackgroundResource(R.drawable.bg_badge_teacher);
            holder.tvTagNguon.setTextColor(Color.parseColor("#713E87"));
        } else {
            holder.tvTagNguon.setText("Cá nhân");
            holder.tvTagNguon.setBackgroundResource(R.drawable.bg_badge_personal);
            holder.tvTagNguon.setTextColor(Color.parseColor("#0284C7"));
        }

        if (bt.isOverdue()) {
            holder.tvTagQuaHan.setVisibility(View.VISIBLE);
        } else {
            holder.tvTagQuaHan.setVisibility(View.GONE);
        }

        holder.tvTrangThai.setText(bt.getTrangThai() != null ? bt.getTrangThai() : "Chưa làm");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bt);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBaiTap, tvNguon, tvHanNop, tvPercent, tvTagNguon, tvTagQuaHan, tvTrangThai;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBaiTap = itemView.findViewById(R.id.tvTenBaiTap);
            tvNguon = itemView.findViewById(R.id.tvNguon);
            tvHanNop = itemView.findViewById(R.id.tvHanNop);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            tvTagNguon = itemView.findViewById(R.id.tvTagNguon);
            tvTagQuaHan = itemView.findViewById(R.id.tvTagQuaHan);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

/**
 * Adapter danh sách bài tập đã giao của Giáo viên
 */
class BaiTapTeacherAdapter extends RecyclerView.Adapter<BaiTapTeacherAdapter.ViewHolder> {

    public interface OnTeacherAssignmentActionListener {
        void onEdit(BaiTap baiTap);
        void onDelete(BaiTap baiTap);
    }

    private final Context context;
    private final List<BaiTap> list;
    private final OnTeacherAssignmentActionListener listener;

    public BaiTapTeacherAdapter(Context context, List<BaiTap> list, OnTeacherAssignmentActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bai_tap_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaiTap bt = list.get(position);

        holder.tvTenBaiTap.setText(bt.getTenBaiTap());
        holder.tvMoTa.setText(bt.getMoTa() != null ? bt.getMoTa() : "Không có mô tả");
        holder.tvHanNop.setText("Hạn nộp: " + bt.getHanNop());

        holder.btnSua.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(bt);
        });

        holder.btnThuHoi.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(bt);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBaiTap, tvMoTa, tvHanNop;
        Button btnSua, btnThuHoi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBaiTap = itemView.findViewById(R.id.tvTenBaiTapTeacher);
            tvMoTa = itemView.findViewById(R.id.tvMoTaTeacher);
            tvHanNop = itemView.findViewById(R.id.tvHanNopTeacher);
            btnSua = itemView.findViewById(R.id.btnSuaBaiTeacher);
            btnThuHoi = itemView.findViewById(R.id.btnThuHoiBaiTeacher);
        }
    }
}

/**
 * Adapter danh sách bài tập toàn hệ thống cho Admin
 */
class BaiTapAdminAdapter extends RecyclerView.Adapter<BaiTapAdminAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(BaiTap baiTap);
    }

    private final Context context;
    private final List<BaiTap> list;
    private final OnDeleteClickListener listener;

    public BaiTapAdminAdapter(Context context, List<BaiTap> list, OnDeleteClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bai_tap_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaiTap bt = list.get(position);

        holder.tvTenBaiTap.setText(bt.getTenBaiTap());
        holder.tvNguon.setText("Nguồn: " + (bt.getTenNguon() != null ? bt.getTenNguon() : "Cá nhân") + " | Hạn: " + bt.getHanNop());

        holder.btnXoa.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(bt);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBaiTap, tvNguon;
        ImageView btnXoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBaiTap = itemView.findViewById(R.id.tvTenBaiTapAdmin);
            tvNguon = itemView.findViewById(R.id.tvNguonAdmin);
            btnXoa = itemView.findViewById(R.id.btnXoaBaiTapAdmin);
        }
    }
}

/**
 * Adapter hiển thị Checklist công việc con
 */
class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    public interface OnChecklistChangeListener {
        void onCheckChanged(ChecklistItem item, boolean isChecked);
        void onDeleteItem(ChecklistItem item);
    }

    private final Context context;
    private final List<ChecklistItem> list;
    private final OnChecklistChangeListener listener;

    public ChecklistAdapter(Context context, List<ChecklistItem> list, OnChecklistChangeListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistItem item = list.get(position);

        holder.tvNoiDung.setText(item.getNoiDung());
        holder.cbChecklist.setOnCheckedChangeListener(null);
        holder.cbChecklist.setChecked(item.isChecked());

        if (item.isChecked()) {
            holder.tvNoiDung.setPaintFlags(holder.tvNoiDung.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvNoiDung.setPaintFlags(holder.tvNoiDung.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbChecklist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCheckChanged(item, isChecked);
            }
        });

        holder.btnXoa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecklist;
        TextView tvNoiDung;
        ImageView btnXoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecklist = itemView.findViewById(R.id.cbChecklist);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDungChecklist);
            btnXoa = itemView.findViewById(R.id.btnXoaChecklist);
        }
    }
}

/**
 * Adapter Báo cáo tiến độ cho Giáo viên
 */
class BaoCaoTienDoAdapter extends RecyclerView.Adapter<BaoCaoTienDoAdapter.ViewHolder> {

    private final Context context;
    private final List<TienDoSinhVien> list;

    public BaoCaoTienDoAdapter(Context context, List<TienDoSinhVien> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bao_cao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TienDoSinhVien td = list.get(position);

        holder.tvTenSinhVien.setText(td.getTenSinhVien() != null ? td.getTenSinhVien() : "Sinh viên #" + td.getSinhVienId());
        holder.tvTrangThai.setText(td.getTrangThai());
        holder.tvMucUuTien.setText("Mức ưu tiên: " + td.getMucUuTien());
        holder.tvPercent.setText(td.getPercentTienDo() + "%");
        holder.progressBar.setProgress(td.getPercentTienDo());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenSinhVien, tvTrangThai, tvMucUuTien, tvPercent;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenSinhVien = itemView.findViewById(R.id.tvTenSinhVien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvMucUuTien = itemView.findViewById(R.id.tvMucUuTien);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

/**
 * Adapter quản lý Tài khoản cho Admin
 */
class TaiKhoanAdapter extends RecyclerView.Adapter<TaiKhoanAdapter.ViewHolder> {

    public interface OnAccountStatusClickListener {
        void onToggleStatus(TaiKhoan taiKhoan);
    }

    private final Context context;
    private final List<TaiKhoan> list;
    private final OnAccountStatusClickListener listener;

    public TaiKhoanAdapter(Context context, List<TaiKhoan> list, OnAccountStatusClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tai_khoan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaiKhoan tk = list.get(position);

        holder.tvHoTen.setText(tk.getHoTen() + (tk.getTrangThai() == 0 ? " (ĐÃ KHÓA)" : ""));
        holder.tvUsername.setText("@" + tk.getUsername() + " | Vai trò: " + tk.getVaiTro());

        if ("ADMIN".equals(tk.getVaiTro())) {
            holder.btnKhoa.setVisibility(View.GONE);
        } else {
            holder.btnKhoa.setVisibility(View.VISIBLE);
            if (tk.getTrangThai() == 1) {
                holder.btnKhoa.setText("Khóa");
                holder.btnKhoa.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.btnKhoa.setText("Mở khóa");
                holder.btnKhoa.setTextColor(Color.parseColor("#16A34A"));
            }
        }

        holder.btnKhoa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleStatus(tk);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHoTen, tvUsername;
        Button btnKhoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHoTen = itemView.findViewById(R.id.tvHoTenAccount);
            tvUsername = itemView.findViewById(R.id.tvUsernameAccount);
            btnKhoa = itemView.findViewById(R.id.btnKhoaMoKhoa);
        }
    }
}

/**
 * Adapter quản lý Lớp học phần cho Admin
 */
class LopHocAdapter extends RecyclerView.Adapter<LopHocAdapter.ViewHolder> {

    public interface OnLopHocActionListener {
        void onDoiGv(LopHoc lopHoc);
        void onThemSv(LopHoc lopHoc);
        void onBotSv(LopHoc lopHoc);
    }

    private final Context context;
    private final List<LopHoc> list;
    private final OnLopHocActionListener listener;

    public LopHocAdapter(Context context, List<LopHoc> list, OnLopHocActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lop_hoc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LopHoc lh = list.get(position);

        holder.tvTenLop.setText(lh.getMaLop() + " - " + lh.getTenLop());
        holder.tvGiaoVien.setText("Giáo viên: " + (lh.getTenGiaoVien() != null ? lh.getTenGiaoVien() : "Chưa phân công"));

        holder.btnDoiGv.setOnClickListener(v -> {
            if (listener != null) listener.onDoiGv(lh);
        });

        holder.btnThemSv.setOnClickListener(v -> {
            if (listener != null) listener.onThemSv(lh);
        });

        holder.btnBotSv.setOnClickListener(v -> {
            if (listener != null) listener.onBotSv(lh);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenLop, tvGiaoVien;
        Button btnDoiGv, btnThemSv, btnBotSv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenLop = itemView.findViewById(R.id.tvTenLop);
            tvGiaoVien = itemView.findViewById(R.id.tvGiaoVien);
            btnDoiGv = itemView.findViewById(R.id.btnDoiGv);
            btnThemSv = itemView.findViewById(R.id.btnThemSv);
            btnBotSv = itemView.findViewById(R.id.btnBotSv);
        }
    }
}
