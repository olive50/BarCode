package com.olivetic.barcode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private TextView tvBarcode, tvCodeId;

    private DatagramSocket ds=null;
    private InetAddress addr;
    private int port;
    byte[] bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBarcode = (TextView)findViewById(R.id.tv_barcode);
        tvCodeId = (TextView)findViewById(R.id.tv_code_id);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendUDP("testcode");

        registerBarcodeScannerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterBarcodeScannerBroadcastReceiver();
    }

    private void registerBarcodeScannerBroadcastReceiver() {
        // 配置条码扫描服务为广播模式，没有自动换行，打开读码提示灯
        Intent intent = new Intent ("ACTION_BAR_SCANCFG");
        intent.putExtra("EXTRA_SCAN_MODE", 3);
        intent.putExtra("EXTRA_SCAN_AUTOENT", 0);
        intent.putExtra("EXTRA_SCAN_NOTY_LED", 1);
        sendBroadcast(intent);
        registerReceiver(barcodeScannerBroadcastReceiver, new IntentFilter("nlscan.action.SCANNER_RESULT"));
    }

    private void unregisterBarcodeScannerBroadcastReceiver() {
        unregisterReceiver(barcodeScannerBroadcastReceiver);
    }

    private BroadcastReceiver barcodeScannerBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String scanResult_1=intent.getStringExtra("SCAN_BARCODE1");
            //final String scanResult_2=intent.getStringExtra("SCAN_BARCODE2");
            final String scanStatus = intent.getStringExtra("SCAN_STATE");
            if (null==scanResult_1 || null==scanStatus
                    || scanResult_1.isEmpty() || scanStatus.isEmpty()) {
                return;
            }
            if ("ok".equals(scanStatus)) {
                sendUDP(scanResult_1);
                tvBarcode.setText(scanResult_1);
                int codeId = intent.getIntExtra("SCAN_BARCODE_TYPE", -1);
                tvCodeId.setText(""+codeId);
            }
        }
    };


    public void  sendUDP( String messageStr) {
        //tvName?.text = ""
        //tvPrice?.text = ""
        //tvTemp?.text = ""
        // Hack Prevent crash (sending should be done using a separate thread)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);  //Just for testing relax the rules...
        try {
            //Open a port to send a UDP package
            DatagramSocket socket = new DatagramSocket();
            //socket.broadcast = true
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("10.10.41.157"),
                    9000);
            socket.send(sendPacket);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
