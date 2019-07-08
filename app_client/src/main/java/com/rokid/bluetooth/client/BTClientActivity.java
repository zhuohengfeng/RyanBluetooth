package com.rokid.bluetooth.client;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.bluetooth.IBluetoothCallback;
import com.rokid.bluetooth.R;
import com.rokid.bluetooth.RokidBluetoothManager;
import com.rokid.bluetooth.message.GlassesMessage;
import com.rokid.bluetooth.message.PoliceMobileMessage;

import java.util.ArrayList;
import java.util.List;

public class BTClientActivity extends BaseActivity implements AdapterView.OnItemClickListener, IBluetoothCallback {

    private TextView mConnectionStatus;
    private ListView mDevicesList;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BaseAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        RokidBluetoothManager.getInstance().addBluetoothCallback(this);

        mDevicesList = findViewById(R.id.list);
        mConnectionStatus = findViewById(R.id.text);
        devices.addAll(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
        mBlueAdapter = new MyAdapter();
        mDevicesList.setAdapter(mBlueAdapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mFindReceiver, intentFilter);
        mDevicesList.setOnItemClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mFindReceiver);
        RokidBluetoothManager.getInstance().removeBluetoothCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RokidBluetoothManager.getInstance().isActivied()) {
            mConnectionStatus.setText("已经连接上服务器");
        }
    }

    public void find(View view) {
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public void sendCmd(View view) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = GlassesMessage.TYPE_MOBILE_FACE;
        msg.mobileMessage = new PoliceMobileMessage();
        RokidBluetoothManager.getInstance().sendMessage(msg);
    }

    public void sendImage(View view) {
        chooseImage();
    }


    private BroadcastReceiver mFindReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            mBlueAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        // 主动连接设备
        mConnectionStatus.setText("正在连接中...");
        Log.d("Rokid_BT", "[Client]开始连接服务器....devices.get(position)="+devices.get(position));
        RokidBluetoothManager.getInstance().connectBTServer(devices.get(position));
    }

    public void onFaceid(View view) {
        if (!RokidBluetoothManager.getInstance().isActivied()) {
            Toast.makeText(this, "请连接服务端", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, FaceIDActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = new TextView(parent.getContext());
            text.setText(devices.get(position).getName());
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
            return text;
        }
    }


    @Override
    public void onBluetoothStatusChange(RokidBluetoothManager.BlueSocketStatus status) {
        Log.d("Rokid_BT", "[Client]onBluetoothStatusChange status="+status);
        if (status == RokidBluetoothManager.BlueSocketStatus.CONNEDTIONED) {
            mConnectionStatus.setText("已经连接上服务器");
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.DISCONNECTION) {
            mConnectionStatus.setText("已经断开连接");
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.CONNECTION_FAILED) {
            mConnectionStatus.setText("连接失败");
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.SEND_MESSAGE_SUCCESS) {
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        }
        else if (status == RokidBluetoothManager.BlueSocketStatus.SEND_MESSAGE_FAILED) {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBluetoothMessageReceiver(GlassesMessage message) {
        Toast.makeText(this, "收到消息, type="+message.type, Toast.LENGTH_SHORT).show();
        if (GlassesMessage.TYPE_TRANSFER == message.type){
            Log.d("Rokid_Bt", "[Client]收到文件个数:"+message.transferMessage.dataList.size()+", 数据大小 ="+message.transferMessage.dataList.get(0).bytes.length);
        }
    }
}
