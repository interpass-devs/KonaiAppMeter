package com.konai.appmeter.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

public class DriveInfoActivity extends Activity {

    setting settings = new setting();

    private FrameLayout frame1 = null;
    private FrameLayout frame2 = null;

    /**
     * 차량 운행정보
     * 1. 총 주행거리
     * 2. 총 주행시간
     * 3. 총 기본회수
     * 4. 총 이후회수
     * 5. 총 할증 기본회수
     * 6. 총 할증 이후회수
     * 7. 총 수입금
     */
    private int tPayDrv = 0;
    private int tEmptyDrv = 0;
    private int tPayment = 0;
    private int tPaySec = 0;
    private int tEmptySec = 0;
    private int tBasePayCnt = 0;
    private int tAfterPayCnt = 0;
    private int tExtraBPayCnt = 0;
    private int tExtraAPayCnt = 0;

    TextView tv_totaldistance;
    TextView tv_totaldrvtime;
    TextView tv_tbasepaycnt;
    TextView tv_tafterpaycnt;
    TextView tv_extrabasepaycnt;
    TextView tv_extraafterpaycnt;
    TextView tv_totalpayment;

    TextView tv_totEmptyTime;
    TextView tv_totDrvTime;
    TextView tv_totDrvRatio;

    Button btn_chkinfo;
    Button btn_goMain;
    Button btn_goMenu;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_driveinfo);
//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

//20220415 ver157        Info.init_SQLHelper(this);

        String[] values = Info.sqlite.totSelect();

        if(values.length > 1)
        for(int i=0; i<values.length; i++) {

            Log.e("getData_total", values.length + "");

            Log.e("getData", values[i] + "");
            String[] res = values[i].split("#");

            tPayDrv = tPayDrv + Integer.parseInt(res[2]);
            tEmptyDrv = tEmptyDrv + Integer.parseInt(res[3]);
            tPayment = tPayment + Integer.parseInt(res[4]);
            tPaySec = tPaySec + Integer.parseInt(res[5]);
            tEmptySec = tEmptySec + Integer.parseInt(res[6]);
            tBasePayCnt = tBasePayCnt + Integer.parseInt(res[7]);
            tAfterPayCnt = tAfterPayCnt + Integer.parseInt(res[8]);
            tExtraBPayCnt = tExtraBPayCnt + Integer.parseInt(res[9]);
            tExtraAPayCnt = tExtraAPayCnt + Integer.parseInt(res[10]);
        }

        tv_totaldistance.setText((tPayDrv/1000) + " KM");
        tv_totaldrvtime.setText((tEmptyDrv/1000) + " KM");
        tv_totEmptyTime.setText((tEmptySec/60) + " 분");
        tv_totDrvTime.setText((tPaySec/60) + " 분");
        tv_tbasepaycnt.setText(tBasePayCnt + " 회");
        tv_tafterpaycnt.setText(tAfterPayCnt + " 회");
        tv_extrabasepaycnt.setText(tExtraBPayCnt + " 회");
        tv_extraafterpaycnt.setText(tExtraAPayCnt + " 회");
        tv_totalpayment.setText(tPayment + " 원");
        tv_totDrvRatio.setText((String.format("%.2f", Float.parseFloat(tPayDrv + "")/(Float.parseFloat((tPayDrv + tEmptyDrv) + "")) * 100.0) + " %"));

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

//20220415 ver157        Info.init_SQLHelper(this);

        String[] values = Info.sqlite.totSelect();
        if(values.length > 1)
        for(int i=0; i<values.length; i++) {

            Log.d("total_cnt", values.length+"");

            Log.e("getData", values[i] + "");
            String[] res = values[i].split("#");

            tPayDrv = tPayDrv + Integer.parseInt(res[2]);
            tEmptyDrv = tEmptyDrv + Integer.parseInt(res[3]);
            tPayment = tPayment + Integer.parseInt(res[4]);
            tPaySec = tPaySec + Integer.parseInt(res[5]);
            tEmptySec = tEmptySec + Integer.parseInt(res[6]);
            tBasePayCnt = tBasePayCnt + Integer.parseInt(res[7]);
            tAfterPayCnt = tAfterPayCnt + Integer.parseInt(res[8]);
            tExtraBPayCnt = tExtraBPayCnt + Integer.parseInt(res[9]);
            tExtraAPayCnt = tExtraAPayCnt + Integer.parseInt(res[10]);
        }

        tv_totaldistance.setText((tPayDrv/1000) + " KM");
        tv_totaldrvtime.setText((tEmptyDrv/1000) + " KM");
        tv_totEmptyTime.setText((tEmptySec/60) + " 분");
        tv_totDrvTime.setText((tPaySec/60) + " 분");
        tv_tbasepaycnt.setText(tBasePayCnt + " 회");
        tv_tafterpaycnt.setText(tAfterPayCnt + " 회");
        tv_extrabasepaycnt.setText(tExtraBPayCnt + " 회");
        tv_extraafterpaycnt.setText(tExtraAPayCnt + " 회");
        tv_totalpayment.setText(tPayment + " 원");
        tv_totDrvRatio.setText((String.format("%.2f", Float.parseFloat(tPayDrv + "")/(Float.parseFloat((tPayDrv + tEmptyDrv) + "")) * 100.0) + " %"));

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    private void initializecontents(int nTP)
    {
        if(nTP == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_driverinfo_v);
            set_frame_orient(0);

        }
        else
        {

            setContentView(R.layout.activity_driverinfo_h);
            set_frame_orient(1);
        }

        btn_chkinfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//                    btn_chkinfo.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_chkinfo.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
