package com.example.bltcamera.utils.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hmspl on 7/2/16.
 */
public interface BluetoothListener {

    void onReceivedData(byte[] bytes);

    void onConnected(BluetoothDevice device);

    void connectionFailed();

    void onLostConnection();

    void onGotAck(String ack);

}
