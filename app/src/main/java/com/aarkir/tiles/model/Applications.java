/*
Holds data about the apps currently installed on the device
 */

package com.aarkir.tiles.model;

import android.content.pm.PackageInfo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import com.aarkir.tiles.AppLauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Applications {
    private ArrayList<AppInfo> apps;
    private List<ResolveInfo> activityList;
    private Intent mainIntent;
    private PackageManager packageManager;

    public Applications(PackageManager packageManager) {
        mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        this.packageManager = packageManager;
        activityList = this.createActivityList(); //used to find intent data to launch application
        apps = this.createApps();
        if (AppLauncher.getBackgrounds()) {
            setBackgrounds(apps);
        }
    }

    private ArrayList<AppInfo> createApps() {
        //list to be returned with the apps added
        ArrayList<AppInfo> apps = new ArrayList<>();

        //list of packages
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        //for each package
        for(PackageInfo pack : packages) {
            //for each activity
            for (ResolveInfo activity : activityList) {
                //if the package name is in the activity list
                if (activity.activityInfo.applicationInfo.packageName.equals(pack.packageName)) {
                    //container for the app data
                    AppInfo newInfo = new AppInfo();

                    //set the app data info based on the package info
                    newInfo.setAppName(pack.applicationInfo.loadLabel(packageManager).toString());
                    newInfo.setPackageName(pack.packageName);
                    newInfo.setVersionName(pack.versionName);
                    newInfo.setVersionCode(pack.versionCode);
                    newInfo.setIcon(pack.applicationInfo.loadIcon(packageManager));
                    newInfo.setClassName(activity.activityInfo.name);

                    apps.add(newInfo);

                    break;
                }
            }
        }

        return apps;
    }

    private List<ResolveInfo> createActivityList() {
        //get the list of resolve info
        List<ResolveInfo> aList = packageManager.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA);
        //sort the resolve info list
        Collections.sort(aList, new ResolveInfo.DisplayNameComparator(packageManager));
        return aList;
    }

    public ArrayList<AppInfo> getApps() {
        return apps;
    }

    private void setBackgrounds(ArrayList<AppInfo> apps) {
        for (AppInfo app : apps) {
            app.setBackgroundColor(getDominantColor(app.getIcon()));
        }
    }

    private static int getDominantColor(Drawable drawable) {
        return Palette.from(((BitmapDrawable) drawable).getBitmap()).generate().getVibrantColor(0xFFFFFF);
    }
}