package com.rokid.bluetooth.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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

import com.jaiky.imagespickers.ImageConfig;
import com.jaiky.imagespickers.ImageSelector;
import com.jaiky.imagespickers.ImageSelectorActivity;
import com.rokid.bluetooth.BaseBTClientActivity;
import com.rokid.bluetooth.R;
import com.rokid.bluetooth.message.GlassesMessage;
import com.rokid.bluetooth.message.PoliceMobileMessage;
import com.rokid.bluetooth.message.TransferMessage;
import com.rokid.bluetooth.utils.GlideLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BTClientActivity extends BaseBTClientActivity implements AdapterView.OnItemClickListener {

    private TextView mConnectionStatus;
    private ListView mDevicesList;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BaseAdapter mBlueAdapter;

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
        setContentView(R.layout.activity_client);

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

    public void find(View view) {
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public void sendCmd(View view) {
        GlassesMessage msg = new GlassesMessage();
        msg.type = GlassesMessage.TYPE_MOBILE_FACE;
        msg.mobileMessage = new PoliceMobileMessage();
        sendMessage(msg);
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mFindReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        // 主动连接设备
        mConnectionStatus.setText("正在连接中...");
        Log.d("Rokid_BT", "[Client]开始连接服务器....");
        connectBTServer(devices.get(position));
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

    public void onFaceid(View view) {
        if (!isActivied()) {
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
    protected void onBluetoothStatusChange(BlueSocketStatus status) {
        Log.d("Rokid_BT", "[Client]onBluetoothStatusChange status="+status);
        if (status == BlueSocketStatus.CONNEDTIONED) {
            mConnectionStatus.setText("已经连接上服务器");
        }
        else if (status == BlueSocketStatus.DISCONNECTION) {
            mConnectionStatus.setText("已经断开连接");
        }
        else if (status == BlueSocketStatus.CONNECTION_FAILED) {
            mConnectionStatus.setText("连接失败");
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
