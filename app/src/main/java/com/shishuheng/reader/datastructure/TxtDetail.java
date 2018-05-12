package com.shishuheng.reader.datastructure;

import com.shishuheng.reader.process.Book;

import java.io.File;
import java.io.Serializable;

/**
 * Created by shishuheng on 2018/1/2.
 */
//此类用来存储书本的数据
public class TxtDetail implements Serializable {
    private String name;
    private String path;
    private long hasReadPointer = 0;
    private String firstLineLastExit = "";
    private int codingFormat = 1;
    private long totality = 0;

    public int getCodingFormat() {
        return codingFormat;
    }

    public void setCodingFormat(int codingFormat) {
        this.codingFormat = codingFormat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setHasReadPointer(long hasReadPointer) {
        this.hasReadPointer = hasReadPointer;
    }

    public long getHasReadPointer() {
        return hasReadPointer;
    }

    public String getFirstLineLastExit() {
        return firstLineLastExit;
    }

    public void setFirstLineLastExit(String firstLineLastExit) {
        this.firstLineLastExit = firstLineLastExit;
    }

    public void setTotality(long totality) {
        this.totality = totality;
    }

    public long getTotality() {
        return totality;
    }

    public void sychronizationToBook(Book book) {
        book.setReadPointer(hasReadPointer);
        book.setCodingFormat(codingFormat);
        book.setName(name);
        book.setTotality(totality);
        book.setFilePath(new File(path));
        if (book.bookFullScreen.size() > 0)
            firstLineLastExit = book.bookFullScreen.get(0) + book.bookFullScreen.get(1) + book.bookFullScreen.get(3);
    }
}
