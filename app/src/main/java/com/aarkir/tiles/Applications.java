/*
Holds data about the apps currently installed on the device
 */

package com.aarkir.tiles;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Applications {
    private ArrayList<AppInfo> packageList;
    private List<ResolveInfo> activityList;
    private Intent mainIntent;
    private PackageManager packMan;

    public Applications(PackageManager packManager){
        mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        packMan = packManager;
        packageList = this.createPackageList();
        activityList = this.createActivityList();
        this.addClassNamesToPackageList();
    }

    public ArrayList getPackageList(){
        return packageList;
    }

    /*
    public List getActivityList(){
        return activityList;
    }
    */

    private ArrayList<AppInfo> createPackageList(){
        //list to be returned with the apps added
        ArrayList<AppInfo> pList = new ArrayList<>();

        //for each package
        List<PackageInfo> packs = packMan.getInstalledPackages(0);

        for(PackageInfo pack : packs) {
            //Try to get the application info for the app
            /*
            try {
                ApplicationInfo ai = packMan.getApplicationInfo(pack.packageName, 0);
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    continue;
                }
            }
            catch (Exception e) {
                Log.e("BACKGROUND PROC:", e.getMessage());
            }
            */

            //container for the app data
            AppInfo newInfo = new AppInfo();

            //set the app data info based on the package info
            newInfo.setAppName(pack.applicationInfo.loadLabel(packMan).toString());
            newInfo.setPackageName(pack.packageName);
            newInfo.setVersionName(pack.versionName);
            newInfo.setVersionCode(pack.versionCode);
            newInfo.setIcon(pack.applicationInfo.loadIcon(packMan));

            pList.add(newInfo);
        }
        return pList;
    }

    private List<ResolveInfo> createActivityList() {
        List<ResolveInfo> aList = packMan.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA);

        Collections.sort(aList, new ResolveInfo.DisplayNameComparator(packMan));

        return aList;
    }

    /*
    private void packageDebug(){
        if(null == packageList){
            return;
        }

        for(AppInfo app : packageList){
            Log.v("PACKINFO: ", "\t" +
                    app.getAppName() + "\t" +
                    app.getPackageName() + "\t" +
                    app.getClassName() + "\t" +
                    app.getVersionName() + "\t" +
                    app.getVersionCode());
        }
    }
    */

    /*
    private void activityDebug(){
        if(null == activityList){
            return;
        }

        for(ResolveInfo activity : activityList){
            ActivityInfo currentActivity = activity.activityInfo;
            Log.v("ACTINFO",
                    "pName="
                            + currentActivity.applicationInfo.packageName +
                            " cName=" + currentActivity.name);
        }
    }
    */

    private void addClassNamesToPackageList() {
        if(null == activityList || null == packageList){
            return;
        }

        for(AppInfo app : packageList) {
            for(ResolveInfo activity : activityList) {
                if(app.getPackageName().equals(activity.activityInfo.applicationInfo.packageName)) {
                    app.setClassName(activity.activityInfo.name);
                }
            }
        }
    }
}