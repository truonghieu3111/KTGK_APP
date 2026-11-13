package com.example.taskmanager.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.taskmanager.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class TaskStorage {
    private static final String PREFS_NAME = "task_prefs";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_NEXT_ID = "next_id";

    private final SharedPreferences prefs;

    public TaskStorage(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<Task> getAll() {
        List<Task> list = new ArrayList<>();
        String json = prefs.getString(KEY_TASKS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(Task.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Task getById(int id) {
        for (Task t : getAll()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    public Task add(Task task) {
        List<Task> list = getAll();
        int id = prefs.getInt(KEY_NEXT_ID, 1);
        task.setId(id);
        list.add(task);
        prefs.edit()
                .putString(KEY_TASKS, toJson(list))
                .putInt(KEY_NEXT_ID, id + 1)
                .apply();
        return task;
    }

    public void update(Task task) {
        List<Task> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == task.getId()) {
                list.set(i, task);
                break;
            }
        }
        save(list);
    }

    public void setDone(int id, boolean done) {
        List<Task> list = getAll();
        for (Task t : list) {
            if (t.getId() == id) {
                t.setDone(done);
                break;
            }
        }
        save(list);
    }

    public void delete(int id) {
        List<Task> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                list.remove(i);
                break;
            }
        }
        save(list);
    }

    private void save(List<Task> list) {
        prefs.edit().putString(KEY_TASKS, toJson(list)).apply();
    }

    private String toJson(List<Task> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Task t : list) {
                JSONObject o = t.toJson();
                arr.put(o);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arr.toString();
    }
}
