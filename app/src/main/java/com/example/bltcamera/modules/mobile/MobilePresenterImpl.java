package com.example.bltcamera.modules.mobile;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.CAdapter;
import com.example.bltcamera.commons.widgets.CTextView;

/**
 * Created by hmspl on 7/2/16.
 */
public class MobilePresenterImpl implements MobilePresenter, MobileModelImpl.MobileModelListener, CAdapter.OnGetViewListener<BluetoothDevice> {

    private MobileView mMobileView;

    private MobileModel mMobileModel;

    public MobilePresenterImpl(MobileView mobileView) {
        mMobileView = mobileView;
        mMobileModel = MobileModelImpl.newInstance(this);
    }

    public static MobilePresenter newInstance(MobileView mobileView) {
        return new MobilePresenterImpl(mobileView);
    }

    @Override
    public void onCreateView() {
        mMobileView.initViews();
        mMobileView.setAdapter(mMobileModel.getAdapter());
        if (mMobileView.isBluetoothEnabled()) {
            onBluetoothEnabled();
        }
    }

    @Override
    public void onClickEnable() {
        mMobileView.enableBluetooth();
    }

    @Override
    public void onBluetoothEnabled() {
        mMobileModel.fetchPairedDevicesList();
        mMobileView.hideInapp();
    }

    @Override
    public void onClickNewDevice() {
        mMobileModel.listenForNewDevices();
        mMobileView.showNewDeviceDialog(mMobileModel.getNewDeviceAdapter());
    }

    @Override
    public void onPairedDeviceSelected(int position) {
        Bundle bundle = mMobileModel.getDeviceBundle(position);
        mMobileView.navigateToCameraActivity(bundle);
    }

    @Override
    public void onNewDeviceClicked(int position) {
        mMobileView.hideNewDeviceList();
        mMobileModel.pairToDevice(position);
        mMobileModel.stopListeningToNewDevices();
    }

    @Override
    public View getView(View convertView, int position, BluetoothDevice object) {
        if (convertView == null) {
            convertView = View.inflate(mMobileView.getContext(), R.layout.inflater_devices_list_item, null);
        }
        ((CTextView) convertView.findViewById(R.id.inflater_device_list_name)).setText(object.getName());
        return convertView;
    }

    @Override
    public Context getContext() {
        return mMobileView.getContext();
    }

    @Override
    public CAdapter.OnGetViewListener<BluetoothDevice> getNewDeviceGetView() {
        return new CAdapter.OnGetViewListener<BluetoothDevice>() {
            @Override
            public View getView(View convertView, int position, BluetoothDevice object) {
                if (convertView == null) {
                    convertView = View.inflate(mMobileView.getContext(), R.layout.inflater_devices_list_item, null);
                }
                ((CTextView) convertView.findViewById(R.id.inflater_device_list_name)).setText(object.getName());
                return convertView;
            }
        };
    }
}
