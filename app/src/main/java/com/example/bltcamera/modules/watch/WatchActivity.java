package com.example.bltcamera.modules.watch;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.commons.widgets.CTextView;

/**
 * Created by hmspl on 7/2/16.
 */
public class WatchActivity extends BaseActivity implements WatchView, View.OnClickListener {

    private static final int BT_ENABLE_REQUEST = 33;

    private Dialog dialog;

    private View mDummyView;

    private View mExitButton;

    private CTextView mProgressText;

    private ImageView mPreviewImageView;

    private WatchPresenter mWatchPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        mPreviewImageView = (ImageView) findViewById(R.id.activity_watch_imageView);
        findViewById(R.id.capture).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);

        mWatchPresenter = WatchPresenterImpl.newInstance(this);
        mWatchPresenter.onCreate(getIntent().getExtras());

    }

    @Override
    public void promptUserToEnableBluetooth() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, BT_ENABLE_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWatchPresenter.initiateBluetooth();
    }

    @Override
    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    @Override
    public void closeWatchView() {
        finish();
    }

    @Override
    public void showFrameInImageView(Bitmap image) {
        mPreviewImageView.setImageBitmap(image);
    }

    @Override
    public void showProgressDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View dialogView = View.inflate(this, R.layout.inflater_waiting_for_connection, null);
        mDummyView = dialogView.findViewById(R.id.dummy);
        mExitButton = dialogView.findViewById(R.id.progress_exit);
        mProgressText = (CTextView) dialogView.findViewById(R.id.progress_message);
        mExitButton.setOnClickListener(this);

        dialog.setCancelable(false);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public void hideProgressDialog() {
        dialog.dismiss();
    }

    @Override
    public void setProgressMessage(String name) {
        mProgressText.setText(name);
        mDummyView.setVisibility(View.GONE);
        mExitButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.progress_exit:
                dialog.dismiss();
                onBackPressed();
                break;
            case R.id.capture:
                mWatchPresenter.onClickCamera();
                break;
            case R.id.record:
                mWatchPresenter.onClickRecord();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_ENABLE_REQUEST) {
            if (resultCode == RESULT_OK) {
                mWatchPresenter.initiateBluetooth();
            } else {
                finish();
            }
        }
    }
}
