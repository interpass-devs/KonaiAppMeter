package com.konai.appmeter.driver.service;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.VO.TIMS_UnitVO;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.Suburbs;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.socket.AMBluetoothLEManager;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;
import com.konai.appmeter.driver.MainActivity;
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.struct.AMdtgform;
import com.konai.appmeter.driver.struct.GetGeoTo;
import com.konai.appmeter.driver.struct.TIMSQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LocService extends Service implements LocationListener {

    //DB (SQLITE)
    SQLiteHelper helper;
    SQLiteControl sqlite;
    String[] todayData, totalData;
    String drvCode;
    int payDiv, drvPay, addPay;

    String TAG = "LocService class";
    NotificationManager notificationManager;
    private LocationManager locationManager;
    private Location mLastLocation = null;
    private Location mCalLocation = null;
    private Location mNowLocation = new Location("FIX");
    private Location mNowCalLocation = new Location("NOW");
    private Location mLastCalLocation = new Location("LAST");
    private Location mCompLocation = new Location("TMP");
    private double mLastCallong = 0.0;
    private double mLastCallat = 0.0;
    private double mBeforeCallong = 0.0;
    private double mBegoreCallat = 0.0;
    private long mtasktimer = 0;

    /////20210310
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private String mDrvnum = "";
    ///////////////////////////
//overlaywindow
    private int m_lbsLastState = 0; // 0: 아무것도 아닌상태, 1:빈차, 2:주행 3:예약

    WindowManager m_WindowManager;
    DisplayMetrics m_matrix = new DisplayMetrics();
    View m_Lbsview = null;
    LinearLayout m_lbslayoutimg = null;
    LinearLayout m_lbslayoutBottom = null;
    TextView transferToFinalPay;

    private double touch_interval_X = 0; // X 터치 간격
    private double touch_interval_Y = 0; // Y 터치 간격
    private int zoom_in_count = 0;       // 줌 인 카운트
    private int zoom_out_count = 0;      // 줌 아웃 카운트
    private int touch_zoom = 0;          // 줌 크기

    LinearLayout.LayoutParams mContainerParams;
    LinearLayout mContainer;
    ImageView increaseIv, decreaseIv;   //크기 증가/ 줄이기 버튼
    ImageView hideIv;               //숨기기 버튼
    LinearLayout menu_btn_layout;
    Boolean isClicked = true;


    //lbsLayout
    Button m_lbsBtnEmpty = null;   //booth 빈차
    Button m_lbsBtnDrive = null;   //booth 주행
    Button m_lbsBtnReserv = null;  //booth 예약

    TextView m_lbsTvCarState = null;
    TextView m_lbsTvPayment = null;
    TextView m_lbsremaindist = null;
    ImageView m_lbsTvBleConn = null; //블루투스 //20210831
    ImageView m_lbsIvHome;
    LinearLayout m_lbsTvPayment_layout;

    private List<TIMS_UnitVO> unitParams;

    public String logTag = "logTag";

    ImageView m_Lbsbtn;
    private WindowManager.LayoutParams m_Params;        //lbsmsg params 객체. 뷰의 위치 및 크기
    private float START_X, START_Y;                        //움직이기 위해 터치한 시작 점
    private int PREV_X, PREV_Y;                            //움직이기 이전에 뷰가 위치한 점
    private int MAX_X = -1, MAX_Y = -1;                    //뷰의 위치 최대 값
    private boolean b_lbsMove = false;

    public int lbs_w, lbs_h;

    public int lbs_initx = -1;
    public int lbs_inity = -1;
    public int lbs_initw = 300;
    public int lbs_inith = 138;
/////////////////////////

    //20200711
    private static AMBluetoothLEManager mBluetoothLE = null;
    BlockingQueue<AMdtgform> mDTGblockQ = new ArrayBlockingQueue<AMdtgform>(60);
    BlockingQueue<CalQueue> mCalblockQ = new ArrayBlockingQueue<CalQueue>(10);
    BlockingQueue<String> mSendDTGQ = new ArrayBlockingQueue<String>(5);
    //tims
    BlockingQueue<TIMSQueue> mTIMSsendQ = new ArrayBlockingQueue<TIMSQueue>(20);

    private final IBinder m_ServiceBinder = new ServiceBinder();

    private TimerTask CalFareTimer; //20210701

    private static Thread mainThread = null;
    private static Thread checkstateThread = null;
    private static Thread netThread = null;
    //tims
    private static Thread TIMSThread = null;

    private String lbs_nPayment = "0";

    private String TIMS_BASEURL = "https://tims-help.kotsa.or.kr";
    private String TIMS_ADDURL = "";
    private String TIMS_BIZ = ":45000/app-meter/biz";
    private String TIMS_BTN = ":45000/app-meter/btn";
    private String TIMS_POWER = ":45000/app-meter/power";
    private String TIMS_VEHICLE = ":55000/app-meter/auth/vehicle?CAR_REG_NO=";
    private String TIMS_DRIVER = ":55000/app-meter/auth/driver?QUALF_NO=";

    //Send DTG var
    private String DTG_BASEURL = "http://49.50.165.75/AppMeterApi/";
    private int interval_DTGLimit = 0;
    private boolean limitCount = false;
    private String DTG_PATH = "";
    private String DTG_PARAMS = "";
    private double dtgReportDist = 0;
    private String mLastDtti = "";
    private String mNowDtti;
    private boolean setURL = false;

    ///////////////////
    double distanceLimit = 0;

    //이동총거리
    double tDistance = 0;

    /////////////20201110
    boolean bInitDTGdata = false;
    public static int mnUseGPS = 0; //20220120 add public //20201110 for 승차시작gps skip ,3초
//mnUseGPS < 2 skip, 99 dtg값이용할때 gps로전환 2회 skip, 100 gps정확도 1회 skip.

    //20210325
    public boolean mbSuburb = false;

    int mngpserror = 0; //20210507
    boolean mbgpserror = false;

    int mndtgused = 0;
/////////////

////////////////////

    public static class CEmpty_val {
        static long mEmptystart = 0;
        static int mSpeed = 0;
        static int mEmptytimeT = 0;
        static double mEmptydistanceT = 0;

        public static void init() {
            mEmptystart = System.currentTimeMillis();
            mSpeed = 0;
            mEmptytimeT = 0;
            mEmptydistanceT = 0;

            CDrive_val.init(); //20210909

        }
    }

    public static class CDrive_val {

        static int mSpeed = 0;
        static long mDrivestart = 0;
        static int mDrivetimeT = 0;
        static double mDrivedistanceT = 0;
        static double mFaredistanceT = 0; //요금거리계산
        static int mFareT = 0; //요금
        static double mFaresubdistT = 0; //요금거리계산
        static int m30cnt = -1;
        static int mFareDiscount = 0; //할인.
        static int mFareAdd = 0; //20210823
        static int mCallPay = 0; //20210909
        static String mStarttime = ""; //AMBlestruct.getCurDateString(); //20201110
        static String mEndtime = "";
        static boolean mbStartExtra = false; //할증포함기본요금적용
        static boolean mbExtraTime = false; //심야할증시간
        static boolean mbExtraComplex = false; //복합.
        static boolean mbExtraSuburb = false; //시외.
        static int mDTGFirstDist = 0; //20210325 dtg거리. for  주행중강제종료, 주행으로앱미터시작
        static int TIMSIDX = 0;
        static double mLastDTGdist = 0; //20210701 dtg사용이 gps로바뀔때 마지막 dtg이동거리를 이용.

        //20201203
        public static void init() {
            mSpeed = 0;
            mDrivestart = System.currentTimeMillis();
            mDrivetimeT = 0;
            mDrivedistanceT = 0;
            mFaredistanceT = 0;

            mbExtraTime = false;
            mbExtraComplex = false;
            mbExtraSuburb = false;

            TIMSIDX = 0; //20210512

            mLastDTGdist = 0; //20210701

            if (Integer.parseInt(Info.getCurHourString()) < 4) {

                mFareT = CalFareBase.BASECOSTEXTRATIME;
                mbStartExtra = true;
            } else {

                mFareT = CalFareBase.BASECOST;
                mbStartExtra = false;
            }

//////////////////////
            if (Info.REPORTREADY) //20210416 for report
            {

                if (CalFareBase.CALTYPE == 1) //시간만검정.
                {


                    CalFareBase.INTERVAL_DIST = 100;
                    CalFareBase.BASEDIST_PER1S = 100 / 30.0;
                    CalFareBase.BASEDRVDIST = 100; //3000;
                    CalFareBase.BASECOST = 0; //6500;
                    CalFareBase.DISTCOST = 200;
                    CalFareBase.TIMECOST_LIMITSECOND = 100; //360km
                    mFareT = CalFareBase.BASECOST;
                } else if (CalFareBase.CALTYPE == 2) //시간을요금검정. ?
                {


                    CalFareBase.INTERVAL_DIST = 100;
                    CalFareBase.BASEDIST_PER1S = 100 / 15.0;
                    CalFareBase.BASEDRVDIST = 100;
                    CalFareBase.BASECOST = 6500;
                    CalFareBase.DISTCOST = 100;
                    mFareT = CalFareBase.BASECOST;
                    CalFareBase.TIMECOST_LIMITSECOND = 100; //360km
                    CalFareBase.INTERVAL_DIST = 100;
                } else if (CalFareBase.CALTYPE == 3) //거리만검정.
                {


                    CalFareBase.INTERVAL_DIST = 100;
                    CalFareBase.BASEDIST_PER1S = 100 / 30.0;
                    CalFareBase.BASEDRVDIST = 100; //3000;
                    CalFareBase.BASECOST = 0; //6500;
                    CalFareBase.DISTCOST = 200;
                    mFareT = CalFareBase.BASECOST;

                } else if (CalFareBase.CALTYPE == 3) //거리만검정.
                {


                    CalFareBase.INTERVAL_DIST = 132;
                    CalFareBase.BASEDIST_PER1S = 132 / 31.0;
                    CalFareBase.BASEDRVDIST = 2000;
                    CalFareBase.BASECOST = 3800;
                    CalFareBase.DISTCOST = 100;
                    mFareT = CalFareBase.BASECOST;

                }
            }
//////////////////////

            mFaresubdistT = 0;
            m30cnt = -1;
//20210823            mFareDiscount = 0;
            mFareAdd = 0; //20210823
            mStarttime = AMBlestruct.getCurDateString(); //20201110
            mEndtime = AMBlestruct.getCurDateString(); //20201110

            mCallPay = 0; //20210909

        }

        public static void setPreInfo(String key, long ltime, int nfare, int ndist, int nremain, int nfaredist, int ndtgdist) {
            mFareT = nfare;
            mDrivestart = ltime;
            mDrivedistanceT = ndist;
            mFaredistanceT = nfaredist;
            mDTGFirstDist = ndtgdist;
            if (nfaredist > CalFareBase.BASEDRVDIST) {

                mFaresubdistT = CalFareBase.INTERVAL_DIST - nremain;

            } else
                mFaresubdistT = CalFareBase.BASEDRVDIST - nremain;

        }

        //20210823
        public static void setmFareAdd(int nfareadd) {

            mFareAdd = nfareadd;

        }

        public static void setmFareDiscount(int nfaredis) {

            mFareDiscount = nfaredis;

        }

        public static void setmFareCallPay(int ncallpay) {

            mCallPay = ncallpay;

        }
//////////////

    }

    //////////
//20220120
    public class CalQueue {
        public long icurtime;
        public long ilasttime;
        public double distance;
        public double speed;
        public double altitude;
        public long itimet; //경과시간.
        public int nType; //1 gps 2 dtg
        public double nowlong; //20210701 x position
        public double nowlat; //20210710 y position
        public double lastlong; //20210701 x 이전position
        public double lastlat; //20210701 y 이전position
        public boolean bused; //20210701 carculate_fare() 반영됐는지 아닌지

        //20220120
        public void init() {
            distance = 0;
            speed = 0;
            nowlong = 0;
            nowlat = 0;
            lastlong = 0;
            lastlat = 0;
        }
    }

    CalQueue mCalque = new CalQueue(); //20210701
    CalQueue mLastCalque = new CalQueue(); //20210701
    CalQueue mLastDTGque = new CalQueue(); //20210701

/////////////////////////

    public AMdtgform mLastDTGform = new AMdtgform();
    AMdtgform mFirstDTGform = new AMdtgform();

    public static int mnReadyToGPS = 0; //20201211
    int mCalwhich = 2; //20201112 after gps 5count, 5seconnd 1; // 1 gps 2 dtg
    long mLasttime = 0;
    long mLastGPStime = 0;
    long mLastDTGtime = 0;
    long mLastCaltime = 0;
//    Location mLastLocation = null;

    private boolean mbNeedDTGFirst = false; //20210325
    public boolean mbDrivestart = false;
    public boolean mbPayView = false; //20220120 지불화면.
    public int mDrivemode = 0;
    public int mCardmode = AMBlestruct.MeterState.NONE;
    static String mDriveCode = "";

    static double mGPSspeed = 0;
    static double mDTGspeed = 0;
    static double mLastspeed = 0;

//////////////////////////

    public class ServiceBinder extends Binder {

        public LocService getService() {
            return LocService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_ServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mBluetoothLE = new AMBluetoothLEManager(this, mDTGblockQ, LocService.this);
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

/*
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_ble)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_ble))
                .setContentTitle(getString(R.string.meter_noti_title))
                .setContentText(getString(R.string.meter_noti_text))
                .setOngoing(true)
                .setChannelId("foreground")
                .setDefaults(Notification.DEFAULT_VIBRATE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationBuilder != null && notificationManager != null){
            startForeground(8099, notificationBuilder.build());
        }

*/

/*
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "appmeter_01")
                .setSmallIcon(R.drawable.ic_ble)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_ble))
                .setContentTitle(getString(R.string.meter_noti_title))
                .setContentText(getString(R.string.meter_noti_text))
                .setOngoing(true)
                .setDefaults(4);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        }
a
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("appmeter_01",
                    getString(R.string.meter_noti_title),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId("appmeter_01");
        }

        final Notification notification = notificationBuilder.build();

        startForeground(8099, notificationBuilder.build());

*/

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            startForegroundService();

        }

//////////////////////////

//202201020
        mCalque.init();
        mLastCalque.init();
        mLastDTGque.init();
////////////

        mLastDTGform.init();
        mFirstDTGform.init();

        mainThread = new Thread(new MainThread());// Event Thread
        mainThread.start();

        checkstateThread = new Thread(new CheckStateThread());// Event Thread
        checkstateThread.start();

        netThread = new Thread(new DTG_NetworkThread()); // DTG Event Thread
        netThread.start();

        TimerStart(); //20210701
//tims
        if (Info.TIMSUSE) {

            TIMSThread = new Thread(new n_TIMS_Thread());
            TIMSThread.start();
        }

//20210310 start

        if (setting.gUseBLE == true)
            setBleScan();

/////////end

        return super.onStartCommand(intent, flag, startId);
    }

    void startForegroundService() {

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "appmeter";
            String TITLE = "AM100";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "appmeter AM100",
                    NotificationManager.IMPORTANCE_LOW);

            channel.setSound(null, null);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);

//            Intent notificationIntent = new Intent(this, MainActivity.class);


            Intent notificationIntent = Info.g_MainIntent;

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            builder.setContentIntent(pendingIntent);

            startForeground(1, builder.build());

/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(CHANNEL_ID, TITLE, NotificationManager.IMPORTANCE_LOW);
                    notificationManager.createNotificationChannel(channel);
                }

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
                startForeground(1, notification);

            }
*/

        } else {
            builder = new NotificationCompat.Builder(this);

//            Intent notificationIntent = new Intent(this, MainActivity.class);

            Intent notificationIntent = Info.g_MainIntent;

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            builder.setContentIntent(pendingIntent);
            builder.setContentTitle("appmeter");
            builder.setContentText("appmeter AM100");

            startForeground(1, builder.build());
        }

    }

    void show_overlay1() {

    }

    //////////////////
