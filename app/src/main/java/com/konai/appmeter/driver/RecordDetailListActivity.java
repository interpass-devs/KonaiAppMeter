package com.konai.appmeter.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.widget.DrawerLayout;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.konai.appmeter.driver.Adapter.ListItem;
import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.VO.RecordVO;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordDetailListActivity extends Activity {
    //DB SQLite
    SQLiteHelper mSQLiteHelper;
    SQLiteControl sqlite;
    private ArrayList<RecordVO> copyRec;
    private String[] recordList;

    public static LocService m_Service = null;
    TextView payDate, stDate, edDate, dist, drvCharge, addCharge, totalCharge, backBtn, drvPayDiv;
    public String mCoordsX, mCoordsY;

    //Navigation Menu
    public Context context;
    DrawerLayout menu;
    View drawerView;
    Button menuBtn, menu_endDrv, menu_endApp, menu_update, menu_close, goMainBtn, mapBtn;
    TextView totalChargeTv, payDateTv, stDateTv, edDateTv, distTv, drvChargeTv, drvPayDayTv, addChargeTv;
    LinearLayout menu_home, menu_drvHistory, menu_submenu, menu_setting, menu_menualpay, menu_getReceipt, menu_cancelPay;
    TextView menu_todayRecord, menu_yesterdayRecord, menu_allRecord;
    Boolean isClicked = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initialzedcontents(getResources().getConfiguration().orientation);
        initialzedcontents(setting.gOrient);

        context = this;
       // getActionBar().hide();
        //Log.d("qq", getIntent().getStringExtra("coordsX")+", "+getIntent().getStringExtra("coordsY"));

        //text
        totalChargeTv = (TextView) findViewById(R.id.textView21);
        payDateTv = (TextView) findViewById(R.id.tv_pay_date_title);
        stDateTv = (TextView) findViewById(R.id.tv_dtlstarttime_title);
        edDateTv = (TextView) findViewById(R.id.tv_dtlendtime_title);
        distTv = (TextView) findViewById(R.id.tv_dist_title);
        drvChargeTv = (TextView) findViewById(R.id.tv_dtlpayment_title);
        addChargeTv = (TextView) findViewById(R.id.tv_add_charge_title);
        drvPayDayTv = (TextView) findViewById(R.id.drv_pay_div_title);

        //value
        payDate = (TextView) findViewById(R.id.tv_pay_date);
        stDate = (TextView) findViewById(R.id.tv_dtlstarttime);
        edDate = (TextView) findViewById(R.id.tv_dtlendtime);
        dist = (TextView) findViewById(R.id.tv_dist);
        drvCharge = (TextView) findViewById(R.id.tv_dtlpayment);
        addCharge = (TextView) findViewById(R.id.tv_add_charge);
        totalCharge = (TextView) findViewById(R.id.tv_total_charge);
        drvPayDiv = (TextView) findViewById(R.id.drv_pay_div);

        backBtn = (TextView) findViewById(R.id.ltv_back);
        menu = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuBtn = (Button) findViewById(R.id.lbtn_menu);
        drawerView = (View) findViewById(R.id.drawer_menu);
        menu_home = (LinearLayout) findViewById(R.id.menu_home);
        menu_drvHistory = (LinearLayout)findViewById(R.id.menu_drvhistory);
        menu_submenu = (LinearLayout)findViewById(R.id.menu_submenu);
        menu_setting = (LinearLayout)findViewById(R.id.menu_setting);
        menu_menualpay = (LinearLayout)findViewById(R.id.menu_menualpay);
        menu_getReceipt = (LinearLayout)findViewById(R.id.menu_getreceipt);
        menu_cancelPay = (LinearLayout)findViewById(R.id.menu_cancelpay);

        menu_endDrv = (Button)findViewById(R.id.menu_enddrv);
        menu_endApp = (Button)findViewById(R.id.menu_endapp);
        menu_update = (Button)findViewById(R.id.menu_update);
        menu_close = (Button)findViewById(R.id.menu_btnclose);

        menu_todayRecord = (TextView)findViewById(R.id.menu_todayrecord);
        menu_yesterdayRecord = (TextView)findViewById(R.id.menu_yestrecord);
        menu_allRecord = (TextView)findViewById(R.id.menu_allrecord);

        //네비게이션 메뉴 생성
        NavigationMenu();

        //뒤로가기 버튼
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        //세부내역 데이터
        Intent intent = getIntent();
        String drvCodeVal = intent.getStringExtra("drvCodeVal");
       // Log.d("drvCodeVal", drvCodeVal+"");

        mSQLiteHelper = new SQLiteHelper(context);
        sqlite = new SQLiteControl(mSQLiteHelper);

        recordList = sqlite.selectedRecordDetail( drvCodeVal );

        ArrayList<RecordVO> records = new ArrayList<>();

        for (int i=0; i<recordList.length; i++){
            String[] splt = recordList[i].split("#");

            RecordVO record = new RecordVO();

            if (splt.length >= 9){
                record.setDrvCode(splt[0]);
                record.setDrvDivision(Integer.parseInt(splt[1]));
                record.setDrvPay(Integer.parseInt(splt[2]));
                record.setDrvPayDivision(Integer.parseInt(splt[3]));
                record.setAddPay(Integer.parseInt(splt[4]));
                record.setCoordsX(splt[5]);
                record.setCoordsY(splt[6]);
                record.setSdate(splt[7]);
                record.setEdate(splt[8]);
                record.setDistance(splt[9]);

               // copyRec.add(record);
                records.add(record);

                if (record.getDrvPayDivision() == 0){drvPayDiv.setText("현금결제");}
                else {drvPayDiv.setText("카드결제");}

                if (record.geteDate().equals("-")){
                    payDate.setText("-");
                }else {
                    payDate.setText(record.geteDate().substring(0,10)+"");
                }

                stDate.setText(record.getsDate()+"");
                edDate.setText(record.geteDate()+"");
                dist.setText(String.format("%.2f", Double.parseDouble(record.getDistance()) / 1000.0) +"km");
                drvCharge.setText(record.getDrvPay()+" 원");
                addCharge.setText(record.getAddPay()+" 원");
                mCoordsX = record.getCoordsX();
                mCoordsY = record.getCoordsY();

                String mDrvPay = record.getDrvPay()+"";
                String mAddPay = record.getAddPay()+"";

                totalCharge.setText(Integer.parseInt(mDrvPay)+Integer.parseInt(mAddPay)+" 원");
               // Log.d("total_charge_1", totalCharge.getText().toString());
            }
        }

        mapBtn = (Button) findViewById(R.id.map_btn);
//        mapBtn.setVisibility(View.GONE);
/**
        mapBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    mapBtn.setBackgroundResource(R.drawable.ok_btn_blue_round_clicked_bg);
                }
                if (event.getAction() == MotionEvent.ACTION_UP){
                    mapBtn.setBackgroundResource(R.drawable.ok_btn_blue_round_bg);
                }

                return false;
            }
        });


        // 발생지역 버튼 (지도)
        if (mCoordsX != null && mCoordsY != null){

            mapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.putExtra("coordsX", mCoordsX);
                    intent.putExtra("coordsY", mCoordsY);
                    startActivity(intent);
                }
            });
        }else {
            Toast.makeText(context, "위치 데이터 없음", Toast.LENGTH_SHORT).show();
        }
**/


        //홈버튼
        goMainBtn = (Button) findViewById(R.id.gomain);
        goMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish();
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
            }
        });



