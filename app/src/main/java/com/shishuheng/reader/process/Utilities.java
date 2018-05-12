package com.shishuheng.reader.process;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.ScreenSize;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.ui.activities.FullscreenActivity;
import com.shishuheng.reader.adapter.ItemMenuAdapter;
import com.shishuheng.reader.ui.activities.MainActivity;
import com.shishuheng.reader.adapter.RecyclerViewAdapter;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shishuheng on 2018/1/2.
 */
//工具类 用来各种数据处理
public class Utilities {
    //数据库名称
    public static String DATABASE_NAME = "BookReader.db";
    public static String TABLE_BOOKS = "Books";
    public static String TABLE_SETTINGS = "Settings";
    public static int DATABASE_VERSION = 1;
    //RecyclerView的Adapter
    RecyclerViewAdapter recyclerViewAdapter = null;
    //获取 /sdcard/EBooks/ 目录下的所有txt文件
    public static void getDirectoryBookFiles(Context context) {
        File path = new File(Environment.getExternalStorageDirectory()+"/EBooks");
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                DatabaseOperator operator = new DatabaseOperator(context, DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
                for (File file : files) {
                    String name = file.getName();
                    if (name.lastIndexOf('.') > -1) {
                        String extension = name.substring(name.lastIndexOf('.'));
                        if (extension.equalsIgnoreCase(".txt") || extension.equalsIgnoreCase(".doc") || extension.equalsIgnoreCase(".docx") || extension.equalsIgnoreCase(".pdf")) {
                            operator.insertFile(file);
                        }
                    }
                }
            }
        }
    }

    public static void reloadHomeListView(final MainActivity activity, View view) {
//        final ArrayList<TxtDetail> txts = Utilities.getDirectoryTXTFiles();


        /*
        DisplayAdapter adapter = new DisplayAdapter(activity, txts);
        ListView display = (ListView) view.findViewById(R.id.display_listView);
        display.setAdapter(adapter);
        display.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                View list = LayoutInflater.from(activity).inflate(R.layout.menu_display, null);
                TextView title = (TextView) list.findViewById(R.id.menu_title_text);
                title.setText(txts.get(position).getName());
                reloadMenuItem(activity, list);

                builder.setView(list);
                builder.create().show();

                return false;
            }
        });

        display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.currentTxt = txts.get(position);
//                ActivitySerializable as = new ActivitySerializable();
//                as.setMainActivity(activity);
                Intent intent = new Intent(activity, FullscreenActivity.class);
                intent.putExtra("currentTextDetail", activity.currentTxt);
                intent.putExtra("TextSize", activity.TextSize);
//                intent.putExtra()
//                intent.putExtra("RootActivity", as);
                activity.startActivity(intent);
            }
        });
        */

    }

    public static void reloadMenuItem(Activity activity, View view, AlertDialog dialog, final RecyclerViewAdapter recyclerViewAdapter, final int itemPosition) {
        final AlertDialog alertDialog = dialog;
        final MainActivity mainActivity = (MainActivity)activity;
        ArrayList<String> items = new ArrayList<>();
        items.add("仅从列表移除");
        items.add("彻底删除文件");
        items.add("详情");
        final ItemMenuAdapter adapter = new ItemMenuAdapter(mainActivity, items);
//        View view = LayoutInflater.from(context).inflate(R.layout.menu_display, null);
        final ListView menu = (ListView) view.findViewById(R.id.menu_listVew);
        menu.setAdapter(adapter);
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file =  new File(mainActivity.currentTxt.getPath());
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                builder.setTitle("详细信息");
                String info = "详细位置:"+file.getAbsolutePath() +"\n\n"+ "文件大小:"+getFileSize(file.length());
                builder.setMessage(info);
                switch (position) {
                    case 0: Utilities.deleteFileInDatabase(mainActivity, file.getAbsolutePath()); recyclerViewAdapter.removeItem(itemPosition); Log.v("外部: ", itemPosition+""); alertDialog.dismiss();break;
                    case 1: Utilities.deleteFile(mainActivity, file); recyclerViewAdapter.removeItem(itemPosition); alertDialog.dismiss();break;
                    case 2: builder.create().show(); alertDialog.dismiss();break;
                    default: break;
                }
            }
        });
    }

    //获取手机高度dp 和 宽度dp
    public static ScreenSize getScreenSize(Activity activity) {
        ScreenSize size = new ScreenSize();
        WindowManager wm = activity.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int height = (int) (dm.heightPixels/density);
        int width = (int) (dm.widthPixels/density);
        size.setHeightDp(height);
        size.setWidthDp(width);
        size.setHeight(dm.heightPixels);
        size.setWidth(dm.widthPixels);
        return size;
    }

    //计算一个屏幕的字符数
    public static int getFullScreenCharactorNumbers(ScreenSize size, float letterSize, float lineHeight, float padding, float letterSpacing) {
        int num = (int)(((size.getHeight()-(padding*2))/lineHeight) * ((size.getWidth()-(padding*2))/(letterSize + letterSpacing)));
        return num;
    }

    //随机读文本 参考 https://www.cnblogs.com/zuochengsi-9/p/6485737.html
    public static byte[] readRandomFile(Activity activity, String path, int pointer, int size) {
        byte[] buff = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(path), "r");
            raf.seek(0);
            buff = new byte[size];
            long hasRead = 0;
            for (int i = 0; i < size; i++) {
                buff[i] = raf.readByte();
            }
            ((FullscreenActivity)activity).getTxtDetail().setHasReadPointer(pointer+size);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buff;
    }

    public static void deleteFile(Activity activity, File file) {
        final MainActivity mainActivity = (MainActivity)activity;
        deleteFileInDatabase(activity, file.getAbsolutePath());
        if (file != null && file.exists() && file.isFile()) {
            if (file.delete()) {
                if (!file.exists()) {
                    Toast.makeText(mainActivity, "文件删除成功，请下拉列表刷新", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mainActivity, "文件删除失败，请稍后再次重试", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mainActivity, "文件删除失败，请稍后再次重试", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mainActivity, "文件不存在或者文件是文件夹", Toast.LENGTH_SHORT).show();
    }

    public static void deleteFileInDatabase(Activity activity, String path) {
        try {
            DatabaseOperator operator = new DatabaseOperator(activity, DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
            operator.deleteRecord(DatabaseOperator.TABLE_BOOKS, "path", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateData(Activity activity, String table, int id_setting, String book_path, String field, long value) {
        BookInformationDatabaseOpenHelper helper = new BookInformationDatabaseOpenHelper(activity, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(field, value);
        if (book_path!=null && book_path.length()!=0 && !book_path.equals(""))
            database.update(table, values, "path=?", new String[]{book_path});
        else if (id_setting > -1)
            database.update(table, values, "id=?", new String[] {id_setting+""});
        database.close();
    }

    public static void updateData(Activity activity, String table, int id_setting, String book_path, String field, String value) {
        BookInformationDatabaseOpenHelper helper = new BookInformationDatabaseOpenHelper(activity, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(field, value);
        if (book_path!=null && book_path.length()!=0 && !book_path.equals(""))
            database.update(table, values, "path=?", new String[]{book_path});
        else if (id_setting > -1)
            database.update(table, values, "id=?", new String[] {id_setting+""});
        database.close();
    }



    public static String getFileSize(long size) {
        int i = 1;
        String unit = "B";
        float temp = size;
        while (temp >= 1024) {
            temp /= 1024;
            i++;
        }
        switch (i) {
            case 1: unit = "B";break;
            case 2: unit = "KB"; break;
            case 3: unit = "MB"; break;
            case 4: unit = "GB"; break;
            case 5: unit = "TB"; break;
            default: unit = "文件太大，已经不知道用什么单位了";
        }
        //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
        NumberFormat percentageFormat = NumberFormat.getNumberInstance();
        percentageFormat.setMaximumFractionDigits(2);
        String result = percentageFormat.format(temp) +" "+ unit;
        return result;
    }

    // 判断是否安装wps
    public static boolean isWpsInstalled(Context context) {
        boolean isInstall = false;
        List<PackageInfo> list = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_PERMISSIONS);

        StringBuilder stringBuilder = new StringBuilder();

        for (PackageInfo packageInfo : list) {
            stringBuilder.append("package name:" + packageInfo.packageName
                    + "\n");
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            stringBuilder.append("应用名称:"
                    + applicationInfo.loadLabel(context.getPackageManager()) + "\n");
            if (packageInfo.permissions != null) {

                for (PermissionInfo p : packageInfo.permissions) {
                    stringBuilder.append("权限包括:" + p.name + "\n");
                }
            }
            stringBuilder.append("\n");
            if ("cn.wps.moffice_eng".equals(packageInfo.packageName)) {
                isInstall = true;
            }
        }
        System.out.println(isInstall);
        return isInstall;
    }

    public static boolean useWpsOpenFile(String path, Context context) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("OpenMode", "ReadOnly");
        bundle.putString("ThirdPackage", context.getPackageName());
        bundle.putBoolean("ClearTrace", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setClassName("cn.wps.moffice_eng",
                "cn.wps.moffice.documentmanager.PreStartActivity");
        File file = new File(path);
        if (file == null || !file.exists()) {
            return false;
        }
        Uri uri = FileProvider.getUriForFile(context, "com.shishuheng.reader.fileProvider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setData(uri);
        intent.putExtras(bundle);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}