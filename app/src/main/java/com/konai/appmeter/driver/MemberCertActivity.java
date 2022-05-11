//20211216 tra..sh
//20211220 tra..sh
//20211229 tra..sh
package com.konai.appmeter.driver;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.TextViewCompat;

import com.konai.appmeter.driver.Dialog.Dlg_Env_setting;
import com.konai.appmeter.driver.Dialog.Dlg_Select_Driver;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.Suburbs;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.socket.UDPClientUtil;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;
import com.konai.appmeter.driver.util.ConnectionHtml;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

//import android.support.annotation.NonNull;
//import android.support.v4.content.FileProvider;
//import android.support.v4.widget.TextViewCompat;

public class MemberCertActivity extends Activity {

    com.konai.appmeter.driver.setting.setting setting = new setting();

    //todo: 20211117
    public Dlg_Env_setting dlg_env_setting;   //환경설정 다이얼로그
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Boolean isClicked = true;
    Context context;
    //todo: end

    private boolean m_bSkipLogin = false; //false;
    private final int mMaxFail = 10; //20211109
    private String CertURL = setting.BASEDOMAIN;
    private String Domain = "";
    private String ResultData;
    private String preResultData; //20211109
    private String prePhoneno = "0"; //20211109
    private String blueOn, blueOff;
    private String verticalOn, horizontalOn;
    private String totData;
    private String cGroup;

    public Dlg_Select_Driver dlg_select_driver;
    LinearLayout check_driver_info;
    EditText inpt_name;
    EditText inpt_carno;
    EditText inpt_certino;
    Boolean editTouch = false;

    TextView driver_name, driver_num, connStatus;

    String logtag = "logtag";
    Button btn_ok;
    Button btn_cancel;
    LinearLayout auto_login_checkbox_layout;
    CheckBox auto_login_checkbox;
    String autoLoginVal, gubunVal;

    private FrameLayout frame1 = null;
    private FrameLayout frame2 = null;
    int dialog_idx = 0;

    ProgressDialog dlgbox; //20210823
    private static Thread MainThread = null; //20210823
    private BroadcastReceiver mReceiver = null; //20210823

