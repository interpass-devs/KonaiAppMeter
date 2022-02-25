package com.konai.appmeter.driver;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.widget.DrawerLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.VO.TIMS_UnitVO;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.AMBleConfigActivity;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.Suburbs;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;
import com.konai.appmeter.driver.struct.CircleProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    String logtag = "logtag_";
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    double mGPSspeed = 0.0;
    Handler handler = new Handler();
    private ObjectAnimator anim = null;
    AnimThread animthread;
    View viewframe2, viewframe1;
    int index = 0; //todo: 20220128
    ArrayList<String> list = new ArrayList<>(); //todo: 20220128
    EditText edit_user, edit_password; //todo: 20220128
    String pwVal;   //todo: 20220128


    //bluetooth check
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private FrameLayout bodyFrame = null;
    private DrawerLayout menu;
    private View drawerView;

    private FrameLayout frametop = null;
    private FrameLayout frame1 = null;
    private FrameLayout frame2 = null;

    private boolean startFlags = false;

    private boolean reservUse = false;

    private boolean finConnect = false;

    double distanceForAdding = 0;
    double timeForAdding = 0;

    int lbs_x = -1;
    int lbs_y = -1;
    int lbs_w = 300;
    int lbs_h = 138;

    //기본요금
    int BASECOST = 3800;
    //거리당요금
    int DISTCOST = 100;
    //시간당요금
    int TIMECOST = 100;
    //시간별제한속도
    int TIMECOST_LIMIT = 10;

    //기본요금 거리
    int BASEDRVDIST = 2000;
    //거리간격
    int INTERVAL_DIST = 132;
    //시간간격
    int INTERVAL_TIME = 31;

    int distanceLimit = 0;

    int tLeftDist = 0;

    //이동총거리
    double tDistance = 0;
    int drvOperatingTime = 0;

    //  0 : 빈차  |  1 : 주행(고객탑승)  |  2 : 예약
    int drvingValue = 0;

    int mChangefare = 0; //수기 변경금액.
    int mAddfare = 0; //추가금액.

    // 현금영수증 0: default, 1: 개인, 2:사업자, 3:카드인식
    int cashReceipt_ = 0;

    private TimerTask second = null; //20210823;

    private boolean bInsertDB = false;
    int mnfare = 0;
    int mnlastcashfare = 0;
    int mnremaindist;
    double mddspeed;
    int mncurdist = 0; //20210928

    int mddistance;
    int mfaredist; //20210325
    int mnseconds;

    int memptyseconds;
    double memptydistance;

    Location mlocation = null;
    Location mlocationtmp = null;
    boolean msetDB = false;

    MediaPlayer mPlayer; //20210827

    int m_tempval;

    //DB (SQLITE)
    SQLiteHelper helper;
    SQLiteControl sqlite;
    String[] todayData, totalData;
    String drvCode;
    int payDiv, drvPay, addPay;

    int recCount = 15;

    //20210407
    static int faresendCount = 0; //20201110
    int mnSendfare = 0; //to AM100 display

    BroadcastReceiver gpsStatusReceiver = null;
    BroadcastReceiver speedReceiver = null;
    private BroadcastReceiver mReceiver = null;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    private final int PERMISSIONS_REQUEST_LOCATION = 1001;
    private final int PERMISSIONS_REQUEST_STORAGE = 1002;

    public static LocService m_Service = null;

    /**할증**/
    boolean extraUse = false;
    boolean suburbUse = false;
    boolean suburbUseAuto = false;
    boolean complexUse = false;

    /**현금결제**/
    boolean cashPay = false;
    int mndrvPayDiv = 0; //20211015 결제방법, 현금 0 카드 1 기타 2
    boolean mndrvtotal = false; //20211022
    /*************LAYOUT 요소***************/
    private LinearLayout emptyFrame1_new;    //todo: 20210917
    private LinearLayout emptyFrame1;
    private LinearLayout driveFrame1;
    private LinearLayout paymentFrame1;
    private LinearLayout payingFrame1;
    private LinearLayout endFrame1;
    private LinearLayout emptyFrame2;
    private LinearLayout driveFrame2;
    private LinearLayout paymentFrame2;
    private LinearLayout payingFrame2;
    private LinearLayout endFrame2;
/////////////////

    private Button btn_connBLE;
//    private ImageView btn_connBLE;
    private Button btn_menu;
    private Button btn_connBLEMachine;
    //20210823
    private Button btn_navi;
    private TextView tv_nowDate;
    private TextView tv_nowTime;
    /*************상단고정버튼***************/

    /** 빈차 **/
    private TextView tv_todayTotalDist;
    private TextView tv_todayTotalDrvCnt;
    private TextView tv_todayTotalPayment;
    private TextView tv_todayreset;
    private TextView tv_curEmptyDist; //20211103
    private TextView hideEmptyIcon;   //todo: 20211118
    private TextView showEmptyIcon;   //todo: 20211118
    private TextView hideReport;   //todo: 20220111
    private TextView showReport;

    private Button btn_driveStart;
    private Button btn_emptyCar_e;
    private Button btn_driveCar_e;
    private Button btn_reserv_e;
    private Button btn_manualpay_e; //20210909

    /** 주행 **/
    private TextView tv_remainfare;
    private TextView tv_boardkm;
    private TextView tv_nowfare;
    private LinearLayout nowfare_layout;
    private FrameLayout getFrameLayoutSize;
    private LinearLayout cover_layout;
    private ImageView iv_car_icon;
    private CircleProgressBar progressremain1;
    private ProgressBar progressremain2;

    private Button btn_extra;
    private Button btn_suburb;
    private Button btn_complex;
    private TextView tv_callfare; //20210917
    private Button btn_status;
    private Button btn_gpstext;
    private Button btn_driveEnd;

    private Button btn_emptyCar_d;
    private Button btn_driveCar_d;
    private Button btn_reserv_d;
    private Button btn_complex_d; //20210909
    private Button btn_surburb_d; //20210909

    /** 요금계산 **/
    private TextView tv_resDistance;
    private TextView tv_resPayment;
    private LinearLayout tv_restotpayment_layout;  //todo: 20220209
    private TextView tv_restotpayment;
    private TextView edt_addpayment;
    private TextView tv_rescallpay; //20210909

    //todo: 20220209
    private TextView pay_title;
    private LinearLayout layout_pay_distance, layout_pay_driving_payment, layout_pay_call_payment, layout_pay_add_payment;
    //todo: 20220209 end..

    private Button btn_endPayment;
    private Button btn_cancelpayment;
    //20210909
    private LinearLayout layout_payment_type;  //todo: 20220209
    private LinearLayout layout_add_payment;  //todo: 20220209
    private RelativeLayout tv_pay_card;  //todo: 20220209
    private TextView tv_title, tv_msg;   //todo: 20220209
    private Button btn_cashPayment;
    private Button btn_cardPayment;
    private Button btn_addPayment;
    private Button btn_callPayment;

    /** 결제 **/
    private ImageView iv_loadingGif;
    private Button btn_cashPay;
    private Button btn_mobilePay;
    private Button btn_payingCancel;

    /** 결제 완료 **/
    private TextView textView12;  //todo: 20220209
    private TextView tv_finDistance;
    private TextView tv_finPayment;
    private TextView tv_finAddPay;
    private TextView tv_finEndPay;
    private TextView tv_fincallpay; //20210909

    private Button btn_cashReceipt;
    private Button btn_receipt;
    private Button btn_emptyCar_ep;
    private Button btn_drive_ep;
    private Button btn_reserv_ep;


    /**
     * menu
     */
    private LinearLayout menu_home;
    private LinearLayout menu_drvHistory;
    private LinearLayout menu_submenu;
    private LinearLayout menu_setting;
    private LinearLayout menu_menualpay;
    private LinearLayout menu_getReceipt;
    private LinearLayout menu_cancelPay;
    private Button menu_endDrv;
    private Button menu_endApp;
    private Button menu_update;
    private Button menu_reset_app;
    private Button menu_close;
    private Boolean isClicked = true;
    private Boolean menuClicked = true;
    private TextView menu_ble, menu_ble_status, menu_ori, menu_ori_status, menu_gubun, menu_gubun_stauts, menu_auto_login, menu_auto_login_status, menu_modem, menu_modem_status;         //todo: 20211230
    private LinearLayout menu_cashReceipt, menu_env_setting, submenu_env_setting;


    private TextView menu_todayRecord;
    private TextView menu_yesterdayRecord;
    private TextView menu_allRecord;
    private TextView menu_drvname;
    private TextView menu_carnum;
    private TextView menu_version;

    private boolean chkSubMenuUse = false;

    //for test
    private String mtestdate;
    private String mtesttime;

    /////////////////
//20210611 for pref tfare
    int mtddistanceE;
    int mtddistanceB;
    int mtfare;
    int mtcnt;

    /**
     * ***********************************************************************************************************************************
     */
    private LocService.maincallback mCallback = new LocService.maincallback() {

        @Override
        public void serviceMessage(int ntype, String message) {
            // Todo: Activity에서 처리합니다.

            if (ntype == 0) {
                Message msg = displayHandler.obtainMessage();
                msg.what = 999;
                String information = message;
                msg.obj = information;

                displayHandler.sendMessage(msg);
                return;
            } else if (ntype == 1) {
                Message msg = displayHandler.obtainMessage();
                msg.what = 998;
                String information = message;
                msg.obj = information;

                displayHandler.sendMessage(msg);
                return;
            } else if (ntype == 2) //for event tims.
            {
                save_TIMS_pref(7);
/*
                Message msg = displayHandler.obtainMessage();
                msg.what = 997;
                String information = message;
                msg.obj = information;

                displayHandler.sendMessage(msg);

 */
                return;
            } else if (ntype == 3) //for 운행시작,종료 tims.
            {
                save_TIMS_pref(6);
/*
                Message msg = displayHandler.obtainMessage();
                msg.what = 996;
                String information = message;
                msg.obj = information;

                displayHandler.sendMessage(msg);
*/
                return;
            } else if (ntype == 99) {
                //todo: 20220209
//                btn_connBLE.setBackgroundResource(R.drawable.btn_bles_on);  //me: 원래이거 - 파란 블루투스 아이콘
                btn_connBLE.setBackgroundResource(R.drawable.bluetooth_green);  //야야야야야

                if (!finConnect && m_Service != null) {
                    m_Service._setLbsBleState(1);
                    finConnect = true;
                }
                //todo: 20220209 end..

            } else if (ntype == 98) { //TIMS차량번호인증실패

                displayHandler.sendEmptyMessage(98);

            } else if (ntype == 97) { //TIMS운전자격인증실패

                displayHandler.sendEmptyMessage(97);

            } else if (ntype == 96) { //TIMS영업data

                displayHandler.sendEmptyMessage(96);

            }

            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void serviceDisplayState(int nfare, int nremaindist, int nfarediscount, double ddspeed, int ddistance, int nseconds, Location location,
                                        int nfaredist, int ncurdist, boolean setDB) {
            // Todo: Activity에서 처리합니다.


            //Log.e("D mnseconds", mnseconds + "");

            mlocation = location;
            mnfare = nfare;
            Info.PAYMENT_COST = (int) (Math.round((Double.parseDouble(mnfare + "") / 100)) * 100); //20210607
            mnremaindist = nremaindist;
            mddspeed = ddspeed;
            mncurdist = ncurdist;
            _setBoardDist(ddistance);

            mnseconds = nseconds;
            mfaredist = nfaredist;
            msetDB = setDB;
            displayHandler.sendEmptyMessage(1);

//20201207
//            faresendCount = (faresendCount + 1);
//            if(faresendCount >= 9)
///                faresendCount = (faresendCount % 10) + 10;

            if (mnfare != mnSendfare) {

                mnSendfare = mnfare;
                AMBlestruct.mbSendfareOK = false;
            }

            if (AMBlestruct.mbSendfareOK == false)
                m_Service.update_BLEmeterfare(nfare, nfarediscount, nremaindist);

/*
            if(false) {

                m_Service.update_BLEmeterfare(nfare, nfarediscount, nremaindist);

            }
*/

//20211019            mnlastcashfare = mnfare;
        }

        @Override
        public void serviceEmptyState(double ndistance, int nseconds, double ddspeed, Location location) {

            mlocation = location;
            mddspeed = ddspeed;
            memptyseconds = nseconds;
//            mddistance = Integer.parseInt(String.valueOf(Math.round(ndistance)));
            memptydistance = ndistance;

            displayHandler.sendEmptyMessage(3);

        }

        @Override
        public void serviceMeterState(int nType, int mfare) {

            Log.e("n Meter State", nType + ",  mfare: " + mfare);

            if (nType == AMBlestruct.MeterState.PAY) {
                if (m_Service.mbDrivestart == false) //20220107 by am100
                {

                    displayHandler.sendEmptyMessage(12);

                } else
                    btn_driveEnd.performClick(); //20210909

            } else if (nType == AMBlestruct.MeterState.EMPTY) {

                endpayment();

            } else if (nType == AMBlestruct.MeterState.DRIVE) {

//                btn_driveCar_e.performClick();

                //20220207
                if(m_Service.mbDrivestart == false) {

                    btn_driveStart.performClick(); //20210909

                }
                else
                    continue_board();

            } else if (nType == AMBlestruct.MeterState.ENDPAYMENT) {

//                01: 카드결제 완료
//                02: 현금영수결제 완료 – 개인
//                03: 현금영수결제 완료 – 사업자
//                04: 현금영수결제 완료 – 카드
//                05: 일반현금결제 완료
//                06: 기타결제 완료 – 온라인 결제 등 외부의 결제 기능

                Info._displayLOG(true, "mndrvPayDiv ========receive(msType---", AMBlestruct.AMCardResult.msType);
                if (AMBlestruct.AMCardResult.msType.equals("01")) {
//현금, 기타결제는 UI버튼으로 TIMS paytype확인
                    if (Info.TIMSUSE == true)
                        setTIMSfinal("1");

                    mndrvPayDiv = 1;

                    mnlastcashfare = 0;

//                    Log.d("mndrvPayDiv", "--1 " + mndrvPayDiv);
                } else if (AMBlestruct.AMCardResult.msType.equals("05")) {

                    mndrvPayDiv = 0;
//                    Log.d("mndrvPayDiv", "--2 " + mndrvPayDiv);
                } else if (AMBlestruct.AMCardResult.msType.equals("06")) {

                    mndrvPayDiv = 2;
//                    Log.d("mndrvPayDiv", "--3 " + mndrvPayDiv);
                } else
                    mndrvPayDiv = 9; //etc

                if (mndrvtotal)
                    mndrvPayDiv = 9; //etc 빈차 버튼클릭.

//                Log.d("mndrvPayDiv", "--4 " + mndrvPayDiv);

                afterPayment();  //결제후

            } else if (nType == AMBlestruct.MeterState.ENDCANCELPAYMENT) //20210512
            {
                //20220107
//                cancelPayment(mfare);
                after_cancel_pay(mfare);
//                Info.makeDriveCode();
//                afterPayment(mfare);  //결제취소후
////////////////////
            } else if (nType == AMBlestruct.MeterState.EXTRATIME) {
                extraUse = true;
                timeExtrachk();
            } else if (nType == AMBlestruct.MeterState.EXTRATIMEOFF) {
                extraUse = false;
                timeExtrachk();
            } else if (nType == AMBlestruct.MeterState.BLELEDON) {
                Log.e("BLE", "On Ble Service");
            } else if (nType == AMBlestruct.MeterState.BLELEDOFF) {
//20211216                btn_connBLE.setBackgroundResource(R.drawable.ic_bluetooth_btn);
                displayHandler.sendEmptyMessage(51); //20211216
                Log.e("BLE", "Off Ble Service");
            } else if (nType == AMBlestruct.MeterState.SUBURBSIN) //20210325
            {
                suburbUseAuto = true;
                if (suburbUse == true)
                    btn_suburb.performClick();
            } else if (nType == AMBlestruct.MeterState.SUBURBSOUT) //20210325
            {

                suburbUseAuto = true;

                if (suburbUse == false)
                    btn_suburb.performClick();

            }

        }

        //20210310
        @Override
        public void serviceLog(int nseconds, Location location, int gpsAcc, int ndtgdist, int ndtgtot, String altitude, boolean drvState,
                               int speed, double dtime, double dtfare, double ddist, double dcfare,
                               double dremain, int nafterfare, int nfare, boolean bextra, boolean bsuburb, double tfaredist) //tmp for log
        {
            if (drvState) {
                double latitude = 0;
                double longitude = 0;
                String GPSUse = "";
                int nextra = 0;
                int nsuburb = 0;

                if (bextra)
                    nextra = 1;

                if (bsuburb)
                    nsuburb = 1;

                if (nseconds == 2) {
                    GPSUse = "B";
                } else {
                    GPSUse = "G";
                }

                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                String stemp = "";
                Date date = new Date();
                SimpleDateFormat dttiFormat = new SimpleDateFormat("yyMMddHHmmss");
                String dtti = dttiFormat.format(date);

                stemp += "T:" + dtti + ",";
//20210310                stemp += String.format("x:%.6f,y:%.6f,AC:%s,d:%d", latitude, longitude, GPSUse+"("+gpsAcc+")", ndtgdist);
//                stemp += String.format("x:%.6f,y:%.6f,%s,d:%d", latitude, longitude, GPSUse, ndtgdist);
                stemp += String.format("x:%.6f,y:%.6f,보정:%s,속도:%d", latitude, longitude, GPSUse, speed);
                stemp += String.format(",시간:%.2f,시간요금:%.2f,거리:%.2f,요금합:%.2f,잔여:%.2f,할증:%d,시계:%d,이후:%d,총이동:%d,현재:%d, 총요금거리: %.4f", dtime, dtfare,
                        ddist, dcfare, dremain, nextra, nsuburb, nafterfare, ndtgtot, nfare, tfaredist);
//                stemp += " F: " + mnfare;
//                Log.e("SAVE DATA", stemp);

                if (Info.SENDDTG)
                    Savedata(Info.g_nowKeyCode + ".txt", stemp, "appmeter");

                if (Info.TIMSUSE) {
                    mtestdate = ndtgtot + " " + GPSUse + ndtgdist;
                    mtesttime = "A:" + gpsAcc;

//                    displayHandler.sendEmptyMessage(99);

                    Savedata(Info.g_nowKeyCode + ".txt", stemp, "appmeter");

                }
            }

        }

        @Override
        public void serviceFarebyMeter(int nseconds, int ndist, int nfare, int naddfare) {


        }

        @Override
        public void serviceLbsControllEvent(int nType, int nLastState) {
            Log.e("LbsContollEvt", nLastState + "/" + nType);

//            nType 1 빈차 2 주행 3 호출
            switch (nLastState) {
                case 1:
                    /*if(nType == 1) {

                    } else if(nType == 2) {

                    } else if(nType == 3) {*/

                    switch (nType) {
                        case 1:
                            if (reservUse) {
                                btn_reserv_e.setBackgroundResource(R.drawable.unselected_btn);
                                m_Service.update_BLEmeterstate("41");
                                reservUse = false;

                                setCallpay(0);

                            }
                            break;

                        case 2:
                            setStateDrive();
                            break;

                        case 3:
                            if (!reservUse) {
                                btn_reserv_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                                reservUse = true;

//20211216                                setCallpay(1000);
                                do_CallPay_other(); //20211229
                            }
                            m_Service.update_BLEmeterstate("40");
                            break;
                        default:
                            break;
                    }
                    break;
                case 2:
                    switch (nType) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;

                        case 4: //20210831 지불
                            btn_driveEnd.performClick();

                            break;
                        default:
                            break;
                    }
                    break;
                case 3:
                    switch (nType) {
                        case 1:
                            btn_reserv_e.setBackgroundResource(R.drawable.unselected_btn);
                            m_Service.update_BLEmeterstate("41");
                            reservUse = false;
                            setCallpay(0);
                            break;
                        case 2:
                            setStateDrive();
                            break;

                        case 3:
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;

            }
        }

        @Override
        public void serviceTIMSDataEvent(int idx, String date, Location location, double speed, String lnk, double dist, double remainsDist,
                                         int fare, int isOutGps, int addfare, double tdist, boolean isnight, boolean issuburb) {
//            double latitude = 0;
///            double longitude = 0;
            String latitude = "";
            String longitude = "";
            String payType = "";
            String isLimitSpd = "0";
            String isNight = "";
            String isOutside = "";
            String stemp = "";
            double intSpd = 0;
            int isAdded = 0;

            if (idx == 0) {
                BASECOST = fare;

//                Info.deletefile(Info.g_nowKeyCode + ".txt", "TIMS");
            }

            if (location != null) {

                latitude = String.format("%.8f", location.getLatitude());
                longitude = String.format("%.8f", location.getLongitude());
            } else {

                latitude = String.format("%.8f", 0.0);
                longitude = String.format("%.8f", 0.0);
            }


            intSpd = (speed * 3.6);
            if (isnight) {
                isNight = "1";
            } else {
                isNight = "0";
            }
            if (issuburb) {
                isOutside = "1";
            } else {
                isOutside = "0";
            }

            if (intSpd < CalFareBase.TIMECOST_LIMITHOUR) {

                isLimitSpd = "1";

            } else
                isLimitSpd = "0";

            if (addfare > 0)
                isAdded = 1;

            if (isAdded == 1) {

                payType = "D";

                if (isnight) {

                    payType += "E";

                }
                if (issuburb) {

                    payType += "Z";

                }

            }

            if (idx == 0) {

                BASECOST = fare;
                payType = "S";

                if (isnight) {

                    payType += "E";

                }
                if (issuburb) {

                    payType += "Z";

                }

            }

            stemp += idx + "|"
                    + date + "|"
                    + longitude + "|"
                    + latitude + "|"
                    + String.format("%.2f", intSpd) + "|"
                    + "|"//+ lnk + "|"
                    + String.format("%.4f", dist) + "|"
                    + payType + "|"
                    + String.format("%.4f", remainsDist) + "|"
                    + "0|"//+ remainSec + "|"
                    + isAdded + "|"
                    + addfare + "|"
                    + fare + "|"
                    + isLimitSpd + "|"
                    + isNight + "|"
                    + isOutside + "|"
                    + isOutGps + "|"
                    + String.format("%.2f", tdist) + "&"; //????????????????


//            if(Info.TIMSUSE_TEST)
///                Info._displayLOG(true, stemp, "MAKE TIMS-");

            Savedata(Info.g_nowKeyCode + ".txt", stemp, "TIMS");

        }

    };

    /**
     public void cancelPayment(int mfare){

     helper = new SQLiteHelper(getBaseContext());
     sqlite = new SQLiteControl(helper);


     //todo: 당일 거래내역
     todayData = sqlite.selectToday();
     if (todayData.length > 0){
     String[] splt = todayData[0].split("#");   //맨 마지막 데이터
     if (splt.length > 0){
     drvCode = splt[0];   //운행코드 string
     drvPay = Integer.parseInt(splt[2]);  //요금(fare)
     payDiv = Integer.parseInt(splt[3]);  //현금/카드/모바일
     Log.d("payDivCheck", payDiv+"");
     //                                payDiv = 0;  //카드
     addPay = Integer.parseInt(splt[4]);  //추가요금
     Log.d("today_data[0]", todayData[0]);
     Log.d("today_data_drvCode", drvCode+"");  //null
     Log.d("today_data_payDivision", payDiv+"");
     Log.d("today_data_drvPay", drvPay+"");
     Log.d("today_data_addPay", addPay+"");

     String strDrvPay = drvPay+"";
     if (strDrvPay.contains("-")){
     Log.d("strdrvpay_confain", strDrvPay);
     Toast.makeText(MainActivity.this, "결제취소가 이미 처리되었습니다.", Toast.LENGTH_SHORT).show();
     //                    dlg.dismiss();

     }else {
     Log.d("strdrvpay", strDrvPay);

     if (payDiv == 0){  //현금
     Log.d("today_data_", "현금");
     //do nothing
     }else if (payDiv == 1){  //카드
     Log.d("today_data_", "카드");

     //마지막 결제아이템 운행코드 가져오기
     after_cancel_pay(drvCode);  //카드결제 취소

     frameviewchange(1);
     showEmptyIcon.performClick();

     }else {   //모바일(2)
     //do nothing
     }
     menu.closeDrawer(drawerView);
     }
     }
     }else {
     Toast.makeText(MainActivity.this, "결제취소할 목록이 없습니다.", Toast.LENGTH_SHORT).show();
     }

     }
     **/

    private void makeSaveFolder() {
        File saveFile = null;
        if (Build.VERSION.SDK_INT < 29)
            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/appmeter");
        else saveFile = MainActivity.this.getExternalFilesDir("/appmeter");

        if (!saveFile.exists())
            saveFile.mkdir();

    }

    private void Savedata(String sfile, String sdata, String path) {

        //Location Detail Log
//        Log.d("File1", "saving Savedata" + sfile + " : " + sdata + " / spd : " + ((int)(mddspeed * 3.6) + ""));


        File saveFile = null;
        if (Build.VERSION.SDK_INT < 29)
            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
        else saveFile = MainActivity.this.getExternalFilesDir("/" + path);

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile + "/" + sfile, true));

            buf.append(sdata);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTIMSfinal(String paytype) {

        Savedata(Info.g_nowKeyCode + ".txt", paytype + "|" + String.format("%.2f", memptydistance), "TIMS");

    }

    public String ReadTextFile(String sfile, String path) {

        File saveFile = null;
        if (Build.VERSION.SDK_INT < 29)
            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
        else saveFile = MainActivity.this.getExternalFilesDir("/" + path);

        StringBuffer strBuffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(saveFile + "/" + sfile));
            String line = "";
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line + "\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "noFile";
        }
        return strBuffer.toString();
    }

    public void setSendTIMSVO(String data) {
        List<TIMS_UnitVO> params = new ArrayList<>();
        String[] splitDatas = data.split("&");

        int npaytype = 0;
        String semptydist = "";

        for (int i = 0; i < splitDatas.length; i++) {
            String[] unitData = splitDatas[i].split("\\|");

            if (unitData.length < 5) {

                npaytype = Integer.parseInt(unitData[0].trim());
                semptydist = unitData[1].trim();

                break;
            }

            TIMS_UnitVO unit = new TIMS_UnitVO();

            unit.setIdx(Integer.parseInt(unitData[0].trim()));
            unit.setDt(unitData[1]);
            unit.setLongitude(unitData[2]);
            unit.setLatitude(unitData[3]);
            unit.setSpd(unitData[4]);
            unit.setLnk(""); //unitData[5]);
            unit.setDist(unitData[6]);
            unit.setPayType(unitData[7]);
            unit.setRemainDist(unitData[8]);
            unit.setRemainSec(unitData[9]);
            unit.setIsAdded(unitData[10]);
            unit.setAddedPay(unitData[11]);
            unit.setSumPay(unitData[12]);
            unit.setIsLimitSpd(unitData[13]);
            unit.setIsNight(unitData[14]);
            unit.setIsOutside(unitData[15]);
            unit.setIsOutGps(unitData[16]);
            unit.setSumdist(unitData[17]);

            params.add(i, unit);
        }

        m_Service.SendTIMS_Data(1, npaytype, params, semptydist);

    }

    //20210323
    private void save_state_pref(String key, int nmode, long ltime, int nfare, int distance, int remain, int nfaredist, int nobddist, float x, float y) {

        SharedPreferences pref = getSharedPreferences("state", Activity.MODE_PRIVATE); //160622
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("MODE", nmode);
        editor.putString("KEY_", key);
        editor.putLong("STME", ltime);
        editor.putInt("FARE", nfare);
        editor.putInt("FADS", nfaredist);
        editor.putInt("DIST", distance);
        editor.putInt("REMA", remain);
        editor.putInt("DTGD", nobddist);
        editor.putFloat("POSX", x);
        editor.putFloat("POSY", y);
        editor.putBoolean("EXT1", suburbUse);
        editor.putBoolean("EXT2", complexUse);
        editor.putInt("UIDX", m_Service.getTimsUnitidx());

        editor.commit();

        if (Info.REPORTREADY) {

            Info._displayLOG(true, "택시요금상태저장" + "요금" + nfare + " /거리" + distance + " /잔여거리" + remain + " /x,y " + x + "," + y, "");

        }

    }

    private void save_TIMS_pref(int ntype) {

        if (!Info.TIMSUSE)
            return;

        if (!Info.gTimsLastDate.substring(0, 8).equals(getCurDateString().substring(0, 8))) {
            Info.gTimsDayIdx = -1;
            Info.gTimsDayPowerIdx = -1;
            Info.gTimsDayEventIdx = -1;

            Info.gTimsLastDate = getCurDateString();

        }

        switch (ntype) {

            case 1:
                Info.gTimsDayIdx++;
                Info.gTimsDayIdxtmp = Info.gTimsDayIdx;
                break;

            case 6:
                Info.gTimsDayPowerIdx++;
                break;

            case 7:
                Info.gTimsDayEventIdx++;
                break;

            case 8:
                Info.gTimsSendDone = true;
                Info.gTimsFile = Info.g_nowKeyCode;

                break;

        }

        SharedPreferences pref = getSharedPreferences("timsstate", Activity.MODE_PRIVATE); //160622
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("DATE", Info.gTimsLastDate);
        editor.putInt("IDX1", Info.gTimsDayIdx);
        editor.putInt("IDX2", Info.gTimsDayEventIdx);
        editor.putInt("IDX3", Info.gTimsDayPowerIdx);
        editor.putString("FILE", Info.gTimsFile); //last sended file.
        editor.putBoolean("SEND", Info.gTimsSendDone);

        editor.commit();

//        Info._displayLOG(true, "tims idx read " + Info.gTimsLastDate + " " + Info.gTimsDayIdx + " " +
//                Info.gTimsDayEventIdx + " " + Info.gTimsDayPowerIdx + " " +
//                Info.gTimsLastDate.substring(0, 8) + " " + getCurDateString().substring(0, 8), "");

    }

    //20210323
    private void get_state_pref() {

        if (Info.g_state_done)
            return;

        Info.g_state_done = true;

        SharedPreferences pref = getSharedPreferences("state", Activity.MODE_PRIVATE); //160622
        int nmode = pref.getInt("MODE", 1);

        if (nmode == 2) {

            String key = pref.getString("KEY_", "00000000");
            Info.g_lastKeyCode = key;
            Info.g_nowKeyCode = key;
            long ltime = pref.getLong("STME", 0);
            int nfare = pref.getInt("FARE", 0);
            int ndist = pref.getInt("DIST", 0);
            int nfaredist = pref.getInt("FADS", 0);
            int nremain = pref.getInt("REMA", 0);
            int ndtgdist = pref.getInt("DTGD", 0);
            float xpos = pref.getFloat("POSX", 0);
            float ypos = pref.getFloat("POSY", 0);
            boolean bExtra1 = pref.getBoolean("EXT1", false);
            boolean bExtra2 = pref.getBoolean("EXT2", false);
            int nunitidx = pref.getInt("UIDX", 0);
            m_Service.setTimsUnitidx(nunitidx);

            if (bExtra1) {

                suburbUseAuto = true;
                btn_suburb.performClick();
            }

            if (bExtra2)
                btn_complex.performClick();

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "USEDRIVESTATEPOWEROFF 직전상태 주행 택시요금읽기", "");
                Info._displayLOG(true, "요금" + nfare + " /거리" + ndist + " /잔여거리" + nremain + " /x,y " + xpos + "," + ypos, "");
            }

            LocService.CDrive_val.setPreInfo(key, ltime, nfare, ndist, nremain, nfaredist, ndtgdist);

            m_Service.drive_state(AMBlestruct.MeterState.POWERONDRIVE);

            mCallback.serviceDisplayState(nfare, nremain, 0, 0, ndist, 0, null, nfaredist, 0, true);

            continue_board();
        } else if (nmode == 1) {
            if (Info.REPORTREADY) {

                Info._displayLOG(true, "USEDRIVESTATEPOWEROFF 직전상태 빈차 택시요금읽기", "");

            }
        }

    }

    //20210512
    private void get_TIMS_pref() {

        SharedPreferences pref = getSharedPreferences("timsstate", Activity.MODE_PRIVATE); //160622

        Info.gTimsLastDate = pref.getString("DATE", getCurDateString());
        Info.gTimsDayIdx = pref.getInt("IDX1", -1);
        Info.gTimsDayEventIdx = pref.getInt("IDX2", -1);
        Info.gTimsDayPowerIdx = pref.getInt("IDX3", -1);
        Info.gTimsFile = pref.getString("FILE", "");
        Info.gTimsSendDone = pref.getBoolean("SEND", true);

//        Info._displayLOG(true, "tims idx save " + Info.gTimsLastDate + " " + Info.gTimsDayIdx + " " +
//                Info.gTimsDayEventIdx + " " + Info.gTimsDayPowerIdx + " " +
//                Info.gTimsLastDate.substring(0, 8) + " " + getCurDateString().substring(0, 8), "");

    }

    private void Savealtitude(String sfile, String sdata) {

        //test appmeter
        Log.d("File2", "saving Savedata" + sfile + " : " + sdata);
        File saveFile = null;
        if (Build.VERSION.SDK_INT < 29)
            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/appmeter");
        else saveFile = MainActivity.this.getExternalFilesDir("/appmeter");

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile + "/" + sfile + "h.txt", true));

            buf.append(sdata);
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //////////////
//20210611
    public void save_totalfare_pref(int nfare, int diste, int distb)
    {
//////////////////
        if (nfare != 0){
            Log.d("mtcnt", mtcnt+"");
            mtcnt++;
            Log.d("mtcnt++", mtcnt+"");
            mtddistanceE += diste;
            mtddistanceB += distb;
            mtfare += nfare;
        }

        SharedPreferences pref = getSharedPreferences("tfare", Activity.MODE_PRIVATE); //160622
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("tcnt_", mtcnt);
        editor.putInt("distE", mtddistanceE);
        editor.putInt("distB", mtddistanceB);
        editor.putInt("tfare", mtfare);

        editor.commit();
    }


    public void minus_totalfare_pref(int nfare, int diste, int distb) {
        if (nfare >= 0) {
            return;
        }

        if (mtcnt <= 0) {
            return;
        }

        Log.d("mtcnt", mtcnt + "");
        mtcnt--;
        Log.d("mtcnt--", mtcnt + "");
        mtddistanceE = mtddistanceE - diste;
        mtddistanceB = mtddistanceB - distb;
        Log.d("mtfare_총", mtfare + "");
        Log.d("mtfare_뺄_값", nfare + "");
        mtfare += nfare;
        Log.d("mtfare_뺀_값", mtfare + "");

        SharedPreferences pref = getSharedPreferences("tfare", Activity.MODE_PRIVATE); //160622
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("tcnt_", mtcnt);
        editor.putInt("distE", mtddistanceE);
        editor.putInt("distB", mtddistanceB);
        editor.putInt("tfare", mtfare);

        editor.commit();

    }


    private void get_totalfare_pref() {

        SharedPreferences pref = getSharedPreferences("tfare", Activity.MODE_PRIVATE); //160622
        mtcnt = pref.getInt("tcnt_", 0);
        mtddistanceE = pref.getInt("distE", 0);
        mtddistanceB = pref.getInt("distB", 0);
        mtfare = pref.getInt("tfare", 0);

    }
