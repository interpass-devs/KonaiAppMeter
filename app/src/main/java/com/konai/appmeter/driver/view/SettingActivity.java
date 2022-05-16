package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver.Dialog.Dlg_Num_Type;
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.util.FontFitTextView;
import com.kyleduo.switchbutton.SwitchButton;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SettingActivity extends Activity {

    private Context context;

    private boolean isChecked = true;

    private FontFitTextView bleText, oriText, gubunText, appControlTitle, appControlText;

    private SwitchButton ble, serialInabi, serialArtview, serialAtlan
                        , oriHorizontal, oriVertical
                        , gubunPersonal , gubunCorporate
                        , appControl;

    private Button cancelBtn, saveBtn;

    private String bleValue, oriValue, gubunValue, appControlValue, password;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public com.konai.appmeter.driver.setting.setting setting = new setting();
    private Dlg_Num_Type pwDlg;
    private Dialog basicDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //todo 1) 가로/세로 화면 붙이기 (x)
        //todo 2) 가로에도 앱 자동실행 목록 추가 (v)


        context = this;

        bleText = findViewById(R.id.ble_text);
        oriText = findViewById(R.id.ori_text);
        gubunText = findViewById(R.id.gubun_text);
        ble = findViewById(R.id.ble);
        serialInabi = findViewById(R.id.serial_inabi);
        serialArtview = findViewById(R.id.serial_artview);
        serialAtlan = findViewById(R.id.serial_atlan);
        oriHorizontal = findViewById(R.id.ori_horizontal);
        oriVertical = findViewById(R.id.ori_vertical);
        gubunPersonal = findViewById(R.id.gubun_personal);
        gubunCorporate = findViewById(R.id.gubun_corporate);
        appControlTitle = findViewById(R.id.app_control_title);
        appControlText = findViewById(R.id.app_control_text);
        appControl = findViewById(R.id.app_control);
        cancelBtn = findViewById(R.id.cancel_btn);
        saveBtn = findViewById(R.id.save_btn);

        ble.setOnClickListener(clickListener);
        serialInabi.setOnClickListener(clickListener);
        serialAtlan.setOnClickListener(clickListener);
        serialArtview.setOnClickListener(clickListener);
        cancelBtn.setOnClickListener(clickListener);
        saveBtn.setOnClickListener(clickListener);
        appControl.setOnClickListener(clickListener);


        pref = getSharedPreferences("env_setting", MODE_PRIVATE);
        editor = pref.edit();

        bleValue = pref.getString("ble_Status","-1");
        oriValue = pref.getString("ori_Status","-1");
        gubunValue = pref.getString("gubun_Status","1");
        appControlValue = pref.getString("app_control_Status","-1");

//        Log.d("appControlSetting_1>", bleValue+", "+oriValue+", "+gubunValue+", "+appControlValue);

        //연결설정
        switch (bleValue) {
            case "true": //블루투스
                com.konai.appmeter.driver.setting.setting.gUseBLE = true;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 0;
                bleText.setText("블루투스");
                ble.setChecked(true);
                break;
            case "1":  //아이나비
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 1;
                bleText.setText("시리얼 아이나비");
                serialInabi.setChecked(true);
                break;
            case "2": //아트뷰
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 2;
                bleText.setText("시리얼 아트뷰");
                serialArtview.setChecked(true);
                break;
            case "3": //아틀란
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 3;
                bleText.setText("시리얼 아틀란");
                serialAtlan.setChecked(true);
                break;
            default:
                com.konai.appmeter.driver.setting.setting.gUseBLE = true;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 0;
                bleText.setText("블루투스");
                ble.setChecked(true);
        }

        //해상도 설정
        switch (oriValue) {
            case "1": //가로
                com.konai.appmeter.driver.setting.setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                oriText.setText("가로");
                oriHorizontal.setChecked(true);
                break;
            case "2": //세로
                com.konai.appmeter.driver.setting.setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                oriText.setText("세로");
                oriVertical.setChecked(true);
                break;
            default:
                //todo: 먼저 display 의 사이즈로 판단하고 세팅해주기
                com.konai.appmeter.driver.setting.setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                oriText.setText("가로");
                oriVertical.setChecked(true);
                break;
        }

        //소속 설정
        switch (gubunValue) {
            case "1": //개인
                com.konai.appmeter.driver.setting.setting.gGubun = 1;
                gubunText.setText("개인");
                gubunPersonal.setChecked(true);
                break;
            case "2": //법인
                com.konai.appmeter.driver.setting.setting.gGubun = 2;
                gubunText.setText("법인");
                gubunCorporate.setChecked(true);
                break;
            default:
                com.konai.appmeter.driver.setting.setting.gGubun = 0;
                gubunText.setText("개인/법인 선택");
                gubunPersonal.setChecked(true);
                break;
        }

        //앱자동실행 설정
        switch (appControlValue) {
            case "true":
                com.konai.appmeter.driver.setting.setting.gAppCotrol = true;
                appControlText.setText("ON");
                appControl.setChecked(true);
                break;
            case "false":
                com.konai.appmeter.driver.setting.setting.gAppCotrol = false;
                appControlText.setText("OFF");
                appControl.setChecked(false);
                break;
            default:
                com.konai.appmeter.driver.setting.setting.gAppCotrol = false;
                appControlText.setText("OFF");
                appControl.setChecked(false);
                break;
        }
    }//onCreate..


