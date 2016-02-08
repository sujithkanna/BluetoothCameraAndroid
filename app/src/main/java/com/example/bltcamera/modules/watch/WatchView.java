package com.example.bltcamera.modules.watch;

import android.graphics.Bitmap;

import com.example.bltcamera.commons.BaseView;

/**
 * Created by hmspl on 7/2/16.
 */
public interface WatchView extends BaseView {

    void promptUserToEnableBluetooth();

    boolean isBluetoothEnabled();

    void closeWatchView();

    void showFrameInImageView(Bitmap image);

    void showProgressDialog();

    void hideProgressDialog();

    void setProgressMessage(String name);

}
