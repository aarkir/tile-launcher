/*
An adapter between the listview and the AppInfo ArrayList
 */

package com.aarkir.tiles.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.aarkir.tiles.R;
import com.aarkir.tiles.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAdapter extends ArrayAdapter<AppInfo> implements DemoAdapter {
    //used for each app
    private ArrayList<AppInfo> items;
    private final LayoutInflater layoutInflater;

    public ApplicationAdapter(Context context, int textViewResourceId, ArrayList<AppInfo> items){
        super(context, textViewResourceId, items);
        this.items = items;
        layoutInflater = LayoutInflater.from(context);

        //DisplayMetrics metrics = new DisplayMetrics();
        //((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //maxImageSize = Math.min(metrics.widthPixels, metrics.heightPixels) / 2;
    }

    public ApplicationAdapter(Context context, List<AppInfo> items) {
        super(context, 0, items);
        layoutInflater = LayoutInflater.from(context);
    }

    public ApplicationAdapter(Context context) {
        super(context, 0);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        //if no view is to be recycled
        if(view == null) {
            view = layoutInflater.inflate(R.layout.applauncheritem, parent, false);
        }

        //get the app to be inflated
        AppInfo appInfo = items.get(position);

        //if the app is not null
        if(appInfo != null) {
            ImageView appIcon = (ImageView) view.findViewById(R.id.icon);

            //if the app has an icon
            if(appIcon != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
                /**
                int imageSize = (int) (maxImageSize*appInfo.getFrequency()/maxFrequency);
                if (imageSize < maxImageSize / 3) {
                    imageSize = maxImageSize / 3;
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                layoutParams.leftMargin = imageSize;
                layoutParams.topMargin = imageSize;
                appIcon.setLayoutParams(layoutParams);
                 **/
            }
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? 1 : 0;
    }

    @Override
    public void appendItems(List<AppInfo> newItems) {
        addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public void setItems(List<AppInfo> moreItems) {
        clear();
        appendItems(moreItems);
    }
}
