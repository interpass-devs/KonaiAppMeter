package com.konai.appmeter.driver;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AMBleConfigActivity extends ListActivity {
	/** Called when the activity is first created. */

	TextView textcut;// 수량
	int clickposition = 0;

	RowAdapter listadapter;
	public ListView m_lv = null;
	ArrayList<String> itemlist = new ArrayList<String>();

	String[] masDisplay = {""};

	boolean bExit = false;

	LinearLayout mlayoutlist;
	LinearLayout mlayoutinfoinput;
	LinearLayout mlayoutinfo;
	LinearLayout mlayoutinput;
	LinearLayout mlayoutnumbtn;

	TextView mtextinfo;
	TextView mtextinput, mCustomKeyboard;  //todo: 2022-05-04
	EditText meditinput;
	Context mContext; //todo: 2022-05-04
	Boolean OnClick = true;

	LinearLayout radio_button_layout, customkeyboardviewLayout;  //todo: 2022-05-04
	ArrayList<String> list = new ArrayList<>();
	int index;

	//20210716
	private Button nBtn0;
	private Button nBtn1;
	private Button nBtn2;
	private Button nBtn3;
	private Button nBtn6;
	String mData;
	String logtag="logtag_";


	//todo: 2022-05-04
	CustomKeyboardView customKeyboardView;
	Keyboard kb;
	String keyValue;
	ArrayList<String> codeList = new ArrayList<>();
	String calVal;
	int lastIndex;
	//todo: end


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.ambleconfig_horizontal);
//		setContentView(R.layout.ambleconfig);

		mContext = this;


//		Log.d("AMCleConfigActivity","AMCleConfigActivity OnCreate");

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		if (setting.gOrient ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			Log.d(logtag+"ori","가로");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //20211229
			setContentView(R.layout.ambleconfig_horizontal);
		}else {
			Log.d(logtag+"ori","세로");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //20211229
			setContentView(R.layout.ambleconfig);
		}

		init();

//20220107 집계메뉴표시
		Intent intent = getIntent();
		mData = intent.getStringExtra("history") + "";
		if(mData.equals("Y")){
//			Log.d(logtag+"YY","YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
			textcut = (TextView) findViewById(R.id.TextViewTitle);
			textcut.setText("거래집계");
//			Button closeBtn = findViewById(R.id.Btnclose);
			sendAMBlehistory(true);
		}
		else
			sendAMBlemenuonoff(true);
/////////////////

//		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		new Thread(new checkThread())
				.start();

		mlayoutlist = (LinearLayout) findViewById(R.id.layoutlist);
		mlayoutlist.setVisibility(View.VISIBLE);
		mlayoutinfoinput = (LinearLayout) findViewById(R.id.layoutinfoinput);
		mlayoutinfoinput.setVisibility(View.INVISIBLE);
		mlayoutinfo = (LinearLayout) findViewById(R.id.layoutinfo);
		mtextinfo = (TextView)findViewById(R.id.textinfo);
		mtextinfo.setMovementMethod(new ScrollingMovementMethod());
		mlayoutinfo.setVisibility(View.VISIBLE);
		mlayoutinput = (LinearLayout) findViewById(R.id.layoutinput);
		mtextinput = (TextView)findViewById(R.id.textinput);
		mCustomKeyboard = (TextView)findViewById(R.id.m_keyboard);//todo: 2022-05-04
		meditinput = (EditText)findViewById(R.id.editinput);
//		meditinput.setTransformationMethod(PasswordTransformationMethod.getInstance());
//		meditinput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		radio_button_layout = (LinearLayout) findViewById(R.id.radio_button_layout);
		customkeyboardviewLayout = (LinearLayout) findViewById(R.id.customkeyboardviewLayout);//todo: 2022-05-04
		mlayoutinput.setVisibility(View.INVISIBLE);
		radio_button_layout.setVisibility(View.INVISIBLE);
		mlayoutnumbtn = (LinearLayout) findViewById(R.id.layoutnumbtn);; //20210716
		mlayoutnumbtn.setVisibility(View.INVISIBLE); //20210716

		customKeyboardView = (CustomKeyboardView) findViewById(R.id.customkeyboardview);  //todo: 2022-05-04
		kb = new Keyboard(mContext, R.xml.custom_keyboard);

