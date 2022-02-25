package com.konai.appmeter.driver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.AMBleConfigActivity;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PaymentActivity extends Activity {

    com.konai.appmeter.driver.setting.setting setting = new setting();

    private static String TAG = "PaymentActivity class";
    //String TAG = "PaymentActivity class";
    public static LocService m_Service = null;
    int lbs_x = -1;
    int lbs_y = -1;

    SQLiteHelper helper;
    SQLiteControl sqlite;

    private TimerTask secondtask;
    private int mseconds = 0;

    Location mlocation = null;
    private boolean m_bDBstart = false;

    private TextView tvMainPay;
    private TextView tvAddPay;
    private TextView tvtextAdd;
    private TextView tvResPay;
    private TextView tvcurtime;
    private TextView tvetdist;

/////////
    private int ResPays = 0;
    private int MainPays = 0;
    private int AddPays = 0;
    private int mEditAddPay = 0; //for 화면전환값보관

    private Button nBtn0;
    private Button nBtn1;
    private Button nBtn2;
    private Button nBtn3;
    private Button nBtn4;
    private Button nBtn5;
    private Button nBtn6;
    private Button nBtn7;
    private Button nBtn8;
    private Button nBtn9;

    private Button bCancel;
    private Button bAdd;
    private Button bPayment;
    private Button bBackspace;
    private Button bReceipt;
    private Button bMenu;
    private Button bmobilepay;

    private boolean payComp = false;
    private boolean m_bAddpay = false;

    private LinearLayout lineartopfare; //시간 거리.
    private LinearLayout m_layoutPay;
    private LinearLayout m_layoutAdd;

    private FrameLayout paymentframe1 = null;
    private FrameLayout paymentframe2 = null;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("pos_app_mainActivity", "pos_app_service_main onServiceConnected");

            LocService.ServiceBinder binder = (LocService.ServiceBinder)iBinder;
            m_Service = binder.getService();
            m_Service.registerCallback(mCallback); //

            Info.m_Service = m_Service;

            //20180817
            m_Service.lbs_initx = lbs_x;
            m_Service.lbs_inity = lbs_y;
            if (Info.bOverlaymode)
                m_Service._overlaycarstate();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("pos_app_mainActivity","pos_app_service_main onServiceDisconnected");
            m_Service = null;
//			isBindService = false;
///			mServiceMessenger = null;

            Info.m_Service = null;

        }
    };

    ////////////////////////////////
    private LocService.maincallback mCallback = new LocService.maincallback() {

        @Override
        public void serviceMessage(int ntype, String message) {
            // Todo: Activity에서 처리합니다.
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void serviceDisplayState(int nfare, int nremaindist, int nfarediscount, double ddspeed, int ddistance, int nseconds, Location location,
                                        int nfaredist, int ncurdist, boolean setDB) {
            // Todo: Activity에서 처리합니다.

            mlocation = location;

        }

        @Override
        public void serviceEmptyState(double distance, int nseconds, double ddspeed, Location location) {
            // Todo: Activity에서 처리합니다.


        }

        @Override
        public void serviceMeterState(int nType, int mfare)
        {

            if(nType == AMBlestruct.MeterState.ENDPAYMENT)
            {

                endpayment(); //? by meter

                return;
            }

            if(nType == AMBlestruct.MeterState.PAY)
            {

                displayHandler.sendEmptyMessage( 1);
                ;

                Intent actIntent = new Intent(getApplicationContext(),
                        PaymentActivity.class);
                actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(actIntent);

            }
            else if(nType == AMBlestruct.MeterState.EMPTY)
            {

                displayHandler.sendEmptyMessage( 2);
                m_Service._setLbsmsg_img(1);

            }
            else if(nType == AMBlestruct.MeterState.DRIVE)
            {

                displayHandler.sendEmptyMessage( 3);
                m_Service._setLbsmsg_img(2);

            }
            else if(nType == AMBlestruct.MeterState.EXTRA)
            {
                displayHandler.sendEmptyMessage( 4);
                ;

            }

            Log.d(TAG, "serviceMeterState ");

        }

        @Override
        public void serviceLog(int nseconds, Location location, int ngpsdist, int ndtgdist, int ndtgtot, String altitude, boolean drvState,
                               int speed, double dtime, double dtfare, double ddist, double dcfare,
                               double dremain, int nafterfare, int nfare, boolean bextra, boolean bsuburb, double tfaredist) //tmp for log
        {



        }

        @Override
        public void serviceFarebyMeter(int nseconds, int ndist, int nfare, int naddfare)
        {

            tvMainPay.setText(nfare + "");
            tvtextAdd.setText(naddfare + "");
            tvResPay.setText(nfare  + naddfare + "");

            MainPays = nfare;
            AddPays = naddfare;
            ResPays = MainPays + AddPays;
        }

        @Override
        public void serviceLbsControllEvent(int nType, int nLastState) {

        }

        @Override
        public void serviceTIMSDataEvent(int idx, String date, Location location, double speed, String lnk, double dist, double remainsDist,
                                         int fare, int isOutGps, int addfare, double tdist, boolean isnight, boolean issuburb) {

        }

    };

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
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Info.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {

                Toast.makeText(this, getString(R.string.info_permission_overlay), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                _start_service();
            }
        }
    }

    public void _start_service()
    {

        Intent service = new Intent(getApplicationContext(), LocService.class);
        service.setPackage("com.enpsystem.gps_sample");

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
//			Intent intent = new Intent().setAction("com.enpsystem.navi.texi.services.pos_app_service_main");
///			bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE); //20180117
            Log.d("pos_app_mainActivity", "pos_app_service_main _start_service");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Info.g_appmode = Info.APP_PAYMENT;

        Info.set_MainIntent(this, PaymentActivity.class);

//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

        loadinit();

        Info.init_SQLHelper(this);

        db_insert_rundata(1); //test.

////////////////////////
        checkOverlayStartservice();

        TimerStart();
/////////////////////

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {

            createGpsDisabledAlert();

        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Do something
            initializecontents(Configuration.ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Do something
            initializecontents(Configuration.ORIENTATION_PORTRAIT);
        }

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();

/*
        if(isForeground(this) == false)
        {

            Log.d(TAG, "onStop() lbs hide");
            if(m_Service != null)
                m_Service._showhideLbsmsg(true);

        }

        Log.d(TAG, "onStop()");
*/

    }

    @Override
    protected void onUserLeaveHint() {

        //이벤트

        if(m_Service != null)
            m_Service._showhideLbsmsg(true);

        super.onUserLeaveHint();
    }


    @Override
    public void onResume() {
        super.onResume();

        if(m_Service != null)
            m_Service._showhideLbsmsg(false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (m_Service != null) {
            unbindService(mServiceConnection);
//			Log.d("pos_app_mainActivity", "pos_app_service_main unbindService");
//
        }
//
        m_Service = null;

        stopService(new Intent(getApplicationContext(),
                LocService.class));


    }

    public static boolean isForeground(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {

                    Log.d(TAG, "--" + activeProcess);

//                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
//                       return true;
//                    }
                }
            }
        }

        return false;

    }

    Handler displayHandler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {

            switch (msg.what)
            {

                case 1:
/////////////////////////////
                    MainPays = AMBlestruct.AMReceiveFare.mFare;
                    AddPays = AMBlestruct.AMReceiveFare.mCallcharge + AMBlestruct.AMReceiveFare.mEtccharge;
                    ResPays = MainPays + AddPays;

                    tvMainPay.setText(MainPays + "");
                    tvtextAdd.setText(AddPays + "");
                    tvResPay.setText(ResPays + "");

                    tvetdist.setText(String.format("주행거리: %.1fkm", AMBlestruct.AMReceiveFare.mBoarddist / 1000.0));
///////////////////////

                    try {
                        long fromtime = 0;
                        long totime = 0;
                        long caltime = 0;
                        // 현재 yyyymmdd로된 날짜 형식으로 java.util.Date객체를 만든다.
                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//                        AMBlestruct.AMReceiveFare.mStarttime = "20201012213000";
///                        AMBlestruct.AMReceiveFare.mEndtime = "20201012223101";
                        if(AMBlestruct.AMReceiveFare.mStarttime.equals("") == false && AMBlestruct.AMReceiveFare.mEndtime.equals("") == false) {
                            fromtime = transFormat.parse(AMBlestruct.AMReceiveFare.mStarttime).getTime() / 1000;
                            totime = transFormat.parse(AMBlestruct.AMReceiveFare.mEndtime).getTime() / 1000;

                            caltime = Math.abs(totime - fromtime);
                            fromtime = caltime / 3600;
                            totime = (caltime % 3600) / 60;
                            tvcurtime.setText(String.format("%02d:%02d", fromtime, totime));
                        }
                        else
                            tvcurtime.setText("00:00");

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Info.g_Runmode = AMBlestruct.MeterState.PAY;

                    break;

                case 2:
///////////////////
                    MainPays = 0;
                    AddPays = 0;
                    ResPays = MainPays + AddPays;

                    tvMainPay.setText(MainPays + "");
                    tvtextAdd.setText(AddPays + "");
                    tvResPay.setText(ResPays + "");

                    tvetdist.setText(String.format("주행거리: %.1fkm", 0.0));
//////////////////

                    db_end_rundata();

                    Info.g_Runmode = AMBlestruct.MeterState.EMPTY;

                    Info.makeDriveCode();

                    db_insert_rundata(1);

                    break;

                case 3:

                    Info.g_Runmode = AMBlestruct.MeterState.DRIVE;

                    Info.makeDriveCode();

                    db_insert_rundata(2);

                    break;

                case 4:

                    break;

                case 98: //location update
                    db_update_runlocationdata();
                    break;

                case 99: //date display

                    if(Info.g_Runmode == AMBlestruct.MeterState.PAY)
                    {

                        return;

                    }

                    SimpleDateFormat format1 = new SimpleDateFormat ( "HH:mm");

                    Calendar time = Calendar.getInstance();

                    format1.format(time.getTime());

                    tvcurtime.setText(format1.format(time.getTime()));

                    db_update_runlocationdata();

                    break;
            }
        }

    };

    public void TimerStart() {

        secondtask = new TimerTask() {
            @Override
            public void run() {

                mseconds = (mseconds + 1) % 30;
                displayHandler.sendEmptyMessage(99);

            }
        };
        Timer timer = new Timer();
        timer.schedule(secondtask, 0, 1000);
    }

    private void initializecontents(int nTP)
    {

        if(nTP == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_payment_v);
            set_frame_orient(0);

        }
        else
        {

            setContentView(R.layout.activity_payment_h);
            set_frame_orient(0);
        }

        //운행요금 설정
        tvMainPay.setText(MainPays + "");
        tvtextAdd.setText(AddPays + "");
        tvResPay.setText(MainPays + AddPays + "");
        //처음설정 운행요금

        View.OnTouchListener touch1 = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent arg1)
            {

                if(m_bAddpay == false) {

                    ;
                }
                else { //if(m_bAddpay == false)
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

                        } else if (view == nBtn4) {

                            nBtn4.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn4.setTextColor(Info.g_number_p);

                        } else if (view == nBtn5) {

                            nBtn5.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn5.setTextColor(Info.g_number_p);

                        } else if (view == nBtn6) {

                            nBtn6.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn6.setTextColor(Info.g_number_p);

                        } else if (view == nBtn7) {

                            nBtn7.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn7.setTextColor(Info.g_number_p);

                        } else if (view == nBtn8) {

                            nBtn8.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn8.setTextColor(Info.g_number_p);

                        } else if (view == nBtn9) {

                            nBtn9.setBackgroundResource(R.drawable.payment_num_p);
                            nBtn9.setTextColor(Info.g_number_p);

                        }

                    }
