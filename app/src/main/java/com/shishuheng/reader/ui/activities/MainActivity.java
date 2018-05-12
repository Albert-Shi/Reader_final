package com.shishuheng.reader.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.Utilities;
import com.shishuheng.reader.ui.filepicker.FilePicker;
import com.shishuheng.reader.ui.fragment.HomeFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public TxtDetail currentTxt = null;
    public ArrayList<TxtDetail> allTxts = null;
    public Fragment currentFragment = null;
    private HomeFragment homeFragment = null;
    private MainActivity activity = this;

    //悬浮按钮相关控件
    private PopupWindow popupWindow;
    private Button addButton_popupWindow;
    private Button editButton_popupWindow;
    private View addButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏显示Activity（不显示状态栏）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setStatusBarColor(Color.argb(0,0,0,0));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //toolBar设置背景图片
        toolbar.setBackground(getDrawable(R.drawable.toolbar_bg));

        toolbar.setTitle("主页");
        toolbar.setSubtitle("书籍列表");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                /*
                */

                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAsDropDown(fab, -50, -500);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //myself
        //check permission
        //如果手机系统SDK等于高于23 且没有获取相关权限（SD卡文件读取） 则弹出授权对话框
        if (Build.VERSION.SDK_INT >= 23) {
            final Activity ma = this; //用于传递Activity
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //maybe should put something
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("警告");
                    builder.setMessage("为了阅读器能够读取文件，请授予相关权限！");
                    builder.setPositiveButton("授予权限", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    });
                    builder.setNegativeButton("放弃退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(1);
                        }
                    });
                    builder.create().show();
                } else {
                    ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            /*
            //MIUI权限检查 参考http://www.miui.com/forum.php?mod=viewthread&tid=4498742
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, android.os.Process.myUid(), getPackageName());
//            if (checkOp == AppOpsManager.MODE_IGNORED) {
                // 权限被拒绝了 .
                ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(ma, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            }
            */
        }

        //设置悬浮按钮 PopupWindow参考 https://www.jianshu.com/p/825d1cc9fa79
        addButtonView = LayoutInflater.from(activity).inflate(R.layout.addbutton_pop, null, false);
        popupWindow = new PopupWindow(activity);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(addButtonView);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffffff));
        addButton_popupWindow = addButtonView.findViewById(R.id.add_addButton);
        addButton_popupWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePicker filePicker = new FilePicker(activity, Environment.getExternalStorageDirectory().getAbsolutePath());
                filePicker.create().show();
                popupWindow.dismiss();
            }
        });
        editButton_popupWindow = activity.findViewById(R.id.edit_addButton);


        //展示和移除Splash
        final FrameLayout splash = findViewById(R.id.splash_main);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        splash.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fm = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        fm.beginTransaction().replace(R.id.content_main, homeFragment).commit();

        if (splash != null) {
            splash.setVisibility(View.GONE);
        }


//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentFragment != homeFragment && currentFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, homeFragment).commit();
            currentFragment = homeFragment;
        } else if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addFromFolder) {
            Utilities.getDirectoryBookFiles(this);
            Toast.makeText(this, "添加完成，请手动下拉刷新书籍列表", Toast.LENGTH_SHORT).show();
        }
        /*
        else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Button getEditButton_popupWindow() {
        return editButton_popupWindow;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public View getAddButtonView() {
        return addButtonView;
    }
}
