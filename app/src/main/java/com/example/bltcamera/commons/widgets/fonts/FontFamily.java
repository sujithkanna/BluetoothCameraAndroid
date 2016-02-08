package com.example.bltcamera.commons.widgets.fonts;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by hmspl on 15/1/16.
 */
public class FontFamily {

    public static final int ROBOTO_BOLD = 3;
    public static final int ROBOTO_LIGHT = 1;
    public static final int ROBOTO_MEDIUM = 2;
    public static final int ROBOTO_REGULAR = 0;


    public static Typeface getTypeface(Context context, int selection) {
        String path;
        switch (selection) {
            case ROBOTO_BOLD:
                path = "fonts/Roboto-Bold.ttf";
                break;
            case ROBOTO_LIGHT:
                path = "fonts/Roboto-Light.ttf";
                break;
            case ROBOTO_MEDIUM:
                path = "fonts/Roboto-Medium.ttf";
                break;
            case ROBOTO_REGULAR:
            default:
                path = "fonts/Roboto-Regular.ttf";
                break;
        }
        return Typefaces.get(context, path);
    }

}
