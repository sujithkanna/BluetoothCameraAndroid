package com.example.bltcamera.modules.camera;

import android.hardware.Camera;
import android.os.Bundle;

/**
 * Created by hmspl on 7/2/16.
 */
public interface CameraPresenter {

    void onCreateView(Bundle extras);

    void onClickCamera();

    void onClickStartRecord();

    void onClickStopRecord();

    void onPictureTaken(byte[] data);

    void stopEverything();

}
