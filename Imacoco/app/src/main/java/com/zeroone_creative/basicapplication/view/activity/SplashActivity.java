package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.content.Context;

import com.zeroone_creative.basicapplication.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.Timer;
import java.util.TimerTask;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends Activity {

    private android.os.Handler mHandler = new android.os.Handler();
    private Context mContext;

    @AfterViews
    void onAfterViews() {
        mContext = this;
        // タイマーのセット
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WelcomeActivity_.intent(mContext).start();
                        finish();
                    }
                });
            }
        }, 1300);
    }


}