    //20210823 getphonenum
    class MainThreads implements Runnable {
        public void run() {

            try {
                boolean certifications = false;
                int nFail = 0;

                initHandler.sendEmptyMessage(1); //20211109 TODO 통신확인중

                while (!Thread.currentThread().isInterrupted()) { //20210823

                    if (checknetwork()) {

                        Thread.sleep(1 * 1000);

                        nFail++;
                        Log.d("last_login_info", "a " + nFail);
                        if (setting.phoneNumber.equals("0")) {

                            getPhonenum();

                            Thread.sleep(1 * 1000);
                            nFail++;
                            Log.d("last_login_info", "b " + nFail);
                        } else {
                            chkCertificatiojn();

//                            Log.d("CERTDATA", ResultData);
                            if (ResultData.equals("Fail")) {
                                Thread.sleep(3 * 1000);
                                nFail += 3;
//                                Log.d("last_login_info", "c " + nFail);
//                                continue;
                            } else {
                                initHandler.sendEmptyMessage(0);
//20211229
                                initHandler.sendEmptyMessage(2); //20211109 TODO 통신확인성공

                                break;
                            }


                        }

                    } else {
                        nFail++;

//                        Log.d("last_login_info", "d " + nFail);
                    }
//20211109 통신 실패 이전에 저장된값으로 로그인진행
                    if (nFail > mMaxFail) {
//                        Log.d(logtag+"확인","ㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅㅅ");
                        initHandler.sendEmptyMessage(3); //20211109 TODO 통신확인실패

                        nFail = 0;

                        get_predata(2);

                        ResultData = preResultData;

                        if (ResultData.equals("Fail") == false) {
                            if (setting.phoneNumber.equals("0"))
                                setting.phoneNumber = prePhoneno;

                            m_bSkipLogin = true;
                            initHandler.sendEmptyMessage(0);
//                            Log.d("last_login_info", "------" + ResultData);
                            break;
                        }

                    }

                    Thread.sleep(1 * 1000);

                }
                //Log.e("cert res" , certifications +"");

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//20210311  scape setContentView(R.layout.activity_certification);
//20210311 scape        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //20201110
///20210311        initializecontents(getResources().getConfiguration().orientation);

        //todo: 20211117
        context = this;

        pref = getSharedPreferences("env_setting", MODE_PRIVATE);

        editor = pref.edit();

        String getPrefData_ble = pref.getString("ble_Status", "-1");
        String getPrefData_ori = pref.getString("ori_Status", "-1");
        gubunVal = pref.getString("gubun_Status", "1"); //20211229
        autoLoginVal = pref.getString("auto_login_Status", "-1");
//        Log.d("getPrefData_ble", getPrefData_ble);
//        Log.d("getPrefData_ori", getPrefData_ori);  //1-가로, 2-세로
//        Log.d("getPrefData_gubun", gubunVal);  //1-개인, 2-법인
//        Log.d("getPreData_autoLogin",autoLoginVal);  //1-자동로그인(O), 2-자동로그인(X)

        if (true) {
            Info.TESTMODE = false; //true; //false;
            Info.TIMSUSE = true; //true; //false; //true; //send tims.
            Info.TIMSUSE_TEST = false; //send tims.
            Info.SENDDTG = true; //send DTG to interpass
            Info.USEDRIVESTATEPOWEROFF = true; //true; //false; //20210407 승차상태의 강제poweroff
            Info.USEDBRUNDATA = true; //20224015
            Info.USEDBLOCATIONDATA = false; //20220415

            if (getPrefData_ori.equals("2")) {
                Log.d("ori", "세로");
                setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; //for smartphone

            } else if (getPrefData_ori.equals("1")) {
                Log.d("ori", "가로");
                setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

            } else {
                Log.d("ori_default", "세로_기본");
//                setting.gOrient =  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; //for smartphone
//                initializecontents(0); //기본 세로로 설정

                if (true) {
                    WindowManager m_WindowManager;
                    DisplayMetrics m_matrix = new DisplayMetrics();
                    m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    m_WindowManager.getDefaultDisplay().getMetrics(m_matrix);
                    DisplayMetrics matrix = new DisplayMetrics();
                    m_WindowManager.getDefaultDisplay().getMetrics(matrix);
                    Log.d("metrix", "" + m_matrix.widthPixels + " " + m_matrix.heightPixels + "\ndenst " + this.getResources().getDisplayMetrics().density +
                            "\nOS " + Build.VERSION.RELEASE + "\nmodel " + Build.MODEL + "\ncompany " + Build.MANUFACTURER + "\nsdk " + Build.VERSION.SDK_INT);
//아트뷰 1024 538
                    if (m_matrix.widthPixels > m_matrix.heightPixels) {
                        setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; //for navi.
                    } else
                        setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; //for smartphone
                }
//                initializecontents(0); //기본 세로로 설정
            }


            if (getPrefData_ble.equals("true")) {
                setting.gUseBLE = true; //false;   //true- 블루투스
                setting.gSerialUnit = 0;
            } else if (getPrefData_ble.equals("1")) {  //시리얼- 아이나비
                setting.gUseBLE = false; //false;
                setting.gSerialUnit = 1;
            } else if (getPrefData_ble.equals("2")) {   //시리얼- 아트뷰
                setting.gUseBLE = false; //false;
                setting.gSerialUnit = 2;
            } else if (getPrefData_ble.equals("3")) {   //시리얼- 아틀란
                setting.gUseBLE = false;
                setting.gSerialUnit = 3;
            } else {
                setting.gUseBLE = true; //false;   //true- 블루투스/ false- 시리얼
                setting.gSerialUnit = 0;
            }


            if (gubunVal.equals("1")) {   //개인
                setting.gGubun = 1;
            } else if (gubunVal.equals("2")) {  //법인
                setting.gGubun = 2;
            } else {
                setting.gGubun = 0;  //기본설정
            }


            if (autoLoginVal.equals("1")) {  //자동로그인(O)
                setting.gAutoLogin = 1;
            } else if (autoLoginVal.equals("2")) {  //자동로그인(X)
                setting.gAutoLogin = 2;
            } else {
                setting.gAutoLogin = 0;
            }


            Suburbs.mSuburbOK = true; //시외
        }


        if (getPrefData_ble.equals("-1") || getPrefData_ori.equals("-1")) {
            dlg_env_setting = new Dlg_Env_setting(context
                    , new View.OnClickListener() {
                @Override
                public void onClick(View v) {  //okBtn
                    String getBlueStatus = dlg_env_setting.return_blueValue();
                    String getOriStatus = dlg_env_setting.return_oriValue();
                    String getGubunStatus = dlg_env_setting.return_gubunValue();
                    //todo: 20220124
//                    String getModemStatus = dlg_env_setting.return_modemValue();
//                    Log.d("final_value", getBlueStatus+", "+getOriStatus+", "+getGubunStatus+", "+getModemStatus);
                    dlg_env_setting.dismiss();

                    //todo: 블루투스 상태저장 및 변경
                    editor.putString("ble_Status", getBlueStatus);
                    editor.putString("ori_Status", getOriStatus);
                    editor.putString("gubun_Status", getGubunStatus);
//                    editor.putString("modem_Status", getModemStatus);
                    //todo: end
                    editor.commit();

                    switch (getBlueStatus) {
                        case "true":
                            setting.gUseBLE = true;
                            setting.gSerialUnit = 0;
                            break;
                        case "1":   //아이나비
                            setting.gUseBLE = false;
                            setting.gSerialUnit = 1;
                            break;
                        case "2":  //아트뷰
                            setting.gUseBLE = false;
                            setting.gSerialUnit = 2;
                            break;
                        case "3":  //아틀란
                            setting.gUseBLE = false;
                            setting.gSerialUnit = 3;
                    }//switch..

///////////////////
                    switch (getOriStatus) {
                        case "1":
                            setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            initializecontents(setting.gOrient); //20211203 1);
                            break;
                        case "2":
                            setting.gOrient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            initializecontents(setting.gOrient); //20211203 0);
                            break;
                    }


                    switch (getGubunStatus) {
                        case "1":   //개인
                            setting.gGubun = 1;
                            auto_login_checkbox_layout.setVisibility(View.VISIBLE);
                            break;
                        case "2":   //법인
                            setting.gGubun = 2;
                            auto_login_checkbox_layout.setVisibility(View.GONE);
                            break;
                    }

                    //todo: 20220124
//                    switch (getModemStatus){
//                        case "1":   //모뎀-일반
//                            setting.gModem = 1;
//                            break;
//                        case "2":   //모뎀-우리넷
//                            setting.gModem = 2;
//                            break;
//                        case "3":   //모뎀-에이엠
//                            setting.gModem = 3;
//                            break;
//                    }
                    //todo: end

////////////////////

                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {  //cancelBtn
                    dlg_env_setting.dismiss();
                }
            }
            );

            dlg_env_setting.setCancelable(true);
            dlg_env_setting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dlg_env_setting.show();


            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            width = (int) (width * 0.9);
//            height = (int)(height * 0.9);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dlg_env_setting.getWindow().getAttributes());
            lp.width = width;
//            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.height = height;
            Window window = dlg_env_setting.getWindow();
            window.setAttributes(lp);


            //todo: end
        } else {
            //todo: 20211201
            // 가로 세로값 가져오기
            /**
             //            editor.putString("ble_Status", getBlueStatus);
             //            editor.putString("ori_Status", getOriStatus);
             if (getPrefData_ori.equals("1")){  //가로
             set_frame_orient(1);
             }else if (getPrefData_ori.equals("2")){ //세로
             set_frame_orient(0);
             }else {
             //do nothing
             }
             **/
        }


///////////////////////

        if (true) {
            if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setting.gUseBLE = true;
            } else {

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }
            Log.d("set_ori", "111");
            initializecontents(setting.gOrient);
        } else
//            initializecontents(getResources().getConfiguration().orientation);
            initializecontents(setting.gOrient);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //20201207

        inpt_name.setEnabled(false);
        inpt_carno.setEnabled(false);
        inpt_certino.setEnabled(false);

        btn_ok.setEnabled(false);
        // btn_ok.setBackgroundColor(Color.parseColor("#2e2e6a"));
//        btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);  //todo: 20220223
        btn_ok.setBackgroundResource(R.drawable.selected_btn_touched_yellow); //todo: 20220223

        Info.init_SQLHelper(this);

        TelephonyManager systemService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String stmp = ChangePhoneNumber(systemService.getLine1Number());

        if (stmp.equals("") && setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

//            setting.phoneNumber = "01040001338"; //인천""01278107115"; //인유진 "01037620057"; //"01212345678";
//            setting.phoneNumber = "01040001338";
            ;
        } else if (stmp.equals("")) {
//            setting.phoneNumber = "01050564465";

//for tims인증번호
//              setting.phoneNumber = "01011113311";
//            setting.phoneNumber = "01011113312";
///            setting.phoneNumber = "01212345678";
        } else if (stmp.equals("+8613922819379")) //for temp.
        {

            setting.phoneNumber = "01074591665";

        } else {

            setting.phoneNumber = stmp;

        }

//        if(setting.phoneNumber.equals("01036610720"))
///            setting.phoneNumber = "01040001338";

        //todo: 20211230
        //m_bSkipLogin
        //true 하면 강제로 로그인됌
        if (false) //m_bSkipLogin)
        {
            setting.phoneNumber = "01036610720";
        }
//        Toast.makeText(MemberCertActivity.this, "" + setting.phoneNumber, Toast.LENGTH_SHORT).show();

//        if(setting.gUseBLE == false)
///            setting.phoneNumber = "01236610720";

//20210823
        registerReceiver();
        dlgbox = new ProgressDialog(this);
        dlgbox.setTitle("잠시만 대기하여 주세요");
        dlgbox.setMessage("파일 다운로드 중입니다.");
        dlgbox.setIndeterminate(true);
        dlgbox.setCancelable(true);
//////
        if (Info.REPORTREADY) {
//            Toast.makeText(MemberCertActivity.this, "전화번호 가져오기 실패!", Toast.LENGTH_SHORT).show();
            Info._displayLOG(Info.LOGDISPLAY, "회원 인증 진행", "");
        }

        get_predata(1);

        if(m_bSkipLogin == false)
        {

            MainThread = new Thread(new MainThreads());
            MainThread.start();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("detect_user_up", event.toString());
            setting.gUserAction = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d("detect_user_down", event.toString());
            setting.gUserAction = false;
        }


        return super.onTouchEvent(event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (dialog_idx != 0) {
            dlg_select_driver.RecyclerDriverSet();

        }

        if (true) {
            WindowManager m_WindowManager;
            DisplayMetrics m_matrix = new DisplayMetrics();
            m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            m_WindowManager.getDefaultDisplay().getMetrics(m_matrix);
            DisplayMetrics matrix = new DisplayMetrics();
            m_WindowManager.getDefaultDisplay().getMetrics(matrix);
            Log.d("metrix", "" + m_matrix.widthPixels + " " + m_matrix.heightPixels + "\ndenst " + this.getResources().getDisplayMetrics().density +
                    "\nOS " + Build.VERSION.RELEASE + "\nmodel " + Build.MODEL + "\ncompany " + Build.MANUFACTURER + "\nsdk " + Build.VERSION.SDK_INT);
        }
    }//onResume

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);


        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("configurechange", "가로");

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("configurechange", "세로");

        }

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);
    }

    //20210823
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (MainThread != null)
            MainThread.interrupt();

        unregisterReceiver();

    }
