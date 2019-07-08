package com.rokid.bluetooth.client;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.jaiky.imagespickers.ImageConfig;
import com.jaiky.imagespickers.ImageSelector;
import com.jaiky.imagespickers.ImageSelectorActivity;
import com.rokid.bluetooth.RokidBluetoothManager;
import com.rokid.bluetooth.message.GlassesMessage;
import com.rokid.bluetooth.message.TransferMessage;
import com.rokid.bluetooth.utils.GlideLoader;
import com.rokid.bluetooth.utils.PermissionHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.rokid.bluetooth.message.GlassesMessage.TYPE_TRANSFER;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


    public void chooseImage(){
        ImageConfig imageConfig
                = new ImageConfig.Builder(new GlideLoader())
                .steepToolBarColor(Color.BLUE)
                .titleBgColor(Color.GREEN)
                .titleSubmitTextColor(Color.WHITE)
                .titleTextColor(Color.WHITE)
                // 开启单选   （默认为多选）
                .singleSelect()
                // 开启拍照功能 （默认关闭）
                .showCamera()
                // 拍照后存放的图片路径（默认 /temp/picture） （会自动创建）
                .filePath("/temp/picture")
                .build();

        ImageSelector.open(this, imageConfig);   // 开启图片选择器
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // 获取选中的图片路径列表 Get Images Path List
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);

            onChooseImage(pathList.get(0));
            for (String path : pathList) {
                Log.i("ImagePath", path);
            }
        }
    }

    public void onChooseImage(String path) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = TYPE_TRANSFER;
        msg.transferMessage = new TransferMessage();
        try {
            List<File> files = new ArrayList<>();
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                return;
            }
            files.add(tempFile);
            msg.transferMessage.processPath(files);
            Log.d("Rokid_Bt", "[Client]onChooseImage 开始发送，tempFile="+tempFile.getAbsolutePath()+
                    ", size="+msg.transferMessage.dataList.get(0).bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RokidBluetoothManager.getInstance().sendMessage(msg);
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
