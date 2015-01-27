/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.controller.provider.NetworkTaskCallback;
import com.zeroone_creative.basicapplication.controller.provider.VolleyHelper;
import com.zeroone_creative.basicapplication.controller.util.JSONRequestUtil;
import com.zeroone_creative.basicapplication.controller.util.UriUtil;
import com.zeroone_creative.basicapplication.model.enumerate.NetworkTasks;
import com.zeroone_creative.basicapplication.model.system.AppConfig;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_welcome)
public class WelcomeActivity extends Activity {

    @ViewById(R.id.welcome_edittext_userid)
    EditText mUserIdEditText;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.PREF_NAME, Context.MODE_PRIVATE);
        String userStr = sharedPreferences.getString(AppConfig.PREF_KEY_USER, "");
        if (!userStr.equals("")) {
            RegistActivity_.intent(mContext).start();
            finish();
        }
    }

    @Click(R.id.welcome_imagebutton_register)
    void clickRegist() {
        findViewById(R.id.welcome_imagebutton_register).setEnabled(false);

        JSONRequestUtil createUserRequest = new JSONRequestUtil(new NetworkTaskCallback() {
            @Override
            public void onSuccessNetworkTask(int taskId, Object object) {
                mContext.getSharedPreferences(AppConfig.PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString(AppConfig.PREF_KEY_USER, object.toString())
                        .commit();
                RegistActivity_.intent(mContext).start();
            }
            @Override
            public void onFailedNetworkTask(int taskId, Object object) {
                Toast.makeText(mContext, getString(R.string.common_faild_network), Toast.LENGTH_SHORT).show();
            }
        },
        this.getClass().getSimpleName(),
        null);
        JSONObject params = new JSONObject();
        try {
            params.put("name", mUserIdEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createUserRequest.onRequest(VolleyHelper.getRequestQueue(this),
                Request.Priority.HIGH,
                UriUtil.postCreateUser(),
                NetworkTasks.Createuser,
                params);
    }
}
