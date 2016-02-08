package com.example.bltcamera.modules.watch;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.bltcamera.commons.ThreadHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothListener;

/**
 * Created by hmspl on 7/2/16.
 */
public class WatchModelImpl implements WatchModel, BluetoothListener {

    private BluetoothHandler mBluetoothHandler;

    private WatchModelListener mWatchModelListener;

    public WatchModelImpl(WatchModelListener watchModelListener) {
        mWatchModelListener = watchModelListener;
    }

    public static WatchModel newInstance(WatchModelListener watchModelListener) {
        return new WatchModelImpl(watchModelListener);

    }

    @Override
    public void listenForBluetoothConnection() {
        mBluetoothHandler = BluetoothHandler.newInstance(this, true);
        mBluetoothHandler.start();
    }

    @Override
    public boolean isConnected() {
        return mBluetoothHandler != null && mBluetoothHandler.getState() == BluetoothHandler.STATE_CONNECTED;
    }

    @Override
    public void commandToTakePhoto() {
        mBluetoothHandler.write(BluetoothHandler.CAMERA.getBytes());
    }

    @Override
    public void commandToStartStopRecording() {
        mBluetoothHandler.write(BluetoothHandler.RECORD.getBytes());
    }

    @Override
    public void onReceivedData(byte[] bytes) {
        final Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        ThreadHandler.getInstance().doInForground(new Runnable() {
            @Override
            public void run() {
                mWatchModelListener.onGotFrameBitmap(image);
            }
        });
    }

    @Override
    public void onConnected(BluetoothDevice device) {
        mWatchModelListener.onDeviceConnected(device);
    }

    @Override
    public void connectionFailed() {

    }

    @Override
    public void onLostConnection() {
        mWatchModelListener.onConnectionFailed();
    }

    @Override
    public void onGotAck(String ack) {

    }

    public interface WatchModelListener {

        void onConnectionFailed();

        void onGotFrameBitmap(Bitmap image);

        void onDeviceConnected(BluetoothDevice device);
    }

}