///////////////////////////////
                    else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        if (view == nBtn0) {

                            nBtn0.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn0.setTextColor(Info.g_number);
                        } else if (view == nBtn1) {

                            nBtn1.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn1.setTextColor(Info.g_number);
                        } else if (view == nBtn2) {

                            nBtn2.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn2.setTextColor(Info.g_number);
                        } else if (view == nBtn3) {

                            nBtn3.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn3.setTextColor(Info.g_number);
                        } else if (view == nBtn4) {

                            nBtn4.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn4.setTextColor(Info.g_number);
                        } else if (view == nBtn5) {

                            nBtn5.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn5.setTextColor(Info.g_number);
                        } else if (view == nBtn6) {

                            nBtn6.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn6.setTextColor(Info.g_number);
                        } else if (view == nBtn7) {

                            nBtn7.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn7.setTextColor(Info.g_number);
                        } else if (view == nBtn8) {

                            nBtn8.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn8.setTextColor(Info.g_number);
                        } else if (view == nBtn9) {

                            nBtn9.setBackgroundResource(R.drawable.payment_subbtn2);
                            nBtn9.setTextColor(Info.g_number);
                        }
                    }
                } //if(m_bAddpay == false)

                return false;
            }
        };

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(m_bAddpay == false) {
                    ;

                    //Toast.makeText(getApplicationContext(), "이미 결제가 완료된 내역입니다.",Toast.LENGTH_LONG).show();
                } else {

                    if (mEditAddPay < 1000000) {

                        if (view == nBtn0) {
                            if (tvAddPay.getText().toString().equals("0")) {

                            } else {
                                tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "0") + "");
                            }
                        } else if (view == nBtn1) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "1") + "");
                        } else if (view == nBtn2) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "2") + "");
                        } else if (view == nBtn3) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "3") + "");
                        } else if (view == nBtn4) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "4") + "");
                        } else if (view == nBtn5) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "5") + "");
                        } else if (view == nBtn6) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "6") + "");
                        } else if (view == nBtn7) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "7") + "");
                        } else if (view == nBtn8) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "8") + "");
                        } else if (view == nBtn9) {
                            tvAddPay.setText(Integer.parseInt(tvAddPay.getText().toString() + "9") + "");
                        } else if (view == bBackspace) {
                            if (tvAddPay.getText().toString().length() > 0) {
                                if (tvAddPay.getText().toString().length() == 1) {
                                    if (tvAddPay.getText().toString().equals("0")) {

                                    } else {
                                        tvAddPay.setText("0");
                                    }
                                } else {
                                    tvAddPay.setText(tvAddPay.getText().toString().substring(0, tvAddPay.getText().toString().length() - 1));
                                }
                            } else {
                                tvAddPay.setText("0");
                            }
                        }

                        //                   if (tvAddPay.getText().toString().length() > 0) {
                        //                       int res = MainPays + Integer.parseInt(tvAddPay.getText().toString());
                        //                       tvResPay.setText(res + "");
                        //                   } else {

