package com.rokid.bluetooth.server;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.bluetooth.IBluetoothCallback;
import com.rokid.bluetooth.R;
import com.rokid.bluetooth.RokidBluetoothManager;
import com.rokid.bluetooth.message.GlassesMessage;
import com.rokid.bluetooth.message.PoliceGlassesMessage;
import com.rokid.bluetooth.message.TransferMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BTServerActivity extends BaseActivity implements IBluetoothCallback {

    private TextView mConnectionStatus;

    private LinearLayout mContentLayout;
    private TextView mWaitingTips;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        RokidBluetoothManager.getInstance().addBluetoothCallback(this);

        mConnectionStatus = findViewById(R.id.text);
        mContentLayout = findViewById(R.id.ll_content);
        mContentLayout.setVisibility(View.GONE);
        mWaitingTips = findViewById(R.id.tx_waiting_tips);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RokidBluetoothManager.getInstance().removeBluetoothCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RokidBluetoothManager.getInstance().isActivied()) { // 如果已经连上了
            mContentLayout.setVisibility(View.VISIBLE);
            mConnectionStatus.setText("已经连接上服务器");
            mWaitingTips.setVisibility(View.GONE);
        }
        else {
            mConnectionStatus.setText("等待连接中...");
            mContentLayout.setVisibility(View.GONE);
            mWaitingTips.setVisibility(View.VISIBLE);
        }
    }

    public void sendCmd(View view) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = GlassesMessage.TYPE_GLASSES_FACE;
        msg.glassesMessage = new PoliceGlassesMessage();
        RokidBluetoothManager.getInstance().sendMessage(msg);
    }

    public void sendImage(View view) {
        chooseImage();
    }

    @Override
    public void onBluetoothStatusChange(RokidBluetoothManager.BlueSocketStatus status) {
        Log.d("Rokid_BT", "[Client]onBluetoothStatusChange status="+status);
        if (status == RokidBluetoothManager.BlueSocketStatus.CONNEDTIONED) {
            mConnectionStatus.setText("有客户端已经连接上");
            mContentLayout.setVisibility(View.VISIBLE);
            mWaitingTips.setVisibility(View.GONE);
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.DISCONNECTION) {
            mConnectionStatus.setText("客户端连接已经断开");
            mContentLayout.setVisibility(View.GONE);
            mWaitingTips.setVisibility(View.VISIBLE);
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.CONNECTION_FAILED) {
            mConnectionStatus.setText("连接失败");
            mContentLayout.setVisibility(View.GONE);
            mWaitingTips.setVisibility(View.VISIBLE);
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.SEND_MESSAGE_SUCCESS) {
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
            Log.d("Rokid_Bt", "[Service] 发送成功 ");
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.SEND_MESSAGE_FAILED) {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
            Log.e("Rokid_Bt", "[Service] 发送失败 ");
        }
    }

    @Override
    public void onBluetoothMessageReceiver(GlassesMessage message) {
        Toast.makeText(this, "收到消息  type="+message.type, Toast.LENGTH_SHORT).show();
        if (GlassesMessage.TYPE_MOBILE_FACE == message.type) {
            Log.d("Rokid_Bt", "[Server]收到消息, 开始发送人脸 type="+message.type);
            //sendFaceData();
        }
        else if (GlassesMessage.TYPE_TRANSFER == message.type){
            Log.d("Rokid_Bt", "[Server]收到文件个数:"+message.transferMessage.dataList.size()+", 数据大小 ="+message.transferMessage.dataList.get(0).bytes.length);
        }
    }

    private void sendFaceData() {
        for (int i=1; i< 11; i++) {
            GlassesMessage msg = new GlassesMessage();
            msg.id = i;
            msg.type = GlassesMessage.TYPE_TRANSFER;
            msg.transferMessage = new TransferMessage();
            List<File> files = new ArrayList<>();
            File tempFile = new File("/sdcard/test_face/"+i+".png");
            if (!tempFile.exists()) {
                continue;
            }
            Log.d("Rokid_Bt", "sendFaceData tempFile="+tempFile.getAbsolutePath());
            files.add(tempFile);
            msg.transferMessage.processPath(files);
            RokidBluetoothManager.getInstance().sendMessage(msg);
        }

    }
}
