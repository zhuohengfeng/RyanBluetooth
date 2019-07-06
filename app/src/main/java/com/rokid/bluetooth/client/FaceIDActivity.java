package com.rokid.bluetooth.client;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.rokid.bluetooth.BaseBTClientActivity;
import com.rokid.bluetooth.R;
import com.rokid.bluetooth.message.GlassesMessage;

public class FaceIDActivity extends BaseBTClientActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_faceid);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onBluetoothStatusChange(BlueSocketStatus status) {
        Log.d("Rokid_BT", "[Client]FaceIDActivity status="+status);
        if (status == BlueSocketStatus.CONNEDTIONED) {

        }
        else if (status == BlueSocketStatus.DISCONNECTION) {

        }
        else if (status == BlueSocketStatus.CONNECTION_FAILED) {

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
        Toast.makeText(this, "FaceIDActivity 收到消息, type="+message.type, Toast.LENGTH_SHORT).show();
    }
}