////////////////////////

    Handler displayHandler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {

            //Log.e("msg", msg.what + "");

            switch (msg.what) {
                case -1: //20210823
                {

                    if (Info.searchAppPackage(getApplicationContext(), "com.enpsystem.drv.texi")) {
                        if (setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            Intent intent;
                            intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.enpsystem.drv.texi");
                            startActivity(intent);
                        }
                    }

//                    Intent intent = new Intent(setting.APPMETER_FMRUN);
//                    sendBroadcast(intent);

                    break;
                }
                case 1: //board display
                    display_Runstate();

                    if (m_Service.mCardmode == AMBlestruct.MeterState.NONE)
                        //tv_cardment.setText((int)(mddspeed * 3.6) + "");

                        break;

                case 2:
                    //tv_cardment.setText("D " + m_tempval);

                    break;

                case 3:
//20201207                    if(m_Service.mCardmode == AMBlestruct.MeterState.NONE)
                    //tv_cardment.setText((int)mddspeed + "");
//20211103
                    tv_curEmptyDist.setText(String.format("%.2fkm", memptydistance / 1000.0));
                    break;

                case 10:
                    if (mChangefare > 0) {
                        Info.makeDriveCode();
                        Info.insert_rundata(mlocation, 1); //drive
                        bInsertDB = true;

                        _setBoardDist(0);

                        //Log.e("frame Change e", "4");

                        AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode;
                        AMBlestruct.AMCardFare.mbCard = true;
                        AMBlestruct.AMCardFare.mstype = "01";
                        AMBlestruct.AMCardFare.mStarttime = getCurDateString();
                        AMBlestruct.AMCardFare.mEndtime = getCurDateString();

                        AMBlestruct.AMCardFare.mFare = mChangefare;
                        AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
                        AMBlestruct.AMCardFare.mCallCharge = 0; //호출요금.
                        AMBlestruct.AMCardFare.mAddCharge = 0; //추가요금.
                        AMBlestruct.AMCardFare.mMoveDistance = 0; //승차거리

                        frameviewchange(4);

                        mnfare = mChangefare; //20210520
                        Info.PAYMENT_COST = mChangefare; //20210607

                        m_Service.drive_state(AMBlestruct.MeterState.MANUALPAY);

                        Info.CALL_PAY = 0;
                        mAddfare = 0;
                        btn_driveEnd.performClick();

                    }
                    break;

                case 11:
                    if (mAddfare > 0) {

                        LocService.CDrive_val.setmFareAdd(mAddfare); //20210823
//                        LocService.CDrive_val.setmFareDiscount(0); //20210823

                        bInsertDB = true;

//20210615                        mddistance = 0;

                        //Log.e("frame Change f", "4");
                        frameviewchange(4);

                        AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode; // Info.g_lastKeyCode;
                        AMBlestruct.AMCardFare.mbCard = true;
                        AMBlestruct.AMCardFare.mstype = "01";
//                        AMBlestruct.AMCardFare.mStarttime = getCurDateString();
//                        AMBlestruct.AMCardFare.mEndtime = getCurDateString();
                        AMBlestruct.AMCardFare.mFare = Info.PAYMENT_COST; //20210607 mnfare;
                        AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
                        AMBlestruct.AMCardFare.mCallCharge = Info.CALL_PAY; //20210909 호출요금.
                        AMBlestruct.AMCardFare.mAddCharge = mAddfare; //추가요금.
                        AMBlestruct.AMCardFare.mMoveDistance = Info.MOVEDIST; //승차거리

                        //tv_nowaddfare.setText(mAddfare + "원");
//20210823
//20210909                        if(false)
                        if (true) {

                            m_Service.drive_state(AMBlestruct.MeterState.ADDPAY);

                        } else {
                            stopdriverstart();
                            m_Service.drive_state(AMBlestruct.MeterState.PAY);
                            m_Service.update_BLEmeterstate("01"); //?
                        }
/////////////////////

                    } else {
                        bInsertDB = true;

                        LocService.CDrive_val.setmFareAdd(0); //20210823
                        LocService.CDrive_val.setmFareDiscount(0); //20210823

//20210615                        mddistance = 0;

                        //Log.e("frame Change f", "4");
                        frameviewchange(4);

                        AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode; //20210611 Info.g_lastKeyCode;
                        AMBlestruct.AMCardFare.mbCard = true;
                        AMBlestruct.AMCardFare.mstype = "01";
//                        AMBlestruct.AMCardFare.mStarttime = getCurDateString();
//                        AMBlestruct.AMCardFare.mEndtime = getCurDateString();
                        AMBlestruct.AMCardFare.mFare = Info.PAYMENT_COST; //20210607 mnfare;
                        AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
                        AMBlestruct.AMCardFare.mCallCharge = Info.CALL_PAY; //호출요금.
                        AMBlestruct.AMCardFare.mAddCharge = mAddfare; //추가요금.
                        AMBlestruct.AMCardFare.mMoveDistance = Info.MOVEDIST; //승차거리

                        //tv_nowaddfare.setText(mAddfare + "원");
//20210823
//20210909                        if(false)
                        if (true) {

                            m_Service.drive_state(AMBlestruct.MeterState.ADDPAY);

                        } else {
                            stopdriverstart();
                            m_Service.drive_state(AMBlestruct.MeterState.PAY);
                            m_Service.update_BLEmeterstate("01"); //?
                        }
////////////
                    }

                    btn_driveEnd.performClick();

                    break;

                //20220107 //지불 by AM100
                case 12: {
                    Info.makeDriveCode();
                    Info.insert_rundata(mlocation, 1); //drive
                    bInsertDB = true;

                    Info.PAYMENT_COST = CalFareBase.BASECOST;
                    _setBoardDist(0);

                    //Log.e("frame Change e", "4");

                    AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode;
                    AMBlestruct.AMCardFare.mbCard = true;
                    AMBlestruct.AMCardFare.mstype = "01";
                    AMBlestruct.AMCardFare.mStarttime = getCurDateString();
                    AMBlestruct.AMCardFare.mEndtime = getCurDateString();

                    AMBlestruct.AMCardFare.mFare = Info.PAYMENT_COST;
                    AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
                    AMBlestruct.AMCardFare.mCallCharge = 0; //호출요금.
                    AMBlestruct.AMCardFare.mAddCharge = 0; //추가요금.
                    AMBlestruct.AMCardFare.mMoveDistance = 0; //승차거리

                    m_Service.drive_state(AMBlestruct.MeterState.DRIVE);

                    frameviewchange(4); //20220107

                    btn_driveEnd.performClick();

                }
                break;
////////////////////////

                case 51: //20211216
//                    btn_connBLE.setBackgroundResource(R.drawable.ic_bluetooth_btn);
                    btn_connBLE.setBackgroundResource(R.drawable.bluetooth_blue);
                    break;

                case 90:
                    progressremain2.setProgress(0);
                    break;

                case 99: //date display
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd");
                    SimpleDateFormat format2 = new SimpleDateFormat("a hh:mm");
                    Calendar time = Calendar.getInstance();
                    format1.format(time.getTime());
//20201211
                    if (false) //Info.TESTMODE)
                    {
                        tv_nowDate.setText(mtestdate);
                        tv_nowTime.setText(mtesttime);

                    } else {

                        if (mlocation != null && m_Service.mbDrivestart == true) {

                            if (mlocation.getAccuracy() < 0 || mlocation.getAccuracy() > 12) //20201215  ///-~ 0~12 높을수록 오차범위가 큼
                            {

                                tv_nowDate.setText("AM100수신...");

                            } else
                                tv_nowDate.setText(format1.format(time.getTime()));

                        } else
                            tv_nowDate.setText(format1.format(time.getTime()));

                        tv_nowTime.setText(format2.format(time.getTime()));
                    }

                    break;

                case 98:

                    _licens_fail(98);
                    break;

                case 97:


                    _licens_fail(97);
                    break;

                case 996: //for 운행시작,종료 tims.
                    save_TIMS_pref(6);

                    Toast.makeText(getApplicationContext(),
                            msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;

                case 997: //for event tims.
                    save_TIMS_pref(7);

                    Toast.makeText(getApplicationContext(),
                            msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;

                case 998:
                    save_TIMS_pref(8);

                    Toast.makeText(getApplicationContext(),
                            msg.obj.toString(), Toast.LENGTH_SHORT).show();

                    break;

                case 999:

                    Toast.makeText(getApplicationContext(),
                            msg.obj.toString(), Toast.LENGTH_SHORT).show();

                    break;

            }

        }

    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("pos_app_mainActivity", "pos_app_service_main onServiceConnected");
            LocService.ServiceBinder binder = (LocService.ServiceBinder) iBinder;
            m_Service = binder.getService();
            m_Service.registerCallback(mCallback); //

            Info.m_Service = m_Service;

            m_Service.lbs_initx = lbs_x;
            m_Service.lbs_inity = lbs_y;
            m_Service.lbs_initw = lbs_w;
            m_Service.lbs_inith = lbs_h;

            if (Info.bOverlaymode)
                m_Service._overlaycarstate();

            afterServiceConnect(); //20210330

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("pos_app_mainActivity", "pos_app_service_main onServiceDisconnected");
            m_Service = null;
//			isBindService = false;
///			mServiceMessenger = null;

            Info.m_Service = null;

        }
    };

    public void display_Runstate() {
        String stemp = "";

//20210611        if(bInsertDB == true) {
//            if(msetDB) {
//                Info.insert_rundata(mlocation, 2); //drive
//                bInsertDB = false;
//            }
//        } else

        if (mnseconds % recCount == 0) {

            if (mlocation != null)
                Info.update_runlocationdata(mlocation);
        }

//        anim.pause();
        String pre_cnt = tv_remainfare.getText().toString();
        tv_remainfare.setText(mnremaindist + "");
        // Log.d("mddspeed>", mddspeed+"");
        //Log.d("mddspeed>", mddspeed+" mGPSspeed : " + (int)mGPSspeed * 3.6);
        String cur_cnt = tv_remainfare.getText().toString();
        int speed = (Integer.parseInt(cur_cnt)) - (Integer.parseInt(pre_cnt));

        // Log.d("tv_remainfare", tv_remainfare.getText().toString());

//        anim.resume();
//        AnimThread thread = new AnimThread();
///        thread.start();
        //todo: end

//        tv_nowfare.setText(mnfare + "원");
        // todo: 20210902
        long value = Long.parseLong(mnfare + "");
        DecimalFormat format = new DecimalFormat("###,###");
        tv_nowfare.setText(format.format(value));

//        if(setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            if (tv_nowfare.getText().toString().length() < 7) {
//                tv_nowfare.setTextSize(12.0f * setting.gTextDenst);
//            } else if (tv_nowfare.getText().toString().length() >= 7) {
//                tv_nowfare.setTextSize(6.0f * setting.gTextDenst);
//            } else {
//            }
//        }
        //todo: end


        m_Service._setLbsPayment(mnremaindist + "", mnfare + "");

        if (Info.REPORTREADY) {

            Info._displayLOG(true, "메인화면 요금표기", "");
            Info._displayLOG(true, "빈차등 요금전송 " + mnfare + "원", "");
        }

        if (!msetDB) {
            //추가금액
            /*tv_addPay.setText("추가 " + mAddfare + "원");
            tv_nowfare.setText((PAYMENT_COST + mAddfare) + "원");*/
        }

        stemp = String.format(Locale.getDefault(), "%.2fkm", Info.MOVEDIST / 1000.0);
        tv_boardkm.setText(stemp);

        //운행시간
        /*stemp = String.format(Locale.getDefault(), "%.1f분", mnseconds / 60.0);
        tv_boardtime.setText(stemp);*/

        if (mlocationtmp == null)
            mlocationtmp = mlocation;

        if (false) {
            if (mlocation != null) //mlocation != null)
            {
                stemp = String.format(Locale.getDefault(), "%d:%.2fm(%.0f) ", mnseconds, mlocation.getAltitude(), mlocation.getAltitude() - mlocationtmp.getAltitude());
                Log.e("date text", stemp);
                //tv_curtime.setText(stemp);
                Log.e("STEMP", stemp);
                Savealtitude(Info.g_nowKeyCode, stemp);
            }
        }

        mlocationtmp = mlocation;
//20210607        PAYMENT_COST = mnfare; //20210607 + mAddfare;

//        save_state(1, PAYMENT_COST, PAYMENT_COST, mddistance, mnremaindist);

        if (Info.USEDRIVESTATEPOWEROFF) {
            if (mlocation != null) {
                save_state_pref(Info.g_nowKeyCode, 2, System.currentTimeMillis(), mnfare, Info.MOVEDIST, mnremaindist, mfaredist,
                        (int) m_Service.mLastDTGform.distance, (float) mlocation.getLongitude(), (float) mlocation.getLatitude());

            } else {
                if (Info.REPORTREADY)
                    Info._displayLOG(true, "USEDRIVESTATEPOWEROFF gps no 저장안함.", "");

            }
        }

        //todo: 20210917
        // anim.setRepeatMode(ObjectAnimator.RESTART);

//20210917
//20210928 TODO.
        if (false) {
            if (anim != null) {
                int spd = (int) (mddspeed * 3.6 / 10);
                int factor = 0;
                if (spd == 0)
                    spd = 1;

                if (spd > 8)
                    spd = 8;

                factor = 10000 / (spd * (spd + 1));
                factor = factor / 10;
                factor = factor * 10;

//            anim.cancel();
//            anim = ObjectAnimator.ofFloat(iv_car_icon, "rotation", 0, 180);

                anim.setDuration(factor);
//            anim.start();

            }
        } else {

            double nProgress = 0;
            int ncurdist = 0;

            if (mfaredist <= CalFareBase.BASEDRVDIST) {

                nProgress = (CalFareBase.BASEDRVDIST - mnremaindist) * 1.0 / CalFareBase.BASEDRVDIST * 100;

            } else
                nProgress = (CalFareBase.INTERVAL_DIST - mnremaindist) * 1.0 / CalFareBase.INTERVAL_DIST * 100;

            Log.d("progress", " " + nProgress + " B" + CalFareBase.BASEDRVDIST + " R" + mnremaindist + " T" + mfaredist + " C" + mncurdist);

            progressremain1.setProgress((int) nProgress);

//            ncurdist = mncurdist + 10;

            ncurdist = (int) mddspeed;
            if (ncurdist > 0)
                ncurdist += 8;

            progressremain2.setProgress(ncurdist);
            displayHandler.removeMessages(90);
            displayHandler.sendEmptyMessageDelayed(90, 500);

        }

    }

    public void TimerStart() {

        second = new TimerTask() {
            @Override
            public void run() {

                displayHandler.sendEmptyMessage(99);

            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1000);
    }

    //todo:  - 바퀴 이미지 스레드 돌리기
    //todo: 20210917
    class AnimThread extends Thread {
        @Override
        public void run() {

//            int index = 0;
//            for (int i = 1; i<100; i++){
//                index += 1;
//
//                try{
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // anim.setDuration()  //변수값 파라미터로 전달..

                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
                            anim.start();
                        }
                    });
                }
            });

        }
    }
    //todo: end

    /**
     * ***********************************************************************************************************************************
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT == 24) {
            Log.d("build_ver", "24");
        } else if (Build.VERSION.SDK_INT == 25) {
            Log.d("build_ver", "25");
        } else if (Build.VERSION.SDK_INT == 26) {
            Log.d("build_ver", "26");
        }


//        Log.d(logtag+"_app_version", "local: "+Info.APP_VERSION+",  server: "+Info.SV_APP_VERSION);


        pref = getSharedPreferences("env_setting", MODE_PRIVATE);
        editor = pref.edit();

        String getPrefData_ble = pref.getString("ble_Status", "-1");
        String getPrefData_ori = pref.getString("ori_Status", "-1");
        Log.d("main_getPrefData_ble", getPrefData_ble);
        Log.d("main_getPrefData_ori", getPrefData_ori);  //1-가로, 2-세로


        Info.g_appmode = Info.APP_METER;
        Info.set_MainIntent(this, MainActivity.class);
        Info.setMainActivity(MainActivity.this); //20210325
//        setContentView(R.layout.activity_new_main);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (true) {
            if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            } else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            initializecontents(setting.gOrient);
        } else
//            initializecontents(getResources().getConfiguration().orientation);
            initializecontents(setting.gOrient);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        loadinit();
        Info.init_SQLHelper(this);

        AMBlestruct.mBTConnected = false; //20201207

        if (setting.gUseBLE == false)
            setting.BLUETOOTH_FINDEND = true;

        /** 전처리 **/
        /**
         * frame 1:빈차 2:주행 3:결제 4:결제진행 5:결제완료 6:예약
         */


        frameviewchange(1);
        //menu_submenu.setVisibility(View.GONE);

