package com.konai.appmeter.driver.view;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.konai.appmeter.driver.R;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimsInfoActivity extends Activity {

    com.konai.appmeter.driver.setting.setting setting = new setting();

    private TextView tv_carnook;
    private TextView tv_driverok;
    private TextView tv_eventdate;
    private TextView tv_eventtype;
    private TextView tv_eventresult;
    private TextView tv_drivedate;
    private TextView tv_driveresult;
    private TextView tv_nowdate;
    private Button btn_goMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_dayreport);
//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

    }

    @Override
    protected void onPause() {
        super.onPause();
//20220503 tra..sh
        if(Info.m_Service != null)
            Info.m_Service._showhideLbsmsg(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
//20220503 tra..sh
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
            setContentView(R.layout.activity_timsinfo);
            set_frame_orient(0);

        }
        else
        {

            setContentView(R.layout.activity_timsinfo);
            set_frame_orient(1);
        }

        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
        tv_nowdate.setText(transFormat.format(time));

        tv_driverok.setText(Info.mAuthdrvTIMS);
        tv_carnook.setText(Info.mAuthVehTIMS);
        tv_eventdate.setText(Info.mEventTIMSdate);
        tv_eventtype.setText(Info.mEventTIMStype);
        tv_eventresult.setText(Info.mEventTIMSok);
        tv_drivedate.setText(Info.mDriveTIMSdate);
        tv_driveresult.setText(Info.mDriveTIMSok);

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

        tv_carnook = (TextView)findViewById(R.id.tv_carnook);
        tv_driverok = (TextView)findViewById(R.id.tv_driverok);
        tv_nowdate = (TextView)findViewById(R.id.rtv_nowdate);
        tv_eventdate = (TextView)findViewById(R.id.tv_eventdate);
        tv_eventresult = (TextView)findViewById(R.id.tv_eventresult);
        tv_eventtype = (TextView)findViewById(R.id.tv_eventtype);
        tv_drivedate = (TextView)findViewById(R.id.tv_drivedate);
        tv_driveresult = (TextView)findViewById(R.id.tv_driveresult);
        btn_goMenu = (Button)findViewById(R.id.dbtn_menu);

    }
}
