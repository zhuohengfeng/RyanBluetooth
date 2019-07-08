package com.rokid.bluetooth;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class BTApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 内存泄露检测
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
        }

        RokidBluetoothManager.getInstance().init(this, RokidBluetoothManager.BlueSocketMode.SERVER);
    }

}
