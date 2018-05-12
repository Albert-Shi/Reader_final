package com.shishuheng.reader.datastructure;

import com.shishuheng.reader.process.Book;

/**
 * Created by shishuheng on 2018/1/17.
 */

public class ViewParams {
    public int height;
    public int width;
    public int paddingTop;
    public int paddingBottom;
    public int paddingLeft;
    public int paddingRight;
    public int letterSpacing;
    public int lineSpacing;
    public int textSize;
    public ViewParams(int height, int width, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight, int letterSpacing, int lineSpacing, int textSize) {
        this.height = height;
        this.width = width;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.letterSpacing = letterSpacing;
        this.lineSpacing = lineSpacing;
        this.textSize = textSize;
    }

    public void transport(Book book) {
        book.setHeight(height);
        book.setWidth(width);
        book.setLineCharacterNumber((int)((width-paddingLeft-paddingRight)/(textSize+letterSpacing)));
        book.setLineTotalNumber((int)((height-paddingTop-paddingBottom)/(textSize+lineSpacing)));
    }
}
