package com.shishuheng.reader.ui.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.ScreenSize;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.Book;
import com.shishuheng.reader.process.Utilities;
import com.shishuheng.reader.ui.activities.FullscreenActivity;
import com.shishuheng.reader.ui.coustomize.ReadView;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

//用于显示具体文字的页面
public class TextFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public ReadView mainDisplay;
    public ArrayList<String> text = null;
    //进度条百分比
    String percentage;
    //获取页面设置参数
    CardView card;
    TxtDetail txt;
    int lineCharacterNumber;
    int lineTotalNumber;
    private Activity rootActivity = null;
    private LinearLayout textMenu;
    private GestureDetector gestureDetector;
    private int displayLineNumber = 0;
    private ScreenSize screenSize;

    /*
    float textSize;
    float lineHeight;
    float letterSpacing;
    float paddingTop;
    float paddingBottom;
    float paddingLeft;
    float paddingRight;
    */
    private int textSizePixel = 48;
    private Book currenBook;
    private SeekBar seekBar = null;
    //显示进度条百分百
    private TextView seekBarPercentage = null;
    private FrameLayout layout;
    private CheckBox checkBox;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TextFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TextFragment newInstance(String param1, String param2) {
        TextFragment fragment = new TextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LinearLayout getTextMenu() {
        return textMenu;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        rootActivity = getActivity();
//        rootActivity.currentFragment =this;

        //获取页面设置参数
        card = (CardView) view.findViewById(R.id.card);
//        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        screenSize = Utilities.getScreenSize(getActivity());
        mainDisplay = new ReadView(getContext());
        mainDisplay.setContentWidth(screenSize.getWidth() - 80);
        mainDisplay.setContentHeight(screenSize.getHeight() - 80);
        mainDisplay.setPadding(20, 8, 8, 8);
        card.addView(mainDisplay);

        txt = ((FullscreenActivity) rootActivity).getTxtDetail();

        final Book book = new Book(getActivity(), new File(txt.getPath()), lineTotalNumber, lineCharacterNumber, txt.getHasReadPointer(), txt.getTotality(), screenSize.getHeight(), screenSize.getWidth());
//        book.setLineTotalNumber(lineTotalNumber);
//        book.setLineCharacterNumber(lineCharacterNumber);
        txt.sychronizationToBook(book);
        currenBook = book;
        //设置字体大小
        changeTextSize();
        //计算页面可显示的字数
        reComputationCharacterNumber(book);
        //将文本显示
        refreshMainDisplay(book);

        GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float interval_X = e2.getX() - e1.getX();
                float interval_Y = e2.getY() - e1.getY();
                float sensitivity = 100;
                float minVelocityX = 20;
                float minVelocityY = 20;
                if (Math.abs(interval_X) > sensitivity && Math.abs(interval_X) > Math.abs(interval_Y * 1.5) && velocityX != 0 && Math.abs(interval_X) > Math.abs(interval_Y) / 2) {
                    if (interval_X < 0) {
                        nextPage(book);
                    } else if (interval_X > 0) {
                        lastPage(book);
                    }

                }
//                Log.v("手势：","X:"+interval_X+" Y:"+interval_Y);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (e.getRawX() >= screenSize.getWidth() / 4 && e.getRawX() <= (screenSize.getWidth() / 4 * 3) && e.getRawY() >= screenSize.getHeight() / 4 && e.getRawY() <= (screenSize.getHeight() / 4 * 3)) {
                    ((FullscreenActivity) rootActivity).toggle();
                } else if (e.getRawX() > (screenSize.getWidth() / 2)) {
                    nextPage(book);
                } else if (e.getRawX() < (screenSize.getWidth() / 2)) {
                    lastPage(book);
                }
                return super.onSingleTapConfirmed(e);
            }
        };
        gestureDetector = new GestureDetector(getActivity(), sogl);

        mainDisplay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.v("动作：","X:"+event.getX()+" Y:"+event.getY());
                return gestureDetector.onTouchEvent(event);
            }
        });


        //设置SeekBar和百分比
        seekBarPercentage = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.seekBarPercentage);
        seekBar = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.seekBar);
        seekBar.setMax((int) book.getTotality());
        seekBar.setProgress((int) txt.getHasReadPointer());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
                NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                percentageFormat.setMaximumFractionDigits(2);
                percentage = percentageFormat.format((float) seekBar.getProgress() / seekBar.getMax());
                seekBarPercentage.setText(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                text.clear();
                text = book.readByte(progress);
                mainDisplay.setText(text);
                txt.setHasReadPointer(progress);
                book.setTotality(progress);

                if (book.bookFullScreen.size() >= 0)
                    txt.setFirstLineLastExit(book.bookFullScreen.get(0) + book.bookFullScreen.get(1) + book.bookFullScreen.get(3));

                //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
                NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                percentageFormat.setMaximumFractionDigits(2);
                percentage = percentageFormat.format((float) seekBar.getProgress() / seekBar.getMax());
                seekBarPercentage.setText(percentage);

                //设置底栏信息
                String b = ((FullscreenActivity) getActivity()).getBatteryPercent();
                mainDisplay.setBottomInfomations(b, txt.getName(), percentage);

            }
        });
        //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMaximumFractionDigits(2);
        percentage = percentageFormat.format((float) seekBar.getProgress() / seekBar.getMax());
        seekBarPercentage.setText(percentage);
        //设置底栏信息
        String b = ((FullscreenActivity) getActivity()).getBatteryPercent();
        mainDisplay.setBottomInfomations(b, txt.getName(), percentage);

        //设置编码RadioButton
        RadioGroup encodeGroup = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.encode_RadioGroup);
        RadioButton encodeRadioButton;
        switch (book.getCodingFormat()) {
            case 2:
                encodeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.encodeFormat_GB2312);
                break;
            case 3:
                encodeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.encodeFormat_GB18030);
                break;
            case 4:
                encodeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.encodeFormat_UTF8);
                break;
            default:
                encodeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.encodeFormat_GBK);
                break;
        }
        encodeRadioButton.setChecked(true);
        encodeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                switch (checkedId) {
                    case R.id.encodeFormat_GBK:
                        txt.setCodingFormat(1);
                        book.setCodingFormat(1);
                        refreshMainDisplay(book);
                        Toast.makeText(rootActivity, "已切换为GBK编码", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.encodeFormat_GB18030:
                        txt.setCodingFormat(3);
                        book.setCodingFormat(3);
                        refreshMainDisplay(book);
                        Toast.makeText(rootActivity, "已切换为GB18030编码", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.encodeFormat_GB2312:
                        txt.setCodingFormat(2);
                        book.setCodingFormat(2);
                        refreshMainDisplay(book);
                        Toast.makeText(rootActivity, "已切换为GB2312编码", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.encodeFormat_UTF8:
                        txt.setCodingFormat(4);
                        book.setCodingFormat(4);
                        refreshMainDisplay(book);
                        Toast.makeText(rootActivity, "已切换为UTF-8编码", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        //设置字体RadioButton
        RadioGroup textSizeGroup = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_RadioGroup);
        RadioButton textSizeRadioButton;
        switch (((FullscreenActivity) getActivity()).getTextSize_Settings()) {
            case 1:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_minimum);
                break;
            case 2:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_small);
                break;
            case 3:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_medium);
                break;
            case 4:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_large);
                break;
            case 5:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_maximum);
                break;
            default:
                textSizeRadioButton = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.textSize_medium);
                break;
        }
        textSizeRadioButton.setChecked(true);
        textSizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.textSize_minimum:
                        Utilities.updateData(getActivity(), Utilities.TABLE_SETTINGS, 1, null, "textSize", 1);
                        ((FullscreenActivity) getActivity()).setTextSize_Settings(1);
                        break;
                    case R.id.textSize_small:
                        Utilities.updateData(getActivity(), Utilities.TABLE_SETTINGS, 1, null, "textSize", 2);
                        ((FullscreenActivity) getActivity()).setTextSize_Settings(2);
                        break;
                    case R.id.textSize_medium:
                        Utilities.updateData(getActivity(), Utilities.TABLE_SETTINGS, 1, null, "textSize", 3);
                        ((FullscreenActivity) getActivity()).setTextSize_Settings(3);
                        break;
                    case R.id.textSize_large:
                        Utilities.updateData(getActivity(), Utilities.TABLE_SETTINGS, 1, null, "textSize", 4);
                        ((FullscreenActivity) getActivity()).setTextSize_Settings(4);
                        break;
                    case R.id.textSize_maximum:
                        Utilities.updateData(getActivity(), Utilities.TABLE_SETTINGS, 1, null, "textSize", 5);
                        ((FullscreenActivity) getActivity()).setTextSize_Settings(5);
                        break;
                }
                changeTextSize();
                reComputationCharacterNumber(book);
                refreshMainDisplay(book);
            }
        });
        //设置夜间模式
        checkBox = ((FullscreenActivity) rootActivity).getController().findViewById(R.id.checkbox_nightShift);
        layout = view.findViewById(R.id.container_fragment);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mainDisplay.setTextColor(Color.rgb(0x56, 0x56, 0x56));
                    layout.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
                    card.setCardBackgroundColor(Color.rgb(0x0d, 0x0e, 0x0d));
                    //card.setRadius(20);
                    mainDisplay.invalidate();
                    checkBox.setText("关闭");
                    ((FullscreenActivity) rootActivity).setNightModeCode(1);
                } else {
                    mainDisplay.setTextColor(Color.rgb(0x00, 0x00, 0x00));
                    layout.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
                    card.setCardBackgroundColor(Color.rgb(0xf0, 0xeb, 0xd5));
                    //card.setRadius(20);
                    mainDisplay.invalidate();
                    checkBox.setText("开启");
                    ((FullscreenActivity) rootActivity).setNightModeCode(0);
                }
            }
        });

        //夜间模式恢复
        switchNightMode();

        return view;
    }

    long nextPage(Book book) {
        book.setReadPointer(txt.getHasReadPointer());
        book.nextPage();
        txt.setHasReadPointer(book.getReadPointer());
        text = book.nextPage();
        book.setReadPointer(txt.getHasReadPointer());
        mainDisplay.setText(text);
        seekBar.setProgress((int) book.getReadPointer());

        //设置底栏信息
        String b = ((FullscreenActivity) getActivity()).getBatteryPercent();
        mainDisplay.setBottomInfomations(b, txt.getName(), percentage);

        if (book.bookFullScreen.size() >= 0)
            txt.setFirstLineLastExit(book.bookFullScreen.get(0) + book.bookFullScreen.get(1) + book.bookFullScreen.get(3));

        return 0;
    }

    long lastPage(Book book) {
        if (book.getReadPointer() > 0) {
            long tp = txt.getHasReadPointer();
            long rp = book.lastPageCount(tp);
            txt.setHasReadPointer(rp);
//            txt.setHasReadPointer(book.lastPageCount(txt.getHasReadPointer()));
            text = book.readByte(txt.getHasReadPointer());
            book.setReadPointer(txt.getHasReadPointer());
            mainDisplay.setText(text);
//            Log.v("tp", tp+"");
//            Log.v("txt.readPointer", txt.getHasReadPointer()+"");
//            Log.v("rp", rp+"\n\n");
        } else {
            txt.setHasReadPointer(0);
            book.setReadPointer(0);
        }
        seekBar.setProgress((int) book.getReadPointer());
        //设置底栏信息
        String b = ((FullscreenActivity) getActivity()).getBatteryPercent();
        mainDisplay.setBottomInfomations(b, txt.getName(), percentage);

        if (book.bookFullScreen.size() >= 0)
            txt.setFirstLineLastExit(book.bookFullScreen.get(0) + book.bookFullScreen.get(1) + book.bookFullScreen.get(3));

        return 0;
    }

    public void changeTextSize() {
        switch (((FullscreenActivity) getActivity()).getTextSize_Settings()) {
            case 1:
                textSizePixel = 16;
                break;
            case 2:
                textSizePixel = 32;
                break;
            case 3:
                textSizePixel = 48;
                break;
            case 4:
                textSizePixel = 60;
                break;
            case 5:
                textSizePixel = 72;
                break;
        }
        mainDisplay.setTextSize(textSizePixel);
    }

    public void reComputationCharacterNumber(Book book) {
        lineCharacterNumber = (int) ((screenSize.getWidth() - 80) / (mainDisplay.getTextSize()));
        lineTotalNumber = (int) ((screenSize.getHeight() - 80) / (mainDisplay.getmLineSpacing() + mainDisplay.getTextSize()));
        book.setLineCharacterNumber(lineCharacterNumber);
        book.setLineTotalNumber(lineTotalNumber);
    }

    public void refreshMainDisplay(Book book) {
        text = book.readByte(txt.getHasReadPointer());
        book.setReadPointer(txt.getHasReadPointer());
        mainDisplay.setText(text);
    }

    public void switchNightMode() {
        int r = ((FullscreenActivity) getActivity()).getNightModeCode();
        if (r == 0) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }

    public Book getCurrenBook() {
        return currenBook;
    }

    public ReadView getMainDisplay() {
        return mainDisplay;
    }
}