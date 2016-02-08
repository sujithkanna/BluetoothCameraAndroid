package com.example.bltcamera.commons.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.bltcamera.R;
import com.example.bltcamera.commons.widgets.fonts.FontFamily;


/**
 * Created by hmspl on 15/1/16.
 */
public class CTextView extends TextView {

    public CTextView(Context context) {
        super(context);
        initFonts(context, null);
    }

    public CTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFonts(context, attrs);
    }

    public CTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFonts(context, attrs);
    }

    private void initFonts(Context context, AttributeSet attrs) {
        int selection = FontFamily.ROBOTO_REGULAR;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CTextView);
            selection = a.getInt(R.styleable.CTextView_typeface, FontFamily.ROBOTO_REGULAR);
        }
        setFontType(selection);
    }

    public void setFontType(int font) {
        setTypeface(FontFamily.getTypeface(getContext(), font));
    }
}
