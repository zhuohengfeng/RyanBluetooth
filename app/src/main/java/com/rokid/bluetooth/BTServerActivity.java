package com.rokid.bluetooth;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaiky.imagespickers.ImageConfig;
import com.jaiky.imagespickers.ImageSelector;
import com.jaiky.imagespickers.ImageSelectorActivity;
import com.rokid.bluetooth.message.GlassesMessage;
import com.rokid.bluetooth.message.PoliceGlassesMessage;
import com.rokid.bluetooth.message.TransferMessage;
import com.rokid.bluetooth.utils.GlideLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class BTServerActivity extends BaseBTServerActivity {

    private EditText mEdit;
    private TextView mStatus;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mEdit = (EditText) findViewById(R.id.edit);
        mStatus = (TextView) findViewById(R.id.text);
        mStatus.setText("等待连接中...");
    }


    public void sendText(View view) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = GlassesMessage.TYPE_GLASSES_FACE;
        msg.glassesMessage = new PoliceGlassesMessage();
        sendMessage(msg);
    }

    public void sendImage(View view) {
        chooseImage();
    }


    public void onChooseImage(String path) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = GlassesMessage.TYPE_TRANSFER;
        msg.transferMessage = new TransferMessage();
        try {
            msg.transferMessage.loadBytes(new FileInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(msg);
    }


    @Override
    protected void onBluetoothStatusChange(BlueSocketStatus status) {
        Log.d("Rokid_BT", "[Client]onBluetoothStatusChange status="+status);
        if (status == BlueSocketStatus.CONNEDTIONED) {
            mStatus.setText("有客户端已经连接上");
        }
        else if (status == BlueSocketStatus.DISCONNECTION) {
            mStatus.setText("客户端连接已经断开");
        }
        else if (status == BlueSocketStatus.CONNECTION_FAILED) {
            mStatus.setText("连接失败");
        }
        else if (status == BlueSocketStatus.SEND_MESSAGE_SUCCESS) {
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        }
        else if (status == BlueSocketStatus.SEND_MESSAGE_FAILED) {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onBluetoothMessageReceiver(GlassesMessage message) {
        Toast.makeText(this, "收到消息, type="+message.type, Toast.LENGTH_SHORT).show();
    }
}
