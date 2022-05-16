package com.konai.appmeter.driver.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.view.MainActivity;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.struct.AMBlestruct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Info {
    //20201211
    public static boolean TESTMODE = false; //true; //false;
    public final static String TIMSKEY = "f4bbc1d0b067002e527e535338668b29164404fd18a4e5c331c70fcb9b07fd62";
    public static boolean TIMSUSE = false; //true; //false; //true; //send tims.
    public static boolean TIMSUSE_TEST = false; //send tims.
    public static boolean SENDDTG = false; //send DTG
    public static boolean USEDRIVESTATEPOWEROFF = false; //true; //false; //20210407 승차상태의 강제poweroff
    public static boolean USEDBRUNDATA = true; //20220415
    public static boolean USEDBLOCATIONDATA = false; //20220415
    public static String G_driver_name = "";
    public static String G_driver_num = "0000"; //사원번호.
    public static String G_license_num = ""; //자격번호.

//20210318
    public static final boolean REPORTREADY = false; //false;
    private static String TAG = "driver: ";
    public static final boolean LOGDISPLAY = false; //20220209

/*
    public static final boolean TESTMODE = true; //true; //true; //false;
    public static final boolean TIMSUSE = true; //true; //send tims.
    public static final boolean SENDDTG = false; //send DTG
    public static final boolean USEDRIVESTATEPOWEROFF = false; //false; //20210407 승차상태의 강제poweroff


 */

//20210402
    public static boolean TIMSCARNUMOK = false;
    public static boolean TIMSDRIVEROK = false;

//20210330
    public static int APPMETERRUNSTOP = 1; //앱시작영업시작영업종료

    public static final int APP_METER = 1; //앱미터
    public static final int APP_PAYMENT = 2; //지불지원용
    public static int g_appmode = APP_METER;

    public static String AREA_CODE = ""; //20211029
    public static double APP_VERSION = 0.0;
    public static double SV_APP_VERSION = 0.0;
    public static double SV_APP_CVERSION = 0.0; //20220506 챠량별앱버전
    public static double APP_SUBURBSVER = 0.1; //20220419
    public static double SV_SUBURBSVER = 0.1; //20220419

    public static LocService m_Service = null;

    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;

    public static boolean bOverlaymode = true; //사용유무.
    public static boolean bOverlayshow = false; //현재상태.
    //public static int nLbsToShow = -1; // lbs -1 : no, 0 ready, 1 show
    public static boolean bException = false;

    public static Intent g_MainIntent = null;
    public static MainActivity gMainactivity; //20210325

//disable font.
    public static final int g_Btncolordis = Color.rgb(0xBD, 0xBD, 0xBD);
    public static final int g_Btncolorena = Color.rgb(0x82, 0x03, 0x16);
    public static final int g_txtmenu = Color.rgb(0x0a, 0xba, 0xff);
//base font.
    public static final int g_cancel = Color.rgb(0xff, 0xff, 0xff);
    public static final int g_cancel_p = Color.rgb(0xf4, 0x2d, 0x49);
    public static final int g_backspace = Color.rgb(0xff, 0xff, 0xff);
    public static final int g_backspace_p = Color.rgb(0xff, 0xac, 0x2a);
    public static final int g_number = Color.rgb(0x53, 0x53, 0x56);
    public static final int g_number_p = Color.rgb(0xff, 0xff, 0xff);
    public static final int g_menu = Color.rgb(0x36, 0x2d, 0xa7);
    public static final int g_menu_p = Color.rgb(0xff, 0xff, 0xff);
    public static final int g_add = Color.rgb(0xff, 0xff, 0xff);
    public static final int g_add_p = Color.rgb(0x36, 0x2d, 0xa7);

///////////////////
    public static int g_Runmode = AMBlestruct.MeterState.EMPTY;
///////////////////
    public static Location mlocation = null;

///////////////////
    public static SQLiteHelper mSQLiteHelper;
    public static SQLiteControl sqlite;
    public static String g_nowKeyCode = "00000000"; //20220411
    public static String g_lastKeyCode = "00000000"; //20220411
    public static String g_cashKeyCode = "00000000"; //20220411 tra..sh
    public static boolean g_state_done = false; //20210512 get_state_pref read done?

//20210607
    public static int PAYMENT_COST = 0;
    public static int MOVEDIST = 0;
    public static int ADDFARE = 0; //20210823
//20210909
    public static int CALL_PAY = 0;
//20210330
    public static int gTimsDayIdx = 0;
    public static int gTimsDayIdxtmp = 0;
    public static int gTimsDayEventIdx = 0;
    public static int gTimsDayPowerIdx = 0;
    public static boolean gTimsSendDone = true;
    public static String gTimsFile = "";
    public static String gTimsLastDate = "";

    public static boolean mBTFirstOK = false;
    public static int mBTOnOffCheck = 0; //20210329 for bluetooth restarting > (10초)
    public static boolean bBTRestarting = true;

//20220413
    public static boolean mAM100FirstOK = false;
//2021019
    public static String mAuthVehTIMS = "";
    public static String mAuthdrvTIMS = "";
    public static String mEventTIMSdate;
    public static String mEventTIMStype;
    public static String mEventTIMSok;
    public static String mDriveTIMSdate;
    public static String mDriveTIMSok;
    public static boolean mPowerTIMSok;

//20210520
    private static SimpleDateFormat mtransFormat = new SimpleDateFormat("yyyyMMddHHmmss");

//20211229
    public final static int CALDISTANCEMAX = 10000;

//20210318

    public static void _displayLOG(boolean bflag, String p1, String p2)
    {

        if(bflag) //20220502추가
            Log.d(TAG, p2 + p1);

    }
//////////////

    public static void set_MainIntent(Context main, Class cWhich)
    {

        g_MainIntent = new Intent(main,
                cWhich);
    }

    public static void do_onPause()
    {

        bOverlayshow = false;

    }

    public static void do_onResume()
    {

        bOverlayshow = true;

    }

///////////////////
    public static void init_SQLHelper(Context context)
    {

        mSQLiteHelper = new SQLiteHelper(context);
        sqlite = new SQLiteControl(mSQLiteHelper);

//20220411 tra..sh        makeDriveCode();

    }

    public static void makeDriveCode() {
        String lKeyCode;
        String skeytmp = g_nowKeyCode; //20220411 tra..sh sqlite.selectKey();
        g_lastKeyCode = g_nowKeyCode;

        if(skeytmp == null || skeytmp == "null" || skeytmp == "") {
            lKeyCode = "00000001";
        } else {
            lKeyCode = (Integer.parseInt(skeytmp) + 1) + "";
        }

        String keyCode = "";
        if(lKeyCode.length() < 8) {
            for(int i=0; i<8-lKeyCode.length(); i++) {
                keyCode += "0";
            }

            keyCode += lKeyCode;
        } else {
            keyCode = lKeyCode;
        }

        g_nowKeyCode = keyCode;

        if(g_lastKeyCode.equals(""))
        {

            g_lastKeyCode = g_nowKeyCode;

        }

    }

    public static void insert_totaldata()
    {
        String totData = sqlite.getTotalKey();
        //Log.e("total Date is", totData);
        if (totData.equals("") || totData.equals("0")) {
            sqlite.insertTotalData();
        }
    }

    public static void insert_rundata(Location location, int runmode)
    {

        if (location != null) {


            sqlite.insert(Info.g_nowKeyCode, 0, 0, 0, 0, location.getLatitude() + "", location.getLongitude() + "", runmode);

        } else
            sqlite.insert(Info.g_nowKeyCode, 0, 0, 0, 0, "", "", runmode);

        Log.d("check_runmode", runmode+" " + sqlite._getLastDriveDate(Info.g_nowKeyCode));

    }


    public static void insert_cancel_data(Location location, int runmode, String drvCode){

        if (location != null){
            sqlite.insert( drvCode, 0,0,0,0,location.getLatitude()+"", location.getLongitude()+"", runmode);
        }else {
            sqlite.insert( drvCode, 0,0,0,0,"", "", runmode );
        }

    }

    public static void update_runlocationdata(Location location)
    {

        sqlite.setUpdateLocation(g_nowKeyCode, 0, 0, 0, location.getLatitude() + "", location.getLongitude() + "", 0, 0, 0);

    }

    public static void end_rundata(Location location, int Payment, int PayDiv, int PayAdd, int distance, int seconds ) {

        Log.e("check_runmode", "end_rundata");


        if (location != null) {

            sqlite.setUpdateLocation(g_nowKeyCode, Payment, PayDiv, PayAdd, location.getLatitude() + "", location.getLongitude() + "", distance, seconds, 99);
            sqlite.setUpdateTotalData(1, Payment + PayAdd, distance, seconds, 0);

        } else {
            sqlite.setUpdateLocation(g_nowKeyCode, Payment, PayDiv, PayAdd, "", "", distance, seconds, 99);
            sqlite.setUpdateTotalData(1, Payment + PayAdd, distance, seconds,0);
        }

//20210611
        gMainactivity.save_totalfare_pref(Payment + PayAdd, 0, distance);
    }

    public static void end_run_cancel_data(Location location, int Payment, int PayDiv, int PayAdd, int distance, int seconds ) {

        Log.e("end_rundata", "end_rundata");


        if (location != null) {

            sqlite.setUpdateLocation(g_nowKeyCode, Payment, PayDiv, PayAdd, location.getLatitude() + "", location.getLongitude() + "", distance, seconds, 99);
            sqlite.setUpdateTotalData(1, Payment + PayAdd, distance, seconds, 0);

        } else {
            sqlite.setUpdateLocation(g_nowKeyCode, Payment, PayDiv, PayAdd, "", "", distance, seconds, 99);
            sqlite.setUpdateTotalData(1, Payment + PayAdd, distance, seconds,0);
        }

        String tot_str = (Payment + PayAdd)+"";
        if (!tot_str.contains("-")){
            //no nothing
        }else {
            gMainactivity.minus_totalfare_pref(Payment + PayAdd, 0, distance);
        }


    }

    public static String getCurHourString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "HH");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    public static String getCurDateString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    public static long getStringTime(String _date) {

        try {
            // 현재 yyyymmdd로된 날짜 형식으로 java.util.Date객체를 만든다.
            return mtransFormat.parse(_date).getTime();

        } catch (ParseException e) {
            e.printStackTrace();

            return System.currentTimeMillis();
        }

    }

