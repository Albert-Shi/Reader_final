package com.shishuheng.reader.ui.filepicker;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.shishuheng.reader.R;
import com.shishuheng.reader.process.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shishuheng on 2018/1/23.
 */

public class FileItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<File> list;
    public HashMap<Integer, Boolean> selectedRecord;
    public FileItemAdapter(Context context, ArrayList<File> list, HashMap<Integer, Boolean> selectedRecord) {
        this.context = context;
        this.list = list;
        this.selectedRecord = selectedRecord;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //获取布局 以及 相关的控件
        File file = list.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            View itemView = LayoutInflater.from(context).inflate(R.layout.fileitemadapter_layout, null);
            convertView = itemView;
            ImageView imageView = itemView.findViewById(R.id.file_icon);
            CheckedTextView filename = itemView.findViewById(R.id.file_name);
            TextView filesize = itemView.findViewById(R.id.file_size);
            holder.checkedTextView = filename;
            holder.imageView = imageView;
            holder.textView = filesize;
            convertView.setTag(holder);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            holder = (ViewHolder) convertView.getTag();
            holder.reset();
        }
        String name = file.getName();   //文件名
        String extension_name = "";     //扩展名
        //判断是否有扩展名 如果有则获取
        if (name.lastIndexOf('.') > -1)
            extension_name = name.substring(name.lastIndexOf('.'));
        //根据不同类型的文件以及是否是文件夹 设置不同的文件图标 (此处可根据具体需要进行补充)
        if (file.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.fileicon_folder);
        } else if (extension_name.equalsIgnoreCase(".txt")) {
            holder.imageView.setImageResource(R.drawable.fileicon_txt);
        } else if (extension_name.equalsIgnoreCase(".doc") || extension_name.equalsIgnoreCase(".docx")) {
            holder.imageView.setImageResource(R.drawable.fileicon_word);
        } else if (extension_name.equalsIgnoreCase(".xls") || extension_name.equalsIgnoreCase(".xlsx")) {
            holder.imageView.setImageResource(R.drawable.fileicon_excel);
        } else if (extension_name.equalsIgnoreCase(".ppt") || extension_name.equalsIgnoreCase(".pptx")) {
            holder.imageView.setImageResource(R.drawable.fileicon_ppt);
        } else if (extension_name.equalsIgnoreCase(".pdf")) {
            holder.imageView.setImageResource(R.drawable.fileicon_pdf);
        } else if (extension_name.equalsIgnoreCase(".apk")) {
            holder.imageView.setImageResource(R.drawable.fileicon_apk);
        } else if (extension_name.equalsIgnoreCase(".jpg") || extension_name.equalsIgnoreCase(".jpeg") || extension_name.equalsIgnoreCase(".gif") || extension_name.equalsIgnoreCase(".png") || extension_name.equalsIgnoreCase(".psd") || extension_name.equalsIgnoreCase(".bmp")) {
            holder.imageView.setImageResource(R.drawable.fileicon_image);
        } else if (extension_name.equalsIgnoreCase(".mid") || extension_name.equalsIgnoreCase(".ogg") || extension_name.equalsIgnoreCase(".wma") || extension_name.equalsIgnoreCase(".ape") || extension_name.equalsIgnoreCase(".flac") || extension_name.equalsIgnoreCase(".aac") || extension_name.equalsIgnoreCase(".mp3")) {
            holder.imageView.setImageResource(R.drawable.fileicon_audio);
        } else if (extension_name.equalsIgnoreCase(".mov") || extension_name.equalsIgnoreCase(".avi") || extension_name.equalsIgnoreCase(".swf") || extension_name.equalsIgnoreCase(".flv") || extension_name.equalsIgnoreCase(".3gp") || extension_name.equalsIgnoreCase(".mkv") || extension_name.equalsIgnoreCase(".mp4") || extension_name.equalsIgnoreCase(".rm") || extension_name.equalsIgnoreCase(".rmvb")) {
            holder.imageView.setImageResource(R.drawable.fileicon_video);
        } else {
            holder.imageView.setImageResource(R.drawable.fileicon_other);
        }
        holder.checkedTextView.setText(name);
        if (file.isFile())
            //获取文件大小
            holder.textView.setText(Utilities.getFileSize(file.length()));
        if (selectedRecord.get(position) != null && selectedRecord.get(position) == true) {
            holder.checkedTextView.setChecked(true);
            convertView.setBackgroundColor(Color.argb(0xC8,0xFF, 0x40, 0x81));
        } else {
            holder.checkedTextView.setChecked(false);
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView imageView;
        CheckedTextView checkedTextView;
        TextView textView;
        public void reset() {
            if (checkedTextView != null && textView != null) {
                checkedTextView.setText(null);
                checkedTextView.setChecked(false);
                textView.setText(null);
            }
        }
    }
}
