package com.example.bltcamera.modules.mobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.bltcamera.Constants.Device;
import com.example.bltcamera.commons.CAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hmspl on 7/2/16.
 */
public class MobileModelImpl implements MobileModel {

    private MobileModelListener mMobileModelListener;

    private CAdapter<BluetoothDevice> mDeviceListAdapter;

    private CAdapter<BluetoothDevice> mNewDeviceAdapter;

    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>();

    private List<BluetoothDevice> newBluetoothDevice = new ArrayList<>();

    private Map<String, String> mNewDeviceAddressMap = new HashMap<>();

    private final BroadcastReceiver mNewDeviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mNewDeviceAddressMap.containsKey(device.getAddress())) {
                    mNewDeviceAddressMap.put(device.getAddress(), "got");
                    newBluetoothDevice.add(device);
                    mNewDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED) {
                    mBluetoothDeviceList.add(device);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public MobileModelImpl(MobileModelListener mobileModelListener) {
        mMobileModelListener = mobileModelListener;
        mDeviceListAdapter = new CAdapter<>(mobileModelListener.getContext(), (CAdapter.OnGetViewListener) mobileModelListener, -1, mBluetoothDeviceList);
        mNewDeviceAdapter = new CAdapter<>(mobileModelListener.getContext(), mobileModelListener.getNewDeviceGetView(), -1, newBluetoothDevice);
    }

    public static MobileModel newInstance(MobileModelListener mobileModelListener) {
        return new MobileModelImpl(mobileModelListener);
    }

    @Override
    public CAdapter<BluetoothDevice> getAdapter() {
        return mDeviceListAdapter;
    }

    @Override
    public void fetchPairedDevicesList() {
        for (BluetoothDevice bluetoothDevice : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
            mBluetoothDeviceList.add(bluetoothDevice);
        }
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public Bundle getDeviceBundle(int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Device.BLUETOOTH_DEVICE_DTO, mDeviceListAdapter.getItem(position));
        return bundle;
    }

    @Override
    public CAdapter<BluetoothDevice> getNewDeviceAdapter() {
        return mNewDeviceAdapter;
    }

    @Override
    public void listenForNewDevices() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mMobileModelListener.getContext().registerReceiver(mNewDeviceFoundReceiver, filter);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    public void stopListeningToNewDevices() {
        mNewDeviceAdapter.clear();
        newBluetoothDevice.clear();
        mNewDeviceAddressMap.clear();
        mMobileModelListener.getContext().unregisterReceiver(mNewDeviceFoundReceiver);
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    @Override
    public void pairToDevice(int position) {
        BluetoothDevice bluetoothDevice = mNewDeviceAdapter.getItem(position);
        pairDevice(bluetoothDevice);
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface MobileModelListener {

        Context getContext();

        CAdapter.OnGetViewListener<BluetoothDevice> getNewDeviceGetView();

    }

}
