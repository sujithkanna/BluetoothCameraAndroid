package com.example.bltcamera.modules.mobile;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import com.example.bltcamera.commons.CAdapter;

/**
 * Created by hmspl on 7/2/16.
 */
public interface MobileModel {

    CAdapter<BluetoothDevice> getAdapter();

    void fetchPairedDevicesList();

    Bundle getDeviceBundle(int position);

    CAdapter<BluetoothDevice> getNewDeviceAdapter();

    void listenForNewDevices();

    void stopListeningToNewDevices();

    void pairToDevice(int position);
}
