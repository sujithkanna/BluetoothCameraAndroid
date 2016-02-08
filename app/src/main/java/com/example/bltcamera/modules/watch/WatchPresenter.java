package com.example.bltcamera.modules.watch;

import android.os.Bundle;

/**
 * Created by hmspl on 7/2/16.
 */
public interface WatchPresenter {

    void onCreate(Bundle extras);

    void initiateBluetooth();

    void onClickCamera();

    void onClickRecord();

}
