package com.text.edit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.File;
import org.mozilla.universalchardet.ReaderFactory;

public class MainActivity extends AppCompatActivity {

    private HighlightTextView mTextView;

    private TextBuffer mTextBuffer;

    private String externalPath = File.separator;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().hide();


        mTextView = findViewById(R.id.mTextView);
        mTextView.setTypeface(Typeface.MONOSPACE);

        mTextBuffer = new TextBuffer();

        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if(!hasPermission(permission)) {
            applyPermission(permission);
        }

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        
        mTextView.post(new Runnable(){

                @Override
                public void run() {
                    // TODO: Implement this method
                }
        });
        
        openFile(externalPath + "/Download/books/doupo.txt");
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
            requestPermissions(new String[] {permission}, 0);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO: Implement this method
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
        case R.id.action_undo:
            mTextView.undo();
            break;
        case R.id.action_redo:
            mTextView.redo();
            break;
        case R.id.action_copy:
            mTextView.copy();
            break;
        case R.id.action_cut:
            mTextView.cut();
            break;
        case R.id.action_paste:
            mTextView.paste();
            break;
        case R.id.action_select_all:
            mTextView.selectAll();
            break;
        case R.id.action_open:
            openFile(externalPath + "/Download/books/doupo.txt");
            break;
        case R.id.action_gotoline:
            showGotoLineDialog();
            break;
        case R.id.action_settings:
            break;
        case R.id.action_replaceFirst:
            mTextView.replaceFirst("haha");
            break;
        case R.id.action_replaceAll:
            mTextView.replaceAll("haha");
            break;
        case R.id.action_find_prev:
            mTextView.prev();
            break;
        case R.id.action_find_next:
            mTextView.next();
            break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showGotoLineDialog() {

        View v = getLayoutInflater().inflate(R.layout.dialog_gotoline, null);
        final EditText mLineEdit = v.findViewById(R.id.mLineEdit);
        mLineEdit.setHint("1.." + mTextBuffer.getLineCount());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setTitle("goto line");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String line = mLineEdit.getText().toString();
                if(line != null && !line.equals("")) {
                    mTextView.gotoLine(Integer.parseInt(line));
                }
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


    public void openFile(String pathname) {
        int lineCount = 0;
        int width = 0;  
        try {
            StringBuilder buf = mTextBuffer.getBuffer();
            BufferedReader br = ReaderFactory.createBufferedReader(new File(pathname));
            String text = null;
            while((text = br.readLine()) != null) {
                ++lineCount;
                buf.append(text + "\n");
                mTextBuffer.getIndexList().add(buf.length());

                width = (int)mTextView.getPaint().measureText(text);
                mTextBuffer.getWidthList().add(width);
            }

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
