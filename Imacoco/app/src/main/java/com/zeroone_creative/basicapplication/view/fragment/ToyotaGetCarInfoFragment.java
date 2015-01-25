package com.zeroone_creative.basicapplication.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;

import com.zeroone_creative.basicapplication.controller.provider.NetworkTaskCallback;
import com.zeroone_creative.basicapplication.controller.util.JSONParseUtil;
import com.zeroone_creative.basicapplication.controller.util.ToyotaApiRequestUtil;
import com.zeroone_creative.basicapplication.controller.util.UriUtil;
import com.zeroone_creative.basicapplication.model.enumerate.NetworkTasks;
import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;
import com.zeroone_creative.basicapplication.model.system.AppConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ToyotaGetCarInfoFragment extends Fragment {
    private OnUpdateDataListener mListener;

    private final String CAR_VID = "ITCJP_VID_001";
    private final int PERIOD_TIME = 30*1000;

    private Timer mTimer;
    private Handler mHandler = new Handler();

    private TimerTask mGetCarInfoTask = new TimerTask() {
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post( new Runnable() {
                public void run() {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("developerkey", AppConfig.DEVELOPERKEY));
                    params.add(new BasicNameValuePair("responseformat", "json"));
                    params.add(new BasicNameValuePair("infoids", "[Posn,Spd,EngN,SteerAg,BrkIndcr,AccrPedlRat,HdLampLtgIndcn,WiprSts,TrsmGearPosn]"));
                    params.add(new BasicNameValuePair("vid", CAR_VID));

                    ToyotaApiRequestUtil toyotaRequest = new ToyotaApiRequestUtil(new NetworkTaskCallback() {
                        @Override
                        public void onSuccessNetworkTask(int taskId, Object object) {
                            Log.d("ToyotaGetCarInfo", object.toString());
                            mListener.onUpdateCarInfo(JSONParseUtil.vehicleParse((JSONObject) object));
                        }
                        @Override
                        public void onFailedNetworkTask(int taskId, Object object) {
                            Log.d("ToyotaGetCarInfo", "error");
                        }
                    });
                    toyotaRequest.onRequest(getActivity().getApplicationContext(), UriUtil.postVehicleInfoUri(), NetworkTasks.CarInfo, params);
                }
            });
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUpdateDataListener) {
            mListener = (OnUpdateDataListener) activity;
        }
        if(mTimer == null){
            //タイマーの初期化処理
            mTimer = new Timer(true);
            mTimer.schedule(mGetCarInfoTask, 100, PERIOD_TIME);
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

    public interface OnUpdateDataListener {
        public void onUpdateCarInfo(VehicleInfo carInfo);
    }

}
