package com.text.edit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class TextScrollView extends ScrollView {


    private OnScrollListener  mScrollListener;

    public TextScrollView(Context context) {
        super(context);
    }

    public TextScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setScrollListener(OnScrollListener listener) {
        mScrollListener = listener;
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO: Implement this method
        super.onScrollChanged(l, t, oldl, oldt);
        if(mScrollListener != null) {
            mScrollListener.onScrollY(t, oldt);
        }
    }
}