//                    btn_chkinfo.setBackgroundColor(Color.parseColor("#2e2eae"));6
                    btn_chkinfo.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }
                return false;
            }
        });

        btn_chkinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actIntent = new Intent(getApplicationContext(),
                        RecordListActivity.class);
                actIntent.putExtra("SDATE", "a");

                startActivity(actIntent);
            }
        });

        btn_goMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_goMain.setBackgroundColor(Color.parseColor("#000000"));
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn_goMain.setBackgroundColor(Color.parseColor("#999999"));
                }
                return false;
            }
        });

        btn_goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
                finish();
            }
        });

    }

    private void set_frame_orient(int tp)
    {
//////////////////////
        View viewframe1 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1); // 1. 기반이 되는 FrameLayout
        if (frame1.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame1.removeViewAt(0);

        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe1 = inflater.inflate(R.layout.driverinfoframe1, frame1,true);

        tv_totaldistance = (TextView)viewframe1.findViewById(R.id.tv_totaldistance);
        tv_totaldrvtime = (TextView)viewframe1.findViewById(R.id.tv_totaldrvtime);
        tv_tbasepaycnt = (TextView)viewframe1.findViewById(R.id.tv_tbasepaycnt);
        tv_tafterpaycnt = (TextView)viewframe1.findViewById(R.id.tv_tafterpaycnt);
        tv_extrabasepaycnt = (TextView)viewframe1.findViewById(R.id.tv_extrabasepaycnt);
        tv_extraafterpaycnt = (TextView)viewframe1.findViewById(R.id.tv_extraafterpaycnt);
        tv_totEmptyTime = (TextView)viewframe1.findViewById(R.id.tv_totemptytime);
        tv_totDrvTime = (TextView)viewframe1.findViewById(R.id.tv_totdrvtime);
        tv_totDrvRatio = (TextView)viewframe1.findViewById(R.id.tv_totdrvpercent);

//////////////////////
        View viewframe2 = null;


        frame2 = (FrameLayout) findViewById(R.id.frame2); // 1. 기반이 되는 FrameLayout
        if (frame2.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame2.removeViewAt(0);

        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe2 = inflater.inflate(R.layout.driverinfoframe2, frame2,true);

        tv_totalpayment = (TextView)viewframe2.findViewById(R.id.tv_totalpayment);
        btn_chkinfo = (Button)viewframe2.findViewById(R.id.btn_totalrec_detail);
        btn_goMain = (Button)viewframe2.findViewById(R.id.ibtn_gomain);

////////////////////////

        btn_goMenu = (Button)findViewById(R.id.dbtn_menu);

        if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
        {



        }
        else if(tp == 1)
        {
            tv_totalpayment.setTextSize(7.0f * setting.gTextDenst);
            btn_chkinfo.setTextSize(6.0f * setting.gTextDenst);  //상세내역
            btn_goMain.setTextSize(6.0f * setting.gTextDenst);  //홈

            tv_totaldistance.setTextSize(2f * setting.gTextDenst);
            tv_totaldrvtime.setTextSize(2f * setting.gTextDenst);
            tv_tbasepaycnt.setTextSize(2f * setting.gTextDenst);
            tv_tafterpaycnt.setTextSize(2f * setting.gTextDenst);
            tv_extrabasepaycnt.setTextSize(2f * setting.gTextDenst);
            tv_extraafterpaycnt.setTextSize(2f * setting.gTextDenst);
            tv_totEmptyTime.setTextSize(2f * setting.gTextDenst);
            tv_totDrvTime.setTextSize(2f * setting.gTextDenst);
            tv_totDrvRatio.setTextSize(2f * setting.gTextDenst);

            TextView textView9 = (TextView)findViewById(R.id.textView9);
            textView9.setTextSize(3.5f * setting.gTextDenst);

            TextView textView11 = (TextView)viewframe1.findViewById(R.id.textView11);
            textView11.setTextSize(2f * setting.gTextDenst);
            TextView textView12 = (TextView)viewframe1.findViewById(R.id.textView12);
            textView12.setTextSize(2f * setting.gTextDenst);
            TextView textView13 = (TextView)viewframe1.findViewById(R.id.textView13);
            textView13.setTextSize(2f * setting.gTextDenst);
            TextView textView14 = (TextView)viewframe1.findViewById(R.id.textView14);
            textView14.setTextSize(2f * setting.gTextDenst);
            TextView textView15 = (TextView)viewframe1.findViewById(R.id.textView15);
            textView15.setTextSize(2f * setting.gTextDenst);
            TextView textView16 = (TextView)viewframe1.findViewById(R.id.textView16);
            textView16.setTextSize(2f * setting.gTextDenst);
            TextView textView17 = (TextView)viewframe1.findViewById(R.id.textView17);
            textView17.setTextSize(2f * setting.gTextDenst);
            TextView textView18 = (TextView)viewframe1.findViewById(R.id.textView18);
            textView18.setTextSize(2f * setting.gTextDenst);
            TextView textView19 = (TextView)viewframe1.findViewById(R.id.textView19);
            textView19.setTextSize(2f * setting.gTextDenst);

            TextView textView21 = (TextView)viewframe2.findViewById(R.id.textView21);
            textView21.setTextSize(4.0f * setting.gTextDenst);

        }

    }

}