//		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(meditinput.getWindowToken(), 0);

		if (list.size() != 0){
			list.removeAll(list);
			Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
		}

		final RadioButton button_0 = (RadioButton) findViewById(R.id.button_0);
		final RadioButton button_1 = (RadioButton) findViewById(R.id.button_1);
		final RadioButton button_2 = (RadioButton) findViewById(R.id.button_2);
		final RadioButton button_3 = (RadioButton) findViewById(R.id.button_3);
		final RadioButton button_4 = (RadioButton) findViewById(R.id.button_4);
		final RadioButton button_5 = (RadioButton) findViewById(R.id.button_5);
		final RadioButton button_6 = (RadioButton) findViewById(R.id.button_6);
		final RadioButton button_7 = (RadioButton) findViewById(R.id.button_7);
		final RadioButton button_8 = (RadioButton) findViewById(R.id.button_8);
		final RadioButton button_9 = (RadioButton) findViewById(R.id.button_9);
		final RadioButton button_back = (RadioButton) findViewById(R.id.button_back);
		final RadioButton button_clear = (RadioButton) findViewById(R.id.button_clear);

		button_0.setOnClickListener(mCalculatorListener);
		button_1.setOnClickListener(mCalculatorListener);
		button_2.setOnClickListener(mCalculatorListener);
		button_3.setOnClickListener(mCalculatorListener);
		button_4.setOnClickListener(mCalculatorListener);
		button_5.setOnClickListener(mCalculatorListener);
		button_6.setOnClickListener(mCalculatorListener);
		button_7.setOnClickListener(mCalculatorListener);
		button_8.setOnClickListener(mCalculatorListener);
		button_9.setOnClickListener(mCalculatorListener);
		button_back.setOnClickListener(mCalculatorListener);
		button_clear.setOnClickListener(mCalculatorListener);

		final Button Btnback = (Button)findViewById(R.id.Btnback);
		Btnback.setOnTouchListener(new OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
			// 뗐을때를 구분)
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//					Btnback.setBackgroundResource(R.drawable.payment_subbtn2_p);
					Btnback.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
				}
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
//					Btnback.setBackgroundResource(R.drawable.payment_subbtn2);
					Btnback.setBackgroundResource(R.drawable.unselected_btn);
				}
				return false;
			}
		});

		Btnback.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendAMBlemenu((byte) '1', 0);
			}
		});

		//날짜입력 editText
		meditinput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		//전송버튼
		final Button Btninputsend = (Button)findViewById(R.id.Btninputsend);
		Btninputsend.setOnTouchListener(new OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
			// 뗐을때를 구분)
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//					Btninputsend.setBackgroundResource(R.drawable.payment_subbtn2_p);
					Btninputsend.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
					Btninputsend.getResources().getColor(R.color.btn_grey);
				}
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
//					Btninputsend.setBackgroundResource(R.drawable.payment_subbtn2);
					Btninputsend.setBackgroundResource(R.drawable.yellow_gradi_btn);
					Btninputsend.getResources().getColor(R.color.btn_grey);
				}
				return false;
			}
		});



		//todo: 2022-05-04
		mCustomKeyboard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//me: 방법 1)