//20210701
    public void TimerStart() {

        CalFareTimer = new TimerTask() {
            long curtimer;

            @Override
            public void run() {

                curtimer = System.currentTimeMillis();
                ;


                if (curtimer / 1000 > mtasktimer) {

                    if (_Receive_calQ())
                        mtasktimer = curtimer / 1000;
                }

            }

        };
        Timer timer = new Timer();
        timer.schedule(CalFareTimer, 0, 100); //1000);

    }

    private boolean _Receive_calQ() {

        if (mbDrivestart == false) {

//20220120
            mLastCalque.init();

            if (mCalLocation != null) {
                mLastCalLocation.setLatitude(mCalLocation.getLatitude());
                mLastCalLocation.setLongitude(mCalLocation.getLongitude());

                mCalque.nowlong = mCalLocation.getLongitude();
                mCalque.nowlat = mCalLocation.getLatitude();

                mNowCalLocation.setLongitude(mCalque.nowlong);
                mNowCalLocation.setLatitude(mCalque.nowlat);
            }

            mCalque.bused = true;
///////////////

            return false;

        }

        mCalque.icurtime = System.currentTimeMillis(); //CDrive_val.mDrivestart + (CDrive_val.TIMSIDX * 1000);
        mCalque.itimet = 1000;

        if (CDrive_val.TIMSIDX == 0) {
//202201020
            mCalque.init();
            mLastCalque.init();
            mLastDTGque.init();
////////////

            mCalque.nType = 2;
            mLastDTGque.bused = true;
            _checkExtraTime();

            if (mCalLocation != null) {
                mLastCalLocation.setLatitude(mCalLocation.getLatitude());
                mLastCalLocation.setLongitude(mCalLocation.getLongitude());

                mCalque.nowlong = mCalLocation.getLongitude();
                mCalque.nowlat = mCalLocation.getLatitude();

                mNowCalLocation.setLongitude(mCalque.nowlong);
                mNowCalLocation.setLatitude(mCalque.nowlat);
            }

//////////////////////

            Date date = new Date(mCalque.icurtime);
            SimpleDateFormat dttiFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String dtti = dttiFormat.format(date);

            if (Info.TIMSUSE)
                mCallback.serviceTIMSDataEvent(CDrive_val.TIMSIDX, dtti, mCalLocation, 0, "", 0, (int) CalFareBase.BASEDRVDIST,
                        CDrive_val.mFareT, 1, 0, 0, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb);
        } else {

            if (mCalque.bused == true) {

                return false;

            } else if (mCalque.nType == 1) {
                if (mLastCalLocation.getLongitude() == 0) //20211220
                {

                    mLastCalLocation.setLongitude(mCalque.nowlong);
                    mLastCalLocation.setLatitude(mCalque.nowlat);
                }

                if (mCalque.nowlong < 1) //0
                {

//20211220                        mNowCalLocation.setLongitude(mLastLocation.getLongitude());
//                        mNowCalLocation.setLatitude(mLastLocation.getLatitude());
                    mNowCalLocation.setLongitude(mLastCalLocation.getLongitude()); //20211220
                    mNowCalLocation.setLatitude(mLastCalLocation.getLatitude()); //2021120
                } else {
                    mNowCalLocation.setLongitude(mCalque.nowlong);
                    mNowCalLocation.setLatitude(mCalque.nowlat);

                }

                if (mLastCalque.nType == 2) {

                    mCalque.distance = mLastDTGque.distance;
                    mCalque.speed = mLastDTGque.distance;
                } else {

                    //                       Info._displayLOG(true, "속도 (" + mCalque.distance + " " + mCalque.speed + ") " + (mCalque.distance - mCalque.speed), "");

//보정.
//20211220                        if(mCalque.distance - mCalque.speed > 3 && mGPSspeed > 1)
                    if (mCalque.distance - mCalque.speed > 3 && mGPSspeed > 1 && mLastCalque.lastlong > 0) {
//                                   mCalque.distance = mLastCalLocation.distanceTo(mNowCalLocation);
///                                   mCalque.speed = mCalque.distance;

                        {

                            double[] xy = new double[2];

                            xy = GetGeoTo.P1P2toP3CompWgs84(mLastCalque.lastlong, mLastCalque.lastlat,
                                    mLastCalque.nowlong, mLastCalque.nowlat, mNowCalLocation.getLongitude(), mNowCalLocation.getLatitude(), mCalque.speed);

                            if (false) //Info.TIMSUSE)
                            {

//                                    Info._displayLOG(true, String.format("GPS보정 전(%.6f, %.6f) --> 후(%.6f, %.6f) %.1f",
///                                            mNowCalLocation.getLatitude(), mNowCalLocation.getLongitude(), xy[1], xy[0], mCalque.distance - mCalque.speed), "");

                                Info.Savedata(Info.g_nowKeyCode + ".txt", String.format("GPS보정 전(%.6f, %.6f) --> 후(%.6f, %.6f) %.1f",
                                        mNowCalLocation.getLatitude(), mNowCalLocation.getLongitude(), xy[1], xy[0], mCalque.distance - mCalque.speed), "appmeter");

                            }
                            mCalque.nowlong = xy[0];
                            mCalque.nowlat = xy[1];

                            mCompLocation.setLongitude(mLastCalque.nowlong);
                            mCompLocation.setLatitude(mLastCalque.nowlat);

                            mNowCalLocation.setLongitude(mCalque.nowlong);
                            mNowCalLocation.setLatitude(mCalque.nowlat);

//                                Info._displayLOG(true, "bearing (" + mCalque.distance + " " + mCalque.speed + ") "
///                                        + mCompLocation.distanceTo(mNowCalLocation), "");

///                                        + mCompLocation.distanceTo(mNowCalLocation), "");

//                                mCalque.distance = mCalque.speed;
//                                if(mCalque.distance > 1)
//                                    mCalque.distance = mCalque.distance - 0.005;

                            mCalque.distance = GetDistWGS84(mLastCalLocation, mNowCalLocation);
                            mCalque.speed = mCalque.distance;
                        }

                        Info._displayLOG(true, "잔여거리6 ", "");
                    } else {

                        mCalque.distance = GetDistWGS84(mLastCalLocation, mNowCalLocation);
//                            if(mCalque.distance > 1)
///                                mCalque.distance = mCalque.distance - 0.005;

                        Info._displayLOG(true, "잔여거리5 ", "");
                        mCalque.speed = mCalque.distance;
                    }

//20211229
                    if (mCalque.distance > Info.CALDISTANCEMAX) {
                        mCalque.distance = 0;
                        mCalque.speed = 0;
                        mCalque.nType = 2;
                    }

                }

            }

            Info._displayLOG(true, "잔여거리3 d" + mCalque.distance + " L " + mLastCalLocation.getLongitude() + " " + mLastCalLocation.getLatitude() +
                    " N " + mNowCalLocation.getLongitude() + " " + mNowCalLocation.getLatitude(), "");

            if (mbPayView == false) //20220120
                carculate_fare(mCalque);

            mCalque.bused = true;

        }

        mLastCalque.nType = mCalque.nType;
        mLastCalque.distance = mCalque.distance;
        mLastCalque.speed = mCalque.distance;
//20211220        mLastCalque.nowlong = mNowCalLocation.getLongitude();
//        mLastCalque.nowlat = mNowCalLocation.getLatitude();
//        mLastCalque.lastlong = mLastCalLocation.getLongitude();
//        mLastCalque.lastlat = mLastCalLocation.getLatitude();

//20211220
        if (mCalque.nType == 1) {
            mLastCalque.nowlong = mNowCalLocation.getLongitude();
            mLastCalque.nowlat = mNowCalLocation.getLatitude();
            mLastCalque.lastlong = mLastCalLocation.getLongitude();
            mLastCalque.lastlat = mLastCalLocation.getLatitude();

        }
////////////////

        mLastCalLocation.setLatitude(mNowCalLocation.getLatitude());
        mLastCalLocation.setLongitude(mNowCalLocation.getLongitude());

        mCalque.distance = 0;
        mCalque.speed = 0;

        if (mbPayView == false) //20220120
            CDrive_val.TIMSIDX++;

        return true;
    }

    ///////////////////////////////////////////////////
    public void _overlaycarstate() {
        if (!Info.bOverlaymode) {
            return;
        }

        //Info.bOverlayshow = true;

        m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        m_WindowManager.getDefaultDisplay().getMetrics(m_matrix);

        if (m_Lbsview != null) {

            m_WindowManager.removeView(m_Lbsview);
            m_Lbsview = null;
        }

        m_lbsLastState = 1;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflater 를 사용해서 실제 view 객체로 만듭니다.
        m_Lbsview = (View) inflater.inflate(R.layout.lbsmsg, null);

        final LinearLayout lbslayouttop = (LinearLayout) m_Lbsview.findViewById(R.id.lbslayouttop);
        m_lbslayoutimg = (LinearLayout) m_Lbsview.findViewById(R.id.lbslayout);
        m_lbslayoutimg.setVisibility(View.GONE); //20210831

        m_lbslayoutBottom = (LinearLayout) m_Lbsview.findViewById(R.id.lbslayout_bottom);
        transferToFinalPay = (TextView) m_Lbsview.findViewById(R.id.transfer_final_payment);  //지불버튼

        mContainer = (LinearLayout) m_Lbsview.findViewById(R.id.mContainer);
        increaseIv = (ImageView) m_Lbsview.findViewById(R.id.ic_increase);
        decreaseIv = (ImageView) m_Lbsview.findViewById(R.id.iv_decrease);
        hideIv = (ImageView) m_Lbsview.findViewById(R.id.iv_hide);
        menu_btn_layout = (LinearLayout) m_Lbsview.findViewById(R.id.menu_btn_layout);

//////////////////
        m_lbsBtnEmpty = (Button) m_Lbsview.findViewById(R.id.lbsbtn_empty);  //빈차버튼
        m_lbsBtnDrive = (Button) m_Lbsview.findViewById(R.id.lbsbtn_drive);  //주행버튼
        m_lbsBtnReserv = (Button) m_Lbsview.findViewById(R.id.lbsbtn_reserv); //예약버튼

        m_lbsTvCarState = (TextView) m_Lbsview.findViewById(R.id.lbs_carstate);
        m_lbsTvPayment = (TextView) m_Lbsview.findViewById(R.id.lbs_payment); //호출버튼
        //todo: 20210831   -> m_lbsTvPayment_layout 변수 지웠습니다.
        //  m_lbsTvPayment_layout = (LinearLayout) m_Lbsview.findViewById(R.id.lbslayout);
        m_lbsremaindist = (TextView) m_Lbsview.findViewById(R.id.lbs_remaindist);
        m_lbsTvBleConn = (ImageView) m_Lbsview.findViewById(R.id.lbs_blestate);//블루투스   //20210831
        m_lbsIvHome = (ImageView) m_Lbsview.findViewById(R.id.lbs_home);    //홈버튼    //20210831
        //todo: 20210831 end

        if (Build.VERSION.SDK_INT > 26) {
            m_lbsTvPayment.setTextSize(3.0f * setting.gOrient);
        }

        //todo: 20210831
        //todo: 숨김버튼
        hideIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isClicked == true) {
                    if (mbDrivestart) //20210831
                        return;
                    m_lbslayoutimg.setVisibility(View.GONE);
                    m_lbslayoutBottom.setVisibility(View.GONE);
//                    hideIv.setText("열기");
                    hideIv.setBackgroundResource(R.drawable.ic_down);
                    menu_btn_layout.setBackgroundResource(R.drawable.edit_backgroud_radius);
                    isClicked = false;
                } else {
                    if (mbDrivestart) //20210831
                        return;
                    m_lbslayoutimg.setVisibility(View.GONE);
                    m_lbslayoutBottom.setVisibility(View.VISIBLE);
//                    hideIv.setText("숨김");
                    hideIv.setBackgroundResource(R.drawable.ic_up);
                    menu_btn_layout.setBackgroundResource(R.drawable.edit_backgroud_radius_bottomright);
                    isClicked = true;


                }
                m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
            }
        });
        //todo: 20210831 end

        //todo: 20210831
        //todo: (+) 버튼
        increaseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Display display = m_WindowManager.getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);

                m_lbsBtnEmpty.setTextSize(m_lbsBtnEmpty.getTextSize() + (m_lbsBtnEmpty.getTextSize() * 0.05f));
                m_lbsBtnDrive.setTextSize(m_lbsBtnDrive.getTextSize() + (m_lbsBtnDrive.getTextSize() * 0.05f));
                m_lbsBtnReserv.setTextSize(m_lbsBtnReserv.getTextSize() + (m_lbsBtnReserv.getTextSize() * 0.05f));
                transferToFinalPay.setTextSize(transferToFinalPay.getTextSize() + (transferToFinalPay.getTextSize() * 0.05f));
//                hideIv.setTextSize(m_lbsBtnReserv.getTextSize() + (m_lbsBtnReserv.getTextSize() * 0.01f));
                m_lbsTvPayment.setTextSize(m_lbsTvPayment.getTextSize() + (m_lbsTvPayment.getTextSize() * 0.03f));

//                if (setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                    if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
//                    {
//
//                    } else {
//                        // Log.d("check_version_2","check_version_1");
//                        m_lbsTvPayment.setTextSize(7.0f * setting.gTextDenst);
//                        m_lbsBtnEmpty.setTextSize(3.0f * setting.gTextDenst);
//                        m_lbsBtnDrive.setTextSize(3.0f * setting.gTextDenst);
//                        m_lbsBtnReserv.setTextSize(3.0f * setting.gTextDenst);
//                        // m_lbsTvBleConn.setTextSize(2.0f * setting.gTextDenst); //블루투스  //20210831
//                        hideIv.setTextSize(1.0f * setting.gTextDenst);
//                    }
//                }

                int new_width = m_Params.width;
                int new_height = m_Params.height;

                new_width = m_Params.width + (int) Math.floor(new_width * 0.05);
                new_height = m_Params.height + (int) Math.floor(new_height * 0.05);

                //m_lbsTvPayment.setTextSize(4.0f * setting.gTextDenst);
                // m_lbsTvPayment.setTextSize((text_size)+());

                int display_width = size.x;
                int display_height = size.y;

                if (new_width < display_width && new_height < display_height) {
                    m_Params.width = new_width;
                    m_Params.height = new_height;

                    m_Lbsview.setVisibility(View.VISIBLE);
                    m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
                }

                Log.d(logTag + "_plus", m_Params.width + ", " + m_Params.height);
            }
        });
        //todo: 20210831 end


        //todo: 20210831
        //todo: (-) 버튼
        decreaseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Display display = m_WindowManager.getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);

                int display_width = size.x;
                int display_height = size.y;

                int new_width = m_Params.width;
                int new_height = m_Params.height;

                if (display_height < 1100) {

                    if (new_width > 200 && new_height > 100) {
                        m_lbsBtnEmpty.setTextSize(m_lbsBtnEmpty.getTextSize() - (m_lbsBtnEmpty.getTextSize() * 0.05f));
                        m_lbsBtnDrive.setTextSize(m_lbsBtnDrive.getTextSize() - (m_lbsBtnDrive.getTextSize() * 0.05f));
                        m_lbsBtnReserv.setTextSize(m_lbsBtnReserv.getTextSize() - (m_lbsBtnReserv.getTextSize() * 0.05f));
//                        hideIv.setTextSize(m_lbsBtnReserv.getTextSize() - (m_lbsBtnReserv.getTextSize() * 0.01f));
                        transferToFinalPay.setTextSize(transferToFinalPay.getTextSize() - (transferToFinalPay.getTextSize() * 0.05f));
                        m_lbsTvPayment.setTextSize(m_lbsTvPayment.getTextSize() - (m_lbsTvPayment.getTextSize() * 0.03f));


                        new_width = m_Params.width - (int) Math.floor(new_width * 0.05);
                        new_height = m_Params.height - (int) Math.floor(new_height * 0.05);
                        Log.d("new_width_1", new_width + " " + new_height);
                        m_Params.width = new_width;
                        m_Params.height = new_height;

                        m_Lbsview.setVisibility(View.VISIBLE);
                        m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
                    }
                } else {
                    if (new_width > 600 && new_height > 400) {
                        m_lbsBtnEmpty.setTextSize(m_lbsBtnEmpty.getTextSize() - (m_lbsBtnEmpty.getTextSize() * 0.05f));
                        m_lbsBtnDrive.setTextSize(m_lbsBtnDrive.getTextSize() - (m_lbsBtnDrive.getTextSize() * 0.05f));
                        m_lbsBtnReserv.setTextSize(m_lbsBtnReserv.getTextSize() - (m_lbsBtnReserv.getTextSize() * 0.05f));
//                        hideIv.setTextSize(m_lbsBtnReserv.getTextSize() - (m_lbsBtnReserv.getTextSize() * 0.01f));
                        transferToFinalPay.setTextSize(transferToFinalPay.getTextSize() - (transferToFinalPay.getTextSize() * 0.05f));
                        m_lbsTvPayment.setTextSize(m_lbsTvPayment.getTextSize() - (m_lbsTvPayment.getTextSize() * 0.01f));

                        new_width = m_Params.width - (int) Math.floor(new_width * 0.05);
                        new_height = m_Params.height - (int) Math.floor(new_height * 0.05);

                        Log.d("new_width_2", new_width + " " + new_height);

                        m_Params.width = new_width;
                        m_Params.height = new_height;

                        m_Lbsview.setVisibility(View.VISIBLE);
                        m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
                    }
                }

                Log.d(logTag + "_minus", m_Params.width + ", " + m_Params.height);
            }
        });

        //todo: 20210831
        //todo: 빈차/주행/예약 버튼 클씨크기 조절
