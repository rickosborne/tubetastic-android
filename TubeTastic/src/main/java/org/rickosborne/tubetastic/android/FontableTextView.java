package org.rickosborne.tubetastic.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Typeface;

public class FontableTextView extends TextView {

    public FontableTextView(Context context) {
        super(context);
        // initialize(context);
    }

    public FontableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        UiUtil.setCustomFont(this,context,attrs, R.styleable.FontableTextView, R.styleable.FontableTextView_font);
        initialize(context, attrs);
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        UiUtil.setCustomFont(this,context,attrs, R.styleable.FontableTextView, R.styleable.FontableTextView_font);
        initialize(context, attrs);
    }

    public void initialize(Context context, AttributeSet attrs){
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs, R.styleable.FontableTextView, R.styleable.FontableTextView_font);
        }
//        if( !isInEditMode() ){
//            try {
//                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableTextView);
//                String fontName = a.getString(R.styleable.FontableTextView_font);
//                String fontPath = "fonts/" + fontName + ".ttf";
//                Log.d("font", fontPath);
//                Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
//                setTypeface(tf);
//            }
//            catch (NullPointerException npe) {
//                // do nothing
//            }
//        }
    }
}