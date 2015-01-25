package com.zeroone_creative.basicapplication.model.system;

public class AppConfig {

	private AppConfig(){
		//restrict instantiation
	}
	//デバッグ常態化どうかのフラグ
	public static final boolean DEBUG = true;
	public static final boolean REAL_DEVICE = true;

    //TOYOTA DEVELOPER KEY
    public static final String DEVELOPERKEY = "4bbf829d5e0c";

    public static final String PREF_NAME = "sharedpreference_imacoco";
    public static final String PREF_KEY_USER = "key_pref_user";
    public static final String PREF_KEY_DRIVE = "key_pref_drive";

}
