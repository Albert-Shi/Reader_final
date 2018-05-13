//此处参考了 https://www.jianshu.com/p/4fc6164e4709
//以及 https://www.cnblogs.com/bugly/p/6264751.html
//RecyclerView动画参考 http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0807/8348.html

package com.shishuheng.reader.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.Utilities;
import com.shishuheng.reader.ui.activities.MainActivity;
import com.shishuheng.reader.ui.activities.FullscreenActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shishuheng on 2018/1/14.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<TxtDetail> mData;
    private MainActivity mainActivity;
    private RecyclerViewAdapter recyclerViewAdapter = this;
//    public boolean showCheckBox = false;
    //记录已选择的项 记录其position和是(true)否(false)已经选择
    private HashMap<Integer, Boolean> selectedHashMap;
    //是否选择全部 -1:取消所有选择 0:默认 1:全选
    private int selectedAll = 0;
    //进入选择模式（编辑模式）
    private boolean selectMode = false;

    public RecyclerViewAdapter(Activity activity, ArrayList<TxtDetail> list, HashMap<Integer, Boolean> selectedHashMap) {
        mainActivity = (MainActivity) activity;
        mData = list;
        this.selectedHashMap =selectedHashMap;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.checkBox.setTag(position);
//        mainActivity.currentTxt = mData.get(position);
//        Log.v("当前selectAll为", ""+selectedAll);
//        Log.v("当前position= "+position, "selectedHashMap.get("+position+")= "+selectedHashMap.get(position));

        holder.name.setText(mData.get(position).getName());
        //设置作者
        //设置图片
        //此处读取Bitmap需要拷贝使用副本 因为不能直接修改资源图片 具体参看 http://blog.csdn.net/j_bing/article/details/45936929
///        Bitmap bitmap = BitmapFactory.decodeResource(mainActivity.getResources(), R.drawable.book_open).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap bitmap = BitmapFactory.decodeResource(mainActivity.getResources(), R.drawable.book_cover).copy(Bitmap.Config.ARGB_8888, true);
        //*
        Paint p = new Paint();
        //创建Typeface字体对象
        Typeface typeface = Typeface.createFromAsset(mainActivity.getAssets(), "fonts/华文行楷.ttf");

        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.RED);
///        p.setTextSize(50);
        p.setTextSize(30);
        p.setAntiAlias(true);