////////////


    //로그인 [확인]버튼 화면
    private void initializecontents(int nTP) {
        Log.d("nTP_ori", "MembercertActivity");
        Log.d("nTP_ori", nTP + "");
//20211203        if(nTP == 0)
        if (nTP == 1) {
            Log.d("set_ori", "세로설정");
            setContentView(R.layout.activity_membercert_v);
            set_frame_orient(0);
//version 1.56            setting.UPFILENAME = "appmeter.apk"; //20210823

        } else {
            Log.d("set_ori", "가로설정");
            setContentView(R.layout.activity_membercert_h);
            set_frame_orient(1);
//            setting.UPFILENAME = "appmeter_l.apk"; //20210823 paju.
////version 1.56            setting.UPFILENAME = "appmeter.apk"; //20210823 cheonan.
        }

        //todo: 20220223
        btn_ok.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // btn_ok.setBackgroundColor(Color.parseColor("#2e2e6a"));
//                    btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
                    btn_ok.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // btn_ok.setBackgroundColor(Color.parseColor("#2e2eae"));
//                    btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                    btn_ok.setBackgroundResource(R.drawable.yellow_gradi_btn);
                }
                return false;
            }
        });
        //todo: end..


        //todo: 20220119
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean updateRes = false;
                if (m_bSkipLogin) {
                    updateRes = true;
                    AMBlestruct.AMLicense.drivername = Info.G_driver_name; //20201110;
                    AMBlestruct.AMLicense.taxinumber = inpt_carno.getText().toString().trim(); //20201110
                    AMBlestruct.AMLicense.drivernum = Info.G_driver_num;
                    AMBlestruct.AMLicense.licensecode = "000000000000".substring(0, 12 - Info.G_license_num.length())
                            + Info.G_license_num + "";
                } else if (inpt_carno.getText().length() < 9) {
                    Toast.makeText(context, "차량번호를 다시 입력해주세요.\n\nex) 서울00가0000", Toast.LENGTH_SHORT).show();
                } else if (inpt_carno.getText().toString().contains("null")) {
                    Toast.makeText(context, "차량번호를 다시 입력해주세요.\n\nex) 서울00가0000", Toast.LENGTH_SHORT).show();
                } else {
                    updateRes = updateCertification(Info.G_driver_name, inpt_carno.getText().toString(), Info.G_license_num);  //me: 서버로 값을 보냄
                    // boolean updateRes = updateCertification(inpt_name.getText().toString(), inpt_carno.getText().toString(), driver_num.getText().toString());
                    AMBlestruct.AMLicense.drivername = Info.G_driver_name; //20201110;
                    AMBlestruct.AMLicense.taxinumber = inpt_carno.getText().toString().trim(); //20201110
                    AMBlestruct.AMLicense.drivernum = Info.G_driver_num;
                    AMBlestruct.AMLicense.licensecode = "000000000000".substring(0, 12 - Info.G_license_num.length())
                            + Info.G_license_num + "";

                }
                //todo: end

                AMBlestruct.AMLicense.phonenumber = setting.phoneNumber.trim();

