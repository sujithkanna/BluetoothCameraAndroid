package com.example.bltcamera.modules.camera;

import android.hardware.Camera;
import android.os.Bundle;

/**
 * Created by hmspl on 7/2/16.
 */
public interface CameraModel {

    void saveDeviceDetail(Bundle extras);

    void saveImage(byte[] data);

    void stopSocket();

    Camera.PreviewCallback getPreviewCallback();

}
