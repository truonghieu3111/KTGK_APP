package com.example.taskmanager.ui;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.taskmanager.R;
import com.example.taskmanager.model.Task;


public final class DialogHelper {

    public interface OnConfirm {
        void onConfirm();
    }

    private DialogHelper() {}

    public static void showDelete(Context context, Task task, OnConfirm callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
        TextView tvMessage = view.findViewById(R.id.tv_delete_message);
        TextView tvChip = view.findViewById(R.id.tv_delete_chip);

        tvMessage.setText("“" + task.getTitle() + "” sẽ bị xóa vĩnh viễn. Bạn không thể hoàn tác.");
        tvChip.setText(task.getCode() + "  ·  " + task.getStatusLabel());

        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        view.findViewById(R.id.btn_keep).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm_delete).setOnClickListener(v -> {
            dialog.dismiss();
            callback.onConfirm();
        });
        dialog.show();
    }
}