//20210419
        if (setting.gUseBLE) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Info._displayLOG(true, "GPS동작 check", "");
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Info.REPORTREADY)
                Info._displayLOG(true, "GPS꺼짐", "");
            createGpsDisabledAlert();
        }
        checkOverlayStartservice();

        TimerStart();
        Request_permission();

        registerReceiver(); //20210823

//20210917
//2010928 TODO.
        if (false) {
            anim = ObjectAnimator.ofFloat(iv_car_icon, "rotation", 0, 360);
            anim.setDuration(1000);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setRepeatCount(ValueAnimator.INFINITE);

//        anim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
////                            super.onAnimationEnd(animation);
//                anim.setDuration(50);
//                anim.start();
//            }
//        });

            anim.start();
//        animthread = new AnimThread();
///        animthread.start();

        }

//20210311        _Find_AMdevice();

        /*setting.BLUETOOTH_DEVICE_ADDRESS = "";
        setting.BLUETOOTH_DEVICE_NAME = "";*/

    }


//    @Override
//    protected void onStart() {
//
//        super.onStart();
//
//        //get getFrameLayoutSize 사이즈..
//        Log.d("getSize_width>", getFrameLayoutSize.getWidth()+"");
//        Log.d("getSize_height>", getFrameLayoutSize.getHeight()+"");
//    }


    private void chkExtraUse() {
        int extraPercent = 0;

        if (extraUse) {

            extraPercent = extraPercent + (int) (CalFareBase.mNightTimerate * 100);


        }

        if (suburbUse) {

            extraPercent = extraPercent + (int) (CalFareBase.mSuburbrate * 100);

        }

        if (complexUse) {

            extraPercent = extraPercent + (int) (CalFareBase.mComplexrate * 100);

        }

        if (extraPercent == 0) {
            btn_status.setVisibility(View.INVISIBLE);
        } else {
            btn_status.setVisibility(View.VISIBLE);
            btn_status.setText("+ 할증 " + extraPercent + "%");

//            m_Service.SendTIMS_Data(2, 0, null, "30&0");

        }


    }

    private void payExtraDefault() {
        extraUse = false;
        suburbUse = false;
        complexUse = false;
        btn_extra.setText("할증 꺼짐"); //20201215
        btn_extra.setTextColor(Color.parseColor("#000000")); //20201215
        btn_extra.setBackgroundResource(R.drawable.radius_extra_button); //20201215
        btn_suburb.setText("시외 꺼짐");
        btn_suburb.setTextColor(Color.parseColor("#000000"));
        btn_suburb.setBackgroundResource(R.drawable.radius_extra_button);
        btn_complex.setText("복합 꺼짐");
        btn_complex.setTextColor(Color.parseColor("#000000"));
        btn_complex.setBackgroundResource(R.drawable.radius_extra_button);

    }

    private void timeExtrachk() {
        if (extraUse) {
            btn_extra.setText("할증 켜짐");
            btn_extra.setTextColor(Color.parseColor("#ffffff"));
            btn_extra.setBackgroundResource(R.drawable.radius_extra_button_on_blue);

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "심야할증 시작 ", "");

            }

            m_Service.SendTIMS_Data(2, 0, null, "31&1");

        } else {
            extraUse = false;
            btn_extra.setText("할증 꺼짐");
            btn_extra.setTextColor(Color.parseColor("#000000"));
            btn_extra.setBackgroundResource(R.drawable.radius_extra_button);

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "심야할증 종료 ", "");

            }

            m_Service.SendTIMS_Data(2, 0, null, "31&0");
        }

        chkExtraUse();

    }

    private void loadinit() {

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE); //160622
        lbs_x = pref.getInt("lbs_x", -1);
        lbs_y = pref.getInt("lbs_y", -1);
        lbs_w = pref.getInt("lbs_w", 300);
        lbs_h = pref.getInt("lbs_h", 138);


//20210520
        SharedPreferences pref2 = getSharedPreferences("BLEINFO", Activity.MODE_PRIVATE); //160622
        setting.BLUETOOTH_DEVICE_ADDRESS = pref2.getString("addr", setting.BLUETOOTH_DEVICE_ADDRESS);
        setting.BLUETOOTH_DEVICE_NAME = pref2.getString("name", setting.BLUETOOTH_DEVICE_NAME);
        setting.BLUETOOTH_CARNO = pref2.getString("car_", setting.BLUETOOTH_CARNO);

//2021061
        get_totalfare_pref();

        if (Info.TIMSUSE)
            get_TIMS_pref();
    }

    private void hidenKeyboard(TextView edt) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    private void stopdriverstart() {
        m_Service.drive_endtime(); //20201112
        m_Service.set_drivestate(false);
    }

    private void frameviewchange(int tp) {

        switch (tp) {
            case 1:
                if (m_Service != null)
                    m_Service.set_payviewstate(false);
//20210611
/*
                if(Info.sqlite != null)
                {
                    String[] dayDrvRecordData = Info.sqlite.todayTotSelect().split("/");
                    String driveCount = Info.sqlite.todayDriveCount();
                    if(dayDrvRecordData[0].equals("") == true || driveCount.equals("0") == true) //20210506
                    {

                        ;

                    }
                    else if(dayDrvRecordData[1].equals("null") == false)
                    {
                        tv_todayTotalDist.setText((String.format("%.2f", (Double.parseDouble(dayDrvRecordData[0]) + Double.parseDouble(dayDrvRecordData[1])) / 1000)) + "km");
                        tv_todayTotalDrvCnt.setText(driveCount + " 회");
                        tv_todayTotalPayment.setText(dayDrvRecordData[4] + "원");
                    }
                }
*/
                //todo: 다시
                Info.init_SQLHelper(this);
                String todayCnt = Info.sqlite.todayDriveCount();
                Log.d("today_cnt", todayCnt);  //당일 총 거래횟수 나옴. (결제취소 된 것 까지나옴)

                String totalCnt = Info.sqlite.totalDriveCount();
                Log.d("total_cnt", totalCnt);


                Info.init_SQLHelper(this);
                String[] values = Info.sqlite.totSelect();
                Log.d("mtcnt_check", values.length + "");

//                mtcnt = Integer.parseInt(totalCnt);


                tv_todayTotalDist.setText((String.format("%.2f", mtddistanceB / 1000.0)) + "km");

                tv_todayTotalDrvCnt.setText(mtcnt + " 회");

                long value = Long.parseLong(mtfare + "");
                DecimalFormat format = new DecimalFormat("###,###");
                tv_todayTotalPayment.setText(format.format(value) + " 원");
                Log.d("tv_todayTotalPayment_2", (format.format(value)) + "");

//                tv_todayTotalPayment.setText(mtfare + "원");

/////////////////////

                payExtraDefault();
                emptyFrame1.setVisibility(View.INVISIBLE);   //todo: 20210917
                emptyFrame1_new.setVisibility(View.VISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.VISIBLE);
                driveFrame1.setVisibility(View.INVISIBLE);
                driveFrame2.setVisibility(View.INVISIBLE);
                paymentFrame1.setVisibility(View.INVISIBLE);
                paymentFrame2.setVisibility(View.INVISIBLE);
                payingFrame1.setVisibility(View.INVISIBLE);
                payingFrame2.setVisibility(View.INVISIBLE);
                endFrame1.setVisibility(View.INVISIBLE);
                endFrame2.setVisibility(View.INVISIBLE);

                break;

            case 2:
                if (m_Service != null)
                    m_Service.set_payviewstate(false);
//20210909
                if (Info.CALL_PAY > 0) {

                    tv_callfare.setText("+" + Info.CALL_PAY);

                } else
                    tv_callfare.setText("");

                chkExtraUse(); //20210917

//20210917                btn_status.setVisibility(View.INVISIBLE);
                emptyFrame1.setVisibility(View.INVISIBLE);
                emptyFrame1_new.setVisibility(View.INVISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.INVISIBLE);
                driveFrame1.setVisibility(View.VISIBLE);
                driveFrame2.setVisibility(View.VISIBLE);
                paymentFrame1.setVisibility(View.INVISIBLE);
                paymentFrame2.setVisibility(View.INVISIBLE);
                payingFrame1.setVisibility(View.INVISIBLE);
                payingFrame2.setVisibility(View.INVISIBLE);
                endFrame1.setVisibility(View.INVISIBLE);
                endFrame2.setVisibility(View.INVISIBLE);
                break;
            case 3:  //결제화면
                if (m_Service != null)
                    m_Service.set_payviewstate(true);
//20210909
                if (Info.CALL_PAY > 0) {

                    btn_callPayment.setBackgroundResource(R.drawable.selected_btn_touched_yellow);

                } else
                    btn_callPayment.setBackgroundResource(R.drawable.unselected_btn);

                edt_addpayment.setText("0");
                emptyFrame1.setVisibility(View.INVISIBLE);
                emptyFrame1_new.setVisibility(View.INVISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.INVISIBLE);
                driveFrame1.setVisibility(View.INVISIBLE);
                driveFrame2.setVisibility(View.INVISIBLE);
                paymentFrame1.setVisibility(View.VISIBLE);
                paymentFrame2.setVisibility(View.VISIBLE);
                payingFrame1.setVisibility(View.INVISIBLE);
                payingFrame2.setVisibility(View.INVISIBLE);
                endFrame1.setVisibility(View.INVISIBLE);
                endFrame2.setVisibility(View.INVISIBLE);
                //todo: 20220209
                if (m_Service.mbDrivestart){
                    btn_cancelpayment.setText("주 행");
                    tv_pay_card.setVisibility(View.GONE);
                    btn_cashPayment.setVisibility(View.VISIBLE);
                    btn_addPayment.setVisibility(View.VISIBLE);
                    btn_callPayment.setVisibility(View.VISIBLE);
                    pay_title.setVisibility(View.GONE);
                    layout_pay_distance.setVisibility(View.VISIBLE);
                    layout_pay_driving_payment.setVisibility(View.VISIBLE);
                    layout_pay_call_payment.setVisibility(View.VISIBLE);
                    layout_pay_add_payment.setVisibility(View.VISIBLE);
                }else{
                    btn_cancelpayment.setText("빈 차");
                }
                //todo: 20220209
///////////////////////
                break;
            case 4:
                if (m_Service != null)
                    m_Service.set_payviewstate(true);

                emptyFrame1.setVisibility(View.INVISIBLE);
                emptyFrame1_new.setVisibility(View.INVISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.INVISIBLE);
                driveFrame1.setVisibility(View.INVISIBLE);
                driveFrame2.setVisibility(View.INVISIBLE);
                paymentFrame1.setVisibility(View.INVISIBLE);
                paymentFrame2.setVisibility(View.INVISIBLE);
                payingFrame1.setVisibility(View.VISIBLE);
                payingFrame2.setVisibility(View.VISIBLE);
                endFrame1.setVisibility(View.INVISIBLE);
                endFrame2.setVisibility(View.INVISIBLE);
                //20220107
                //todo: 20220209
                if (m_Service.mbDrivestart){
                    btn_cancelpayment.setText("주 행");
                    tv_pay_card.setVisibility(View.GONE);
                    btn_cashPayment.setVisibility(View.VISIBLE);
                    btn_addPayment.setVisibility(View.VISIBLE);
                    btn_callPayment.setVisibility(View.VISIBLE);
                    pay_title.setVisibility(View.GONE);
                    layout_pay_distance.setVisibility(View.VISIBLE);
                    layout_pay_driving_payment.setVisibility(View.VISIBLE);
                    layout_pay_call_payment.setVisibility(View.VISIBLE);
                    layout_pay_add_payment.setVisibility(View.VISIBLE);

                }else {
                    btn_cancelpayment.setText("빈 차");
                }
                //todo: 20220209

///////////////////////
                break;
            case 5:
                if (m_Service != null)
                    m_Service.set_payviewstate(false);

                if (cashPay) {
                    btn_cashReceipt.setVisibility(View.VISIBLE);
                    cashPay = false;
                } else {
                    btn_cashReceipt.setVisibility(View.INVISIBLE);
                }
                payExtraDefault();
                emptyFrame1.setVisibility(View.INVISIBLE);
                emptyFrame1_new.setVisibility(View.INVISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.INVISIBLE);
                driveFrame1.setVisibility(View.INVISIBLE);
                driveFrame2.setVisibility(View.INVISIBLE);
                paymentFrame1.setVisibility(View.INVISIBLE);
                paymentFrame2.setVisibility(View.INVISIBLE);
                payingFrame1.setVisibility(View.INVISIBLE);
                payingFrame2.setVisibility(View.INVISIBLE);
                endFrame1.setVisibility(View.VISIBLE);
                endFrame2.setVisibility(View.VISIBLE);

                String stemp = String.format(Locale.getDefault(), "%.2fkm", Info.MOVEDIST / 1000.0);
                tv_finDistance.setText(stemp);
//20210607                tv_finPayment.setText(mnfare + " 원");
//                tv_finAddPay.setText(mAddfare + " 원");
//                tv_finEndPay.setText((mnfare + mAddfare) +" 원");
                tv_finPayment.setText(Info.PAYMENT_COST + " 원");
                tv_finAddPay.setText(mAddfare + " 원");
                tv_fincallpay.setText(Info.CALL_PAY + " 원");
                tv_finEndPay.setText((Info.PAYMENT_COST + mAddfare + Info.CALL_PAY) + " 원");
                mnlastcashfare = (Info.PAYMENT_COST + mAddfare + Info.CALL_PAY); //20211019

                //todo: 20210831 1758 ???
                m_Service.drive_state(777);
                //todo: end

                break;

            case 6:
                if (m_Service != null)
                    m_Service.set_payviewstate(false);

                emptyFrame1.setVisibility(View.INVISIBLE);
                emptyFrame1_new.setVisibility(View.INVISIBLE); //todo: 20210917
                emptyFrame2.setVisibility(View.INVISIBLE);
                driveFrame1.setVisibility(View.INVISIBLE);
                driveFrame2.setVisibility(View.INVISIBLE);
                paymentFrame1.setVisibility(View.INVISIBLE);
                paymentFrame2.setVisibility(View.INVISIBLE);
                payingFrame1.setVisibility(View.INVISIBLE);
                payingFrame2.setVisibility(View.INVISIBLE);
                endFrame1.setVisibility(View.VISIBLE);
                endFrame2.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String getCurDateString() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    private void Request_permission() {
        int permssionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permssionCheck != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "권한 승인이 필요합니다", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_LONG).show();

            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_STORAGE);

            makeSaveFolder();

        }

