package io.passworks.lighthousetestbed;

import android.app.Application;

import io.passworks.lighthouse.utils.LHLog;

/**
 * Created by ivanbruel on 02/09/15.
 */
public class BeaconsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LHLog.setVerbose(true);
    }
}
