package com.example.bltcamera.modules.test;

import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.commons.ThreadHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by hmspl on 5/2/16.
 */
public class TestActivity extends BaseActivity implements SurfaceHolder.Callback, CompoundButton.OnCheckedChangeListener, Camera.PreviewCallback {

    private int mSampler = 0;

    private int width = 320;
    private int height = 240;

    private static final String TAG = "TestActivity";

    private Camera mCamera;

    private ImageView mImageView;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private ToggleButton mCameraOnOffBUtton;

    private BluetoothDevice mBluetoothDevice;

    private BluetoothHandler mBluetoothHandler;

    private MediaRecorder mMediaRecorder;
    private int previewFormat;
    private boolean mInitAck = true;
    private boolean mDataAck = true;
    private byte[] mPendingImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mCameraOnOffBUtton = (ToggleButton) findViewById(R.id.camera_on_off_toggle);

        mImageView = (ImageView) findViewById(R.id.imageView);

        setCamera();
        setCameraOrientation();

        mBluetoothDevice = getIntent().getExtras().getParcelable("data");

        mBluetoothHandler = BluetoothHandler.newInstance(new BluetoothListener() {
            @Override
            public void onReceivedData(byte[] bytes) {

            }

            @Override
            public void onConnected(BluetoothDevice device) {
                mCamera.setPreviewCallback(TestActivity.this);
                mCamera.startPreview();
            }

            @Override
            public void connectionFailed() {

            }

            @Override
            public void onLostConnection() {

            }

            @Override
            public void onGotAck(String ack) {
                Log.e(TAG, ack);
                if (ack.equals(BluetoothHandler.ACK_INIT)) {
                    mInitAck = true;
                    mBluetoothHandler.write(mPendingImageBytes);
                    mBluetoothHandler.write(BluetoothHandler.DATA_END.getBytes());
                } else if (ack.equals(BluetoothHandler.ACK_DATA_RECEIVED)) {
                    mDataAck = true;
                }
            }
        }, false);

        mBluetoothHandler.start();
        mBluetoothHandler.connect(mBluetoothDevice);

        //This is to allow window to select pixel format by itself
        getWindow().setFormat(PixelFormat.UNKNOWN);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        //Deprecated and ignored(Values set automatically)
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCameraOnOffBUtton.setOnCheckedChangeListener(this);


    }

    private void setCamera() {
        mCamera = Camera.open();
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);

            Camera.Parameters parameters = mCamera.getParameters();
            previewFormat = ImageFormat.NV21;
            parameters.setPreviewSize(320, 240);
//            mSurfaceHolder.setFixedSize(width, height);
            parameters.setRotation(90);
            mCamera.setParameters(parameters);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }catch (Exception e){

        }
        mBluetoothHandler.stop();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            try {
                startRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stopRecording();
        }
    }


    protected void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
    }

    private void setCameraOrientation() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "Surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "Surface changed: " + width + " - " + height);
        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setPreviewSize(this.width, this.height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "Surface destroyed");
        mCamera.stopPreview();
    }

    protected void startRecording() throws IOException {
        mMediaRecorder = new MediaRecorder();  // Works well
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile("/sdcard/zzzz.mp4");

        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }


    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (mInitAck && mDataAck) {
            mInitAck = false;
            mDataAck = false;
            ThreadHandler.getInstance().doInBackground(new Runnable() {
                @Override
                public void run() {
                    Camera.Parameters parameters = mCamera.getParameters();
                    if (parameters.getPreviewFormat() == ImageFormat.NV21) {
                        width = parameters.getPreviewSize().width;
                        height = parameters.getPreviewSize().height;
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
                        byte[] imageBytes = out.toByteArray();
                        mBluetoothHandler.write(BluetoothHandler.DATA_START.getBytes());
                        mPendingImageBytes = imageBytes;
                        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        printBitmap(image);


                    /*int[] previewPixels = new int[width * height];
                    Log.e(TAG, "Preview:" + width + " " + height + " " + data.length);
                    decodeYUV(previewPixels, data, width, height);
                    Bitmap bitmap = Bitmap.createBitmap(previewPixels, width, height, Bitmap.Config.ARGB_8888);
                    printBitmap(bitmap);
                    Log.e(TAG, "first "+previewPixels.length);*/
                    } else if (previewFormat == ImageFormat.JPEG || previewFormat == ImageFormat.RGB_565) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inDither = true;
                        opts.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                        printBitmap(bitmap);
                        Log.e(TAG, "second");
                    }
               /* decodeYUV420SP(previewPixels, data, width, height);
                Bitmap bitmap = Bitmap.createBitmap(previewPixels, width, height, Bitmap.Config.RGB_565);
                printBitmap(bitmap);*/
                }

            });
        }
    }

    private void printBitmap(final Bitmap bitmap) {
        if (bitmap != null) {
            Log.e(TAG, "Bitmap:" + bitmap.getWidth() + " - " + bitmap.getHeight() + " - " + bitmap.getByteCount());
            ThreadHandler.getInstance().doInForground(new Runnable() {
                @Override
                public void run() {
                    mImageView.setImageBitmap(bitmap);
                }
            });
        }
    }


    public void decodeYUV(int[] out, byte[] fg, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length
                    + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length
                    + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

    }
}
