package com.skynet.logviewer.mainActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skynet.logviewer.MainActivity;
import com.skynet.logviewer.R;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class LogListeningTask extends BaseMainActivityComponent{
    private String filterString="";
    private boolean isRunning;
    private Thread udpListeningThread;
    private WifiManager.MulticastLock mcLock;
    private MulticastSocket socket;
    private TextView txtFilterContent;
    private Button btnConfigFilter;

    public void init(MainActivity mainActivity) {
        super.init(mainActivity);
        btnConfigFilter = (Button) mainActivity.findViewById(R.id.btn_set_filter);
        btnConfigFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterSettingDialog();
            }
        });
        txtFilterContent = (TextView) mainActivity.findViewById(R.id.label_filter_content);
//        txtFilter = (EditText) mainActivity.findViewById(R.id.txtFilter);
//        txtFilter.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                filterString = s.toString();
//                Log.d("CHANGE-FILTER",filterString);
//            }
//        });

        isRunning = false;
    }

    private void showFilterSettingDialog() {
        final EditText editText = new EditText(mainActivity);
        editText.setText(filterString);
        //editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mainActivity);
        inputDialog.setTitle("过滤内容").setView(editText);
        inputDialog.setNegativeButton("取消",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filterString = editText.getText().toString();
                txtFilterContent.setText(filterString);
            }
        });
        inputDialog.show();
    }

    private void initUdpListener() throws Exception{

        WifiManager wifi = (WifiManager) mainActivity.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            throw new Exception("无法获得Wifi");
        }
        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
            throw new IllegalStateException("Please enable wifi manually!");
        }

        if(!wifi.isWifiEnabled()){
            throw new IllegalStateException("Please enable wifi manually!");
        }

        mcLock = wifi.createMulticastLock("mylock");
        mcLock.acquire();//new InetSocketAddress(InetAddress.getByName(group), port);

        //InetSocketAddress groupInetSocketAddress = new InetSocketAddress(InetAddress.getByName("228.5.6.7"), 6789);

        InetAddress group = InetAddress.getByName("224.0.0.7");
        socket = new MulticastSocket(6789);
        socket.setSoTimeout(2000);
        socket.joinGroup(group);
    }

    public void startToRun() {
        udpListeningThread = new Thread(){
            public void run(){
                try {
                    initUdpListener();
                } catch (Exception e) {
                    mainActivity.appendContent("初始化失败："+e.getMessage());
                    mainActivity.appendContent("正确配置手机wifi等设置后，重新打开本应用。");
                    return;
                }
                while(isRunning){
                    try {
                        recieveLogMessage();
                    } catch (SocketTimeoutException e){
                        // ignore timeout
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        isRunning = true;
        udpListeningThread.start();
    }

    private void recieveLogMessage() throws Exception{
        byte[] buf = new byte[1500];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        socket.receive(recv);

        byte[] recvedBytes = new byte[recv.getLength()];
        System.arraycopy(buf, 0, recvedBytes, 0, recv.getLength());
        byte[] decodeBytes = codec(recvedBytes);
        String message = new String(decodeBytes);
        Log.i("REV_LOG", message);
        if (message.indexOf(filterString) < 0){
            mainActivity.incFiltered();
            return;
        }
        mainActivity.appendContent(message);
    }

    private byte[] codec(byte[] str){
        if (str == null || str.length == 0){
            return str;
        }

        byte[] result = new byte[str.length];
        for(int i=0;i<result.length;i++){
            result[i] = (byte)(255 - str[i]);
        }
        return result;
    }

    public void stop() {
        udpListeningThread.interrupt();
        try {
            udpListeningThread.join();
        } catch (InterruptedException e) {

        }
        socket.close();
        mcLock.release();
    }
}