//                    }

                        mEditAddPay = Integer.parseInt(tvAddPay.getText().toString());

                    }
                }

                switch(view.getId())
                {
                    case R.id.layout_top:
                    {

                        setupBluetooth();
                        break;
                    }
                    case R.id.et_curtime:
                    {
                        Intent actIntent = new Intent(getApplicationContext(),
                                RecordListActivity.class);
                        actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                        startActivity(actIntent);
                        break;
                    }

                }
            }
        };

//////////////////////
        nBtn0.setOnTouchListener(touch1);
        nBtn1.setOnTouchListener(touch1);
        nBtn2.setOnTouchListener(touch1);
        nBtn3.setOnTouchListener(touch1);
        nBtn4.setOnTouchListener(touch1);
        nBtn5.setOnTouchListener(touch1);
        nBtn6.setOnTouchListener(touch1);
        nBtn7.setOnTouchListener(touch1);
        nBtn8.setOnTouchListener(touch1);
        nBtn9.setOnTouchListener(touch1);
        bBackspace.setOnTouchListener(touch1);

//////////////////////
        nBtn0.setOnClickListener(click);
        nBtn1.setOnClickListener(click);
        nBtn2.setOnClickListener(click);
        nBtn3.setOnClickListener(click);
        nBtn4.setOnClickListener(click);
        nBtn5.setOnClickListener(click);
        nBtn6.setOnClickListener(click);
        nBtn7.setOnClickListener(click);
        nBtn8.setOnClickListener(click);
        nBtn9.setOnClickListener(click);
        bBackspace.setOnClickListener(click);

        lineartopfare.setOnClickListener(click);

        tvcurtime.setOnClickListener(click);
