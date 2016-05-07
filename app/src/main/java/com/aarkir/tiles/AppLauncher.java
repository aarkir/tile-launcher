package com.aarkir.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.aarkir.tiles.model.AppInfo;
import com.aarkir.tiles.model.Applications;
import com.aarkir.tiles.widget.ApplicationAdapter;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridViewAdapter;

public class AppLauncher extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    private ApplicationAdapter appAdapter;
    private ProgressDialog progressDialog;
    private ArrayList<AppInfo> apps;
    private SharedPreferences mSharedPreferences;
    private AsymmetricGridView listView;
    private double maxFrequency;
    private int columnCount = 60;
    private int maximumApps = 5;
    private AsymmetricGridViewAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
            }
        };
        Thread appLoaderThread = new Thread(null, viewApps, "AppLoaderThread");
        appLoaderThread.start();
        progressDialog = ProgressDialog.show(AppLauncher.this, "Hold on...", "Loading your apps...", true);

        //list for the grid view
        listView = (AsymmetricGridView) findViewById(R.id.listView);
        listView.setRequestedColumnCount(columnCount);
        //listView.setRequestedHorizontalSpacing(Utils.dpToPx(this, 3));
        //listView.setAllowReordering(true);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listViewAdapter = new AsymmetricGridViewAdapter(this, listView, appAdapter);
        listView.setAdapter(listViewAdapter);
    }

    private void getApps(){
        try{
            //generate app info for each app
            apps = new Applications(getPackageManager()).getApps();
            //get frequency data for each app
            loadFrequencies(apps);
            //get the max frequency out of any app
            maxFrequency = getMaxFrequency(apps);
            //set app positions and sizes
            setSizesAndPositions(apps);
            //sort the apps by alphabet and size
            sortApps(apps);
        }
        catch(Exception exception) {
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
        listViewAdapter.notifyDataSetChanged();

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
        Toast.makeText(this, position + " " +rowClicked.getAppName(), Toast.LENGTH_SHORT).show();
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
    private void loadFrequencies(ArrayList<AppInfo> apps) {
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
    }

    private double getMaxFrequency(ArrayList<AppInfo> items) {
        double maxFrequency = 0;
        for (AppInfo app : items) {
            if (app.getFrequency() > maxFrequency) {
                maxFrequency = app.getFrequency();
            }
        }
        return maxFrequency;
    }

    private void setSizesAndPositions(ArrayList<AppInfo> apps) {
        int size;
        for (AppInfo app : apps) {
            size = (int) Math.ceil((app.getFrequency() / maxFrequency) * 0.5 * columnCount);
            if (size < columnCount / maximumApps) {
                size = columnCount / maximumApps;
            }
            app.setColumnSpan(size);
            app.setRowSpan(size);
        }
    }

    private void sortApps(ArrayList<AppInfo> apps) {
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.getAppName().compareTo(b.getAppName());
            }
        });
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return b.getColumnSpan() - a.getColumnSpan() ;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings :
                startActivity(new Intent(this, Settings.class));
                break;
        }
        return true;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public void setMaximumApps(int maximumApps) {
        this.maximumApps = maximumApps;
    }
}