package com.example.bltcamera.modules.camera;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;

import com.example.bltcamera.Constants.Device;
import com.example.bltcamera.commons.ThreadHandler;
import com.example.bltcamera.utils.CodeSnippet;
import com.example.bltcamera.utils.bluetooth.BluetoothHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hmspl on 7/2/16.
 */
public class CameraModelImpl implements CameraModel, BluetoothListener, Camera.PreviewCallback {

    private int previewMissedCount;

    private byte[] mPendingImageBytes;

    private BluetoothDevice mBluetoothDevice;

    private BluetoothHandler mBluetoothHandler;

    private final CameraModelListener mCameraModelListener;

    private boolean mInitAck = true;
    private boolean mDataAck = true;

    public CameraModelImpl(CameraModelListener cameraModelListener) {
        mCameraModelListener = cameraModelListener;
        mBluetoothHandler = BluetoothHandler.newInstance(this, false);
    }

    public static CameraModel newInstance(CameraModelListener cameraModelListener) {
        return new CameraModelImpl(cameraModelListener);
    }

    @Override
    public void saveDeviceDetail(Bundle extras) {
        mBluetoothDevice = extras.getParcelable(Device.BLUETOOTH_DEVICE_DTO);
        mBluetoothHandler.connect(mBluetoothDevice);
    }

    @Override
    public void saveImage(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 6;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[32 * 1024];
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // others devices
        int orientation;
        if (bMap.getHeight() < bMap.getWidth()) {
            orientation = 90;
        } else {
            orientation = 0;
        }

        Bitmap bMapRotate;
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
        } else {
            bMapRotate = Bitmap.createScaledBitmap(bMap, bMap.getWidth(), bMap.getHeight(), true);
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(CodeSnippet.getOutputMediaFile());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bMapRotate.compress(Bitmap.CompressFormat.PNG, 100, stream);
            out.write(stream.toByteArray());
            out.flush();
            if (bMapRotate != null) {
                bMapRotate.recycle();
                bMapRotate = null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopSocket() {
        mBluetoothHandler.stop();
    }

    @Override
    public CameraModelImpl getPreviewCallback() {
        return this;
    }


    @Override
    public void onReceivedData(byte[] bytes) {

    }

    @Override
    public void onConnected(BluetoothDevice device) {
        mCameraModelListener.onConnectionSuccessful(this);
    }

    @Override
    public void connectionFailed() {
        mCameraModelListener.onConnectionFailed();
    }

    @Override
    public void onLostConnection() {
        mCameraModelListener.onLostConnection();
    }

    @Override
    public void onGotAck(String ack) {
        if (ack.equals(BluetoothHandler.ACK_INIT)) {
            mInitAck = true;
            mBluetoothHandler.write(mPendingImageBytes);
            mBluetoothHandler.write(BluetoothHandler.DATA_END.getBytes());
        } else if (ack.equals(BluetoothHandler.ACK_DATA_RECEIVED)) {
            mDataAck = true;
        } else if (ack.equals(BluetoothHandler.CAMERA)) {
            mCameraModelListener.onCameraCommandReceived();
        } else if (ack.equals(BluetoothHandler.RECORD)) {
            mCameraModelListener.onRecordCommandReceived();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (mInitAck && mDataAck) {
            mInitAck = false;
            mDataAck = false;
            previewMissedCount = 0;
            ThreadHandler.getInstance().doInBackground(new Runnable() {
                @Override
                public void run() {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, out);
                    byte[] imageBytes = out.toByteArray();
                    mBluetoothHandler.write(BluetoothHandler.DATA_START.getBytes());
                    mPendingImageBytes = imageBytes;
                }
            });
        } else {
            previewMissedCount++;
            if (previewMissedCount > 50) {
                mInitAck = true;
                mDataAck = true;
            }
        }
    }

    public interface CameraModelListener {

        void onConnectionSuccessful(Camera.PreviewCallback previewCallback);

        void onConnectionFailed();

        void onLostConnection();

        void onCameraCommandReceived();

        void onRecordCommandReceived();

    }

}
