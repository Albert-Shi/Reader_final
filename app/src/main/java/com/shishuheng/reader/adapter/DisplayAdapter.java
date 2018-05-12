package com.shishuheng.reader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.TxtDetail;

import java.util.List;

/**
 * Created by shishuheng on 2018/1/2.
 */
//listView所必须的Adapter实现 用来适配TxtDetail类的数据
//此处参考 http://blog.csdn.net/acm_th/article/details/51130198
public class DisplayAdapter extends BaseAdapter {
    protected List<TxtDetail> lists;
    private Context mc;

    public DisplayAdapter(Context context, List<TxtDetail> lists) {
        this.lists = lists;
        mc = context;
    }

    @Override
    public int getCount() {
        return lists == null?0 : lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TxtDetail txtDetail = (TxtDetail) getItem(position);
        View view = LayoutInflater.from(mc).inflate(R.layout.listview_display, null);
        ImageView cover = (ImageView) view.findViewById(R.id.cover_display_list);
        //设置封面 保留
        cover.setImageResource(R.mipmap.ic_launcher);
        TextView name = (TextView) view.findViewById(R.id.name_display);
        //显示作者 保留
        TextView autor = (TextView) view.findViewById(R.id.author_display);
        name.setText(txtDetail.getName());
        /*
        TextPaint tp = new TextPaint();
        tp.setAntiAlias(true);
        tp.setColor(Color.argb(255, 144, 202, 249));
        tp.setTextSize(28);
        */

        return view;
    }
}