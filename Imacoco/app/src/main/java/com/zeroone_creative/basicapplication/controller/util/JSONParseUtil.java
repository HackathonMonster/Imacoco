package com.zeroone_creative.basicapplication.controller.util;import com.google.android.gms.maps.model.LatLng;import com.zeroone_creative.basicapplication.model.pojo.PosN;import com.zeroone_creative.basicapplication.model.pojo.VehicleData;import com.zeroone_creative.basicapplication.model.pojo.VehicleInfo;import org.json.JSONException;import org.json.JSONObject;public class JSONParseUtil {		public static VehicleInfo vehicleParse(JSONObject rootObject){		VehicleInfo info = new VehicleInfo();		try{			JSONObject infoObject = rootObject.getJSONArray("vehicleinfo").getJSONObject(0);			info.vid = infoObject.getString("vid");			info.userid = infoObject.getString("userid");						JSONObject dataObject = infoObject.getJSONArray("data").getJSONObject(0);			VehicleData data = new VehicleData();			try{				data.createtime = dataObject.getString("createtime");							data.spd = dataObject.getDouble("Spd");				data.accrPedlRat = dataObject.getInt("AccrPedlRat");				try{					data.brkIndcr = dataObject.getInt("BrkIndcr");				}catch(JSONException e){					data.brkIndcr = 0;				}				try{					data.trsmGearPosn = dataObject.getString("TrsmGearPosn");				}catch(JSONException e){					data.trsmGearPosn = "D";				}				data.steerAg = dataObject.getInt("SteerAg");				try{					data.engN = dataObject.getInt("EngN");				}catch(JSONException e){					data.engN = 0;				}				try{					data.hdLampLtgIndcn = dataObject.getInt("HdLampLtgIndcn");				}catch(JSONException e){					data.hdLampLtgIndcn = 0;				}				try{					data.wiprSts = dataObject.getInt("WiprSts");				}catch(JSONException e){					data.wiprSts = 0;				}			}catch(JSONException e){				e.printStackTrace();			}			JSONObject posnObject = dataObject.getJSONObject("Posn");			PosN posn = new PosN();			try{				posn.mapMtchg = posnObject.getInt("MapMtchg");				posn.latlng = new LatLng(posnObject.getDouble("lat"), posnObject.getDouble("lon"));			}catch(JSONException e){				e.printStackTrace();			}			data.posN = posn;			info.data = data;		}catch(JSONException e){			e.printStackTrace();		}		return info;	}			}