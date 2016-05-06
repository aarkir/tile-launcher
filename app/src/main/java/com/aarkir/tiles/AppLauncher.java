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

import com.aarkir.tiles.model.AppInfo;
import com.aarkir.tiles.model.Applications;
import com.aarkir.tiles.widget.ApplicationAdapter;
import com.felipecsl.asymmetricgridview.library.Utils;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridViewAdapter;

public class AppLauncher extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    private ApplicationAdapter appAdapter;
    private ProgressDialog progressDialog;
    private ArrayList<AppInfo> apps;
    private SharedPreferences mSharedPreferences;
    private AsymmetricGridView listView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        //list of apps
        apps = new ArrayList<>();
        //adapter
        appAdapter = new ApplicationAdapter(this, R.layout.applauncheritem, apps);
         Runnable viewApps = new Runnable() {
            @Override
            public void run() {
                getApps();
                //initialize vars
                loadAppsAndFrequencies();
            }
        };
        Thread appLoaderThread = new Thread(null, viewApps, "AppLoaderThread");
        appLoaderThread.start();
        progressDialog = ProgressDialog.show(AppLauncher.this, "Hold on...", "Loading your apps...", true);

        //list for the grid view
        listView = (AsymmetricGridView) findViewById(R.id.listView);
        listView.setRequestedColumnCount(5);
        listView.setRequestedHorizontalSpacing(Utils.dpToPx(this, 0));
        listView.setAdapter(getNewAdapter());
        listView.setDebugging(true);
        listView.setOnItemClickListener(this);
    }

    private void getApps(){
        try{
            //generate app info for each app
            Applications myApps = new Applications(getPackageManager());
            //get app info
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

    @Override
    public void onItemClick(AdapterView parentView, View childView, int position, long id) {
        AppInfo rowClicked = (AppInfo) listView.getAdapter().getItem(position);

        //increase frequency of app clicked
        updateFrequency(rowClicked.getPackageName());
        appAdapter.notifyDataSetChanged();

        Intent startApp = new Intent();
        ComponentName component = new ComponentName(rowClicked.getPackageName(), rowClicked.getClassName());
        startApp.setComponent(component);
        startApp.setAction(Intent.ACTION_MAIN);

        startActivity(startApp);
    }

    @Override
    public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
        // this will provide the value
        AppInfo rowClicked = (AppInfo) listView.getAdapter().getItem(position);
        return false;
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

    private AsymmetricGridViewAdapter getNewAdapter() {
        return new AsymmetricGridViewAdapter(this, listView, appAdapter);
    }
}