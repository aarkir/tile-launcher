/*
An adapter between the listview and the AppInfo ArrayList
 */

package com.aarkir.tiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ApplicationAdapter extends ArrayAdapter<AppInfo> {
    private ArrayList<AppInfo> items;

    public ApplicationAdapter(Context context, int textViewResourceId, ArrayList<AppInfo> items){
        super(context, textViewResourceId, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null) {
            LayoutInflater layout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layout.inflate(R.layout.applauncherrow, parent, false);
        }

        AppInfo appInfo = items.get(position);
        if(appInfo != null) {
            //TextView appName = (TextView) view.findViewById(R.id.applauncherrow_appname);
            ImageView appIcon = (ImageView) view.findViewById(R.id.applauncherrow_icon);

            //if(appName != null){
            //    appName.setText(appInfo.getAppName());
            //}
            if(appIcon != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
            }
        }

        return view;
    }
}
