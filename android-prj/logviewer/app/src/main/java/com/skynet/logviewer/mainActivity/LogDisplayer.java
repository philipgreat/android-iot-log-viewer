package com.skynet.logviewer.mainActivity;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.skynet.logviewer.MainActivity;
import com.skynet.logviewer.R;

public class LogDisplayer extends BaseMainActivityComponent{
    protected TextView txtViewLogContent;
    protected TextView txtLabel;
    protected StringBuffer sbContent;
    protected CheckBox checkBoxAutoScroll;
    protected Button btnClearAll;
    protected long filteredCnt = 0;

    private static final int MAX_CONTENT_LENGTH = 1*1024*1024;
    public void init(MainActivity mainActivity) {
        super.init(mainActivity);
        txtViewLogContent = (TextView) mainActivity.findViewById(R.id.txtViewLogContent);
        txtViewLogContent.setMovementMethod(new ScrollingMovementMethod());
        txtViewLogContent.setText("READY TO GO\n");
        txtLabel = (TextView) mainActivity.findViewById(R.id.label_filtered_num);
        sbContent = new StringBuffer();

        btnClearAll = (Button) mainActivity.findViewById(R.id.btn_clear_content);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllLogContent();
            }
        });

        checkBoxAutoScroll = (CheckBox) mainActivity.findViewById(R.id.chckbox_auto_scroll);
    }

    private void clearAllLogContent() {
        sbContent = new StringBuffer();
        txtViewLogContent.setText("");
        filteredCnt = 0;
        txtLabel.setText("0");
    }


    public void appendContent(String message) {
        if (!message.endsWith("\n")){
            message = message +"\n";
        }
        sbContent.append(message);
        boolean renewContent = false;
        if (sbContent.length() > MAX_CONTENT_LENGTH){
            int charsNeedRemove = sbContent.length() - MAX_CONTENT_LENGTH;
            int canRemovedChars = findCharsCanRemoved(sbContent, charsNeedRemove);
            if (canRemovedChars > 0){
                sbContent.delete(0, canRemovedChars);
                renewContent = true;
//                txtViewLogContent.setText(sbContent.toString());
            }else{
//                txtViewLogContent.append(message);
            }
        }else{
//            txtViewLogContent.append(message);
        }
//        System.out.println(sbContent.toString());

        final boolean newContent = renewContent;
        final String newMessage = message;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newContent) {
                    txtViewLogContent.setText(sbContent.toString());
                }else{
                    txtViewLogContent.append(newMessage);
                }
                if (!checkBoxAutoScroll.isChecked()){
                    return;
                }
                int offset=txtViewLogContent.getLineCount()*txtViewLogContent.getLineHeight();
                if(offset>txtViewLogContent.getHeight()){
                    Log.i("SCROLL","offset " + offset +", content " + txtViewLogContent.getLineCount()+"x"+txtViewLogContent.getLineHeight()+", height " + txtViewLogContent.getHeight());
                    Log.i("SCROLL","scroll to " + (offset-txtViewLogContent.getHeight()));
                    txtViewLogContent.scrollTo(0,offset-txtViewLogContent.getHeight());
                }
            }
        });

    }

    private int findCharsCanRemoved(StringBuffer sbContent, int charsNeedRemove) {
        if (sbContent.length() < 1){
            return 0;
        }
        if (sbContent.length() < charsNeedRemove){
            return sbContent.length();
        }

        int pos = -1;
        for(;;){
            pos = sbContent.indexOf("\n", pos+1);
            if (pos < 0){
                return 0;
            }
            if (pos >= charsNeedRemove){
                return pos;
            }
        }
    }

    public void incFiltered() {
        filteredCnt ++;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtLabel.setText(String.valueOf(filteredCnt));
            }
        });

    }
}