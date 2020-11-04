package com.rnsantos.dofjava.model;

public class Task {
    private final int id;
    private String title;
    private String activity;
    private String date;
    private int status;

    public Task(int id, String title, String activity, String date, int status) {
        this.id = id;
        this.title = title;
        this.activity = activity;
        this.status = status;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {  return status; }

    public void setStatus(int status) {
        this.status = status;
    }
}
