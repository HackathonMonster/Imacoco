package com.zeroone_creative.basicapplication.controller.util;

import android.net.Uri;

/**
 * Created by shunhosaka on 2014/12/06.
 */
public class UriUtil {

    static private String baseUrl = "t-ghack.herokuapp.com";

    static private Uri.Builder getBaseUri(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.encodedAuthority(baseUrl);
        return builder;
    }

    static public String postVehicleInfoUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.encodedAuthority("api-jp-t-itc.com");
        builder.path("/GetVehicleInfo");
		return builder.build().toString();
    }

    static public String postCreateUser() {
        Uri.Builder builder = getBaseUri();
        builder.path("/create_user");
        return builder.build().toString();
    }

    static public String postDrive() {
        Uri.Builder builder = getBaseUri();
        builder.path("/drive");
        return builder.build().toString();
    }

    static public String postStatus() {
        Uri.Builder builder = getBaseUri();
        builder.path("/status");
        return builder.build().toString();
    }

}
