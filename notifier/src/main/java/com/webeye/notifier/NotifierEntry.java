package com.webeye.notifier;


public class NotifierEntry {
    private int id;
    private String packageName;
    private String className;
    private int badgeCount;

    public NotifierEntry(int id, String packageName, String className, int badgeCount) {
        this.id = id;
        this.packageName = packageName;
        this.className = className;
        this.badgeCount = badgeCount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public int getBadgeCount() {
        return this.badgeCount;
    }

    @Override
    public String toString() {
        return "[ " + packageName + "#" + className + ", " + "badge count: " + badgeCount + " ]";
    }
}