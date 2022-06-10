package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.VO.RecordVO;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PayListActivity extends Activity {

    Context context;

    private GridView mRecordList = null;
    int cnt;
    //DB (SQLITE)
    SQLiteHelper mSQLiteHelper;
    SQLiteControl sqlite;
    private ArrayList<RecordVO> copyRec;
    private String[] recordList;

    private TextView tv_date_drvCnt;
    private TextView tv_back;

    private Button btn_menu;

    public static LocService m_Service = new LocService();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordlist);

        context = this;

        tv_date_drvCnt = (TextView)findViewById(R.id.rtv_nowdate);
        tv_back = (TextView)findViewById(R.id.ltv_back);
        btn_menu = (Button)findViewById(R.id.lbtn_menu);

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

        //
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
        String driveCount = Info.sqlite.todayDriveCount();



        Intent intent = getIntent();
        String sDate = intent.getStringExtra("SDATE");

        if(sDate != null && sDate.equals("t")) {
            tv_date_drvCnt.setText(transFormat.format(time) + " / 운행 : " + driveCount + " 회");
            recordList = Info.sqlite.selectToday();
        } else {
            tv_date_drvCnt.setText(driveCount + " 회");
            recordList = Info.sqlite.select();
        }
        ArrayList<RecordVO> records = new ArrayList<>();
        copyRec = new ArrayList<>();

        //Log.e("records : " , "1번째" + recordList[1]);

        for(int i=0; i<recordList.length; i++) {
            //Log.e("records : " , recordList[i] + "     ");

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

                copyRec.add(record);
                records.add(record);
            }
        }

        ArrayList<ListItem> itemData = new ArrayList<>();

        for(int k=0; k<records.size(); k++) {
            ListItem item = new ListItem();
            item.drvCode = records.get(k).getDrvCode();
            item.drvDivision = records.get(k).getDrvDivision();
            item.drvPay = records.get(k).getDrvPay();
            item.addPay = records.get(k).getAddPay();
            item.drvPayDivision = records.get(k).getDrvPayDivision();
            item.sDate = records.get(k).getsDate();
            item.eDate = records.get(k).geteDate();

            itemData.add(item);
        }

        mRecordList = (GridView)findViewById(R.id.lv_record);
        ListAdapter adapter = new ListAdapter(context, itemData);

        mRecordList.setAdapter(adapter);

        mRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.e("select item" , copyRec.get(position).getDrvCode());
                AMBlestruct.AMCardResult.msOpercode = copyRec.get(position).getDrvCode();

                m_Service.writeBLE("26");
            }
        });

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

        if(sDate.equals("t")) {   //me: 당일거래 일 경우
            tv_date_drvCnt.setText(transFormat.format(time) + " / 운행 : " + adapter.getCount() + " 회");
        } else {                  //me: 종합거래 일 경우
            tv_date_drvCnt.setText(adapter.getCount() + " 회");
        }


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

            TextView textView9 = (TextView)findViewById(R.id.textView9);
            textView9.setTextSize(4.0f * setting.gTextDenst);

        }

    }

}
