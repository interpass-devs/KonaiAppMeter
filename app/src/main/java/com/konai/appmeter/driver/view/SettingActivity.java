package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
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

    private TextView bleText, oriText, gubunText, appControlTitle, appControlText;

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
//        setContentView(R.layout.activity_setting);

        initializecontents(setting.gOrient);

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
        oriHorizontal.setOnClickListener(clickListener);
        oriVertical.setOnClickListener(clickListener);
        gubunPersonal.setOnClickListener(clickListener);
        gubunCorporate.setOnClickListener(clickListener);
        appControl.setOnClickListener(clickListener);


        pref = getSharedPreferences("env_setting", MODE_PRIVATE);
        editor = pref.edit();

        bleValue = pref.getString("ble_Status","-1");
        oriValue = pref.getString("ori_Status","-1");
        gubunValue = pref.getString("gubun_Status","1");
        appControlValue = pref.getString("app_control_Status","-1");

//        Log.d("appControlSetting_1>", bleValue+", "+oriValue+", "+gubunValue+", "+appControlValue);

        //????????????
        switch (bleValue) {
            case "true": //????????????
                com.konai.appmeter.driver.setting.setting.gUseBLE = true;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 0;
                bleText.setText("????????????");
                ble.setChecked(true);
                serialInabi.setChecked(false);
                serialArtview.setChecked(false);
                serialAtlan.setChecked(false);
                break;
            case "1":  //????????????
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 1;
                bleText.setText("????????? ????????????");
                serialInabi.setChecked(true);
                ble.setChecked(false);
                serialArtview.setChecked(false);
                serialAtlan.setChecked(false);
                break;
            case "2": //?????????
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 2;
                bleText.setText("????????? ?????????");
                serialArtview.setChecked(true);
                ble.setChecked(false);
                serialInabi.setChecked(false);
                serialAtlan.setChecked(false);
                break;
            case "3": //?????????
                com.konai.appmeter.driver.setting.setting.gUseBLE = false;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 3;
                bleText.setText("????????? ?????????");
                serialAtlan.setChecked(true);
                ble.setChecked(false);
                serialInabi.setChecked(false);
                serialArtview.setChecked(false);
                break;
            default:
                com.konai.appmeter.driver.setting.setting.gUseBLE = true;
                com.konai.appmeter.driver.setting.setting.gSerialUnit = 0;
                bleText.setText("????????????");
                ble.setChecked(true);
                serialInabi.setChecked(false);
                serialArtview.setChecked(false);
                serialAtlan.setChecked(false);
        }


        //????????? ??????
        switch (oriValue) {
            case "1": //??????
                com.konai.appmeter.driver.setting.setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                oriText.setText("??????");
                oriHorizontal.setChecked(true);
                oriVertical.setChecked(false);
                break;
            case "2": //??????
                com.konai.appmeter.driver.setting.setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                oriText.setText("??????");
                oriVertical.setChecked(true);
                oriHorizontal.setChecked(false);
                break;
            default:
                //me: ?????? display ??? ???????????? ???????????? ???????????????

                if (com.konai.appmeter.driver.setting.setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    oriText.setText("??????");
                    oriVertical.setChecked(true);
                    oriHorizontal.setChecked(false);
                }else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    oriText.setText("??????");
                    oriHorizontal.setChecked(true);
                    oriVertical.setChecked(false);
                }
                break;
        }


        //?????? ??????
        switch (gubunValue) {
            case "1": //??????
                com.konai.appmeter.driver.setting.setting.gGubun = 1;
                gubunText.setText("??????");
                gubunPersonal.setChecked(true);
                gubunCorporate.setChecked(false);
                break;
            case "2": //??????
                com.konai.appmeter.driver.setting.setting.gGubun = 2;
                gubunText.setText("??????");
                gubunCorporate.setChecked(true);
                gubunPersonal.setChecked(false);
                break;
            default:
                com.konai.appmeter.driver.setting.setting.gGubun = 0;
                gubunText.setText("??????/?????? ??????");
                gubunPersonal.setChecked(true);
                gubunCorporate.setChecked(false);
                break;
        }

        //??????????????? ??????
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
                    ble.setChecked(true);
                    serialInabi.setChecked(false);
                    serialArtview.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "true";
                    bleText.setText("????????????");
                    break;
                case R.id.serial_inabi:
                    serialInabi.setChecked(true);
                    ble.setChecked(false);
                    serialArtview.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "1";
                    bleText.setText("????????? ????????????");
                    break;
                case R.id.serial_artview:
                    serialArtview.setChecked(true);
                    ble.setChecked(false);
                    serialInabi.setChecked(false);
                    serialAtlan.setChecked(false);
                    bleValue = "2";
                    bleText.setText("????????? ?????????");
                    break;
                case R.id.serial_atlan:
                    serialAtlan.setChecked(true);
                    ble.setChecked(false);
                    serialInabi.setChecked(false);
                    serialArtview.setChecked(false);
                    bleValue = "3";
                    bleText.setText("????????? ?????????");
                    break;
                case R.id.ori_horizontal:
                    oriHorizontal.setChecked(true);
                    oriVertical.setChecked(false);
                    oriValue = "1";
                    oriText.setText("??????");
                    break;
                case R.id.ori_vertical:
                    oriVertical.setChecked(true);
                    oriHorizontal.setChecked(false);
                    oriValue = "2";
                    oriText.setText("??????");
                    break;
                case R.id.gubun_personal:
                    gubunCorporate.setChecked(false);
                    gubunValue = "1";
                    gubunText.setText("??????");
                    break;
                case R.id.gubun_corporate:
                    gubunPersonal.setChecked(false);
                    gubunValue = "2";
                    gubunText.setText("??????");
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
                case R.id.cancel_btn:  //????????????

                    if (Info.m_Service != null) {
                        Info.m_Service._showhideLbsmsg(false);
                    }

                    finish();

                    break;
                case R.id.save_btn:  //????????????

                    //??????

                    //????????? ???
                    DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();

                    //me) 1. ???????????? ??? ?????? ???????????????
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
                                Toast.makeText(SettingActivity.this, "??????????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                            }else {
                                if (password.equals(today)) {

                                    pwDlg.dismiss();

                                    //me) 2. ??????????????? ?????? ?????? ?????? ????????? ????????????

//                                    Log.d("appControlSetting_2>", bleValue+", "+oriValue+", "+gubunValue+", "+appControlValue);

                                    setBasicDlg("??????????????? ?????? ????????????????","??????","??????", bleValue, oriValue, gubunValue, appControlValue);

                                }else {
                                    Toast.makeText(SettingActivity.this, "??????????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
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





    private void setBasicDlg(String msg, String ok, String cancel, String sbleValue, String soriValue, String sgubunValue, String sappControlValue) {
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

                //preference ??? [????????????]
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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //20220531
            setContentView(R.layout.activity_setting);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //20220531
            setContentView(R.layout.activity_setting_h);
        }
    }


}