//        if (setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
//            {
//
//            } else {
//                // Log.d("check_version_2","check_version_1");
//                m_lbsTvPayment.setTextSize(7.0f * setting.gTextDenst);
//                m_lbsBtnEmpty.setTextSize(3.0f * setting.gTextDenst);
//                m_lbsBtnDrive.setTextSize(3.0f * setting.gTextDenst);
//                m_lbsBtnReserv.setTextSize(3.0f * setting.gTextDenst);
//                // m_lbsTvBleConn.setTextSize(2.0f * setting.gTextDenst); //블루투스  //20210831
//                hideIv.setTextSize(1.0f * setting.gTextDenst);
//            }
//        }
        //todo: 20210831 end


 /*       params = layoutmain.getLayoutParams();
//		params.width = info.calBtnSize(890);
        params.height = info.getbottomtablesize() / 10 * 11;
        layoutmain.setLayoutParams(params);
///////////////////////top title layout
        layoutmain = (LinearLayout) findViewById(R.id.linearLayout2);
        params = layoutmain.getLayoutParams();
        params.height = info.gettoplayoutsize();
        layoutmain.setLayoutParams(params);
*/


		/*m_Params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_Params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        } else {
            m_Params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        }

        m_Params.gravity = Gravity.LEFT | Gravity.TOP;

        m_Lbsview.setOnTouchListener(new View.OnTouchListener() { // 터치 이벤트 리스너 등록(누를때와
            // 뗐을때를 구분)
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (MAX_X == -1) {
                        DisplayMetrics matrix = new DisplayMetrics();
                        m_WindowManager.getDefaultDisplay().getMetrics(matrix);        //화면 정보를 가져와서

                        MAX_X = matrix.widthPixels - m_Lbsview.getWidth();            //x 최대값 설정
                        MAX_Y = matrix.heightPixels - m_Lbsview.getHeight();            //y 최대값 설정
                    }

                    b_lbsMove = false;

                    START_X = event.getRawX();                    //터치 시작 점
                    START_Y = event.getRawY();                    //터치 시작 점
                    PREV_X = m_Params.x;                            //뷰의 시작 점
                    PREV_Y = m_Params.y;
                    //Btn2.setImageResource(R.drawable.alloc1_no_sel);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    //동적 브로드캐스트 - http://mydevromance.tistory.com/24
                    if (!b_lbsMove) {

//20210909                        show_mainActivity(); //20210823

                    } else //20180817
                    {
                        lbs_w = m_Params.width;
                        lbs_h = m_Params.height;

                        Log.d(logTag + "--save", lbs_w + ", " + lbs_h);

                        SharedPreferences pref = getSharedPreferences("pref",
                                Activity.MODE_PRIVATE);
                        // UI 상태를 저장합니다.
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("lbs_x", m_Params.x);
                        editor.putInt("lbs_y", m_Params.y);
                        editor.putInt("lbs_w", lbs_w);
                        editor.putInt("lbs_h", lbs_h);
                        //todo: 20211213
                        editor.commit();

                    }

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    b_lbsMove = true;

                    int x = (int) (event.getRawX() - START_X);
                    int y = (int) (event.getRawY() - START_Y);

                    final int num = 10;
                    if ((x > -num && x < num) && (y > -num && y < num)) {
                        b_lbsMove = false;

                    } else {

                        //터치해서 이동한 만큼 이동 시킨다
                        m_Params.x = PREV_X + x;
                        m_Params.y = PREV_Y + y;

                        //최대값 넘어가지 않게 설정
                        if (m_Params.x > MAX_X) m_Params.x = MAX_X;
                        if (m_Params.y > MAX_Y) m_Params.y = MAX_Y;
                        if (m_Params.x < 0) m_Params.x = 0;
                        if (m_Params.y < 0) m_Params.y = 0;

                        m_WindowManager.updateViewLayout(m_Lbsview, m_Params);    //뷰 업데이트
                    }
                }
                return false;
            }
        });

        //todo: 20210831
        //todo: 홈버튼
        m_lbsIvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                show_mainActivity(); //20210823

            }
        });
        //todo: 20210831 end


        //todo: 20220209
        m_lbsTvPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                show_mainActivity();
            }
        });
        //todo: 20220209 end..


        m_lbsBtnEmpty.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (m_lbsLastState == 1) {
                    return false;
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                        m_lbsBtnEmpty.setBackgroundResource(R.color.yellow_down);
                        m_lbsBtnEmpty.setBackgroundResource(R.drawable.touched_down_empty_btn_yellow);
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        m_lbsBtnEmpty.setBackgroundResource(R.color.yellow_up);
                        m_lbsBtnEmpty.setBackgroundResource(R.drawable.touched_up_empty_btn_grey);
                    }
                    return false;
                }
            }
        });

        //todo: 20210831
        //빈차 버튼
        m_lbsBtnEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallback.serviceLbsControllEvent(1, m_lbsLastState);

            }
        });
        //todo: 20210831 end

        m_lbsBtnDrive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (m_lbsLastState == 2) {
                    return false;
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        m_lbsBtnDrive.setBackgroundResource(R.color.yellow_down);
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        m_lbsBtnDrive.setBackgroundResource(R.color.edit_bc);
                    }
                    return false;
                }
            }
        });

        //todo: 20210831
        //주행버튼
        m_lbsBtnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.serviceLbsControllEvent(2, m_lbsLastState);
                show_mainActivity(); //20210823
                _lbsBtnSet(2);

            }
        });
        //todo: 20210831 end

        m_lbsBtnReserv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (m_lbsLastState == 3) {
                    return false;
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        m_lbsBtnReserv.setBackgroundResource(R.drawable.touched_down_reserve_btn_yellow);
                        //edit_backgroud_radius_bottomright_yellow
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        m_lbsBtnReserv.setBackgroundResource(R.color.yellow_up);
                        m_lbsBtnReserv.setBackgroundResource(R.drawable.radius_btn_bottom_right_grey);
                        //edit_backgroud_radius_bottomright_lighter
                    }
                    return false;
                }
            }
        });
        m_lbsBtnReserv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.serviceLbsControllEvent(3, m_lbsLastState);
            }
        });

        //지불버튼
        transferToFinalPay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                        transferToFinalPay.setBackgroundResource(R.color.yellow_down);
                    transferToFinalPay.setBackgroundResource(R.drawable.touched_down_pay_btn_yellow);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        transferToFinalPay.setBackgroundResource(R.color.action_up);
                    transferToFinalPay.setBackgroundResource(R.drawable.touched_up_pay_btn_yellow);
                }
                return false;
            }
        });

        //todo: 20210831
        //지불버튼
        transferToFinalPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallback.serviceLbsControllEvent(4, m_lbsLastState);
                show_mainActivity(); //20210823
            }
        });
        //todo: 20210831 end

        //todo: 20210831
        Display dp = m_WindowManager.getDefaultDisplay();
        Point size = new Point();
        dp.getRealSize(size);

        int dp_width = size.x;
        int dp_height = size.y;

//        Log.d(TAG, "display " + dp_width + " " + this.getResources().getDisplayMetrics().density);

        if (setting.gTextDenst < 23) {
            if (dp_width < 1100) {

                m_Params.width = 300; //350;
                m_Params.height = 138; //160;

                m_lbsTvPayment.setTextSize(4.0f * setting.gTextDenst);
                m_lbsBtnEmpty.setTextSize(2.0f * setting.gTextDenst);
                m_lbsBtnDrive.setTextSize(2.0f * setting.gTextDenst);
                m_lbsBtnReserv.setTextSize(2.0f * setting.gTextDenst);
//                hideIv.setTextSize(1.5f * setting.gTextDenst);

            } else {

                m_Params.width = 600;
                m_Params.height = 250;

            }
        } else {
            m_Params.width = 600;
            m_Params.height = 320;

        }

        //todo: 20210831 end

        if (setting.gOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (false) //Build.VERSION.SDK_INT >= 26) //20210823 8.0
            {

            } else {
                // Log.d("check_version_2","check_version_1");
                m_lbsTvPayment.setTextSize(7.0f * setting.gTextDenst);
                m_lbsBtnEmpty.setTextSize(3.0f * setting.gTextDenst);
                m_lbsBtnDrive.setTextSize(3.0f * setting.gTextDenst);
                m_lbsBtnReserv.setTextSize(3.0f * setting.gTextDenst);
                // m_lbsTvBleConn.setTextSize(2.0f * setting.gTextDenst); //블루투스  //20210831
                transferToFinalPay.setTextSize(3.0f * setting.gTextDenst);
//                hideIv.setTextSize(2.2f * setting.gTextDenst);
            }
        }

        m_WindowManager.addView(m_Lbsview, m_Params);

        m_Lbsview.setVisibility(View.INVISIBLE); //20180315

        if (lbs_initx == -1) {

            m_Params.x = (int) (m_matrix.widthPixels / 2 - m_Lbsview.getWidth() / 2);            //x 최대값 설정
            m_Params.y = 0;
        } else {
            m_Params.x = lbs_initx;
            m_Params.y = lbs_inity;
        }


//////////////////////////////아래버튼크기
//        ViewGroup.LayoutParams params;
//        params = m_lbslayoutimg.getLayoutParams();
//        m_Params.height = params.height;
//
//        params = lbslayouttop.getLayoutParams();
//
//        m_Params.height += params.height;

        m_WindowManager.updateViewLayout(m_Lbsview, m_Params);    //뷰 업데이트
    }

    public void _showhideLbsmsg(boolean bshow) {
        if (!Info.bOverlaymode) {
            return;
        }
        if (m_Lbsview == null)
            return;

        if (bshow == true) {
            Info.bOverlayshow = true;
            m_Lbsview.setVisibility(View.VISIBLE);
            m_WindowManager.updateViewLayout(m_Lbsview, m_Params);    //뷰 업데이트
        } else {
            Info.bOverlayshow = false;
            m_Lbsview.setVisibility(View.INVISIBLE);
            m_WindowManager.updateViewLayout(m_Lbsview, m_Params);    //뷰 업데이트
        }
    }


    //Background app icons
    public void _setLbsmsg_img(int nType) {

        if (!Info.bOverlaymode) {
            return;
        }
        if (m_Lbsview == null)
            return;
        m_lbsLastState = nType;
        _lbsBtnSet(nType);

        payHandler.sendEmptyMessage(2);

        /*if(Info.bOverlayshow) {
            m_WindowManager.updateViewLayout(m_Lbsview, m_Params);    //뷰 업데이트
        }*/
    }

    public void _setLbsBleState(int nType) {
        if (nType == 1) {

            Log.d("m_lbsTvBleConn", "m_lbsTvBleConn");

            //todo: 20210831
            //todo: blooth on
            m_lbsTvBleConn.setBackgroundResource(R.drawable.btn_bles_on);
            // m_lbsTvBleConn.setText("");

            //todo: 20210831 end
        }

        payHandler.sendEmptyMessage(2);
    }

    public void _setLbsPayment(String remaindist, String nPay) {
        if (!Info.bOverlaymode) {

            if (Info.REPORTREADY)
                Info._displayLOG(true, "앱화면 요금표기 " + nPay + "원", "");

            return;
        }
        if (m_Lbsview == null)
            return;

        lbs_nPayment = nPay;
        /*m_lbsTvPayment.setText(nPay);
        m_lbsremaindist.setText(remaindist);*/

        payHandler.sendEmptyMessage(1);

        if (Info.REPORTREADY)
            Info._displayLOG(true, "미니바 요금표기 " + nPay + "원", "");

        /*if(Info.bOverlayshow) {
            m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        try {
            locationManager.removeUpdates(this);

            notificationManager.cancelAll();

            if (m_WindowManager != null && Info.bOverlaymode) {        //서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.

                if (m_Lbsview != null) {

                    m_WindowManager.removeView(m_Lbsview);
                    m_Lbsview = null;
                }

            }

        } catch (NullPointerException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void _lbsBtnSet(int state) {

        switch (state) {
            case 1:
                _setLbsPayment("0", "0");
//20210831
                if (isClicked == false) {
                    m_lbslayoutimg.setVisibility(View.GONE);
                    m_lbslayoutBottom.setVisibility(View.GONE);

                } else {
                    m_lbslayoutimg.setVisibility(View.GONE);
                    m_lbslayoutBottom.setVisibility(View.VISIBLE);

                }

                //todo: 20211201
                m_lbsTvCarState.setText("빈차");
                m_lbsBtnEmpty.setBackgroundResource(R.drawable.radius_btn_bottom_left);
                m_lbsBtnEmpty.setTextColor(Color.parseColor("#000000"));
                m_lbsBtnDrive.setBackgroundColor(Color.parseColor("#222233"));
                m_lbsBtnDrive.setTextColor(Color.parseColor("#999999"));
                m_lbsBtnReserv.setBackgroundResource(R.drawable.radius_btn_bottom_right_grey);
                m_lbsBtnReserv.setTextColor(Color.parseColor("#999999"));
                //todo: end
                break;

            case 2:

//20210831
                isClicked = true;
//                hideIv.setText("숨김");
                hideIv.setBackgroundResource(R.drawable.ic_up);
                m_lbslayoutimg.setVisibility(View.VISIBLE);
                m_lbslayoutBottom.setVisibility(View.GONE);

                m_lbsTvCarState.setText("주행");
                m_lbsBtnEmpty.setBackgroundColor(Color.parseColor("#B33c3c4a"));
                m_lbsBtnEmpty.setTextColor(Color.parseColor("#999999"));
                m_lbsBtnDrive.setBackgroundColor(Color.parseColor("#B3ffc700"));
                m_lbsBtnDrive.setTextColor(Color.parseColor("#000000"));
                m_lbsBtnReserv.setBackgroundColor(Color.parseColor("#B33c3c4a"));
                m_lbsBtnReserv.setTextColor(Color.parseColor("#999999"));
                break;

            case 3:
//20210831
                if (isClicked == false) {
                    m_lbslayoutimg.setVisibility(View.GONE);
                    m_lbslayoutBottom.setVisibility(View.GONE);

                }
                m_lbsTvCarState.setText("예약");
//                m_lbsBtnEmpty.setBackgroundColor(Color.parseColor("#B33c3c4a"));
                m_lbsBtnEmpty.setBackgroundResource(R.drawable.touched_up_empty_btn_grey);
                m_lbsBtnEmpty.setTextColor(Color.parseColor("#999999"));
                m_lbsBtnDrive.setBackgroundColor(Color.parseColor("#222233"));
                m_lbsBtnDrive.setTextColor(Color.parseColor("#999999"));
//                m_lbsBtnReserv.setBackgroundColor(Color.parseColor("#B3ffc700"));
                m_lbsBtnReserv.setBackgroundResource(R.drawable.touched_up_reserve_btn_yellow);
                m_lbsBtnReserv.setTextColor(Color.parseColor("#000000"));
                break;

            default:

        }
    }

    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    ///////////////////
// Activity에서 정의해 해당 서비스와 통신할 함수를 추상 함수로 정의
    public interface maincallback {
        void serviceMessage(int ntype, String message);

        void serviceDisplayState(int nfare, int nremaindist, int nfarediscount, double speed, int ndistance, int nseconds, Location location,
                                 int nfaredist, int ncurdist, boolean setDB);

        void serviceEmptyState(double ndistance, int nseconds, double ddspeed, Location location);

        void serviceMeterState(int nType, int mfare);

        void serviceLog(int nseconds, Location location, int gpsAcc, int ndtgdist, int ndtgtot, String altitude, boolean drvState,
                        int speed, double dtime, double dtfare, double ddist, double dcfare,
                        double dremain, int nafterfare, int nfare, boolean bextra, boolean bsuburb, double tfaredist); //tmp for log

        void serviceFarebyMeter(int nseconds, int ndist, int nfare, int naddfare);

        void serviceLbsControllEvent(int nType, int nLastState);

        void serviceTIMSDataEvent(int idx, String date, Location location, double speed, String lnk, double dist, double remainsDist,
                                  int fare, int isOutGps, int addfare, double tdist, boolean isnight, boolean issuburb);
    }

    // Activity랑 통신할 Callback 객체..
    private maincallback mCallback = null;

    // Callback 객체 등록 함수..
    public void registerCallback(maincallback callback) {
        Log.d(TAG, "registerCallback");
        mCallback = callback;

//        mCallback.serviceFarebyMeter(100, 10320, 7000, 1000);

    }

    //////////////
    public void close() {

        Log.d(TAG, "exit service");

        if (m_WindowManager != null && Info.bOverlaymode) {        //서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.

            if (m_Lbsview != null) {

                m_WindowManager.removeView(m_Lbsview);
                m_Lbsview = null;
            }

        }

        if (mBluetoothLE != null) {

            mBluetoothLE.disconnectAM();
            mBluetoothLE.close();
        }

        mainThread.interrupt();

        checkstateThread.interrupt();

        netThread.interrupt();

        if (Info.TIMSUSE)
            TIMSThread.interrupt(); //tims

/////////////////////
        stopForeground(true);

        try {
            locationManager.removeUpdates(this);

            notificationManager.cancelAll();
        } catch (NullPointerException e) {
            Log.e("ERROR", e.toString());
        }

    }

    public void SendTIMS_Data(int div, int payType, List<TIMS_UnitVO> Params, String subParams) {

        /**
         * URL | div
         * POST:35000/app-meter/biz           | 1: 앱미터 자기인증 영업정보 이벤트 발생에 대한 전송
         * POST:35000/app-meter/btn           | 2: 앱미터 자기인증 이벤트 발생에 대한 전송
         * POST:35000/app-meter/power         | 3: 앱미터 자기인증 운행 시작/종료 이벤트 발생에 대한 전송
         * 1~3 테스트전용
         * POST:45000/app-meter/biz           | 4: 앱미터 상시 영업정보 이벤트 발생에 대한 전송
         * POST:45000/app-meter/btn           | 5: 앱미터 상시 이벤트 발생에 대한 전송
         * POST:45000/app-meter/power         | 6: 앱미터 상시 운행 시작/종료 이벤트 발생에 대한 전송
         * GET :55000/app-meter/auth/vehicle  | 7: 자동차 정보 확인 API
         * GET :55000/app-meter/auth/driver   | 8: 운전자 정보 확인 API
         */

        if (Info.TIMSUSE == false) {

            return;

        }

//for test 20210504.
//        if(div > 2)
//            return;

        double latitude = 0;
        double longitude = 0;


//20210701        if(mLastLocation != null)
        if (mCalLocation != null) {

//            latitude = mLastLocation.getLatitude();
//            longitude = mLastLocation.getLongitude();
            latitude = mCalLocation.getLatitude();
            longitude = mCalLocation.getLongitude();
        }

        Date date = new Date();
        SimpleDateFormat dttiFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String dtti = dttiFormat.format(date);

        String sTIME = "";
        String eTIME = "";
        String sXPOS = "";
        String sYPOS = "";
        String eXPOS = "";
        String eYPOS = "";

        String mDIST = "0";
        String ePAY = "0";

        int mSendTYPE = 0;
        int mSubtype = 0;

        try {
            JSONObject infoObj = new JSONObject();
            JSONObject iObj = new JSONObject();
            switch (div) {

                case 1:

                    Info._displayLOG(true, "TIMS 영업DATA전송idx " + Info.gTimsDayIdxtmp, "");
                    TIMS_ADDURL = TIMS_BIZ;
                    mSendTYPE = 1;
                    mSubtype = 1;

                    int nextra = 0; //20210520

                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    //infoObj.put("license", AMBlestruct.AMLicense.licensecode);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    //infoObj.put("regNo", AMBlestruct.AMLicense.taxinumber);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    //infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayIdxtmp);


                    JSONArray unitArray = new JSONArray();
                    for (int i = 0; i < Params.size(); i++) {
                        if (i == 0) {
                            sTIME = Params.get(i).getDt();
                            sXPOS = Params.get(i).getLongitude();
                            sYPOS = Params.get(i).getLatitude();

                        } else if (i == Params.size() - 1) {
                            eTIME = Params.get(i).getDt();
                            eXPOS = Params.get(i).getLongitude();
                            eYPOS = Params.get(i).getLatitude();

                            mDIST = Params.get(i).getSumdist();
                            ePAY = Params.get(i).getSumPay();

                        }

                        JSONObject uObj = new JSONObject();

                        uObj.put("idx", Params.get(i).getIdx());
                        uObj.put("dt", Params.get(i).getDt());
                        uObj.put("longitude", Params.get(i).getLongitude());
                        uObj.put("latitude", Params.get(i).getLatitude());
                        uObj.put("spd", Double.parseDouble(Params.get(i).getSpd()));
                        uObj.put("lnk", ""); //20210512 Params.get(i).getLnk());
                        uObj.put("dist", Double.parseDouble(Params.get(i).getDist()));
                        uObj.put("payType", Params.get(i).getPayType());
                        uObj.put("remainDist", Double.parseDouble(Params.get(i).getRemainDist()));
                        uObj.put("remainSec", Integer.parseInt(Params.get(i).getRemainSec()));
                        uObj.put("isAdded", Integer.parseInt(Params.get(i).getIsAdded()));
                        uObj.put("addedPay", Integer.parseInt(Params.get(i).getAddedPay()));
                        uObj.put("sumPay", Integer.parseInt(Params.get(i).getSumPay()));
                        uObj.put("isLimitSpd", Integer.parseInt(Params.get(i).getIsLimitSpd()));
                        uObj.put("isNight", Integer.parseInt(Params.get(i).getIsNight()));
                        if (Integer.parseInt(Params.get(i).getIsNight()) > 0) //20210520
                            nextra = 1;

                        uObj.put("isOutside", Integer.parseInt(Params.get(i).getIsOutside()));

                        if (Integer.parseInt(Params.get(i).getIsOutside()) > 0) //20210520
                            nextra = 1;

                        uObj.put("isOutGps", Integer.parseInt(Params.get(i).getIsOutGps()));
                        uObj.put("isOutPow", 0);
                        uObj.put("isWait", 0);

                        unitArray.put(uObj);
                    }

                    JSONObject finPayObj = new JSONObject();

                    finPayObj.put("dt", dtti);
                    finPayObj.put("pay", (int) (Math.round(Double.parseDouble(ePAY) / 100) * 100));
                    finPayObj.put("call", Integer.parseInt("0"));
                    finPayObj.put("etc", Integer.parseInt("0"));
                    finPayObj.put("ext", nextra); //20210520
                    finPayObj.put("inDt", sTIME);
                    finPayObj.put("inLongitude", sXPOS);
                    finPayObj.put("inLatitude", sYPOS);
                    finPayObj.put("outDt", eTIME);
                    finPayObj.put("outLongitude", eXPOS);
                    finPayObj.put("outLatitude", eYPOS);
                    finPayObj.put("onDist", Double.parseDouble(mDIST));
                    finPayObj.put("emptyDist", Double.parseDouble(subParams));
                    finPayObj.put("type", payType);
                    finPayObj.put("trepay", 0);

                    infoObj.put("units", unitArray);
                    infoObj.put("finalPayInfo", finPayObj);

                    if (Info.REPORTREADY)
                        Info._displayLOG(true, infoObj.toString().length() + " " + infoObj.toString(), "TIMS BIZ-");

                    Date time = new Date();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                    Info.mDriveTIMSdate = transFormat.format(time);

                    break;

                case 2:

                    mCallback.serviceMessage(2, "TIMS 버튼DATA전송 시작");

                    Info._displayLOG(true, "TIMS 버튼DATA전송idx " + Info.gTimsDayEventIdx, "");
                    TIMS_ADDURL = TIMS_BTN;
                    mSendTYPE = 1;
                    mSubtype = 2;
                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    //infoObj.put("license", AMBlestruct.AMLicense.licensecode);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    //infoObj.put("regNo", AMBlestruct.AMLicense.taxinumber);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    //infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayEventIdx);

                    String[] subDatas = subParams.split("&");

                    /**
                     * btn info
                     * 01 : 지불
                     * 05 : 빈차
                     * 10 : 영수증 또는 출력
                     * 20 : 주행
                     * 30 : 할증
                     * 31 : 자동할증
                     * 32 : 시계할증-자동
                     * 33 : 시계할증-수동
                     */

                    time = new Date();
                    transFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                    Info.mEventTIMSdate = transFormat.format(time);

                    if (subDatas[0].equals("01"))
                        Info.mEventTIMStype = "지불";
                    else if (subDatas[0].equals("05"))
                        Info.mEventTIMStype = "빈차";
                    else if (subDatas[0].equals("10"))
                        Info.mEventTIMStype = "영수증";
                    else if (subDatas[0].equals("20"))
                        Info.mEventTIMStype = "주행";
                    else if (subDatas[0].equals("30"))
                        Info.mEventTIMStype = "할증";
                    else if (subDatas[0].equals("31"))
                        Info.mEventTIMStype = "자동할증";
                    else if (subDatas[0].equals("32"))
                        Info.mEventTIMStype = "시계할증자동";
                    else if (subDatas[0].equals("33"))
                        Info.mEventTIMStype = "시계할증수동";
                    else
                        Info.mEventTIMStype = "-";

                    iObj.put("dt", dtti);
                    iObj.put("btn", subDatas[0]);
                    iObj.put("state", subDatas[1]);
                    iObj.put("longitude", String.format("%.6f", longitude));
                    iObj.put("latitude", String.format("%.6f", latitude));

                    infoObj.put("infos", iObj);

                    if (Info.REPORTREADY)
                        Info._displayLOG(true, infoObj.toString(), "TIMS BTN-");

                    break;

                case 3:
                    mCallback.serviceMessage(3, "TIMS POWER DATA전송 시작");
                    TIMS_ADDURL = TIMS_POWER;
                    mSendTYPE = 1;
                    mSubtype = 3;

                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    //infoObj.put("license", AMBlestruct.AMLicense.licensecode);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    //infoObj.put("regNo", AMBlestruct.AMLicense.taxinumber);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    //infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayPowerIdx);

                    iObj.put("dt", dtti);
                    iObj.put("state", subParams);

                    infoObj.put("infos", iObj);

                    break;

                case 4:
                    //TIMS_ADDURL = TIMS_VEHICLE +  + "&APPMETER_ID=" + + "&KEY=" + ;
                    TIMS_ADDURL = TIMS_VEHICLE + AMBlestruct.AMLicense.timstaxinum +
                            "&APPMETER_ID=" + AMBlestruct.AMLicense.timscode + "&KEY=f4bbc1d0b067002e527e535338668b29164404fd18a4e5c331c70fcb9b07fd62";
                    mSendTYPE = 4;

                    Info._displayLOG(true, "TIMS차량인증요청GET " + TIMS_BASEURL + TIMS_ADDURL, "");

                    break;
                case 5:
                    TIMS_ADDURL = TIMS_DRIVER + AMBlestruct.AMLicense.timslicense +
                            "&APPMETER_ID=" + AMBlestruct.AMLicense.timscode + "&KEY=f4bbc1d0b067002e527e535338668b29164404fd18a4e5c331c70fcb9b07fd62";
                    mSendTYPE = 5;
                    Info._displayLOG(true, "TIMS자격인증요청GET " + TIMS_BASEURL + TIMS_ADDURL, "");

                    break;

            }

            if (Info.TIMSUSE_TEST) {

                if (mSendTYPE != 1)
                    return;
            }

            TIMSQueue que = new TIMSQueue();

            que.mURLs = TIMS_BASEURL + TIMS_ADDURL;
            que.mJson = infoObj;
            que.mSendType = mSendTYPE;
            que.mSubType = mSubtype;

            add_timsQueue(que);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 버튼정보
     * <p>
     * 01. 지불
     * 05. 빈차
     * 10. 영수증, 출력
     * 20. 주행
     * 30. 할증
     * 31. 자동할증
     * 32. 시계할증-자동
     * 33. 시계할증-수동
     * 34. 복합할증
     * 35. 복합할증 취소
     * 37. 자동할증 취소
     * 38. 시계할증 취소
     * 40. 예약호출
     * 41. 예약호출 취소
     * 42. 도어1 열림
     * 43. 도어1 닫힘
     * 44. 도어2 열림
     * 45. 도어2 닫힘
     *
     * @param div
     * @param subParam - 이벤트정보
     */

    public void Send_DTGData(int div, String[] subParam) {
        if (Info.SENDDTG == true) {
            Log.d("send_dtg", Info.SENDDTG + "");
            Log.d("send_dtg", div + "");
            Log.d("send_dtg", subParam + "");

        } else {
            return;
        }

        Date date = new Date();
        SimpleDateFormat dttiFormat = new SimpleDateFormat("yyMMddHHmmss");
        String dtti = dttiFormat.format(date);
        String CARNUM = AMBlestruct.AMLicense.taxinumber;
        String DRIVERID = AMBlestruct.AMLicense.licensecode;

        String lXpos = "";
        String lYpos = "";

        if (mLastLocation == null) {
            lXpos = 0.0 + "";
            lYpos = 0.0 + "";
        } else {
            lXpos = mLastLocation.getLatitude() + "";
            lYpos = mLastLocation.getLongitude() + "";
        }

        /*Log.e("dayDrvRecord")*/

        Log.d("Send_DTGData", div + "");

        switch (div) {
            //info_dtti = "YYMMDDHHmmss"
            case 1:  //1뷴에 한번씩 위치정보
                Log.d("Send_DTGData", div + "");
                DTG_PARAMS = "info_dtti=" + dtti +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&speed=" + (int) (mGPSspeed * 3.6) +
                        "&dist=" + (int) dtgReportDist +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&state=" + "1" +  // 시동 ON/OFF
                        "&office_id=" + AMBlestruct.AMLicense.companynum; //20220103
//                        "&office_id=" + "9876543211";
                DTG_PATH = "Drive1MinAPI?" + DTG_PARAMS;
                break;
            case 2:  //앱미터 시동
                Log.d("Send_DTGData", div + "");
                String[] dayDrvRecordData = Info.sqlite.todayTotSelect().split("/");
                String driveCount = Info.sqlite.todayDriveCount();
                DTG_PARAMS = "info_dtti=" + dtti +
                        "&pairing_type=" + subParam[0] +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&dtg_model=" + subParam[1] +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&base_rate=" + CalFareBase.BASECOST +
                        "&base_dist=" + CalFareBase.BASEDRVDIST +
                        "&after_rate=" + CalFareBase.DISTCOST +
                        "&after_dist=" + CalFareBase.INTERVAL_DIST +
                        "&time_rate=" + CalFareBase.mNightTimerate +
                        "&time_fare=" + CalFareBase.TIMECOST +
                        "&time_fare_speed=" + CalFareBase.TIMECOST_LIMITHOUR +
                        "&start_extra_time=" + "00:00" +
                        "&end_extra_time=" + "04:00" +
                        "&city_extra_rate=" + CalFareBase.mSuburbrate +
                        "&call_fare=" + "0" +
                        "&call_extra_rate=" + "0" +
                        "&spec_extra_rate=" + CalFareBase.mComplexrate +
                        "&range_extra_dist=" + "0" + //구간할증구간 이동거리
                        "&range_extra_rate=" + CalFareBase.mComplexrate +
                        "&cut_off_fare=" + "2" + // 요금절사 - 0:안함 1:1원단위 2:10원단위
                        "&today_dist=" + (Integer.parseInt(dayDrvRecordData[0]) + Integer.parseInt(dayDrvRecordData[1])) +
                        "&bincha_dist=" + Integer.parseInt(dayDrvRecordData[1]) +
                        "&juhaeng_dist=" + Integer.parseInt(dayDrvRecordData[0]) +
                        "&juhaengcount=" + driveCount +
                        "&worktime=" + (Integer.parseInt(dayDrvRecordData[2]) + Integer.parseInt(dayDrvRecordData[3])) +
                        "&money=" + dayDrvRecordData[4];
                DTG_PATH = "PairingInfoAPI?" + DTG_PARAMS;
                break;
            case 3:   //버튼 이벤트
//                if (subParam[0].equals("05")) break;
                DTG_PARAMS = "info_dtti=" + dtti +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&button_val=" + subParam[0] +
                        "&dist=" + subParam[1] +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&state=" + "1" + //시동 0:OFF / 1:ON
                        "&paytype=" + subParam[2] + // 1: 현금 2: 카드 3: 지역화폐
                        "&fare=" + subParam[3] +
                        "&zero_base_fare=" + subParam[4] + // 기본요금 : 0 , 기본요금외요금 :1
                        "&call_payment=" + "0" + //호출요금
                        "&add_fare=" + subParam[5] + //추가요금
                        "&time_extra_fare=" + subParam[6] + //야간할증요금
                        "&range_extra_fare=" + subParam[7] + //구간할증요금
                        "&city_extra_fare=" + subParam[8] + //시외할증요금
                        "&time_rate_fare=" + subParam[9] + //차가 서있을 때 미터기 감소로 발생하는 요금
                        "&base_dist=" + CalFareBase.BASEDRVDIST +
                        "&meter_per_sec1=" + (int) (CalFareBase.INTERVAL_DIST / CalFareBase.INTERVAL_TIME) +
                        "&meter_per_sec2=" + (int) (CalFareBase.INTERVAL_DIST / CalFareBase.INTERVAL_TIME) +
                        "&time_extra_dist=" + subParam[10] + //야간할증거리
                        "&city_extra_dist=" + subParam[11] + //시외할증거리
                        "&range_extra_dist=" + subParam[12] + //구간할증거리
                        "&time_extra_sec=" + subParam[13] + //야간할증시간
                        "&city_extra_sec=" + subParam[14] + //시외할증시간
                        "&range_extra_sec=" + subParam[15] + //구간할증시간
                        "&time_fare=" + "0" + //◇계산 방법=시속 l5km를 기준으로 그 이상 속도일 때는 거리가 요금으로 나오고 그 이하일 때는 시간이 요금으로 계산되는 상호병산제. 시간 요금은 15km 이하 속도일 때는 l5km 속도로 계속 달린 것으로 상정해 그때 나올 거리 요금을 시간으로 쪼갠 1분 36초에 50원씩이다
                        "&fee_counter=" + "0" +
                        "&office_id=" + AMBlestruct.AMLicense.companynum; //20220103
//                        "&office_id=" + "9876543211";
                DTG_PATH = "EventAPI?" + DTG_PARAMS;
//                Log.d("final_distance", subParam[1]+"");  //거리
//                Log.d("final_distance", subParam[3]+"");  //요금
//                Log.d("final_distance", subParam[5]+"");  //추가요금

                Log.d("Send_DTGData", div + ": " + DTG_PARAMS);


                break;
            default:
                DTG_PATH = "empty";
                break;

        }

        if (mSendDTGQ.remainingCapacity() == 0) {
            try {
                mSendDTGQ.take();
            } catch (Exception e) {

            }
        }
        mSendDTGQ.add(DTG_PATH);

        mNowDtti = dtti;
//        if (!setURL) {
//            setURL = true;
//        }
//        Log.e("params ", DTG_PATH);
    }

    class DTG_NetworkThread implements Runnable {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (true) {
                        if (mSendDTGQ.remainingCapacity() < 5) {

                            URL url = new URL(DTG_BASEURL + mSendDTGQ.take());
                            Log.d("Send_DTGData", "uri " + url.toString());
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setConnectTimeout(2000);
                                conn.setUseCaches(false);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();
                                        Log.e("read", connres);
                                    } catch (Exception e) {
                                        Log.e("read DTG", e.getMessage());
                                    }

                                }
                            }
                            mLastDtti = mNowDtti;
                        }
                        setURL = false;
                    }
                    Thread.sleep(500);
                }
            } catch (Exception ex) {
                Log.e("DTG Thread Error", "error is : " + ex.toString());
                ex.printStackTrace();
            }

        }
    }

    class n_TIMS_Thread implements Runnable {
        public void run() {

            TIMSQueue que = null;
            TIMSQueue errorque = null;
            boolean mSendFlag = true;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mTIMSsendQ.remainingCapacity() < 20 || errorque != null) {

                        if (Info.REPORTREADY) {
                            if (mSendFlag == false)
                                Thread.sleep(1000);

                            if (errorque != null) {

                                que = errorque;
                                if (que.mtrycount % 15 == 0) {

                                    que.mtrycount++;
                                } else {
                                    que.mtrycount++;
                                    continue;

                                }

                            } else {
                                que = mTIMSsendQ.take();
                                errorque = null;

                            }
                        } else
                            que = mTIMSsendQ.take();

                        que.mtrytime = System.currentTimeMillis(); //20210419
                        que.mtrycount++;

                        mSendFlag = true;
                        if (que.mSendType == 1) {

//for test. 20210504 임시.

                            if (que.mSubType == 1) {

                                Info.Savedata(Info.g_nowKeyCode + "_tims.txt", que.mJson.toString(), "TIMS");
                                mCallback.serviceMessage(2, "TIMS 영업정보보관 OK");
//                                    continue;
                            } else if (que.mSubType == 2) {
                                Info.Savedata(Info.gTimsLastDate + "_event.txt", que.mJson.toString(), "TIMS");
//                                    mCallback.serviceMessage(2, "TIMS 버튼DATA보관 OK");
//                                    continue;
                            } else if (que.mSubType == 3) {
                                Info.Savedata(Info.gTimsLastDate + "_power.txt", que.mJson.toString(), "TIMS");
//                                    mCallback.serviceMessage(3, "TIMS 버튼DATA보관 OK");

                            }

                            if (Info.TIMSUSE_TEST)
                                continue;

                            URL url = new URL(que.mURLs);

                            switch (que.mSubType) {
                                case 1:
                                    Info.mDriveTIMSok = "실패";
                                    break;

                                case 2:
                                    Info.mEventTIMSok = "실패";
                                    break;

                                case 3:
                                    Info.mPowerTIMSok = false;
                                    break;

                            }

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Accept", "application/json");
                                conn.setRequestProperty("Content-type", "application/json");
                                conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);
                                conn.setDoOutput(true);
                                conn.setDoInput(true);

                                OutputStream os = conn.getOutputStream();
                                os.write(que.mJson.toString().getBytes("UTF-8"));
                                os.flush();

                                if (Info.REPORTREADY)
                                    Info._displayLOG(true, "" + que.mJson.toString().length(), "TIMS SEND-");

                                if (conn != null) {

                                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        try {
                                            BufferedReader br = new BufferedReader(
                                                    new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                            String connres = br.readLine();
                                            if (Info.REPORTREADY)
                                                Info._displayLOG(true, connres, "TIMS RECEIVE(POST)-");

                                            switch (que.mSubType) {
                                                case 1:
                                                    Info.mDriveTIMSok = "성공";
                                                    mCallback.serviceMessage(1, "TIMS 영업DATA전송 OK");
                                                    break;

                                                case 2:
                                                    Info.mEventTIMSok = "성공";
                                                    mCallback.serviceMessage(0, "TIMS 버튼DATA전송 OK");

                                                    break;

                                                case 3:
                                                    Info.mPowerTIMSok = true;
                                                    mCallback.serviceMessage(0, "TIMS POWER DATA전송 OK");

                                                    break;

                                            }

                                        } catch (Exception e) {
                                            Log.e("e POST TIMS", e.getMessage());

                                            mSendFlag = false;

                                        }
                                    } else {
                                        Info._displayLOG(true, "status code=" + conn.getResponseCode(), "");
                                        Info._displayLOG(true, "content message=" + conn.getErrorStream(), "");
                                        mSendFlag = false;
                                    }

                                    conn.disconnect();

                                    if (mSendFlag == false) {
                                        switch (que.mSubType) {
                                            case 1:
                                                mCallback.serviceMessage(0, "TIMS 영업DATA전송 FAIL!");
                                                break;

                                            case 2:

                                                mCallback.serviceMessage(0, "TIMS 버튼DATA전송 FAIL!");
                                                break;

                                            case 3:
                                                ;
                                                break;
                                        }
                                    }

                                }
                            }

                        } else if (que.mSendType == 2) {
//일반GET url
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();
                                        if (Info.REPORTREADY)
                                            Info._displayLOG(true, " 성공 " + connres, "TIMS RECEIVE(GET)-");

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(true, "status code=" + conn.getResponseCode(), "");
                                    Info._displayLOG(true, "content message=" + conn.getErrorStream(), "");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        } else if (que.mSendType == 4) {
//택시차량번호인증
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();

                                        if (connres.contains("ERROR")) {

                                            Info._displayLOG(true, " 차량번호인증실패 " + connres, "TIMS RECEIVE(GET)-");
                                            mCallback.serviceMessage(98, "차량번호인증실패");
                                        } else {

                                            mCallback.serviceMessage(0, "TIMS 차량번호인증성공");
                                            Info._displayLOG(true, " 차량번호인증성공 " + connres, "TIMS RECEIVE(GET)-");

                                        }

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(true, "차량번호인증실패 " + "status code=" + conn.getResponseCode(), "");
                                    mCallback.serviceMessage(98, "차량번호인증실패");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        } else if (que.mSendType == 5) {
//운전자격인증
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();

                                        if (connres.contains("ERROR")) {

                                            Info._displayLOG(true, " 운전자격인증실패 " + connres, "TIMS RECEIVE(GET)-");
                                            mCallback.serviceMessage(97, "운전자격인증실패");
                                        } else {

                                            mCallback.serviceMessage(0, "TIMS 운전자격인증성공");
                                            Info._displayLOG(true, " 운전자격인증성공 " + connres, "TIMS RECEIVE(GET)-");
                                        }

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(true, "운전자격인증실패 " + "status code=" + conn.getResponseCode(), "");
                                    mCallback.serviceMessage(97, "운전자격인증실패");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        }

                        Thread.sleep(300); //20210419

                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                    mSendFlag = false;

                }

                if (Info.REPORTREADY) {
                    if (mSendFlag == false) {
                        errorque = que;

                        Info._displayLOG(true, "TIMS전송실패", "");
                    } else
                        errorque = null;

                }

            }

        }
    }

    /*class n_TIMS_Thread implements Runnable {
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {

                    URL url = new URL()
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }*/
    ////////////////////////
