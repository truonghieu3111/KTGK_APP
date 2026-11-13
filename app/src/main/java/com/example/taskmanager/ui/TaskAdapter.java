package com.example.taskmanager.ui;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.util.DateHelper;

import java.util.ArrayList;
import java.util.List;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface Listener {
        void onToggle(Task task, boolean done);
        void onClick(Task task);
        void onLongClick(Task task);
    }

    private final List<Task> items = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Task> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Task t = items.get(position);
        Context c = h.itemView.getContext();

        h.tvCode.setText(t.getCode());
        h.tvTitle.setText(t.getTitle());
        h.tvDesc.setText(t.getDescription());
        h.tvDesc.setVisibility(t.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
        h.cb.setChecked(t.isDone());

        h.tvStatus.setText(t.getStatusLabel());
        if (t.isDone()) {
            h.tvStatus.setTextColor(ContextCompat.getColor(c, R.color.green));
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setTextColor(ContextCompat.getColor(c, R.color.text_gray));
        } else {
            h.tvStatus.setTextColor(ContextCompat.getColor(c, R.color.text_gray));
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setTextColor(ContextCompat.getColor(c, R.color.text_dark));
        }

        h.tvDate.setText(DateHelper.formatItemDate(t.getDueDate()));
        boolean overdue = !t.isDone() && DateHelper.daysFromToday(t.getDueDate()) < 0;
        h.tvDate.setTextColor(ContextCompat.getColor(c, overdue ? R.color.red : R.color.text_gray));

        h.tvPriority.setText(Task.priorityLabel(t.getPriority()));
        switch (t.getPriority()) {
            case Task.PRIORITY_HIGH:
                h.tvPriority.setBackgroundResource(R.drawable.bg_chip_high);
                h.tvPriority.setTextColor(ContextCompat.getColor(c, R.color.red));
                break;
            case Task.PRIORITY_LOW:
                h.tvPriority.setBackgroundResource(R.drawable.bg_chip_low);
                h.tvPriority.setTextColor(ContextCompat.getColor(c, R.color.green));
                break;
            default:
                h.tvPriority.setBackgroundResource(R.drawable.bg_chip_med);
                h.tvPriority.setTextColor(ContextCompat.getColor(c, R.color.amber));
                break;
        }

        h.cb.setOnClickListener(v -> listener.onToggle(t, h.cb.isChecked()));
        h.itemView.setOnClickListener(v -> listener.onClick(t));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(t);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        final TextView tvCode, tvStatus, tvTitle, tvDesc, tvDate, tvPriority;

        VH(@NonNull View v) {
            super(v);
            cb = v.findViewById(R.id.cb_done);
            tvCode = v.findViewById(R.id.tv_code);
            tvStatus = v.findViewById(R.id.tv_status);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvDate = v.findViewById(R.id.tv_date);
            tvPriority = v.findViewById(R.id.tv_priority);
        }
    }
}