/////////////
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    makeSaveFolder();

                } else {
//                    Toast.makeText(this,"아직 저장소 권한이 없습니다.",Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    //todo: 20220208
    private void setupBluetooth(int type) { // BT setup

        menu.closeDrawer(drawerView); //20220107

        if (type == 1){ //메인- 블루투스 아이콘 클릭했을 경우
            Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
            blueIntent.putExtra("drvnum", "");
            startActivity(blueIntent);
        }else {  //빈차등메뉴 버튼 클릭했을 경우
            if (AMBlestruct.mBTConnected) {
                Intent actIntent = new Intent(this,
                        AMBleConfigActivity.class);
                startActivity(actIntent);
            } else {
                Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
                Log.d(logtag+"show_BT_list", "true");
                blueIntent.putExtra("drvnum", "");
                startActivity(blueIntent);
            }
        }

//        if (AMBlestruct.mBTConnected) {
//            Intent actIntent = new Intent(this,
//                    AMBleConfigActivity.class);
//            startActivity(actIntent);
//        } else {
//            Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
//            Log.d(logtag+"show_BT_list", "true");
//            blueIntent.putExtra("drvnum", "");
//            startActivity(blueIntent);
//        }

//        Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
//        blueIntent.putExtra("drvnum", "");
//        startActivity(blueIntent);
    }
    //todo: 20220208 end...


    //20201215
    private void _Find_AMdevice() {

        setting.BLUETOOTH_DEVICE_ADDRESS = "";
        setting.BLUETOOTH_DEVICE_NAME = "";


        Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
        blueIntent.putExtra("drvnum", AMBlestruct.AMLicense.taxinumber.substring(AMBlestruct.AMLicense.taxinumber.length() - 4));
        startActivity(blueIntent);

    }

    //20210823
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("getSize_width>", cover_layout.getWidth() + "");  //366
        Log.d("getSize_height>", cover_layout.getHeight() + ""); //392
        if (cover_layout.getWidth() <= 366) {
            if (tv_nowfare.getText().toString().length() < 7) {
                tv_nowfare.setTextSize(8.0f * setting.gTextDenst);
            } else if (tv_nowfare.getText().toString().length() >= 7) {
                tv_nowfare.setTextSize(5.0f * setting.gTextDenst);
            } else {
            }
        } else {
            if (tv_nowfare.getText().toString().length() < 7) {
                tv_nowfare.setTextSize(12.0f * setting.gTextDenst);
            } else if (tv_nowfare.getText().toString().length() >= 7) {
                tv_nowfare.setTextSize(6.0f * setting.gTextDenst);
            } else {
            }
        }
        //499
        //446
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if(setting.BLUETOOTH_DEVICE_NAME.equals("") == true) {
            _Find_AMdevice();
        }*/

        //get getFrameLayoutSize 사이즈..
//        Log.d("getSize_width>", cover_layout.getWidth()+"");
//        Log.d("getSize_height>", cover_layout.getHeight()+"");


        if (m_Service != null)
            m_Service._showhideLbsmsg(false);

        if (setting.gUseBLE) {
            if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        }

        if(Info.REPORTREADY) {
            Info._displayLOG(true, "GPS동작 check", "");
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                ;

            }
            else
                Info._displayLOG(true, "GPS정상동작", "");
        }

    }

    @Override public void onBackPressed() {
        menu.closeDrawer(drawerView);
    }

    @Override
    protected void onUserLeaveHint() {

        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {

        if(m_Service != null)
            m_Service._showhideLbsmsg(true);


        super.onPause();
    }

//20210823
    @Override
    public void onDestroy() {
        super.onDestroy();

        readyclose();

//20210917
//20210928 TODO.
        if(false) {
            if (anim != null) {
                anim.cancel();
                anim.end();
                anim.removeAllListeners();
            }
        }

    }

    //20210823
    private void readyclose()
    {

        if(second != null)
            second.cancel();

        unregisterReceiver(); //20210823

        if (m_Service != null) {

//20210823 종료 코드 전송
            AMBlestruct.mSState = "60";
            m_Service.writeBLE("15");

            m_Service.close(); //20220103

            unbindService(mServiceConnection);
        }
        m_Service = null;
        stopService(new Intent(getApplicationContext(),
                LocService.class));

    }

    private void initializecontents(int nTP) {

//        Log.d("check_initializecontents", nTP+"");

        if (nTP == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_newmain_v);
            set_frame_orient(0);

        } else {

            setContentView(R.layout.activity_newmain_h);
            set_frame_orient(1);
        }

        /*** Main Layout Button ***/
        btn_driveStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                   // btn_driveStart.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_driveStart.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_driveStart.setBackgroundColor(Color.parseColor("#2e2eae"));
                    btn_driveStart.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }
                return false;
            }
        });

        //todo: 20210917
        // 손님탑승 버튼
        btn_driveStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//20210928 TODO.
                if(false) {
                    if (false) {
                        anim = ObjectAnimator.ofFloat(iv_car_icon, "rotation", 0, 360);
                        anim.setDuration(2000);
//                anim.setRepeatCount(ObjectAnimator.INFINITE);
                        anim.start();
                    } else
                        anim.start();
                }

                // Log.d("btn_driveStart", "손님탑승 클릭");
                setStateDrive();
                faresendCount = 0; //20201207

                tv_curEmptyDist.setText("0.00km"); //20211103

                if(Info.REPORTREADY)
                    Info._displayLOG(true, "이전상태 빈차 - 주행 변경", "");
            }
        });
        //todo: end


        btn_emptyCar_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reservUse) {
                   // btn_reserv_e.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_reserv_e.setBackgroundResource(R.drawable.unselected_btn);
                    m_Service.update_BLEmeterstate("41");
                    reservUse = false;
                    setCallpay(0);
                }

                Info.CALL_PAY = 0; //20210909
            }
        });

        btn_driveCar_e.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_driveCar_e.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_driveCar_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_driveCar_e.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_driveCar_e.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });

        //todo: 20210917
        // 주행버튼
        btn_driveCar_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//20210928 TODO.
                if(false) {
                    anim = ObjectAnimator.ofFloat(iv_car_icon, "rotation", 360);
                    anim.setDuration(2000);
                    anim.start();
                }

                setStateDrive();

            }
        });
        //todo: end


        btn_manualpay_e.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_manualpay_e.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_manualpay_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_driveCar_e.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_manualpay_e.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_manualpay_e.setOnClickListener(new View.OnClickListener() {  //추가버튼
            @Override
            public void onClick(View view) {
                mChangefare = 0;

                get_manualfare(2);
            }
        });

        btn_reserv_e.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                   // btn_reserv_e.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_reserv_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_reserv_e.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    if(reservUse) {
                        // btn_reserv_e.setBackgroundColor(Color.parseColor("#97833a"));
                        btn_reserv_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);

                    }
                    else
                        btn_reserv_e.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_reserv_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //frameviewchange(6);
                if(!reservUse) {
                   // btn_reserv_e.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_reserv_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                    reservUse = true;

//20211216
//                    if(Info.AREA_CODE.equals("천안"))
//                    {
//
//                        setCallpay(0);
//
//                    }
//                    else
//                        setCallpay(1000);
//
                    do_CallPay_other(); //20211229

                }

                m_Service.update_BLEmeterstate("40");

                if(Info.REPORTREADY)
                {

                    if(Info.REPORTREADY)
                        Info._displayLOG(true, "이전상태 빈차 - 예약 변경", "");

                }

            }
        });

        //todo: 20210917
        // 빈차 숨기기 아이콘
        hideEmptyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyFrame1.setVisibility(View.GONE);
                emptyFrame1_new.setVisibility(View.VISIBLE);
            }
        });

        showEmptyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyFrame1.setVisibility(View.VISIBLE);
                emptyFrame1_new.setVisibility(View.GONE);
            }
        });
//        //todo: end

        //거래집계 버튼
        hideReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "거래내역으로 이동", Toast.LENGTH_SHORT).show();
                emptyFrame1.setVisibility(View.GONE);
                emptyFrame1_new.setVisibility(View.VISIBLE);

                show_drvhistory(); //20220110
            }
        });


        //me: 금액마감
        tv_todayreset.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    tv_todayreset.getTextColors();
                    tv_todayreset.setTextColor(Color.parseColor("#00ff00"));

                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {

                    tv_todayreset.setTextColor(Color.parseColor("#BEBEBE"));

                }
                return false;
            }
        });

        //금액마감
        tv_todayreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mtddistanceB = 0;
                mtcnt = 0;
                mtfare = 0;
                mtddistanceE = 0;

                save_totalfare_pref(0, 0, 0);

                tv_todayTotalDist.setText((String.format("%.2f", mtddistanceB / 1000.0)) + "km");

                tv_todayTotalDrvCnt.setText(mtcnt + " 회");
                tv_todayTotalPayment.setText(mtfare + " 원");
                Log.d("tv_todayTotalPayment_1", mtfare+"");
            }
        });

        /** 운 행 **/

        btn_extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(extraUse) {
                    extraUse = false;
                    btn_extra.setText("할증 꺼짐");
                    btn_extra.setTextColor(Color.parseColor("#000000"));
                    btn_extra.setBackgroundResource(R.drawable.radius_extra_button);
                } else {
                    extraUse = true;
                    btn_extra.setText("할증 켜짐");
                    btn_extra.setTextColor(Color.parseColor("#ffffff"));
                    btn_extra.setBackgroundResource(R.drawable.radius_extra_button_on);
                }

                chkExtraUse();*/
            }
        });

        btn_suburb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(suburbUse) {
                    suburbUse = false;
                    m_Service.mbSuburb = false;
                    btn_suburb.setText("시외 꺼짐");
                    btn_suburb.setTextColor(Color.parseColor("#000000"));
                    btn_suburb.setBackgroundResource(R.drawable.radius_extra_button);
                    m_Service.drive_state(AMBlestruct.MeterState.EXTRASUBURBOFF);

                    if(Info.REPORTREADY)
                        Info._displayLOG(true, "시계할증 종료", "");

                    if(suburbUseAuto) {

                        m_Service.SendTIMS_Data(2, 0, null, "32&0");

                    }
                    else
                        m_Service.SendTIMS_Data(2, 0, null, "33&0");

                } else {
                    m_Service.mbSuburb = true;
                    suburbUse = true;
                    btn_suburb.setText("시외 켜짐");
                    btn_suburb.setTextColor(Color.parseColor("#ffffff"));
                    btn_suburb.setBackgroundResource(R.drawable.radius_extra_button_on_black);
                    m_Service.drive_state(AMBlestruct.MeterState.EXTRASUBURB);

                    if(Info.REPORTREADY)
                    {

                        Info._displayLOG(true, "시계할증 시작 ", "");

                    }

                    if(suburbUseAuto) {

                        m_Service.SendTIMS_Data(2, 0, null, "32&1");

                    }
                    else
                        m_Service.SendTIMS_Data(2, 0, null, "33&1");

                }
                suburbUseAuto = false;
                chkExtraUse();

            }
        });

        btn_complex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(complexUse) {
                    complexUse = false;
                    btn_complex.setText("복합 꺼짐");
                    btn_complex.setTextColor(Color.parseColor("#000000"));
                    btn_complex.setBackgroundResource(R.drawable.radius_extra_button);
                    m_Service.drive_state(AMBlestruct.MeterState.EXTRACOMPLEXOFF);
                    m_Service.SendTIMS_Data(2, 0, null, "34&0");
                } else {
                    complexUse = true;
                    btn_complex.setText("복합 켜짐");
                    btn_complex.setTextColor(Color.parseColor("#ffffff"));
                    btn_complex.setBackgroundResource(R.drawable.radius_extra_button_on_blue);
                    m_Service.drive_state(AMBlestruct.MeterState.EXTRACOMPLEX);
                    m_Service.SendTIMS_Data(2, 0, null, "34&1");
                }
                chkExtraUse();
            }
        });

        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //요금상태
            }
        });

        btn_gpstext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_gpstext.setVisibility(View.INVISIBLE); //for report ??????
            }
        });

        btn_driveEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                   // btn_driveEnd.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_driveEnd.setBackgroundResource(R.drawable.btn_drive_end_touch);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                   // btn_driveEnd.setBackgroundColor(Color.parseColor("#2e2eae"));
                    btn_driveEnd.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }
                return false;
            }
        });
        btn_driveEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameviewchange(3);
                // 인천제한
                if(false) //20210607
                {
                    mnfare = (int)(Math.round((Double.parseDouble(mnfare + "")/100)) * 100);

                    //m_Service.update_BLEmeterstate("01");
                }


//20210607                tv_resPayment.setText(mnfare + " 원");
                tv_resPayment.setText(Info.PAYMENT_COST + " 원");
                String stemp = String.format(Locale.getDefault(), "%.2fkm", Info.MOVEDIST / 1000.0);
                tv_resDistance.setText(stemp);
                tv_rescallpay.setText(Info.CALL_PAY + " 원");
                edt_addpayment.setText(mAddfare + ""); //20210909
                //인천한정 반올림
//20210607                tv_restotpayment.setText(mnfare + " 원");
                tv_restotpayment.setText((Info.PAYMENT_COST + Info.CALL_PAY + mAddfare) + " 원");

//20210909 ???                mAddfare = 0; //20210611

//20211229
                sendbroadcast_state(5001, 3, Info.PAYMENT_COST + mAddfare + Info.CALL_PAY,
                        Info.MOVEDIST, 0); //20210823 20210827

                Log.e("drive END", m_Service.mCardmode + ""); //99

                if(m_Service.mCardmode == AMBlestruct.MeterState.EMPTYPAY)
                    return;

                if(m_Service.mCardmode == AMBlestruct.MeterState.ADDPAY)
                {
                    m_Service.update_BLEmeterstate("01");
                    return;
                }
                if(m_Service.mCardmode == AMBlestruct.MeterState.MANUALPAY)
                {
                    m_Service.update_BLEmeterstate("01");
                    return;
                }
                if(m_Service.mCardmode == AMBlestruct.MeterState.CANCELPAY)
                {
//취소시 결재기 지불만적용됨                    m_Service.update_BLEmeterstate("01");
                    return;
                }

                if(m_Service.mbDrivestart == false) //20201112
                {
                    Info.makeDriveCode();
                    bInsertDB = true;

                    _setBoardDist(0);

                    //Log.e("frame Change a", "4");
                    frameviewchange(4);

                    m_Service.drive_state(AMBlestruct.MeterState.EMPTYPAY);
                    m_Service.update_BLEmeterstate("01");
                }
                else
                {
                    //Log.e("frame Change b", "3");

//                    frameviewchange(3);
//20210823
//20210909                    if(false)
                    if(true)
                    {
//                        stopdriverstart();
                        m_Service.drive_endtime(); //20220107
                        m_Service.drive_state(AMBlestruct.MeterState.PAY);
                        m_Service.update_BLEmeterstate("01"); //?
                    }
                    else
                    {

                        m_Service.drive_endtime(); //20210823

                    }
                }
            }
        });

        btn_emptyCar_d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_emptyCar_d.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_emptyCar_d.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_emptyCar_d.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_emptyCar_d.setBackgroundResource(R.drawable.unselected_btn); //20210827
//                    btn_emptyCar_d.setTextColor(Color.parseColor("#000000")); //20210827
                }
                return false;
            }
        });
        btn_emptyCar_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

//                mndrvtotal = true; //20211022
//                btn_emptyCar_ep.performClick();
//
////20210909                btn_cashPay.performClick(); //20210823
//                btn_cashPayment.performClick();



//                mndrvtotal = true; //20211022
//20211210 get rid                btn_emptyCar_ep.performClick();

//20210909                btn_cashPay.performClick(); //20210823

                m_Service.drive_state(AMBlestruct.MeterState.PAY); //20211210 추가

                try {
//20211216 need timing
                    Thread.sleep(200);

                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }

//20211220                btn_cashPayment.performClick();
                if(AMBlestruct.mBTConnected)
                {

                    btn_cashPayment.performClick();

                }
                else {
                    mndrvPayDiv = 2; //20220103
                    btn_emptyCar_ep.performClick();
                }


/////////

/*
                m_Service.drive_state(AMBlestruct.MeterState.EMPTY);
                if(false) //20210607
                {
                    ;

//                    PAYMENT_COST = (int) (Math.round((Double.parseDouble(PAYMENT_COST + "") / 100)) * 100);
                }
                Info.end_rundata(mlocation, Info.PAYMENT_COST, 0, mAddfare, mddistance, mnseconds);
                bInsertDB = false;
                frameviewchange(1);
                if (Info.USEDRIVESTATEPOWEROFF)
                    save_state_pref("", 1, System.currentTimeMillis(), 0, 0, 0, 0, 0, 0, 0);

                if(Info.TIMSUSE == true)
                    setTIMSfinal("2");

                sendTimsAfterDrive();
                m_Service.SendTIMS_Data(2, 0, null, "05&0");


                if(Info.REPORTREADY)
                    Info._displayLOG(true, "이전상태 주행 - 빈차 변경", "");


 */
            }

        });

        btn_reserv_d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_reserv_d.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_reserv_d.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                   // btn_reserv_d.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_reserv_d.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_reserv_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*frameviewchange(6);
                m_Service.update_BLEmeterstate("40");*/
            }
        });

//20210909
        btn_complex_d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_reserv_d.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_complex_d.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    // btn_reserv_d.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_complex_d.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_complex_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_complex.performClick();

            }
        });

//20210909
        btn_surburb_d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    btn_surburb_d.setBackgroundResource(R.drawable.selected_btn_touched_green);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {

                    btn_surburb_d.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_surburb_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_suburb.performClick();

            }
        });

        /** 결 제 **/
        if(false) {
            edt_addpayment.setImeOptions(EditorInfo.IME_ACTION_DONE);
            edt_addpayment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (get_addpayment() == true)
                            hidenKeyboard(edt_addpayment);

                        return false;
                    }
                    return false;
                }
            });
            edt_addpayment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (false) //20210827
                    {
                        if (!edt_addpayment.getText().toString().equals("")) {
                            mAddfare = Integer.parseInt(edt_addpayment.getText().toString());
//20210607                    tv_restotpayment.setText(mnfare + Integer.parseInt(edt_addpayment.getText().toString()) + " 원");
                            tv_restotpayment.setText(Info.PAYMENT_COST + Integer.parseInt(edt_addpayment.getText().toString()) + " 원");

                            if (Info.REPORTREADY) {

                                Info._displayLOG(true, "추가요금 " + mAddfare + "원", "");
                                Info._displayLOG(true, "합계요금 " + Info.PAYMENT_COST + Integer.parseInt(edt_addpayment.getText().toString()) + "원", "");
                            }
                        } else {
                            mAddfare = 0;
//20210607                    tv_restotpayment.setText(mnfare + " 원");
                            tv_restotpayment.setText(Info.PAYMENT_COST + " 원");
                        }
                    }

                    edt_addpayment.setTextColor(Color.parseColor("#ffffff"));

                    if (edt_addpayment.getText().length() > 5) {

                        if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            edt_addpayment.setTextSize(setting.gTextDenst);

                        } else {
                            if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
                            {

                                edt_addpayment.setTextSize(setting.gTextDenst);
                                ;
                            } else
                                edt_addpayment.setTextSize(6.0f * setting.gTextDenst);

                        }

                    } else {
                        if (setting.gOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

                            edt_addpayment.setTextSize(1.5f * setting.gTextDenst);

                        } else {
                            if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
                            {

                                edt_addpayment.setTextSize(1.5f * setting.gTextDenst);
                                ;
                            } else
                                edt_addpayment.setTextSize(6.0f * setting.gTextDenst);

                        }
                    }

                }
            });
        }
        edt_addpayment.setShowSoftInputOnFocus(false);
        edt_addpayment.setFocusable (false);

        edt_addpayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    get_manualfare(2);
                }

                return false;
            }
        });



        btn_endPayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_endPayment.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_endPayment.setBackgroundColor(Color.parseColor("#ffc700"));
                }
                return false;
            }
        });
        btn_endPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayHandler.sendEmptyMessage(11);
                frameviewchange(4);

//20211229                sendbroadcast_state(5001, 3, Info.PAYMENT_COST + mAddfare + Info.CALL_PAY,
///                        Info.MOVEDIST, 0); //20210823 20210827
            }
        });

///////////////////////
//20210909
        btn_cashPayment.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//20211216                    btn_cashPayment.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_cashPayment.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
//20211216                    btn_cashPayment.setBackgroundColor(Color.parseColor("#ffc700"));
                    btn_cashPayment.setBackgroundResource(R.drawable.selected_btn);
                }
                return false;
            }
        });

        //[현금]버튼 클릭시
        btn_cashPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mnlastcashfare = Info.PAYMENT_COST + mAddfare + Info.CALL_PAY; //20211019
                cashPay = true;
                m_Service.send_BLEpaymenttype(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYCASH);
                m_Service.SendTIMS_Data(2, 0, null, "01&0");

            }
        });

        //todo: 20220209
//        btn_cardPayment.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//
//                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
////20211216                    btn_cardPayment.setBackgroundColor(Color.parseColor("#97833a"));
//                    btn_cardPayment.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
//
//
//                }
//                if (arg1.getAction() == MotionEvent.ACTION_UP) {
////20211216                    btn_cardPayment.setBackgroundColor(Color.parseColor("#ffc700"));
//                    btn_cardPayment.setBackgroundResource(R.drawable.selected_btn);
//                }
//                return false;
//            }
//        });

        //[카드]버튼 클릭시   //필요없는 버튼임. 사용안함.
//        btn_cardPayment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        //todo: 20220209 end..

/////////
        btn_addPayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_addPayment.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_addPayment.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_addPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                get_manualfare(2);

            }
        });

/////////
        btn_callPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Info.CALL_PAY > 0) //20220110
                    setCallpay(0);
                else
                    do_CallPay_pay(); //20211229

                displayHandler.sendEmptyMessage(11);

            }
        });

///////////////////////

        GlideDrawableImageViewTarget gifImages = new GlideDrawableImageViewTarget(iv_loadingGif);
        Glide.with(this).load(R.drawable.pay_loadings).into(gifImages);

        btn_cashPay.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_cashPay.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_cashPay.setBackgroundColor(Color.parseColor("#ffc700"));
                }
                return false;
            }
        });

        //[현금]버튼 클릭시
        btn_cashPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashPay = true;
                m_Service.send_BLEpaymenttype(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYCASH);
                m_Service.SendTIMS_Data(2, 0, null, "01&0");
            }
        });

        btn_mobilePay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_mobilePay.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_mobilePay.setBackgroundColor(Color.parseColor("#ffc700"));
                }
                return false;
            }
        });

        //[모바일] 버튼 클릭시
        btn_mobilePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appPaymentJSON();
                m_Service.send_BLEpaymenttype(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYETC);
                frameviewchange(5);

                m_Service.SendTIMS_Data(2, 0, null, "01&0");
            }
        });

        btn_payingCancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_payingCancel.setBackgroundColor(Color.parseColor("#5c5c5c"));
                    btn_payingCancel.setTextColor(Color.parseColor("#ffffff"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_payingCancel.setBackgroundColor(Color.parseColor("#999999"));
                    btn_payingCancel.setTextColor(Color.parseColor("#000000"));
                }
                return false;
            }
        });

        btn_payingCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//20210607
                if(true)
                {
//                    btn_cashPay.performClick(); //20210823
                    continue_board(); //20210823
//20210823

                    if(false) {
                        m_Service.mDrivemode = AMBlestruct.MeterState.EMPTY;
                        m_Service.mCardmode = AMBlestruct.MeterState.NONE;
                        Info.end_rundata(mlocation, Info.PAYMENT_COST, 0, mAddfare, Info.MOVEDIST, mnseconds);
                        m_Service.set_drivestate(false);
                        frameviewchange(1);
                    }
                }
//20210607                    continue_board();
            }
        });

        btn_cancelpayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_cancelpayment.setBackgroundColor(Color.parseColor("#5c5c5c"));
                    btn_cancelpayment.setTextColor(Color.parseColor("#ffffff"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_cancelpayment.setBackgroundColor(Color.parseColor("#999999"));
                    btn_cancelpayment.setTextColor(Color.parseColor("#000000"));
                }
                return false;
            }
        });
        btn_cancelpayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tv_pay_card.setVisibility(View.GONE);

                mAddfare = 0;
                LocService.CDrive_val.setmFareAdd(0); //20210917

//20210607
//                if(m_Service.mCardmode == AMBlestruct.MeterState.MANUALPAY)
                if(m_Service.mCardmode == AMBlestruct.MeterState.MANUALPAY || m_Service.mbDrivestart == false) //20220107
                {
//                    Info.end_rundata(mlocation, Info.PAYMENT_COST, 0, mAddfare, mddistance, mnseconds);
                    Info.sqlite.delete(Info.g_nowKeyCode); //20210611
                    frameviewchange(1);
                    m_Service.update_BLEmeterstate("20");
//                    m_Service.mDrivemode = AMBlestruct.MeterState.EMPTY;
//                    m_Service.mCardmode = AMBlestruct.MeterState.NONE;
                    m_Service.drive_state(AMBlestruct.MeterState.EMPTY);
                }
                else
                    continue_board();

            }
        });

        btn_cashReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_cashReceipt.setBackgroundColor(Color.parseColor("#2e2e6a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_cashReceipt.setBackgroundColor(Color.parseColor("#2e2eae"));
                }
                return false;
            }
        });
        btn_cashReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashReceipt_ = 0;
                get_Cashreceipts();
            }
        });

        btn_receipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_receipt.setBackgroundColor(Color.parseColor("#2e2e6a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_receipt.setBackgroundColor(Color.parseColor("#2e2eae"));
                }
                return false;
            }
        });
        btn_receipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(AMBlestruct.AMCardResult.msOpercode.equals(""))
                {
                    Info.makeDriveCode();
                    AMBlestruct.AMCardResult.msOpercode = Info.g_nowKeyCode;
                }
                m_Service.writeBLE("26");

                m_Service.SendTIMS_Data(2, 0, null, "10&0");


            }
        });
        btn_emptyCar_ep.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                   // btn_emptyCar_ep.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_emptyCar_ep.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_emptyCar_ep.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_emptyCar_ep.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        //me: 결제완료- 빈차버틀 클릭
        btn_emptyCar_ep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Log.d("empty_btn_final", "결제완료 빈차 클릭");
                m_Service.drive_state(AMBlestruct.MeterState.EMPTY);
