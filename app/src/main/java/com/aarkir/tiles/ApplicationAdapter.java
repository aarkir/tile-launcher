/*
An adapter between the listview and the AppInfo ArrayList
 */

package com.aarkir.tiles;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ApplicationAdapter extends ArrayAdapter<AppInfo> {
    //used for each app
    private ArrayList<AppInfo> items;
    private int maxImageSize;

    public ApplicationAdapter(Context context, int textViewResourceId, ArrayList<AppInfo> items){
        super(context, textViewResourceId, items);
        this.items = items;

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        maxImageSize = Math.min(metrics.widthPixels, metrics.heightPixels) / 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        double maxFrequency = getMaxFrequency(items);

        //if no view is to be recycled
        if(view == null) {
            LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layout.inflate(R.layout.applauncheritem, parent, false);
        }

        //get the app to be inflated
        AppInfo appInfo = items.get(position);

        //if the app is not null
        if(appInfo != null) {
            TextView frequency = (TextView) view.findViewById(R.id.frequency);
            ImageView appIcon = (ImageView) view.findViewById(R.id.icon);

            if(frequency != null){
                frequency.setText(String.valueOf(appInfo.getFrequency()));
            }

            //if the app has an icon
            if(appIcon != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
                int imageSize = (int) (maxImageSize*appInfo.getFrequency()/maxFrequency);
                if (imageSize < maxImageSize / 3) {
                    imageSize = maxImageSize / 3;
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                appIcon.setLayoutParams(layoutParams);
            }
        }

        return view;
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
}
