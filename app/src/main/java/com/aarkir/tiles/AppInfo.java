/*
A Class meant to hold application data that is easier to access than with PackageInfo
 */

package com.aarkir.tiles;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appName;
    private String packageName;
    private String className;
    private String versionName;
    private int versionCode;
    private Drawable icon;
    private int frequency = 1;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getFrequency () {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}