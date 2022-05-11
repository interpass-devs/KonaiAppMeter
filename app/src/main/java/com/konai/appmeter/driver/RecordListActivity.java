package com.konai.appmeter.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.konai.appmeter.driver.Adapter.ListAdapter;
import com.konai.appmeter.driver.Adapter.ListItem;
import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.VO.RecordVO;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordListActivity extends Activity {

    private GridView mRecordList = null;
    private int cnt;
    Context context;

    //DB (SQLITE)
    SQLiteHelper mSQLiteHelper;
    SQLiteControl sqlite;
    private ArrayList<RecordVO> copyRec;
    private String[] recordList;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private TextView tv_date_drvCnt, tv_back, cashPay, cardPay, textView9;
    private Button btn_menu;
    public String drvCodeVal, startDateVal, endDateVal, drvPayVal, addPayVal, distanceVal, drvPayDivisionVal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordlist);

        Log.d("상세내역", "tttt");

        context = this;

        pref = getSharedPreferences("tfare", Activity.MODE_PRIVATE);

        tv_date_drvCnt = (TextView)findViewById(R.id.rtv_nowdate);
        tv_back = (TextView)findViewById(R.id.ltv_back);
        btn_menu = (Button)findViewById(R.id.lbtn_menu);
        textView9 = (TextView)findViewById(R.id.textView9);

        tv_back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tv_back.setTextColor(Color.parseColor("#999999"));
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    tv_back.setTextColor(Color.parseColor("#000000"));
                }
                return false;
            }
        });

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_menu.setBackgroundResource(R.drawable.btn_menus_c);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn_menu.setBackgroundResource(R.drawable.btn_menus);
                }
                return false;
            }
        });

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
        String driveCount = Info.sqlite.todayDriveCount();

        Intent intent = getIntent();
        String sDate = intent.getStringExtra("SDATE");

        mSQLiteHelper = new SQLiteHelper(this);
        sqlite = new SQLiteControl(mSQLiteHelper);

        try{

            if(sDate.equals("t")) {   //me: 당일거래 일 경우
                // tv_date_drvCnt.setText(transFormat.format(time) + " / 운행 : " + driveCount + " 회");
                recordList = sqlite.selectToday();
            } else {                  //me: 종합거래 일 경우
                // tv_date_drvCnt.setText(driveCount + " 회");    //todo: 숫자가 안맞음..
                recordList = sqlite.select();
            }

        }catch (Exception e){

        }


        ArrayList<RecordVO> records = new ArrayList<>();
        copyRec = new ArrayList<>();
        //Log.e("records : " , "1번째" + recordList[1]);

        try{

            for(int i=0; i<recordList.length; i++) {
                Log.e("records : " , recordList[i] + "     ");

                String[] splt = recordList[i].split("#");

                RecordVO record = new RecordVO();

                //Log.e("pay res", splt[2]);

                if(splt.length >= 9) { //tra..sh 20200731
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

                    //   Log.d("drvCodeVal", record.getDrvCode()+"");

                    copyRec.add(record);
                    records.add(record);
                }
            }
        }catch (Exception e){

        }


        final ArrayList<ListItem> itemData = new ArrayList<>();

        try{

            for(int k=0; k<records.size(); k++) {
                ListItem item = new ListItem();
                item.drvCode = records.get(k).getDrvCode();
                item.drvDivision = records.get(k).getDrvDivision();  //할증여부
                item.drvPay = records.get(k).getDrvPay();
                item.addPay = records.get(k).getAddPay();
                item.drvPayDivision = records.get(k).getDrvPayDivision();  //결제방법
                item.sDate = records.get(k).getsDate();
                item.eDate = records.get(k).geteDate();
                item.distance = Integer.parseInt(records.get(k).getDistance()); //20211103

                // drvCodeVal = records.get(k).getDrvCode();    //00000005 만 나옴 -> 리스트 포리션을 찾아줘야 함
                startDateVal = records.get(k).getsDate();
                endDateVal = records.get(k).getDrvCode();
                addPayVal = records.get(k).getAddPay()+"";
//            drvPayVal = records.get(k).getAddPay()+"";
                drvPayVal = records.get(k).getDrvPay()+"";
                distanceVal = records.get(k).getDistance()+"";
//            Log.d("distanceVal", records.get(k).getDistance()+"");


                if (item.drvPayDivision == 0){drvPayDivisionVal = "현금결제";}
                else {drvPayDivisionVal = "카드결제";}

                String mDistanceCheck = records.get(k).getDistance();
//            Log.d("88888888",mDistanceCheck+"");  //null

                itemData.add(item);
            }

        }catch (Exception e){

        }


        mRecordList = (GridView)findViewById(R.id.lv_record);
        final ListAdapter adapter = new ListAdapter(context, itemData);
        mRecordList.setAdapter(adapter);

        if (Build.VERSION.SDK_INT <= 25){
            //리스트뷰 그리드뷰로 바꾸기
            Log.d("상세내역1","25");
            mRecordList.setNumColumns(2);
        }else {

            mRecordList.setNumColumns(1);
            tv_date_drvCnt.setTextSize(18);
            textView9.setTextSize(25);
        }

        //todo: 20211203
        //todo: 다시다시
        mRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 요금 세부내역 화면으로 전달

                Intent i = new Intent(getApplicationContext(), RecordDetailListActivity.class);
                i.putExtra("SDATE","t");
                drvCodeVal = itemData.get(position).drvCode;
                i.putExtra("drvCodeVal",drvCodeVal);  //운행코드
                startActivity(i);

            }
        });
        //todo: end




        //todo: 20211124

        //Log.d("recordListSize", adapter.getCount()+"@@@");
        //me: 운행횟수
        if(sDate.equals("t")) {   //me: 당일거래 일 경우
            Log.d("drv_cnt_total", transFormat.format(time)+", "+ adapter.getCount());
//             tv_date_drvCnt.setText(transFormat.format(time) + " / 운행 : " + adapter.getCount() + " 회");
             Log.d("final_cnt", adapter.return_cnt()+"");
            Log.d("drv_cnt_with_cancel", transFormat.format(time)+", "+ adapter.return_cnt());
            tv_date_drvCnt.setText(transFormat.format(time) + " / 운행 : " + adapter.return_cnt() + " 회");
        } else {                  //me: 종합거래 일 경우
             tv_date_drvCnt.setText(adapter.return_cnt() + " 회");
        }

        Log.d("check_mtcnt", (pref.getInt("tcnt",0))+"");

        //todo: end

        /*final ImageButton Btnhome = (ImageButton) findViewById(R.id.Btnhome);

        Btnhome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent actIntent = Info.g_MainIntent;

                actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(actIntent);

                finish();
            }
        });*/

       // for(int i=0;)

//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

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

    private void initializecontents(int nTP) {
        if (nTP == Configuration.ORIENTATION_PORTRAIT) {

            set_frame_orient(0);

        } else {

            set_frame_orient(1);
        }
    }

    private void set_frame_orient(int tp)
    {
        if (Build.VERSION.SDK_INT >= 26) //20210823 8.0
        {

            ;

        }
        else if(tp == 1)
        {
            tv_date_drvCnt.setTextSize(3.0f * setting.gTextDenst);
            tv_back.setTextSize(3.0f * setting.gTextDenst);


            textView9.setTextSize(3.5f * setting.gTextDenst);

        }

    }
}
