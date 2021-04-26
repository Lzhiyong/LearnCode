package com.text.edit;

import android.app.Application;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

public class MyApplication extends Application {
    
    private final String TAG = this.getClass().getSimpleName();
    
    private ExceptionHandler.OnExceptionListener listener = new ExceptionHandler.OnExceptionListener() {
        @Override
        public void handleException(Thread thread, Throwable throwable) {
            // TODO: Implement this method
            showErrorDialog(throwable.getMessage());
            Log.e(TAG, throwable.getMessage());
        }
    };
    
    @Override
    public void onCreate() {
        // TODO: Implement this method
        super.onCreate();
        ExceptionHandler.setOnExceptionListener(listener);
        ExceptionHandler.crashCapture();
    }

    public void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Exception");
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        builder.setCancelable(true).show();
    }
}
