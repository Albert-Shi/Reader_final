package com.shishuheng.reader.process;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.shishuheng.reader.datastructure.TxtDetail;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shishuheng on 2018/1/27.
 */

public class DatabaseOperator {
    //数据库名称
    public static String DATABASE_NAME = "BookReader.db";
    public static String TABLE_BOOKS = "Books";
    public static String TABLE_SETTINGS = "Settings";
    public static String TABLE_BOOKMARKS = "Bookmarks";
    public static int DATABASE_VERSION = 1;

//    String q1 = "select readPointer from Books where path=?";
    BookInformationDatabaseOpenHelper helper;
    SQLiteDatabase db;
    Cursor cursor;
    public DatabaseOperator(Context context, String database, int version) {
        helper = new BookInformationDatabaseOpenHelper(context, database, null, version);
        db = helper.getWritableDatabase();
    }

    public int getInt(String table, String field, String field_PrimaryKey, String value_PrimaryKey) {
        int r = -1;
        String query = "select " +field+ " from " +table+ " where " +field_PrimaryKey+ "=?";
        cursor = db.rawQuery(query, new String[] {value_PrimaryKey});
        if (cursor.moveToFirst()) {
            r = cursor.getInt(cursor.getColumnIndex(field));
        }
        return r;
    }

    public synchronized String getString(String table, String field, String field_PrimaryKey, String value_PrimaryKey) {
        String r = null;
        String query = "select " +field+ " from " +table+ " where " +field_PrimaryKey+ "=?";
        cursor = db.rawQuery(query, new String[] {value_PrimaryKey});
        if (cursor.moveToFirst()) {
            r = cursor.getString(cursor.getColumnIndex(field));
        }
        return r;
    }

    public synchronized boolean insertData(String table, ContentValues values) {
        try {
            db.insert(table, null, values);
            return true;
        }catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public synchronized boolean insertFile(File txt) {
        try {
            ContentValues values = new ContentValues();
            values.put("path", txt.getPath());
            values.put("author", "");
            values.put("title", txt.getName());
            values.put("category", "");
            values.put("image", "");
            values.put("readPointer", 0);
            values.put("codingFormat", 1);
            values.put("totality", 0);
            db.insert(TABLE_BOOKS, null, values);
            values.clear();
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public synchronized boolean updateData(String table, ContentValues values, String field_PrimaryKey, String value_PrimaryKey) {
        try {
            db.update(table, values, field_PrimaryKey + "=?", new String[]{value_PrimaryKey});
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean deleteRecord(String table, String field_PrimaryKey, String value_PrimaryKey) {
        try {
            db.delete(table, field_PrimaryKey+"=?", new String[] {value_PrimaryKey});
            return true;
        } catch (SQLException sql) {
            Log.v("注意","SQLException");
            sql.printStackTrace();
            return false;
        }
    }

    public synchronized boolean setTxtDetailList(List<TxtDetail> list) {
        try {
            String query = "select * from Books";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst() && list != null) {
                list.clear();
                for (int i = 0; i < cursor.getCount(); i++) {
                    TxtDetail detail = new TxtDetail();
                    detail.setPath(cursor.getString(cursor.getColumnIndex("path")));
                    detail.setName(cursor.getString(cursor.getColumnIndex("title")));
                    detail.setHasReadPointer(cursor.getInt(cursor.getColumnIndex("readPointer")));
                    detail.setCodingFormat(cursor.getInt(cursor.getColumnIndex("codingFormat")));
                    detail.setTotality(cursor.getInt(cursor.getColumnIndex("totality")));
                    list.add(detail);
                    cursor.moveToNext();
                }
            }
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public Integer findBookIdByPath(String path) {
        String queryId = "select id from Books where path=?";
        cursor = db.rawQuery(queryId, new String[]{ path });
        cursor.moveToFirst();
        Integer id = cursor.getInt(cursor.getColumnIndex("id"));
        return id;
    }

    public boolean addBookmark(String path,String text, long position) {
        Integer id = findBookIdByPath(path);
        if (id != null) {
            ContentValues values = new ContentValues();
            values.put("bookId", id);
            values.put("readPointer", position);
            values.put("text", text);
            db.insert(TABLE_BOOKMARKS, null, values);
            return true;
        }
        return false;
    }

    public boolean getBookmarks(String path, List<Map<String, String>> bookmarkList) {
        Integer id = findBookIdByPath(path);
        if (id != null) {
            bookmarkList.clear();
            String query = "select * from Bookmarks where bookId=?";
            cursor = db.rawQuery(query, new String[]{id.toString()});
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                File file = new File(path);
                Integer pos = cursor.getInt(cursor.getColumnIndex("readPointer"));
                Float fpos = Float.valueOf(pos);
                //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
                NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                percentageFormat.setMaximumFractionDigits(2);
                String percentage = percentageFormat.format(fpos/file.length());
                String text = cursor.getString(cursor.getColumnIndex("text"));
                Map<String, String> map = new HashMap<>();
                map.put("position", pos.toString());
                map.put("percentage", percentage);
                map.put("text", text);
                bookmarkList.add(map);
                cursor.moveToNext();
            }
            return true;
        }
        return false;
    }

    public void close() {
        try {
            if (helper != null)
                helper.close();
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
