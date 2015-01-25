package com.zeroone_creative.basicapplication.model.pojo;

public class VehicleData {
	public String createtime;
	//スピード　0~327.67
	public double spd;
	//アクセル開度　％
	public int accrPedlRat;
	//ブレーキのオン・オフ 0:オフ 1:オン
	public int brkIndcr;
	//ステアリング舵角を取得
	public int steerAg;
	//1 分間当たりのエンジン回転数 を取得。 0~12800
	public int engN;
	//ヘッドライトの点灯状態を取得 0:オフ 1:Lo オン 2:Hi オン
	public int hdLampLtgIndcn;
	//ワイパー動作状態を取得 0:オフ 1:間欠作動 2:Lo(低速動作) 3:Hi(高速動作)
	public int wiprSts;
	//シフトポジションを取得。
	public String trsmGearPosn;
	//位置を入力
	public PosN posN; 
	
}