//				keyboard.setVisibility(View.VISIBLE);
//				radio_button_layout.setVisibility(View.GONE);


				//me: 방법 2)
				if (OnClick){
					mCustomKeyboard.setText("숫자");
					radio_button_layout.setVisibility(View.GONE);
					Log.d("keyboardValue", customKeyboardView.isInEditMode()+""); //true
					customKeyboardView.setActionListenerActivity((Activity) mContext);
					customKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
					customkeyboardviewLayout.setVisibility(View.VISIBLE);
					OnClick = false;
				}else {
					mCustomKeyboard.setText("영문");
					customkeyboardviewLayout.setVisibility(View.GONE);
					radio_button_layout.setVisibility(View.VISIBLE);
					OnClick = true;
				}
			}
		});
		//todo: end



		Btninputsend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				sendAMBleinput((byte)'1');

				if (list.size() != 0){   //전송하면서 list 초기화 - why? 이전버튼 눌렀을 때 다시 날짜입력 알림창이 뜨면 전에 선택한 list 값이 불려짐.
					list.removeAll(list);
					Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
				}
			}
		});

		//취소버튼
		final Button Btninputcancel = (Button)findViewById(R.id.Btninputcancel);
		Btninputcancel.setOnTouchListener(new OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
			// 뗐을때를 구분)
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//					Btninputcancel.setBackgroundResource(R.drawable.payment_subbtn2_p);
					Btninputcancel.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
					Btninputcancel.getResources().getColor(R.color.btn_grey);
				}
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
//					Btninputcancel.setBackgroundResource(R.drawable.payment_subbtn2);
					Btninputcancel.setBackgroundResource(R.drawable.unselected_btn);
					Btninputcancel.getResources().getColor(R.color.grey);
				}
				return false;
			}
		});

		Btninputcancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				sendAMBleinput((byte)'0');

			}
		});

		final Button Btnclose = (Button) findViewById(R.id.Btnclose);
		Btnclose.setOnTouchListener(new OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
			// 뗐을때를 구분)
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//					Btnclose.setBackgroundResource(R.drawable.payment_subbtn2_p);
					Btnclose.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
				}
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
//					Btnclose.setBackgroundResource(R.drawable.payment_subbtn2);
					Btnclose.setBackgroundResource(R.drawable.unselected_btn);
				}
				return false;
			}
		});

		Btnclose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendAMBlemenuonoff(false);
//				Intent actIntent = new Intent(getApplicationContext(),
//						MainActivity.class);
				Intent actIntent = Info.g_MainIntent;
				actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(actIntent);

				close();
				finish();
			}
		});

		final ImageButton Btnhome = (ImageButton) findViewById(R.id.Btnhome);

		Btnhome.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendAMBlemenuonoff(false);
//				Intent actIntent = new Intent(getApplicationContext(),
///						MainActivity.class);
				Intent actIntent = Info.g_MainIntent;
				actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(actIntent);

				close();
				finish();
			}
		});
		displaynumbtn();
	}



	//todo: 2022-05-04
	private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {		@Override
	public void onKey(int primaryCode, int[] keyCodes) {

		List<Keyboard.Key> key = kb.getKeys();

		// i = selected keyboard index
		for (int i=0; i<key.size(); i++) {

			Log.d("keySize-> ", i+": "+key.size()+"");
			Keyboard.Key currentKey = key.get(i);
			Log.d("keyCurrent-> ", currentKey.codes+ ": " +(String) currentKey.label);

			int codes[] = currentKey.codes;


//                //todo: 현재 선택한 index 찾기
			if (primaryCode == codes[0]) {

				if (primaryCode == 67) { //[삭제]버튼

					Log.d("remove_삭제버튼",primaryCode+"");

					if (list.size() != 0) {
						list.removeAll(list);
//							meditinput.setText("");
						Log.e("remove_all", list.size()+"개: "+ list.toString());
					}
				}
				else if (primaryCode == 5) { //[지우기]버튼

					Log.d("remove_지우기버튼",primaryCode+"");

					try {
						lastIndex = list.size() - 1;
						Log.e("remove_1", lastIndex+"번: "+list.get(lastIndex));

						if (lastIndex <= 0) {

							Log.e("remove_2", lastIndex+",  값: "+list.get(lastIndex));
							list.removeAll(list);
							meditinput.setText("");

						} else {
							list.remove(lastIndex);
							Log.d("remove_3", list.toString());
						}

					}catch (Exception e) { e.printStackTrace(); }
				}
				else {  //리스트에 더하기
					calVal = currentKey.label+"";
					list.add(currentKey.label+"");
					Log.d("remove_nothing_but_add", list.toString());
				}
			}
		}//for

		Log.d("codeList2-> ", list.toString());

		try {

			if (list.size() == 0) {
				Log.d("listSizeCode",list.size()+" = 0!!");
				meditinput.setText("");
				calVal = "";
			}else {
				calVal = TextUtils.join("", list);
				meditinput.setText(calVal);
				Log.d("listSizeCode",list.size()+"");
				Log.d("listCodeCalVal",calVal);
				Log.d("listEditText", meditinput.getText().toString());
			}

			Log.d("listFinalCalVal", calVal);

		}catch (Exception e) { e.printStackTrace(); }


	}

		@Override
		public void onPress(int primaryCode) {

		}

		@Override
		public void onRelease(int primaryCode) {

		}

		@Override
		public void onText(CharSequence text) {

		}

		@Override
		public void swipeLeft() {

		}

		@Override
		public void swipeRight() {

		}

		@Override
		public void swipeDown() {

		}

		@Override
		public void swipeUp() {

		}
	};
	//todo: end




