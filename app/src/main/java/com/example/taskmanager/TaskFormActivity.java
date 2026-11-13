package com.example.taskmanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.taskmanager.data.TaskStorage;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.ui.DialogHelper;
import com.example.taskmanager.util.DateHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;


public class TaskFormActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";

    private TaskStorage storage;
    private Task editing;
    private long dueDate = DateHelper.today();
    private int priority = Task.PRIORITY_MEDIUM;

    private EditText etTitle, etDesc;
    private TextView tvTitleError, tvDescCount, tvDate;
    private TextView tvPrioLow, tvPrioMed, tvPrioHigh;
    private SwitchMaterial swDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);
        storage = new TaskStorage(this);

        int id = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        if (id != -1) editing = storage.getById(id);

        bindViews();
        setupListeners();

        if (editing == null) {
            setupAddMode();
        } else {
            setupEditMode();
        }
        tvDate.setText(DateHelper.formatFull(dueDate));
        updatePriorityUi();
    }

    private void bindViews() {
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        tvTitleError = findViewById(R.id.tv_title_error);
        tvDescCount = findViewById(R.id.tv_desc_count);
        tvDate = findViewById(R.id.tv_date);
        tvPrioLow = findViewById(R.id.tv_prio_low);
        tvPrioMed = findViewById(R.id.tv_prio_med);
        tvPrioHigh = findViewById(R.id.tv_prio_high);
        swDone = findViewById(R.id.sw_done);
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etTitle.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) hideTitleError();
            }
        });

        etDesc.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tvDescCount.setText(s.length() + "/500");
            }
        });

        findViewById(R.id.layout_date).setOnClickListener(v -> showDatePicker());

        tvPrioLow.setOnClickListener(v -> { priority = Task.PRIORITY_LOW; updatePriorityUi(); });
        tvPrioMed.setOnClickListener(v -> { priority = Task.PRIORITY_MEDIUM; updatePriorityUi(); });
        tvPrioHigh.setOnClickListener(v -> { priority = Task.PRIORITY_HIGH; updatePriorityUi(); });

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void setupAddMode() {
        ((TextView) findViewById(R.id.tv_screen_title)).setText("Thêm công việc");
        ((TextView) findViewById(R.id.tv_form_heading)).setText("Bắt đầu một việc có ý nghĩa");
        ((TextView) findViewById(R.id.tv_form_sub)).setText("Sắp xếp rõ ràng, hoàn thành nhẹ nhàng.");
        ((MaterialButton) findViewById(R.id.btn_save)).setText("Tạo công việc");
    }

    private void setupEditMode() {
        ((TextView) findViewById(R.id.tv_screen_title)).setText("Chỉnh sửa");
        ((TextView) findViewById(R.id.tv_form_heading)).setText(
                editing.getCode() + " · Tạo ngày " + DateHelper.formatFull(editing.getCreatedAt()));
        ((TextView) findViewById(R.id.tv_form_sub)).setText("Cập nhật thông tin và trạng thái công việc.");
        ((MaterialButton) findViewById(R.id.btn_save)).setText("Lưu thay đổi");

        findViewById(R.id.layout_add_info).setVisibility(View.GONE);
        findViewById(R.id.layout_edit_extras).setVisibility(View.VISIBLE);

        etTitle.setText(editing.getTitle());
        etDesc.setText(editing.getDescription());
        dueDate = editing.getDueDate();
        priority = editing.getPriority();
        swDone.setChecked(editing.isDone());

        View btnMore = findViewById(R.id.btn_more);
        btnMore.setVisibility(View.VISIBLE);
        btnMore.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMore);
            menu.getMenu().add("Xóa công việc");
            menu.setOnMenuItemClickListener(item -> {
                confirmDelete();
                return true;
            });
            menu.show();
        });
        findViewById(R.id.tv_delete).setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dueDate);
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(year, month, day, 0, 0, 0);
            dueDate = c.getTimeInMillis();
            tvDate.setText(DateHelper.formatFull(dueDate));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updatePriorityUi() {
        styleChoice(tvPrioLow, priority == Task.PRIORITY_LOW, R.drawable.bg_choice_low, R.color.green);
        styleChoice(tvPrioMed, priority == Task.PRIORITY_MEDIUM, R.drawable.bg_choice_med, R.color.amber);
        styleChoice(tvPrioHigh, priority == Task.PRIORITY_HIGH, R.drawable.bg_choice_high, R.color.red);
    }

    private void styleChoice(TextView tv, boolean selected, int selectedBg, int selectedColor) {
        tv.setBackgroundResource(selected ? selectedBg : R.drawable.bg_choice);
        tv.setTextColor(ContextCompat.getColor(this, selected ? selectedColor : R.color.text_gray));
    }


    private void save() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            showTitleError();
            return;
        }
        String desc = etDesc.getText().toString().trim();

        if (editing == null) {
            Task t = new Task();
            t.setTitle(title);
            t.setDescription(desc);
            t.setDueDate(dueDate);
            t.setPriority(priority);
            t.setDone(false);
            t.setCreatedAt(DateHelper.today());
            storage.add(t);
            Toast.makeText(this, "Đã tạo công việc", Toast.LENGTH_SHORT).show();
        } else {
            editing.setTitle(title);
            editing.setDescription(desc);
            editing.setDueDate(dueDate);
            editing.setPriority(priority);
            editing.setDone(swDone.isChecked());
            storage.update(editing);
            Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showTitleError() {
        etTitle.setBackgroundResource(R.drawable.bg_input_error);
        tvTitleError.setVisibility(View.VISIBLE);
        etTitle.requestFocus();
    }

    private void hideTitleError() {
        etTitle.setBackgroundResource(R.drawable.bg_input);
        tvTitleError.setVisibility(View.GONE);
    }

    private void confirmDelete() {
        if (editing == null) return;
        DialogHelper.showDelete(this, editing, () -> {
            storage.delete(editing.getId());
            Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
