/*
 * Copyright 2014 Sony Corporation
 */
package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.SampleApplication;
import com.zeroone_creative.basicapplication.controller.sony.DisplayHelper;
import com.zeroone_creative.basicapplication.controller.sony.ServerDevice;
import com.zeroone_creative.basicapplication.controller.sony.ShootingStatusUtil;
import com.zeroone_creative.basicapplication.controller.sony.SimpleCameraEventObserver;
import com.zeroone_creative.basicapplication.controller.sony.SimpleRemoteApi;
import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;
import com.zeroone_creative.basicapplication.view.fragment.GetPictureTimerFragment;
import com.zeroone_creative.basicapplication.view.fragment.PostStatusFragment;
import com.zeroone_creative.basicapplication.view.fragment.ToyotaGetCarInfoFragment;
import com.zeroone_creative.basicapplication.view.widget.SimpleStreamSurfaceView;

import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Activity class of Sample Camera screen.
 */
@EActivity(R.layout.activity_sony_camera)
public class SonyCameraActivity extends Activity implements GetPictureTimerFragment.OnUpdatePictureListener, PostStatusFragment.GetUpdateDataListener, ToyotaGetCarInfoFragment.OnUpdateDataListener{
    private static final String TAG = SonyCameraActivity.class.getSimpleName();

    private SimpleStreamSurfaceView mLiveviewSurface;
    private ServerDevice mTargetServer;
    private SimpleRemoteApi mRemoteApi;
    private SimpleCameraEventObserver mEventObserver;
    private SimpleCameraEventObserver.ChangeListener mEventListener;
    private final Set<String> mAvailableCameraApiSet = new HashSet<String>();
    private final Set<String> mSupportedApiSet = new HashSet<String>();

    private Bitmap mLastPicture;
    private VehicleInfo mLastCarInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        SampleApplication app = (SampleApplication) getApplication();
        mTargetServer = app.getTargetServerDevice();
        mRemoteApi = new SimpleRemoteApi(mTargetServer);
        app.setRemoteApi(mRemoteApi);
        mEventObserver = new SimpleCameraEventObserver(getApplicationContext(), mRemoteApi);
        mEventListener = new SimpleCameraEventObserver.ChangeListenerTmpl() {
            @Override
            public void onCameraStatusChanged(String status) {
                Log.d(TAG, "onCameraStatusChanged() called: " + status);
                refreshUi();
            }
            @Override
            public void onApiListModified(List<String> apis) {
                Log.d(TAG, "onApiListModified() called");
                synchronized (mAvailableCameraApiSet) {
                    mAvailableCameraApiSet.clear();
                    for (String api : apis) {
                        mAvailableCameraApiSet.add(api);
                    }
                    if (!mEventObserver.getLiveviewStatus() //
                            && isCameraApiAvailable("startLiveview")) {
                        if (mLiveviewSurface != null && !mLiveviewSurface.isStarted()) {
                            startLiveview();
                        }
                    }
                }
            }
            @Override
            public void onLiveviewStatusChanged(boolean status) {
                Log.d(TAG, "onLiveviewStatusChanged() called = " + status);
            }
            @Override
            public void onStorageIdChanged(String storageId) {
                Log.d(TAG, "onStorageIdChanged() called: " + storageId);
                refreshUi();
            }
        };

        {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            GetPictureTimerFragment getPictureTimerFragment = new GetPictureTimerFragment();
            transaction.add(getPictureTimerFragment, "get_picture_timer");
            transaction.commit();
        }
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
        mEventObserver.activate();
        mLiveviewSurface = (SimpleStreamSurfaceView) findViewById(R.id.sony_camera_surfaceview_liveview);
        prepareOpenConnection();
        Log.d(TAG, "onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeConnection();
        Log.d(TAG, "onPause() completed.");
    }

