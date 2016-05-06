package com.aarkir.tiles.widget;

import android.widget.ListAdapter;

import com.aarkir.tiles.model.AppInfo;

import java.util.List;

public interface DemoAdapter extends ListAdapter {

  void appendItems(List<AppInfo> newItems);

  void setItems(List<AppInfo> moreItems);
}
