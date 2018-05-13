package com.shishuheng.reader.process;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shishuheng on 2018/1/9.
 */

public class BookInformationDatabaseOpenHelper extends SQLiteOpenHelper {
    private static String CREAT_BOOKSINFO = "create table Books("
            + "id integer primary key autoincrement,"
            + "path text not null,"
            + "author text,"
            + "title text,"
            + "category text,"
            + "image text,"
            + "readPointer integer,"
            + "codingFormat integer,"
            + "totality integer)";

    private static String CREAT_BOOKMARK = "create table BookMarks("
            + "id integer primary key autoincrement,"
            + "bookId integer,"
            + "readPointer integer,"
            + "text text,"
            + "FOREIGN KEY (bookId) REFERENCES Books(id))";

    private static String CREAT_SETTING = "create table Settings("
            + "id integer primary key default 1,"
            + "nightMode integer default 0,"
            + "textSize integer default 3)";

    private Context mContext;

    public BookInformationDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAT_SETTING);
        db.execSQL(CREAT_BOOKSINFO);
        db.execSQL(CREAT_BOOKMARK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