//////////////////
        View.OnTouchListener touch2 = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent arg1)
            {

                { //if(m_bAddpay == false)
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

                        if (view == bCancel) {

                            bCancel.setBackgroundResource(R.drawable.payment_cancel_p);
                            bCancel.setTextColor(Info.g_cancel_p);
                        } else if (view == bAdd) {

                            bAdd.setBackgroundResource(R.drawable.payment_add_p);
                            bAdd.setTextColor(Info.g_add_p);
                        } else if (view == bPayment) {

                            bPayment.setBackgroundResource(R.drawable.payment_add_p);
                            bPayment.setTextColor(Info.g_add_p);
                        } else if (view == bReceipt) {

                            bReceipt.setBackgroundResource(R.drawable.payment_menu_p);
                            bReceipt.setTextColor(Info.g_menu_p);
                        } else if (view == bMenu) {

                            bMenu.setBackgroundResource(R.drawable.payment_menu_p);
                            bMenu.setTextColor(Info.g_menu_p);
                        }
                        else if (view == bBackspace) {

                            bBackspace.setBackgroundResource(R.drawable.payment_backspace_p);
                            bBackspace.setTextColor(Info.g_backspace_p);
                        }

                    }
///////////////////////////////
                    else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        if (view == bCancel) {

                            bCancel.setBackgroundResource(R.drawable.payment_cancel);
                            bCancel.setTextColor(Info.g_cancel);

                        } else if (view == bAdd) {

                            bAdd.setBackgroundResource(R.drawable.payment_add);
                            bAdd.setTextColor(Info.g_add);

                        } else if (view == bPayment) {

                            bPayment.setBackgroundResource(R.drawable.payment_add);
                            bPayment.setTextColor(Info.g_add);

                        } else if (view == bReceipt) {

                            bReceipt.setBackgroundResource(R.drawable.payment_menu);
                            bReceipt.setTextColor(Info.g_menu);

                        } else if (view == bMenu) {

                            bMenu.setBackgroundResource(R.drawable.payment_menu);
                            bMenu.setTextColor(Info.g_menu);

                        }
                        else if (view == bBackspace) {

                            bBackspace.setBackgroundResource(R.drawable.payment_backspace);
                            bBackspace.setTextColor(Info.g_backspace);

                        }
                    }
                } //if(m_bAddpay == false)

                return false;
            }
        };