//                Log.d("mndrvPayDiv", "1 " + mndrvPayDiv);
                if(mndrvPayDiv != 9 || mndrvtotal) //20211019
                {
                    Info.end_rundata(mlocation, Info.PAYMENT_COST, mndrvPayDiv, mAddfare + Info.CALL_PAY, Info.MOVEDIST, mnseconds);

                    bInsertDB = false;

                    if (Info.USEDRIVESTATEPOWEROFF)
                        save_state_pref("", 1, System.currentTimeMillis(), 0, 0, 0, 0, 0, 0, 0);

                    if (Info.TIMSUSE == true)
                        setTIMSfinal("2");

                    sendTimsAfterDrive();

                    m_Service.SendTIMS_Data(2, 0, null, "05&0");

//20211229                    sendbroadcast_state(5001, 1, Info.PAYMENT_COST + mAddfare + Info.CALL_PAY,
///                            Info.MOVEDIST, 0); //20210823 20210827

//                    Log.d("mndrvPayDiv", "2 " + mndrvPayDiv);

                }

//20211229
                sendbroadcast_state(5001, 1, Info.PAYMENT_COST + mAddfare + Info.CALL_PAY,
                        Info.MOVEDIST, 0); //20210823 20210827

                Info.CALL_PAY = 0; //20210909
                mndrvPayDiv = 0; //20211015

                frameviewchange(1);

//20210917
//20120928 TODO>
                if(false) {
                    if (anim != null)
                        anim.cancel();

                }

            }
        });
        btn_drive_ep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //frameviewchange(2);

            }
        });
        btn_reserv_ep.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                   // btn_reserv_ep.setBackgroundColor(Color.parseColor("#97833a"));
                    btn_reserv_ep.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                   // btn_reserv_ep.setBackgroundColor(Color.parseColor("#3c3c4a"));
                    btn_reserv_ep.setBackgroundResource(R.drawable.unselected_btn);
                }
                return false;
            }
        });
        btn_reserv_ep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //frameviewchange(6);
//                m_Service.update_BLEmeterstate("40");
            }
        });





        btn_menu.setOnTouchListener(new View.OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
            // 뗐을때를 구분)
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_menu.setBackgroundResource(R.drawable.ic_menu);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_menu.setBackgroundResource(R.drawable.ic_menu);
                }
                return false;
            }
        });

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//20210611
//                emptyFrame1_new.setClickable(false)
                viewframe1.setClickable(false);

                if(m_Service.mbDrivestart == false)
                {
                    menu_menualpay.setEnabled(true);
                }
                else {
                    menu_menualpay.setEnabled(false);
                    return; //20210827
                }

                menu.openDrawer(drawerView);

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(true, "메뉴버튼, 메뉴창 ", "");

                }
            }
        });




//        if (menu.isDrawerVisible()){
//            Log.d("menu_open","open");
//        }else {
//            Log.d("menu_open","false");
//        }


        btn_connBLE.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    btn_connBLE.setBackgroundResource(R.drawable.btn_bles_c);
                      btn_connBLE.setBackgroundResource(R.drawable.bluetooth_clicked);  //todo: 20220209
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(AMBlestruct.mBTConnected) {

//                        btn_connBLE.setBackgroundResource(R.drawable.btn_bles_on); //20220207
                        btn_connBLE.setBackgroundResource(R.drawable.bluetooth_green); //todo: 20220209


                    }
                    else
//                        btn_connBLE.setBackgroundResource(R.drawable.ic_bluetooth_btn);
                        btn_connBLE.setBackgroundResource(R.drawable.bluetooth_blue);  //todo: 20220209
                }
                return false;
            }
        });



        //메인- 블루투스 아이콘
        btn_connBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setupBluetooth(1);  //todo: 20220208

//                btn_gpstext.setVisibility(View.VISIBLE); //for report ??????

            }
        });

//20210823
///////////////////
        btn_navi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_navi.setBackgroundResource(R.drawable.navi_sel);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    btn_navi.setBackgroundResource(R.drawable.navi);
                }
                return false;
            }
        });

        btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendbroadcast_normal(setting.BROADCAST_SHOWAPP, 1);

            }
        });

////////////////////////////

        /************* MENU ****************/
//20201207
//20210310        menu_drvname.setText(AMBlestruct.AMLicense.drivername);
//20210310        menu_carnum.setText(AMBlestruct.AMLicense.taxinumber);

        menu_home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_home.setBackgroundResource(R.drawable.shadow_menu);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_home.setBackgroundResource(R.drawable.menu_borders);
                }
                return false;
            }
        });

        menu_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent infoIntent = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(infoIntent);
            }
        });

        menu_setting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_setting.setBackgroundResource(R.drawable.shadow_menu);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_setting.setBackgroundResource(R.drawable.menu_borders);
                }
                return false;
            }
        });

        menu_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setupBluetooth(2);  //todo: 20220208
            }
        });

        menu_menualpay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_menualpay.setBackgroundResource(R.drawable.shadow_menu);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_menualpay.setBackgroundResource(R.drawable.menu_borders);
                }
                return false;
            }
        });

        menu_menualpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChangefare = 0;

                menu.closeDrawer(drawerView);


                get_manualfare(1);
            }
        });

        menu_drvHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_drvHistory.setBackgroundResource(R.drawable.shadow_menu);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                }
                return false;
            }
        });

        menu_todayRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    menu_todayRecord.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                }
                return false;
            }
        });

        menu_yesterdayRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    menu_yesterdayRecord.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                }
                return false;
            }
        });

        menu_allRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    menu_allRecord.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_allRecord.setBackgroundResource(R.drawable.non_shadow_effect);
                }
                return false;
            }
        });

        menu_allRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blueIntent = new Intent(getApplicationContext(), PayListActivity.class);
                blueIntent.putExtra("SDATE", "a");
                startActivity(blueIntent);
            }
        });

        menu_yesterdayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blueIntent = new Intent(getApplicationContext(), DriveInfoActivity.class);
                startActivity(blueIntent);

            }
        });



        menu_todayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blueIntent = new Intent(getApplicationContext(), DayReportActivity.class);
                startActivity(blueIntent);
            }
        });

        menu_drvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(chkSubMenuUse) {
//                    menu_submenu.setVisibility(View.GONE);
//                    chkSubMenuUse = false;
//                } else {
//                    menu_submenu.setVisibility(View.GONE);
//                    chkSubMenuUse = true;
//                }
//20220107
                menu.closeDrawer(drawerView);

                show_drvhistory();
////////
            }
        });

        menu_env_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClicked == true){
                    submenu_env_setting.setVisibility(View.VISIBLE);
                    isClicked = false;
                }else {
                    submenu_env_setting.setVisibility(View.GONE);
                    isClicked = true;
                }

            }
        });

        //todo: 20211230
        // 메뉴에 블루투스/ 해상도/ 자동로그인 상태 체크
        SharedPreferences pref = getSharedPreferences("env_setting", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        String ble_Status =  pref.getString("ble_Status", "");
        String ori_Status =  pref.getString("ori_Status","");
        String gubun_Status = pref.getString("gubun_Status","");
        String auto_login_Status = pref.getString("auto_login_Status","");
        String modem_Status = pref.getString("modem_Status","");
        Log.d("checkPrefData_ble", pref.getString("ble_Status",""));
        Log.d("checkPrefData_ori", pref.getString("ori_Status",""));
        Log.d("checkPrefData_ori", pref.getString("gubun_Status",""));
        Log.d("checkPrefData_login", pref.getString("auto_login_Status",""));
        Log.d("checkPrefData_modem", pref.getString("modem_Status",""));

        switch (ble_Status){
            case "0":
                menu_ble.setText("시리얼");
                menu_ble_status.setText("OFF");
                menu_ble_status.setTextColor(Color.parseColor("#636167"));
                menu_ble_status.setBackgroundResource(R.drawable.cancel_btn_dark);
                break;
            case "true":
                menu_ble.setText("블루투스");
                menu_ble_status.setText("ON");
                menu_ble_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "1":
                menu_ble.setText("시리얼 (아이나비)");
                menu_ble_status.setText("ON");
                menu_ble_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "2":
                menu_ble.setText("시리얼 (아트뷰)");
                menu_ble_status.setText("ON");
                menu_ble_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "3":
                menu_ble.setText("시리얼 (아틀란)");
                menu_ble_status.setText("ON");
                menu_ble_status.setTextColor(Color.parseColor("#3F51B5"));
        }

        switch (ori_Status){
            case "0":
                menu_ori.setText("가로/세로");
                menu_ori_status.setText("ON");
                menu_ori_status.setTextColor(Color.parseColor("#636167"));
                break;
            case "1":
                menu_ori.setText("가로");
                menu_ori_status.setText("ON");
                menu_ori_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "2":
                menu_ori.setText("세로");
                menu_ori_status.setText("ON");
                menu_ori_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
        }

        switch (gubun_Status){
            case "0":
                menu_gubun.setText("소속");
                menu_gubun_stauts.setText("X");
                menu_gubun_stauts.setTextColor(Color.parseColor("#636167"));
                break;
            case "1":
                menu_gubun.setText("개인");
                menu_gubun_stauts.setText("ON");
                menu_gubun_stauts.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "2":
                menu_gubun.setText("법인");
                menu_gubun_stauts.setText("ON");
                menu_gubun_stauts.setTextColor(Color.parseColor("#3F51B5"));
                break;
        }

        switch (auto_login_Status){
            case "0":
                menu_auto_login.setText("자동로그인");
                menu_auto_login_status.setText("X");
                menu_auto_login_status.setTextColor(Color.parseColor("#636167"));
                break;
            case "1":
                menu_auto_login.setText("자동로그인");
                menu_auto_login_status.setText("ON");
                menu_auto_login_status.setTextColor(Color.parseColor("#3F51B5"));
                break;
            case "2":
                menu_auto_login.setText("자동로그인");
                menu_auto_login_status.setText("OFF");
                menu_auto_login_status.setTextColor(Color.parseColor("#636167"));
                break;
        }

//        switch (modem_Status){
//            case "0":
//                menu_modem.setText("모뎀설정");
//                menu_modem_status.setText("OFF");
//                menu_modem_status.setTextColor(Color.parseColor("#636167"));
//                break;
//            case "1":
//                menu_modem.setText("일반모뎀");
//                menu_modem_status.setText("ON");
//                menu_modem_status.setTextColor(Color.parseColor("#3F51B5"));
//                break;
//            case "2":
//                menu_modem.setText("rndis 우리넷");
//                menu_modem_status.setText("ON");
//                menu_modem_status.setTextColor(Color.parseColor("#3F51B5"));
//                break;
//            case "3":
//                menu_modem.setText("rndis 에이엠");
//                menu_modem_status.setText("ON");
//                menu_modem_status.setTextColor(Color.parseColor("#3F51B5"));
//                break;
//        }

        //todo: end

        menu_getReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    menu_getReceipt.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_getReceipt.setBackgroundResource(R.drawable.non_shadow_effect);
                }
                return false;
            }
        });

        menu_getReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AMBlestruct.AMCardResult.msOpercode.equals(""))
                {
                    Info.makeDriveCode();
                    AMBlestruct.AMCardResult.msOpercode = Info.g_nowKeyCode;
                }
                m_Service.writeBLE("26");

                m_Service.SendTIMS_Data(2, 0, null, "10&0");


                menu.closeDrawer(drawerView);
            }
        });

        menu_cancelPay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                    menu_cancelPay.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_cancelPay.setBackgroundResource(R.drawable.non_shadow_effect);
                }
                return false;
            }
        });


        //todo: 20211125
        // 결제취소 메뉴 클릭 시
        menu_cancelPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                //결제취소 다이얼로그 생성
                final LinearLayout dialogView;
                dialogView = (LinearLayout)View.inflate(context, R.layout.dlg_basic, null);

                final TextView msg = (TextView)dialogView.findViewById(R.id.msg);
                final Button cancelBtn = (Button)dialogView.findViewById(R.id.cancel_btn);
                final Button okayBtn = (Button)dialogView.findViewById(R.id.okay_btn);
                msg.setText("결제취소를 진행하시겠습니까?");

                final Dialog dlg = new Dialog(MainActivity.this);
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dlg.setContentView(dialogView);
                dlg.setCancelable(true);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                        menu.closeDrawer(drawerView);
                    }
                });

                //todo: 20220111
                okayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        helper = new SQLiteHelper(getBaseContext());
                        sqlite = new SQLiteControl(helper);

//                      //todo: 당일 거래내역
                        todayData = sqlite.selectToday();
                        if (todayData.length > 0){
                            String[] splt = todayData[0].split("#");   //맨 마지막 데이터
                            if (splt.length > 0){
                                drvCode = splt[0];   //운행코드 string
                                drvPay = Integer.parseInt(splt[2]);  //요금(fare)
                                payDiv = Integer.parseInt(splt[3]);  //현금/카드/모바일
                                Log.d("payDivCheck", payDiv+"");
//                                payDiv = 0;  //카드
                                addPay = Integer.parseInt(splt[4]);  //추가요금
                                Log.d("today_data[0]", todayData[0]);
                                Log.d("today_data_drvCode", drvCode+"");  //null
                                Log.d("today_data_payDivision", payDiv+"");
                                Log.d("today_data_drvPay", drvPay+"");
                                Log.d("today_data_addPay", addPay+"");

                                String strDrvPay = drvPay+"";
                                if (strDrvPay.contains("-")){
                                    Log.d("strdrvpay_confain", strDrvPay);
                                    Toast.makeText(MainActivity.this, "결제취소가 이미 처리되었습니다.", Toast.LENGTH_SHORT).show();
                                    dlg.dismiss();

                                }else {
                                    Log.d("strdrvpay", strDrvPay);

                                    if (payDiv == 0){  //현금
                                        Log.d("today_data_", "현금");
                                        AMBlestruct.AMCardFare.mstype ="05";
//20220110                                        Toast.makeText(context, "현금결제는 취소가 불가능합니다.", Toast.LENGTH_SHORT).show();
//                                        if(!AMBlestruct.AMCardResult.msOpercode.equals(""))
                                        {
                                            m_Service.writeBLE("23");
                                        }
                                        //do nothing
                                    }else if (payDiv == 1){  //카드
                                        Log.d("today_data_", "카드");

                                        //마지막 결제아이템 운행코드 가져오기
//                                        after_cancel_pay(drvCode);  //카드결제 취소

                                        AMBlestruct.AMCardFare.mstype ="01";
//                                        if(!AMBlestruct.AMCardResult.msOpercode.equals(""))
                                        {
                                            m_Service.writeBLE("23");
                                        }

                                    }else if (payDiv == 2){   //모바일(2)
                                        //do nothing
                                        Log.d("today_data_", "모바일");
                                        AMBlestruct.AMCardFare.mstype ="06";
//20220110                                        Toast.makeText(context, "모바일결제는 취소가 불가능합니다.", Toast.LENGTH_SHORT).show();
//                                        if(!AMBlestruct.AMCardResult.msOpercode.equals(""))
                                        {
                                            m_Service.writeBLE("23");
                                        }
                                    }

                                    Log.d("check_mstype", AMBlestruct.AMCardFare.mstype);


                                    frameviewchange(1);
                                    showEmptyIcon.performClick();

                                    menu.closeDrawer(drawerView);
                                }


                            }
                        }else {
                            Toast.makeText(MainActivity.this, "결제취소할 목록이 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        dlg.dismiss();

                    }
                });
                dlg.dismiss();

                DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                if (Build.VERSION.SDK_INT <= 25) {
//                    title.setTextSize(3.0f * setting.gTextDenst);
                    msg.setTextSize(3.0f * setting.gTextDenst);
                    cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                    okayBtn.setTextSize(2.5f * setting.gTextDenst);
                    width = (int) (width * 0.6);
                    height = (int) (height * 0.5);
                }else {
                    width = (int)(width * 0.9);
                    height = (int)(height * 0.5);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dlg.getWindow().getAttributes());
                lp.width = width;
                lp.height= height;
                Window window = dlg.getWindow();
                window.setAttributes(lp);

                dlg.show();

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

//                menu.closeDrawer(drawerView);
                //todo: end
            }
        });




        //todo: 20210923
        // 현금영수증
        menu_cashReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_cashReceipt.setBackgroundResource(R.drawable.shadow_effect);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    menu_cashReceipt.setBackgroundResource(R.drawable.non_shadow_effect);
                }
                return false;
            }
        });
        menu_cashReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_Cashreceipts();
            }
        });
        //todo: end

        menu_endDrv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_endDrv.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_endDrv.setBackgroundColor(Color.parseColor("#ffc700"));
                }
                return false;
            }
        });
        menu_endDrv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//20220126
                {

                    tv_todayreset.performClick();

                }

//                menu_endApp.performClick();

                if (false) {
                    if (Info.TIMSUSE) {
//20210512 for timstest. no use after
                        if (Info.APPMETERRUNSTOP == 0) {
                            Info.APPMETERRUNSTOP = 1;
                            menu_endDrv.setBackgroundColor(Color.parseColor("#3c3c4a"));
                        } else {
                            Info.APPMETERRUNSTOP = 0;
                            menu_endDrv.setBackgroundColor(Color.parseColor("#ffc700"));

                        }
                    } else {

                        if (Info.APPMETERRUNSTOP == 0) {
                            Info.APPMETERRUNSTOP = 1;
                            menu_endDrv.setBackgroundColor(Color.parseColor("#3c3c4a"));
                            m_Service.SendTIMS_Data(3, 2, null, "0");
                        } else {
                            Info.APPMETERRUNSTOP = 0;
                            menu_endDrv.setBackgroundColor(Color.parseColor("#ffc700"));
                            m_Service.SendTIMS_Data(3, 2, null, "1");
                        }

                    }

                }

///////////20210906
                if (false) //Info.TIMSUSE)
                {
                    if (Info.APPMETERRUNSTOP == 0) {
                        Info.APPMETERRUNSTOP = 1;
                        menu_endDrv.setBackgroundColor(Color.parseColor("#3c3c4a"));
                        m_Service.SendTIMS_Data(3, 2, null, "0");
                    } else {
                        Info.APPMETERRUNSTOP = 0;
                        menu_endDrv.setBackgroundColor(Color.parseColor("#ffc700"));
                        m_Service.SendTIMS_Data(3, 2, null, "1");
                    }
                }

            }

        });

        menu_endApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_endApp.setBackgroundColor(Color.parseColor("#DC143C"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_endApp.setBackgroundColor(Color.parseColor("#800000"));
                }
                return false;
            }
        });
        menu_endApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //frameviewchange(1);

                if(Info.REPORTREADY)
                {

                    if(Info.REPORTREADY)
                        Info._displayLOG(true, "이전상태 빈차 - 대기 변경", "");

                    Info._displayLOG(true, "앱종료 버튼, 앱종료 ", "");

                }

                readyclose(); //20210823

                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });


        if(Info.APP_VERSION >= Info.SV_APP_VERSION) {
            menu_update.setVisibility(View.INVISIBLE);
        }

        menu_update.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_update.setBackgroundColor(Color.parseColor("#4682B4"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_update.setBackgroundColor(Color.parseColor("#800000"));
                }
                return false;
            }
        });

        menu_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://test.psweb.kr/apk/posappmeter.apk"));
///                startActivity(intent);

                Uri uri = Uri.parse("market://details?id=com.konai.appmeter.driver");


                if(false) //Info.REPORTREADY)
                {

                    Info._displayLOG(true, "업데이트버튼클릭 ", "");
                    Info._displayLOG(true, "마켓주소 " + uri, "");
                }

                //Uri uri = Uri.parse("http://www.enpossystem.kr/posapk/posnavi.apk");

                Intent intent = new Intent(
                        Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });

        //todo: 20220128
        //환경설정 초기화
        menu_reset_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout dialogView;

                if (ori_Status.equals("1")){  //가로
                    dialogView = (LinearLayout)View.inflate(context, R.layout.dlg_inputfare_h, null);
                }else {
                    dialogView = (LinearLayout)View.inflate(context, R.layout.dlg_inputfare, null);
                }

//                dialogView = (LinearLayout)View.inflate(context, R.layout.dlg_basic, null);
                final TextView msg = (TextView)dialogView.findViewById(R.id.title);  //msg
                final TextView sub_msg = (TextView)dialogView.findViewById(R.id.title_2);
                edit_password = (EditText)dialogView.findViewById(R.id.edit_user);  //password
                final TextView won = (TextView)dialogView.findViewById(R.id.won);
                final Button cancelBtn = (Button)dialogView.findViewById(R.id.btn_cancel);  //cancel_btn
                final Button okayBtn = (Button)dialogView.findViewById(R.id.btn_ok);  //okay_btn
//                password.setVisibility(View.VISIBLE);
                won.setVisibility(View.GONE);
//                edit_password.setHint("ex) 0207");  //todo: 20220209
                edit_password.setGravity(Gravity.CENTER);
                sub_msg.setVisibility(View.VISIBLE);

                final RadioButton btn_0 = (RadioButton) dialogView.findViewById(R.id.btn_0);
                final RadioButton btn_1 = (RadioButton) dialogView.findViewById(R.id.btn_1);
                final RadioButton btn_2 = (RadioButton) dialogView.findViewById(R.id.btn_2);
                final RadioButton btn_3 = (RadioButton) dialogView.findViewById(R.id.btn_3);
                final RadioButton btn_4 = (RadioButton) dialogView.findViewById(R.id.btn_4);
                final RadioButton btn_5 = (RadioButton) dialogView.findViewById(R.id.btn_5);
                final RadioButton btn_6 = (RadioButton) dialogView.findViewById(R.id.btn_6);
                final RadioButton btn_7 = (RadioButton) dialogView.findViewById(R.id.btn_7);
                final RadioButton btn_8 = (RadioButton) dialogView.findViewById(R.id.btn_8);
                final RadioButton btn_9 = (RadioButton) dialogView.findViewById(R.id.btn_9);
                final RadioButton btn_clear = (RadioButton) dialogView.findViewById(R.id.btn_clear);
                final RadioButton btn_back = (RadioButton) dialogView.findViewById(R.id.btn_back);
                btn_0.setOnClickListener(mResetSettingListener);
                btn_1.setOnClickListener(mResetSettingListener);
                btn_2.setOnClickListener(mResetSettingListener);
                btn_3.setOnClickListener(mResetSettingListener);
                btn_4.setOnClickListener(mResetSettingListener);
                btn_5.setOnClickListener(mResetSettingListener);
                btn_6.setOnClickListener(mResetSettingListener);
                btn_7.setOnClickListener(mResetSettingListener);
                btn_8.setOnClickListener(mResetSettingListener);
                btn_9.setOnClickListener(mResetSettingListener);
                btn_back.setOnClickListener(mResetSettingListener);
                btn_clear.setOnClickListener(mResetSettingListener);


                //get current date
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
                final String currentDate = sdf.format(now);
                Log.d(logtag+"currentDate", currentDate);

                final Dialog dlg = new Dialog(MainActivity.this);
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dlg.setContentView(dialogView);
                dlg.setCancelable(true);

                msg.setTextSize(25);
                msg.setText("환경설정 초기화");

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });

                okayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edit_password.getText().toString().length() == 0){
                            Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }else {
                            if (edit_password.getText().toString().equals(currentDate)){
                                editor.putString("ble_Status","-1");
                                editor.putString("ori_Status","-1");
                                editor.putString("auto_login_Status","-1");
                                editor.putString("modem_Status","-1");
                                editor.commit();
                                finish();
                                Toast.makeText(MainActivity.this, "환경설정이 초기화 되었습니다.", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show();
                                edit_password.setText("");
                            }
                        }
                    }
                });

                DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                //todo://20211203
                if (Build.VERSION.SDK_INT <= 25){
                    msg.setTextSize(3.0f * setting.gTextDenst);
                    cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                    okayBtn.setTextSize(2.5f * setting.gTextDenst);
                    width = (int)(width * 0.8);
                    height = (int)(height * 0.7);
                }else {
                    width = (int)(width * 0.9);
                    height = (int)(height * 0.8);
                }
                //todo: end

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dlg.getWindow().getAttributes());
                lp.width = width;
                lp.height= height;
                Window window = dlg.getWindow();
                window.setAttributes(lp);

                dlg.show();
            }
        });
        //todo: 20220128 end...

        menu_close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    menu_close.setBackgroundResource(R.drawable.nbtn_exit_c);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    menu_close.setBackgroundResource(R.drawable.nbtn_exit);
                }
                return false;
            }
        });

        menu_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.closeDrawer(drawerView);
