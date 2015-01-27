package com.zeroone_creative.basicapplication.controller.provider;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.zeroone_creative.basicapplication.model.pojo.BtSendObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by shunhosaka on 2015/01/25.
 */
public class ReadWriteModel extends Thread {
    //ソケットに対するI/O処理

    public static InputStream in;
    public static OutputStream out;
    private BtSendObject mBtSendObject;
    private Context mContext;

    //コンストラクタの定義
    public ReadWriteModel(Context context, BluetoothSocket socket, BtSendObject btSendObject){
        mBtSendObject = btSendObject;
        mContext = context;

        try {
            //接続済みソケットからI/Oストリームをそれぞれ取得
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void run() {
        byte[] buf = new byte[1024];
        int tmpBuf = 0;
        //書き込み
        write(mBtSendObject);
        while(true){
            try {
                tmpBuf = in.read(buf);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(tmpBuf!=0){
                BtSendObject readObject = read();

            }
        }
    }

    public void write(BtSendObject object){
        ObjectOutputStream objWriter;
        try {
            objWriter = new ObjectOutputStream(out);
            objWriter.writeObject(object);
            objWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BtSendObject read() {
        ObjectInputStream objInputter = null;
        try {
            objInputter = new ObjectInputStream(in);
            Object object = objInputter.readObject();
            if (object != null && object instanceof BtSendObject) {
                byte[] buffer = ((BtSendObject) object).bytes;
                return (BtSendObject) object;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}