///////////////

        bCancel.setOnTouchListener(touch2);
        bAdd.setOnTouchListener(touch2);
        bPayment.setOnTouchListener(touch2);
        bReceipt.setOnTouchListener(touch2);
        bMenu.setOnTouchListener(touch2);
        bBackspace.setOnTouchListener(touch2);

        //취소
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                //intent.addCategory(FLAG);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                        | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);

/*
                tvResPay.setText(MainPays + "");
                tvtextAdd.setText("0");

                AddPays = 0;

                if(m_bAddpay)
                    _showlayoutpay(true);
*/

/////////////////취소
                AMBlestruct.AMCardFare.msOpercode = "00000000"; //lastDriveCode();
                AMBlestruct.AMCardFare.mbCard = true;
                AMBlestruct.AMCardFare.mFare = ResPays;
                AMBlestruct.AMCardFare.mstype = "01";
                AMBlestruct.AMCardFare.mCardcode = "00000000";
                AMBlestruct.AMCardFare.mCardtime = getCurDateString(); //승차시간.

                m_Service.drive_state(AMBlestruct.MeterState.CANCELPAY);

            }
        });

        //추가
        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                m_bAddpay = true;
                mEditAddPay = 0;

                _showlayoutpay(false);

            }
        });

        //확인
        bPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payComp = true;
                /**
                 * 결제 확정 / 확정이후 요금 변경불가
                 */

                if(m_bAddpay) {

                    if (tvAddPay.getText().toString().length() > 0) {
                        AddPays = Integer.parseInt(tvAddPay.getText().toString());
                        tvtextAdd.setText(AddPays + "");
                        ResPays = MainPays + AddPays;
                        tvResPay.setText(ResPays + "");

                    }

                    _showlayoutpay(true);

                }

