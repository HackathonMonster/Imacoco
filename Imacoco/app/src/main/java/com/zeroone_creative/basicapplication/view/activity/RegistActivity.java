package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.controller.provider.NetworkTaskCallback;
import com.zeroone_creative.basicapplication.controller.provider.VolleyHelper;
import com.zeroone_creative.basicapplication.controller.util.JSONRequestUtil;
import com.zeroone_creative.basicapplication.controller.util.UriUtil;
import com.zeroone_creative.basicapplication.model.enumerate.NetworkTasks;
import com.zeroone_creative.basicapplication.model.pojo.User;
import com.zeroone_creative.basicapplication.model.system.AppConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_regist)
public class RegistActivity extends Activity {

    @ViewById(R.id.regist_edittext_goal)
    EditText mGoalEditText;
    @ViewById(R.id.regist_edittext_mail)
    EditText mMailEditText;

    private Context mContext;

    @AfterViews
    void onAfterViews() {
        mContext = this;
    }

    @Click(R.id.regist_imagebutton_register)
    void driveStart() {
        DeviceDiscoveryActivity_.intent(mContext).start();
        findViewById(R.id.regist_imagebutton_register).setEnabled(false);

        JSONRequestUtil createUserRequest = new JSONRequestUtil(new NetworkTaskCallback() {
            @Override
            public void onSuccessNetworkTask(int taskId, Object object) {
                findViewById(R.id.regist_imagebutton_register).setEnabled(true);
                mContext.getSharedPreferences(AppConfig.PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString(AppConfig.PREF_KEY_DRIVE, object.toString())
                        .commit();
                DeviceDiscoveryActivity_.intent(mContext).start();
            }
            @Override
            public void onFailedNetworkTask(int taskId, Object object) {
                findViewById(R.id.regist_imagebutton_register).setEnabled(true);
                Toast.makeText(mContext, getString(R.string.common_faild_network), Toast.LENGTH_SHORT).show();
            }
        },
                this.getClass().getSimpleName(),
                null);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConfig.PREF_NAME, Context.MODE_PRIVATE);
        String userStr = sharedPreferences.getString(AppConfig.PREF_KEY_USER, "");
        User user = new Gson().fromJson(userStr, User.class);
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Integer.toString(user.id));
            params.put("goal", mGoalEditText.getText().toString());
            params.put("mail", mGoalEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createUserRequest.onRequest(VolleyHelper.getRequestQueue(this),
                Request.Priority.HIGH,
                UriUtil.postDrive(),
                NetworkTasks.CreateDrive,
                params);
    }

}