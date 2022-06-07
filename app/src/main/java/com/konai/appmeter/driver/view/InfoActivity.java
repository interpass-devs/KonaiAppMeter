package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.VO.TIMS_UnitVO;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;
import com.konai.appmeter.driver.struct.DTGQueue;
import com.konai.appmeter.driver.struct.TIMSQueue;

import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InfoActivity extends Activity {

    //깃헙테스트333??????????????????????

    private TextView textView9, rtv_name, phone_title, unique_title, carno_title, company_title, gps_title, bluetooth_title, obd_title, osver_title, maker_title;
    private TextView tv_name, tv_phoneno, tv_unique, tv_carno, tv_company, tv_gps, tv_bluetooth, tv_obd, tv_osver, tv_maker, tv_version, area_name, basic_fare, basic_dist, after_fare;
    private TextView tv_area_name, tv_base_cost, tv_base_drvdist, tv_dist_cost, tv_interval_dist, tv_night_time_rate;
    private Button btn_goMenu, btn_tims, connStatusBtn;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context mContext;

    private String Domain = "";
    private String TIMS_BASEURL = "https://tims-help.kotsa.or.kr";
    private String TIMS_ADDURL = "";
    private MainActivity mainActivity;

    private String[] splt;
    private SQLiteHelper helper;
    private SQLiteControl sqlite;

    private String phoneNo, carno, logs, logtime, logtype, log;

    public static LocService m_Service = null;
    private LocService mService;

    Thread timsSendThread = null;
    Thread dtgSendThread = null;
    public BlockingQueue<DTGQueue> mSendDTGQ = new ArrayBlockingQueue<DTGQueue>(5);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializecontents(setting.gOrient);

        mContext = this;

        helper = new SQLiteHelper(mContext);
        sqlite = new SQLiteControl(helper);

        //me: 3일 전 데이터 삭제
//        sqlite.deleteConnStatus();  //데이터 전체가 삭제되고 있음..

//        timsSendThread = new Thread(new TIMS_THREAD());
//        timsSendThread.start();

//        dtgSendThread = new Thread(new DTG_NETWORK_THREAD());
//        dtgSendThread.start();

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(Info.m_Service != null)
            Info.m_Service._showhideLbsmsg(false);
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

    private void initializecontents(int nTP)
    {
        if(nTP == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //20220531
            setContentView(R.layout.activity_info);  //세로
            set_frame_orient(0);
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //20220531
            setContentView(R.layout.activity_info_h);  //가로
            set_frame_orient(1);
        }
        tv_area_name.setText(Info.AREA_CODE); //20220318 tra..sh
        tv_base_cost.setText(CalFareBase.BASECOST+" 원");
        tv_base_drvdist.setText(CalFareBase.BASEDRVDIST+" m");
        tv_dist_cost.setText(CalFareBase.DISTCOST+" 원");
        tv_interval_dist.setText(CalFareBase.INTERVAL_DIST+" m");
        tv_night_time_rate.setText((int) CalFareBase.INTERVAL_TIME+" 초");
//        Log.d("check_night_rate>>",CalFareBase.mNightTimerate+"");

        tv_name.setText(AMBlestruct.AMLicense.drivername);
        tv_carno.setText(AMBlestruct.AMLicense.taxinumber);

        tv_phoneno.setText(AMBlestruct.AMLicense.phonenumber);
        tv_unique.setText(AMBlestruct.AMLicense.licensecode);
        tv_company.setText("코나아이");

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            tv_gps.setText("OFF");
        }
        else
            tv_gps.setText("ON");

        final BluetoothAdapter BluetoothAdapter;
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter = bluetoothManager.getAdapter();
        if (BluetoothAdapter != null && BluetoothAdapter.isEnabled()) //20220318 tra..sh
        {
            tv_bluetooth.setText("ON");
        }
        else
            tv_bluetooth.setText("OFF");

        if(AMBlestruct.mBTConnected)
        {
            tv_obd.setText("연결됨");
        }
        else
            tv_obd.setText("연결안됨");

        tv_osver.setText(Build.VERSION.RELEASE + "\n" + Build.MODEL); //20220318 tra..sh + "\n" + Build.MANUFACTURER);

        tv_maker.setText("ver " + Info.APP_VERSION + "/" + getBuildtime() + "/Konai"); //20220318 tra..sh

//        tv_version.setText("ver " + Info.APP_VERSION + "/" + getBuildtime());

        btn_goMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_goMenu.setBackgroundResource(R.drawable.btn_menus_c);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn_goMenu.setBackgroundResource(R.drawable.btn_menus);
                }
                return false;
            }
        });

        btn_goMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(InfoActivity.this, "정보화면 메뉴버튼 클릭", Toast.LENGTH_SHORT).show();

                if (Info.m_Service != null) {
                    Log.d("info_m_Service","not null");
                    Info.m_Service._showhideLbsmsg(false);
                }else {
                    Log.d("info_m_Service","null");
                }
                finish();
            }
        });

        btn_tims.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    btn_tims.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_tims.setBackgroundResource(R.drawable.yellow_gradi_btn);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    btn_tims.setBackgroundColor(Color.parseColor("#2e2eae"));
                    btn_tims.setBackgroundResource(R.drawable.selected_btn_touched_yellow);
                }
                return false;
            }
        });

        btn_tims.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actIntent = new Intent(getApplicationContext(),
                        TimsInfoActivity.class);

                startActivity(actIntent);

                finish();
            }
        });

        //연결상태 전송 버튼
        connStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                final LinearLayout dialogView;
                dialogView = (LinearLayout)View.inflate(mContext, R.layout.dlg_basic, null);

                final TextView msg = (TextView) dialogView.findViewById(R.id.msg);
                final Button okBtn = (Button) dialogView.findViewById(R.id.okay_btn);
                final Button cancelBtn = (Button)dialogView.findViewById(R.id.cancel_btn);
                msg.setText("데이터를 전송하시겠습니까?");
                okBtn.setText("전송");

                final Dialog dlg = new Dialog(InfoActivity.this);
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dlg.setContentView(dialogView);
                dlg.setCancelable(true);

                //전송버튼
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mainActivity.m_Service.m_timsdtg._sendTIMSConnStatus();

                        dlg.dismiss();
                    }
                });

                //취소버튼
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });

                DisplayMetrics dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                if (Build.VERSION.SDK_INT <= 25) {
                    msg.setTextSize(3.0f * setting.gTextDenst);
                    cancelBtn.setTextSize(2.5f * setting.gTextDenst);
                    okBtn.setTextSize(2.5f * setting.gTextDenst);
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

            }
        });

    }



    private void set_frame_orient(int tp)
    {
        textView9 = (TextView) findViewById(R.id.textView9);
        rtv_name = (TextView)findViewById(R.id.rtv_name);
        connStatusBtn = (Button)findViewById(R.id.conn_status_btn);
        phone_title = (TextView)findViewById(R.id.phone_title);
        unique_title = (TextView)findViewById(R.id.unique_title);
        carno_title = (TextView)findViewById(R.id.carno_title);
        company_title = (TextView)findViewById(R.id.company_title);
        gps_title = (TextView)findViewById(R.id.gps_title);
        bluetooth_title = (TextView)findViewById(R.id.bluetooth_title);
        obd_title = (TextView)findViewById(R.id.obd_title);
        osver_title = (TextView)findViewById(R.id.osver_title);
        maker_title = (TextView)findViewById(R.id.maker_title);

        tv_phoneno = (TextView)findViewById(R.id.tv_phoneno);
        tv_unique = (TextView)findViewById(R.id.tv_unique);
        tv_carno = (TextView)findViewById(R.id.tv_carno);
        tv_company = (TextView)findViewById(R.id.tv_company);
        tv_gps = (TextView)findViewById(R.id.tv_gps);
        tv_bluetooth = (TextView)findViewById(R.id.tv_bluetooth);
        tv_obd = (TextView)findViewById(R.id.tv_obd);

        tv_osver = (TextView)findViewById(R.id.tv_osver);
        tv_maker = (TextView)findViewById(R.id.tv_maker);
        tv_version = (TextView)findViewById(R.id.tv_version);

        tv_area_name = (TextView)findViewById(R.id.area_name);
        tv_base_cost = (TextView)findViewById(R.id.base_cost);
        tv_base_drvdist = (TextView)findViewById(R.id.base_drvdist);
        tv_dist_cost = (TextView)findViewById(R.id.dist_cost);
        tv_interval_dist = (TextView)findViewById(R.id.interval_dist);
        tv_night_time_rate = (TextView)findViewById(R.id.night_time_rate);

        tv_name = (TextView)findViewById(R.id.rtv_name);
        btn_goMenu = (Button)findViewById(R.id.dbtn_menu);
        btn_tims = (Button)findViewById(R.id.btn_tims);

        if (tp == 1){
            if (Build.VERSION.SDK_INT >= 24){

                textView9.setTextSize(3.5f * setting.gTextDenst);
                rtv_name.setTextSize(3 * setting.gTextDenst);
                phone_title.setTextSize(2 * setting.gTextDenst);
                unique_title.setTextSize(2 * setting.gTextDenst);
                carno_title.setTextSize(2 * setting.gTextDenst);
                company_title.setTextSize(2 * setting.gTextDenst);
                gps_title.setTextSize(2 * setting.gTextDenst);
                bluetooth_title.setTextSize(2 * setting.gTextDenst);
                obd_title.setTextSize(2 * setting.gTextDenst);
                osver_title.setTextSize(2 * setting.gTextDenst);
                maker_title.setTextSize(2 * setting.gTextDenst);

                tv_phoneno.setTextSize(2 * setting.gTextDenst);
                tv_unique.setTextSize(2 * setting.gTextDenst);
                tv_carno.setTextSize(2 * setting.gTextDenst);
                tv_company.setTextSize(2 * setting.gTextDenst);
                tv_gps.setTextSize(2 * setting.gTextDenst);
                tv_bluetooth.setTextSize(2 * setting.gTextDenst);
                tv_obd.setTextSize(2 * setting.gTextDenst);
                tv_osver.setTextSize(2 * setting.gTextDenst);
                tv_maker.setTextSize(2 * setting.gTextDenst);
                //   tv_version.setTextSize(3 * setting.gTextDenst);
            }else {}
        }

    }

    private String getBuildtime() {
        Date buildDate = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yy.MM.dd");
        return transFormat.format(buildDate);
    }


}
