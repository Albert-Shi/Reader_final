package com.shishuheng.reader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shishuheng.reader.R;

import java.util.ArrayList;

public class ItemMenuAdapter extends BaseAdapter {
    private ArrayList lists;
    private Context context;

    public ItemMenuAdapter(Context context, ArrayList lists) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return lists != null?lists.size():0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String titleText = (String)lists.get(position);
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, null);
        TextView title = (TextView) view.findViewById(R.id.item_menu_text);
        title.setText(titleText);
        return view;
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}