//main thread
//20201211
    class MainThread implements Runnable {
        public void run() {

            double getSpeed = 0;
            boolean buseDTG = false;
            boolean buseGPS = false;
            int iGPSTimer = 0;
            int iDTGTimer = 0;
            CalQueue que;
            double ndisttemp = 0;
            long ntimetemp = 0;

            CalQueue quetmp = new CalQueue();

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    if (mCalblockQ.remainingCapacity() < 10) {

                        que = mCalblockQ.take();

                        if (que.nType == 1) //gps
                        {

                            if (mbDrivestart) {

                                update_calque(que);

                            } else
                                carculate_empty(que.distance, que.speed);

                            mCalwhich = 1;
                            mnUseGPS = 99;

                            mndtgused = 0;

                            ntimetemp = 0; //20201211
                            ndisttemp = 0; //20201211

                        } else if (que.nType == 2) //dtg
                        {
                            if (mCalwhich == 2) {
//20210703                                    if (que.speed < CalFareBase.TIMECOST_LIMITSECOND)
                                if (que.speed * 3.6 < CalFareBase.TIMECOST_LIMITHOUR)
                                    ntimetemp = que.itimet;

                                if (mbDrivestart) {

                                    update_calque(que);

                                } else
                                    carculate_empty(que.distance, que.speed);

                                ntimetemp = 0;

//20210701                                    if (mnUseGPS == 99)
                                if (mnUseGPS == 100)
                                    mnUseGPS = 1;

                            } else {
                                if (mndtgused >= 3) //gps에서 dtg로바뀔때 3회수신이후 전환
                                {

                                    mCalwhich = 2;

                                    mnUseGPS = 100; //20210701

                                    if (mbDrivestart) {

//                                            que.speed = que.distance / mndtgused; //que.speed;
                                        que.distance += ndisttemp;

//20210711                                            carculate_fare(que);
                                        update_calque(que);
                                    } else {
                                        que.distance += ndisttemp;
                                        carculate_empty(que.distance, que.speed);

                                    }
                                    mngpserror++;
                                    if (mngpserror > 10) {
                                        if (mbgpserror == false) {

                                            mbgpserror = true;
                                            //                                            maincallback.GpsState(false);

                                        }
                                    }

                                    mndtgused = 0;
                                    ntimetemp = 0;
                                    ndisttemp = 0;


                                } else {

                                    ndisttemp += que.distance;

                                    mndtgused++;

                                }

                            }

                            set_lastDTGque(que);

                        }
                    } //if(mCalblockQ
                    else {
                        mngpserror++;
                        if (mngpserror > 10) {
                            if (mbgpserror == false) {

                                mbgpserror = true;
                                //                                maincallback.GpsState(false);

                            }
                        }

                    }

                    //                    quetmp.ilasttime = System.currentTimeMillis();

////////////////////////////

                    Thread.sleep(50); //250); //20210701 500);
//                        Date date = new Date(System.currentTimeMillis());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    //////////////////////////
    class CheckStateThread implements Runnable {

        public void run() {

            int ncount = 0;
            int drvTimes = 0;
            int nBTCconnect = 0;

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    if (false) //Info.mBTFirstOK)
                    {

                        if (Info.mBTOnOffCheck > 20) {

                            if (Info.bBTRestarting == false)
                                set_meterhandler.sendEmptyMessage(99);

                            Info.bBTRestarting = true;
                            Info.mBTOnOffCheck++;

                        } else if (Info.bBTRestarting == false)
                            Info.mBTOnOffCheck++;

                    }

                    if (AMBlestruct.mbRStateupdated) {

                        set_meterhandler.sendEmptyMessage(2);

                    }

                    if (setting.gUseBLE && setting.BLUETOOTH_FINDEND) {
                        if (AMBlestruct.mBTConnected == false && (mBluetoothLE != null) && setting.BLUETOOTH_DEVICE_ADDRESS.equals("") == false) //20201215
                        {

                            //20210310                        nBTCconnect = (nBTCconnect + 1) % 20; // + 1;
                            if (nBTCconnect == 0) {

                                connectAM();

                            }

                            nBTCconnect = (nBTCconnect + 1) % 20; //20210615 30; //20210310

                            bInitDTGdata = false; //20201110
                        } else
                            nBTCconnect = 0; //20210310

                        ///////////////////////
                        //20201110


                        if (bInitDTGdata == false) {

                            if (mBluetoothLE._is_gattCharTrans() && AMBlestruct.mBTConnected == true) {
                                bInitDTGdata = true;

                                Thread.sleep(500);
                                mBluetoothLE._init_Am100DTGdata();
                            }

                        }
                    } else if (setting.BLUETOOTH_FINDEND) {
                        if (AMBlestruct.mBTConnected == false && (mBluetoothLE != null) && bInitDTGdata == false) {
                            connectAM();
                            bInitDTGdata = true;
                            Thread.sleep(500);
                            mBluetoothLE._init_Am100DTGdata();

                        }
                    }

                    drvTimes++;
                            /*if(drvTimes%2 == 0) {
                                Log.e("1 Sec Record",  mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude() + "/" + (int)(mGPSspeed * 3.6) + "/" + (int)dtgReportDist);
                            }*/

                    if (drvTimes % 120 == 0) {  // 1분 = 120,    30초 = 60
                        //                            Log.e("drive Time", drvTimes / 2 + " | " + date);
                        Send_DTGData(1, null);
                        dtgReportDist = 0;
                    }

                    Thread.sleep(500);  //0.5초에

                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }


            }

        }
    }

    public Handler payHandler = new Handler() {
        public void handleMessage(Message msg) {
            ////////////////////////////////////////

            if (msg.what == 1) {
//                m_lbsTvPayment.setText(lbs_nPayment);
                //todo: 20210902
                long value = Long.parseLong(lbs_nPayment);
                DecimalFormat format = new DecimalFormat("###,###");
                String payVal = format.format(value);

                m_lbsTvPayment.setText(payVal);
                //m_lbsTvPayment.setText(lbs_nPayment);
                //todo: 20220209
                menu_btn_layout.setBackgroundResource(R.drawable.edit_backgroud_radius_bottomright);
                //todo: end

                if (Info.bOverlayshow) {
                    m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
                }

            } else if (msg.what == 2) {
                m_WindowManager.updateViewLayout(m_Lbsview, m_Params);
            }
        }
    };

    public Handler set_meterhandler = new Handler() {
        //		@Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.e("SET MeterHandler", msg.what + "");

            if (msg.what == 1) {

//                connectBLE();
                connectAM();

            } else if (msg.what == 2) {
                Log.e("getMSG", AMBlestruct.mRState);

                if (AMBlestruct.mRState.equals("01")) //01: 지불
                {

//                    mtextinfo.setText("지 불");
                    Log.d(TAG, "지불수신함");

                    mCallback.serviceMeterState(AMBlestruct.MeterState.PAY, 0);


                } else if (AMBlestruct.mRState.equals("05")) //05: 빈차 예약상태 등에서 빈차 상태로 변경이 되는 경우
                {
//                    mtextinfo.setText("빈 차");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.EMPTY, 0);

                } else if (AMBlestruct.mRState.equals("20")) //20: 주행
                {
//                    mtextinfo.setText("주 행");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.DRIVE, 0);

                } else if (AMBlestruct.mRState.equals("30")) //30: 할증
                {
//                    mtextinfo.setText("할 증");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.EXTRA, 0);
//20211220                    update_BLEmeterstate("30");

                } else if (AMBlestruct.mRState.equals("40")) //40: 예약/호출
                {
//                    mtextinfo.setText("예 약");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.APPOINT, 0);
//20211220                    update_BLEmeterstate("40");
                } else if (AMBlestruct.mRState.equals("50")) //20210823 50: 휴무.
                {
//                    mtextinfo.setText("휴 무");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.HOLIDAY, 0);
//20211220                    update_BLEmeterstate("50");
                } else if (AMBlestruct.mRState.equals("61")) //20211220 전환.
                {

                    show_mainActivity();
//20211220                    update_BLEmeterstate("61");
                } else if (AMBlestruct.mRState.equals("62")) //20220126 시외.
                {

                    ;

                }
                AMBlestruct.setRStateupdate(false);

            } else if (msg.what == 3) { //결제결과

                mCallback.serviceMeterState(AMBlestruct.MeterState.ENDPAYMENT, 0);

            } else if (msg.what == 5) { //결제취소결과

                mCallback.serviceMeterState(AMBlestruct.MeterState.ENDCANCELPAYMENT, AMBlestruct.AMCardCancel.mFare);

            } else if (msg.what == 11) //20201112
            {
//no use

                try {

                    Thread.sleep(200);

                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                writeBLE("11"); //for 인증, dtg활성화

            } else if (msg.what == 12) {

                if (AMBlestruct.AMReceiveFare.mstate == 1) {
                    Log.d(TAG, "지불수신함...");
                    mCallback.serviceMeterState(AMBlestruct.MeterState.PAY, 0);

                } else if (AMBlestruct.AMReceiveFare.mstate == 2) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.EMPTY, 0);

                } else if (AMBlestruct.AMReceiveFare.mstate == 3) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.DRIVE, 0);

                } else if (AMBlestruct.AMReceiveFare.mstate == 4) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.EXTRA, 0);

                }

            } else if (msg.what == 13) //20201110
            {

/*
                try {

                    Thread.sleep(500);

                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
*/

                writeBLE("14"); //for 인증, dtg활성화

            } else if (msg.what == AMBlestruct.MeterState.EXTRATIME) //20201203
            {

                mCallback.serviceMeterState(AMBlestruct.MeterState.EXTRATIME, 0);

            } else if (msg.what == AMBlestruct.MeterState.EXTRATIMEOFF) //20201203
            {

                mCallback.serviceMeterState(AMBlestruct.MeterState.EXTRATIMEOFF, 0);

            } else if (msg.what == AMBlestruct.MeterState.SUBURBSIN) //20210716
            {

                mCallback.serviceMeterState(AMBlestruct.MeterState.SUBURBSIN, 0);

            } else if (msg.what == AMBlestruct.MeterState.SUBURBSOUT) //20210716
            {

                mCallback.serviceMeterState(AMBlestruct.MeterState.SUBURBSOUT, 0);

            } else if (msg.what == AMBlestruct.MeterState.BLELEDON) {
                mCallback.serviceMeterState(AMBlestruct.MeterState.BLELEDON, 0);
                String param[] = {"1", "AM100"};
                Send_DTGData(2, param);

            } else if (msg.what == AMBlestruct.MeterState.BLELEDOFF) {

                mCallback.serviceMeterState(AMBlestruct.MeterState.BLELEDOFF, 0);
                String param[] = {"0", "AM100"};
                Send_DTGData(2, param);

            } else if (msg.what == AMBlestruct.MeterState.BLE1SDATAOK) //20201215
            {

                mCallback.serviceMessage(99, "AM빈차등 DATA수신합니다.");
//20220107 in ambluetoothlemanager                send_driverinfo(); //20220107 ???

                Log.d(TAG, "AM빈차등 DATA수신합니다.");
            } else if (msg.what == AMBlestruct.MeterState.BLE1SDATAERROR) //20201215
            {
                mCallback.serviceMeterState(AMBlestruct.MeterState.BLELEDOFF, 0);
//20210823                mCallback.serviceMessage(98, "AM빈차등 DATA수신을 할수 없습니다.");
                Log.d(TAG, "AM빈차등 DATA수신을 할수 없습니다.");
            } else if (msg.what == 90) {

            } else if (msg.what == 99) //20210329
            {

            }


        }
    };


    ////////////////////////
    public void _checkExtraTime() {

        if (Info.REPORTREADY) {

            SimpleDateFormat format2 = new SimpleDateFormat("a hh:mm:ss");
            Calendar time = Calendar.getInstance();
            Info._displayLOG(true, "GPS시간 " + format2.format(time.getTime()), "");

        }

        if (Integer.parseInt(Info.getCurHourString()) < 4) {

            if (CDrive_val.mbExtraTime == false)
                set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.EXTRATIME);

            CDrive_val.mbExtraTime = true;
        } else {
            if (CDrive_val.mbExtraTime == true)
                set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.EXTRATIMEOFF);

            CDrive_val.mbExtraTime = false;

        }
    }

    //////////////////
