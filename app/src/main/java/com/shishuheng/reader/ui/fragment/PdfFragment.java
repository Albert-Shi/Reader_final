package com.shishuheng.reader.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.shishuheng.reader.R;
import com.shishuheng.reader.datastructure.ScreenSize;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.process.Utilities;
import com.shishuheng.reader.ui.activities.FullscreenActivity;

import java.io.File;
import java.text.NumberFormat;

public class PdfFragment extends Fragment {
    private TxtDetail detail;
    private PDFView.Configurator pdfConfigurator;
    private ScreenSize screenSize;
    private FullscreenActivity parentActivity;

    private SeekBar seekBar;
    private TextView battery;
    private TextView title;
    private TextView progress;

    public PdfFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (this.detail != null) {
            View view = inflater.inflate(R.layout.fragment_pdf, container, false);
            screenSize = Utilities.getScreenSize(getActivity());
            parentActivity = (FullscreenActivity) getActivity();
            battery = view.findViewById(R.id.battery_masking);
            title = view.findViewById(R.id.title_masking);
            progress = view.findViewById(R.id.progress_masking);

            final PDFView pdfView = view.findViewById(R.id.pdfView);
            final FrameLayout masking = view.findViewById(R.id.masking);
            pdfConfigurator = pdfView.fromFile(new File(this.detail.getPath()));
            pdfConfigurator.onPageChange(new OnPageChangeListener() {
                @Override
                public void onPageChanged(int page, int pageCount) {
                    seekBar.setMax(pageCount);
                    seekBar.setProgress(page);
                    setBottomInformation();
                }
            });
            pdfConfigurator.defaultPage((int)detail.getHasReadPointer())
                .enableSwipe(true)
                .swipeHorizontal(true)
                .load();
            //菜单设置
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
                    if (Math.abs(interval_X) > sensitivity && Math.abs(interval_X) > Math.abs(interval_Y*1.5) && velocityX != 0 && Math.abs(interval_X) > Math.abs(interval_Y)/2) {
                        if (interval_X < 0) {
                            pdfConfigurator.defaultPage(pdfView.getCurrentPage()+1).load();
                            detail.setHasReadPointer((int)detail.getHasReadPointer()+1);
                        } else if (interval_X > 0) {
                            pdfConfigurator.defaultPage(pdfView.getCurrentPage()-1).load();
                            detail.setHasReadPointer((int)detail.getHasReadPointer()-1);
                        }
                    }
//                Log.v("手势：","X:"+interval_X+" Y:"+interval_Y);
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    int position = (int)detail.getHasReadPointer();
                    if (e.getRawX() >= screenSize.getWidth()/4 && e.getRawX() <= (screenSize.getWidth()/4*3) && e.getRawY() >= screenSize.getHeight()/4 && e.getRawY() <= (screenSize.getHeight()/4*3)) {
                        ((FullscreenActivity) parentActivity).toggle();
                    } else if (e.getRawX() > (screenSize.getWidth()/2)) {
                        pdfConfigurator.defaultPage(position+1).load();
                        detail.setHasReadPointer(position+1);
                        seekBar.setProgress(position+1);
                    } else if (e.getRawX() < (screenSize.getWidth()/2)) {
                        pdfConfigurator.defaultPage(position-1).load();
                        detail.setHasReadPointer(position-1);
                        seekBar.setProgress(position-1);
                    }
                    return super.onSingleTapConfirmed(e);
                }
            };
            final GestureDetector gestureDetector = new GestureDetector(getActivity(), sogl);

            masking.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
//                Log.v("动作：","X:"+event.getX()+" Y:"+event.getY());
                    return gestureDetector.onTouchEvent(event);
                }
            });

            //设置SeekBar和百分比
            final TextView seekBarPercentage = getActivity().findViewById(R.id.seekBarPercentage);
            seekBar = getActivity().findViewById(R.id.seekBar);
            int count = pdfView.getPageCount();
            seekBar.setMax(count);
            seekBar.setProgress((int) detail.getHasReadPointer());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
                    seekBarPercentage.setText(getProgressPercentage());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    pdfConfigurator.defaultPage(progress).load();

                    //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
                    seekBarPercentage.setText(getProgressPercentage());

                    //设置底栏信息
                    setBottomInformation();

                }
            });
            //格式化两位小数 参考 http://blog.csdn.net/chivalrousli/article/details/51122113
            seekBarPercentage.setText(getProgressPercentage());

            //设置夜间模式
            final CheckBox checkBox = getActivity().findViewById(R.id.checkbox_nightShift);
            final FrameLayout layout = view.findViewById(R.id.container_fragment);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        masking.setBackgroundColor(Color.argb(0xa0, 0x00, 0x00, 0x00));
                        checkBox.setText("关闭");
                    } else {
                        masking.setBackgroundColor(Color.argb(0x00, 0x00, 0x00, 0x00));
                        checkBox.setText("开启");
                    }
                }
            });

            //底栏信息设置
            setBottomInformation();

            return view;
        } else
            return null;
    }

    public void setFile(TxtDetail detail) {
        this.detail = detail;
    }

    private String getProgressPercentage() {
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMaximumFractionDigits(2);
        String percentage = percentageFormat.format((float)seekBar.getProgress()/seekBar.getMax());
        return percentage;
    }

    private void setBottomInformation() {
        battery.setText(parentActivity.getBatteryPercent());
        title.setText(detail.getName());
        progress.setText(getProgressPercentage());
    }
}
