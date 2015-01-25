package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.app.FragmentTransaction;

import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;
import com.zeroone_creative.basicapplication.view.fragment.ToyotaGetCarInfoFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements ToyotaGetCarInfoFragment.OnUpdateDataListener {

    private VehicleInfo mCarInfo;

    @AfterViews
    public void onAfterViews(){
        {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            ToyotaGetCarInfoFragment toyotaGetCarInfoFragment = new ToyotaGetCarInfoFragment();
            transaction.add(toyotaGetCarInfoFragment, "toyota_get_carinfo");
            transaction.commit();
        }
    }

    @Override
    public void onUpdateCarInfo(VehicleInfo carInfo) {
        this.mCarInfo = carInfo;
    }
}
