package com.example.bltcamera.modules.camera;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.utils.CodeSnippet;

import java.io.IOException;

/**
 * Created by hmspl on 7/2/16.
 */
public class CameraActivity extends BaseActivity implements CameraView, View.OnClickListener, SurfaceHolder.Callback {

    private Camera mCamera;

    private SurfaceView mSurfaceView;

    private View mRecordbutton;

    private boolean recording = false;

    private SurfaceHolder mSurfaceHolder;

    private MediaRecorder mMediaRecorder;

    private CameraPresenter mCameraPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraPresenter = CameraPresenterImpl.newInstance(this);
        mCameraPresenter.onCreateView(getIntent().getExtras());
    }

    @Override
    public void initViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.activity_camera_surface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mRecordbutton = findViewById(R.id.record);
        mRecordbutton.setOnClickListener(this);

        findViewById(R.id.camera).setOnClickListener(this);
    }

    @Override
    public void startRecording(Camera.PreviewCallback previewCallback) {
        recording = true;
        mCamera.unlock();
        mRecordbutton.setBackgroundResource(R.drawable.red_circle_background);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setOutputFile("/sdcard/Video.mp4");
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mCamera.startPreview();
            mCamera.setPreviewCallback(previewCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecording() {
        recording = false;
        mRecordbutton.setBackgroundResource(R.drawable.circle_background);
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mCamera.lock();
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takePhotoViaCamera() {
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                mCameraPresenter.onPictureTaken(data);
                mCamera.startPreview();
            }
        });
    }

    @Override
    public void setCameraPreview(Camera.PreviewCallback previewCallback) {
        mCamera.setPreviewCallback(previewCallback);
    }

    @Override
    public void navigateBack() {
        kill();
        finish();
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    private void kill() {
        try {
            stopRecording();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCameraPresenter.stopEverything();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                mCameraPresenter.onClickCamera();
                break;
            case R.id.record:
                if (!recording) {
                    mCameraPresenter.onClickStartRecord();
                } else {
                    mCameraPresenter.onClickStopRecord();
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        if (mCamera != null) {
            Camera.Parameters p = mCamera.getParameters();
            p.setPreviewSize(320, 240);
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(p);
            if (Integer.parseInt(Build.VERSION.SDK) >= 8)
                CodeSnippet.setDisplayOrientation(mCamera, 90);
            else {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    p.set("orientation", "portrait");
                    p.set("rotation", 90);
                }
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    p.set("orientation", "landscape");
                    p.set("rotation", 90);
                }
            }
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        kill();
    }
}
