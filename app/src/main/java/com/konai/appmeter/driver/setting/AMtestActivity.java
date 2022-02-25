package com.konai.appmeter.driver.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.R;

public class AMtestActivity extends Activity
{

    boolean mbFare = false;
    int mFare = 3000;
    boolean bExit = false;

    int mState = 0;

    boolean mSign = true;

    TextView mtextinfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.amtest);


        final Button btnempty = (Button)findViewById(R.id.btnempty);
        btnempty.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Info.m_Service.update_BLEmeterstate("05");
                mbFare = false;
                mState = 1;

            }
        });

        final Button btnboard = (Button)findViewById(R.id.btnboard);
        btnboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Info.m_Service.update_BLEmeterstate("20");

                mbFare = true;
                mState = 2;

            }
        });

        final Button btnconfig = (Button)findViewById(R.id.btnconfig);
        btnconfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent actIntent = new Intent(getApplicationContext(),
                        AMBleConfigActivity.class);

                actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(actIntent);

            }
        });

        final Button btnappoint = (Button)findViewById(R.id.btnappoint);
        btnappoint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mSign)
                {

                    Info.m_Service.update_BLEmeterstate("40");

                }
                else
                    Info.m_Service.update_BLEmeterstate("50");

                    mSign = !mSign;

            }
        });

        mtextinfo = (TextView)findViewById(R.id.textinfo);

        final Button btnfare = (Button)findViewById(R.id.btnfare);
        btnfare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Info.m_Service.writeBLE("11");

            }
        });

        final Button btn14 = (Button)findViewById(R.id.btn14);
        btn14.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Info.m_Service.writeBLE("14");

            }
        });

        final Button btnarrival = (Button)findViewById(R.id.btnarrival);
        btnarrival.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mbFare = false;
                Info.m_Service.writeBLE("20");
                Info.m_Service.update_BLEmeterstate("01");

            }
        });

        final Button btncard = (Button)findViewById(R.id.btncard);
        btncard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mbFare = false;
//                Info.m_Service.writeBLE("23");
                Info.m_Service.writeBLE("21");
///               Info.m_Service.writeBLE("26");

            }
        });

        final Button btncancel = (Button)findViewById(R.id.btncancel);
        btncancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mbFare = false;
                Info.m_Service.writeBLE("23");
///               Info.m_Service.writeBLE("26");

            }
        });

        final ImageButton Btnhome = (ImageButton)findViewById(R.id.Btnhome);
        Btnhome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

//                Intent actIntent = new Intent(getApplicationContext(),
///                        MainActivity.class);

                Intent actIntent = Info.g_MainIntent;

                actIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(actIntent);

                finish();
            }
        });

        new Thread(new faresendThread())
                .start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bExit = true;

    }

    @Override
    public void onPause() {
        super.onPause();


    }

    public Handler statedisplay = new Handler() {
        //		@Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            if (msg.what == 1) {

                Info.m_Service.connectAM();
            }
            else if(msg.what == 2)
            {
                if(AMBlestruct.mRState.equals("01")) //01: 지불
                {

                    mtextinfo.setText("지 불");
                    Info.m_Service.writeBLE("20");

                    Info.m_Service.update_BLEmeterstate("01");

                }
                else if(AMBlestruct.mRState.equals("05")) //05: 빈차 예약상태 등에서 빈차 상태로 변경이 되는 경우
                {

                    mtextinfo.setText("빈 차");
                    Info.m_Service.update_BLEmeterstate("05");
                }
                else if(AMBlestruct.mRState.equals("20")) //20: 주행
                {

                    mtextinfo.setText("주 행");
                    Info.m_Service.update_BLEmeterstate("20");
                }
                else if(AMBlestruct.mRState.equals("30")) //30: 할증
                {

                    mtextinfo.setText("할 증");
                    Info.m_Service.update_BLEmeterstate("30");

                }
                else if(AMBlestruct.mRState.equals("40")) //40: 예약/호출
                {
                    mtextinfo.setText("예 약");
                    Info.m_Service.update_BLEmeterstate("40");

                    if(Info.REPORTREADY)
                    {

                        Info._displayLOG(true,"예 약", "");

                    }
                }

                AMBlestruct.setRStateupdate(false);
            }
        }
    };

    class faresendThread implements Runnable {

        public void run() {

            int ncount = 0;
            while(!bExit)
            {
                if(AMBlestruct.mBTConnected == false)
                {

                    statedisplay.sendEmptyMessage(1);

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    continue;
                }

                if(AMBlestruct.mbRStateupdated)
                {

                    statedisplay.sendEmptyMessage(2);

                }

                if(mbFare)
                {

                    if(ncount == 0)
                    {
                        mFare = (mFare + 100) % 100000;

                        Info.m_Service.update_BLEmeterfare(mFare, mFare / 100, mFare % 1000);
                    }

                    ncount++;
                    ncount = ncount % 20;

                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

        }
    }

}