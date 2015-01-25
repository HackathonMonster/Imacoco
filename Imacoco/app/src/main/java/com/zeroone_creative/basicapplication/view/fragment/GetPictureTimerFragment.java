package com.zeroone_creative.basicapplication.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class GetPictureTimerFragment extends Fragment {

    private OnUpdatePictureListener mListener;

    private final int PERIOD_TIME = 30*1000;

    private Timer mTimer;
    private Handler mHandler = new Handler();

    private TimerTask mGetCarInfoTask = new TimerTask() {
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post( new Runnable() {
                public void run() {
                    mListener.onCameraShatter();
                }
            });
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUpdatePictureListener) {
            mListener = (OnUpdatePictureListener) activity;
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

    public interface OnUpdatePictureListener {
        public void onCameraShatter();
    }


}
