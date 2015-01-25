package com.zeroone_creative.basicapplication.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.zeroone_creative.basicapplication.controller.provider.NetworkTaskCallback;
import com.zeroone_creative.basicapplication.controller.provider.VolleyHelper;
import com.zeroone_creative.basicapplication.controller.util.ImageUtil;
import com.zeroone_creative.basicapplication.controller.util.MultiPartRequestUtil;
import com.zeroone_creative.basicapplication.controller.util.UriUtil;
import com.zeroone_creative.basicapplication.model.enumerate.NetworkTasks;
import com.zeroone_creative.basicapplication.model.pojo.Drive;
import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;
import com.zeroone_creative.basicapplication.model.system.AppConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PostStatusFragment extends Fragment {

    private GetUpdateDataListener mListener;

    private final String CAR_VID = "ITCJP_VID_001";
    private final int PERIOD_TIME = 60*1000;

    private VehicleInfo mCarInfo;
    private Bitmap mPictureData;
    private Drive mDrive;

    private Timer mTimer;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(AppConfig.PREF_NAME, Context.MODE_PRIVATE);
        String driveStr = sharedPreferences.getString(AppConfig.PREF_KEY_DRIVE, "");
        mDrive = new Gson().fromJson(driveStr, Drive.class);
    }

    private TimerTask mGetCarInfoTask = new TimerTask() {
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post( new Runnable() {
                public void run() {
                    mCarInfo = mListener.getVehicleInfo();
                    mPictureData = mListener.getDrawableData();
                    Log.d("PostStatusFragment", "Picture" + mPictureData != null ? "Setted" : "Unsetted");
                    Log.d("PostStatusFragment", "CarInfo" + mCarInfo != null ? "Setted" : "Unsetted");

                    //データがなかったら処理キャンセル
                    if(mCarInfo==null || mPictureData==null) return;

                    MultiPartRequestUtil updateStatusRequest = new MultiPartRequestUtil(new NetworkTaskCallback() {
                        @Override
                        public void onSuccessNetworkTask(int taskId, Object object) {

                        }
                        @Override
                        public void onFailedNetworkTask(int taskId, Object object) {

                        }
                    },
                    this.getClass().getSimpleName(),
                    null);
                    HashMap paramsString = new HashMap();
                    LatLng carLatLng =  mCarInfo.data.posN.latlng;

                    paramsString.put("drive_id", Integer.toString(mDrive.id));
                    paramsString.put("drive_image", ImageUtil.encodeImageBase64(mPictureData));
                    paramsString.put("present_gps_lat", Double.toString(carLatLng.latitude));
                    paramsString.put("present_gps_lon", Double.toString(carLatLng.longitude));
                    paramsString.put("message", "");
                    updateStatusRequest.onRequest(VolleyHelper.getRequestQueue(getActivity().getApplicationContext()),
                            Request.Priority.HIGH,
                            UriUtil.postStatus(),
                            NetworkTasks.PostStatus,
                            paramsString,
                            new HashMap<String, File>());
                }
            });
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof GetUpdateDataListener) {
            mListener = (GetUpdateDataListener) activity;
        }

        if(mTimer == null){
            //タイマーの初期化処理
            mTimer = new Timer(true);
            mTimer.schedule(mGetCarInfoTask, 60*1000, PERIOD_TIME);
            //一分Delayさせて開始
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        //タイマーの停止処理
        mTimer.cancel();
        mTimer = null;
    }

    public interface GetUpdateDataListener {
        public Bitmap getDrawableData();
        public VehicleInfo getVehicleInfo();
    }


}
