package com.example.bltcamera.modules.mobile;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import com.example.bltcamera.commons.BaseView;
import com.example.bltcamera.commons.CAdapter;

/**
 * Created by hmspl on 7/2/16.
 */
public interface MobileView extends BaseView{

    void initViews();

    void enableBluetooth();

    void hideInapp();

    boolean isBluetoothEnabled();

    void setAdapter(CAdapter<BluetoothDevice> adapter);

    void navigateToCameraActivity(Bundle bundle);

    void showNewDeviceDialog(CAdapter<BluetoothDevice> newDeviceAdapter);

    void hideNewDeviceList();

}
