package com.example.quanlybaitapsinhvien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.model.ChecklistItem;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {

    public interface OnChecklistActionListener {
        void onToggle(ChecklistItem item, boolean isChecked);
        void onDelete(ChecklistItem item);
    }

    private List<ChecklistItem> items;
    private final OnChecklistActionListener listener;

    public ChecklistAdapter(List<ChecklistItem> items, OnChecklistActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<ChecklistItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        ChecklistItem item = items.get(position);
        holder.tvContent.setText(item.getContent());
        holder.cbChecklist.setOnCheckedChangeListener(null);
        holder.cbChecklist.setChecked(item.isCompleted());

        holder.cbChecklist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggle(item, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecklist;
        TextView tvContent;
        ImageButton btnDelete;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecklist = itemView.findViewById(R.id.cb_checklist);
            tvContent = itemView.findViewById(R.id.tv_checklist_content);
            btnDelete = itemView.findViewById(R.id.btn_delete_checklist);
        }
    }
}
