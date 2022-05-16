package com.konai.appmeter.driver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.view.MemberCertActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class IntroActivity extends Activity {

    com.konai.appmeter.driver.setting.setting setting = new setting();
    private String ResultData;
    private boolean chkRes;

    private NotificationChannel nfChannel;
    private NotificationManager nfManager;
    private NotificationChannel nfGpsChannel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); //20220207
        Info.APP_VERSION = Double.parseDouble(BuildConfig.VERSION_NAME);

//20210823         chkRes = getVersionCheck();

//20210310
        setting.setTextDest(this.getResources().getDisplayMetrics().density);

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    nfManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    int importance = NotificationManager.IMPORTANCE_DEFAULT;

                    if (nfChannel == null) {
                        nfChannel = new NotificationChannel("ch_main", "메인", importance);
                        nfChannel.setDescription("taxi_main");
                        nfChannel.enableVibration(true);
                        nfManager.createNotificationChannel(nfChannel);

                        nfGpsChannel = new NotificationChannel("foreground", "gps", importance);
                        nfGpsChannel.setDescription("taxi_main");
                        nfGpsChannel.enableVibration(true);
                        nfManager.createNotificationChannel(nfGpsChannel);
                    }
                }

//                if(chkRes) {
                if(true) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), MemberCertActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                }

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

//20220207
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            TedPermission.with(this)
                    .setPermissionListener(permissionListener)
                    .setRationaleTitle("권한 설정")
                    .setRationaleMessage("몇몇 권한이 필요합니다.")
                    .setDeniedTitle("권한 거부")
                    .setDeniedMessage("거부완료")
                    .setDeniedCloseButtonText("앱종료")
                    .setGotoSettingButtonText("설정하기")
                    .setPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.READ_PHONE_NUMBERS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT

                    ).check();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            TedPermission.with(this)
                    .setPermissionListener(permissionListener)
                    .setRationaleTitle("권한 설정")
                    .setRationaleMessage("몇몇 권한이 필요합니다.")
                    .setDeniedTitle("권한 거부")
                    .setDeniedMessage("거부완료")
                    .setDeniedCloseButtonText("앱종료")
                    .setGotoSettingButtonText("설정하기")
                    .setPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.READ_PHONE_NUMBERS,
                            Manifest.permission.READ_PHONE_STATE

                    ).check();
        } else {
            TedPermission.with(this)
                    .setPermissionListener(permissionListener)
                    .setRationaleTitle("권한 설정")
                    .setRationaleMessage("몇몇 권한이 필요합니다.")
                    .setDeniedTitle("권한 거부")
                    .setDeniedMessage("거부완료")
                    .setDeniedCloseButtonText("앱종료")
                    .setGotoSettingButtonText("설정하기")
                    .setPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE

                    ).check();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public boolean getVersionCheck() {
        Thread NetworkThreads = new Thread(new NetworkThreads());
        NetworkThreads.start();

        try {
            NetworkThreads.join();
        } catch(Exception e) {
            e.printStackTrace();
        }

        String versionCheck = parsingVersion(ResultData);

        if(versionCheck.equals("n")) {
            return false;
        } else {
            return true;
        }
    }




    class NetworkThreads implements Runnable {
        public void run() {

            StringBuilder jsonData = new StringBuilder();

            try {
                URL url = new URL(setting.BASEDOMAIN + "versionChk");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                if(conn != null) {
                    conn.setConnectTimeout(2000);
                    conn.setUseCaches(false);

                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader((conn.getInputStream()), "UTF-8"));

                        for(;;) {
                            String line = br.readLine();

                            if(line == null) {
                                break;
                            }
                            jsonData.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                    ResultData = jsonData.toString();
                    Log.e("jsons", ResultData);
                } else {
                    ResultData = "Fail";
                }

            } catch(Exception e) {
                ResultData = "Fail";
                e.printStackTrace();
            }
        }
    }

    public String parsingVersion(String json) {
        String certRes = "";
        if(json.equals("Fail"))
        {

            return "n";

        }

        try {
            Log.e("json", json);
            JSONObject jsonObject = new JSONObject(json);
            String cert = jsonObject.getString("version");
            if(cert.equals("0")) {
                certRes = "n";
            } else {
                certRes = "y";
                Info.SV_APP_VERSION = Integer.parseInt(cert);

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY, "업데이트버전 " + Info.SV_APP_VERSION, "");

                }
            }

        } catch(Exception e) {
            certRes = "n";
            e.printStackTrace();
        }
        return certRes;
    }

}
