package com.shishuheng.reader.datastructure;

import com.shishuheng.reader.ui.activities.MainActivity;

import java.io.Serializable;

/**
 * Created by shishuheng on 2018/1/4.
 */

public class ActivitySerializable implements Serializable {
    private MainActivity mainActivity;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
