package com.shishuheng.reader.ui.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.adapter.BookmarkAdapter;
import com.shishuheng.reader.datastructure.ActivitySerializable;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.BookInformationDatabaseOpenHelper;
import com.shishuheng.reader.process.DatabaseOperator;
import com.shishuheng.reader.process.Utilities;
import com.shishuheng.reader.ui.fragment.OfficeFragment;
import com.shishuheng.reader.ui.fragment.PdfFragment;
import com.shishuheng.reader.ui.fragment.TextFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    final List<Map<String, String>> bookmarkList = new ArrayList<>();
    private final Handler mHideHandler = new Handler();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private MainActivity mainActivity;
    private TxtDetail currentTxt;
    private int position;
    private int screenTextSize;
    private ActivitySerializable activitySerializable;
    private TextFragment textFragment;
    private OfficeFragment officeFragment;
    private int textSize_Settings = 3;
    private int nightModeCode;
    private ListView bookmarklistView;
    private BookmarkAdapter bookmarkAdapter;
    //电池信息接收器
    private BroadcastReceiver batteryReceiver;
    //电量百分比
    private String batteryPercent = "正在获取数据";
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private View controller;
    private View bookmark;
    private ArrayList<View> viewList;
    private DatabaseOperator operator;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        Transition animation = TransitionInflater.from(getApplicationContext()).inflateTransition(R.transition.slide);
        getWindow().setEnterTransition(animation);
        //全屏显示Activity（不显示状态栏）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.argb(0, 0, 0, 0));

        //屏幕常亮 参考 http://blog.csdn.net/a57565587/article/details/51669520
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_fullscreen);

//        //添加侧栏导航
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.fullscreen_drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//        NavigationView navigationView = (NavigationView) findViewById(R.id.fullscreen_nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        mVisible = false;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        //创建菜单与书签的View Pager
        setViewPager(new ArrayList<String>());

        currentTxt = (TxtDetail) getIntent().getSerializableExtra("currentTextDetail");