//22010827
                if (Info.G_license_num.equals("") == false) {

                    AMBlestruct.AMLicense.timslicense = Info.G_license_num;
                } else
                    AMBlestruct.AMLicense.timslicense = "000000000";
                AMBlestruct.AMLicense.timstaxinum = AMBlestruct.AMLicense.taxinumber;

                if(Info.TIMSUSE_TEST) {
                    AMBlestruct.AMLicense.companynum = AMBlestruct.AMLicense.companynumtmp;
                    AMBlestruct.AMLicense.timstaxinum = AMBlestruct.AMLicense.timstaxinumtmp;
                    AMBlestruct.AMLicense.timslicense = AMBlestruct.AMLicense.timslicensetmp;
                }

//                Log.d("timslicense", "- " + AMBlestruct.AMLicense.timslicense);
                Log.d("login_info", "- " + AMBlestruct.AMLicense.timslicense + " - " + AMBlestruct.AMLicense.timstaxinum + AMBlestruct.AMLicense.companynum);
                Log.d("login_info_2", "- " + Info.G_license_num + " " + Info.G_driver_num + " " + AMBlestruct.AMLicense.licensecode);

/////////////////////
                if (updateRes) {
                    try {
                        if (m_bSkipLogin == false) { //20211109
                            SharedPreferences sf = getSharedPreferences("last_login_info", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sf.edit();
                            editor.putString("driver_name", Info.G_driver_name);
                            editor.putString("driver_num", Info.G_driver_num);
                            editor.putString("license_num", Info.G_license_num);
                            editor.putString("car_no", AMBlestruct.AMLicense.taxinumber);
                            editor.putString("phoneno", setting.phoneNumber); //20211109
                            editor.putString("resultdata", preResultData); //20211109
                            editor.commit();
//                            Log.d("last_login_info", setting.phoneNumber + " " + preResultData);
                        }
                    } catch (Exception e) {

                    }
                    totData = Info.sqlite.getTotalKey();
                    //Log.e("total Date is", totData);
                    if (totData.equals("") || totData.equals("0")) {
                        Info.sqlite.insertTotalData();
                    }

                    //todo: 20220104
//                    update_centerapk(2);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Log.d(logtag + "_lead_main", "to main activity");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MemberCertActivity.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //todo: 20211230
        auto_login_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //me: 로그인 Handler
                //me: 로그인 상태값 저장하기
                if (isChecked) {
//                    Toast.makeText(context, "auto login clicked", Toast.LENGTH_SHORT).show();
                    editor.putString("auto_login_Status", "1");
                    setting.gAutoLogin = 1;
                    autoLoginVal = "1";
                    editor.commit();
                } else {
//                    Toast.makeText(context, "auto login canceled", Toast.LENGTH_SHORT).show();
                    editor.putString("auto_login_Status", "2");
                    setting.gAutoLogin = 2;
                    autoLoginVal = "2";
                    editor.commit();
                }
            }
        });


    }


    private void set_frame_orient(int tp) {
//////////////////////
        View viewframe1 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1); // 1. 기반이 되는 FrameLayout
        if (frame1.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame1.removeViewAt(0);

        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe1 = inflater.inflate(R.layout.membercertframe1, frame1, true);    //me: 로그인 화면

        check_driver_info = viewframe1.findViewById(R.id.check_driver_info);
        inpt_name = viewframe1.findViewById(R.id.inpt_name);
        inpt_carno = viewframe1.findViewById(R.id.inpt_carno);
        inpt_certino = viewframe1.findViewById(R.id.inpt_certino);

        driver_name = viewframe1.findViewById(R.id.driver_name);
        driver_num = viewframe1.findViewById(R.id.driver_num);


        inpt_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setting.editTouch = true;
                    Log.d("setting.editTouch!!", setting.editTouch + "");
                }
            }
        });

        inpt_carno.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setting.editTouch = true;
                    Log.d("setting.editTouch!!", setting.editTouch + "");
                }
            }
        });

        //me: 운전자 등록 확인 다이얼로그
        check_driver_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setting.editTouch = true; //20220303 tra..sh

                dialog_idx++;
                dlg_select_driver = new Dlg_Select_Driver(MemberCertActivity.this);
                dlg_select_driver.setCancelable(false);
                dlg_select_driver.show();

                DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dlg_select_driver.getWindow().getAttributes());

                if (Build.VERSION.SDK_INT <= 25) {
                    width = (int) (width * 0.6);
                    height = (int) (height * 0.8);

                    lp.width = width;
//                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = height;

                } else {
                    width = (int) (width * 0.9);
                    height = (int) (height * 0.7);

                    lp.width = width;
                    lp.height = height;
                }


                Window window = dlg_select_driver.getWindow();
                window.setAttributes(lp);
                dlg_select_driver.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dlg_select_driver.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (null != Info.G_driver_name) {
                            driver_name.setText(Info.G_driver_name);
                            driver_num.setText(Info.G_driver_num);
                            inpt_name.setText(Info.G_driver_name);
                            inpt_certino.setText(Info.G_license_num); //20210901
                        }
                    }
                });
            }
        });

