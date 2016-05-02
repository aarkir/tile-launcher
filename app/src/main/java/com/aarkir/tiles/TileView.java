package com.aarkir.tiles;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

public class TileView extends AdapterView {

    /**
     * The adapter with all the data
     */
    private Adapter mAdapter;

    /**
     * Constructor
     *
     * @param context The context
     * @param attrs   Attributes
     */
    public TileView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(final Adapter adapter) {
        mAdapter = adapter;
        requestLayout();
        removeAllViewsInLayout();
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setSelection(final int position) {
        throw new UnsupportedOperationException("setSelection supported");
    }

    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException("getSelectedView supported");
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // if we don't have an adapter, we don't need to do anything
        if (mAdapter == null) {
            return;
        }
        if (getChildCount() == 0) {
            for (int position = 0; position < mAdapter.getCount(); position++) {
                View newBottomChild = mAdapter.getView(position, null, this);
                addAndMeasureChild(newBottomChild);
                position++;
            }
        }
    }

    private void addAndMeasureChild(View child) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        addViewInLayout(child, -1, params, true);

        int itemWidth = getWidth();
        child.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.UNSPECIFIED);
    }
}
