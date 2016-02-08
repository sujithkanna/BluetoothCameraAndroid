package com.example.bltcamera.modules.watch;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.bltcamera.commons.ThreadHandler;

/**
 * Created by hmspl on 7/2/16.
 */
public class WatchPresenterImpl implements WatchPresenter, WatchModelImpl.WatchModelListener {

    private WatchView mWatchView;

    private WatchModel mWatchModel;

    public WatchPresenterImpl(WatchView watchView) {
        mWatchView = watchView;
        mWatchModel = WatchModelImpl.newInstance(this);
    }

    public static WatchPresenter newInstance(WatchView watchView) {
        return new WatchPresenterImpl(watchView);
    }

    @Override
    public void onCreate(Bundle extras) {

    }

    @Override
    public void initiateBluetooth() {
        checkForBluetoothConnection();
    }

    @Override
    public void onClickCamera() {
        mWatchModel.commandToTakePhoto();
    }

    @Override
    public void onClickRecord() {
        mWatchModel.commandToStartStopRecording();
    }

    private void checkForBluetoothConnection() {
        if (mWatchView.isBluetoothEnabled()) {
            if (!mWatchModel.isConnected()) {
                mWatchModel.listenForBluetoothConnection();
                mWatchView.showProgressDialog();
            }
        } else {
            mWatchView.promptUserToEnableBluetooth();
        }
    }

    @Override
    public void onConnectionFailed() {
        mWatchView.showToast("Connection lost");
        mWatchView.closeWatchView();
    }

    @Override
    public void onGotFrameBitmap(Bitmap image) {
        mWatchView.showFrameInImageView(image);
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        mWatchView.setProgressMessage(device.getName());
        ThreadHandler.getInstance().doInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dismissProgressAfterTimer();
            }
        });
    }

    private void dismissProgressAfterTimer() {
        ThreadHandler.getInstance().doInForground(new Runnable() {
            @Override
            public void run() {
                mWatchView.hideProgressDialog();
            }
        });
    }
}
