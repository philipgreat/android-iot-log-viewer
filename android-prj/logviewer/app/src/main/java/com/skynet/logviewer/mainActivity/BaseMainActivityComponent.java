package com.skynet.logviewer.mainActivity;

import com.skynet.logviewer.MainActivity;

public class BaseMainActivityComponent {
    protected MainActivity mainActivity;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void init(MainActivity activity) {
        setMainActivity(activity);
    }
}