//20220303 tra..sh
	private View.OnClickListener mCalculatorListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {

			for (int i=0; i<1; i++){

				index = i;
				final int listSize = list.size();

				switch (v.getId()){
					case R.id.button_0:
//						if (listSize != 6)
						{
							list.add("0");
						}
						break;
					case R.id.button_1:
//						if (listSize != 6)
						{
							list.add("1");
						}
						break;
					case R.id.button_2:
//						if (listSize != 6)
						{
							list.add("2");
						}
						break;
					case R.id.button_3:
//						if (listSize != 6)
						{
							list.add("3");
						}
						break;
					case R.id.button_4:
//						if (listSize != 6)
						{
							list.add("4");
						}
						break;
					case R.id.button_5:
//						if (listSize != 6)
						{
							list.add("5");
						}
						break;
					case R.id.button_6:
//						if (listSize != 6)
						{
							list.add("6");
						}
						break;
					case R.id.button_7:
//						if (listSize != 6)
						{
							list.add("7");
						}
						break;
					case R.id.button_8:
//						if (listSize != 6)
						{
							list.add("8");
						}
						break;
					case R.id.button_9:
//						if (listSize != 6)
						{
							list.add("9");
						}
						break;
					case R.id.button_back:

						try{
							int lastIndex = list.size() - 1;
							Log.d(logtag+"remove_previous_index", lastIndex+"번: "+list.get(lastIndex));

							if (lastIndex <= 0){
								Log.d(logtag+"last_index==0", lastIndex+",  값: "+list.get(lastIndex));
								list.removeAll(list);
								meditinput.setText("");
							}else {
								list.remove(lastIndex);
								Log.d(logtag+"last_index", lastIndex+",  값: "+list.get(lastIndex));
							}

						}catch (Exception e){}
						break;
					case R.id.button_clear:
						if (list.size() != 0){
							list.removeAll(list);
							Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
						}
						break;
				}
			}//for..

			Log.d(logtag+"list_final", list.toString()+",   사이즈: "+list.size());

			try{
				String calVal;

				if (list.size() == 0){
					Log.d(logtag+"listSize","000000000000");
					meditinput.setText("");
					calVal = "";
				}else {

					calVal = TextUtils.join("", list);
					Log.d(logtag+"calVal", calVal);
//					int calValInt = Integer.parseInt(calVal);
//					DecimalFormat f = new DecimalFormat("###,###");
//					String formatVal = f.format(calValInt);
					meditinput.setText(calVal);
					Log.d(logtag+"getEditTextVal", meditinput.getText().toString());
				}

				Log.d(logtag+"final_calVal!!", calVal+"_!");

				if (calVal.equals(null) || calVal.equals("") || Integer.parseInt(calVal) < 0){

				}else {

				}

			}catch (Exception e){}

		}//onClick..
	};



	public void init() {
//20160903 tra..sh		setNotification();
		AMBlestruct.AMmenu.mMenu = true;

		textcut = (TextView) findViewById(R.id.TextViewTitle);
//		if (mData.equals("Y")){
//			textcut.setText("거래래내역");
//		}

		listclear();

//20220107		sendAMBlemenuonoff(true);

		listview();

	}

	public void close()
	{
		AMBlestruct.AMmenu.mMenu = false;
		bExit = true;
	}

	public Handler updatedisplay = new Handler() {
		//		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			if(msg.what == 99)
			{
				masDisplay = AMBlestruct.AMmenu.menudisplay.split("\n");

				if(AMBlestruct.AMmenu.menutype == '0')
				{

//					Intent actIntent = new Intent(getApplicationContext(),
///							MainActivity.class);

					Intent actIntent = Info.g_MainIntent;

					actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);

					startActivity(actIntent);

					close();
					AMBlestruct.setMenuupdate(false);

					finish();

				}
				else if(AMBlestruct.AMmenu.menutype == '1')
				{

					mlayoutinfoinput.setVisibility(View.INVISIBLE);
					mlayoutnumbtn.setVisibility(View.INVISIBLE); //20210716
					mlayoutlist.setVisibility(View.VISIBLE);
					listview();

				}
				else if(AMBlestruct.AMmenu.menutype == '2')
				{
					mlayoutinfoinput.setVisibility(View.VISIBLE);
					mlayoutinput.setVisibility(View.INVISIBLE);
					radio_button_layout.setVisibility(View.INVISIBLE);
					mlayoutlist.setVisibility(View.INVISIBLE);
					mlayoutnumbtn.setVisibility(View.INVISIBLE); //20210716
					mtextinfo.setText(AMBlestruct.AMmenu.menudisplay);

				}
				else if(AMBlestruct.AMmenu.menutype == '3') //20210716
				{
					mlayoutinfoinput.setVisibility(View.VISIBLE);
					mlayoutinput.setVisibility(View.INVISIBLE);
					radio_button_layout.setVisibility(View.INVISIBLE);
					mlayoutlist.setVisibility(View.INVISIBLE);
					mlayoutnumbtn.setVisibility(View.VISIBLE); //20210716
					mtextinfo.setText(AMBlestruct.AMmenu.menudisplay);

				}
				else if(AMBlestruct.AMmenu.menutype == '7')
				{
					mlayoutinfoinput.setVisibility(View.VISIBLE);
					mtextinfo.setText(AMBlestruct.AMmenu.menudisplay);
					mlayoutinput.setVisibility(View.VISIBLE);
					radio_button_layout.setVisibility(View.VISIBLE);
					mlayoutlist.setVisibility(View.INVISIBLE);
					mlayoutnumbtn.setVisibility(View.INVISIBLE); //20210716
					mtextinput.setText(AMBlestruct.AMmenu.menuinputdisplay);

//20220318 tra..sh
					if (list.size() != 0){
						list.removeAll(list);
						Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
					}

					meditinput.setText("");
					meditinput.requestFocus();
					if(AMBlestruct.AMmenu.menuinputtype == '0')
					{

						meditinput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

					}
					else if(AMBlestruct.AMmenu.menuinputtype == '1') {

						meditinput.setTransformationMethod(PasswordTransformationMethod.getInstance());

					}

//					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

				}
				else if(AMBlestruct.AMmenu.menutype == '9') //input
				{
					mlayoutinfoinput.setVisibility(View.VISIBLE);
					mtextinfo.setText("");
					mlayoutinput.setVisibility(View.VISIBLE);
					radio_button_layout.setVisibility(View.VISIBLE);
					mlayoutlist.setVisibility(View.INVISIBLE);
					mlayoutnumbtn.setVisibility(View.INVISIBLE); //20210716
					mtextinput.setText(AMBlestruct.AMmenu.menuinputdisplay);

//20220318 tra..sh
					if (list.size() != 0){
						list.removeAll(list);
						Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
					}

					meditinput.setText("");
					meditinput.requestFocus();
					if(AMBlestruct.AMmenu.menuinputtype == '0')
					{

						meditinput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

					}
					else if(AMBlestruct.AMmenu.menuinputtype == '1') {

						meditinput.setTransformationMethod(PasswordTransformationMethod.getInstance());

					}
					//AMBlestruct.AMmenu.menuinputtype

//					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

				}

				AMBlestruct.setMenuupdate(false);

			}
		}

	};

	class checkThread implements Runnable {

		public void run() {

			while(!bExit)
			{
				if(AMBlestruct.AMmenu.mbMenuupdated)
				{

					updatedisplay.sendEmptyMessage(99);

				}

				try {
//					Thread.sleep(500);
					Thread.sleep(100); //20220407 tra..sh
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

//20210823
			if(AMBlestruct.AMmenu.menutype != '0') {
				AMBlestruct.AMmenu.menuseltype = '2';
				AMBlestruct.AMmenu.menuselval = '0';
				Info.m_Service.writeBLE("43");
			}

		}
	}

	// ListView의 아이템이 클릭되면 호출되는 callback.
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		clickposition = position;
		String msg = "";
		String stmp = "";
		msg = (String)l.getItemAtPosition(position);

		try {
			StringTokenizer tokens = new StringTokenizer(msg);

			if(tokens.hasMoreTokens()) {
				stmp = tokens.nextToken(".");   // 이름
//stmp가 숫자인지문자인지 구분해야함 TODD.
				if (stmp.length() <= 2 && Integer.parseInt(stmp) > 0)
					sendAMBlemenu((byte) '0', Integer.parseInt(stmp));

			}

		}
		catch (NullPointerException e) {
			e.printStackTrace();

		}

		// Toast.makeText(this, list.get(position), Toast.LENGTH_SHORT).show();
	}

	private void listview()
	{

		listclear();

		listadapter = new RowAdapter(this, R.layout.ambllelistview, masDisplay);

		m_lv = getListView();
		m_lv.setAdapter(listadapter);

	}
	private void listclear()
	{

		itemlist.clear();

	}

	private void sendAMBlemenu(byte type, int nselect)
	{
		AMBlestruct.AMmenu.menuseltype = type;
		AMBlestruct.AMmenu.menuselval = (byte)(nselect + 0x30);
		Info.m_Service.writeBLE("43");
	}

	private void sendAMBleinput(byte type)
	{
		AMBlestruct.AMmenu.menuinputsendhow = type;
		if(type == '0')
		{
			AMBlestruct.AMmenu.menuinputsendval = "0";
			AMBlestruct.AMmenu.menuinputsendlen = 0;
		}
		else if(type == '1') {

			AMBlestruct.AMmenu.menuinputsendval = meditinput.getText().toString();
			AMBlestruct.AMmenu.menuinputsendlen = AMBlestruct.AMmenu.menuinputsendval.length();
		}

		Info.m_Service.writeBLE("47");

	}


	//todo: 집계함수 하나 더 생성 - 20211227  ******

	//todo: 메인함수
	private void sendAMBlemenuonoff(boolean bonoff)
	{
		AMBlestruct.setMenuupdate(false);

		if(bonoff == true)
		{
			AMBlestruct.AMmenu.mMenuState = AMBlestruct.OPEN;
			Info.m_Service.writeBLE("41");
		}
		else {
			AMBlestruct.AMmenu.menuseltype = AMBlestruct.CLOSE;
			AMBlestruct.AMmenu.menuselval = '0';
			Info.m_Service.writeBLE("43");
		}

	}

	//20220107
	private void sendAMBlehistory(boolean bonoff)
	{
		AMBlestruct.setMenuupdate(false);
		{

			AMBlestruct.mSState = "61";
			Info.m_Service.writeBLE("15");
		}

	}

	//20210716
	void displaynumbtn()
	{

		nBtn0 = (Button)findViewById(R.id.btn_0);
		nBtn1 = (Button)findViewById(R.id.btn_1);
		nBtn2 = (Button)findViewById(R.id.btn_2);
		nBtn3 = (Button)findViewById(R.id.btn_3);
		nBtn6 = (Button)findViewById(R.id.btn_6);


		View.OnTouchListener touch1 = new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent arg1)
			{
				{ //if(m_bAddpay == false)
					if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

						if (view == nBtn0) {

							nBtn0.setBackgroundResource(R.drawable.payment_num_p);
							nBtn0.setTextColor(Info.g_number_p);
						} else if (view == nBtn1) {

							nBtn1.setBackgroundResource(R.drawable.payment_num_p);
							nBtn1.setTextColor(Info.g_number_p);
						} else if (view == nBtn2) {

							nBtn2.setBackgroundResource(R.drawable.payment_num_p);
							nBtn2.setTextColor(Info.g_number_p);

						} else if (view == nBtn3) {

							nBtn3.setBackgroundResource(R.drawable.payment_num_p);
							nBtn3.setTextColor(Info.g_number_p);

						}else if (view == nBtn6) {

							nBtn6.setBackgroundResource(R.drawable.payment_num_p);
							nBtn6.setTextColor(Info.g_number_p);

						}

					}
///////////////////////////////
					else if (arg1.getAction() == MotionEvent.ACTION_UP) {
						if (view == nBtn0) {

							nBtn0.setBackgroundResource(R.drawable.payment_subbtn2);
							nBtn0.setTextColor(Color.rgb(0x82, 0x03, 0x16));
						} else if (view == nBtn1) {

							nBtn1.setBackgroundResource(R.drawable.payment_subbtn2);
							nBtn1.setTextColor(Info.g_number);
						} else if (view == nBtn2) {

							nBtn2.setBackgroundResource(R.drawable.payment_subbtn2);
							nBtn2.setTextColor(Info.g_number);
						} else if (view == nBtn3) {

							nBtn3.setBackgroundResource(R.drawable.payment_subbtn2);
							nBtn3.setTextColor(Info.g_number);
						}else if (view == nBtn6) {

							nBtn6.setBackgroundResource(R.drawable.payment_subbtn2);
							nBtn6.setTextColor(Color.rgb(0x82, 0x03, 0x16));
						}
					}
				}

				return false;
			}
		};

		View.OnClickListener click = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				{
					int val = 0;

					if (view == nBtn1) {
						val = 1;
					} else if (view == nBtn2) {
						val = 2;
					} else if (view == nBtn3) {
						val = 3;
					}else if (view == nBtn6) {
						val = 6;
					}
					else if (view == nBtn0) {
						val = 0;
					}

					sendAMBlemenu((byte) '0', val);

				}
			}
		};

