package com.shishuheng.reader.ui.filepicker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.DatabaseOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by shishuheng on 2018/1/23.
 */

public class FilePicker extends AlertDialog.Builder {
    View view;              //此FilePick的主界面
    Context context;
    File directory = null;  //当前的目录文件
    ArrayList<File> list;   //当前目录下的所有文件
    ListView filesList;     //文件列表ListView
    TextView returnToUp;    //返回上一层文本按钮
    AlertDialog dialog;     //外部调用create()方法生成的AlertDialog实例
    DatabaseOperator operator;      //数据库操作
    boolean multiChoice = false;    //是否为多选状态
    ArrayList<File> selected;     //已选定的文件
    HashMap<Integer, Boolean> selectedRecord;   //记录选择状态
    public FilePicker(Context mContext, String path) {
        super(mContext);
        this.context = mContext;
        directory = new File(path);
        operator = new DatabaseOperator(context, DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
        list = new ArrayList<>();
        view = LayoutInflater.from(context).inflate(R.layout.dialog_filepicker, null);
        returnToUp = view.findViewById(R.id.back_FilePicker);
        filesList = view.findViewById(R.id.listView_FilePicker);
        setView(view);
        fillList();
        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selected != null && selected.size() > 0) {
                    DatabaseOperator operator = new DatabaseOperator(context, DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
                    for (int i = 0; i < selected.size(); i++) {
                        operator.insertFile(selected.get(i));
                    }
                    operator.close();
                }
            }
        });
        setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    void fillList() {
        //判断当前的文件是否为目录 如果是目录 获得此目录下的所有文件 并存入ArrayList
        if (directory.isDirectory() && directory != null) {
            list.clear();
            //初次创建的时候将当前目录显示为标题
            setTitle(directory.getName());
            //当目录改变时 重新设置标题
            if (dialog != null)
                dialog.setTitle(directory.getName());
            //获取所有此目录下的文件
            File[] files = directory.listFiles();
            //存入list
            if (files != null && files.length > 0) {
                for (File file : files) {
                    list.add(file);
                }
            }
            //按字典序排序
            Collections.sort(list);

            //创建ListView的适配器
            selectedRecord = new HashMap<>();
            final FileItemAdapter adapter = new FileItemAdapter(context, list, selectedRecord);
            filesList.setAdapter(adapter);

            filesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            //设置ListView中的条目点击动作
            //*
            filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File file = list.get(position);
                    if (multiChoice && selectedRecord != null) {
                        CheckedTextView textView = view.findViewById(R.id.file_name);
                        if (textView.isChecked()) {
                            textView.setChecked(false);
                            selected.remove(selected.indexOf(file));
                            selectedRecord.put(position, false);
                            view.setBackgroundColor(Color.WHITE);
                        } else {
                            selected.add(file);
                            textView.setChecked(true);
                            selectedRecord.put(position, true);
                            view.setBackgroundColor(Color.argb(0xC8, 0xFF, 0x40, 0x81));
                        }
                    } else {
                        //如果选择的是目录 则将directory设置为当前选择 并递归调用fillList方法
                        if (file.isDirectory()) {
                            directory = file;
                            fillList();
                        } else {
                            //如果选择的是文件 则将此文件的绝对路径输出 (此处可改为需要的动作)
//                        Log.v("文件选定", file.getAbsolutePath());
                            TxtDetail txt = new TxtDetail();
                            String name = file.getName();
                            if (name.lastIndexOf('.') != -1)
                                name = name.substring(0, name.lastIndexOf('.'));
                            txt.setName(name);
                            txt.setHasReadPointer(0);
                            txt.setCodingFormat(1);
                            txt.setTotality(0);
                            txt.setPath(file.getAbsolutePath());
//                        selects.add(txt);
                            operator.insertFile(file);
                            operator.close();
                            Toast.makeText(context, "文件添加完成，请下拉刷新书籍列表", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                }
            });
            //*/

            //长按
            filesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!multiChoice) {
                        selectedRecord.clear();
                        selected = new ArrayList<>();
                        File file = list.get(position);
                        CheckedTextView textView = view.findViewById(R.id.file_name);
                        selected.add(file);
                        textView.setChecked(true);
                        multiChoice = true;
                        selectedRecord.put(position, true);
                        view.setBackgroundColor(Color.argb(0xC8, 0xFF, 0x40, 0x81));
                        return true;
                    } else
                        return false;
                }
            });

            //返回上一层按钮
            returnToUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //将directory设置为directory的parent 并递归调用fillList方法
                    if (!directory.getAbsolutePath().equals("\\")) {
                        File parent = directory.getParentFile();
                        if (parent != null && parent.exists()) {
                            directory = directory.getParentFile();
                            fillList();
                        }
                    }
                }
            });
        }
    }

    @Override
    public AlertDialog create() {
        //当创建dialog的时候 获取此AlertDialog实例
        dialog = super.create();
        return dialog;
    }
}
