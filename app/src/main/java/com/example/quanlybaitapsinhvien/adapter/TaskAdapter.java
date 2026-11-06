package com.example.quanlybaitapsinhvien.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlybaitapsinhvien.R;
import com.example.quanlybaitapsinhvien.model.Task;
import com.example.quanlybaitapsinhvien.model.TaskProgress;
import com.example.quanlybaitapsinhvien.model.TaskWithProgress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskWithProgress taskWithProgress);
    }

    private List<TaskWithProgress> taskList;
    private final OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public TaskAdapter(List<TaskWithProgress> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateData(List<TaskWithProgress> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskWithProgress item = taskList.get(position);
        Task task = item.getTask();
        TaskProgress progress = item.getProgress();

        holder.tvTitle.setText(task.getTitle());
        holder.tvSubject.setText(item.getSubjectOrClassName());
        holder.tvDesc.setText(task.getDescription() != null ? task.getDescription() : "");
        holder.tvDueDate.setText("⏰ Hạn: " + task.getDueDate());

        // Source Tag Badge
        if (task.isPersonal()) {
            holder.tvSourceTag.setText("Cá nhân");
            holder.tvSourceTag.setBackgroundColor(Color.parseColor("#713E87"));
        } else {
            holder.tvSourceTag.setText("Giáo viên giao");
            holder.tvSourceTag.setBackgroundColor(Color.parseColor("#0288D1"));
        }

        // Priority Badge
        String priority = (progress != null) ? progress.getPriority() : TaskProgress.PRIORITY_MEDIUM;
        holder.tvPriority.setText("Ưu tiên: " + priority);

        // Status Badge & Overdue Check
        String status = (progress != null) ? progress.getStatus() : TaskProgress.STATUS_NOT_STARTED;
        boolean isOverdue = false;
        try {
            Date dueDate = dateFormat.parse(task.getDueDate());
            if (dueDate != null && dueDate.before(new Date()) && !TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(status)) {
                isOverdue = true;
            }
        } catch (ParseException e) {
            // Ignore parse exception
        }

        if (isOverdue) {
            holder.tvStatus.setText("Quá hạn");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#D32F2F"));
            holder.tvStatus.setTextColor(Color.WHITE);
        } else if (TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Hoàn thành");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#388E3C"));
            holder.tvStatus.setTextColor(Color.WHITE);
        } else if (TaskProgress.STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Đang làm");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#F57C00"));
            holder.tvStatus.setTextColor(Color.WHITE);
        } else {
            holder.tvStatus.setText("Chưa làm");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#757575"));
            holder.tvStatus.setTextColor(Color.WHITE);
        }

        // Checklist Progress
        if (item.getTotalChecklistCount() > 0) {
            holder.layoutProgressBar.setVisibility(View.VISIBLE);
            int pct = item.getProgressPercentage();
            holder.pbChecklist.setProgress(pct);
            holder.tvProgressPercent.setText(pct + "% (" + item.getCompletedChecklistCount() + "/" + item.getTotalChecklistCount() + ")");
        } else {
            holder.layoutProgressBar.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvSourceTag, tvSubject, tvPriority, tvTitle, tvDesc, tvDueDate, tvStatus, tvProgressPercent;
        ProgressBar pbChecklist;
        View layoutProgressBar;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSourceTag = itemView.findViewById(R.id.tv_source_tag);
            tvSubject = itemView.findViewById(R.id.tv_subject_name);
            tvPriority = itemView.findViewById(R.id.tv_priority_tag);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            tvProgressPercent = itemView.findViewById(R.id.tv_progress_percent);
            pbChecklist = itemView.findViewById(R.id.pb_checklist);
            layoutProgressBar = itemView.findViewById(R.id.layout_progress_bar);
        }
    }
}
