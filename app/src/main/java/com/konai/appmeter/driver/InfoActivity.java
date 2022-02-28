package com.konai.appmeter.driver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.LocationManager;
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
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InfoActivity extends Activity {

    private TextView textView9, rtv_name, phone_title, unique_title, carno_title, company_title, gps_title, bluetooth_title, obd_title, osver_title, maker_title;  //todo: 20210902
    private TextView tv_name;
    private TextView tv_phoneno;
    private TextView tv_unique;
    private TextView tv_carno;
    private TextView tv_company;
    private TextView tv_gps;
    private TextView tv_bluetooth;
    private TextView tv_obd;
    private TextView tv_osver;
    private TextView tv_maker;
    private TextView tv_version;
    private TextView area_name;
    private TextView basic_fare;
    private TextView basic_dist;
    private TextView after_fare;
    private Button btn_goMenu;
    private Button btn_tims;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private TextView tv_area_name, tv_base_cost, tv_base_drvdist, tv_dist_cost, tv_interval_dist, tv_night_time_rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initializecontents(getResources().getConfiguration().orientation);
        initializecontents(setting.gOrient);

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

//		Log.i("TAg", "---------- screenWidthDp : " + newConfig.screenWidthDp + ", screenHeightDp : " + newConfig.screenHeightDp);

    }

    private void initializecontents(int nTP)
    {
        if(nTP == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_info);  //세로
            set_frame_orient(0);
        }
        else
        {
            setContentView(R.layout.activity_info_h);  //가로   //todo: 20210902
            set_frame_orient(1);
        }
        tv_area_name.setText(AMBlestruct.AMLicense.taxinumber.substring(0,2));
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
        if (!BluetoothAdapter.isEnabled())
        {
            tv_bluetooth.setText("OFF");
        }
        else
            tv_bluetooth.setText("ON");

        if(AMBlestruct.mBTConnected)
        {
            tv_obd.setText("연결됨");
        }
        else
            tv_obd.setText("연결안됨");

        tv_osver.setText(Build.VERSION.RELEASE + "\n" + Build.MODEL + "\n" + Build.MANUFACTURER);

        tv_maker.setText("Konai");
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
                finish();
            }
        });

        btn_tims.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn_tims.setBackgroundColor(Color.parseColor("#2e2e6a"));
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn_tims.setBackgroundColor(Color.parseColor("#2e2eae"));
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

    }

    private void set_frame_orient(int tp)
    {
//////////////////////
//todo: 20210902
        textView9 = (TextView) findViewById(R.id.textView9);
        rtv_name = (TextView)findViewById(R.id.rtv_name);
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

////////////////////////

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
        //todo: end

    }

//    private String getBuildtime() {
//        Date buildDate = new Date(BuildConfig.TIMESTAMP);
//        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
//        return transFormat.format(buildDate);
//    }
}