//////////////////////
/*
        View viewframe2 = null;

        frame2 = (FrameLayout) findViewById(R.id.frame2); // 1. 기반이 되는 FrameLayout
        if (frame2.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame2.removeViewAt(0);
        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe2 = inflater.inflate(R.layout.membercertframe2, frame2,true);
*/
        btn_ok = findViewById(R.id.btn_ok);

        //todo: 20211230
        auto_login_checkbox = findViewById(R.id.auto_login_checkbox);
        auto_login_checkbox_layout = findViewById(R.id.auto_login_checkbox_layout);

        if (gubunVal.equals("1")) {  //개인택시 일 때만
            auto_login_checkbox_layout.setVisibility(View.VISIBLE);
            if (autoLoginVal.equals("1")) {  //자동로그인 체크됐을 때만
                auto_login_checkbox.setChecked(true);
            } else {
                auto_login_checkbox.setChecked(false);
            }
        } else {
//            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
//            auto_login_checkbox.setEnabled(false);
            auto_login_checkbox_layout.setVisibility(View.GONE);
        }
        //todo: end

        connStatus = findViewById(R.id.tv_conn_status);

        if (Build.VERSION.SDK_INT <= 25) //20210823 8.0
        {
            inpt_name.setTextSize((float) (3 * setting.gTextDenst));
            inpt_carno.setTextSize((float) (3 * setting.gTextDenst));

            TextView textView10 = viewframe1.findViewById(R.id.textView10);
            textView10.setTextSize(setting.gTextDenst);

        }

    }

    public boolean updateCertification(String name, String carno, String certNo) {

        Domain = "/updateMember?p=" + setting.phoneNumber + "&carno=" + carno + "&dname=" + name + "&drvReg=" + certNo +
                "&biz_no=" + AMBlestruct.AMLicense.companynum;

        Thread NetworkThread = new Thread(new NetworkThreads());
        NetworkThread.start();

        try {
            NetworkThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ResultData.equals("Fail") || ResultData == null) {
            Log.d(logtag + "resultData", ResultData);
            return false;
        } else {
            return true;
        }
    }

    Handler initHandler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                String parseRes = parsingCert(ResultData);