//        screenTextSize = getIntent().getIntExtra("TextSize", 512);


        // Set up the user interaction to manually show or hide the system UI.
        //用不到 注释掉
        /*
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        */

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //读取数据库中TXT书籍及应用相关设置
        operator = new DatabaseOperator(this, DatabaseOperator.DATABASE_NAME, DatabaseOperator.DATABASE_VERSION);
        currentTxt.setHasReadPointer(operator.getInt(DatabaseOperator.TABLE_BOOKS, "readPointer", "path", currentTxt.getPath()));
        currentTxt.setCodingFormat(operator.getInt(DatabaseOperator.TABLE_BOOKS, "codingFormat", "path", currentTxt.getPath()));
        currentTxt.setTotality(operator.getInt(DatabaseOperator.TABLE_BOOKS, "totality", "path", currentTxt.getPath()));
        textSize_Settings = operator.getInt(DatabaseOperator.TABLE_SETTINGS, "textSize", "id", 1 + "");
        nightModeCode = operator.getInt(DatabaseOperator.TABLE_SETTINGS, "nightMode", "id", 1 + "");

        //创建电池信息接收器 此处参考 http://www.jb51.net/article/72799.htm
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int current = intent.getExtras().getInt("level");//获得当前电量
                int total = intent.getExtras().getInt("scale");//获得总电量
                int percent = current * 100 / total;
                batteryPercent = percent + "%";
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        //设置文本显示
        if (currentTxt != null) {
            String extension = currentTxt.getName().substring(currentTxt.getName().lastIndexOf('.'));
            if (extension.equalsIgnoreCase(".txt")) {
                setTextContent();
            } else if (extension.equalsIgnoreCase(".pdf")) {
                setPDFView(currentTxt);
            } else if (extension.equalsIgnoreCase(".doc") || extension.equalsIgnoreCase(".docx")) {
                setOfficeView(currentTxt.getPath());
//                useWpsOpenFile(currentTxt.getPath());
            }
        }

        //设置ActionBar自定义背景
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(currentTxt.getName());
        actionBar.setBackgroundDrawable(getDrawable(R.drawable.toolbar_bg_fullscreen));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    public void toggle() {
        if (mVisible) {
            hide();
            //          fragment.getMainDisplay().setMovementMethod(null);
        } else {
            show();
//            fragment.getMainDisplay().setMovementMethod(ScrollingMovementMethod.getInstance());
            mControlsView.bringToFront();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TxtDetail getTxtDetail() {
        return currentTxt;
    }

    public void setTxtDetail(TxtDetail txtDetail) {
        this.currentTxt = txtDetail;
    }

    public void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.fullscreen_content, fragment).commit();
        setBookmarkListView();
    }

    public void setTextContent() {
        textFragment = new TextFragment();
        startFragment(textFragment);
    }

    /*
    private void setText() {
        TextView mainDisplay = (TextView) findViewById(R.id.fullscreen_content);
        byte[] text = Utilities.readRandomFile(this, this.getTxtDetail().getPath(), 0, Utilities.getFinalTextSize(this, 2, 16));
        String utf8text = null;
        try {
            utf8text = new String(text, "GBK");
        } catch (Exception e) {
            utf8text = "不支持utf8的编码文件";
            e.printStackTrace();
        }
        mainDisplay.setText(utf8text);
        mainDisplay.setTextSize(40);
        mainDisplay.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    */

    public TextFragment getTextFragment() {
        return textFragment;
    }

    @Override
    public void onBackPressed() {
        if (mVisible) {
            toggle();
        } else {
            //保存书籍信息到数据库
            if (currentTxt.getHasReadPointer() < 0)
                currentTxt.setHasReadPointer(0);
            BookInformationDatabaseOpenHelper helper = new BookInformationDatabaseOpenHelper(this, Utilities.DATABASE_NAME, null, Utilities.DATABASE_VERSION);
            SQLiteDatabase db = helper.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("readPointer", currentTxt.getHasReadPointer());
            values.put("codingFormat", currentTxt.getCodingFormat());
            db.update("Books", values, "path=?", new String[]{currentTxt.getPath()});

            ContentValues settingValue = new ContentValues();
            settingValue.put("nightMode", nightModeCode);
            settingValue.put("textSize", textSize_Settings);
            db.update(DatabaseOperator.TABLE_SETTINGS, settingValue, "id=?", new String[]{1 + ""});

            db.close();

            //注销电池信息接收器
            unregisterReceiver(batteryReceiver);

            super.onBackPressed();
        }
    }

    public int getTextSize_Settings() {
        return textSize_Settings;
    }

    public void setTextSize_Settings(int textSize_Settings) {
        this.textSize_Settings = textSize_Settings;
    }

    public String getBatteryPercent() {
        return batteryPercent;
    }

    public void setOfficeView(String docPath) {
        officeFragment = new OfficeFragment();
        officeFragment.setFile(docPath);
        startFragment(officeFragment);
    }

    public void setPDFView(TxtDetail detail) {
        PdfFragment pdfFragment = new PdfFragment();
        pdfFragment.setFile(detail);
        startFragment(pdfFragment);
    }

    public void setViewPager(List<String> data) {
        viewPager = (ViewPager) findViewById(R.id.viewpager_fullscreen);
        LayoutInflater inflater = getLayoutInflater();
        controller = inflater.inflate(R.layout.controller, null);

        bookmark = inflater.inflate(R.layout.bookmarks, null);
        bookmarklistView = bookmark.findViewById(R.id.listView_bookmark);

        setBookMark(bookmark, data);

        viewList = new ArrayList<>();
        viewList.add(controller);
        viewList.add(bookmark);

        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(viewList.get(position));
            }
        };

        viewPager.setAdapter(pagerAdapter);

        Button addBookmarkButton = controller.findViewById(R.id.addBookmark_button);
        addBookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textFragment.getCurrenBook().bookFullScreen.get(0) + textFragment.getCurrenBook().bookFullScreen.get(1) + textFragment.getCurrenBook().bookFullScreen.get(2);
                text = text.replace("\r", " ").replace("\n", " ") + "……";
                boolean flag = operator.addBookmark(currentTxt.getPath(), text, textFragment.getCurrenBook().getReadPointer());
                if (flag == true) {
                    bookmarkList.clear();
                    operator.getBookmarks(currentTxt.getPath(), bookmarkList);
                    bookmarkAdapter.notifyDataSetChanged();
                    showToast("添加成功");
                }
            }
        });

        //查找功能
        final EditText findEditText = controller.findViewById(R.id.findText_editText);
        findEditText.clearFocus();
        ImageView findButton = controller.findViewById(R.id.findText_button);
        findButton.setImageResource(R.drawable.search_button);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "";
                String p = findEditText.getText().toString();
                if (!(p.equals("") && p.equals("\n"))) {
                    for (int i = 0; i < textFragment.getCurrenBook().bookFullScreen.size(); i++) {
                        text += textFragment.getCurrenBook().bookFullScreen.get(i);
                    }
                    Integer index = text.indexOf(p);
                    if (index == -1) {
                        showToast("未找到内容");
                    } else {
                        int count = 0;
                        for (int i = 0; i < textFragment.getCurrenBook().bookFullScreen.size(); i++) {
                            int lineCount = textFragment.getCurrenBook().bookFullScreen.get(i).length();
                            if (count + lineCount >= index) {
                                showToast("出现在第 " + (i + 1) + " 行，第 " + (index - count) + " 列");
                                index = text.indexOf(findEditText.getText().toString(), (index + p.length()));
                                if (index == -1) {
                                    break;
                                }
                            } else {
                                count += lineCount;
                            }
                        }
                    }
                }
            }
        });
    }

    public void setBookmarkListView() {
        if (operator.getBookmarks(currentTxt.getPath(), bookmarkList)) {
            bookmarkAdapter = new BookmarkAdapter(this, bookmarkList);
            bookmarklistView.setAdapter(bookmarkAdapter);
            bookmarklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Integer pos = Integer.valueOf(bookmarkList.get(position).get("position"));
                    currentTxt.setHasReadPointer(pos);
                    textFragment.text.clear();
                    textFragment.text = textFragment.getCurrenBook().readByte(pos);
                    textFragment.mainDisplay.setText(textFragment.text);
                    currentTxt.setHasReadPointer(pos);
                    textFragment.getCurrenBook().setTotality(pos);

                    if (textFragment.getCurrenBook().bookFullScreen.size() >= 0)
                        currentTxt.setFirstLineLastExit(textFragment.getCurrenBook().bookFullScreen.get(0) + textFragment.getCurrenBook().bookFullScreen.get(1) + textFragment.getCurrenBook().bookFullScreen.get(3));
                }
            });

            bookmarklistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    operator.deleteRecord(operator.TABLE_BOOKMARKS, "readPointer", bookmarkList.get(position).get("position"));
                    bookmarkList.remove(position);
                    bookmarkAdapter.notifyDataSetChanged();
                    showToast("已删除书签");
                    return true;
                }
            });
        }
    }

    public void setBookMark(View bookmark, List<String> data) {
        ListView listView = bookmark.findViewById(R.id.listView_bookmark);
        ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.item_bookmark, data);
        listView.setAdapter(adapter);
    }

    public View getController() {
        return controller;
    }

    public void setController(View controller) {
        this.controller = controller;
    }

    public View getBookmark() {
        return bookmark;
    }

    public void setBookmark(View bookmark) {
        this.bookmark = bookmark;
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public int getNightModeCode() {
        return nightModeCode;
    }

    public void setNightModeCode(int nightModeCode) {
        this.nightModeCode = nightModeCode;
    }
}