//20210716
    private void _checkSuburbs(double lon, double lat) {

        if (lon == 0) //20211229
            return;

        if (mbDrivestart == true && Suburbs.mSuburbOK == true) {

            boolean bResult = Suburbs.Suburb_check(lon, lat);

            if (false) //Info.REPORTREADY)
            {

                if (bResult) {

                    Info._displayLOG(true, "시경계check: IN (" + mLastLocation.getLongitude() + ", " + mLastLocation.getLatitude() + ")" + mbSuburb, "");

                } else {

                    Info._displayLOG(true, "시경계check: OUT (" + mLastLocation.getLongitude() + ", " + mLastLocation.getLatitude() + ")" + mbSuburb, "");

                }

            }

            if (true) //20210818 Info.APPMETERRUNSTOP == 1) //임시.
            {

                if (bResult == true) {
                    if (mbSuburb == true) {
                        mbSuburb = false;
                        CDrive_val.mbExtraSuburb = false;
                        set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.SUBURBSIN);
//                        mCallback.serviceMessage(0, "시경계check: IN");

                    }

                } else if (bResult == false) {
                    if (mbSuburb == false) {
                        mbSuburb = true;
                        CDrive_val.mbExtraSuburb = true;
                        set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.SUBURBSOUT);
//                        mCallback.serviceMessage(0, "시경계check: OUT");

                    }

                }
            }

        }
    }

    ///////////////////
