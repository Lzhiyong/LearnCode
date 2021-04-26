package com.text.edit;

import android.os.Handler;
import android.os.Looper;

public final class ExceptionHandler {
    
    // exception listener
    private static OnExceptionListener mExceptionListener;
    
    // uncaught exception handler
    private static Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;    
    
    private ExceptionHandler() {/* nothing to do*/}
    
    public static void setOnExceptionListener(OnExceptionListener listener) {
        mExceptionListener = listener;
    }
    
    public static synchronized void crashCapture() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        if (mExceptionListener != null) {
                            mExceptionListener.handleException(Looper.getMainLooper().getThread(), e);
                        }
                    }
                }
            }
        });

        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (mExceptionListener != null) {
                    mExceptionListener.handleException(t, e);
                }
            }
        });
    }
    
    interface OnExceptionListener {
        void handleException(Thread thread, Throwable throwable);
    }
}
