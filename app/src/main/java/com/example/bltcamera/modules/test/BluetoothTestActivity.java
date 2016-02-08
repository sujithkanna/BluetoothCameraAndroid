package com.example.bltcamera.modules.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.commons.CAdapter;
import com.example.bltcamera.commons.widgets.CTextView;
import com.example.bltcamera.utils.bluetooth.BluetoothHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by hmspl on 6/2/16.
 */
public class BluetoothTestActivity extends BaseActivity implements CAdapter.OnGetViewListener<BluetoothDevice>, AdapterView.OnItemClickListener, BluetoothListener, View.OnClickListener {

    private BluetoothHandler mBluetoothHandler;

    private BluetoothAdapter mBluetoothAdapter;

    private Button mNewDeviceButton;

    private ListView mPairedDevicesListView;

    private CAdapter<BluetoothDevice> mDeviceListAdapter;

    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        mBluetoothHandler = BluetoothHandler.newInstance(this, false);
        mBluetoothHandler.start();


        mNewDeviceButton = (Button) findViewById(R.id.newDevice);
        mNewDeviceButton.setOnClickListener(this);

        mPairedDevicesListView = (ListView) findViewById(R.id.pairedDevices);
        mPairedDevicesListView.setOnItemClickListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            showToast("Sorry, your device don't have bluetooth connection.");
            finish();
        } else {
            getPairedDevices();
        }

    }

    private void getPairedDevices() {
        mDeviceListAdapter = new CAdapter<>(this, this, -1, mBluetoothDeviceList);
        mPairedDevicesListView.setAdapter(mDeviceListAdapter);
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            mBluetoothDeviceList.add(bluetoothDevice);
        }
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public View getView(View convertView, int position, BluetoothDevice object) {
        if (convertView == null) {
            convertView = View.inflate(this, R.layout.inflater_devices_list_item, null);
        }
        ((CTextView) convertView.findViewById(R.id.inflater_device_list_name)).setText(object.getName());
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        mBluetoothHandler.connect(mDeviceListAdapter.getItem(position));
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", mDeviceListAdapter.getItem(position));
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onReceivedData(byte[] bytes) {
        Log.e("Byt", bytes.length + "");
        try {
            String data = new String(bytes, "UTF-8");
            Log.e("Data", data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("Message", "error");
        }
    }

    @Override
    public void onConnected(BluetoothDevice device) {
        showToast("Connected");
    }

    @Override
    public void connectionFailed() {
        Log.e("Connection", "failed");
    }

    @Override
    public void onLostConnection() {

    }

    @Override
    public void onGotAck(String ack) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newDevice:
                mBluetoothHandler.write("Hello bluetooth".getBytes());
                break;
        }
    }
}