//20210701
    private void set_lastDTGque(CalQueue que) {
        mLastDTGque.distance = que.distance;
        mLastDTGque.speed = que.speed;
        mLastDTGque.altitude = que.altitude;
        mLastDTGque.ilasttime = que.ilasttime;
        mLastDTGque.icurtime = que.icurtime;
        mLastDTGque.itimet = que.itimet;
        mLastDTGque.nowlong = que.nowlong;
        mLastDTGque.nowlat = que.nowlat;
        mLastDTGque.lastlong = que.lastlong;
        mLastDTGque.lastlat = que.lastlat;
        mLastDTGque.nType = que.nType;
        mLastDTGque.bused = false;
    }

    private void update_calque(CalQueue que) {
        mCalque.distance = que.distance;
        mCalque.speed = que.speed;
        mCalque.altitude = que.altitude;
        mCalque.ilasttime = que.ilasttime;
        mCalque.icurtime = que.icurtime;
        mCalque.itimet = que.itimet;
        if (que.nType == 1) {

            mCalque.nowlong = que.nowlong;
            mCalque.nowlat = que.nowlat;
            mCalque.lastlong = que.lastlong;
            mCalque.lastlat = que.lastlat;
        }

        mCalque.nType = que.nType;
        mCalque.bused = false;

//        if(Info.TIMSUSE_TEST)
//        {
//
//            Info._displayLOG(true, "--update " + que.nType, "");
//
//        }

//        _Receive_calQ(); //20210702

    }
////////////////////////

    public void carculate_fare(CalQueue que) {
        //Log.e("curSpeed" , (curSpeed * 3.6) + "");
        double distance = que.distance;
        double curSpeed = que.speed;
        String altitude = String.format("%.2f", que.altitude);
        long timet = que.itimet;
        long curtime = que.icurtime;
        long lasttime = que.ilasttime;
        int ntype = que.nType;

        double deltaDistance = 0;

        int nCalfare = 0; //20201203

        String stype = "";

        if (Info.REPORTREADY) {

            if (ntype == 1) {

                Info._displayLOG(true, "GPS정상수신 ", "");
                stype = " ";
            } else {

                Info._displayLOG(true, "GPS오류(비정상)", "");
                stype = "OBD ";
            }

//for test???
            if (CalFareBase.CALTYPE == 1 || CalFareBase.CALTYPE == 2)
                distance = 0;

            if (curSpeed < CalFareBase.TIMECOST_LIMITSECOND && CalFareBase.CALTYPE == 4) {

                distance = 0;

            }

        }

        _checkExtraTime();

        _checkSuburbs(que.nowlong, que.nowlat);

        CDrive_val.mDrivetimeT = (int) (System.currentTimeMillis() - CDrive_val.mDrivestart);
//20210703        CDrive_val.mDrivedistanceT += distance;

        if (CDrive_val.mFaredistanceT >= CalFareBase.BASEDRVDIST) {

            CDrive_val.mFaresubdistT += distance;

            if (curSpeed * 3.6 < CalFareBase.TIMECOST_LIMITHOUR) //CalFareBase.TIMECOST_LIMITSECOND)
            {

//20210703                if (timet > 0)
///                    deltaDistance = (CalFareBase.BASEDIST_PER1S * (timet / 1000.0)); //20201211

                deltaDistance = CalFareBase.BASEDIST_PER1S; //

                if (Info.REPORTREADY && ((CalFareBase.CALTYPE == 3) || CalFareBase.CALTYPE == 5)) {

                    deltaDistance = 0;

                }

                CDrive_val.mFaresubdistT += deltaDistance;

                distanceLimit = CalFareBase.INTERVAL_DIST - CDrive_val.mFaresubdistT;

//                Log.e("Limit Value", CDrive_val.mFareT + " || " + distance + " || " + timet + " || " + deltaDistance
///                        + " || " + CDrive_val.mFaresubdistT + " == " + distanceLimit + " || " + CDrive_val.mDrivetimeT / 1000 + "/" + ntype);

            } else {

                distanceLimit = CalFareBase.INTERVAL_DIST - CDrive_val.mFaresubdistT;

            }

            if (Info.REPORTREADY) {

                Info._displayLOG(true, stype + "한계속도(" + CalFareBase.TIMECOST_LIMITHOUR + ")" + "미만, 현재속도 " + curSpeed * 3.6, "");

            }

            if (Info.REPORTREADY) {
                Info._displayLOG(true, stype + "시간요금적용 경과시간 " + (timet / 1000.0) + "초", "");

                if (CalFareBase.CALTYPE == 2) {

                    Info._displayLOG(true, stype + "택시요금 시간요금 " + deltaDistance + "원", "");

                } else
                    Info._displayLOG(true, stype + "택시요금 시간거리환산 " + deltaDistance + "m", "");

            }

            if (Info.REPORTREADY) {
                Info._displayLOG(true, stype + "택시요금 거리환산 " + distance + "m", "");
                if (CalFareBase.CALTYPE == 2) {
                    Info._displayLOG(true, stype + "택시요금잔여(잔여=기준금액-운행합산(현합산)) " + distanceLimit + "=" + CalFareBase.BASEDRVDIST + "-"
                            + CDrive_val.mFaresubdistT + "(" + (distance + deltaDistance) + ")", "");

                } else
                    Info._displayLOG(true, stype + "택시요금거리(잔여=기준거리-운행합산(현합산)) " + distanceLimit + "=" + CalFareBase.BASEDRVDIST + "-"
                            + CDrive_val.mFaresubdistT + "(" + (distance + deltaDistance) + ")", "");
            }

            CDrive_val.mFaredistanceT += (distance + deltaDistance); //20210729

//20210319            while (distanceLimit <= 0) {
            while (CDrive_val.mFaresubdistT >= CalFareBase.INTERVAL_DIST) {
                nCalfare = CalFareBase.DISTCOST; // TIMECOST;

                if (CDrive_val.mbExtraTime == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mNightTimerate / 10) * 10;

                if (CDrive_val.mbExtraComplex == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mComplexrate / 10) * 10;

                if (CDrive_val.mbExtraSuburb == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mSuburbrate / 10) * 10;

                CDrive_val.mFareT += nCalfare; //CalFareBase.TIMECOST + (CalFareBase.TIMECOST * CalFareBase.mComplexrate);

                CDrive_val.mFaresubdistT = CDrive_val.mFaresubdistT - CalFareBase.INTERVAL_DIST; //20210319 Math.abs(distanceLimit);
//20210319                    distanceLimit += CalFareBase.INTERVAL_DIST;
                distanceLimit = CalFareBase.INTERVAL_DIST - CDrive_val.mFaresubdistT;

                Info._displayLOG(true, "//잔여거리2 " + distanceLimit, "");

            }

        } else {

//            if(distance > 1) { //for tims.
//                distance = distance - 0.02; //20210703
//                curSpeed = distance;

//            }

//            if (curSpeed < CalFareBase.TIMECOST_LIMITSECOND)
            if (curSpeed * 3.6 < CalFareBase.TIMECOST_LIMITHOUR) //CalFareBase.TIMECOST_LIMITSECOND)
            {

//20210703                if (timet > 0)
///                    deltaDistance = (CalFareBase.BASEDIST_PER1S * (timet / 1000.0)); //20201211
                deltaDistance = CalFareBase.BASEDIST_PER1S;

                if (Info.REPORTREADY && ((CalFareBase.CALTYPE == 3) || CalFareBase.CALTYPE == 5)) {

                    deltaDistance = 0;

                }

                if (false) //CDrive_val.mbStartExtra == false && CDrive_val.mbExtraTime == true)
                {

                    distance = distance + distance * 0.2;
                    deltaDistance = deltaDistance + deltaDistance * 0.2;
                }

                CDrive_val.mFaresubdistT += distance;
                CDrive_val.mFaresubdistT += deltaDistance;

                distanceLimit = CalFareBase.BASEDRVDIST - CDrive_val.mFaresubdistT;

//                Log.d(TAG, "Limit Value " + CDrive_val.mFareT + " || " + distance + " || " + timet + " || " + deltaDistance
///                        + " || " + CDrive_val.mFaresubdistT + " == " + distanceLimit + " || " + CDrive_val.mDrivetimeT / 1000 + "/" + ntype);

            } else {
//                Log.e("curSpeed High", (curSpeed * 3.6) + "");

                if (false) //CDrive_val.mbStartExtra == false && CDrive_val.mbExtraTime == true)
                {

                    distance = distance + distance * 0.2;

                }

                CDrive_val.mFaresubdistT += distance;
                distanceLimit = CalFareBase.BASEDRVDIST - CDrive_val.mFaresubdistT;

            }

            if (Info.REPORTREADY) {

                if (curSpeed < CalFareBase.TIMECOST_LIMITSECOND) {

                    Info._displayLOG(true, stype + "한계속도(" + CalFareBase.TIMECOST_LIMITHOUR + ")" + "미만, 현재속도 " + curSpeed * 3.6, "");

                } else
                    Info._displayLOG(true, stype + "한계속도(" + CalFareBase.TIMECOST_LIMITHOUR + ")" + "이상, 현재속도 " + curSpeed * 3.6, "");

            }

            if (Info.REPORTREADY) {

                Info._displayLOG(true, stype + "시간요금적용 경과시간 " + (timet / 1000.0) + "초", "");
                if (CalFareBase.CALTYPE == 2) {

                    Info._displayLOG(true, stype + "택시요금 시간요금 " + deltaDistance + "원", "");

                } else
                    Info._displayLOG(true, stype + "택시요금 시간거리환산 " + deltaDistance + "m", "");
            }

            if (Info.REPORTREADY) {
                Info._displayLOG(true, stype + "택시요금 거리환산 " + distance + "m", "");
                if (CalFareBase.CALTYPE == 2) {
                    Info._displayLOG(true, stype + "택시요금잔여(잔여=기준금액-운행합산(현합산)) " + distanceLimit + "=" + CalFareBase.BASEDRVDIST + "-"
                            + CDrive_val.mFaresubdistT + "(" + (distance + deltaDistance) + ")", "");
                } else
                    Info._displayLOG(true, stype + "택시요금거리(잔여=기준거리-운행합산(현합산)) " + distanceLimit + "=" + CalFareBase.BASEDRVDIST + "-"
                            + CDrive_val.mFaresubdistT + "(" + (distance + deltaDistance) + ")", "");
            }

            CDrive_val.mFaredistanceT = CDrive_val.mFaresubdistT;

//            if (distanceLimit <= 0)
//            while (distanceLimit <= 0) {
            while (CDrive_val.mFaresubdistT - CalFareBase.BASEDRVDIST >= 0) {
                nCalfare = CalFareBase.DISTCOST;

                if (CDrive_val.mbExtraTime == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mNightTimerate / 10) * 10;

                if (CDrive_val.mbExtraComplex == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mComplexrate / 10) * 10;

                if (CDrive_val.mbExtraSuburb == true)
                    nCalfare += Math.round(CalFareBase.DISTCOST * CalFareBase.mSuburbrate / 10) * 10;

                CDrive_val.mFaresubdistT = CDrive_val.mFaresubdistT - CalFareBase.BASEDRVDIST; //20210319 Math.abs(distanceLimit);
//20210319                    distanceLimit += CalFareBase.INTERVAL_DIST;

                distanceLimit = CalFareBase.INTERVAL_DIST - CDrive_val.mFaresubdistT;
                if (Info.REPORTREADY)
                    Info._displayLOG(true, "//잔여거리1 " + distanceLimit, "");

                CDrive_val.mFareT += nCalfare;

            }

        }

        CDrive_val.mDrivedistanceT += distance; //20210703

        mLastCaltime = curtime;

        if (Info.TIMSUSE) {
            Date date = new Date(que.icurtime);
            SimpleDateFormat dttiFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String dtti = dttiFormat.format(date);
//            mCallback.serviceTIMSDataEvent(TIMSIDX, dtti, mLastLocation, curSpeed, "", distance, (int) distanceLimit, CDrive_val.mFareT, que.nType + "");
            mCallback.serviceTIMSDataEvent(CDrive_val.TIMSIDX, dtti, mNowCalLocation, curSpeed, "", distance, distanceLimit,
                    CDrive_val.mFareT, que.nType - 1, nCalfare, CDrive_val.mDrivedistanceT, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb);
//                    CDrive_val.mFaredistanceT, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb);
//                    CDrive_val.mDrivedistanceT, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb); ??????????????

        }

        mCallback.serviceDisplayState(CDrive_val.mFareT, (int) distanceLimit, CDrive_val.mFareDiscount, curSpeed,
                Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))), CDrive_val.mDrivetimeT / 1000, mLastLocation,
                (int) CDrive_val.mFaredistanceT, (int) (distance + deltaDistance), true);

        if (Info.REPORTREADY) {

            Info._displayLOG(true, "현재 요금 " + CDrive_val.mFareT, "");

        }

        if (Info.TIMSUSE) {
            if (mNowCalLocation != null && mLastLocation != null) {
                if (que.nType == 1) {
                    mCallback.serviceLog(1, mNowCalLocation, (int) mLastLocation.getAccuracy(), (int) que.distance,
                            Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))), "",
                            mbDrivestart, (int) (curSpeed * 3.6), (timet / 1000.0), deltaDistance, distance, deltaDistance + distance,
                            distanceLimit, nCalfare, CDrive_val.mFareT, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb, CDrive_val.mFaredistanceT);

                } else {
                    mCallback.serviceLog(2, mNowCalLocation, (int) mLastLocation.getAccuracy(), (int) que.distance,
                            Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))), "",
                            mbDrivestart, (int) (curSpeed * 3.6), (timet / 1000.0), deltaDistance, distance, deltaDistance + distance,
                            distanceLimit, nCalfare, CDrive_val.mFareT, CDrive_val.mbExtraTime, CDrive_val.mbExtraSuburb, CDrive_val.mFaredistanceT);
                }
            }
        }
