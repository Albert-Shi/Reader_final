package com.shishuheng.reader.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.adapter.RecyclerViewAdapter;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.BookInformationDatabaseOpenHelper;
import com.shishuheng.reader.process.DatabaseOperator;
import com.shishuheng.reader.ui.activities.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.shishuheng.reader.process.Utilities.reloadHomeListView;

public class HomeFragment extends Fragment {
    MainActivity rootActivity = null;
    boolean clickSelectAll = false;
    HashMap<Integer, Boolean> selectedHashMap;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_home, container, false);
        rootActivity = (MainActivity) getActivity();
        rootActivity.currentFragment = this;

        final ArrayList<TxtDetail> txts = new ArrayList<>();
        selectedHashMap = new HashMap<>();
        rootActivity.allTxts = txts;
        //书籍信息写入数据库 此处参考 https://www.jianshu.com/p/0d8fa55d603b
        DatabaseOperator operator = new DatabaseOperator(getActivity(), DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
        ContentValues values = new ContentValues();
        //创建设置信息
        values.clear();
        values.put("textSize", 3);
        operator.insertData(DatabaseOperator.TABLE_SETTINGS, values);
        //从数据库读取书籍信息
        operator.setTxtDetailList(txts);
        //关闭数据库
        operator.close();

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), txts, selectedHashMap);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.display_listView);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        refreshLayout.setColorSchemeColors(Color.argb(255, 214, 69, 69));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DatabaseOperator dbo = new DatabaseOperator(getActivity(), DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
                txts.clear();
                dbo.setTxtDetailList(txts);
                dbo.close();
                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), "数据已更新", Toast.LENGTH_SHORT).show();
            }
        });

        final Button editModeButton = rootActivity.getAddButtonView().findViewById(R.id.edit_addButton);
        final LinearLayout box_toolbar = getActivity().findViewById(R.id.box_toolbar);
        final Button selectedAll = getActivity().findViewById(R.id.selectAll_toolbar);
        Button delete = getActivity().findViewById(R.id.delete_toolbar);
        editModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!adapter.isSelectMode()) {
                    adapter.setSelectMode();
                    editModeButton.setText("退出编辑");
                    box_toolbar.setVisibility(View.VISIBLE);
                } else {
                    adapter.cancelSelectMode();
                    editModeButton.setText("编辑模式");
                    adapter.setCancelSelected();
                    box_toolbar.setVisibility(View.GONE);
                    clickSelectAll = false;
                    selectedAll.setText("全选");
                    rootActivity.getPopupWindow().dismiss();
                }
                adapter.notifyDataSetChanged();
            }
        });
        selectedAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickSelectAll) {
                    clickSelectAll = false;
                    adapter.setCancelSelected();
                    selectedAll.setText("全选");
                } else {
                    clickSelectAll = true;
                    adapter.setSelectedAll();
                    selectedAll.setText("取消");
                }
                adapter.notifyDataSetChanged();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LinearLayout layout = new LinearLayout(getActivity());
                final CheckBox checkBox = new CheckBox(getActivity());
                View spacing = new View(getActivity());
                TextView textView = new TextView(getActivity());
                textView.setText("彻底删除文件");
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.CENTER);
                layout.addView(checkBox);
                layout.addView(textView);
                builder.setTitle("是否删除选定的"+selectedHashMap.size()+"个文件?");
                builder.setView(layout);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DatabaseOperator dbo = new DatabaseOperator(getActivity(), DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
                                    for (int i = 0; i < txts.size(); i++) {
                                        if (selectedHashMap.get(i) != null && selectedHashMap.get(i) == true) {
                                            File file = new File(txts.get(i).getPath());
                                            dbo.deleteRecord(DatabaseOperator.TABLE_BOOKS, "path", txts.get(i).getPath());
//                                            adapter.removeItem(i);
                                            if (checkBox.isChecked() && file.exists())
                                                file.delete();
                                        }
                                    }
                                    txts.clear();
                                    dbo.setTxtDetailList(txts);
                                    dbo.close();
                                    selectedHashMap.clear();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(rootActivity, "删除完成", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (SQLException e) {
                                    Log.v("注意", "SQLException");
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    }
                });
                builder.create().show();
            }
        });

        return view;
    }
}