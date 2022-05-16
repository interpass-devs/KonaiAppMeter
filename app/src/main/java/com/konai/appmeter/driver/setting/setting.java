package com.konai.appmeter.driver.setting;

import android.content.pm.ActivityInfo;

import java.util.UUID;

public class setting {

    public static int gOrient =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; //for smartphone
//    public static int gOrient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; //for navi.
    public static boolean gUseBLE = true; //false;
    public static Integer gSerialUnit = 0;  // 0=default, 1=아이나비, 2=아트뷰, 3=아틀란
    public static Integer gGubun = 0;  //0=default, 1=개인, 2=법인
    public static boolean gAppCotrol = true;  //앱 자동실행
    public static Integer gAutoLogin = 0;  //0=default, 1=자동로그인(O), 2=자동로그인(X)
    public static Integer gModem = 0; //0=default, 1=일반, 2=우리넷, 3=에이엠
    public static boolean gUserAction = false;
    public static boolean editTouch = false;

    public static boolean bluetoothStatus = false;

    public static float gTextDenst = 0;
    public static float gTextDenst2 = 0;

    public static String phoneNumber = "0";

    public String sDriverCode;
    public String sCarNumber;
    public String sDriverID;

    //Taxi 기본요금
    private int BASEPAY = 2800;

    private int STD_SPEED = 10000;

    //시간별 요금
    private int INTERVAL_SEC = 30;
    private int TIMEPAY = 100;

    //거리별 요금
    private int INTERVAL_DIST = 140;
    private int DISTPAY = 100;

    public int VERSION_CODE = 0;
    public int SERVER_VERSION_CODE = 0;

    public static String BASEDOMAIN = "https://acc.psweb.kr/drvlogs/";
    public static String FILESERVERDOMAIN = "https://postaxis.psweb.kr/";
    public static String FILESERVERAPK= FILESERVERDOMAIN + "posapk/";
    public static String FILESERVERSUBURB = FILESERVERDOMAIN + "resources/pos_web/suburbs/";

    public static String UPFILENAME = "appmeter.apk"; //"appmeter.apk";
    public final static String BROADCAST_TMSG = "com.konai.appmeter.driver.TMSG";
    public final static String BROADCAST_FMSG = "com.konai.appmeter.driver.FMSG";
    public static final String APPMETER_FMRUN = "com.konai.appmeter.driver.FMRUN";
    public final static int BROADCAST_STATE = 5001;
    public final static int BROADCAST_SHOWAPP = 5002;

    /////////////////////////
    //bluetooth 20200711
    public static boolean BLESCANNING_MODE = false; //20220407
    public static final long SCAN_PERIOD = 15000;
    public static final String AM100 = "AM100";
    public static boolean BLUETOOTH_FINDEND = false; //20210520
    public static String BLUETOOTH_CARNO = ""; //20210520
    public static String BLUETOOTH_DEVICE_ADDRESS = "";
    public static String BLUETOOTH_DEVICE_NAME = "";
    public static String BLUETOOTH_CONNECTED_HISTORY = "";	// 마지막으로 연결 성공 했던 블루투스 장치의 MAC ADDRESS
    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_TRANJACTION =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
//			UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");

    public final static UUID UUID_DESCRIPTION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CONFIGURE = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    /////////////////////////

    public void setTextDest(float density)
    {

        gTextDenst = density * 10;
        gTextDenst2 = density * 1;

    }

}