//        mCallback.serviceDisplayState(CDrive_val.mFareT, (int)(mLastDTGform.distance - mFirstDTGform.distance), CDrive_val.mFareDiscount, curSpeed,
//                Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))), CDrive_val.mDrivetimeT/1000, mLastLocation, true);

/////////////////////////
//temp log
///////////////////////

//        Log.d(TAG, "시간" + timet + " 속도" + curSpeed + " 남은거리" + distanceLimit);

        if (CDrive_val.m30cnt != (int) CDrive_val.mDrivetimeT / 1000 / 30) {

            CDrive_val.m30cnt = (int) CDrive_val.mDrivetimeT / 1000 / 30;
            if (mLastDTGform == null) {
                /*mCallback.serviceLog((int) CDrive_val.mDrivetimeT / 1000, mLastLocation, Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))),
                        0, 0, altitude, mbDrivestart);*/

            }
            /*else
                mCallback.serviceLog((int) CDrive_val.mDrivetimeT / 1000, mLastLocation, Integer.parseInt(String.valueOf(Math.round(CDrive_val.mDrivedistanceT))),
                        (int)(mLastDTGform.distance - mFirstDTGform.distance), (int)mLastDTGform.distance, altitude, mbDrivestart);*/ //tmp for log
        }

//20210325        CDrive_val.m30cnt = CDrive_val.mDrivetimeT / 1000 / 30;


        dtgReportDist += que.distance; //20210623 for dtg

    }

    //////////////////////
    public void carculate_empty(double distance, double curSpeed) {
        if (CEmpty_val.mEmptystart == 0) {
            CEmpty_val.init();
        }

        CEmpty_val.mEmptydistanceT += distance;
        CEmpty_val.mSpeed = (int) (curSpeed * 3.6);
        CEmpty_val.mEmptytimeT = (int) (System.currentTimeMillis() - CEmpty_val.mEmptystart);
        mCallback.serviceEmptyState(CEmpty_val.mEmptydistanceT, CEmpty_val.mEmptytimeT / 1000, CEmpty_val.mSpeed, mLastLocation);

        dtgReportDist += distance; //20210623 for dtg

//        Info._displayLOG(true, "enmpty dist. " + CEmpty_val.mEmptydistanceT , "");

    }

    /////////////////////////
//20210325
    synchronized public void add_calqueue(CalQueue que) {

        if (mbNeedDTGFirst) {
            if (mFirstDTGform.bvalid == true && que.nType == 2
                    && mCalblockQ.remainingCapacity() > 0) {

                mCalwhich = 2;
                mCalblockQ.add(que);
                mbNeedDTGFirst = false;
            }

        } else {

            if (mCalblockQ.remainingCapacity() > 0)
                mCalblockQ.add(que);
        }

    }

    synchronized public void add_timsQueue(TIMSQueue que) {

        if (mTIMSsendQ.remainingCapacity() > 0)
            mTIMSsendQ.add(que);
    }

    public double GetDistance_gps3D(Location lastlocation, Location nowlocation, double mGPSspeed) {

        double ndist = GetDistWGS84(lastlocation, nowlocation);

/*
        double altdist = Math.abs(nowlocation.getAltitude() - lastlocation.getAltitude());

        if(altdist > 30) //20210507
            altdist = 0;
*/

//20210701        double distance = Math.sqrt(Math.pow(ndist, 2) + Math.pow(altdist, 2));
        double distance = ndist; //택시미터검정기준

//        Log.d(TAG, "dist1 " + ndist + " dist2" + lastlocation.distanceTo(nowlocation) + " speed " + mGPSspeed);

/////////////
        if (ndist < 1) {

            distance = 0;
            ndist = 0;
        }

//?        distance = ndist; //no use altitude???

//        String temp;
//        temp = "" + String.format("%.1f", distance - ndist) + "(" + String.format("%.1f", distance) + "-" + String.format("%.1f", ndist) +
//               "/" + String.format("%.1f", (nowlocation.getAltitude() - lastlocation.getAltitude())) + ")";

//        mCallback.serviceLog(0, mLastLocation, (int)distance, (int)ndist, (int)mLastDTGform.distance, temp);

        return distance; // * 0.98;

    }

    ///////////////////////////
