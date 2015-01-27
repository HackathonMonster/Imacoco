package com.zeroone_creative.basicapplication.controller.provider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.zeroone_creative.basicapplication.model.pojo.BtSendObject;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by shunhosaka on 2015/01/25.
 */
public class BluetoothClientThread extends Thread {
    //クライアント側の処理
    private final BluetoothSocket clientSocket;
    private final BluetoothDevice mDevice;
    private Context mContext;
    //UUIDの生成
    public static final UUID TECHBOOSTER_BTSAMPLE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static BluetoothAdapter myClientAdapter;
    public BtSendObject mSendObject;

    //コンストラクタ定義
    public BluetoothClientThread(Context context, BtSendObject sendObject, BluetoothDevice device, BluetoothAdapter btAdapter){
        //各種初期化
        mContext = context;
        BluetoothSocket tmpSock = null;
        mDevice = device;
        myClientAdapter = btAdapter;
        mSendObject = sendObject;
        try{
            //自デバイスのBluetoothクライアントソケットの取得
            tmpSock = device.createRfcommSocketToServiceRecord(TECHBOOSTER_BTSAMPLE_UUID);
        }catch(IOException e){
            e.printStackTrace();
        }
        clientSocket = tmpSock;
    }

    public void run(){
        //接続要求を出す前に、検索処理を中断する。
        if(myClientAdapter.isDiscovering()){
            myClientAdapter.cancelDiscovery();
        }
        try{
            //サーバー側に接続要求
            clientSocket.connect();
        }catch(IOException e){
            try {
                clientSocket.close();
            } catch (IOException closeException) {
                e.printStackTrace();
            }
            return;
        }
        //接続完了時の処理
        ReadWriteModel rw = new ReadWriteModel(mContext, clientSocket, mSendObject);
        rw.start();
    }

    public void cancel() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}