//                emptyFrame1_new.setClickable(true);

            }
        });

    }

    private void set_frame_orient(int tp)
    {

/////////////////////
        menu = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer_menu);

        /**
         * menu item match
         */

        menu_version = (TextView)findViewById(R.id.menuversion);

        menu_version.setText("ver " + Info.APP_VERSION);

        menu_env_setting = (LinearLayout)findViewById(R.id.menu_env_setting);
        submenu_env_setting = (LinearLayout)findViewById(R.id.submenu_env_setting);
        menu_ble = (TextView)findViewById(R.id.menu_ble);
        menu_ble_status = (TextView)findViewById(R.id.menu_ble_status);
        menu_ori = (TextView)findViewById(R.id.menu_ori);
        menu_ori_status = (TextView)findViewById(R.id.menu_ori_status);
        menu_gubun = (TextView)findViewById(R.id.menu_gubun);
        menu_gubun_stauts = (TextView)findViewById(R.id.menu_gubun_status);
        menu_auto_login = (TextView)findViewById(R.id.menu_auto_login);
        menu_auto_login_status = (TextView)findViewById(R.id.menu_auto_login_status);
        menu_modem = (TextView)findViewById(R.id.menu_modem);
        menu_modem_status = (TextView)findViewById(R.id.menu_modem_status);
        menu_home = (LinearLayout)findViewById(R.id.menu_home);
        menu_drvHistory = (LinearLayout)findViewById(R.id.menu_drvhistory);
        menu_submenu = (LinearLayout)findViewById(R.id.menu_submenu);
        menu_setting = (LinearLayout)findViewById(R.id.menu_setting);
        menu_menualpay = (LinearLayout)findViewById(R.id.menu_menualpay);
        menu_getReceipt = (LinearLayout)findViewById(R.id.menu_getreceipt);
        menu_cancelPay = (LinearLayout)findViewById(R.id.menu_cancelpay);
        menu_endDrv = (Button)findViewById(R.id.menu_enddrv);
        menu_endApp = (Button)findViewById(R.id.menu_endapp);
        menu_update = (Button)findViewById(R.id.menu_update);
        menu_reset_app = (Button)findViewById(R.id.reset_app_btn);
        menu_close = (Button)findViewById(R.id.menu_btnclose);

        menu_todayRecord = (TextView)findViewById(R.id.menu_todayrecord);
        menu_yesterdayRecord = (TextView)findViewById(R.id.menu_yestrecord);
        menu_allRecord = (TextView)findViewById(R.id.menu_allrecord);

        menu_cashReceipt = (LinearLayout)findViewById(R.id.menu_cashreceipt); //20210923

//20201207
//20210310        menu_drvname = (TextView)findViewById(R.id.menu_drvname);
//20210310        menu_carnum = (TextView)findViewById(R.id.menu_carnum);

//////////////////////////
        LayoutInflater inflater = null;

/*        View viewtop = null;
        frametop = (FrameLayout) findViewById(R.id.frametop); // 1. 기반이 되는 FrameLayout
        if (frametop.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frametop.removeViewAt(0);

        }

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewtop = inflater.inflate(R.layout.newmainframetop, frametop,true);
*/
        btn_menu = (Button)findViewById(R.id.nbtn_menu);
        btn_connBLE = (Button) findViewById(R.id.nbtn_connectble);  //메인- 블루투스 아이콘
//20210823
        btn_navi = (Button)findViewById(R.id.nbtn_navi);
        btn_navi.setVisibility(View.INVISIBLE);
        if(tp == 1) //for landscape
        {

            btn_navi.setVisibility(View.VISIBLE);

        }
//////////////
        tv_nowDate = (TextView)findViewById(R.id.ntv_nowdate);
        tv_nowTime = (TextView)findViewById(R.id.ntv_nowtime);

///////////////////////////
        viewframe1 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1); // 1. 기반이 되는 FrameLayout
        if (frame1.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame1.removeViewAt(0);

        }

//////////////////////////////
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe1 = inflater.inflate(R.layout.newmainframe1, frame1,true);
        /** Frame Layout **/
        emptyFrame1_new = (LinearLayout)viewframe1.findViewById(R.id.emptylayouts_new);  //todo: 20210917
        emptyFrame1 = (LinearLayout)viewframe1.findViewById(R.id.emptylayouts);
        driveFrame1 = (LinearLayout)viewframe1.findViewById(R.id.drivelayouts);
        paymentFrame1 = (LinearLayout)viewframe1.findViewById(R.id.paymentlayouts);  //결제화면 버튼 레이아웃
        payingFrame1 = (LinearLayout)viewframe1.findViewById(R.id.payinglayout);
        endFrame1 = (LinearLayout)viewframe1.findViewById(R.id.endpayment);
        textView12 = (TextView)viewframe1.findViewById(R.id.textView12);  //todo: 20220209

        /** Empty match **/
        tv_todayTotalDist = (TextView)viewframe1.findViewById(R.id.ntv_daytotdist);
        tv_todayTotalDrvCnt = (TextView)viewframe1.findViewById(R.id.ntv_daytotdrvcnt); //총 거래
        tv_todayTotalPayment = (TextView)viewframe1.findViewById(R.id.ntv_daytotpay);   //총 수입
        tv_todayreset = (TextView)viewframe1.findViewById(R.id.resettfare);       //금액마감
        tv_curEmptyDist = (TextView)viewframe1.findViewById(R.id.curemptydist);   //20211103
        hideEmptyIcon = (TextView) viewframe1.findViewById(R.id.hide_empty_icon); //todo: 20211118 돌아가기 버튼
        showEmptyIcon = (TextView)viewframe1.findViewById(R.id.show_empty_icon);  //todo: 20210917 셍세보기 버튼
        hideReport = (TextView) viewframe1.findViewById(R.id.hide_report);       //거래집계 버튼
//        showReport = (TextView)viewframe1.findViewById(R.id.show_report);

        /** 주행 **/
        tv_remainfare = (TextView)viewframe1.findViewById(R.id.ntv_remainfare);
        tv_remainfare.setVisibility(View.GONE); //20210917
        iv_car_icon = (ImageView)viewframe1.findViewById(R.id.iv_car_icon);    //me: 20210917 차/말/바람개비
        progressremain1 = viewframe1.findViewById(R.id.progressremain1);      //me: 20210928 차/말/바람개비
        progressremain2 = viewframe1.findViewById(R.id.progressremain2);     //me: 20210928 차/말/바람개비
        tv_boardkm = (TextView)viewframe1.findViewById(R.id.ntv_boardkm);
        tv_nowfare = (TextView)viewframe1.findViewById(R.id.ntv_nowfare);
        nowfare_layout = (LinearLayout)viewframe1.findViewById(R.id.nowfare_layout);
        cover_layout = (LinearLayout)viewframe1.findViewById(R.id.cover_layout);
        getFrameLayoutSize = (FrameLayout)viewframe1.findViewById(R.id.getFrameLayoutSize);
        btn_extra = (Button)viewframe1.findViewById(R.id.nbtn_extra);   //할증 껌짐/ 할증 켜짐
        btn_suburb = (Button)viewframe1.findViewById(R.id.nbtn_suburb); //시외 꺼짐/ 시외 켜짐
        btn_complex = (Button)viewframe1.findViewById(R.id.nbtn_complex);
        btn_complex.setVisibility(View.GONE);
        tv_callfare = (TextView)viewframe1.findViewById(R.id.ntv_callfare); //20210917
        btn_status = (Button)viewframe1.findViewById(R.id.ntv_status);  //+할증 30%
        btn_gpstext = (Button)viewframe1.findViewById(R.id.ntv_gpstext);
        btn_gpstext.setVisibility(View.GONE); //20210917
        btn_gpstext.setVisibility(View.INVISIBLE); //for report ??????

        //결제화면
        /** 요금계산 **/
        tv_resDistance = (TextView)viewframe1.findViewById(R.id.ntv_resdistance);
        tv_resPayment = (TextView)viewframe1.findViewById(R.id.ntv_respayment);
        tv_restotpayment_layout = (LinearLayout)viewframe1.findViewById(R.id.ntv_restotpayment_layout);  //todo: 20220209
        tv_restotpayment = (TextView)viewframe1.findViewById(R.id.ntv_restotpayment);
        edt_addpayment = (TextView)viewframe1.findViewById(R.id.nedt_addpayment);
        tv_rescallpay = (TextView)viewframe1.findViewById(R.id.ntv_rescallpay); //20210909
        //todo: 20220209
        pay_title = (TextView)viewframe1.findViewById(R.id.pay_title);
        layout_pay_distance = (LinearLayout)viewframe1.findViewById(R.id.layout_pay_distance);
        layout_pay_driving_payment = (LinearLayout)viewframe1.findViewById(R.id.layout_pay_driving_payment);
        layout_pay_call_payment = (LinearLayout)viewframe1.findViewById(R.id.layout_pay_call_payment);
        layout_pay_add_payment = (LinearLayout)viewframe1.findViewById(R.id.layout_pay_add_payment);
        //todo: 20220209 end....


        /** 결제 **/
        iv_loadingGif = (ImageView)viewframe1.findViewById(R.id.iv_loadinggif);

        /** 결제 완료 **/
        tv_finDistance = (TextView)viewframe1.findViewById(R.id.ntv_findistance);
        tv_finPayment = (TextView)viewframe1.findViewById(R.id.ntv_finpayment);
        tv_finAddPay = (TextView)viewframe1.findViewById(R.id.ntv_finaddpay);
        tv_finEndPay = (TextView)viewframe1.findViewById(R.id.ntv_finendpay);  //
        tv_fincallpay = (TextView)viewframe1.findViewById(R.id.ntv_fincallpay); //20210909

//////////////////////
        viewframe2 = null;
        frame2 = (FrameLayout) findViewById(R.id.frame2); // 1. 기반이 되는 FrameLayout
        if (frame2.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame2.removeViewAt(0);
        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe2 = inflater.inflate(R.layout.newmainframe2, frame2,true);
        /** Frame Layout **/
        emptyFrame2 = (LinearLayout)viewframe2.findViewById(R.id.emptylayouts);
        driveFrame2 = (LinearLayout)viewframe2.findViewById(R.id.drivelayouts);
        paymentFrame2 = (LinearLayout)viewframe2.findViewById(R.id.paymentlayouts);
        payingFrame2 = (LinearLayout)viewframe2.findViewById(R.id.payinglayout);
        endFrame2 = (LinearLayout)viewframe2.findViewById(R.id.endpayment);

        /** Empty match **/
        btn_driveStart = (Button)viewframe2.findViewById(R.id.nbtn_drivestart);
        btn_emptyCar_e = (Button)viewframe2.findViewById(R.id.nbtn_emptycar_e);  //빈차
        btn_driveCar_e = (Button)viewframe2.findViewById(R.id.nbtn_drivecar_e);
        btn_driveCar_e.setVisibility(View.GONE); //20210909
        btn_reserv_e = (Button)viewframe2.findViewById(R.id.nbtn_reserv_e);
        btn_manualpay_e = (Button)viewframe2.findViewById(R.id.nbtn_manualpay_e); //20210909

        /** 주행 **/
        btn_driveEnd = (Button)viewframe2.findViewById(R.id.nbtn_driveend);   //지불버튼
        btn_emptyCar_d = (Button)viewframe2.findViewById(R.id.nbtn_emptycar_d);
        btn_driveCar_d = (Button)viewframe2.findViewById(R.id.nbtn_drivecar_d);
        btn_driveCar_d.setVisibility(View.GONE); //20210909
        btn_reserv_d = (Button)viewframe2.findViewById(R.id.nbtn_reserv_d);
        btn_reserv_d.setVisibility(View.GONE); //20210909
        btn_complex_d = (Button)viewframe2.findViewById(R.id.nbtn_complex_d);
        btn_surburb_d = (Button)viewframe2.findViewById(R.id.nbtn_surburb_d);  //시외

        /** 요금계산 **/
        btn_endPayment = (Button)viewframe2.findViewById(R.id.nbtn_endpayment);
        btn_endPayment.setVisibility(View.GONE); //20210909
        btn_cancelpayment = (Button)viewframe2.findViewById(R.id.nbtn_cancelpayment);
//20210909
        layout_payment_type = (LinearLayout)viewframe2.findViewById(R.id.layout_payment_type);  //todo: 20220209
        layout_add_payment = (LinearLayout)viewframe2.findViewById(R.id.layout_add_payment);   //todo: 20220209
        tv_pay_card = (RelativeLayout) viewframe2.findViewById(R.id.tv_pay_card);   //todo: 20220209
        tv_title = (TextView)viewframe2.findViewById(R.id.tv_title);         //todo: 20220209
        tv_msg = (TextView)viewframe2.findViewById(R.id.tv_msg);           //todo: 20220209
        btn_cashPayment = (Button)viewframe2.findViewById(R.id.nbtn_cashpayment);  //현금버튼
//        btn_cardPayment = (Button)viewframe2.findViewById(R.id.nbtn_cardpayment);  //카드버튼
        btn_addPayment = (Button)viewframe2.findViewById(R.id.nbtn_addpayment); //추가요금 버튼
        btn_callPayment = (Button)viewframe2.findViewById(R.id.nbtn_callpayment); //호출요금 버튼

        /** 결제 **/
        btn_cashPay = (Button)viewframe2.findViewById(R.id.btn_paycash);
        btn_mobilePay = (Button)viewframe2.findViewById(R.id.btn_mobilepay);
        btn_payingCancel = (Button)viewframe2.findViewById(R.id.btn_payingcancel);  //취소버튼

        /** 결제 완료 **/
        btn_receipt = (Button)viewframe2.findViewById(R.id.nbtn_receipt);
        btn_cashReceipt = (Button)viewframe2.findViewById(R.id.nbtn_cashreceipt);
        btn_emptyCar_ep = (Button)viewframe2.findViewById(R.id.nbtn_emptycar_ep);  //빈차
        btn_drive_ep = (Button)viewframe2.findViewById(R.id.nbtn_drivecar_ep);  //주행
        btn_drive_ep.setVisibility(View.INVISIBLE); //20210909
        btn_reserv_ep = (Button)viewframe2.findViewById(R.id.nbtn_reserv_ep);
        btn_reserv_ep.setVisibility(View.INVISIBLE); //20210909
////////////////

        //세로
        if(tp == 0)
        {
            edt_addpayment.setTextSize(1.5f * setting.gTextDenst);
            TextView textView14 = (TextView)viewframe1.findViewById(R.id.textView14);
            textView14.setTextSize(1.5f * setting.gTextDenst);
        }

        //가로
        if(tp == 1)
        {
            tv_nowDate.setTextSize(3.0f * setting.gTextDenst);
            tv_nowTime.setTextSize(5.0f * setting.gTextDenst);

            /** Empty match **/
            tv_todayTotalDist.setTextSize(5.0f * setting.gTextDenst);
            tv_todayTotalDrvCnt.setTextSize(5.0f * setting.gTextDenst);
            tv_todayTotalPayment.setTextSize(5.0f * setting.gTextDenst);
            tv_curEmptyDist.setTextSize(5.0f * setting.gTextDenst);

            //todo: 20211201
            TextView textView5 = (TextView)viewframe1.findViewById(R.id.textView5);
            TextView textView6 = (TextView)viewframe1.findViewById(R.id.textView6);
            TextView textView7 = (TextView)viewframe1.findViewById(R.id.textView7);

            if (Build.VERSION_CODES.N >= Build.VERSION.SDK_INT){
                textView5.setTextSize(3.0f * setting.gTextDenst);
                textView6.setTextSize(3.0f * setting.gTextDenst);
                textView7.setTextSize(3.0f * setting.gTextDenst);
            }

            TextView textView5new = (TextView)viewframe1.findViewById(R.id.textView5new);
            textView5new.setTextSize(3.0f * setting.gTextDenst);
//            TextView textView7new = (TextView)viewframe1.findViewById(R.id.textView7new);
//            textView7new.setTextSize(3.0f * setting.gTextDenst);
            TextView textView6new = (TextView)viewframe1.findViewById(R.id.textView6new);
            textView6new.setTextSize(10.0f * setting.gTextDenst);

            /** 주행 **/
            tv_remainfare.setTextSize(5.0f * setting.gTextDenst);
            tv_callfare.setTextSize(5.0f * setting.gTextDenst); //20210917
            tv_boardkm.setTextSize(5.0f * setting.gTextDenst);
//            tv_nowfare.setTextSize(6.0f * setting.gTextDenst);
           // btn_extra.setTextSize(3.0f * setting.gTextDenst);
           // btn_suburb.setTextSize(3.0f * setting.gTextDenst);
            btn_complex.setTextSize(3.0f * setting.gTextDenst);
           // btn_status.setTextSize(3.0f * setting.gTextDenst);

            /** 요금계산 **/
//            tv_resDistance.setTextSize(6.0f * setting.gTextDenst);
//            tv_resPayment.setTextSize(6.0f * setting.gTextDenst);
            tv_title.setTextSize(50);  //todo: 20220209
            tv_msg.setTextSize(30);    //todo: 20220209
            pay_title.setTextSize(65);                          //todo: 20220209
            tv_restotpayment.setTextSize(11.0f * setting.gTextDenst);  //todo: 20220209
            if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
            {
                pay_title.setTextSize(55);  //todo: 20220209
                tv_restotpayment.setTextSize(2.0f * setting.gTextDenst); //todo: 20220209
                edt_addpayment.setTextSize(1.5f * setting.gTextDenst);
                ;
            }
            else
                edt_addpayment.setTextSize(6.0f * setting.gTextDenst);

            tv_rescallpay.setTextSize(6.0f * setting.gTextDenst);
            tv_rescallpay.setTextSize(6.0f * setting.gTextDenst);

            TextView textView11 = (TextView)viewframe1.findViewById(R.id.textView11);
            textView11.setTextSize(3.0f * setting.gTextDenst);
            textView12 = (TextView)viewframe1.findViewById(R.id.textView12);
            textView12.setTextSize(3.0f * setting.gTextDenst);
            TextView textView13 = (TextView)viewframe1.findViewById(R.id.textView13);
            textView13.setTextSize(3.0f * setting.gTextDenst);
            TextView textView14 = (TextView)viewframe1.findViewById(R.id.textView14);
            textView14.setTextSize(6.0f * setting.gTextDenst);
            TextView textView15 = (TextView)viewframe1.findViewById(R.id.textView15);
            textView15.setTextSize(3.0f * setting.gTextDenst);

            /** 결제 **/
            TextView textView21 = (TextView)viewframe1.findViewById(R.id.textView21);
            textView21.setTextSize(5.0f * setting.gTextDenst);

            /** 결제 완료 **/
            tv_finDistance.setTextSize(6.0f * setting.gTextDenst);
            tv_finPayment.setTextSize(6.0f * setting.gTextDenst);
            tv_finAddPay.setTextSize(6.0f * setting.gTextDenst);
            tv_finEndPay.setTextSize(9.0f * setting.gTextDenst);
            tv_fincallpay.setTextSize(6.0f * setting.gTextDenst);
            tv_fincallpay.setTextSize(6.0f * setting.gTextDenst);

            TextView textView31 = (TextView)viewframe1.findViewById(R.id.textView31);
            textView31.setTextSize(3.0f * setting.gTextDenst);
            TextView textView32 = (TextView)viewframe1.findViewById(R.id.textView32);
            textView32.setTextSize(3.0f * setting.gTextDenst);
            TextView textView33 = (TextView)viewframe1.findViewById(R.id.textView33);
            textView33.setTextSize(3.0f * setting.gTextDenst);
            TextView textView34 = (TextView)viewframe1.findViewById(R.id.textView34);
            textView34.setTextSize(3.0f * setting.gTextDenst);



            if (Build.VERSION.SDK_INT <= 25){  //네비게이션 해상도 (가로)

                Log.d("navi","navigation");

                /** 빈차 **/
                btn_driveStart.setTextSize(8.0f * setting.gTextDenst);  //손님탑승
                btn_emptyCar_e.setTextSize(6.0f * setting.gTextDenst);  //빈차
                btn_driveCar_e.setTextSize(6.0f * setting.gTextDenst);  //주행
                btn_reserv_e.setTextSize(6.0f * setting.gTextDenst);   //예약
                btn_manualpay_e.setTextSize(6.0f * setting.gTextDenst);

                textView6new.setTextSize(8.0f * setting.gTextDenst);
                tv_curEmptyDist.setTextSize(3.5f * setting.gTextDenst);  //상세보기- 운행거리 0.00km
                textView5new.setTextSize(2.5f * setting.gTextDenst);  //상세보기- 운행거리
                showEmptyIcon.setTextSize(2.5f * setting.gTextDenst); //상세보기
                tv_todayTotalDist.setTextSize(5.0f * setting.gTextDenst);  //돌아가기- 운행거리 0.00km
                textView5.setTextSize(2.5f * setting.gTextDenst);    //돌아가기- 운행거리
                textView6.setTextSize(2.5f * setting.gTextDenst);    //돌아가기- 총거래
                textView7.setTextSize(2.5f * setting.gTextDenst);    //돌아가기- 총수입
//                hideEmptyIcon.setTextSize(2.5f * setting.gTextDenst); //돌아가기

                /** 주행 **/
                btn_driveEnd.setTextSize(8.0f * setting.gTextDenst);
                btn_emptyCar_d.setTextSize(6.0f * setting.gTextDenst);
                btn_driveCar_d.setTextSize(6.0f * setting.gTextDenst);
                btn_reserv_d.setTextSize(6.0f * setting.gTextDenst);
                btn_complex_d.setTextSize(6.0f * setting.gTextDenst);
                btn_surburb_d.setTextSize(6.0f * setting.gTextDenst);

                tv_boardkm.setTextSize(3.5f * setting.gTextDenst); //distance //ex; 0.00km
                btn_extra.setTextSize(2.0f * setting.gTextDenst);  //할증꺼짐
                btn_suburb.setTextSize(2.0f * setting.gTextDenst); //시외꺼짐
                btn_status.setTextSize(2.0f * setting.gTextDenst); //+할증 20%
                tv_callfare.setTextSize(3.5f * setting.gTextDenst); //+1000



                //주행요금 텍스트
//                if (tv_nowfare.getText().toString().length() < 7) {
//                    tv_nowfare.setTextSize(8.0f * setting.gTextDenst);
//                } else if (tv_nowfare.getText().toString().length() >= 7) {
//                    tv_nowfare.setTextSize(5.0f * setting.gTextDenst);
//                } else { }

                /** 요금계산 **/
                btn_endPayment.setTextSize(6.0f * setting.gTextDenst);
                btn_cancelpayment.setTextSize(6.0f * setting.gTextDenst);
                btn_cashPayment.setTextSize(6.0f * setting.gTextDenst);
//                btn_cardPayment.setTextSize(6.0f * setting.gTextDenst);
                btn_addPayment.setTextSize(4.0f * setting.gTextDenst);
                btn_callPayment.setTextSize(4.0f * setting.gTextDenst);

//                textView14.setTextSize(4.0f * setting.gTextDenst);      //원
                tv_resDistance.setTextSize(4.0f * setting.gTextDenst);  //운행거리
                tv_resPayment.setTextSize(4.0f * setting.gTextDenst);   //운행요금
                tv_rescallpay.setTextSize(4.0f * setting.gTextDenst);   //호출요금
                edt_addpayment.setTextSize(4.0f * setting.gTextDenst);  //추가요금

                /** 결제 **/
                btn_cashPay.setTextSize(8.0f * setting.gTextDenst);
                btn_mobilePay.setTextSize(8.0f * setting.gTextDenst);
                btn_payingCancel.setTextSize(6.0f * setting.gTextDenst);

                /** 결제 완료 **/
                btn_receipt.setTextSize(8.0f * setting.gTextDenst);
                btn_cashReceipt.setTextSize(8.0f * setting.gTextDenst);
                btn_emptyCar_ep.setTextSize(6.0f * setting.gTextDenst);
                btn_drive_ep.setTextSize(6.0f * setting.gTextDenst);
                btn_reserv_ep.setTextSize(6.0f * setting.gTextDenst);

            }
            else {

                /** 빈차 **/
                btn_driveStart.setTextSize(10.0f * setting.gTextDenst);
                btn_emptyCar_e.setTextSize(10.0f * setting.gTextDenst);
                btn_driveCar_e.setTextSize(10.0f * setting.gTextDenst);
                btn_reserv_e.setTextSize(10.0f * setting.gTextDenst);
                btn_manualpay_e.setTextSize(10.0f * setting.gTextDenst);

                /** 주행 **/
                btn_driveEnd.setTextSize(7.0f * setting.gTextDenst);
                btn_emptyCar_d.setTextSize(6.0f * setting.gTextDenst);
                btn_driveCar_d.setTextSize(6.0f * setting.gTextDenst);
                btn_reserv_d.setTextSize(6.0f * setting.gTextDenst);
                btn_complex_d.setTextSize(6.0f * setting.gTextDenst);
                btn_surburb_d.setTextSize(6.0f * setting.gTextDenst);

                //주행요금 텍스트
//                if (tv_nowfare.getText().toString().length() < 7) {
//                    tv_nowfare.setTextSize(12.0f * setting.gTextDenst);
//                } else if (tv_nowfare.getText().toString().length() >= 7) {
//                    tv_nowfare.setTextSize(6.0f * setting.gTextDenst);
//                } else { }

                /** 요금계산 **/
                btn_endPayment.setTextSize(7.0f * setting.gTextDenst);
                btn_cancelpayment.setTextSize(8.0f * setting.gTextDenst);
                btn_cashPayment.setTextSize(6.0f * setting.gTextDenst);
//                btn_cardPayment.setTextSize(6.0f * setting.gTextDenst);
                btn_addPayment.setTextSize(5.0f * setting.gTextDenst);
                btn_callPayment.setTextSize(5.0f * setting.gTextDenst);

                tv_resDistance.setTextSize(6.0f * setting.gTextDenst);  //운행거리
                tv_resPayment.setTextSize(6.0f * setting.gTextDenst);   //운행요금
                tv_rescallpay.setTextSize(6.0f * setting.gTextDenst);   //호출요금
                edt_addpayment.setTextSize(6.0f * setting.gTextDenst);  //추가요금

                /** 결제 **/
                btn_cashPay.setTextSize(7.0f * setting.gTextDenst);
                btn_mobilePay.setTextSize(7.0f * setting.gTextDenst);
                btn_payingCancel.setTextSize(7.0f * setting.gTextDenst);

                /** 결제 완료 **/
                btn_receipt.setTextSize(7.0f * setting.gTextDenst);
                btn_cashReceipt.setTextSize(7.0f * setting.gTextDenst);
                btn_emptyCar_ep.setTextSize(5.0f * setting.gTextDenst);
                btn_drive_ep.setTextSize(5.0f * setting.gTextDenst);
                btn_reserv_ep.setTextSize(5.0f * setting.gTextDenst);
            }






        }

    }

    private void createGpsDisabledAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.meter_dialog_gps_unavailable))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.meter_dialog_gps_unavailable_open_setting),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){
                                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        })
                .setNegativeButton(getString(R.string.meter_dialog_gps_unavailable_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){
                                finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void checkOverlayStartservice(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.info_permission_overlay))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.setting_dialog_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id){
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + getPackageName()));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                        startActivityForResult(intent, Info.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                _start_service();
            }
        }
        else
            _start_service();


    }

    public void stopCount(View v){
        drvingValue = 0;
        //m_Service.drive_state(drvingValue);

        Info.sqlite.setUpdateLocation(Info.g_nowKeyCode, Info.PAYMENT_COST, 0, 0,  "", "", 0, 0, 99);

        Info.PAYMENT_COST = (Info.PAYMENT_COST + 50) / 100 * 100;

        AlertDialog.Builder stopDialog = new AlertDialog.Builder(this);
        stopDialog.setTitle(getString(R.string.meter_dialog_finish_title));
        stopDialog.setMessage(String.format(Locale.getDefault(), "최종요금 : %d원\n이동거리 : %dm", Info.PAYMENT_COST, Info.MOVEDIST));
        stopDialog.setPositiveButton(getString(R.string.meter_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                //finish();
            }
        });
        stopDialog.show();
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Info.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리

            } else {

                    _start_service();

            }

        }
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

            _start_service();

        }
    }


    //결제취소 메뉴 - 카드결제만 취소하기..
    public void after_cancel_pay(int mfare){

        //기존코드
        Info.makeDriveCode();
        Info.insert_rundata(mlocation, 2);
//        Info.end_run_cancel_data(mlocation, -drvPay, payDiv, -addPay, AMBlestruct.AMReceiveFare.mBoarddist, 0);
        Log.d("mfare-->", mfare+"");

        if (AMBlestruct.AMCardCancel.msType.equals("01")){
            Log.d("mfare1-->", mfare+"");
            Info.end_run_cancel_data(mlocation, (-1)*(mfare), 1, 0, AMBlestruct.AMReceiveFare.mBoarddist, 0);
        }else if (AMBlestruct.AMCardCancel.msType.equals("05")){
            Log.d("mfare5-->", mfare+"");
            Info.end_run_cancel_data(mlocation, (-1)*(mfare), 0, 0, AMBlestruct.AMReceiveFare.mBoarddist, 0);
        }else if (AMBlestruct.AMCardCancel.msType.equals("06")){  // 모바일
            Log.d("mfare6-->", mfare+"");
            Info.end_run_cancel_data(mlocation, (-1)*(mfare), 2, 0, AMBlestruct.AMReceiveFare.mBoarddist, 0);
        }else {}


    }

    public void _start_service()
    {

        if(setting.gUseBLE) {
            if (!mBluetoothAdapter.isEnabled()) //20210419
                return;

        }

        Info.g_appmode = Info.APP_METER; //20210416
        Info.set_MainIntent(this, MainActivity.class); //20210416

        Intent service = new Intent(getApplicationContext(), LocService.class);
        service.setPackage("com.konai.appmeter.driver");

//20180410
        try {

            unbindService(mServiceConnection); //20180410
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        catch (IllegalArgumentException e){

            ;
//			Log.d("pos_app_mainActivity", "pos_app_service_main not bounded ok");
        }

        m_Service = null;

        if (Build.VERSION.SDK_INT >= 26) {
            getApplicationContext().startForegroundService(service);
        } else {
            startService(service);
        }

        try {

            Thread.sleep(1000);

        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

//		startService(new Intent(getApplicationContext(), pos_app_service_main.class));

        if(m_Service == null) {
            bindService(new Intent(getApplicationContext(),
                    LocService.class), mServiceConnection, Context.BIND_AUTO_CREATE); //20180117
        }

    }

    private void afterPayment()
    {

        if(true)
        {
            mndrvtotal = false; //20211022
            btn_emptyCar_ep.performClick(); //20210917
        }
        else
            frameviewchange(5);

    }

    private void endpayment()
    {

        btn_cashPayment.performClick();

//20211220        m_Service.drive_state(AMBlestruct.MeterState.EMPTY);
//        frameviewchange(1);
//        if (Info.USEDRIVESTATEPOWEROFF)
//            save_state_pref("", 1, System.currentTimeMillis(), 0, 0, 0, 0, 0, 0, 0);

    }

    public void appPaymentJSON() {
        Thread NetworkThreads = new Thread(new NetworkThread());
        NetworkThreads.start();
    }

    private void continue_board()
    {
        frameviewchange(2);

        faresendCount = 0; //20201207
        m_Service.mbDrivestart = true;
        m_Service.mCardmode = AMBlestruct.MeterState.NONE; //20210409
        m_Service.update_BLEmeterstate("20");
        m_Service.update_BLEmeterfare(mnfare, 0, mnremaindist);
        bInsertDB = true; //20210325
    }

    //현금영수증 Dailog
    public void get_Cashreceipts() {
        LayoutInflater inflater = getLayoutInflater();
//        final View dialogView;
        final LinearLayout dialogView;
        final DialogInterface PopupDlg;

        menu.closeDrawer(drawerView); //20211019
//        dialogView = inflater.inflate(R.layout.dlg_res_payment, null);
        dialogView = (LinearLayout)View.inflate(this, R.layout.dlg_res_payment, null);

        final TextView receipt_title = (TextView) dialogView.findViewById(R.id.receipt_title);
        final Button btn_pernum = (Button) dialogView.findViewById(R.id.btn_telnum);
        final Button btn_businum = (Button) dialogView.findViewById(R.id.btn_businum);
        final Button btn_cards = (Button) dialogView.findViewById(R.id.btn_cardscan);
        final Button btn_complete = (Button) dialogView.findViewById(R.id.btn_complete);
        /**
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.create();

        PopupDlg = builder.show();
        **/
        final Dialog dlg = new Dialog(MainActivity.this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlg.setContentView(dialogView);
        dlg.setCancelable(false);

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        Log.d("navi2_display_size", width+", "+height);  // (navi_1: 1024, 600)   /    (navi_2: 1024, 538)

        if (Build.VERSION.SDK_INT <= 25){  //네비게이션 화면
            //textsize
            receipt_title.setTextSize(3.0f * setting.gTextDenst);
            btn_pernum.setTextSize(3.0f * setting.gTextDenst);
            btn_businum.setTextSize(3.0f * setting.gTextDenst);
            btn_cards.setTextSize(3.0f * setting.gTextDenst);
            btn_complete.setTextSize(3.0f * setting.gTextDenst);

            width = (int)(width * 0.6);
            height = (int)(height * 0.8);
        }else {
            //textsize
            receipt_title.setTextSize(7.0f * setting.gTextDenst);
            btn_pernum.setTextSize(7.0f * setting.gTextDenst);
            btn_businum.setTextSize(7.0f * setting.gTextDenst);
            btn_cards.setTextSize(7.0f * setting.gTextDenst);
            btn_complete.setTextSize(7.0f * setting.gTextDenst);


            width = (int)(width * 0.9);
            height = (int)(height * 0.7);
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dlg.getWindow().getAttributes());
        lp.width = width;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = height;
        Window window = dlg.getWindow();
        window.setAttributes(lp);

        dlg.show();

        btn_pernum.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_pernum.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_pernum.setBackgroundColor(Color.parseColor("#3c3c4a"));
                }
                return false;
            }
        });

        btn_pernum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashReceipt_ = 1;
