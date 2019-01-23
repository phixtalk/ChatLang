package com.chatlang.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.chatlang.R;

/**
 * Created by ADMIN on 10/2/2018.
 */

public class CustomFontsView extends android.support.v7.widget.AppCompatTextView {
    public CustomFontsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public CustomFontsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CustomFontsView(Context context) {
        super(context);
        init();
    }
    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.font_family));
        setTypeface(tf ,1);
    }
}