package com.zeroone_creative.basicapplication.model.enumerate;

import com.android.volley.Request;

public enum NetworkTasks {
    CarInfo(0, Request.Method.POST),
    Createuser(1, Request.Method.POST),
    CreateDrive(1, Request.Method.POST),
    PostStatus(1, Request.Method.POST),
	;
	public int id;
	//Request
	public int method;
	
	private NetworkTasks(int id, int method) {
		this.id = id;
		this.method = method;
	}
}
