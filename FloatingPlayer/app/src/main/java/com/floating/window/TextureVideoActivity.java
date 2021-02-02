package com.floating.window;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


public class TextureVideoActivity extends AppCompatActivity {

    private String mVideoPathname;

    private TextureView mTextureView;

    private ImageView pipVideo;

    private ViewGroup.LayoutParams lparams;

    private SharedPreferences mSharedPreferences;

    private VideoPlayerController mController;

    private CardView mCardView;
    private LinearLayout mLinearLayout;

    private ProgressBar mProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        mSharedPreferences = getSharedPreferences("default", MODE_PRIVATE);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        mLinearLayout = findViewById(R.id.activity_texture_LinearLayout);
        mCardView = findViewById(R.id.cardView);
        lparams = mCardView.getLayoutParams();
        lparams.width = dm.widthPixels;
        lparams.height = (int) ((float) lparams.width / 16 * 9);
        mCardView.setLayoutParams(lparams);
        mCardView.setBackgroundColor(Color.BLACK);


        pipVideo = findViewById(R.id.video_pip);
        pipVideo.setVisibility(View.VISIBLE);
        pipVideo.setOnClickListener(mClickListener);

        mProgressbar = findViewById(R.id.video_progressbar);
        mTextureView = findViewById(R.id.textureView);
        mVideoPathname = mSharedPreferences.getString("pathname", "");
        int position = mSharedPreferences.getInt("position", 0);
        mController = new VideoPlayerController(mTextureView, mVideoPathname);
        mController.setProgressbar(mProgressbar);
        mController.startPlay(position);

        getSupportActionBar().hide();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO: Implement this method
        super.onNewIntent(intent);
        mLinearLayout.addView(mCardView);
        //mCardView.setVisibility(View.VISIBLE);
        mController = new VideoPlayerController(mTextureView, mVideoPathname);
        mController.startPlay(mSharedPreferences.getInt("position", 0));

    }


    View.OnClickListener mClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO: Implement this method

            mSharedPreferences.edit().putInt("position", mController.getVideoPosition()).commit();
            mController.stopPlay();
            mLinearLayout.removeView(mCardView);
            Intent intent = new Intent(FloatingVideoService.VIDEO_ACTION);
            intent.setPackage(getPackageName());
            startService(intent);
        }
    };

    @Override
    protected void onDestroy() {
        // TODO: Implement this method
        super.onDestroy();
        //mSharedPreferences.edit().putInt("position", 0).commit();
    }

}