    private void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");
        setProgressBarIndeterminateVisibility(true);
        new Thread() {
            @Override
            public void run() {
                try {
                    // Get supported API list (Camera API)
                    JSONObject replyJsonCamera = mRemoteApi.getCameraMethodTypes();
                    loadSupportedApiList(replyJsonCamera);
                    try {
                        // Get supported API list (AvContent API)
                        JSONObject replyJsonAvcontent = mRemoteApi.getAvcontentMethodTypes();
                        loadSupportedApiList(replyJsonAvcontent);
                    } catch (IOException e) {
                        Log.d(TAG, "AvContent is not support.");
                    }
                    SampleApplication app = (SampleApplication) getApplication();
                    app.setSupportedApiList(mSupportedApiSet);
                    if (!isApiSupported("setCameraFunction")) {
                        openConnection();
                    } else {
                        // this device supports setCameraFunction.
                        // after confirmation of camera state, open connection.
                        Log.d(TAG, "this device support set camera function");
                        if (!isApiSupported("getEvent")) {
                            Log.e(TAG, "this device is not support getEvent");
                            openConnection();
                            return;
                        }
                        // confirm current camera status
                        String cameraStatus = null;
                        JSONObject replyJson = mRemoteApi.getEvent(false);
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                        String type = cameraStatusObj.getString("type");
                        if ("cameraStatus".equals(type)) {
                            cameraStatus = cameraStatusObj.getString("cameraStatus");
                        } else {
                            throw new IOException();
                        }
                        if (ShootingStatusUtil.isShootingStatus(cameraStatus)) {
                            Log.d(TAG, "camera function is Remote Shooting.");
                            openConnection();
                        } else {
                            startOpenConnectionAfterChangeCameraState();
                            replyJson = mRemoteApi.setCameraFunction("Remote Shooting");
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(SonyCameraActivity.this, false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(SonyCameraActivity.this, false);
                }
            }
        }.start();
    }

    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEventObserver
                        .setEventChangeListener(new SimpleCameraEventObserver.ChangeListenerTmpl() {
                            @Override
                            public void onCameraStatusChanged(String status) {
                                Log.d(TAG, "onCameraStatusChanged:" + status);
                                if ("IDLE".equals(status)) {
                                    openConnection();
                                }
                                refreshUi();
                            }
                            @Override
                            public void onShootModeChanged(String shootMode) {
                                refreshUi();
                            }
                            @Override
                            public void onStorageIdChanged(String storageId) {
                                refreshUi();
                            }
                        });
                mEventObserver.start();
            }
        });
    }

    /**
     * Open connection to the camera device to start monitoring Camera events
     * and showing liveview.
     */
    private void openConnection() {
        mEventObserver.setEventChangeListener(mEventListener);
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "openConnection(): exec.");
                try {
                    JSONObject replyJson = null;
                    // getAvailableApiList
                    replyJson = mRemoteApi.getAvailableApiList();
                    loadAvailableCameraApiList(replyJson);
                    // check version of the server device
                    if (isCameraApiAvailable("getApplicationInfo")) {
                        Log.d(TAG, "openConnection(): getApplicationInfo()");
                        replyJson = mRemoteApi.getApplicationInfo();
                        if (!isSupportedServerVersion(replyJson)) {
                            DisplayHelper.toast(getApplicationContext(), //
                                    R.string.msg_error_non_supported_device);
                            SonyCameraActivity.this.finish();
                            return;
                        }
                    } else {
                        // never happens;
                        return;
                    }
                    // startRecMode if necessary.
                    if (isCameraApiAvailable("startRecMode")) {
                        Log.d(TAG, "openConnection(): startRecMode()");
                        replyJson = mRemoteApi.startRecMode();
                        // Call again.
                        replyJson = mRemoteApi.getAvailableApiList();
                        loadAvailableCameraApiList(replyJson);
                    }
                    // getEvent start
                    if (isCameraApiAvailable("getEvent")) {
                        Log.d(TAG, "openConnection(): EventObserver.start()");
                        mEventObserver.start();
                    }
                    // Liveview start
                    if (isCameraApiAvailable("startLiveview")) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        startLiveview();
                    }
                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    DisplayHelper.setProgressIndicator(SonyCameraActivity.this, false);
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_connection);
                }
            }
        }.start();

    }

    /**
     * Stop monitoring Camera events and close liveview connection.
     */
    private void closeConnection() {
        Log.d(TAG, "closeConnection(): exec.");
        // Liveview stop
        Log.d(TAG, "closeConnection(): LiveviewSurface.stop()");
        if (mLiveviewSurface != null) {
            mLiveviewSurface.stop();
            mLiveviewSurface = null;
            stopLiveview();
        }
        // getEvent stop
        Log.d(TAG, "closeConnection(): EventObserver.release()");
        mEventObserver.release();
        // stopRecMode if necessary.
        if (isCameraApiAvailable("stopRecMode")) {
            new Thread() {

                @Override
                public void run() {
                    Log.d(TAG, "closeConnection(): stopRecMode()");
                    try {
                        mRemoteApi.stopRecMode();
                    } catch (IOException e) {
                        Log.w(TAG, "closeConnection: IOException: " + e.getMessage());
                    }
                }
            }.start();
        }
        Log.d(TAG, "closeConnection(): completed.");
    }

    /**
     * Refresh UI appearance along with current "cameraStatus" and "shootMode".
     */
    private void refreshUi() {
        String cameraStatus = mEventObserver.getCameraStatus();
        String shootMode = mEventObserver.getShootMode();
        // Take picture Button
        if ("still".equals(shootMode) && "IDLE".equals(cameraStatus)) {
            //mButtonTakePicture.setEnabled(true);
        } else {
            //mButtonTakePicture.setEnabled(false);
        }
    }

    /**
     * Retrieve a list of APIs that are available at present.
     * @param replyJson
     */
    private void loadAvailableCameraApiList(JSONObject replyJson) {
        synchronized (mAvailableCameraApiSet) {
            mAvailableCameraApiSet.clear();
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("result");
                JSONArray apiListJson = resultArrayJson.getJSONArray(0);
                for (int i = 0; i < apiListJson.length(); i++) {
                    mAvailableCameraApiSet.add(apiListJson.getString(i));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadAvailableCameraApiList: JSON format error.");
            }
        }
    }

    /**
     * Retrieve a list of APIs that are supported by the target device.
     * 
     * @param replyJson
     */
    private void loadSupportedApiList(JSONObject replyJson) {
        synchronized (mSupportedApiSet) {
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("results");
                for (int i = 0; i < resultArrayJson.length(); i++) {
                    mSupportedApiSet.add(resultArrayJson.getJSONArray(i).getString(0));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadSupportedApiList: JSON format error.");
            }
        }
    }

    /**
     * Check if the specified API is available at present. This works correctly
     * only for Camera API.
     * @param apiName
     * @return
     */
    private boolean isCameraApiAvailable(String apiName) {
        boolean isAvailable = false;
        synchronized (mAvailableCameraApiSet) {
            isAvailable = mAvailableCameraApiSet.contains(apiName);
        }
        return isAvailable;
    }

    /**
     * Check if the specified API is supported. This is for camera and avContent
     * service API. The result of this method does not change dynamically.
     * 
     * @param apiName
     * @return
     */
    private boolean isApiSupported(String apiName) {
        boolean isAvailable = false;
        synchronized (mSupportedApiSet) {
            isAvailable = mSupportedApiSet.contains(apiName);
        }
        return isAvailable;
    }

    /**
     * Check if the version of the server is supported in this application.
     * 
     * @param replyJson
     * @return
     */
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            Log.w(TAG, "isSupportedServerVersion: Number format error.");
        }
        return false;
    }

    //TODO Take a picture
    /**
     * Take a picture and retrieve the image data.
     */
    private void takeAndFetchPicture() {
        if (mLiveviewSurface == null || !mLiveviewSurface.isStarted()) {
            DisplayHelper.toast(getApplicationContext(), R.string.msg_error_take_picture);
            return;
        }
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        Log.w(TAG, "takeAndFetchPicture: post image URL is null.");
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_take_picture);
                        return;
                    }
                    // Show progress indicator
                    DisplayHelper.setProgressIndicator(SonyCameraActivity.this, true);

                    URL url = new URL(postImageUrl);
                    InputStream istream = new BufferedInputStream(url.openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // irresponsible value
                    final Bitmap picture = BitmapFactory.decodeStream(istream, null, options);
                    istream.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Set Picture (to pictureDrawable)
                            mLastPicture =  picture;
                        }
                    });
                } catch (IOException e) {
                    Log.w(TAG, "IOException while closing slicer: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while closing slicer");
                    DisplayHelper.toast(getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } finally {
                    DisplayHelper.setProgressIndicator(SonyCameraActivity.this, false);
                }
            }
        }.start();
    }

    private void startLiveview() {
        if (mLiveviewSurface == null) {
            Log.w(TAG, "startLiveview mLiveviewSurface is null.");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();

                    if (!SimpleRemoteApi.isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            final String liveviewUrl = resultsObj.getString(0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLiveviewSurface.start(liveviewUrl, //
                                            new SimpleStreamSurfaceView.StreamErrorListener() {
                                                @Override
                                                public void onError(StreamErrorReason reason) {
                                                    stopLiveview();
                                                }
                                            });
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLiveview IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLiveview JSONException: " + e.getMessage());
                }
            }
        }.start();
    }

    private void stopLiveview() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mRemoteApi.stopLiveview();
                } catch (IOException e) {
                    Log.w(TAG, "stopLiveview IOException: " + e.getMessage());
                }
            }
        }.start();
    }

    @Override
    public Bitmap getDrawableData() {
        return mLastPicture;
    }

    @Override
    public VehicleInfo getVehicleInfo() {
        return mLastCarInfo;
    }

    @Override
    public void onUpdateCarInfo(VehicleInfo carInfo) {
        mLastCarInfo = carInfo;
    }

    @Override
    public void onCameraShatter() {
        //シャッターを押す
        takeAndFetchPicture();
    }
}