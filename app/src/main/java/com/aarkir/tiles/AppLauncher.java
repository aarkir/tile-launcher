package com.aarkir.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.aarkir.tiles.model.AppInfo;
import com.aarkir.tiles.model.Applications;
import com.aarkir.tiles.widget.ApplicationAdapter;
import com.felipecsl.asymmetricgridview.AsymmetricGridView;
import com.felipecsl.asymmetricgridview.AsymmetricGridViewAdapter;
import com.felipecsl.asymmetricgridview.Utils;


public class AppLauncher extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    private ApplicationAdapter appAdapter;
    private static ArrayList<AppInfo> apps;
    private static SharedPreferences mSharedPreferences;
    private AsymmetricGridView listView;
    private double maxFrequency;
    private static SharedPreferences.Editor mSharedPreferencesEditor;

    //Settings
    private static int columnDivisions; //number of divisions per column - used to theoretically create more random looks
    private static int columns; //number of full columns
    private static double largestSize; //largest size of an app on the screen, in an int from 0-100
    private static boolean backgrounds = false;
    private static boolean sizeSort = false;

    private AsymmetricGridViewAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        this.setContentView(R.layout.activity_main);

        getSettings();
        findViewById(R.id.listView).setBackgroundDrawable(WallpaperManager.getInstance(this).getDrawable());

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
        //progressDialog = ProgressDialog.show(AppLauncher.this, "Hold on...", "Loading your apps...", true);

        //list for the grid view
        listView = (AsymmetricGridView) findViewById(R.id.listView);
        listView.setRequestedColumnCount(columns * columnDivisions);
        listView.setRequestedHorizontalSpacing(Utils.dpToPx(this, 3));
        listView.setAllowReordering(true);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listViewAdapter = new AsymmetricGridViewAdapter(this, listView, appAdapter);
        listView.setAdapter(listViewAdapter);
    }

    private void getApps() {
        try{
            //generate app info for each app
            apps = new Applications(getPackageManager()).getApps();
            //get frequency data for each app
            loadFrequencies(apps);
            //get the max frequency out of any app
            maxFrequency = getMaxFrequencies(apps);
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
                //listViewAdapter.notifyDataSetChanged();

                for(AppInfo app : apps){
                    appAdapter.add(app);
                }
            }
            //progressDialog.dismiss();
        }
    };

    @Override
    public void onItemClick(AdapterView parentView, View childView, int position, long id) {
        AppInfo rowClicked = (AppInfo) listView.getAdapter().getItem(position);

        //increase frequency of app clicked
        updateFrequency(rowClicked.getPackageName());
        //appAdapter.notifyDataSetChanged();
        //listViewAdapter.notifyDataSetChanged();
        updateApps();

        Intent startApp = new Intent();
        ComponentName component = new ComponentName(rowClicked.getPackageName(), rowClicked.getClassName());
        startApp.setComponent(component);
        startApp.setAction(Intent.ACTION_MAIN);

        startActivity(startApp);
    }

    @Override
    public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
        // this will provide the value
        final AppInfo rowClicked = (AppInfo) listView.getAdapter().getItem(position);
        //Toast.makeText(this, rowClicked.getColumnSpan() + " " +rowClicked.getClassName(), Toast.LENGTH_SHORT).show();

        //show dialog with app menu
        View view = getLayoutInflater().inflate(R.layout.app_menu, (ViewGroup) findViewById(R.id.about_layout), false);
        view.findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall(rowClicked);
                //!! close dialog
                //!! refresh screen to remove missing app
                //appAdapter.notifyDataSetChanged();
                //listViewAdapter.notifyDataSetChanged();
                updateApps();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(rowClicked.getIcon());
        builder.setTitle(rowClicked.getAppName());
        builder.setView(view);
        builder.create();
        builder.show();
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateApps();
    }

    private void updateApps() {
        apps.clear();
        appAdapter = new ApplicationAdapter(this, R.layout.applauncheritem, apps);
        Runnable viewApps = new Runnable() {
            @Override
            public void run() {
                getApps();
            }
        };
        new Thread(null, viewApps, "AppLoaderThread").start();

        listViewAdapter = new AsymmetricGridViewAdapter(this, listView, appAdapter);
        listView.setAdapter(listViewAdapter);

        //List<AppInfo> fakeApps = new ArrayList<>();
        //appAdapter.appendItems(fakeApps);
    }

    private void updateFrequency(String packageName) {
        mSharedPreferencesEditor = mSharedPreferences.edit();

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

    private double getMaxFrequencies(ArrayList<AppInfo> items) {


        for (int i = 0; i < maxCount; i++) {
            maxFrequencies[i] = apps.get(n*i).getFrequency();
        }

        return maxFrequencies;
    }

    private void setSizesAndPositions(ArrayList<AppInfo> apps) {
        int maxCount = ((int) largestSize/100*columns*columnDivisions) - 1; //number of max elements
        double[] maxFrequencies = new double[maxCount]; //max frequencies of top apps
        int n = 3; //number of apps of a certain size

        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.getFrequency() - b.getFrequency();
            }
        });

        int size;
        for (int i = 0; i < apps.size(); i++) {
            size = (int) ;
            size = (int) Math.ceil((app.getFrequency() / maxFrequency) * largestSize / 100 * columnDivisions * columns);
            if (size < columnDivisions) { //less than 1 full column
                size = columnDivisions;
            }
            apps.get(i).setColumnSpan(size);
            apps.get(i).setRowSpan(size);
        }
    }

    private void sortApps(ArrayList<AppInfo> apps) {
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.getAppName().compareTo(b.getAppName());
            }
        });
        if (sizeSort) {
            Collections.sort(apps, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo a, AppInfo b) {
                    return b.getColumnSpan() - a.getColumnSpan() ;
                }
            });
        }
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

    private void getSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        columns = settings.getInt("columns", 6);
        columnDivisions = settings.getInt("columnDivisions", 1);
        largestSize = settings.getInt("largestSize", 50);
        backgrounds = settings.getBoolean("backgrounds", false);
        sizeSort = settings.getBoolean("sizeSort", false);
    }

    public static boolean getBackgrounds() {
        return backgrounds;
    }

    public static void setBackgrounds(boolean backgrounds) {
        AppLauncher.backgrounds = backgrounds;
    }

    public static void setColumnCount(int columnDivisions) {
        AppLauncher.columnDivisions = columnDivisions;
    }

    public static void setMaximumApps(int columns) {
        AppLauncher.columns = columns;
    }

    public static void setLargestSize(int largestSize) {
        AppLauncher.largestSize = largestSize;
    }

    public static void setSizeSort(boolean sizeSort) {
        AppLauncher.sizeSort = sizeSort;
    }

    private void uninstall(AppInfo rowClicked) {
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+rowClicked.getPackageName()));
        startActivity(intent);
    }

    public static void resetFrequencies() {
        //set shared preferences
        mSharedPreferencesEditor = mSharedPreferences.edit();

        /*
        //for each item in shared preferences
        for(Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
            for (AppInfo app : apps) {
                if (app.getPackageName().contains(".")) {
                    app.setFrequency(0);
                    break;
                }
            }
        }*/

        mSharedPreferencesEditor.clear();

        mSharedPreferencesEditor.apply();
    }

    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }
    }
}