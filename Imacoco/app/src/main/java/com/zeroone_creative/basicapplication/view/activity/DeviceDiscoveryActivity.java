/*
 * Copyright 2014 Sony Corporation
 */

package com.zeroone_creative.basicapplication.view.activity;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zeroone_creative.basicapplication.R;
import com.zeroone_creative.basicapplication.SampleApplication;
import com.zeroone_creative.basicapplication.controller.sony.ServerDevice;
import com.zeroone_creative.basicapplication.controller.sony.SimpleSsdpClient;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
@EActivity(R.layout.activity_device_discovery)
public class DeviceDiscoveryActivity extends Activity {

    private static final String TAG = DeviceDiscoveryActivity.class.getSimpleName();
    private SimpleSsdpClient mSsdpClient;
    private DeviceListAdapter mListAdapter;
    private boolean mActivityActive;

    @ViewById(R.id.discovery_imagebutton_search)
    ImageButton mSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        mSsdpClient = new SimpleSsdpClient();
        mListAdapter = new DeviceListAdapter(this);
        Log.d(TAG, "onCreate() completed.");
    }

    @Click(R.id.discovery_imagebutton_search)
    void clickSearch() {
        if (!mSsdpClient.isSearching()) {
            searchDevices();
            mSearchButton.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityActive = true;
        ListView listView = (ListView) findViewById(R.id.list_device);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ServerDevice device = (ServerDevice) listView.getAdapter().getItem(position);
                launchSampleActivity(device);
            }
        });

        // Show Wi-Fi SSID.
        TextView textWifiSsid = (TextView) findViewById(R.id.discover_textview_wifi_ssid);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            textWifiSsid.setText(Html.fromHtml(htmlLabel));
        } else {
            textWifiSsid.setText(R.string.msg_wifi_disconnect);
        }

        Log.d(TAG, "onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityActive = false;
        if (mSsdpClient != null && mSsdpClient.isSearching()) {
            mSsdpClient.cancelSearching();
        }

        Log.d(TAG, "onPause() completed.");
    }

    /**
     * Start searching supported devices.
     */
    private void searchDevices() {
        mListAdapter.clearDevices();
        setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {
            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.addDevice(device);
                    }
                });
            }
            @Override
            public void onFinished() {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search finished.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);
                        mSearchButton.setEnabled(true);
                        if (mActivityActive) {
                            Toast.makeText(DeviceDiscoveryActivity.this, //
                                    R.string.msg_device_search_finish, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }
            @Override
            public void onErrorFinished() {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search Error finished.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);
                        mSearchButton.setEnabled(true);
                        if (mActivityActive) {
                            Toast.makeText(DeviceDiscoveryActivity.this, //
                                    R.string.msg_error_device_searching, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }
        });
    }

    /**
     * Launch a SampleCameraActivity.
     * 
     * @param device
     */
    private void launchSampleActivity(ServerDevice device) {
        // Go to CameraSampleActivity.
        Toast.makeText(DeviceDiscoveryActivity.this, device.getFriendlyName(), Toast.LENGTH_SHORT).show();
        // Set target ServerDevice instance to control in Activity.
        SampleApplication app = (SampleApplication) getApplication();
        app.setTargetServerDevice(device);
        SonyCameraActivity_.intent(this).start();
    }

    /**
     * Adapter class for DeviceList
     */
    private static class DeviceListAdapter extends BaseAdapter {

        private final List<ServerDevice> mDeviceList;

        private final LayoutInflater mInflater;

        public DeviceListAdapter(Context context) {
            mDeviceList = new ArrayList<ServerDevice>();
            mInflater = LayoutInflater.from(context);
        }

        public void addDevice(ServerDevice device) {
            mDeviceList.add(device);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            mDeviceList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0; // not fine
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = (TextView) mInflater.inflate(R.layout.device_list_item, parent, false);
            }
            ServerDevice device = (ServerDevice) getItem(position);
            ServerDevice.ApiService apiService = device.getApiService("camera");
            String endpointUrl = null;
            if (apiService != null) {
                endpointUrl = apiService.getEndpointUrl();
            }

            // Label
            String htmlLabel =
                    String.format("%s ", device.getFriendlyName()) //
                            + String.format(//
                                    "<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", //
                                    endpointUrl);
            textView.setText(Html.fromHtml(htmlLabel));

            return textView;
        }
    }
}
