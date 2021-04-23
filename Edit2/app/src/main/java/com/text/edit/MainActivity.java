package com.text.edit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.mozilla.universalchardet.UniversalDetector;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private HighlightTextView mTextView;
    private TextBuffer mTextBuffer;

    private AlertDialog mProgressDialog;
    private ProgressBar mIndeterminateBar;

    private SharedPreferences mSharedPreference;
    private Charset mDefaultCharset = StandardCharsets.UTF_8;
    private String externalPath = File.separator;

    private final int DISABLE_PROGRESD_DIALOG = 0;
    private final int REFRESH_OPTION_MENU = 1;
    
    private final String TAG = this.getClass().getSimpleName();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO: Implement this method
            super.handleMessage(msg);
            switch(msg.what) {
            case DISABLE_PROGRESD_DIALOG:
                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                mIndeterminateBar.setVisibility(View.VISIBLE);
                break;
            case REFRESH_OPTION_MENU:
                invalidateOptionsMenu();
            }
        }
    };

    private OnTextChangedListener textListener = new OnTextChangedListener() {
        @Override
        public void onTextChanged() {
            // TODO: Implement this method
            mHandler.sendEmptyMessage(REFRESH_OPTION_MENU);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().hide();

        mIndeterminateBar = findViewById(R.id.indeterminateBar);
        mIndeterminateBar.setBackground(null);

        mTextView = findViewById(R.id.mTextView);
        mTextView.setTypeface(Typeface.MONOSPACE);
        mTextView.setOnTextChangedListener(textListener);
        
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);

        mTextView.setText("Hello");

        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if(!hasPermission(permission)) {
            applyPermission(permission);
        }

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
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

    private void toggleEditMode() {
        mTextView.setEditedMode(!mTextView.getEditedMode());
        mHandler.sendEmptyMessage(REFRESH_OPTION_MENU);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO: Implement this method
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: Implement this method
        MenuItem itemUndo = menu.findItem(R.id.menu_undo);
        itemUndo.setIcon(R.drawable.ic_undo_white_24dp);
        if(mTextView.getUndoStack().canUndo())
            itemUndo.setEnabled(true);
        else
            itemUndo.setEnabled(false);
            
        MenuItem itemRedo = menu.findItem(R.id.menu_redo);
        itemRedo.setIcon(R.drawable.ic_redo_white_24dp);
        if(mTextView.getUndoStack().canRedo())
            itemRedo.setEnabled(true);
        else
            itemRedo.setEnabled(false);
            
        MenuItem itemEdit = menu.findItem(R.id.menu_edit);
        if(mTextView.getEditedMode())
            itemEdit.setIcon(R.drawable.ic_edit_white_24dp);     
        else
            itemEdit.setIcon(R.drawable.ic_look_white_24dp);     
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_undo:
            mTextView.undo();
            break;
        case R.id.menu_redo:
            mTextView.redo();
            break;
        case R.id.menu_edit:
            toggleEditMode();
            break;
        case R.id.menu_open:
            showOperateFileDialog("open file", true);
            break;
        case R.id.menu_gotoline:
            showGotoLineDialog();
            break;
        case R.id.menu_settings:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGotoLineDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_gotoline, null);
        final EditText lineEdit = v.findViewById(R.id.lineEdit);
        lineEdit.setHint("1.." + mTextBuffer.getLineCount());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setTitle("goto line");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String line = lineEdit.getText().toString();
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

    private void showProgressBarDialig() {
        View v = getLayoutInflater().inflate(R.layout.dialog_progressbar, null);
        TextView textMessage = v.findViewById(R.id.textMessage);
        SpannableStringBuilder span = new SpannableStringBuilder(textMessage.getText());
        buildTextSpans(span, textMessage);
        textMessage.setText(span);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);

        mProgressDialog = builder.setCancelable(false).create();
        mProgressDialog.show();
    }

    // open and save the file
    private void showOperateFileDialog(String title, final boolean isRead) {
        View v = getLayoutInflater().inflate(R.layout.dialog_openfile, null);
        final EditText pathEdit = v.findViewById(R.id.pathEdit);
        if(isRead) {
            String path = mSharedPreference.getString("opened_filepath", null);
            if(path != null && !path.isEmpty()) pathEdit.setText(path);
        }
        pathEdit.setHint("please enter the file path");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setTitle(title);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pathname = pathEdit.getText().toString();
                    if(pathname != null && !pathname.isEmpty()) {
                        // add an opened file
                        FileUtils.addOpenedFile(pathname);
                        if(isRead) {
                            mSharedPreference.edit().putString("opened_filepath", pathname).commit();
                            new ReadFileThread().execute(pathname);
                        } else {
                            new WriteFileThread().execute(pathname);
                        }
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

    private TextAnimate[] buildTextSpans(SpannableStringBuilder span, TextView textview) {
        TextAnimate[] textSpans;
        int duration = 1200;
        // start position
        int start = span.toString().indexOf(".");
        // end position
        int end = textview.getText().length();
        // each char delay
        int charDelay = duration / (3 * (end - start));

        textSpans = new TextAnimate[end - start];
        for(int pos = start; pos < end; pos++) {
            TextAnimate animates =
                new TextAnimate(textview, duration, pos - start, charDelay, 0.35f);
            span.setSpan(animates, pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textSpans[pos - start] = animates;
        }
        return textSpans;
    }

    // read file
    class ReadFileThread extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            // TODO: Implement this method
            super.onPreExecute();
            mTextView.setEditedMode(false);
            showProgressBarDialig();
            // create text buffer
            mTextBuffer = new TextBuffer();
            mTextView.setTextBuffer(mTextBuffer);
        }

        @Override
        protected Boolean doInBackground(String...params) {
            // TODO: Implement this method
            Path path = Paths.get(params[0]);
            if(!FileUtils.checkOpenFileState(path) 
               && !FileUtils.checkSameFile(path)) 
                return false;

            mTextBuffer.tempLineCount = FileUtils.getLineNumber(path.toFile());

            try {
                // detect the file encoding
                String charset = UniversalDetector.detectCharset(path.toFile());
                if(charset != null) mDefaultCharset = Charset.forName(charset);

                // create buffered reader
                BufferedReader bufferRead = null;
                bufferRead = Files.newBufferedReader(path, mDefaultCharset);

                StringBuilder buf = mTextBuffer.getBuffer();
                String text = null;
                // read file
                while((text = bufferRead.readLine()) != null) {
                    buf.append(text + "\n");

                    if(mTextBuffer.getIndexList().size() == 0) {
                        // add first index 0
                        mTextBuffer.getIndexList().add(0);
                        mHandler.sendEmptyMessage(DISABLE_PROGRESD_DIALOG);
                    }
                    mTextBuffer.getIndexList().add(buf.length());

                    // text line width
                    int width = mTextView.getTextMeasureWidth(text);
                    if(width > mTextBuffer.tempLineWidth)
                        mTextBuffer.tempLineWidth = width;
                    mTextBuffer.getWidthList().add(width);
                }
                // close stream
                bufferRead.close();
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
            // remove the last index of '\n'
            int size = mTextBuffer.getIndexList().size();
            mTextBuffer.getIndexList().remove(size - 1);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO: Implement this method
            super.onPostExecute(result);
            mTextBuffer.onReadFinish = true;
            mTextView.setEditedMode(true);
            mIndeterminateBar.setVisibility(View.GONE);
        }
    }

    // write file
    class WriteFileThread extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String...params) {
            // TODO: Implement this method
            Path path = Paths.get(params[0]);
            if(!FileUtils.checkSaveFileState(path)) 
                return false;
                
            try {
                BufferedWriter bufferWrite = null;
                bufferWrite = Files.newBufferedWriter(path, mDefaultCharset, 
                                                      StandardOpenOption.WRITE);
                bufferWrite.write(mTextBuffer.getBuffer().toString());     
                bufferWrite.flush();
                bufferWrite.close();
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO: Implement this method
            super.onPostExecute(result);
            mTextBuffer.onWriteFinish = true;
            Toast.makeText(getApplicationContext(), "saved success!", Toast.LENGTH_SHORT).show();
        }
    }
}
