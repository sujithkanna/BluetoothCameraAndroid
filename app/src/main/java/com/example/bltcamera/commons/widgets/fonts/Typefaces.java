package com.example.bltcamera.commons.widgets.fonts;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;


public class Typefaces {

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}