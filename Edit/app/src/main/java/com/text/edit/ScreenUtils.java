package com.text.edit;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;

public final class ScreenUtils {

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;

        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            statusBarHeight = context.getResources().getDimensionPixelSize(Integer.parseInt(field.get(obj).toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statusBarHeight;
    }


    public static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();

        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                              context.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }


    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((AppCompatActivity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }


    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((AppCompatActivity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
