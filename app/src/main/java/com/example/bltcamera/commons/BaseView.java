package com.example.bltcamera.commons;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

/**
 * Created by hmspl on 5/2/16.
 */
public interface BaseView {

    void showToast(String message);

    void showToast(String message, int time);

    Context getContext();

    FragmentActivity getActivity();

}
