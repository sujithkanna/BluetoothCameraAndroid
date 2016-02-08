package com.example.bltcamera.modules.mobile;

/**
 * Created by hmspl on 7/2/16.
 */
public interface MobilePresenter {

    void onCreateView();

    void onClickEnable();

    void onBluetoothEnabled();

    void onClickNewDevice();

    void onPairedDeviceSelected(int position);

    void onNewDeviceClicked(int position);

}
