package com.floating.window;

import android.media.MediaPlayer;
import android.view.TextureView;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.content.Context;
import android.widget.ProgressBar;

import java.io.IOException;

public class VideoPlayerController implements MediaPlayer.OnPreparedListener
        ,TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private int mVideoPosition;
    private MediaPlayer mMediaPlayer;
    private String mVideoPathname;
    private TextureView mTextureView;
    private ProgressBar mProgressbar;

    public VideoPlayerController(TextureView textureView, String videoPathname) {
        mVideoPathname = videoPathname;
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        mMediaPlayer.setOnPreparedListener(this);
        try {
            mMediaPlayer.setDataSource(mVideoPathname);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startPlay(final int position) {

        new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO: Implement this method
                if (position != 0)
                    mMediaPlayer.seekTo(position);
                mMediaPlayer.start();
            }
        }).start();
    }


    public void pausePlay() {

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }

    }

    public void stopPlay() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void setProgressbar(ProgressBar progressbar){
        if(mProgressbar == null)
            mProgressbar = progressbar;
    }

    public int getVideoPosition() {
        return mVideoPosition;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        // TODO: Implement this method
        mMediaPlayer.setSurface(new Surface(texture));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        // TODO: Implement this method
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        // TODO: Implement this method
        stopPlay();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // TODO: Implement this method
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mVideoPosition = mMediaPlayer.getCurrentPosition();
            if(mProgressbar != null)
                mProgressbar.setProgress(mVideoPosition / mMediaPlayer.getDuration() * 100);
        }
    }


    @Override
    public void onPrepared(MediaPlayer player) {
        // TODO: Implement this method
        player.setLooping(true);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO: Implement this method
    }
}
