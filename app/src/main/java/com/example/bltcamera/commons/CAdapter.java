package com.example.bltcamera.commons;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by hmspl on 14/1/16.
 */
public class CAdapter<T> extends ArrayAdapter<T> {

    private OnGetViewListener<T> onGetViewListener;

    public CAdapter(Context context, OnGetViewListener<T> onGetViewListener, int resource, T[] objects) {
        super(context, resource, objects);
        this.onGetViewListener = onGetViewListener;
    }

    public CAdapter(Context context, OnGetViewListener<T> onGetViewListener, int resource, List<T> objects) {
        super(context, resource, objects);
        this.onGetViewListener = onGetViewListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return onGetViewListener.getView(convertView, position, getItem(position));
    }

    public interface OnGetViewListener<T> {

        View getView(View convertView, int position, T object);

    }

}
