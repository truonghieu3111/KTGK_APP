package com.example.taskmanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskmanager.R;
import com.example.taskmanager.data.TaskStorage;
import com.example.taskmanager.model.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;


public class StatsFragment extends Fragment {

    private TaskStorage storage;
    private TextView tvLabel, tvPercent, tvSummary, tvTotal, tvDone, tvTodo;
    private TextView tvCountHigh, tvCountMed, tvCountLow;
    private LinearProgressIndicator progress, barHigh, barMed, barLow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storage = new TaskStorage(requireContext());

        tvLabel = view.findViewById(R.id.tv_stat_label);
        tvPercent = view.findViewById(R.id.tv_stat_percent);
        tvSummary = view.findViewById(R.id.tv_stat_summary);
        tvTotal = view.findViewById(R.id.tv_total);
        tvDone = view.findViewById(R.id.tv_done);
        tvTodo = view.findViewById(R.id.tv_todo);
        tvCountHigh = view.findViewById(R.id.tv_count_high);
        tvCountMed = view.findViewById(R.id.tv_count_med);
        tvCountLow = view.findViewById(R.id.tv_count_low);
        progress = view.findViewById(R.id.progress_stat);
        barHigh = view.findViewById(R.id.bar_high);
        barMed = view.findViewById(R.id.bar_med);
        barLow = view.findViewById(R.id.bar_low);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindStats();
    }

    private void bindStats() {
        List<Task> all = storage.getAll();
        int total = all.size();
        int done = 0, high = 0, med = 0, low = 0;
        for (Task t : all) {
            if (t.isDone()) done++;
            switch (t.getPriority()) {
                case Task.PRIORITY_HIGH: high++; break;
                case Task.PRIORITY_LOW: low++; break;
                default: med++; break;
            }
        }
        int todo = total - done;
        int percent = total == 0 ? 0 : done * 100 / total;

        if (total == 0) {
            tvLabel.setText("CHƯA CÓ CÔNG VIỆC");
        } else if (percent >= 50) {
            tvLabel.setText("BẠN ĐANG LÀM RẤT TỐT");
        } else {
            tvLabel.setText("HÃY TIẾP TỤC CỐ GẮNG");
        }
        tvPercent.setText(percent + "%");
        progress.setProgress(percent);
        tvSummary.setText(done + " việc đã xong · Còn " + todo + " việc đang chờ bạn");

        tvTotal.setText(String.valueOf(total));
        tvDone.setText(String.valueOf(done));
        tvTodo.setText(String.valueOf(todo));

        tvCountHigh.setText(String.valueOf(high));
        tvCountMed.setText(String.valueOf(med));
        tvCountLow.setText(String.valueOf(low));
        setBar(barHigh, high, total);
        setBar(barMed, med, total);
        setBar(barLow, low, total);
    }

    private void setBar(LinearProgressIndicator bar, int count, int total) {
        bar.setProgress(total == 0 ? 0 : count * 100 / total);
    }
}