//20210325
    public static MainActivity getMainActivity() {//메인액티비티 값

        return gMainactivity;

    }

//20210325
    public static void setMainActivity(MainActivity mainActivity) {

        gMainactivity = mainActivity;

    }

//20210512
    public static void Savedata(String sfile, String sdata, String path ) {

        //Location Detail Log
    //        Log.d("File1", "saving Savedata" + sfile + " : " + sdata + " / spd : " + ((int)(mddspeed * 3.6) + ""));

        File saveFile = null;
//20220503 tra..sh        if( Build.VERSION.SDK_INT < 29) saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
///        else saveFile = gMainactivity.getExternalFilesDir("/" + path);

        saveFile = gMainactivity.getExternalFilesDir("/" + path);

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile + "/" + sfile, true));
            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS 영업정보저장", saveFile + " " + Build.VERSION.SDK_INT);
            buf.append(sdata);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS 영업정보저장실패", e.toString() + " " + Build.VERSION.SDK_INT);
        }

    }
//////////////

    public static String ReadTextFile(String sfile, String path) {

        File saveFile = null;
//20220503 tra..sh        if (Build.VERSION.SDK_INT < 29)
//            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
//        else saveFile = gMainactivity.getExternalFilesDir("/" + path);

        saveFile = gMainactivity.getExternalFilesDir("/" + path);

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
            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS 영업정보읽기", e.toString() + " " + Build.VERSION.SDK_INT);
            return "noFile";
        }
        return strBuffer.toString();
    }

//20210803
    public static void Deletefile(String sfile, String path ) {

        File saveFile = null;
//20220503 tra..sh        if( Build.VERSION.SDK_INT < 29)
//            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
//        else saveFile = gMainactivity.getExternalFilesDir("/" + path);

        saveFile = gMainactivity.getExternalFilesDir("/" + path);

        try {

            File file = new File(saveFile + "/" + sfile);
            if(file.exists())
                file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

/////20210923
    public static boolean searchAppPackage(Context context, String packageName){
        boolean bExist = false;

        // 패키지 정보 리스트 추출
        PackageManager pkgMgr = context.getPackageManager();
        List<ResolveInfo> mAppList;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mAppList = pkgMgr.queryIntentActivities(mainIntent, 0);

        // 패키지 리스트 순회하면서 특정 패키지명 검색
        try{
            for(int i=0;i<mAppList.size();i++){
                if(mAppList.get(i).activityInfo.packageName.startsWith(packageName)){
                    bExist = true;
                    break;
                }
            }
        }catch(Exception e){
            bExist = false;
        }
        return bExist;
    }

//////////////

}
