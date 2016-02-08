package com.example.bltcamera.commons;

import android.support.v4.app.Fragment;

/**
 * Created by hmspl on 5/2/16.
 */
public abstract class BaseFragment extends Fragment implements BaseView {

    @Override
    public void showToast(String message) {
        BaseView baseView = getActivityView();
        if (baseView != null) {
            baseView.showToast(message);
        }
    }

    @Override
    public void showToast(String message, int time) {
        BaseView baseView = getActivityView();
        if (baseView != null) {
            baseView.showToast(message, time);
        }
    }

    private BaseView getActivityView() {
        return (BaseView) getActivity();
    }
}