//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (Info.m_Service != null) {
//            Info.m_Service._showhideLbsmsg(true);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Info.m_Service != null) {
            Log.d("m_Service_Resume", "not null");
            Info.m_Service._showhideLbsmsg(false);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.app_control_text:
//                    appControlTitle.
                    break;
                case R.id.ble:
                    serialInabi.setChecked(false);
                    serialArtview.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "true";
                    bleText.setText("블루투스");
                    break;
                case R.id.serial_inabi:
                    ble.setChecked(false);
                    serialArtview.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "1";
                    bleText.setText("시리얼 아이나비");
                    break;
                case R.id.serial_artview:
                    ble.setChecked(false);
                    serialInabi.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "2";
                    bleText.setText("시리얼 아트뷰");
                    break;
                case R.id.serial_atlan:
                    ble.setChecked(false);
                    serialInabi.setChecked(false);
                    serialArtview.setChecked(false);
                    bleValue = "3";
                    bleText.setText("시리얼 아틀란");
                    break;
                case R.id.ori_horizontal:
                    oriVertical.setChecked(false);
                    oriValue = "1";
                    oriText.setText("가로");
                    break;
                case R.id.ori_vertical:
                    oriHorizontal.setChecked(false);
                    oriValue = "2";
                    oriText.setText("세로");
                    break;
                case R.id.gubun_personal:
                    gubunCorporate.setChecked(false);
                    gubunValue = "1";
                    gubunText.setText("개인");
                    break;
                case R.id.gubun_corporate:
                    gubunPersonal.setChecked(false);
                    gubunValue = "2";
                    gubunText.setText("법인");
                    break;
                case R.id.app_control:

                    if (isChecked) {
                        appControl.setChecked(true);
                        appControlValue = "true";
                        appControlText.setText("ON");
                        isChecked = false;
                    }else {
                        appControl.setChecked(false);
                        appControlValue = "false";
                        appControlText.setText("OFF");
                        isChecked = true;
                    }
                    break;
                case R.id.cancel_btn:  //취소버튼

                    if (Info.m_Service != null) {
                        Info.m_Service._showhideLbsmsg(false);
                    }

                    finish();

                    break;
                case R.id.save_btn:  //저장버튼

                    //조건

                    //해상도 값
                    DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();

                    //me) 1. 저장하기 전 확인 다이얼로그
                    pwDlg = new Dlg_Num_Type(context, "password", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //get current date
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
                            final String today = sdf.format(date);
                            Log.d("today", today);

                            password = pwDlg.returnNumTypeVal();
                            Log.d("today_pw", password);

                            if (password.length() == 0 || password.length() < 4) {
                                Toast.makeText(SettingActivity.this, "비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }else {
                                if (password.equals(today)) {

                                    pwDlg.dismiss();

                                    //me) 2. 다얼로그로 다시 한번 저장 할건지 확인하기

//                                    Log.d("appControlSetting_2>", bleValue+", "+oriValue+", "+gubunValue+", "+appControlValue);

                                    setBasicDlg("환경설정을 저장 하시겠습까?","저장","취소", bleValue, oriValue, gubunValue, appControlValue);

                                }else {
                                    Toast.makeText(SettingActivity.this, "비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pwDlg.dismiss();
                        }
                    }, dm.widthPixels,dm.heightPixels
                    );

                    pwDlg.setCancelable(true);
                    pwDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    pwDlg.show();

                    break;
            }//switch


            Log.d("appControlSetting_chk",
                    "ble_Status: " + bleValue
                            + " | ori_Status: " + oriValue
                            + " | gubun_Status: " + gubunValue
                            + " | app_control_Status: " + appControlValue);
        }
    };





    private void setBasicDlg(String msg, String ok, String cancel, String bleValue, String oriValue, String gubunValue, String appControlValue) {
        LayoutInflater flater = getLayoutInflater();
        final View dView;
        dView = flater.inflate(R.layout.dlg_basic, null);
        final TextView message = (TextView)dView.findViewById(R.id.msg);
        final Button okBtn = (Button)dView.findViewById(R.id.okay_btn);
        final Button cancelBtn = (Button)dView.findViewById(R.id.cancel_btn);

        basicDlg = new Dialog(SettingActivity.this);
        basicDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        basicDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        basicDlg.setContentView(dView);
        basicDlg.setCancelable(false);

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        int h = dm.heightPixels;

        if (Build.VERSION.SDK_INT <= 25){
            w = (int)(w * 0.6);
            h = (int)(h * 0.8);
        }else {
            w = (int)(w * 0.9);
            h = (int)(h * 0.5);
        }

        message.setText(msg);
        if (cancel.equals("")){
            cancelBtn.setVisibility(View.GONE);
        }else {
            cancelBtn.setVisibility(View.VISIBLE);
        }

        okBtn.setText(ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d("appControlSetting_4>", bleValue+", "+oriValue+", "+gubunValue+", "+appControlValue);

                //preference 에 [최종저장]
                editor.putString("ble_Status", bleValue);
                editor.putString("ori_Status", oriValue);
                editor.putString("gubun_Status", gubunValue);
                editor.putString("app_control_Status", appControlValue);
                editor.commit();

                basicDlg.dismiss();
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                basicDlg.dismiss();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(basicDlg.getWindow().getAttributes());
        params.width = w;
        params.height = h;
        Window window = basicDlg.getWindow();
        window.setAttributes(params);
        basicDlg.show();
    }


    private void initializecontents(int ntp) {
        if (ntp == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_setting);
        }else {
            setContentView(R.layout.activity_setting);
        }
    }


}