package com.rokid.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;

import com.rokid.bluetooth.utils.PermissionHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PermissionHelper.hasWriteStoragePermission(this)) {
            PermissionHelper.requestWriteStoragePermission(this);
        }

        acquireWakeLock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }

    public void onBtnServer(View view) {
        Intent intent = new Intent(this, BTServerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onBtnClient(View view) {
        Intent intent = new Intent(this, BTClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private PowerManager.WakeLock wakeLock;
    public void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            wakeLock.acquire();
        }
    }

    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock =null;
        }
    }
}
