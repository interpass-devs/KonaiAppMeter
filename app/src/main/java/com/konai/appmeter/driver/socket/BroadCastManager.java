package com.konai.appmeter.driver.socket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.konai.appmeter.driver.IntroActivity;
import com.konai.appmeter.driver.view.SettingActivity;


public class BroadCastManager extends BroadcastReceiver {

	Context mContext = null;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;

	@Override
	public void onReceive(Context context, Intent intent) {

		// 수신 된 Intent 처리

		String action = intent.getAction();
		mContext = context;


		//단말이 켜졌을 때 -> 앱미터 실행시키기 위해 연결
		if (action.equals("android.intent.action.BOOT_COMPLETED")) {

			Log.d("BroadCastManager", "boot completed");

			SharedPreferences pref1 = context.getSharedPreferences("env_setting", Activity.MODE_PRIVATE);
			String appControlStatus = pref1.getString("app_control_Status",  String.valueOf(SettingActivity.MODE_PRIVATE));
			Log.d("appControlStatus_1", appControlStatus);


			if (appControlStatus.equals("true")) {

				Log.d("appControlStatus_2", appControlStatus);

				Intent it = new Intent();
				it.setClassName("com.konai.appmeter.driver", "com.konai.appmeter.driver.IntroActivity");
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(it);
				return;

			}else { //false

				Log.d("appControlStatus_3", appControlStatus);

				//do nothing
			}


		}
		else if (action.equals("com.gini.intent.action.GNX_POSTMESSAGE")) {
			// 수신 된 Intent 처리
			Uri data = intent.getData();
			// return;
			int type = intent.getIntExtra("wParam",0);
		}else {
			Log.e("BroadCastManager", "not completed");
		}
		//todo: end

	}//onReceive

//start
//20160811 tra..sh 서비스초기화 r4000에서 종료하지않고 대기
/// action_screen_on메세지로 앱실행 하기 위함.

	public Handler startMainActivityHandler = new Handler() {
		// @Override
		public void handleMessage(Message msg) {

			Intent actIntent = new Intent(mContext,
					IntroActivity.class);

			actIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			mContext.startActivity(actIntent);

		}

	};

}
