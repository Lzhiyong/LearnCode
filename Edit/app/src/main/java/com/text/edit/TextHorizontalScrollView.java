package com.text.edit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class TextHorizontalScrollView extends HorizontalScrollView {
    private OnScrollListener  mScrollListener;

    public TextHorizontalScrollView(Context context) {
        super(context);
    }

    public TextHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setScrollListener(OnScrollListener listener){
        mScrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO: Implement this method
        super.onScrollChanged(l, t, oldl, oldt);
        if(mScrollListener != null){
            mScrollListener.onScrollX(l, oldl);
        }
    }
}

