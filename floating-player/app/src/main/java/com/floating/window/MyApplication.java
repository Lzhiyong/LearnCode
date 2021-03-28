package com.floating.window;

import android.app.Application;
import android.os.Handler;

public class MyApplication extends Application {

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
}
