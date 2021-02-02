package com.floating.window;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class FloatingVideoService extends Service {

	private FloatingVideoView mFloatingView;
    public static final String VIDEO_ACTION = "intent.action.FLOATING_VIDEO_SERVICE";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO: Implement this method
		return super.onStartCommand(intent, flags, startId);
	}
	
	
    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     *
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    
    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingView = new FloatingVideoView(this, R.layout.activity_texture);
        mFloatingView.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFloatingView != null)
            mFloatingView.dismiss();
        stopSelf();
    }
}
