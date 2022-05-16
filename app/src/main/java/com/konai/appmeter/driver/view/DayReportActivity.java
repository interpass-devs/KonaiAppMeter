package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DayReportActivity extends Activity {

    private FrameLayout frame1 = null;
    private FrameLayout frame2 = null;

    private TextView tv_nowDate;
    public String st_date;

    private TextView tv_TRDistance;
    private TextView tv_TRDirectPayCnt;
    private TextView tv_TRCashPay;
    private TextView tv_TRCardPay;
    private TextView tv_TRAutoPay;
    private TextView tv_TRTotalPayCnt;
    private TextView tv_TRTotalPay;

    private Button btn_goMain;
    private Button btn_goMenu;
    private Button btn_TRDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_dayreport);
//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

//20220415 ver157        Info.init_SQLHelper(this);
        String[] dayDrvRecordData = Info.sqlite.todayTotSelect().split("/");
        String driveCount = Info.sqlite.todayDriveCount();

        if(dayDrvRecordData.length > 1 && dayDrvRecordData[1].equals("null") == false)
        {
            tv_TRDistance.setText((String.format("%.2f", (Double.parseDouble(dayDrvRecordData[0]) + Double.parseDouble(dayDrvRecordData[1])) / 1000)) + "km");
            tv_TRDirectPayCnt.setText(driveCount + " 건");
            tv_TRTotalPayCnt.setText(driveCount + " 건");
            tv_TRTotalPay.setText(dayDrvRecordData[4] + "원");
        }
        String[] payDiv = Info.sqlite.todayPaymentCount().split("/");

        if(payDiv.length > 1) {
            tv_TRCashPay.setText(payDiv[0] + " 건");
            tv_TRCardPay.setText(payDiv[1] + " 건");
        }
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
        String[] dayDrvRecordData = Info.sqlite.todayTotSelect().split("/");
        String driveCount = Info.sqlite.todayDriveCount();

        if(dayDrvRecordData.length > 1 && dayDrvRecordData[1].equals("null") == false)
        {
            tv_TRDistance.setText((String.format("%.2f", (Double.parseDouble(dayDrvRecordData[0]) + Double.parseDouble(dayDrvRecordData[1])) / 1000)) + "km");
            tv_TRDirectPayCnt.setText(driveCount + " 건");
            tv_TRTotalPayCnt.setText(driveCount + " 건");
            tv_TRTotalPay.setText(dayDrvRecordData[4] + "원");
        }
        String[] payDiv = Info.sqlite.todayPaymentCount().split("/");

        if(payDiv.length > 1) {
            tv_TRCashPay.setText(payDiv[0] + " 건");
            tv_TRCardPay.setText(payDiv[1] + " 건");
        }

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);
    }

    private void initializecontents(int nTP)
    {
        if(nTP == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_dayreport_v);
            set_frame_orient(0);
        }
        else
        {
            setContentView(R.layout.activity_dayreport_h);
            set_frame_orient(1);
        }

        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
        tv_nowDate.setText(transFormat.format(time));
        st_date = transFormat.format(time);

        btn_TRDetail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
//                    btn_TRDetail.setBackgroundColor(Color.parseColor("#2e2e6a"));
                    btn_TRDetail.setBackgroundResource(R.drawable.btn_drive_end_touch);
                }
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
//                    btn_TRDetail.setBackgroundColor(Color.parseColor("#2e2eae"));
                    btn_TRDetail.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }
                return false;
            }
        });

        //me: [확인] 버튼
        btn_TRDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actIntent = new Intent(getApplicationContext(), RecordListActivity.class);    //당일거래내역에서 상세내역 화면으로 이동
                actIntent.putExtra("SDATE", "t");
                actIntent.putExtra("st_date",st_date);

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
        viewframe1 = inflater.inflate(R.layout.dayreportframe1, frame1,true);

        tv_TRDistance = (TextView)viewframe1.findViewById(R.id.tv_todayrecdist);      //운행거리
        tv_TRDirectPayCnt = (TextView)viewframe1.findViewById(R.id.tv_todayrecdirpaycnt);  //직접결제
        tv_TRCashPay = (TextView)viewframe1.findViewById(R.id.tv_todayrec_cash);   //현금결제
        tv_TRCardPay = (TextView)viewframe1.findViewById(R.id.tv_todayrec_card);   //카드결제
        tv_TRAutoPay = (TextView)viewframe1.findViewById(R.id.tv_todayrecautopay);   //자동결제
        tv_TRTotalPayCnt = (TextView)viewframe1.findViewById(R.id.tv_todayrectotpaycnt);   //총거래건

//////////////////////
        View viewframe2 = null;


        frame2 = (FrameLayout) findViewById(R.id.frame2); // 1. 기반이 되는 FrameLayout
        if (frame2.getChildCount() > 0) {
            // FrameLayout에서 뷰 삭제.
            frame2.removeViewAt(0);

        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 2. inflater 생성
        viewframe2 = inflater.inflate(R.layout.dayreportframe2, frame2,true);

        tv_TRTotalPay = (TextView)viewframe2.findViewById(R.id.tv_todayrectotpay);
        btn_goMain = (Button)viewframe2.findViewById(R.id.dbtn_gomain);
        btn_TRDetail = (Button)viewframe2.findViewById(R.id.btn_todayrec_detail);

////////////////////////

        tv_nowDate = (TextView)findViewById(R.id.rtv_nowdate);
        btn_goMenu = (Button)findViewById(R.id.dbtn_menu);

        if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
        {

            ;

        }
        else if(tp == 1)
        {
            tv_nowDate.setTextSize(4.0f * setting.gTextDenst);

            tv_TRTotalPay.setTextSize(7.0f * setting.gTextDenst);
            btn_goMain.setTextSize(6.0f * setting.gTextDenst);
            btn_TRDetail.setTextSize(6.0f * setting.gTextDenst);

            tv_TRDistance.setTextSize(3.0f * setting.gTextDenst);
            tv_TRDirectPayCnt.setTextSize(3.0f * setting.gTextDenst);
            tv_TRCashPay.setTextSize(3.0f * setting.gTextDenst);
            tv_TRCardPay.setTextSize(3.0f * setting.gTextDenst);
            tv_TRAutoPay.setTextSize(3.0f * setting.gTextDenst);
            tv_TRTotalPayCnt.setTextSize(3.0f * setting.gTextDenst);

            TextView textView9 = (TextView)findViewById(R.id.textView9);
            textView9.setTextSize(3.5f * setting.gTextDenst);

            TextView textView11 = (TextView)viewframe1.findViewById(R.id.textView11);
            textView11.setTextSize(3.0f * setting.gTextDenst);
            TextView textView12 = (TextView)viewframe1.findViewById(R.id.textView12);
            textView12.setTextSize(3.0f * setting.gTextDenst);
            TextView textView13 = (TextView)viewframe1.findViewById(R.id.textView13);
            textView13.setTextSize(3.0f * setting.gTextDenst);
            TextView textView14 = (TextView)viewframe1.findViewById(R.id.textView14);
            textView14.setTextSize(3.0f * setting.gTextDenst);
            TextView textView15 = (TextView)viewframe1.findViewById(R.id.textView15);
            textView15.setTextSize(3.0f * setting.gTextDenst);
            TextView textView16 = (TextView)viewframe1.findViewById(R.id.textView16);
            textView16.setTextSize(3.0f * setting.gTextDenst);

            TextView textView21 = (TextView)viewframe2.findViewById(R.id.textView21);
            textView21.setTextSize(4.0f * setting.gTextDenst);
        }

    }
}