////////////////////////
                    AMBlestruct.AMCardFare.msOpercode = "00000000";
                    AMBlestruct.AMCardFare.mbCard = true;
                    AMBlestruct.AMCardFare.mstype = "01";
                    AMBlestruct.AMCardFare.mStarttime = getCurDateString();
                    AMBlestruct.AMCardFare.mEndtime = getCurDateString();
                    AMBlestruct.AMCardFare.mFare = MainPays;
                    AMBlestruct.AMCardFare.mFareDis = 0; //할인금액.
                    AMBlestruct.AMCardFare.mCallCharge = 0; //호출요금.
                    AMBlestruct.AMCardFare.mAddCharge = AddPays; //추가요금.
//                        AMBlestruct.AMCardFare.mMoveDistance = 0; //승차거리

                    m_Service.writeBLE("21");

                    m_Service.update_BLEmeterstate("01"); //?

////////////////////

            }
        });

        bReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 영수증 출력
                 */

                if(AMBlestruct.AMCardResult.msOpercode.equals(""))
                    AMBlestruct.AMCardResult.msOpercode = "00000000";

                m_Service.writeBLE("26");

//                endpayment();

            }
        });

        bMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actIntent = new Intent(getApplicationContext(),
                        AMBleConfigActivity.class);

                actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

                startActivity(actIntent);

            }
        });

        bmobilepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appPaymentJSON("222");

            }
        });

        if(m_bAddpay)
            _showlayoutpay(false);

    }

    private void set_frame_orient(int tp)
    {
//////////////////////
        View viewframe1 = null;
        paymentframe1 = (FrameLayout) findViewById(R.id.paymentframe1); // 1. 기반이 되는 FrameLayout
        if (paymentframe1.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            paymentframe1.removeViewAt(0);

        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe1 = inflater.inflate(R.layout.paymentframe1, paymentframe1,true);

        lineartopfare = (LinearLayout)viewframe1.findViewById(R.id.layout_top);

        tvMainPay = (TextView)viewframe1.findViewById(R.id.tv_mainpay);
        tvtextAdd = (TextView)viewframe1.findViewById(R.id.et_addpayment);
        tvAddPay = (TextView)viewframe1.findViewById(R.id.et_addtmp);
        tvResPay = (TextView)viewframe1.findViewById(R.id.tv_finpayment);
        tvcurtime = (TextView)viewframe1.findViewById(R.id.et_curtime);
        tvetdist =(TextView)viewframe1.findViewById(R.id.et_dist);

//////////////////////
        View viewframe2 = null;


        paymentframe2 = (FrameLayout) findViewById(R.id.paymentframe2); // 1. 기반이 되는 FrameLayout
        if (paymentframe2.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            paymentframe2.removeViewAt(0);

        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe2 = inflater.inflate(R.layout.paymentframe2, paymentframe2,true);

        nBtn0 = (Button)viewframe2.findViewById(R.id.btn_0);
        nBtn1 = (Button)viewframe2.findViewById(R.id.btn_1);
        nBtn2 = (Button)viewframe2.findViewById(R.id.btn_2);
        nBtn3 = (Button)viewframe2.findViewById(R.id.btn_3);
        nBtn4 = (Button)viewframe2.findViewById(R.id.btn_4);
        nBtn5 = (Button)viewframe2.findViewById(R.id.btn_5);
        nBtn6 = (Button)viewframe2.findViewById(R.id.btn_6);
        nBtn7 = (Button)viewframe2.findViewById(R.id.btn_7);
        nBtn8 = (Button)viewframe2.findViewById(R.id.btn_8);
        nBtn9 = (Button)viewframe2.findViewById(R.id.btn_9);

        bBackspace = (Button)viewframe2.findViewById(R.id.btn_backspace);

        /**
         *
         */

        bCancel = (Button)viewframe2.findViewById(R.id.btn_cancel);
        bAdd = (Button)viewframe2.findViewById(R.id.btn_add);
        bPayment = (Button)viewframe2.findViewById(R.id.btn_payment);

        bReceipt = (Button)viewframe2.findViewById(R.id.btn_receipt);

        bMenu = (Button)viewframe2.findViewById(R.id.btn_menuconfig);

        bmobilepay = (Button)viewframe2.findViewById(R.id.btn_mobilepay);

////////////

//////////////init

        _initPaylayout();

    }

    void _initPaylayout()
    {
        m_layoutPay = (LinearLayout)findViewById(R.id.layoutpay) ;
        m_layoutAdd = (LinearLayout)findViewById(R.id.layoutadd) ;

        m_layoutPay.setVisibility(View.VISIBLE);
        m_layoutAdd.setVisibility(View.INVISIBLE);
    }

    void _showlayoutpay(boolean bflag)
    {
        if(bflag)
        {
            m_layoutPay.setVisibility(View.VISIBLE);
            m_layoutAdd.setVisibility(View.INVISIBLE);

            m_bAddpay = false;
        }
        else
        {
            tvAddPay.setText(AddPays + "");
            m_layoutPay.setVisibility(View.INVISIBLE);
            m_layoutAdd.setVisibility(View.VISIBLE);
        }

        _enable_btn(bflag);

    }

    private void _enable_btn(boolean bflag)
    {

        if(bflag == true) //payment
        {

            bCancel.setEnabled(true);
            bCancel.setTextColor(Info.g_cancel);
            bCancel.setBackgroundResource(R.drawable.payment_cancel);
            bAdd.setEnabled(true);
            bAdd.setTextColor(Info.g_add);
            bAdd.setBackgroundResource(R.drawable.payment_add);
            bPayment.setEnabled(true);
            bPayment.setTextColor(Info.g_add);
            bPayment.setBackgroundResource(R.drawable.payment_add);
            bReceipt.setEnabled(true);
            bReceipt.setTextColor(Info.g_menu);
            bReceipt.setBackgroundResource(R.drawable.payment_menu);
            bMenu.setEnabled(true);
            bMenu.setTextColor(Info.g_menu);
            bMenu.setBackgroundResource(R.drawable.payment_menu);
            bBackspace.setEnabled(false);
            bBackspace.setTextColor(Info.g_backspace);
            bBackspace.setBackgroundResource(R.drawable.payment_backspace);
            bmobilepay.setEnabled(true);
            bmobilepay.setTextColor(Info.g_menu);
            bmobilepay.setBackgroundResource(R.drawable.payment_menu);
        }
        else //payadd
        {

            bCancel.setEnabled(true);
            bCancel.setTextColor(Info.g_cancel);
            bCancel.setBackgroundResource(R.drawable.payment_cancel);
            bAdd.setEnabled(false);
            bAdd.setTextColor(Info.g_add_p);
            bAdd.setBackgroundResource(R.drawable.payment_btndis);
            bPayment.setEnabled(true);
            bPayment.setTextColor(Info.g_add);
            bPayment.setBackgroundResource(R.drawable.payment_add);
            bReceipt.setEnabled(false);
            bReceipt.setTextColor(Info.g_menu_p);
            bReceipt.setBackgroundResource(R.drawable.payment_btndis);
            bMenu.setEnabled(false);
            bMenu.setTextColor(Info.g_menu_p);
            bMenu.setBackgroundResource(R.drawable.payment_btndis);
            bBackspace.setEnabled(true);
            bBackspace.setTextColor(Info.g_backspace);
            bBackspace.setBackgroundResource(R.drawable.payment_backspace);
            bmobilepay.setEnabled(false);
            bmobilepay.setTextColor(Info.g_menu_p);
            bmobilepay.setBackgroundResource(R.drawable.payment_btndis);
        }

    }

    public void db_insert_rundata(int runmode) {

        m_bDBstart = true;

        Info.insert_rundata(mlocation, runmode);

    }

    public void db_update_runlocationdata() {

        if (mseconds == 0 && m_bDBstart == true) {

            if(mlocation != null)
                Info.update_runlocationdata(mlocation);

        }
    }

    public void db_end_rundata() {

        //Log.e("db_end_rundata", "end_rundata");

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long fromtime = 0;
        long totime = 0;
        try {
            // 현재 yyyymmdd로된 날짜 형식으로 java.util.Date객체를 만든다.

            if(AMBlestruct.AMReceiveFare.mStarttime.equals("") == false && AMBlestruct.AMReceiveFare.mEndtime.equals("") == false) {
                fromtime = transFormat.parse(AMBlestruct.AMReceiveFare.mStarttime).getTime() / 1000;
                totime = transFormat.parse(AMBlestruct.AMReceiveFare.mEndtime).getTime() / 1000;

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Info.end_rundata(mlocation, ResPays, 0, AddPays, AMBlestruct.AMReceiveFare.mBoarddist, (int)Math.abs(totime - fromtime));


    }

    private String getCurDateString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    private void loadinit()
    {

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE); //160622
        lbs_x = pref.getInt("lbs_x", -1);
        lbs_y = pref.getInt("lbs_y", -1);

    }

    private void endpayment()
    {

        m_Service.drive_state(AMBlestruct.MeterState.EMPTY);

    }


    /**
     * {
     "merchantId": "410141811913401",
     "trList":
     {
     "cardType": "03",
     "par": "Q173ACED17FFD50F23AC72051B9",
     "posEntryMode": "03",
     "tips": 0,
     "trAmount": 1000, <-- 총 결제 금액
     "vat": 0 <- 부가세
     }
     }
     */
    public String appPaymentJSON(String pay) {
        Thread NetworkThreads = new Thread(new NetworkThreads());
        NetworkThreads.start();

        return pay;
    }

    private void setupBluetooth() { // BT setup // TODO Auto-generated method

        Intent blueIntent = new Intent(this, pos_app_bluetoothLeActivity.class);
        startActivity(blueIntent);
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


    class NetworkThreads implements Runnable {
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
                trList.accumulate("par","Q173ACED17FFD50F23AC72051B9");
                trList.accumulate("posEntryMode","03");
                trList.accumulate("tips",0);
                trList.accumulate("trAmount",1080);
                trList.accumulate("vat",120);

                trArray.put(trList);

                main.accumulate("merchantId", "410141811913401");
                main.accumulate("trList", trArray);

                //Log.e("SEND JSON", main.toString());

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

                //Log.e("POST RESULT : ", result);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
