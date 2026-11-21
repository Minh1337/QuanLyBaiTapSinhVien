package com.example.quanlybaitapsinhvien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/** Adapter gắn dữ liệu hàng vào giao diện ListView. */
public class MucDanhSachAdapter extends ArrayAdapter<MucDanhSach> {
    public MucDanhSachAdapter(Context context, ArrayList<MucDanhSach> danhSach) {
        super(context, 0, danhSach);
    }

    @Override
    public View getView(int viTri, View hangCu, ViewGroup phuHuynh) {
        View hang = hangCu;
        if (hang == null) {
            hang = LayoutInflater.from(getContext()).inflate(R.layout.item_muc_danh_sach, phuHuynh, false);
        }

        MucDanhSach muc = getItem(viTri);
        TextView tieuDe = hang.findViewById(R.id.tvTenMuc);
        TextView moTa = hang.findViewById(R.id.tvMoTaMuc);
        TextView trangThai = hang.findViewById(R.id.tvTrangThaiMuc);
        tieuDe.setText(muc.getTieuDe());
        moTa.setText(muc.getMoTa());
        trangThai.setText(muc.getTrangThai());
        trangThai.setVisibility(muc.getTrangThai().isEmpty() ? View.GONE : View.VISIBLE);
        return hang;
    }
}
