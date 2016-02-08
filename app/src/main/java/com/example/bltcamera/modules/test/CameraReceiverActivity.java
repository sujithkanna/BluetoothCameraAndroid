package com.example.bltcamera.modules.test;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.commons.ThreadHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothHandler;
import com.example.bltcamera.utils.bluetooth.BluetoothListener;

/**
 * Created by hmspl on 7/2/16.
 */
public class CameraReceiverActivity extends BaseActivity {

    private ImageView mImageView;

    private BluetoothHandler mBluetoothHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_receiver);

        mImageView = (ImageView) findViewById(R.id.imageView);

        mBluetoothHandler = BluetoothHandler.newInstance(new BluetoothListener() {
            @Override
            public void onReceivedData(final byte[] bytes) {
                final Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                Log.e("Bitmap", image.getWidth() + " - " + image.getHeight());

                ThreadHandler.getInstance().doInForground(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(image);
                    }
                });
            }

            @Override
            public void onConnected(BluetoothDevice device) {

            }

            @Override
            public void connectionFailed() {

            }

            @Override
            public void onLostConnection() {

            }

            @Override
            public void onGotAck(String ack) {

            }
        }, true);
        mBluetoothHandler.start();

    }

    @Override
    protected void onStop() {
        mBluetoothHandler.stop();
        super.onStop();
    }
}