//                Log.d("resultData", parseRes+"");  //y

                if (parseRes.equals("n") || ResultData == null) {

                    if (Info.REPORTREADY) {

                        Info._displayLOG(Info.LOGDISPLAY, "회원 인증 진행 실패 앱 종료", "");
                    }

                    Toast.makeText(MemberCertActivity.this, "인증되지 않은 번호입니다. " + setting.phoneNumber, Toast.LENGTH_SHORT).show();
//                finish();
                } else {
                    inpt_name.setEnabled(true);
                    inpt_carno.setEnabled(true);
                    inpt_certino.setEnabled(true);
                    btn_ok.setEnabled(true);
                    //btn_ok.setBackgroundColor(Color.parseColor("#2e2eae"));
//                    btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_bg); //todo: 20220223
                    btn_ok.setBackgroundResource(R.drawable.yellow_gradi_btn);    //todo: 20220223

                    if (Info.REPORTREADY) {
                        Info._displayLOG(Info.LOGDISPLAY, "회원 인증 성공", "");
                        Toast.makeText(MemberCertActivity.this, "회원 인증 성공. " + setting.phoneNumber.substring(0, 7) + "****",
                                Toast.LENGTH_SHORT).show();
                    }

                    if (true) //Info.REPORTREADY)
                    {
                        Info._displayLOG(Info.LOGDISPLAY, "현재앱미터 버전  Local" + Info.APP_VERSION + " Server" + Info.SV_APP_VERSION +
                                " cserver" + Info.SV_APP_CVERSION, "");

                        //todo: 20220104
//                        Info.APP_VERSION = 1.0;
//                        Info.SV_APP_VERSION = 2.0;

                        if (false) {
                            Info.AREA_CODE = "수원";
                            Info._displayLOG(Info.LOGDISPLAY, "앱미터업데이트 진행 ", "");
                            setting.editTouch = true;  //20220126
                            update_centerapk(2);

                        } else if (Info.APP_VERSION < Info.SV_APP_VERSION || Info.APP_VERSION < Info.SV_APP_CVERSION) {
                            Info._displayLOG(Info.LOGDISPLAY, "앱미터업데이트 진행 ", "");
                            setting.editTouch = true;  //20220126
                            update_centerapk(2);
                        }//20220126

                    }
//                    initHandler.sendEmptyMessage(2); //20211229 20211109 TODO 통신확인성공
                }

            } else if (msg.what == 1) //20211109 TODO 통신확인중입니다.
            {

                Log.d("last_login_info", "통신확인증");
                connStatus.setVisibility(View.VISIBLE);
                connStatus.setText("통신확인중입니다...");

            } else if (msg.what == 2) //20211109 TODO 통신확인성공
            {

                Log.d("last_login_info", "통신확인성공");
                connStatus.setVisibility(View.VISIBLE);
                connStatus.setText("통신확인성공!");

                //todo: 20211230
                if (gubunVal.equals("1")) {  //개인택시 일 때만

                    Toast.makeText(context, "개인택시", Toast.LENGTH_SHORT).show();

                    if (autoLoginVal.equals("1")) {  //자동로그인 체크됐을 때

                        Log.d("detect_user_gUserAction", setting.gUserAction + "");
                        initHandler.sendEmptyMessageDelayed(10, 3000);
                    }
                }
            } else if (msg.what == 10) //todo: 20211230 개인택시 자동로그인
            {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//                finish();

                if (setting.editTouch == true) {
                    Log.d("detect_user_gUserAction", setting.gUserAction + "");
//                            initHandler.removeMessages(10);

                } else {

                    btn_ok.performClick(); //20211229
                }


            } else if (msg.what == 3) //20211109 TODO 통신확인실패
            {
                Log.d("last_login_info", "통신확인실패");
                connStatus.setVisibility(View.VISIBLE);
                connStatus.setText("통신확인실패 X");
//              자동로그인 확인 후 btn
                //todo: 20211230
                if (gubunVal.equals("1")) {  //개인택시 일 때만

//                    Toast.makeText(context, "개인택시", Toast.LENGTH_SHORT).show();

                    if (autoLoginVal.equals("1")) {   //자동로그인 체크됐을 때

                        Log.d("setting.gUserAction>>", "edittouch" + setting.gUserAction + "");
                        Log.d("setting.edittouch>>", setting.editTouch + "");

                        initHandler.removeMessages(10);
                        initHandler.sendEmptyMessageDelayed(10, 3000);

                    }
                }

            }
        }

    };

    public void chkCertificatiojn() {
        //폰번호를 가져온다.
        Domain = "/certification?p=" + setting.phoneNumber;

/*
        while(true)
        {

            if(checknetwork())
                break;

            try {
                Thread.sleep(1 * 1000);


            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
*/

        Thread NetworkThread = new Thread(new NetworkThreads());
        NetworkThread.start();

        try {
            NetworkThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //20210823
    public boolean checknetwork() {
        try {
            boolean isMobileAvail = false;
            boolean isMobileConn = false;
            boolean isWifiAvail = false;
            boolean isWifiConn = false;
            int nType = 0;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
/* 20210601
			NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(ni != null)
			{

				isWifiAvail = ni.isAvailable();
				isWifiConn = ni.isConnected();
			}
			ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if(ni != null) {

				isMobileAvail = ni.isAvailable();
				isMobileConn = ni.isConnected();
			}
*/

            NetworkInfo ni = cm.getActiveNetworkInfo();
            nType = ni.getType();
            if (nType == ConnectivityManager.TYPE_WIFI) {

                isWifiAvail = ni.isAvailable();
                isWifiConn = ni.isConnected();
            } else {

                isMobileAvail = ni.isAvailable();
                isMobileConn = ni.isConnected();
            }

//			String status = "WiFi\nAvail = " + isWifiAvail + "\nConn = "
//					+ isWifiConn + "\nMobile\nAvail = " + isMobileAvail
//					+ "\nConn = " + isMobileConn + "\n";

//			util.log(EventInfo.ACTIVITY.MAINPOPUP, status);

            //Log.d("순서", "WIFI USE");

            if (isMobileConn == true || isWifiConn == true) {
                Log.d("isMobileConn", "isMobileConn true");
                return true;
            } else {
                Log.d("isMobileConn", "isMobileConn false");
            }
            /*
             * if (isWifiConn == false) { return false; }
             */
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    class NetworkThreads implements Runnable {
        public void run() {
            HttpURLConnection conn = null;
            StringBuilder jsonData = new StringBuilder();

            if (Info.REPORTREADY) {

                Info._displayLOG(Info.LOGDISPLAY, " NetworkThreads " + CertURL + Domain, "");

            }

            try {
                URL url = new URL(CertURL + Domain);
                conn = (HttpURLConnection) url.openConnection();

                if (Info.REPORTREADY) {

                    Info._displayLOG(Info.LOGDISPLAY, "회원 인증, 요금체계수신 주소 " + CertURL + Domain, "");

                }

                if (conn != null) {
                    conn.setConnectTimeout(2000);
                    conn.setUseCaches(false);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader((conn.getInputStream()), "UTF-8"));

                        for (; ; ) {
                            String line = br.readLine();

                            if (line == null) {
                                break;
                            }
                            jsonData.append(line + "\n");

                            if (Info.REPORTREADY) {

                                Info._displayLOG(Info.LOGDISPLAY, "수신 data " + line, "");

                            }

                        }
                        br.close();
                    }
                    conn.disconnect();
                    ResultData = jsonData.toString();
                } else {
                    ResultData = "Fail";
                }

                if (Info.REPORTREADY) {

                    Info._displayLOG(Info.LOGDISPLAY, "회원 인증, 요금체계수신 종료 " + CertURL + Domain, "");

                }

            } catch (Exception e) {
                if (conn != null)
                    conn.disconnect();

                ResultData = "Fail";
                e.printStackTrace();
            }
        }
    }

    public String parsingCert(String json) {

        if (json.equals("Fail")) {

            return "n";

        }

//        Log.d("CERTDATA", json);
        String certRes = "";
        try {
            JSONObject jsonObject = new JSONObject(json);

            String cert = jsonObject.getString("result");
            String mSeq = jsonObject.getString("mSeq");
            String tel = jsonObject.getString("telephone");
            String carno = jsonObject.getString("carno");
            String drvname = jsonObject.getString("drvname");
            String drvReg = jsonObject.getString("drvRegno");
            String group_code = jsonObject.getString("group_code");

            Info.AREA_CODE = jsonObject.getString("area"); //20211029

            int basePay = Integer.parseInt(jsonObject.getString("Bpay"));
            int baseDist = Integer.parseInt(jsonObject.getString("Bdist"));
            int t_interval = Integer.parseInt(jsonObject.getString("Tinterval"));
            int t_payment = Integer.parseInt(jsonObject.getString("Tpay"));
            int d_interval = Integer.parseInt(jsonObject.getString("Dinterval"));
            int d_payment = Integer.parseInt(jsonObject.getString("Dpay"));
            Double T_ExtraRate = Double.parseDouble(jsonObject.getString("Erate"));
            Double S_ExtraRate = Double.parseDouble(jsonObject.getString("Srate"));
            Double C_ExtraRate = Double.parseDouble(jsonObject.getString("Crate"));
            Info.SV_APP_VERSION = Double.parseDouble(jsonObject.getString("versions")); //20211029
            AMBlestruct.AMLicense.companynum = jsonObject.getString("biz_no"); //20211109
            try {
                Info.SV_SUBURBSVER = Double.parseDouble(jsonObject.getString("Suburbs")); //20220419 version 1.58
            }
            catch (Exception e) {
                ;
            }
            try {
                Info.SV_APP_CVERSION = Double.parseDouble(jsonObject.getString("c_version")); //20220506 tra..sh
            }
            catch (Exception e)
            {
                ;
            }
            try {
                String stims = jsonObject.getString("tims"); //20220506 tra..sh
                if(stims != null && stims.equals("N"))
                    Info.TIMSUSE = false;
            }
            catch (Exception e)
            {
                ;

            }

            Log.d(logtag + "_사업자번호", AMBlestruct.AMLicense.companynum + " suburbs " + Info.SV_SUBURBSVER + " " + Info.TIMSUSE);

            if (cert.equals("1")) {
                preResultData = ResultData; //20211109
                certRes = "y";

                Info.G_license_num = drvReg; //20220429

                CalFareBase.BASECOST = basePay;
                CalFareBase.BASECOSTEXTRATIME = (int) ((basePay * T_ExtraRate) + basePay);
                CalFareBase.BASECOSTEXTRASUBURB = (int) ((basePay * S_ExtraRate) * basePay);
                CalFareBase.BASECOSTEXTRACOMPLEX = (int) ((basePay * C_ExtraRate) * basePay);
                CalFareBase.DISTCOST = d_payment;
                CalFareBase.TIMECOST = t_payment;
                CalFareBase.INTERVAL_DIST = d_interval;
                CalFareBase.INTERVAL_TIME = t_interval;
                CalFareBase.BASEDRVDIST = baseDist;
                CalFareBase.mComplexrate = C_ExtraRate;
                CalFareBase.mNightTimerate = T_ExtraRate;
                CalFareBase.mSuburbrate = S_ExtraRate;

            } else if (cert.equals("0")) {
                certRes = "n";
            }

        } catch (Exception e) {
            certRes = "n";
            e.printStackTrace();
        }
        return certRes;
    }

    public static String ChangePhoneNumber(String phone) {
        if (phone == null || phone == "") {
            return "";
        }

        String tmp = phone.substring(0, 3);
        if (tmp.equals("+82")) {
            phone = "0" + phone.substring(3);
        } else {
            tmp = phone.substring(0, 2);
            if (tmp.equals("82")) {
                phone = "0" + phone.substring(2);
            }
        }

        return phone;
    }

    //20210823
    private void update_centerapk(int iTP) {
        //todo: 20220104

        // 업데이트
        if (iTP == 1) {

            final LinearLayout dialogView;
            dialogView = (LinearLayout) View.inflate(context, R.layout.dlg_basic, null);
            final TextView msg = (TextView) dialogView.findViewById(R.id.msg);
            final Button updateBtn = (Button) dialogView.findViewById(R.id.cancel_btn);
            final Button cancelBtn = (Button) dialogView.findViewById(R.id.okay_btn);

            msg.setText("프로그램 업데이트가 있습니다.\n");
            updateBtn.setText("업데이트");
            updateBtn.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
            cancelBtn.setText("아니오");
            cancelBtn.setBackgroundResource(R.drawable.cancel_btn_dark);


            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(dialogView);
            dialog.setCancelable(false);

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("market://details?id=com.konai.appmeter.driver");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                    dialog.dismiss();
                }
            });
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();

            int width = dm.widthPixels;
            int height = dm.heightPixels;

            //todo://20211203
            if (Build.VERSION.SDK_INT <= 25) {
                msg.setTextSize(3.0f * setting.gTextDenst);
                updateBtn.setTextSize(2.5f * setting.gTextDenst);
                cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                width = (int) (width * 0.6);
                height = (int) (height * 0.5);
            } else {
                width = (int) (width * 0.9);
                height = (int) (height * 0.5);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(updateBtn, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(cancelBtn, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
                updateBtn.setTextSize(25);
                cancelBtn.setTextSize(25);
            }

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = width;
            lp.height = height;
            Window window = dialog.getWindow();
            window.setAttributes(lp);

            dialog.show();

        } else if (iTP == 2) {

            final LinearLayout dialogView;
            dialogView = (LinearLayout) View.inflate(context, R.layout.dlg_basic, null);
            final TextView msg = (TextView) dialogView.findViewById(R.id.msg);
            final Button updateBtn = (Button) dialogView.findViewById(R.id.cancel_btn);
            final Button cancelBtn = (Button) dialogView.findViewById(R.id.okay_btn);

            msg.setText("프로그램 업데이트를 진행 하시겠습니까?");
            updateBtn.setText("업데이트");
            updateBtn.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
            cancelBtn.setText("아니오");
            cancelBtn.setBackgroundResource(R.drawable.cancel_btn_dark);

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(dialogView);
            dialog.setCancelable(false);

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlgbox.show();
                    TextView viewer = (TextView) dlgbox.findViewById(android.R.id.message);
                    viewer.setTextSize(30);

                    new Thread(new UpdateThread())
                            .start();// 배차 소리

                    dialog.dismiss();
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();

            int width = dm.widthPixels;
            int height = dm.heightPixels;

            //todo://20211203
            if (Build.VERSION.SDK_INT <= 25) {
                msg.setTextSize(3.0f * setting.gTextDenst);
                updateBtn.setTextSize(2.5f * setting.gTextDenst);
                cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                width = (int) (width * 0.6);
                height = (int) (height * 0.5);
            } else {
                width = (int) (width * 0.9);
                height = (int) (height * 0.5);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(updateBtn, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(cancelBtn, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
                updateBtn.setTextSize(25);
                cancelBtn.setTextSize(25);
            }

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = width;
            lp.height = height;
            Window window = dialog.getWindow();
            window.setAttributes(lp);

            dialog.show();

        }
    }
////////////todo: end
////////////todo: end

    //20210823 getphonenum
    public String getPhonenum() {
        UDPClientUtil mrndis_woorinet = null; //20210823
        int trycnt = 0;

        try {

            if (setting.phoneNumber.equals("0")) {
                if (mrndis_woorinet == null) {
                    mrndis_woorinet = new UDPClientUtil();
                    mrndis_woorinet.connectUdpAddressAndPort("192.168.225.1", 5002);
                    Log.d("UDPClientUtil", "start0");
                }

                Thread.sleep(1 * 200);
                mrndis_woorinet.sendphonenum();

                Thread.sleep(1 * 100);

                mrndis_woorinet.stopUdp();
                mrndis_woorinet = null;
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

//        setting.phoneNumber = "01050564465";
        return setting.phoneNumber;

    }

    ///20210823
    class UpdateThread implements Runnable {

        public void run() {


            update();
            dlgbox.dismiss();

        }
    }

    public void update() {

        try {
            //String url = "https://download.enpossystem.kr/posapk/drvpos.apk";
//20211029 add Info.AREA_CODE
            String url = setting.FILESERVERAPK + Info.AREA_CODE + setting.UPFILENAME;
            URLConnection uc = new URL(url).openConnection();
            InputStream in = uc.getInputStream();

            int len = 0, total = 0;
            byte[] buf = new byte[2048];

            //File apk = new File("/sdcard/apk/" + "aa.apk");
            // File apk=new File("/local/tmp/"+apkName+".apk");
            File path = getFilesDir();
            File apk = new File(path, "update.apk");

            if (apk.exists()) {
                apk.delete();
            }
            apk.createNewFile();
            // 다운로드
            FileOutputStream fos = new FileOutputStream(apk);

            while ((len = in.read(buf, 0, 2048)) != -1) {
                total += len;
                fos.write(buf, 0, len);
            }
            in.close();

            fos.flush();
            fos.close();

        } catch (Exception e) {

            e.printStackTrace();

            return;
        }
        //File apkFile = new File("/sdcard/apk/" + "aa.apk");
        File paths = getFilesDir();
        File apkFile = new File(paths, "update.apk");
        // File apkFile = new File("/local/tmp/"+apkName+".apk");

        if (apkFile != null) {

            if (Build.VERSION.SDK_INT >= 24) //20210823 8.0
            {

                installApk(apkFile);

            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
                startActivity(intent);

            }
        }

    }

    public void installApk(File file) {
        Uri fileUri = FileProvider.getUriForFile(this.getApplicationContext(), this.getApplicationContext().getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        finish();
    }

    private void registerReceiver() {


        IntentFilter filter;
        filter = new IntentFilter(setting.BROADCAST_TMSG);

        if (mReceiver != null)
            return;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
//			<action android:name="com.artncore.power.action.DTG_SHUTDOWN" />
///			<action android:name="com.artncore.power.action.DTG_RESUME" />
                Log.d("MemberCertActivity", "appmeter tomsg");
                if (action.equals(setting.BROADCAST_TMSG)) {
                    int msg = intent.getIntExtra("msgID", 0);

                    if (msg == 5100) {

                        Log.d("MemberCertActivity", "appmeter tomsg 5100");

                    } else if (msg == 5101) {

                        int state = intent.getIntExtra("value", 0);

                        Log.d("MemberCertActivity", "appmeter tomsg " + state);

                        switch (state) {
                            case 1: //설정.
                                break;

                            case 2: //해제.
                                break;

                        }


                    } //if(msg == 5001)

                }

            }

        };

        this.registerReceiver(mReceiver, filter);

        Log.d("MemberCertActivity", "registerReceiver");
    }

    private void unregisterReceiver() {

        Log.d("MemberCertActivity", "unregisterReceiver");
        if (mReceiver != null) {

            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    //20211109 통신 안될때 일정시간후 저장값으로 login.
    private void get_predata(int iwhich) {

        SharedPreferences editor = getSharedPreferences("last_login_info", MODE_PRIVATE);

        if (m_bSkipLogin) {
            inpt_name.setEnabled(true);
            inpt_carno.setEnabled(true);
            inpt_certino.setEnabled(true);
            btn_ok.setEnabled(true);

        }

        if(iwhich == 1) {
            inpt_name.setText(editor.getString("driver_name", ""));
            inpt_certino.setText(editor.getString("license_num", ""));
            inpt_carno.setText(editor.getString("car_no", ""));
            driver_num.setText(editor.getString("driver_num", "0000"));
            driver_name.setText(editor.getString("driver_name", ""));

            Info.G_driver_num = driver_num.getText().toString();
            Info.G_license_num = inpt_certino.getText().toString();
            Info.G_driver_name = driver_name.getText().toString();

        }
        else {
            prePhoneno = editor.getString("phoneno", "0"); //20211109
            preResultData = editor.getString("resultdata", "Fail"); //20211109

        }
        //todo: 20211115

        //todo: end

//        Log.d("login_info", "file " + Info.G_license_num + " " + Info.G_driver_num);
    }

///////////////

}