//        p.setColor(Color.rgb(236, 106, 92));
        p.setColor(Color.rgb(41, 36, 33));
        p.setTypeface(typeface);
        String text = mData.get(position).getName();
        int Y = 70;
        if (text.length() > 6)
            text = text.substring(0, 6) + "……";
        for (int i = 0; i < 7; i++) {
            if (i >= text.length())
                break;
            else {
///                canvas.drawText(text.substring(i,i+1), 150, Y, p);
                canvas.drawText(text.substring(i,i+1), 60, Y, p);
                Y += 35;
            }
        }
        //*/

        holder.cover.setImageBitmap(bitmap);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                notifyDataSetChanged();
                if (selectMode) {
                    if (holder.checkBox.isChecked())
                        holder.checkBox.setChecked(false);
                    else
                        holder.checkBox.setChecked(true);
                } else {
                    if (!holder.occupy) {
                        String extension = mData.get(position).getName().substring(mData.get(position).getName().lastIndexOf('.'));
                        if (extension != null && (extension.equals(".txt") || extension.equals(".doc") || extension.equals(".docx") || extension.equals(".xls")
                                || extension.equals(".xlsx") || extension.equals(".ppt") || extension.equals(".pptx") || equals(".pdf"))) {
                            //若安装了wps则用wps打开office文档
                            if ((extension.equals(".xls") || extension.equals(".xlsx") || extension.equals(".ppt") || extension.equals(".pptx")) || mainActivity.getInstalledWps() == false) {
                                Toast.makeText(mainActivity, "未安装WPS，无法打开文件", Toast.LENGTH_SHORT).show();
                            } else if ((extension.equals(".doc") || extension.equals(".docx") || extension.equals(".xls") || extension.equals(".xlsx") || extension.equals(".ppt") || extension.equals(".pptx")) && mainActivity.getInstalledWps()) {
                                Utilities.useWpsOpenFile(mData.get(position).getPath(), mainActivity);
                            } else {
                                Intent intent = new Intent(mainActivity, FullscreenActivity.class);
                                intent.putExtra("currentTextDetail", mData.get(position));
                                intent.putExtra("TextSize", mData.get(position));
                                mainActivity.currentTxt = mData.get(position);
                                mainActivity.startActivity(intent);
                            }
                        } else {
                            Toast.makeText(mainActivity, "无法打开该格式文件", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                notifyDataSetChanged();
                if (!selectMode) {
                    holder.occupy = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    View list = LayoutInflater.from(mainActivity).inflate(R.layout.menu_display, null);
                    TextView title = (TextView) list.findViewById(R.id.menu_title_text);
                    title.setText(mData.get(position).getName());
                    builder.setView(list);
                    AlertDialog dialog = builder.create();
                    Utilities.reloadMenuItem(mainActivity, list, dialog, recyclerViewAdapter, position);
                    mainActivity.currentTxt = mData.get(position);
                    dialog.show();
                    holder.occupy = false;
                }
                return false;
            }
        });
        if (selectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = (int)holder.checkBox.getTag();
                if (isChecked) {
                    selectedHashMap.put(pos, true);
//                    Log.v("已经将selectAll手动切换为", ""+selectedAll);
//                    Log.v("当前position= "+pos, "selectedHashMap.get("+pos+")= "+selectedHashMap.get(pos));

                } else {
                    selectedAll = 0;
//                    Log.v("已经将selectAll手动切换为", ""+selectedAll);
                    selectedHashMap.remove(pos);
//                    Log.v("当前position= "+pos, "selectedHashMap.get("+pos+")= "+selectedHashMap.get(pos));
                }
            }
        });

        if (selectedAll == 1) {
            holder.checkBox.setChecked(true);
            selectedHashMap.put(position, true);
        } else if (selectedAll == -1) {
            holder.checkBox.setChecked(false);
        }

        if (selectedHashMap.get(holder.checkBox.getTag()) == null || selectedHashMap.get(holder.checkBox.getTag()) == null) {
            holder.checkBox.setChecked(false);
        } else
            holder.checkBox.setChecked(selectedHashMap.get(holder.checkBox.getTag()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final CheckBox checkBox;
        public final TextView name;
        public final TextView author;
        public final ImageView cover;
        //记录是否长按 长按时禁止点击（防止长按的时候又进行了点击操作）
        public boolean occupy = false;
        public ViewHolder(View view) {
            super(view);
            name = (TextView)view.findViewById(R.id.name_display);
            author = (TextView)view.findViewById(R.id.author_display);
            cover = (ImageView)view.findViewById(R.id.cover_display_list);
            checkBox = view.findViewById(R.id.checkbox_display_list);
        }
    }

    public void removeItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        //每次移除一个Item后需要执行notifyDataSsetChanged()方法 不然RecyclerView会出现Item的position不能准确的一一对应 具体是哪一步出现了bug 暂时还没分析出来 暂且通过调用notifyDataSsetChanged()方法来解决问题吧
        //sleep一定时间后运行notifyDataSsetChanged()方法 不直接运行而是等待一会是为了让notifyItemRemoved()方法移除动画放完
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(800);
                    //返回主线程调用notifyDataSsetChanged()方法
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void setSelectedAll() {
        selectedAll = 1;
        for (int i = 0; i < mData.size(); i++) {
            selectedHashMap.put(i, true);
        }
    }

    public void setCancelSelected() {
        selectedAll = -1;
        selectedHashMap.clear();
    }

    public void setSelectMode() {
        selectMode = true;
    }

    public void cancelSelectMode() {
        selectMode = false;
    }

    public boolean isSelectMode() {
        return selectMode;
    }
}
