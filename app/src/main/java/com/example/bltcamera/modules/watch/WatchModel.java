package com.example.bltcamera.modules.watch;

/**
 * Created by hmspl on 7/2/16.
 */
public interface WatchModel {

    void listenForBluetoothConnection();

    boolean isConnected();

    void commandToTakePhoto();

    void commandToStartStopRecording();

}
