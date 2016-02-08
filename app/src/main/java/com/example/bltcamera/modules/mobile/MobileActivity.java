package com.example.bltcamera.modules.mobile;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.BaseActivity;
import com.example.bltcamera.commons.CAdapter;
import com.example.bltcamera.commons.widgets.CTextView;
import com.example.bltcamera.modules.camera.CameraActivity;

/**
 * Created by hmspl on 7/2/16.
 */
public class MobileActivity extends BaseActivity implements MobileView, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int BT_ENABLE_REQUEST = 99;

    private MobilePresenter mMobilePresenter;

    private ListView mDevicesListView;
    private Dialog mNewDeviceListDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        mMobilePresenter = MobilePresenterImpl.newInstance(this);
        mMobilePresenter.onCreateView();
    }

    @Override
    public void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CTextView cTextView = (CTextView) toolbar.findViewById(R.id.toolbar_textview);
        cTextView.setText("Devices");
        toolbar.setNavigationIcon(R.drawable.back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDevicesListView = (ListView) findViewById(R.id.activity_mobile_device_list);
        mDevicesListView.setOnItemClickListener(this);

        findViewById(R.id.inapp_enable).setOnClickListener(this);
        findViewById(R.id.activity_mobile_new_device).setOnClickListener(this);
    }


    @Override
    public void enableBluetooth() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, BT_ENABLE_REQUEST);
    }

    @Override
    public void hideInapp() {
        findViewById(R.id.inflater_inapp).setVisibility(View.GONE);
    }

    @Override
    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    @Override
    public void setAdapter(CAdapter<BluetoothDevice> adapter) {
        mDevicesListView.setAdapter(adapter);
    }

    @Override
    public void navigateToCameraActivity(Bundle bundle) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void showNewDeviceDialog(CAdapter<BluetoothDevice> newDeviceAdapter) {
        mNewDeviceListDialog = new Dialog(this);
        mNewDeviceListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(this, R.layout.inflater_new_devices, null);
        ListView listView = (ListView) view.findViewById(R.id.new_device_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMobilePresenter.onNewDeviceClicked(position);
            }
        });
        listView.setAdapter(newDeviceAdapter);
        mNewDeviceListDialog.setContentView(view);
        mNewDeviceListDialog.setCancelable(true);
        mNewDeviceListDialog.setCanceledOnTouchOutside(false);
        mNewDeviceListDialog.show();
    }

    @Override
    public void hideNewDeviceList() {
        mNewDeviceListDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inapp_enable:
                mMobilePresenter.onClickEnable();
                break;
            case R.id.activity_mobile_new_device:
                mMobilePresenter.onClickNewDevice();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mMobilePresenter.onBluetoothEnabled();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMobilePresenter.onPairedDeviceSelected(position);
    }
}