//////////////////////
		nBtn0.setOnTouchListener(touch1);
		nBtn1.setOnTouchListener(touch1);
		nBtn2.setOnTouchListener(touch1);
		nBtn3.setOnTouchListener(touch1);
		nBtn6.setOnTouchListener(touch1);


//////////////////////
		nBtn0.setOnClickListener(click);
		nBtn1.setOnClickListener(click);
		nBtn2.setOnClickListener(click);
		nBtn3.setOnClickListener(click);
		nBtn6.setOnClickListener(click);


	}

	// ListView의 아이템이 클릭되면 호출되는 callback.

	class RowAdapter extends ArrayAdapter<String>{
		Context context;
		String[] data;
		int resID;

		public RowAdapter(Context c, int resID, String[] data){
			super(c, resID, data);
			this.context = c;
			this.data = data;
			this.resID = resID;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View row = convertView;
			if(row == null){
				Activity activity = (Activity) context;
				LayoutInflater inflater = activity.getLayoutInflater();

				row = inflater.inflate(R.layout.ambllelistview, null);

			}

			TextView textnoti = (TextView)row.findViewById(R.id.textnoti);
			textnoti.setText(data[position]);

			//row
			return row;


		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

	}

	@Override
	protected void onPause() {
		super.onPause();

		if(Info.m_Service != null)
			Info.m_Service._showhideLbsmsg(true);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if(Info.m_Service != null)
			Info.m_Service._showhideLbsmsg(false);

	}

	public void onDestroy() {
		super.onDestroy();

		close();

//		sendAMBlemenuclose();

	}

	public void onBackPressed() {
		super.onBackPressed();

		close();

		System.out
				.println("/////////////////// onBackPressed /////////////////");
	}

}