//20201211
    public void get_gps(Location location) {

        mngpserror = 0; //20210507

        mNowLocation.setLongitude(location.getLongitude());
        mNowLocation.setLatitude(location.getLatitude());

        if (mbgpserror == true) {

            mbgpserror = false;
//            maincallback.GpsState(true);
        }

        if (Info.g_appmode == Info.APP_PAYMENT) {

            mCallback.serviceDisplayState(0, 0, 0, 0, 0, 0, location, 0, 0, true);
            return;
        }

        location.getTime();
        mGPSspeed = (Double.parseDouble(String.format(Locale.getDefault(), "%.3f", location.getSpeed())));  // m/s
///        Log.e("curSpeed" , (mGPSspeed * 3.6) + "");

        if (false) {
            if (mCalLocation != null) {
                Info.Savedata(Info.g_nowKeyCode + "_odd.txt", "" + location.getTime() + " " + location.getAccuracy() + " (" + location.getLatitude() + ", "
                        + location.getLongitude() + ") " + GetDistWGS84(mLastLocation, location), "TIMS");

                Log.d("savedata", "" + location.getTime() + " " + location.getAccuracy() + " (" + location.getLatitude() + ", "
                        + location.getLongitude() + ") " + GetDistWGS84(mLastLocation, location));
            }
        }

        if (false) //Info.TIMSUSE_TEST)
        {

//            Info._displayLOG(true, "get_gps " + GetDistWGS84(mLastLocation, location) + " " + mNowLocation.getLongitude() + " " + mNowLocation.getLatitude(), "");
//            Info._displayLOG(true, "get_gpstmp " + GetDistWGS84tmp(mLastLocation, location) + " " + String.format("%.6f",mNowLocation.getLongitude())
//                    + " " + String.format("%.6f", mNowLocation.getLatitude()) + "(" + (GetDistWGS84(mLastLocation, location) - GetDistWGS84tmp(mLastLocation, location) - 0.005) + ")", "");
            Info._displayLOG(true, "get_gps dist " + GetDistWGS84(mLastLocation, location) + "---" + GetDistWGS84tmp(mLastLocation, location) + " " + CalFareBase.BASEDIST_PER1S, "");

        }

        long curtime = System.currentTimeMillis();
        long timet = 0;

        double ndistance = 0;

//cal 이전 mLastLocation과 location 거리.

        if ((location.getAccuracy() < 0 || location.getAccuracy() > 12)) //20210729 && mGPSspeed >= 1) // || (mLastLocation.distanceTo(location) - mGPSspeed > 10))
        //20201215  ///-~ 0~12 높을수록 오차범위가 큼 speed 10km
        {
            //오차범위가 커서 블루투스 기기를 활용
            mCalwhich = 2;
            mnUseGPS = 100;

//            if(Info.REPORTREADY)
//            {

//                Info._displayLOG(true, "GPS ACCURACY BAD OBD data전환 " + location.getAccuracy(), "");

//            }

            if (Info.TIMSUSE_TEST && mbDrivestart) {
                Info._displayLOG(true, "GPS ACCURACY BAD OBD data전환 " + location.getAccuracy(), "");
                Date date = new Date(curtime);
                SimpleDateFormat dttiFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String dtti = dttiFormat.format(date);

                Info.Savedata(Info.g_nowKeyCode + "_odd.txt", "" + dtti + " " + location.getAccuracy() + " (" + GetDistWGS84(mLastLocation, location)
                        + " - " + mGPSspeed + ") " + (GetDistWGS84(mLastLocation, location) - mGPSspeed), "TIMS");
            }
        } else if (mbDrivestart && mnUseGPS <= 2) {
            {

                Info._displayLOG(true, "mnUseGPS " + mnUseGPS, "");

            }
            mCalwhich = 2;
            mnUseGPS++;

            if (mGPSspeed >= 1) //20210729
                mCalLocation = location;

        } else if (mnUseGPS > 2) {
            //오차범위가 커서 블루투스 기기를 활용

            if (mCalLocation == null)
                mCalLocation = location;

            if (mCalLocation != null) {

                ndistance = GetDistance_gps3D(mLastLocation, mNowLocation, mGPSspeed);

//20210701
                if (mGPSspeed < 1) {

                    mNowLocation.setLongitude(mCalLocation.getLongitude());
                    mNowLocation.setLatitude(mCalLocation.getLatitude());
                }

//////////////
            }

            if (mLastGPStime > 0) {

                timet = curtime - mLastGPStime;
                if (timet < 0) //20210507
                    timet = 0;

            }

            if (ndistance < 80) {
                CalQueue que = new CalQueue();
                que.distance = ndistance;
                que.speed = mGPSspeed;
                que.altitude = location.getAltitude();
                que.icurtime = curtime;
                que.ilasttime = mLastGPStime;
                que.itimet = 1000; //timet;
                que.nowlong = mNowLocation.getLongitude();
                que.nowlat = mNowLocation.getLatitude();
                que.lastlong = mLastCallong;
                que.lastlat = mLastCallat;
                que.nType = 1;

                if (mnUseGPS == 3) //20210701
                    que.distance = CDrive_val.mLastDTGdist;

                mCalLocation = location;

                mLastCallong = mNowLocation.getLongitude();
                mLastCallat = mNowLocation.getLatitude();

                mCalLocation.setLongitude(mLastCallong);
                mCalLocation.setLatitude(mLastCallat);

                if (ndistance - mGPSspeed > 10) //20210701 15km
                {

                    mCalwhich = 2;
                    mnUseGPS = 100;
                } else {
                    add_calqueue(que);

                    mnUseGPS = 99;
                }

            } else {
                mCalwhich = 2;
                mnUseGPS = 100;

            }

        } else if (!mbDrivestart) {
            mnUseGPS = 99;
            mCalLocation = location;

        }

        mLastspeed = mGPSspeed;

//20210310
/*
        if (Info.TESTMODE) //20201211
        {
            if (location.getAccuracy() < 0 || location.getAccuracy() > 12) {
                mCallback.serviceLog(0, mLastLocation, (int) location.getAccuracy(), (int) ndistance, (int) mLastDTGform.distance - (int) mFirstDTGform.distance, "", mbDrivestart);
            } else {
                mCallback.serviceLog(0, mLastLocation, 9999, (int) ndistance, (int) mLastDTGform.distance - (int) mFirstDTGform.distance, "", mbDrivestart);
            }

        }
*/

        mLastLocation = location;
        mLastGPStime = curtime;

    }

    ////////////////////
    public void get_dtg(AMdtgform dtgform) {
        long curtime = System.currentTimeMillis();
        long timet = 0;
        double distance = 0;
        CalQueue que = new CalQueue();

        if (mLastDTGform.bvalid == true) {
            distance = dtgform.distance - mLastDTGform.distance;
            if (distance < 0)
                distance = 0;

//20201110            Log.d(TAG, "DTG distance speed " + mLastDTGform.distance + " " + dtgform.distance + "(" + distance + ") " + mLastDTGform.speed);
        }

//20211220        if (mLastDTGtime > 0 && mCalwhich == 2)
//        {
//            timet = curtime - mLastDTGtime;
//            if(timet < 0) //2010507
//                timet = 0;
//        }

//20121220
        if (mLastDTGtime > 0) {
            timet = curtime - mLastDTGtime;
            if (timet < 0 || timet > 30 * 1000) {
                distance = 0;
                timet = 0;

            } else if (distance > 100)
                distance = 0;

        } else
            distance = 0;

///////////////

//        distance = Math.random() * 30;

        que.distance = distance;
        que.speed = distance; //dtgform.speed;
        que.altitude = 0;
        que.icurtime = curtime;
        que.ilasttime = mLastDTGtime;
        que.itimet = 1000; //timet;

        que.nType = 2;

        CDrive_val.mLastDTGdist = que.distance;

        if (CDrive_val.TIMSIDX == 0) //20210701
        {

            que.distance = 0;
            que.speed = 0;
        }

        mLastDTGform.distance = dtgform.distance;
        mLastDTGform.speed = dtgform.speed;
        mLastDTGform.rpm = dtgform.rpm;
        mLastDTGform.breakstate = dtgform.breakstate;
        mLastDTGform.gpsstate = dtgform.gpsstate;
        mLastDTGform.gpsx = dtgform.gpsx;
        mLastDTGform.gpsy = dtgform.gpsy;
        mLastDTGform.bvalid = dtgform.bvalid;

        if (mFirstDTGform.bvalid == false) {
            mFirstDTGform.distance = mLastDTGform.distance;
            mFirstDTGform.speed = mLastDTGform.speed;
            mFirstDTGform.rpm = mLastDTGform.rpm;
            mFirstDTGform.breakstate = mLastDTGform.breakstate;
            mFirstDTGform.gpsstate = mLastDTGform.gpsstate;
            mFirstDTGform.gpsx = mLastDTGform.gpsx;
            mFirstDTGform.gpsy = mLastDTGform.gpsy;
            mFirstDTGform.bvalid = mLastDTGform.bvalid;

            if (mbNeedDTGFirst == true) {

                if (Info.REPORTREADY) {

                    Info._displayLOG(true, "주행중 앱미터종료후 재시작 이동거리 보정값거리 315m", "");

                }

                que.icurtime = curtime;
                que.ilasttime = curtime;
                que.itimet = 0;

                if (System.currentTimeMillis() - CDrive_val.mDrivestart > 3600 * 1000 || System.currentTimeMillis() - CDrive_val.mDrivestart < 0) {

                    que.distance = 0;
                    Info._displayLOG(true, "날짜경과는 " + (System.currentTimeMillis() - CDrive_val.mDrivestart), "");
                } else
                    que.distance = mFirstDTGform.distance - CDrive_val.mDTGFirstDist;

                if (que.distance < 0) //20211220
                    que.distance = 0;

            }

        }

//20211229
        if (que.distance > Info.CALDISTANCEMAX) //60km. in 30minutes
        {

            que.distance = 0;

        }
        mLastDTGtime = curtime;

        add_calqueue(que);

    }

    synchronized public void set_drivestate(boolean bflag) {

        mtasktimer = mtasktimer - 1; //20210702
        mnUseGPS = 0; //20201110
        mbDrivestart = bflag;

        if (bflag == true) {
            update_BLEmeterstate("20");
            update_BLEmeterfare(CDrive_val.mFareT, 0, CalFareBase.BASEDRVDIST); //20201215 //CalFareBase.BASECOST, 0, CalFareBase.BASEDRVDIST);

            if (AMBlestruct.mb1sdata13code == false) //20201215
                mCallback.serviceMeterState(AMBlestruct.MeterState.BLELEDOFF, 0);


//20201215 1초데이타            set_meterhandler.sendEmptyMessage(11);

            /////////1초data수신 확인.
//            writeBLE("11"); //20201110 ???

//            AMBlestruct.mSState = "20";
//            writeBLE("15");

//////////////
//            AMBlestruct.AMFare.mCurtaxifare = CalFareBase.BASECOST;
//            AMBlestruct.AMFare.mCurtaxifareDis = 0;
//            AMBlestruct.AMFare.mCurdistanceR = CalFareBase.BASEDRVDIST;
//            writeBLE("31");

        } else
            update_BLEmeterstate("05");

    }

    ////////////////////20201112
    public void drive_endtime() {

        CDrive_val.mEndtime = AMBlestruct.getCurDateString();

    }

    ////////////////////
    public void drive_state(int nmode) {
        if (nmode == AMBlestruct.MeterState.POWERONDRIVE) { //20210325
            if (mbDrivestart == false) {
                mbNeedDTGFirst = true;
                mDrivemode = AMBlestruct.MeterState.DRIVE;
                mCardmode = AMBlestruct.MeterState.NONE;

                set_drivestate(true);

                _setLbsmsg_img(2);

                mLastDTGform.init();
                mFirstDTGform.init();

//                String[] param = {"20", "0", "0", AMBlestruct.AMCardFare.mFare + "", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
///                Send_DTGData(3, param);

                if (Info.REPORTREADY) {

                    Info._displayLOG(true, "운행상태 주행", "");

                }

            }
            return;
        } else if (nmode == AMBlestruct.MeterState.DRIVE) {
            if (mbDrivestart == false) {
                mDrivemode = AMBlestruct.MeterState.DRIVE;
                mCardmode = AMBlestruct.MeterState.NONE;

                mLastDTGform.init(); //20211220
                mFirstDTGform.init(); //20211220

                CDrive_val.init(); //20201215

                CDrive_val.setmFareAdd(0); //20210823
                CDrive_val.setmFareDiscount(0); //20210823
                CDrive_val.setmFareCallPay(Info.CALL_PAY); //20210917

//20211012
                mbSuburb = true; //시외시작승차는 시계적용안함

                set_drivestate(true);

                _setLbsmsg_img(2);

//                update_BLEmeterstate("20");

                mCallback.serviceDisplayState(CDrive_val.mFareT, CalFareBase.BASEDRVDIST, CDrive_val.mFareDiscount, 0,
                        0, 2, mLastLocation, 0, 0, true);

//20211220                mLastDTGform.init();
//20211220                mFirstDTGform.init();

                //todo: 20211214
//                String[] param = {"20", "0", "0", AMBlestruct.AMCardFare.mFare + "", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
                String[] param = {"20", "0", "0", "0" + "", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
                Send_DTGData(3, param);

                if (Info.REPORTREADY) {

                    Info._displayLOG(true, "운행상태 주행", "");

                }

            }
            return;
        } else if (nmode == AMBlestruct.MeterState.EMPTY) {
//            if(mDrivemode != nmode)
            {
                mDrivemode = AMBlestruct.MeterState.EMPTY;
                mCardmode = AMBlestruct.MeterState.NONE;
                set_drivestate(false);
                CEmpty_val.init();

                _setLbsmsg_img(1);

                String[] param = {"05", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
                Send_DTGData(3, param);

                if (Info.REPORTREADY) {

                    Info._displayLOG(true, "운행상태 빈차", "");

                }

                if (mbNeedDTGFirst == true) //20211220
                    mbNeedDTGFirst = false;

            }

            return;

        } else if (nmode == AMBlestruct.MeterState.EMPTYPAY) //20201112
        {

            mCardmode = AMBlestruct.MeterState.EMPTYPAY;

            CDrive_val.init();

            CDrive_val.setmFareDiscount(0); //20210823
            CDrive_val.setmFareAdd(0); //20210823
            CDrive_val.setmFareCallPay(0); //20210909

            mCallback.serviceDisplayState(CDrive_val.mFareT, CalFareBase.BASEDRVDIST, CDrive_val.mFareDiscount, 0,
                    0, 2, mLastLocation, 0, 0, true);

            AMBlestruct.AMCardFare.mFare = Info.PAYMENT_COST; //20210607 CDrive_val.mFareT; //요금.
            AMBlestruct.AMCardFare.mFareDis = CDrive_val.mFareDiscount; //할인금액.
            AMBlestruct.AMCardFare.mCallCharge = 0; //호출요금.
            AMBlestruct.AMCardFare.mAddCharge = 0; //추가요금.
            AMBlestruct.AMCardFare.mMoveDistance = 0; //승차거리
            AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode;
            AMBlestruct.AMCardFare.mbCard = true;
            AMBlestruct.AMCardFare.mstype = "01";
            AMBlestruct.AMCardFare.mStarttime = CDrive_val.mStarttime;
            AMBlestruct.AMCardFare.mEndtime = CDrive_val.mEndtime;

            writeBLE("21");

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "운행상태 수기 결제", "");

            }

            return;

        } else if (nmode == AMBlestruct.MeterState.ADDPAY) {

            mCardmode = AMBlestruct.MeterState.ADDPAY;
//20210409            CDrive_val.init();

//20210409            mCallback.serviceDisplayState(AMBlestruct.AMCardFare.mFare, 0, AMBlestruct.AMCardFare.mFareDis, 0,
///                    0, 2, mLastLocation, 0,false);

            writeBLE("21");

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "지불 추가요금 " + AMBlestruct.AMCardFare.mFareDis, "");

            }

            return;

        } else if (nmode == AMBlestruct.MeterState.MANUALPAY) {

            mCardmode = AMBlestruct.MeterState.MANUALPAY;
            CDrive_val.init();

//20210409            mCallback.serviceDisplayState(AMBlestruct.AMCardFare.mFare, 0, AMBlestruct.AMCardFare.mFareDis, 0,
///                    0, 2, mLastLocation, 0,true);

            writeBLE("21");

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "운행상태 수기 결제", "");

            }

            return;

        } else if (nmode == AMBlestruct.MeterState.CANCELPAY) {

            mCardmode = AMBlestruct.MeterState.CANCELPAY;

            writeBLE("23");

            return;

        } else if (nmode == AMBlestruct.MeterState.PAY) {

            if (Info.REPORTREADY) {

                Info._displayLOG(true, "운행상태 지불", "");
                Info._displayLOG(true, "결제기요금전송 " + CDrive_val.mFareT + "원", "");
            }

            mCardmode = AMBlestruct.MeterState.PAY;

            AMBlestruct.AMCardFare.mFare = Info.PAYMENT_COST; //20210607 CDrive_val.mFareT; //요금.
            AMBlestruct.AMCardFare.mFareDis = CDrive_val.mFareDiscount; //할인금액.
            AMBlestruct.AMCardFare.mCallCharge = CDrive_val.mCallPay; //20210909호출요금.
            AMBlestruct.AMCardFare.mAddCharge = CDrive_val.mFareAdd; //20210823추가요금.
            AMBlestruct.AMCardFare.mMoveDistance = Info.MOVEDIST; //승차거리
            AMBlestruct.AMCardFare.msOpercode = Info.g_nowKeyCode;
            AMBlestruct.AMCardFare.mbCard = true;
            AMBlestruct.AMCardFare.mstype = "01";
            AMBlestruct.AMCardFare.mStarttime = CDrive_val.mStarttime;
            AMBlestruct.AMCardFare.mEndtime = CDrive_val.mEndtime;

            writeBLE("21");

        } else if (nmode == AMBlestruct.MeterState.EXTRACOMPLEX) {
            CDrive_val.mbExtraComplex = true;
            String[] param = {"34", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
            Send_DTGData(3, param);
        } else if (nmode == AMBlestruct.MeterState.EXTRACOMPLEXOFF) {
            CDrive_val.mbExtraComplex = false;
            String[] param = {"35", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
            Send_DTGData(3, param);
        } else if (nmode == AMBlestruct.MeterState.EXTRASUBURB) {
            CDrive_val.mbExtraSuburb = true;
        } else if (nmode == AMBlestruct.MeterState.EXTRASUBURBOFF) {
            CDrive_val.mbExtraSuburb = false;
        } else if (nmode == 777) { //todo: 20210831 1758
            writeBLE("777");
        }


    }

    /////////////////////////////
//gps provider

    @Override
    public void onLocationChanged(Location location) {

        if (mLastLocation != null) {
//            double getSpeed = (Double.parseDouble(String.format(Locale.getDefault(), "%.3f", location.getSpeed())));  // m/s
//          Intent intent = new Intent("CURRENT_SPEED");
//          intent.putExtra("curSpeed",getSpeed);
//          sendBroadcast(intent);

            get_gps(location);

//            Log.d("CURRENT_SPEED", String.valueOf(getSpeed));

        } else //20210701
            mLastLocation = location;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Intent intent = new Intent("GPS_STATUS");
        switch (status) {
            case LocationProvider.AVAILABLE:
                intent.putExtra("curStatus", true);
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                intent.putExtra("curStatus", false);
                break;
        }
        sendBroadcast(intent);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    ////////////////////////////////////
//20200711
    public boolean connectAM() {

        if (mBluetoothLE != null)
//            mBluetoothLE.connectBLE();
            mBluetoothLE.connectAM();

        setting.BLUETOOTH_FINDEND = true;

        return true;
    }

    //20210429
    public boolean disconnectAM() {

        if (mBluetoothLE != null)
//            mBluetoothLE.connectBLE();
            mBluetoothLE.disconnectAM();

        return true;
    }

    ///////////////////
    public void int_aboutDTG() {

//        mLastDTGform = null;
        mLastDTGtime = 0;
    }

    //////////////////////////
    //요금 변경 전송
    public boolean update_BLEmeterfare(int nfare, int nfaredis, int ndistanceremain) {
        if (mBluetoothLE != null)
//            mBluetoothLE.update_AMmeterfare(nfare, nfaredis, ndistanceremain);
            mBluetoothLE.update_AMmeterfare(nfare, nfare, ndistanceremain);

        return true;
    }

    //빈차승차
    public boolean update_BLEmeterstate(String sstate) {

        if (sstate.equals("40")) {
            _setLbsmsg_img(3);
        } else if (sstate.equals("41")) {
            _setLbsmsg_img(1);
            sstate = "05"; //20220105 41 to 05

        }

        mBluetoothLE.update_AMmeterstate(sstate);
        return true;
    }

    //운행정보전달
    public boolean send_BLEpaymenttype(String opercode, String stype) {
        mBluetoothLE.send_Paymenttype(opercode, stype);
        writeBLE("20");
        return true;
    }

    public boolean send_CashReceiptType(String opercode, String stype, String telnum) {
        mBluetoothLE.send_CashPayment(opercode, stype, telnum);
        writeBLE("20");
        return true;
    }

    public boolean writeBLE(String curcode) {

        if (mBluetoothLE != null) {
            mBluetoothLE.makepacketsend(curcode);
        }


        return true;

    }

    //2021012
    public void setTimsUnitidx(int iunitidx) {

        CDrive_val.TIMSIDX = iunitidx;

    }

    public int getTimsUnitidx() {

        return CDrive_val.TIMSIDX;

    }
///////////////

    //20210823
    private void show_mainActivity() {
        //Intent sendIntent = new Intent("com.enpsystem.drv.texi.MAINS");
        //Intent sendIntent = new Intent(getApplicationContext(), PaymentActivity.class);
        Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(sendIntent);

							/*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
								//Intent sendIntent = new Intent("com.enpsystem.drv.texi.MAINS");
								Intent sendIntent = new Intent(getApplicationContext(), pos_app_mainActivity.class);
                                sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(sendIntent);

							} else {
								Intent sendintent = new Intent("com.enpsystem.intent.action.LBSMSG");
								sendBroadcast(sendintent);
							}*/
    }

/////////////20210310 start
// BLE 스캔시작

    public void setBleScan() {

        mDrvnum = setting.AM100 + AMBlestruct.AMLicense.taxinumber.substring(AMBlestruct.AMLicense.taxinumber.length() - 4);

        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (setting.BLUETOOTH_DEVICE_ADDRESS.equals("") == false && setting.BLUETOOTH_CARNO.equals(mDrvnum)) {

                setting.BLUETOOTH_FINDEND = true;
                scanLeDevice(true);
            } else
                scanLeDevice(true);

            Info._displayLOG(true, "bluetoothManager.getAdapter() ok", "");
        }

//        startScanBLE();

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // SCAN_PERIOD 값만큼 시간이 지나면 스캐닝 중지
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    stopScanBLE();


                }
            }, setting.SCAN_PERIOD);

            startScanBLE();
        } else {

            stopScanBLE();
        }

    }

    private void startScanBLE() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

    }

    // BLE 스캔중지
    private void stopScanBLE() {

        Log.d(TAG, "---------stopScanBLE");

        mHandler.removeCallbacksAndMessages(null); //20210329

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            try {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                final String deviceName = device.getName();
                if (deviceName != null && deviceName.equals("") == false) {

                    {
                        if (deviceName.contains(mDrvnum)) {

                            setting.BLUETOOTH_DEVICE_ADDRESS = device.getAddress();
                            setting.BLUETOOTH_DEVICE_NAME = device.getName();
                            setting.BLUETOOTH_CARNO = mDrvnum;
                            save_bleinfo();
                            stopScanBLE(); //20210126
                            Log.d(TAG, "---------found" + deviceName);
                        }
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();

            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.e("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        final String deviceName = device.getName();
                        if(deviceName != null && deviceName.equals("") == false) {
                            if (deviceName.contains(mDrvnum)) {

                                setting.BLUETOOTH_DEVICE_ADDRESS = device.getAddress();
                                setting.BLUETOOTH_DEVICE_NAME = device.getName();
                                setting.BLUETOOTH_CARNO = mDrvnum;
                                save_bleinfo();
                                stopScanBLE(); //20210126
                                Log.d(TAG, "---------found" + deviceName);
                            }
                        }
                    }
                    catch (Exception e)
                    {

                        e.printStackTrace();

                    }
                }
            };

    public void save_bleinfo()
    {
        SharedPreferences pref = getSharedPreferences("BLEINFO",
                Activity.MODE_PRIVATE);
        // UI 상태를 저장합니다.

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("addr", setting.BLUETOOTH_DEVICE_ADDRESS);
        editor.putString("name", setting.BLUETOOTH_DEVICE_NAME);
        editor.putString("car_", setting.BLUETOOTH_CARNO);
        editor.commit();

        setting.BLUETOOTH_FINDEND = true;
    }

    //20210827
    private void send_driverinfo()
    {

        writeBLE("48");

    }

    //20220120
    public void set_payviewstate(boolean bFlag)
    {

        mbPayView = bFlag;

    }

    //20210701 haversign 거리구하기
    public static double distanceByHaversine(double y1, double x1, double y2, double x2) {
        double distance;
        double radius = 6371; // 지구 반지름(km)
        double toRadian = Math.PI / 180;

        double deltaLatitude = Math.abs(x1 - x2) * toRadian;
        double deltaLongitude = Math.abs(y1 - y2) * toRadian;

        double sinDeltaLat = Math.sin(deltaLatitude / 2);
        double sinDeltaLng = Math.sin(deltaLongitude / 2);
        double squareRoot = Math.sqrt(
                sinDeltaLat * sinDeltaLat +
                        Math.cos(x1 * toRadian) * Math.cos(x2 * toRadian) * sinDeltaLng * sinDeltaLng);

        distance = 2 * radius * Math.asin(squareRoot);

        return distance * 1000;
    }

    public double CalculationByDistance(double initialLong, double initialLat,
                                        double finalLong, double finalLat){
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat-initialLat);
        double dLon = toRadians(finalLong-initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (R * c) * 1000.0;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI/180);
    }

    public double GetDistWGS84(Location from, Location to)
    {

//        return CalculationByDistance(
        return distanceByHaversine(
                Double.parseDouble(String.format("%.8f", from.getLongitude())),
                Double.parseDouble(String.format("%.8f", from.getLatitude())),
                Double.parseDouble(String.format("%.8f", to.getLongitude())),
                Double.parseDouble(String.format("%.8f", to.getLatitude()))
        );


//                (from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());

    }

    public double GetDistWGS84tmp(Location from, Location to)
    {
        double x1;
        double x2;
        double y1;
        double y2;
        x1 = Double.parseDouble(String.format("%.8f", from.getLongitude())) ;
        y1 = Double.parseDouble(String.format("%.8f", from.getLatitude())) ;
        x2 = Double.parseDouble(String.format("%.8f", to.getLongitude())) ;
        y2 = Double.parseDouble(String.format("%.8f", to.getLatitude())) ;

        return distanceByHaversine(x1, y1, x2, y2);

    }

/////////////////end

}
