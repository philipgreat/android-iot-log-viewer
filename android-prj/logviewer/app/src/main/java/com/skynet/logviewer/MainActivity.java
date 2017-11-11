package com.skynet.logviewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.skynet.logviewer.mainActivity.LogDisplayer;
import com.skynet.logviewer.mainActivity.LogListeningTask;

public class MainActivity extends AppCompatActivity {
    private int showFullScreenFlag = 0
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE;

    public static MainActivity me = null;

    private String LOG_TAG = "LOG_VIEWER";

    private LogListeningTask logListeningTask;
    private LogDisplayer logDisplayer;
    private TextView txtFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        initMembers();

        logListeningTask.startToRun();
    }

    private void initMembers() {
        logListeningTask = new LogListeningTask();
        logListeningTask.init(this);

        logDisplayer = new LogDisplayer();
        logDisplayer.init(this);


    }


    public void appendContent(final String message) {
        logDisplayer.appendContent(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logListeningTask.stop();
    }

    public void incFiltered() {
        logDisplayer.incFiltered();

    }
}
