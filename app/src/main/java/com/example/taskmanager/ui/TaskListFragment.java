package com.example.taskmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskFormActivity;
import com.example.taskmanager.data.TaskStorage;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.util.DateHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class TaskListFragment extends Fragment {

    private static final int STATUS_ALL = 0;
    private static final int STATUS_TODO = 1;
    private static final int STATUS_DONE = 2;
    private static final int PRIORITY_ALL = -1;

    private TaskStorage storage;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();

    private String query = "";
    private int statusFilter = STATUS_ALL;
    private int priorityFilter = PRIORITY_ALL;

    private TextView tvProgressCount, tvProgressText, tabAll, tabTodo, tabDone, tvEmpty;
    private LinearProgressIndicator progressOverall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storage = new TaskStorage(requireContext());

        ((TextView) view.findViewById(R.id.tv_date_label)).setText(DateHelper.headerLabel());
        tvProgressCount = view.findViewById(R.id.tv_progress_count);
        tvProgressText = view.findViewById(R.id.tv_progress_text);
        progressOverall = view.findViewById(R.id.progress_overall);
        tabAll = view.findViewById(R.id.tab_all);
        tabTodo = view.findViewById(R.id.tab_todo);
        tabDone = view.findViewById(R.id.tab_done);
        tvEmpty = view.findViewById(R.id.tv_empty);
        EditText etSearch = view.findViewById(R.id.et_search);

        RecyclerView rv = view.findViewById(R.id.rv_tasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(new TaskAdapter.Listener() {
            @Override
            public void onToggle(Task task, boolean done) {
                storage.setDone(task.getId(), done);
                loadData();
            }

            @Override
            public void onClick(Task task) {
                Intent i = new Intent(requireContext(), TaskFormActivity.class);
                i.putExtra(TaskFormActivity.EXTRA_TASK_ID, task.getId());
                startActivity(i);
            }

            @Override
            public void onLongClick(Task task) {
                DialogHelper.showDelete(requireContext(), task, () -> {
                    storage.delete(task.getId());
                    loadData();
                });
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TaskFormActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                applyFilter();
            }
        });

        tabAll.setOnClickListener(v -> { statusFilter = STATUS_ALL; applyFilter(); });
        tabTodo.setOnClickListener(v -> { statusFilter = STATUS_TODO; applyFilter(); });
        tabDone.setOnClickListener(v -> { statusFilter = STATUS_DONE; applyFilter(); });

        view.findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterSheet());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        allTasks = storage.getAll();
        Collections.sort(allTasks, (a, b) -> Integer.compare(b.getId(), a.getId())); // mới nhất trước
        updateProgressCard();
        applyFilter();
    }

    private void updateProgressCard() {
        int total = allTasks.size();
        int done = 0;
        for (Task t : allTasks) if (t.isDone()) done++;
        int percent = total == 0 ? 0 : done * 100 / total;
        tvProgressCount.setText(done + "/" + total);
        tvProgressText.setText("Bạn đã hoàn thành " + percent + "% công việc.");
        progressOverall.setProgress(percent);
    }

    private boolean matches(Task t, String q, int status, int priority) {
        if (!q.isEmpty() && !t.getTitle().toLowerCase(Locale.getDefault())
                .contains(q.toLowerCase(Locale.getDefault()))) return false;
        if (status == STATUS_TODO && t.isDone()) return false;
        if (status == STATUS_DONE && !t.isDone()) return false;
        return priority == PRIORITY_ALL || t.getPriority() == priority;
    }

    private List<Task> filter(int status, int priority) {
        List<Task> result = new ArrayList<>();
        for (Task t : allTasks) {
            if (matches(t, query, status, priority)) result.add(t);
        }
        return result;
    }

    private void applyFilter() {
        List<Task> result = filter(statusFilter, priorityFilter);
        adapter.submit(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);

        int done = 0;
        for (Task t : allTasks) if (t.isDone()) done++;
        tabAll.setText("Tất cả " + allTasks.size());
        tabTodo.setText("Chưa làm " + (allTasks.size() - done));
        tabDone.setText("Đã xong " + done);
        setTab(tabAll, statusFilter == STATUS_ALL);
        setTab(tabTodo, statusFilter == STATUS_TODO);
        setTab(tabDone, statusFilter == STATUS_DONE);
    }

    private void setTab(TextView tv, boolean selected) {
        tv.setBackgroundResource(selected ? R.drawable.bg_tab_selected : 0);
        tv.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.primary : R.color.text_gray));
    }

    private void showFilterSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(v);

        RadioGroup rg = v.findViewById(R.id.rg_status);
        TextView[] chips = {
                v.findViewById(R.id.tv_f_all), v.findViewById(R.id.tv_f_low),
                v.findViewById(R.id.tv_f_med), v.findViewById(R.id.tv_f_high)
        };
        MaterialButton btnApply = v.findViewById(R.id.btn_apply);

        int[] tmpStatus = {statusFilter};
        int[] tmpPriority = {priorityFilter};

        Runnable refresh = () -> {
            int checkedId = tmpStatus[0] == STATUS_ALL ? R.id.rb_all
                    : tmpStatus[0] == STATUS_TODO ? R.id.rb_todo : R.id.rb_done;
            rg.check(checkedId);
            for (int i = 0; i < chips.length; i++) {
                boolean sel = tmpPriority[0] == i - 1;
                chips[i].setBackgroundResource(sel ? R.drawable.bg_choice_primary : R.drawable.bg_choice);
                chips[i].setTextColor(ContextCompat.getColor(requireContext(),
                        sel ? R.color.primary : R.color.text_gray));
            }
            int n = filter(tmpStatus[0], tmpPriority[0]).size();
            btnApply.setText("Hiển thị " + n + " công việc");
        };

        rg.setOnCheckedChangeListener((group, id) -> {
            tmpStatus[0] = id == R.id.rb_all ? STATUS_ALL : id == R.id.rb_todo ? STATUS_TODO : STATUS_DONE;
            refresh.run();
        });
        for (int i = 0; i < chips.length; i++) {
            final int priorityValue = i - 1;
            chips[i].setOnClickListener(x -> {
                tmpPriority[0] = priorityValue;
                refresh.run();
            });
        }
        v.findViewById(R.id.tv_reset).setOnClickListener(x -> {
            tmpStatus[0] = STATUS_ALL;
            tmpPriority[0] = PRIORITY_ALL;
            refresh.run();
        });
        btnApply.setOnClickListener(x -> {
            statusFilter = tmpStatus[0];
            priorityFilter = tmpPriority[0];
            applyFilter();
            dialog.dismiss();
        });

        refresh.run();
        dialog.show();
    }
}