//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        if (setting.gOrient == Configuration.ORIENTATION_PORTRAIT)
        {

            set_frame_orient(0);

        }
        else
            set_frame_orient(1);

    }//onCreate..

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

    // Orientation 확인
    private void initialzedcontents(int nTP){
        Log.d("check_ntp::", nTP+"");

        if (nTP == Configuration.ORIENTATION_PORTRAIT){   //1
            Log.d("check_ntp_1", nTP+"");
            setContentView(R.layout.activity_record_detail_list_v);

        }else {
            Log.d("check_ntp_2", nTP+"");
//            setContentView(R.layout.activity_record_detail_list_v);
            setContentView(R.layout.activity_record_detail_list_h);  //todo: 가로버전 생성

        }

    }

    private void set_frame_orient(int tp)
    {
        if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
        {

            Log.d("tp","2");

        }
        else if(tp == 1)
        {
            Log.d("tp","1");
            backBtn.setTextSize(3.0f * setting.gTextDenst);

            TextView textView9 = (TextView)findViewById(R.id.textView9);
            textView9.setTextSize(4.0f * setting.gTextDenst);

            totalChargeTv.setTextSize(4.0f * setting.gTextDenst);
            totalCharge.setTextSize(6.0f * setting.gTextDenst);
            goMainBtn.setTextSize(5.0f * setting.gTextDenst);
            mapBtn.setTextSize(5.0f * setting.gTextDenst);

            payDateTv.setTextSize(2.5f * setting.gTextDenst);
            payDate.setTextSize(2.5f * setting.gTextDenst);
            stDateTv.setTextSize(2.5f * setting.gTextDenst);
            stDate.setTextSize(2.5f * setting.gTextDenst);
            edDateTv.setTextSize(2.5f * setting.gTextDenst);
            edDate.setTextSize(2.5f * setting.gTextDenst);
            distTv.setTextSize(2.5f * setting.gTextDenst);
            dist.setTextSize(2.5f * setting.gTextDenst);
            drvChargeTv.setTextSize(2.5f * setting.gTextDenst);
            drvCharge.setTextSize(2.5f * setting.gTextDenst);
            addChargeTv.setTextSize(2.5f * setting.gTextDenst);
            addCharge.setTextSize(2.5f * setting.gTextDenst);
            drvPayDayTv.setTextSize(2.5f * setting.gTextDenst);
            drvPayDiv.setTextSize(2.5f * setting.gTextDenst);

        }

    }

    private void RecordDetailVal(){

    }


    //네비게이션 메뉴
    private void NavigationMenu() {
        menuBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    menuBtn.setBackgroundResource(R.drawable.btn_menus_c);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    menuBtn.setBackgroundResource(R.drawable.btn_menus);
                }
                return false;
            }
        });

        //메뉴 버튼
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "메뉴 버튼", Toast.LENGTH_SHORT).show();
                menu.openDrawer(drawerView);
            }
        });

        menu_home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_home.setBackgroundResource(R.drawable.shadow_menu);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_home.setBackgroundResource(R.drawable.menu_borders);}
                return false;
            }
        });


        menu_drvHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_drvHistory.setBackgroundResource(R.drawable.shadow_menu);}
                if (event.getAction() == MotionEvent.ACTION_UP){}
                return false;
            }
        });

        //메뉴= 거래내역
        menu_drvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClicked == true){
                    menu_submenu.setVisibility(View.VISIBLE);
                    isClicked = false;
                }else {
                    menu_submenu.setVisibility(View.GONE);
                    isClicked = true;
                }
            }
        });

        //메뉴- 당일 거래 내역
        menu_todayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DayReportActivity.class);
                startActivity(i);
            }
        });

        //메뉴- 통합 운행 정보
        menu_yesterdayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DriveInfoActivity.class);
                startActivity(i);
            }
        });

        //메뉴- 거래 내역 조회
        menu_allRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PayListActivity.class);
                startActivity(i);
            }
        });

        menu_setting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_setting.setBackgroundResource(R.drawable.shadow_menu);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_setting.setBackgroundResource(R.drawable.menu_borders);}
                return false;
            }
        });

        menu_close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_close.setBackgroundResource(R.drawable.nbtn_exit_c);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_close.setBackgroundResource(R.drawable.nbtn_exit);}
                return false;
            }
        });

        //메뉴 닫기버튼
        menu_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.closeDrawer(drawerView);
            }
        });

        //메뉴- 정보
        menu_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoActivity.class);
                startActivity(intent);
            }
        });

        //메뉴- 설정
        menu_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: set up bluetooth
            }
        });

        //메뉴- 수기결제
        menu_menualpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: get menual fare
            }
        });

        menu_getReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_getReceipt.setBackgroundResource(R.drawable.shadow_effect);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_getReceipt.setBackgroundResource(R.drawable.non_shadow_effect);}
                return false;
            }
        });

        //메뉴- 영수증 출력
        menu_getReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if(AMBlestruct.AMCardResult.msOpercode.equals(""))
                {
                    Info.makeDriveCode();
                    AMBlestruct.AMCardResult.msOpercode = Info.g_nowKeyCode;
                }
                m_Service.writeBLE("26");

                m_Service.SendTIMS_Data(2, 0, null, "10&0"); */

                menu.closeDrawer(drawerView);
            }
        });

        menu_cancelPay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_cancelPay.setBackgroundResource(R.drawable.shadow_effect);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_cancelPay.setBackgroundResource(R.drawable.non_shadow_effect);}
                return false;
            }
        });

        //메뉴- 결제취소
        menu_cancelPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        menu_endDrv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_endDrv.getResources().getColor(R.color.menu_endDrv_act_down);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_endDrv.getResources().getColor(R.color.menu_endDrv_act_up);}
                return false;
            }
        });

        //메뉴- 금일 영업 마감
        menu_endDrv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: 앱 자꾸 종료됨
              /*  if (Info.TIMSUSE){
                    if (Info.APPMETERRUNSTOP == 0){
                        Info.APPMETERRUNSTOP = 1;
                        menu_endDrv.setBackgroundColor(context.getColor(R.color.menu_endDrv_act_down_1));
                    }else {
                        Info.APPMETERRUNSTOP = 0;
                        menu_endDrv.setBackgroundColor(context.getColor(R.color.menu_endDrv_act_up));
                    }
                }else {
                    if (Info.APPMETERRUNSTOP == 0){
                        Info.APPMETERRUNSTOP = 1;
                        menu_endDrv.setBackgroundColor(context.getColor(R.color.menu_endDrv_act_down_1));
                        m_Service.SendTIMS_Data(3, 2, null, "0");     //todo: null object reference ..
                    }else {
                        Info.APPMETERRUNSTOP = 0;
                        menu_endDrv.setBackgroundColor(context.getColor(R.color.menu_endDrv_act_up));
                        m_Service.SendTIMS_Data(3, 2, null, "1");
                    }
                }*/
            }//onClick..
        });

        menu_endApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){menu_endApp.getResources().getColor(R.color.menu_endApp_act_down);}
                if (event.getAction() == MotionEvent.ACTION_UP){menu_endApp.getResources().getColor(R.color.menu_endApp_act_up);}
                return false;
            }
        });

        //메뉴- 앱종료
        menu_endApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                if (Info.REPORTREADY){
                    if (Info.REPORTREADY){Info._displayLOG(true, "이전상태 빈차 - 대기 변경", "");}
                    Info._displayLOG(true, "앱종료 버튼, 앱종료 ", "");
                }

                if (m_Service != null){
                    //unbindService();
                }
                 **/
            }//onclick..

        });

    }//NavigationMenu()..





    //뒤로가기 버튼
    @Override
    public void onBackPressed() {
        menu.closeDrawer(drawerView);
        finish();
    }


}//RecordDetailListActivity..