//                PopupDlg.dismiss();
                dlg.dismiss();
                getReceiptInputDialog();
            }
        });

        btn_businum.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_businum.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_businum.setBackgroundColor(Color.parseColor("#3c3c4a"));
                }
                return false;
            }
        });

        btn_businum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashReceipt_ = 2;
//                PopupDlg.dismiss();
                dlg.dismiss();
                getReceiptInputDialog();
            }
        });

        btn_cards.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_cards.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_cards.setBackgroundColor(Color.parseColor("#3c3c4a"));
                }
                return false;
            }
        });

        btn_cards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashReceipt_ = 3;
//                PopupDlg.dismiss();
                dlg.dismiss();
                getReceiptInputDialog();

            }
        });

        btn_complete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_complete.setBackgroundColor(Color.parseColor("#2e2e6a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_complete.setBackgroundColor(Color.parseColor("#2e2eae"));
                }
                return false;
            }
        });

        btn_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                PopupDlg.dismiss();
                dlg.dismiss();
            }
        });


    }






    //todo: 20211115
    // 가로세로 해상도 세팅 다이얼로그
    private void setMenuOrientationCheck(){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView;
        final DialogInterface popupDlg;

        dialogView = inflater.inflate(R.layout.dlg_orientation, null);

        final Button vertical_btn = (Button)dialogView.findViewById(R.id.vertical_btn);
        final Button horizontal_btn = (Button)dialogView.findViewById(R.id.horizontal_btn);
        final Button ok_btn = (Button)dialogView.findViewById(R.id.ok_btn);

        //세로 버튼
        vertical_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true){
                    vertical_btn.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                    horizontal_btn.setBackgroundResource(R.drawable.edit_backgroud_radius);
                }else {
                    vertical_btn.setBackgroundResource(R.drawable.edit_backgroud_radius);
                }
            }
        });
        //가로 버튼
        horizontal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true){
                    horizontal_btn.setBackgroundResource(R.drawable.edit_backgroud_radius_clicked);
                    vertical_btn.setBackgroundResource(R.drawable.edit_backgroud_radius);
                }else {
                    horizontal_btn.setBackgroundResource(R.drawable.edit_backgroud_radius);
                }
            }
        });
        //확인 버튼
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);

        builder.setCancelable(true);
        builder.create();

        popupDlg = builder.show();
    }
    //todo: end


    // 수기결제 요금 다이얼로그
    private void get_manualfare(int ntype) {

        Log.d("ntype>", ntype+""); //ntype 1 수기요금, 2 추가요금.

//        btn_cancelpayment.setTextColor(Color.parseColor("#999999"));
//        btn_cancelpayment.setBackgroundResource(R.drawable.unselected_btn);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        SharedPreferences pref = getSharedPreferences("env_setting", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        String ori_Status =  pref.getString("ori_Status","");

        final LinearLayout dialogView;
        final DialogInterface PopupDlg;
        final int nfaretype = ntype;

        if (ori_Status.equals("1")){  //가로
            dialogView = (LinearLayout)View.inflate(this, R.layout.dlg_inputfare_h, null);
        }else {
            dialogView = (LinearLayout)View.inflate(this, R.layout.dlg_inputfare, null);
        }

        final TextView title = (TextView) dialogView.findViewById(R.id.title);
        edit_user = (EditText) dialogView.findViewById(R.id.edit_user);  //수기결제/ 추가요금
        edit_password = (EditText) dialogView.findViewById(R.id.edit_user);


//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        imm.hideSoftInputFromWindow(edit_user.getWindowToken(), 0);

        final RadioButton btn_0 = (RadioButton) dialogView.findViewById(R.id.btn_0);
        final RadioButton btn_1 = (RadioButton) dialogView.findViewById(R.id.btn_1);
        final RadioButton btn_2 = (RadioButton) dialogView.findViewById(R.id.btn_2);
        final RadioButton btn_3 = (RadioButton) dialogView.findViewById(R.id.btn_3);
        final RadioButton btn_4 = (RadioButton) dialogView.findViewById(R.id.btn_4);
        final RadioButton btn_5 = (RadioButton) dialogView.findViewById(R.id.btn_5);
        final RadioButton btn_6 = (RadioButton) dialogView.findViewById(R.id.btn_6);
        final RadioButton btn_7 = (RadioButton) dialogView.findViewById(R.id.btn_7);
        final RadioButton btn_8 = (RadioButton) dialogView.findViewById(R.id.btn_8);
        final RadioButton btn_9 = (RadioButton) dialogView.findViewById(R.id.btn_9);
        final RadioButton btn_clear = (RadioButton) dialogView.findViewById(R.id.btn_clear);
        final RadioButton btn_back = (RadioButton) dialogView.findViewById(R.id.btn_back);
        btn_0.setOnClickListener(mCalculatorListener);
        btn_1.setOnClickListener(mCalculatorListener);
        btn_2.setOnClickListener(mCalculatorListener);
        btn_3.setOnClickListener(mCalculatorListener);
        btn_4.setOnClickListener(mCalculatorListener);
        btn_5.setOnClickListener(mCalculatorListener);
        btn_6.setOnClickListener(mCalculatorListener);
        btn_7.setOnClickListener(mCalculatorListener);
        btn_8.setOnClickListener(mCalculatorListener);
        btn_9.setOnClickListener(mCalculatorListener);
        btn_back.setOnClickListener(mCalculatorListener);
        btn_clear.setOnClickListener(mCalculatorListener);
        final Button btn_ok = (Button) dialogView.findViewById(R.id.btn_ok);
//        btn_ok.setOnClickListener(mCalculatorListener);
        final Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);

        /**
        edit_user.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit_user.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    hidenKeyboard(edit_user);
                    String CuserNum = edit_user.getText().toString();
                    int nmanulafare = 0;

                    if (CuserNum.equals(null) || CuserNum.equals("") || Integer.parseInt(CuserNum) < 0) {
                        nmanulafare = 0;
                    } else {
                        nmanulafare = Integer.parseInt(CuserNum);
                    }

                    if(nmanulafare % 10 > 0 && nmanulafare % 10 < 10 || nmanulafare > 500000) {
                        edit_user.setTextColor(Color.parseColor("#ff0000"));
                    }
                    return false;
                }
                return false;
            }
        });
        **/

        final Dialog dlg = new Dialog(MainActivity.this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlg.setContentView(dialogView);
        dlg.setCancelable(false);

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dlg.getWindow().getAttributes());

        if (Build.VERSION.SDK_INT <= 25){  //네비게이션
            int w = dm.widthPixels;
            int h = dm.heightPixels;
            w = (int)(w * 0.7);
            h = (int)(h * 0.7);
            lp.width = w;
            lp.height = h;
            title.setTextSize(4.0f * setting.gTextDenst);

        }else {
            int width = dm.widthPixels;
            width = (int)(width * 0.9);
            lp.width = width;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            title.setTextSize(1.0f * setting.gTextDenst);
        }

        Window window = dlg.getWindow();
        window.setAttributes(lp);

        dlg.show();

        edit_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_user.setText("");
                edit_user.setTextColor(Color.parseColor("#000000"));
            }
        });

        // this는Activity의this//160919
        // 여기서 부터는 알림창의 속성 설정

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();

                tv_pay_card.setVisibility(View.GONE);  //todo: 20220209
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                if (edit_user.getText().length() == 0){
                    Toast.makeText(context, "요금을 입력하세요", Toast.LENGTH_SHORT).show();
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }else {
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    //todo: 20220209
                    //버튼쪽
                    tv_title.setText("수기결제");
                    tv_pay_card.setVisibility(View.VISIBLE);
                    btn_cashPayment.setVisibility(View.GONE);
                    btn_addPayment.setVisibility(View.GONE);
                    btn_callPayment.setVisibility(View.GONE);

                    //결과쪽
                    pay_title.setVisibility(View.VISIBLE);
                    layout_pay_distance.setVisibility(View.GONE);
                    layout_pay_driving_payment.setVisibility(View.GONE);
                    layout_pay_call_payment.setVisibility(View.GONE);
                    layout_pay_add_payment.setVisibility(View.GONE);

                    if (Build.VERSION.SDK_INT >= 26){
//                        tv_restotpayment.setTextSize(2.0f * setting.gTextDenst);
//                        tv_restotpayment_layout.Layout
//                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv_restotpayment_layout.getLayoutParams();
//                        tv_restotpayment_layout.getLayoutParams();
                    }
                    //todo: 20220209 end..

                    String CuserNum = "";
                    int nfare = 0;
                    CuserNum = edit_user.getText().toString().replaceAll(",",""); //4,500
                    Log.d(logtag+"1", CuserNum); //4500 without ","

                    if (Integer.parseInt(CuserNum) < 0){Log.d(logtag+"cuserNum", CuserNum);}

                    try{
                        if (CuserNum.equals(null) || CuserNum.equals("") || Integer.parseInt(CuserNum) < 0) {
                            Log.d(logtag+"1_!", CuserNum);
                            nfare = 0;
                        } else {
                            nfare = Integer.parseInt(CuserNum);
                            Log.d(logtag+"2", nfare+"");
                        }

                        Log.d(logtag+"3", nfare+"");

                        if(nfare % 10 > 0 && nfare % 10 < 10 || nfare > 500000) {
                            edit_user.setTextColor(Color.parseColor("#ff0000"));
                            new Thread(new SoundThread(2)).start();
                            return;
                        }
                        else if(nfare >= 100000)
                        {
                            new Thread(new SoundThread(1)).start();
                        }
                        if(nfaretype == 1) {
                            mChangefare = nfare;
                            if (mChangefare > 0)
                                displayHandler.sendEmptyMessage(10);
                        }
                        else
                        {
                            mAddfare = nfare;
                            displayHandler.sendEmptyMessage(11);
                        }
                    }catch (Exception e){}

//                PopupDlg.dismiss();
                    dlg.dismiss();
                    btn_clear.performClick();
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }


            }
        });
    }
    //todo: 20220128 end


    //todo: 20220128
    //수기결제 요금창 계산기 버튼
    private View.OnClickListener mCalculatorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i = 0; i<1; i++){

                index = i;

                switch (v.getId()){
                    case R.id.btn_0:
//                        list.add(i, "0");  //거꾸로 삽입됌
                        list.add("0");
                        break;
                    case R.id.btn_1:
                        list.add("1");
                        break;
                    case R.id.btn_2:
                        list.add("2");
                        break;
                    case R.id.btn_3:
                        list.add("3");
                        break;
                    case R.id.btn_4:
                        list.add("4");
                        break;
                    case R.id.btn_5:
                        list.add("5");
                        break;
                    case R.id.btn_6:
                        list.add("6");
                        break;
                    case R.id.btn_7:
                        list.add("7");
                        break;
                    case R.id.btn_8:
                        list.add("8");
                        break;
                    case R.id.btn_9:
                        list.add("9");
                        break;
                    case R.id.btn_back: //지우기 버튼
                        try{
                            //마지막값 지우기
                            int lastIndex = list.size() - 1;
//                            Log.d(logtag+"remove_previous_index", lastIndex+"번: "+list.get(lastIndex));

                            if (lastIndex == 0){
                                Log.d(logtag+"last_index", lastIndex+",  값: "+list.get(lastIndex));
                                list.removeAll(list);
                                edit_user.setText("");
                            }else {
                                list.remove(lastIndex);
                            }
                        }catch (Exception e){}
                        break;
                    case R.id.btn_clear: //모두 삭제버튼
//                        list.clear();
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
                int nmanulafare = 0;

                if (list.size() == 0){
                    Log.d("0000000","0000000");
                    edit_user.setText("");
                    calVal ="";
                }else {

                    calVal = TextUtils.join("",list);  //instead of String.join
                    Log.d(logtag+"calVal", calVal);
                    int calValInt = Integer.parseInt(calVal);
                    DecimalFormat format = new DecimalFormat("###,###");
                    String formatVal = format.format(calValInt);
                    edit_user.setText(formatVal);
                    Log.d(logtag+"get_edit_user_1", edit_user.getText().toString());
                }

                Log.d(logtag+"final_calVal!!", calVal+"_!");

                if (calVal.equals(null) || calVal.equals("") || Integer.parseInt(calVal) < 0){
                    nmanulafare = 0;
                }else {
                    nmanulafare = Integer.parseInt(calVal);
                }

//                if (nmanulafare % 10 > 0 && nmanulafare % 10 < 10 || nmanulafare > 500000){
//                    edit_user.setTextColor(Color.parseColor("#ff0000"));
//                    new Thread(new SoundThread(2)).start();
//                    return;
//                }else if (nmanulafare >= 100000){
//                    new Thread(new SoundThread(1)).start();
//                }

            }catch (Exception e){}

        }//onClick..
    };
    //todo: 20220128 end




    //todo: 20221028
    //환경설정 초기화
    private View.OnClickListener mResetSettingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i = 0; i<1; i++){

                index = i;

                switch (v.getId()){
                    case R.id.btn_0:
//                        list.add(i, "0");  //거꾸로 삽입됌
                        if (list.size() < 4){
                            list.add("0");
                        }
                        break;
                    case R.id.btn_1:
                        if (list.size() < 4){
                            list.add("1");
                        }
                        break;
                    case R.id.btn_2:
                        if (list.size() < 4){
                            list.add("2");
                        }
                        break;
                    case R.id.btn_3:
                        if (list.size() < 4){
                            list.add("3");
                        }
                        break;
                    case R.id.btn_4:
                        if (list.size() < 4){
                            list.add("4");
                        }
                        break;
                    case R.id.btn_5:
                        if (list.size() < 4){
                            list.add("5");
                        }
                        break;
                    case R.id.btn_6:
                        if (list.size() < 4){
                            list.add("6");
                        }
                        break;
                    case R.id.btn_7:
                        if (list.size() < 4){
                            list.add("7");
                        }
                        break;
                    case R.id.btn_8:
                        if (list.size() < 4){
                            list.add("8");
                        }
                        break;
                    case R.id.btn_9:
                        if (list.size() < 4){
                            list.add("9");
                        }
                        break;
                    case R.id.btn_back: //지우기 버튼
                        try{
                            //마지막값 지우기
                            int lastIndex = list.size() - 1;
//                            Log.d(logtag+"remove_previous_index", lastIndex+"번: "+list.get(lastIndex));

                            if (lastIndex == 0){
                                Log.d(logtag+"last_index", lastIndex+",  값: "+list.get(lastIndex));
                                list.removeAll(list);
                                edit_password.setText("");
                            }else {
                                list.remove(lastIndex);
                            }
                        }catch (Exception e){}
                        break;
                    case R.id.btn_clear: //모두 삭제버튼
//                        list.clear();
                        if (list.size() != 0){
                            list.removeAll(list);
                            Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
                        }
                        break;
                }
            }//for..

            Log.d(logtag+"list_final", list.toString()+",   사이즈: "+list.size());

            try{
                int nmanulafare = 0;

                if (list.size() == 0){
                    Log.d("0000000","0000000");
                    edit_password.setText("");
                    pwVal ="";

                }else if (list.size() <= 4){
                    pwVal = TextUtils.join("",list);  //instead of String.join
                    Log.d(logtag+"pwVal", pwVal);
                    edit_password.setText(pwVal);
                    Log.d(logtag+"get_edit_user_1", edit_password.getText().toString());
                }else {}

                Log.d(logtag+"final_calVal!!", pwVal+"_!");

                if (pwVal.equals(null) || pwVal.equals("") || Integer.parseInt(pwVal) < 0){
                    nmanulafare = 0;
                }else {
                    nmanulafare = Integer.parseInt(pwVal);
                }

            }catch (Exception e){}

        }//onClick..
    };
    //todo: 20221028 end..






    private void getReceiptInputDialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView;
        final DialogInterface PopupDlg;

        dialogView = inflater.inflate(R.layout.dlg_inptcreceipt_info, null);

        final EditText editReceiptInfo = (EditText) dialogView.findViewById(R.id.edit_receiptinfo);
        final TextView tv_CardReceipt = (TextView) dialogView.findViewById(R.id.tv_receiptinfo);
        final Button btn_ok = (Button) dialogView.findViewById(R.id.btn_ok);
        final Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

