package com.text.edit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
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

        mTextView.setText("Hello");
        
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if(!hasPermission(permission)) {
            applyPermission(permission);
        }

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        
        //new ReadFileThread().execute(new File(externalPath + "/Download/books/doupo.txt"));
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
            //openFile(externalPath + "/Download/books/doupo.txt");
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
    
    
    class ReadFileThread extends AsyncTask<File, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            // TODO: Implement this method
            super.onPreExecute();
        }
        
        
        @Override
        protected Boolean doInBackground(File... files) {
            // TODO: Implement this method
            int lineCount = (int) FileUtils.getLineNumber(files[0]);
            //mTextBuffer.tempLineCount = lineCount;
            
            try {
                StringBuilder buf = mTextBuffer.getBuffer();
                BufferedReader br = ReaderFactory.createBufferedReader(files[0]);
                String text = null;

                // add first index 0
                mTextBuffer.getIndexList().add(0);
                while((text = br.readLine()) != null) {
                    buf.append(text + "\n");
                    mTextBuffer.getIndexList().add(buf.length());

                    int width = mTextView.getTextMeasureWidth(text);
                    mTextBuffer.getWidthList().add(width);
                }

                br.close();
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // remove the last index of '\n'
            int size = mTextBuffer.getIndexList().size();
            mTextBuffer.getIndexList().remove(size - 1);
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            // TODO: Implement this method
            super.onProgressUpdate(values);
            
        }
        
        
        @Override
        protected void onPostExecute(Boolean result) {
            // TODO: Implement this method
            super.onPostExecute(result);
            //mTextBuffer.isWriteBufferFinish = result;
        }
    }
}
