package com.text.edit;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import android.os.Environment;
import android.util.Log;
import com.text.edit.R;
import java.util.ArrayList;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.graphics.Rect;
import java.lang.reflect.Field;
import android.text.SpannedString;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity {

    private HighlightTextView mTextView;

    private TextBuffer mTextBuffer;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();


        mTextView = findViewById(R.id.mTextView);
        mTextView.setTypeface(Typeface.MONOSPACE);

        TextScrollView scrollView = findViewById(R.id.mScrollView);
        TextHorizontalScrollView horizontalScrollView = findViewById(R.id.mHorizontalScrollView);
        
        mTextView.setScrollView(scrollView, horizontalScrollView);
        
        mTextBuffer = new TextBuffer(mTextView.getPaint());
        
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if(!hasPermission(permission)) {
            applyPermission(permission);
        }

        String pathname = File.separator;

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            pathname = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        openFile(pathname + File.separator + "Download/books/doupo.txt");

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO: Implement this method
        super.onWindowFocusChanged(hasFocus);
    }

    public boolean hasPermission(String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        else
            return true;
    }

    public void applyPermission(String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, "request read sdcard permmission", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[] {permission},0);
        }
    }

    public void openFile(String pathname) {
        FileInputStream fin = null;
        InputStreamReader ins = null;
        BufferedReader br = null;

        StringBuilder buf = mTextBuffer.getBuffer();
        String text = null;
        int width = 0;
        int lineCount = 0;
        
        try {
            fin = new FileInputStream(new File(pathname));
            ins = new InputStreamReader(fin, "utf-8");
            br = new BufferedReader(ins);
            
            while((text = br.readLine()) != null) {
                ++lineCount;
                buf.append(text + "\n");
                mTextBuffer.getIndexList().add(buf.length());
                
                width = (int)mTextView.getPaint().measureText(text);
                mTextBuffer.getWidthList().add(width);
            }

            fin.close();
            ins.close();
            br.close();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // remove the last index of '\n'
        mTextBuffer.getIndexList().remove(lineCount);
        mTextBuffer.setLineCount(lineCount);
        
        mTextView.setTextBuffer(mTextBuffer);
        mTextView.setCursorPosition(0, 0);
    }
}
