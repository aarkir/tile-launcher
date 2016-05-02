package com.aarkir.tiles;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

public class AppLauncher extends Activity {
    private ApplicationAdapter appAdapter;
    private ProgressDialog progressDialog;
    private ArrayList<AppInfo> apps;
    private SharedPreferences mSharedPreferences;
    //private AdapterView mainView;
    private RelativeLayout mainView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.applauncher);

        apps = new ArrayList<>();

        //mainView = (AdapterView) findViewById(R.id.applauncher_layout);

        //adapter
        //appAdapter = new ApplicationAdapter(this, R.layout.applauncheritem, apps);
        //mainView.setAdapter(appAdapter);

         Runnable viewApps = new Runnable() {
            @Override
            public void run() {
                getApps();
                //initialize vars
                loadAppsAndFrequencies();
                setOnItemClickListener();
                setOnLongClickListener();
            }
        };

        Thread appLoaderThread = new Thread(null, viewApps, "AppLoaderThread");
        appLoaderThread.start();

        progressDialog = ProgressDialog.show(AppLauncher.this, "Hold on...", "Loading your apps...", true);
    }

    private void getApps(){
        try{
            Applications myApps = new Applications(getPackageManager());
            apps = myApps.getPackageList();
        }
        catch(Exception exception){
            Log.e("BACKGROUND PROC:", exception.getMessage());
        }
        this.runOnUiThread(returnRes);
    }

    private Runnable returnRes = new Runnable(){
        public void run(){
            if(apps != null && apps.size() > 0){
                appAdapter.notifyDataSetChanged();

                for(AppInfo app : apps){
                    appAdapter.add(app);
                }
            }
            progressDialog.dismiss();
            appAdapter.notifyDataSetChanged();
        }
    };

    //add increase usage count
    private void setOnItemClickListener() {
        mainView.setClickable(true);
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentView, View childView, int position, long id) {
                AppInfo rowClicked = (AppInfo) mainView.getAdapter().getItem(position);

                //increase frequency of app clicked
                updateFrequency(rowClicked.getPackageName());
                appAdapter.notifyDataSetChanged();

                Intent startApp = new Intent();
                ComponentName component = new ComponentName(rowClicked.getPackageName(), rowClicked.getClassName());
                startApp.setComponent(component);
                startApp.setAction(Intent.ACTION_MAIN);

                startActivity(startApp);
            }
        });
    }

    /**
    //add increase usage count
    private void setOnItemClickListener() {
        mainView.setClickable(true);
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentView, View childView, int position, long id) {
                AppInfo rowClicked = (AppInfo) mainView.getAdapter().getItem(position);

                //increase frequency of app clicked
                updateFrequency(rowClicked.getPackageName());
                appAdapter.notifyDataSetChanged();

                Intent startApp = new Intent();
                ComponentName component = new ComponentName(rowClicked.getPackageName(), rowClicked.getClassName());
                startApp.setComponent(component);
                startApp.setAction(Intent.ACTION_MAIN);

                startActivity(startApp);
            }
        });
    }
     **/

    private void setOnLongClickListener() {
        mainView.setLongClickable(true);
        mainView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
                // this will provide the value
                AppInfo rowClicked = (AppInfo) mainView.getAdapter().getItem(position);
                return false;
            }
        });
    }

    private void updateFrequency(String packageName) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();

        //increase the value of the package name by 1
        for (AppInfo app : apps) {
            if (app.getPackageName().equals(packageName)) {
                app.setFrequency(app.getFrequency() + 1);
                mSharedPreferencesEditor.putInt(app.getPackageName(), app.getFrequency());
                break;
            }
        }
        //apply the change
        mSharedPreferencesEditor.apply();
    }

    //load initial frequency data from the shared preferences
    private void loadAppsAndFrequencies() {
        //set shared preferences
        mSharedPreferences = getPreferences(MODE_PRIVATE);

        //for each item in shared preferences
        for(Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
            for (AppInfo app : apps) {
                if (app.getPackageName().equals(entry.getKey())) {
                    app.setFrequency(Integer.parseInt(entry.getValue().toString()));
                    break;
                }
            }
        }

        //sort list by usage, then alphabetically
    }
}