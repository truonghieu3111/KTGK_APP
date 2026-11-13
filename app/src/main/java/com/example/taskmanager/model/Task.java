package com.example.taskmanager.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class Task {
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    private int id;
    private String title = "";
    private String description = "";
    private long dueDate;
    private int priority = PRIORITY_MEDIUM;
    private boolean done;
    private long createdAt;

    public String getCode() {
        return String.format(Locale.US, "CV-%03d", id);
    }

    public static String priorityLabel(int priority) {
        switch (priority) {
            case PRIORITY_LOW:
                return "Thấp";
            case PRIORITY_HIGH:
                return "Cao";
            default:
                return "Trung bình";
        }
    }

    public String getStatusLabel() {
        return done ? "Đã hoàn thành" : "Chưa thực hiện";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("title", title);
        o.put("desc", description);
        o.put("due", dueDate);
        o.put("prio", priority);
        o.put("done", done);
        o.put("created", createdAt);
        return o;
    }

    public static Task fromJson(JSONObject o) {
        Task t = new Task();
        t.id = o.optInt("id");
        t.title = o.optString("title", "");
        t.description = o.optString("desc", "");
        t.dueDate = o.optLong("due");
        t.priority = o.optInt("prio", PRIORITY_MEDIUM);
        t.done = o.optBoolean("done", false);
        t.createdAt = o.optLong("created");
        return t;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
