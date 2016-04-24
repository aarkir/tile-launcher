package com.aarkir.tiles;

import java.util.ArrayList;
import java.util.Map;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class AppLauncher extends ListActivity {
    private ApplicationAdapter appAdapter;
    private ProgressDialog progressDialog;
    private ArrayList<AppInfo> packageList;
    private Applications myApps;
    private SharedPreferences mSharedPreferences;
    private Map<String, Integer> entrySet;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.applauncher);

        //initialize vars
        packageList = new ArrayList<>();
        loadAppsAndFrequencies();
        setOnLongClickListener();

        //adapter
        appAdapter = new ApplicationAdapter(this, R.layout.applauncherrow, packageList);
        this.setListAdapter(appAdapter);

         Runnable viewApps = new Runnable() {
            @Override
            public void run() {
                getApps();
            }
        };

        Thread appLoaderThread = new Thread(null, viewApps, "AppLoaderThread");
        appLoaderThread.start();

        progressDialog = ProgressDialog.show(AppLauncher.this, "Hold on...", "Loading your apps...", true);
    }

    private void getApps(){
        try{
            myApps = new Applications(getPackageManager());
            packageList = myApps.getPackageList();
        }
        catch(Exception exception){
            Log.e("BACKGROUND PROC:", exception.getMessage());
        }
        this.runOnUiThread(returnRes);
    }

    private Runnable returnRes = new Runnable(){
        public void run(){
            if(packageList != null && packageList.size() > 0){
                appAdapter.notifyDataSetChanged();

                for(AppInfo app : packageList){
                    appAdapter.add(app);
                }
            }
            progressDialog.dismiss();
            appAdapter.notifyDataSetChanged();
        }
    };

    //add increase usage count
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        AppInfo rowClicked = (AppInfo) this.getListAdapter().getItem(position);

        //increase frequency of app clicked
        //updateFrequency(rowClicked.getPackageName());

        Intent startApp = new Intent();
        ComponentName component = new ComponentName(rowClicked.getPackageName(), rowClicked.getClassName());
        startApp.setComponent(component);
        startApp.setAction(Intent.ACTION_MAIN);
        Toast.makeText(this, rowClicked.getClassName(), Toast.LENGTH_SHORT).show();
        //startActivity(startApp);
    }

    private void setOnLongClickListener() {
        this.getListView().setLongClickable(true);
        this.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
                // this will provide the value
                AppInfo rowClicked = (AppInfo) getListAdapter().getItem(position);
                Toast.makeText(getApplication(), rowClicked.getAppName(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    private void updateFrequency(String packageName) {
        //get the editor
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        //increase the value of the package name by 1
        mSharedPreferencesEditor.putInt(packageName, entrySet.get(packageName)+1);
        //apply the change
        mSharedPreferencesEditor.apply();
    }

    private void loadAppsAndFrequencies() {
        //set shared preferences
        mSharedPreferences = getPreferences(MODE_PRIVATE);

        //get apps
        for(Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
            entrySet.put(entry.getKey(), Integer.getInteger(entry.getValue().toString()));
        }

        //add new apps
        for (AppInfo app : packageList) {

        }
    }
}