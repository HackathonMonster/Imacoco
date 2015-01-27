package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.controller.provider.BluetoothServerThread;
import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;
import com.zeroone_creative.basicapplication.view.fragment.PostStatusFragment;
import com.zeroone_creative.basicapplication.view.fragment.ToyotaGetCarInfoFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements PostStatusFragment.GetUpdateDataListener, ToyotaGetCarInfoFragment.OnUpdateDataListener {

    private Bitmap mLastPicture;
    private VehicleInfo mLastCarInfo;
    private BluetoothAdapter mBtAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //BluetoothAdapter取得
        BluetoothAdapter Bt = BluetoothAdapter.getDefaultAdapter();
        if(!Bt.equals(null)){
            //Bluetooth対応端末の場合の処理
            Log.d("", "Bluetoothがサポートされてます。");
        }else{
            //Bluetooth非対応端末の場合の処理
            Log.d("", "Bluetoothがサポートれていません。");
            finish();
        }
    }

    @AfterViews
    public void onAfterViews(){
        {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            ToyotaGetCarInfoFragment toyotaGetCarInfoFragment = new ToyotaGetCarInfoFragment();
            transaction.add(toyotaGetCarInfoFragment, "toyota_get_carinfo");
            transaction.commit();
        }
        {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            PostStatusFragment postStatusFragment = new PostStatusFragment();
            transaction.add(postStatusFragment, "post_status");
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //サーバースレッド起動、クライアントのからの要求待ちを開始
        BluetoothServerThread BtServerThread = new BluetoothServerThread(this, mBtAdapter);
        BtServerThread.start();
    }

    @Override
    public void onUpdateCarInfo(VehicleInfo carInfo) {
        this.mLastCarInfo = carInfo;
    }
    @Override
    public Bitmap getDrawableData() {
        return mLastPicture;
    }

    @Override
    public VehicleInfo getVehicleInfo() {
        return mLastCarInfo;
    }

}
