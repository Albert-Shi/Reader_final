package com.shishuheng.reader.ui.coustomize;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by shishuheng on 2018/1/16.
 */

public class ReaderTextView extends TextView {
    Layout layout = getLayout();
    public ReaderTextView(Context context) {
        super(context);
    }

    public ReaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReaderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReaderTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getLineNumber() {
        int height = getHeight();
        int lineHeight = getLineHeight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        return (height-paddingTop-paddingBottom)/lineHeight;
    }

    public int getCharNumber() {
        float textSize = getTextSize();
        int width = getWidth();
        float letterSpacing = getLetterSpacing();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        return (int)((width-paddingLeft-paddingRight)/(textSize+letterSpacing));
    }

    public float getLetterSize() {
        return getPaint().measureText("W");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
