package com.shishuheng.reader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shishuheng.reader.R;

import java.util.List;
import java.util.Map;

public class BookmarkAdapter extends BaseAdapter {
    Context context;
    List<Map<String, String>> bookmarkList;
    public BookmarkAdapter(Context context, List<Map<String, String>> bookmarkList) {
        this.context = context;
        this.bookmarkList = bookmarkList;
    }

    @Override
    public int getCount() {
        return bookmarkList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookmarkList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String percentage = bookmarkList.get(position).get("percentage");
        String text = bookmarkList.get(position).get("text");
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark, null);
        TextView pv = view.findViewById(R.id.item_bookmark_percentage);
        TextView tv = view.findViewById(R.id.item_bookmark_text);
        pv.setText(percentage);
        tv.setText(text);
        return view;
    }
}
