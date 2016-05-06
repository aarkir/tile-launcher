/*
Holds data about the apps currently installed on the device
 */

package com.aarkir.tiles.model;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.aarkir.tiles.model.AppInfo;

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
        activityList = this.createActivityList(); //used to find intent data to launch application
        packageList = this.createPackageList();
    }

    public ArrayList getPackageList(){
        return packageList;
    }

    private ArrayList<AppInfo> createPackageList() {
        //list to be returned with the apps added
        ArrayList<AppInfo> pList = new ArrayList<>();

        //list of packages
        List<PackageInfo> packs = packMan.getInstalledPackages(0);

        //for each package
        for(PackageInfo pack : packs) {
            //for each activity
            for (ResolveInfo activity : activityList) {
                //if the package name is in the activity list
                if (activity.activityInfo.applicationInfo.packageName.equals(pack.packageName)) {
                    //container for the app data
                    AppInfo newInfo = new AppInfo();

                    //set the app data info based on the package info
                    newInfo.setAppName(pack.applicationInfo.loadLabel(packMan).toString());
                    newInfo.setPackageName(pack.packageName);
                    newInfo.setVersionName(pack.versionName);
                    newInfo.setVersionCode(pack.versionCode);
                    newInfo.setIcon(pack.applicationInfo.loadIcon(packMan));
                    newInfo.setClassName(activity.activityInfo.name);

                    pList.add(newInfo);

                    break;
                }
            }
        }

        return pList;
    }

    private List<ResolveInfo> createActivityList() {
        //get the list of resolve info
        List<ResolveInfo> aList = packMan.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA);
        //sort the resolve info list
        Collections.sort(aList, new ResolveInfo.DisplayNameComparator(packMan));

        return aList;
    }
}