///////////////////
        AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode;
        AMBlestruct.AMCardFare.mbCard = true;
        AMBlestruct.AMCardFare.mstype = "01";
        AMBlestruct.AMCardFare.mFare = mnlastcashfare;
        AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
        AMBlestruct.AMCardFare.mCallCharge = 0; //호출요금.
        AMBlestruct.AMCardFare.mAddCharge = 0; //추가요금.
        AMBlestruct.AMCardFare.mMoveDistance = 0; //승차거리

        m_Service.writeBLE("21");
//        m_Service.update_BLEmeterstate("01"); //20211220 TODO.

////////////////////

        //
        String dlgTitle = "";
        if(cashReceipt_ == 1) {
            tv_CardReceipt.setVisibility(View.INVISIBLE);
            dlgTitle = "개인] 전화번호";
        } else if(cashReceipt_ == 2) {
            tv_CardReceipt.setVisibility(View.INVISIBLE);
            dlgTitle = "법인] 사업자번호";
        } else {
            dlgTitle = "CARD인식";
            editReceiptInfo.setVisibility(View.GONE);
            btn_ok.setVisibility(View.INVISIBLE); //20210923
//            m_Service.send_BLEpaymenttype(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYCASHRECEIPTCARD);

            m_Service.send_BLEpaymenttype(AMBlestruct.AMCardResult.msOpercode, AMBlestruct.PaymentType.BYCASHRECEIPTCARD);
        }

        builder.setTitle(dlgTitle);
        builder.setView(dialogView);


        editReceiptInfo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(cashReceipt_ != 3)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        builder.setCancelable(false);
        builder.create();
        PopupDlg = builder.show();

        editReceiptInfo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    editReceiptInfo.setText("");
                }
                return false;
            }
        });

        editReceiptInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                //Log.e("before", cs.toString() + "/" + i + "/" + i1 + "/" + i2);
            }
            @Override
            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                //Log.e("onChange", cs.toString() + "/" + i + "/" + i1 + "/" + i2);
                if(cashReceipt_ == 1) {

                    if(i == 2) {
                        editReceiptInfo.setText(editReceiptInfo.getText() + "-");
                        editReceiptInfo.setSelection(editReceiptInfo.getText().length());
                    } else if(i==7) {
                        editReceiptInfo.setText(editReceiptInfo.getText() + "-");
                        editReceiptInfo.setSelection(editReceiptInfo.getText().length());
                    }

                    if(cs.length() > 13) //20210729
                    {
                        editReceiptInfo.setText(editReceiptInfo.getText().toString().substring(0, 13));
                        editReceiptInfo.setSelection(editReceiptInfo.length());
                    }

                } else if(cashReceipt_ == 2) {

                    if(i == 2) {
                        editReceiptInfo.setText(editReceiptInfo.getText() + "-");
                        editReceiptInfo.setSelection(editReceiptInfo.getText().length());
                    } else if(i==5) {
                        editReceiptInfo.setText(editReceiptInfo.getText() + "-");
                        editReceiptInfo.setSelection(editReceiptInfo.getText().length());
                    }

                    if(cs.length() > 12) //20210729
                    {

                        editReceiptInfo.setText(editReceiptInfo.getText().toString().substring(0, 12));
                        editReceiptInfo.setSelection(editReceiptInfo.length());
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editReceiptInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReceiptInfo.setText("");
            }
        });


        // this는Activity의this//160919
        // 여기서 부터는 알림창의 속성 설정

        btn_cancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_cancel.setBackgroundColor(Color.parseColor("#97833a"));
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    btn_cancel.setBackgroundColor(Color.parseColor("#3c3c4a"));
                }
                return false;
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                m_Service.drive_state(AMBlestruct.MeterState.EMPTY); //20211019
                PopupDlg.dismiss();
            }

        });

        btn_ok.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn_ok.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    //btn_ok.setBackgroundColor(Color.parseColor("#2e2eae"));
                    btn_ok.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
                }
                return false;
            }
        });



        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo: 20220119
                hidenKeyboard(editReceiptInfo);
//                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                //todo: end

                if(cashReceipt_ == 1 || cashReceipt_ == 2) {

                    if (cashReceipt_ == 1) {

//                    m_Service.send_CashReceiptType(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYCASHRECEIPT_G, editReceiptInfo.getText().toString().replaceAll("-", ""));
                        m_Service.send_CashReceiptType(AMBlestruct.AMCardResult.msOpercode, AMBlestruct.PaymentType.BYCASHRECEIPT_G, editReceiptInfo.getText().toString().replaceAll("-", ""));
                    } else if (cashReceipt_ == 2) {
//                    m_Service.send_CashReceiptType(Info.g_nowKeyCode, AMBlestruct.PaymentType.BYCASHRECEIPT_C, editReceiptInfo.getText().toString().replaceAll("-", ""));
                        m_Service.send_CashReceiptType(AMBlestruct.AMCardResult.msOpercode, AMBlestruct.PaymentType.BYCASHRECEIPT_C, editReceiptInfo.getText().toString().replaceAll("-", ""));
                    }
                }
//20211019                PopupDlg.dismiss();
                //todo: 20220119
                PopupDlg.dismiss();
                //todo: end
            }

        });

    }

    class NetworkThread implements Runnable {
        public void run() {
            String result = "";
            String URLs = "http://118.33.122.28:10210/kod_etn/callcenter/purchase";
            InputStream is = null;

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(URLs);

                JSONObject main = new JSONObject();
                JSONObject trList = new JSONObject();
                JSONArray trArray = new JSONArray();

                trList.accumulate("cardType", "03");
                trList.accumulate("par","Q1588B6616FFD08060929CD77FB");
                trList.accumulate("posEntryMode","03");
                trList.accumulate("tips",0);
                /*trList.accumulate("trAmount",PAYMENT_COST*0.9);
                trList.accumulate("vat",PAYMENT_COST*0.1);*/

                trList.accumulate("trAmount",90);
                trList.accumulate("vat",10);

                trArray.put(trList);

                main.accumulate("merchantId", "410111810359101");
                main.accumulate("trList", trArray);

                StringEntity json = new StringEntity(main.toString());

                post.setEntity(json);

                post.setHeader("Content-Type", "application/json");
                post.setHeader("Accept", "application/json");

                HttpResponse httpResponse = httpclient.execute(post);
                is = httpResponse.getEntity().getContent();

                if(is != null) {
                    result = "work";
                } else {
                    result = "no work";
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setStateDrive()
    {
        mnSendfare = 0; //20210407
        mAddfare = 0; //20210909

        if(reservUse) {
            btn_reserv_e.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
            reservUse = false;
        }

        frameviewchange(2);
        if(m_Service.mbDrivestart)
            return;

        Info.sqlite.setUpdateTotalData(0, 0, (int)memptydistance, memptyseconds, 0);
        Info.makeDriveCode();
//20210611
        Info.insert_rundata(mlocation, 2); //drive

        bInsertDB = true;
        m_Service.drive_state(AMBlestruct.MeterState.DRIVE);
        _setBoardDist(0);
        mnseconds = 0;

        save_TIMS_pref(1);
        m_Service.SendTIMS_Data(2, 0, null, "20&0");
        sendbroadcast_state(5001, 2, 0, 0, 0); //20210823 20210827
    }

    private void sendTimsAfterDrive() {
        if(Info.TIMSUSE == false)
            return;

        String result = ReadTextFile(Info.g_nowKeyCode + ".txt", "TIMS");

        if (!result.equals("noFile")) {
            setSendTIMSVO(result);
        }
    }

    private void afterServiceConnect()
    {
        if(m_Service != null && Info.USEDRIVESTATEPOWEROFF)
            get_state_pref();

        Info.APPMETERRUNSTOP = 0;

        if(Info.TIMSUSE) {
            m_Service.SendTIMS_Data(4, 2, null, "1"); //차량인증.
            m_Service.SendTIMS_Data(5, 2, null, "1"); //운전자격인증
        }
        if(Suburbs.mSuburbOK == true)
            Suburbs.point_suburbtmp();
//20210823
        displayHandler.sendEmptyMessageDelayed(-1, 1000);
    }

    private void _setBoardDist(int distance)
    {
        mddistance = distance;
        Info.MOVEDIST = distance;
    }

    private void _licens_fail( int iTP)
    {
        if(true) //20211206
        {
            Info.TIMSUSE = false;
            return;
        }

        Info._displayLOG(true, "운영을 종료합니다. 앱종료진행!", "");

        if (iTP == 98) {
            // 팝업
            AlertDialog alert;
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setTitle("인증실패")
                    .setMessage(
                            "차량번호 인증 실패했습니다!\n종료합니다.\n")
                    .setPositiveButton("종료",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int whichButton) {

                                    menu_endApp.performClick();
                                }
                            });
            alert = alert_confirm.create();
            alert.show();

            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(30);
            textView.setBackgroundColor(Color.parseColor("#ffc700"));
            textView.setTextColor(Color.parseColor("#000000"));
            Button btn2 = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button btn1 = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn2.setTextSize(30);
            btn1.setTextSize(30);
            btn1.setBackgroundColor(Color.parseColor("#2e2eae"));
            btn1.setTextColor(Color.parseColor("#FFFFFF"));
        }
        else if (iTP == 97) {
            // 팝업
            AlertDialog alert;
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setTitle("인증실패")
                    .setMessage(
                            "운전자격 인증 실패했습니다!\n종료합니다.")
                    .setPositiveButton("종료",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int whichButton) {

                                    menu_endApp.performClick();
                                }
                            });
            alert = alert_confirm.create();
            alert.show();

            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(30);
            textView.setBackgroundColor(Color.parseColor("#ffc700"));
            textView.setTextColor(Color.parseColor("#000000"));
            Button btn2 = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button btn1 = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn2.setTextSize(30);
            btn1.setTextSize(30);
            btn1.setBackgroundColor(Color.parseColor("#2e2eae"));
            btn1.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

//20210827
    private boolean get_addpayment()
    {
        if(!edt_addpayment.getText().toString().equals("")) {
            mAddfare = Integer.parseInt(edt_addpayment.getText().toString());
//20210607                    tv_restotpayment.setText(mnfare + Integer.parseInt(edt_addpayment.getText().toString()) + " 원");

            if(mAddfare % 10 < 10 && mAddfare % 10 > 0 || mAddfare > 500000)
            {
                mAddfare = 0;
                edt_addpayment.setTextColor(Color.parseColor("#FF0000"));
                new Thread(new SoundThread(2)).start();
                return false;
            }
            else if(mAddfare >= 100000)
            {
                new Thread(new SoundThread(1)).start();
            }

            tv_restotpayment.setText(Info.PAYMENT_COST + Info.CALL_PAY + mAddfare + " 원");

            if(Info.REPORTREADY)
            {
                Info._displayLOG(true, "추가요금 " + mAddfare + "원", "");
                Info._displayLOG(true, "합계요금 " + Info.PAYMENT_COST + mAddfare + "원", "");
            }
            return true;

        } else {
            mAddfare = 0;
//20210607                    tv_restotpayment.setText(mnfare + " 원");
            tv_restotpayment.setText(Info.PAYMENT_COST + " 원" + Info.CALL_PAY);
            return true;
        }
    }

//20210823
////////////////////
    private void registerReceiver() {


        IntentFilter filter;
        filter = new IntentFilter(setting.BROADCAST_TMSG);

        if(mReceiver != null)
            return;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //			<action android:name="com.artncore.power.action.DTG_SHUTDOWN" />
                ///			<action android:name="com.artncore.power.action.DTG_RESUME" />
                Log.d("MemberCertActivity", "appmeter tomsg");
                if(action.equals(setting.BROADCAST_TMSG)) {
                    int msg = intent.getIntExtra("msgID", 0);

                    if(msg == 5100)
                    {

                        Log.d("MemberCertActivity", "appmeter tomsg5100");

                    }
                    else if(msg == 5101)
                    {

                        int state = intent.getIntExtra("value", 0);

                        Log.d("MemberCertActivity", "appmeter tomsg5101 " + state);

                        switch (state)
                        {
//                            01: 지불
//                            05: 빈차
//                            20: 주행
//                            30: 할증
//                            31: 자동할증
//                            32: 시계할증-자동
//                            33: 시계할증-수동
//                            34: 복합할증
//                            40: 예약/호출
//                            50: 휴무
//                            60: 앱 종료

                            case 5: //빈차.
//                                if(m_Service != null)
///                                    m_Service._set_by_otherapp(msg, state);
                                btn_emptyCar_e.performClick();
                                break;

                            case 20: //주행.
//                                if(m_Service != null)
//                                   m_Service._set_by_otherapp(msg, state);
//                                btn_driveCar_e.performClick();
                                btn_driveStart.performClick(); //20210909


                                Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
                                sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(sendIntent);

                                break;

                            case 40: //예약설정
//                                if(m_Service != null)
///                                    m_Service._set_by_otherapp(msg, state);
                                btn_reserv_e.performClick();

                                setCallpay(intent.getIntExtra("call_pay", 0));
//                                Info.CALL_PAY = intent.getIntExtra("call_pay", 0);

                                break;

                            case 41: //예약해제
//                                if(m_Service != null)
///                                    m_Service._set_by_otherapp(msg, state);
                                btn_emptyCar_e.performClick();

                                break;

                            case 50: //휴무.
                                if(m_Service != null)
//                                    m_Service._set_by_otherapp(msg, state);
                                    m_Service.update_BLEmeterstate("50");

                                break;

                                //20220105
                            case 51:
                                if(m_Service != null)
//                                    m_Service._set_by_otherapp(msg, state);
                                    m_Service.update_BLEmeterstate("05");

                                break;
                        }


                    } //if(msg == 5002)
                }

            }

        };

        this.registerReceiver(mReceiver, filter);

        Log.d("MemberCertActivity", "registerReceiver");

    }

    private void unregisterReceiver() {

        Log.d("MemberCertActivity", "unregisterReceiver");
        if(mReceiver != null)
        {

            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public void sendbroadcast_state(int msg, int ndrive_type, int nfinal_pay, int nfinal_dis, int npay_type)
    {

        Intent intent = new Intent(setting.BROADCAST_FMSG);
        intent.putExtra("msgID", msg);
        intent.putExtra("driver_type", ndrive_type);
        intent.putExtra("pay_type", npay_type);
        intent.putExtra("final_dis", nfinal_dis);
        intent.putExtra("final_pay", nfinal_pay);
        sendBroadcast(intent);

//        switch (msg)
//        {
//            case 5001:
//                break;
//
//            case 5002: //show other app
//            {
//                if(nvalue == 1) //for navigation exe
//                {
//
//                }
//                else(nvalue == 2) // for call.
//                {
//
//                }
//
//            }
//                break;
//        }


    }

    public void sendbroadcast_normal(int msg, int nvalue)
    {
        Intent intent = new Intent(setting.BROADCAST_FMSG);
        intent.putExtra("msgID", msg);
        intent.putExtra("value", nvalue);
        sendBroadcast(intent);

//        switch (msg)
//        {
//            case 5001:
//                break;
//
//            case 5002: //show other app
//            {
//                if(nvalue == 1) //for navigation exe
//                {
//
//                }
//                else(nvalue == 2) // for call.
//                {
//
//                }
//
//            }
//                break;
//        }


    }


    //20210909
    private void setCallpay(int npay)
    {

//20211216        if(Info.AREA_CODE.equals("파주"))
//        {
//
//            Info.CALL_PAY = npay;
//
//        }
//        else
//            Info.CALL_PAY = 0;

        Info.CALL_PAY = npay;

        LocService.CDrive_val.setmFareCallPay(Info.CALL_PAY); //20210917

    }

//20211229 20211216
    private void do_CallPay_other()
    {
        if(Info.AREA_CODE.equals("파주"))
        {

            setCallpay(1000);

        }
        else
            setCallpay(0);

    }

//20211229
    private void do_CallPay_pay()
    {
        if(Info.AREA_CODE.equals("대전"))
        {

            setCallpay(0);

        }
        else
            setCallpay(1000);

    }

//20220110
    private void show_drvhistory()
    {
        if(AMBlestruct.mBTConnected) {
            Intent i = new Intent(context, AMBleConfigActivity.class);
            i.putExtra("history", "Y");
            startActivity(i);
        }
        else{
            //빈차등과 연결을 확인하세요.
            final LinearLayout dialogView;
            dialogView = (LinearLayout)View.inflate(context, R.layout.dlg_basic, null);

            final TextView msg = (TextView)dialogView.findViewById(R.id.msg);
            final Button cancelBtn = (Button)dialogView.findViewById(R.id.cancel_btn);
            final Button okayBtn = (Button)dialogView.findViewById(R.id.okay_btn);
            msg.setText("거래내역을 조회할 수 없습니다.\n\n빈차등 연결을 확인하세요.");
            cancelBtn.setVisibility(View.GONE);

            final Dialog dlg = new Dialog(MainActivity.this);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dlg.setContentView(dialogView);
            dlg.setCancelable(true);

            okayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });

            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;

            if (Build.VERSION.SDK_INT <= 25){
                msg.setTextSize(3.0f * setting.gTextDenst);
                cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                okayBtn.setTextSize(2.5f * setting.gTextDenst);
                width = (int)(width * 0.6);
                height = (int)(height * 0.5);
            }else {
                width = (int)(width * 0.9);
                height = (int)(height * 0.5);
            }

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dlg.getWindow().getAttributes());
            lp.width = width;
            lp.height= height;
            Window window = dlg.getWindow();
            window.setAttributes(lp);

            dlg.show();

        }
    }

//20210827
    class SoundThread implements Runnable {

        int cmd = -1;
        MediaPlayer mPlayerthis = null;

        SoundThread(int cmd) {
            this.cmd = cmd;
        }

        @Override
        public void run() {

            if (cmd == 1) {

                mPlayer = MediaPlayer.create(getApplicationContext(),
                        R.raw.payhigh);
                if (mPlayer != null) {
                    mPlayerthis = mPlayer;
                    mPlayer.start();

                }

            } else if (cmd == 2) {
                mPlayer = MediaPlayer.create(getApplicationContext(),
                        R.raw.paylimit);
                if (mPlayer != null) {
                    mPlayerthis = mPlayer;
                    mPlayer.start();

                }

                ///////////////

                try {

                    while (true) {

                        if (mPlayerthis == null)
                            break;

                        if (mPlayerthis != mPlayer) {
                            //						Log.d("mainactivity", "sound thread1");

                            if (mPlayerthis.isPlaying() == true) {
                                mPlayerthis.stop();
                                mPlayerthis.reset();
                                mPlayerthis.release();
                                //							Log.d("mainactivity", "sound thread2");
                            }
                            break;
                        }

                        if (mPlayerthis.isPlaying() == false) {
                            mPlayerthis.stop();
                            mPlayerthis.reset();
                            mPlayerthis.release();
                            //						Log.d("mainactivity", "sound thread3");
                            break;

                        }

                        Thread.sleep(500);
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                cmd = -1;
            }
        }
    }
/////////////////////////
 }
