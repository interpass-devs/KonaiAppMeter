package com.konai.appmeter.driver.tims;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.konai.appmeter.driver.DB.SQLiteControl;
import com.konai.appmeter.driver.DB.SQLiteHelper;
import com.konai.appmeter.driver.VO.TIMS_UnitVO;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.CalFareBase;
import com.konai.appmeter.driver.struct.DTGQueue;
import com.konai.appmeter.driver.struct.TIMSQueue;
import com.konai.appmeter.driver.view.InfoActivity;
import com.konai.appmeter.driver.view.MemberCertActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.LogRecord;

public class TimsDtg {

    boolean bExit = false;
    private static boolean bTimsSendFail = true; //재전송건을 보내기위함
    private static boolean bSendingTIMS = true; //전송진행중TIMS
    private String DTG_PATH = "";
    private String DTG_PARAMS = "";

    public MemberCertActivity memberCertActivity;

    private LocService mService = null;

    private String DTG_BASEURL = "http://49.50.165.75/AppMeterApi/";

//dtg
    public BlockingQueue<DTGQueue> mSendDTGQ = new ArrayBlockingQueue<DTGQueue>(5);
//tims
    public BlockingQueue<TIMSQueue> mTIMSsendQ = new ArrayBlockingQueue<TIMSQueue>(20);

    public BlockingQueue<DTGQueue> mLogSendQ = new ArrayBlockingQueue<DTGQueue>(4);

//////////////
    Thread TimsSendThread = null;
    Thread DtgSendThread = null;
    Thread TimsResendThread = null;
    Thread DtgResendThread = null;
    Thread LogSendThread = null;


    private String TIMS_BASEURL = "https://tims-help.kotsa.or.kr";
    private String TIMS_ADDURL = "";
    private String TIMS_BIZ = ":45000/app-meter/biz";
    private String TIMS_BTN = ":45000/app-meter/btn";
    private String TIMS_POWER = ":45000/app-meter/power";
    private String TIMS_VEHICLE = ":55000/app-meter/auth/vehicle?CAR_REG_NO=";
    private String TIMS_DRIVER = ":55000/app-meter/auth/driver?QUALF_NO=";


    //me: TimsDtg 의 쓰레드들은 자동으로 계속 돌아감

    public TimsDtg(LocService service)
    {
        mService = service;

        ///todo: 2022-05-20
        TimsSendThread = new Thread(new n_TIMS_Thread());  //팀스 쓰레드에.. -> 6번(연결상태) 케이스 만들어주기
        TimsSendThread.start();
        //todo: end


        if(Info.TIMSUSE) {
            Log.d("tt","tttt timsuses");
            TimsSendThread = new Thread(new n_TIMS_Thread());
            TimsSendThread.start();

            TimsResendThread = new Thread(new TimsResendThread());
            TimsResendThread.start();

        }


        if(Info.SENDDTG) {  //send DTG to interpass aka 타코
            Log.d("tt","tttt send dtg");
            DtgSendThread = new Thread(new DTG_NetworkThread());
            DtgSendThread.start();


            DtgResendThread = new Thread(new DtgResendThread());
            DtgResendThread.start();
        }

    }

    public void ThreadExit()
    {
        bExit = true;

        if(Info.TIMSUSE) {
            TimsSendThread.interrupt();
            TimsResendThread.interrupt();
        }

        if(Info.SENDDTG) {
            DtgResendThread.interrupt();
            DtgResendThread.interrupt();
        }
    }

    /////////////////////
//실패data재전송여부
    private boolean _check_Timsdate(String drvdate)
    {
        Date now = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");

        try
        {

            Date date = stTransFormat.parse(drvdate);
//            Log.d("TimsTIMS", "checktime1 " + stTransFormat.format(date).toString());
            long calDate = now.getTime() - date.getTime();
            if(calDate > (24*60*60*1000) * 3) {

                return false;
            }
        }
        catch (ParseException e)
        {

            return false;

        }

//        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
//        String strDate = "2016-10-17 18:30";
//        Date date1 = dateFormat.parse(strDate);

        return true;

    }

    /////////////////////
//첫전송성공
    public void _after_sendTIMS_ok(String url, String sdata, int event)
    {
        return;

//        Info.Deletefile(keycode + ".txt", "TIMS");
//        Info.sqlite.insertTimsdata(url, sdata, event,1);
//        Log.d("DBTEST" , "TimsTims " + Info.sqlite.selectLastTimsdata()[0] +" " + Info.sqlite.selectLastTimsdata()[3]);
    }

    /////////////////////
//재전송성공처리
    public void _after_ResendTIMS_ok(String drvdate, int subtype)
    {
        if(subtype == 1)
            Info.Deletefile(drvdate + ".tms", "TIMS");

        Info.sqlite.deleteTimsdata(drvdate);

        Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS _after_ResendTIMS_ok", drvdate + "");
//        Info.sqlite.updateTimsdata(drvdate);
//        Log.d("DBTEST" , "TimsTims _after_ResendTIMS_ok " + drvdate + " " + Info.sqlite.selectTimsResenddata()[0] + " " + Info.sqlite.selectTimsResenddata()[2]);
    }

    /////////////////////
//첫전송실패처리
    public void _after_sendTIMS_fail(String url, String sdata, int event)
    {
        String drvdate;
        if(event == 1) //영업정보. 파일저장로 저장함
        {
            drvdate = Info.sqlite.insertTimsdata(url, "", event, 0);
            Info.Savedata(drvdate + ".tms", sdata, "TIMS");
        }
        else //버튼정보.
        {
            Info.sqlite.insertTimsdata(url, sdata, event, 0);
        }


//        Info.Savedata(Info.g_nowKeyCode + ".txt", Info.sqlite.selectLastTimsdata()[4], "appmeter");
//        Log.d("TimsTims", "전송실패DB저장 " + Info.sqlite.selectLastTimsdata()[0] +" " + Info.sqlite.selectLastTimsdata()[3]);
    }

/////////////////////
//첫전송성공
    public void _after_sendDTG_ok(String keycode, String sdata)
    {
//        Info.Deletefile(keycode + ".txt", "DTG");
        Info.sqlite.insertDtgdata(keycode, sdata, 1);
//        Log.d("DBTEST" , "TimsDtg " + Info.sqlite.selectLastDtgdata()[0] +" " + Info.sqlite.selectLastDtgdata()[3]);
    }

/////////////////////
//재전송성공처리
    public void _after_ResendDTG_ok(String drvdate)
    {
//        Info.Deletefile(keycode + ".txt", "DTG");
        Info.sqlite.updateDtgdata(drvdate);
//        Log.d("DBTEST" , "TimsDtg _after_ResendDTG_ok " + drvdate + " " + Info.sqlite.selectDtgResenddata()[0] + " " + Info.sqlite.selectDtgResenddata()[2]);
    }

/////////////////////
//첫전송실패처리
    public void _after_sendDTG_fail(String keycode, String sdata)
    {
//        Info.Savedata(keycode + ".txt", sdata, "DTG");
        Info.sqlite.insertDtgdata(keycode, sdata, 0);
//        Log.d("TimsDtg", "전송실패DB저장 " + Info.sqlite.selectLastDtgdata()[0] +" " + Info.sqlite.selectLastDtgdata()[3]);
    }


    synchronized public void add_TIMSQueue(TIMSQueue que) {

        if(bTimsSendFail && que.mResend == false && que.mSendType == 1)
        {
            //전송실패는 파일보관하고 전송하지않음 재전송진행해야함
            _after_sendTIMS_fail(que.mURLs, que.mData, que.mSubType);
            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS TIMS전송direct 재전송DB입력", "");



//            Log.d("finalObj_que", que.mURLs);
//            Log.d("finalObj_que", que.mData);
//            Log.d("finalObj_que", que.mdrvtime);
//            Log.d("finalObj_que", que.mResend+"");  //
//            Log.d("finalObj_que", que.mSubType+"");
//            Log.d("finalObj_que", que.mSendType+"");  //
//            Log.d("finalObj_que", que.mtrytime+"");

            return;
        }


        //연결상태 case 6 은 이 2 조건문을 태워야함
        if (mTIMSsendQ.remainingCapacity() > 0)

//            Log.d("finalObj_capacity > 0", mTIMSsendQ.toString());

            mTIMSsendQ.add(que);



    }


    synchronized public void add_DTGQueue(DTGQueue que) {

        if (mSendDTGQ.remainingCapacity() > 0)
            mSendDTGQ.add(que);
    }

//=======================================//
//by error Resend Thread.
//=======================================//
//////////////////
    class TimsResendThread implements Runnable { //20201112

        public void run() {
            String[] ssecode = new String[5];

            try {

                Thread.sleep(2000);

                ssecode = Info.sqlite.selectTimsResenddata();
                if(ssecode[0].equals("FAIL") == true)
                    bTimsSendFail = false; //재전송건없으면

                Thread.sleep(8000);

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (Exception e)
            {
                ;
            }

            while(!Thread.currentThread().isInterrupted())
            {

                try {
                    ssecode = Info.sqlite.selectTimsResenddata();
                    if(ssecode[0].equals("FAIL") == false)
                    {
                        if(_check_Timsdate(ssecode[0]) == false)
                        {

                            _after_ResendTIMS_ok(ssecode[0], 1);

                            Thread.sleep(1000);
    //                            Log.d("TimsTIMS", "checktime2 " + ssecode[0]);
                            continue;
                        }

                        if(_check_sending_TIMS(true, true) == true)
                        {

                            Thread.sleep(3000); //22020527
                            continue;
                        }

                        bTimsSendFail = true; //재전송건있으면 먼저보내야함

                        TIMSQueue que = new TIMSQueue();
                        que.mURLs = ssecode[1];
                        que.mSendType = 1;
                        try {
                            que.mSubType = Integer.parseInt(ssecode[3]);
                        }
                        catch (Exception e)
                        {
                            _after_ResendTIMS_ok(ssecode[0], 1);
                            Thread.sleep(3000); //22020527
                            continue;
                        }
                        if(que.mSubType == 1) {
                            que.mData = Info.ReadTextFile(ssecode[0] + ".tms", "TIMS");
                            if(que.mData.equals("noFile") == true)
                                _after_ResendTIMS_ok(ssecode[0], 1);

                            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS 재전송 영업정보size", que.mData.length() + " " + ssecode[0]);

    //                            Log.d("TimsTIMS2", "재전송 영업정보size "  + que.mData.length() + " " + que.mData);
                        }
                        else {
                            que.mData = ssecode[4];
    //                            Log.d("TimsTIMS2", " "  + que.mData.length() + " " + que.mData);
                        }
                        que.mResend = true;
                        que.mdrvtime = ssecode[0];
                        add_TIMSQueue(que);

                        Thread.sleep(5000); //20220527
                    }
                    else
                    {
                        bTimsSendFail = false;
                        Thread.sleep(20000);

                    }

    //                    Log.d("TimsTIMS0", "check1 ");

                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
    //                    Log.d("TimsTIMS2", "false");

                    try {

                        Thread.sleep(20000);

                    }
                    catch (Exception e)
                    {
                        ;
                    }

                }
                catch (Exception e)
                {
                    try {

                        Thread.sleep(20000);

                    }
                    catch (Exception f)
                    {
                        ;
                    }

    //                    Log.d("TimsTIMS0", "check2 ");
                }

                if(bExit)
                    break;
            }

        }
    }

    class DtgResendThread implements Runnable { //20201112

        public void run() {
            String[] ssecode = new String[4];

            try {
                Thread.sleep(5000);

                Info.sqlite.deleteDtgdata();
                Info.sqlite.updateDtgdataClear();
                Info.sqlite.deleteConnStatus();

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (Exception e)
            {
                ;
            }

            while(!Thread.currentThread().isInterrupted())
            {
                try {

                    ssecode = Info.sqlite.selectDtgResenddata();
                    if(ssecode[0].equals("FAIL") == false)
                    {
                        DTGQueue que = new DTGQueue();
                        que.mKeycode = ssecode[1];
                        que.mData = ssecode[3];
                        que.mSendType = 1;
                        que.mSubType = 1;
                        que.mResend = true;
                        que.mdrvtime = ssecode[0];
                        add_DTGQueue(que);
                    }

                    Thread.sleep(20000);
//                    Log.d("DBTEST", Info.g_nowKeyCode + " " + Info.sqlite.selectDtgdata()[0] + Info.sqlite.selectDtgdata()[1] + Info.sqlite.selectDtgdata()[3]);

                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                if(bExit)
                    break;
            }

        }
    }

///////////////////////
//for resend중 결과를 받기전에같은값을 재전송할때 중복 전송됨
    synchronized private boolean _check_sending_TIMS(boolean bflag, boolean bcheck)
    {
        if(bcheck)
            return bSendingTIMS;

        bSendingTIMS = bflag;

        return bSendingTIMS;
    }

//=======================================//
//for sendThread.
//=======================================//
    class n_TIMS_Thread implements Runnable {
        public void run() {

            TIMSQueue que = null;
            TIMSQueue errorque = null;
            boolean mSendFlag = true;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mTIMSsendQ.remainingCapacity() < 20 || errorque != null) {

                        //capacity 저장공간 20개 일 때 - que

                        _check_sending_TIMS(true, false);

                        que = mTIMSsendQ.take(); //
                        que.mtrycount = 0;

                        que.mtrytime = System.currentTimeMillis();
                        que.mtrycount++;

                        mSendFlag = true;

                        if (que.mSendType == 1) {  //post //참조

                            if(false)
                            {
                                if (que.mSubType == 1) { //영업정보.

//                                Info.Savedata(Info.g_nowKeyCode + "_tims.txt", que.mData, "TIMS");
//                                _after_sendTIMS_ok(que.mURLs, que.mData, que.mSubType);
                                    if (que.mResend == false) {
                                        _after_sendTIMS_fail(que.mURLs, que.mData, que.mSubType);
                                        bTimsSendFail = true;
                                    } else
                                        _after_ResendTIMS_ok(que.mdrvtime, que.mSubType);

//                                mService.mCallback.serviceMessage(2, "TIMS 영업정보보관 OK");
//                                    continue;
                                } else if (que.mSubType == 2) { //버튼정보.
                                    if (que.mResend == false) {
                                        _after_sendTIMS_fail(que.mURLs, que.mData, que.mSubType);
                                        bTimsSendFail = true;
                                    } else
                                        _after_ResendTIMS_ok(que.mdrvtime, que.mSubType);

//                                Info.Savedata(Info.gTimsLastDate + "_event.txt", que.mData, "TIMS");
//                                    mCallback.serviceMessage(2, "TIMS 버튼DATA보관 OK");
//                                    continue;
                                } else if (que.mSubType == 3) { //power정보.
//                                Info.Savedata(Info.gTimsLastDate + "_power.txt", que.mData, "TIMS");
//                                    mCallback.serviceMessage(3, "TIMS 버튼DATA보관 OK");
                                } else if (que.mSendType == 6) {  //블루투스 & 시경계 연결상태
                                    //재전송 할 필요없음. 그냥 바로 전송.
                                    Log.d("finalObj_timsuses","false");
                                }

                                continue;
                            }

                            URL url = new URL(que.mURLs);
                            Log.d("finalObj_url", url.toString());

                            {
                                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                switch (que.mSubType) {
                                    case 1:
                                        Info.mDriveTIMSdate = transFormat.format(new Date());
                                        Info.mDriveTIMSok = "실패";
                                        break;

                                    case 2:
                                        Info.mEventTIMSdate = transFormat.format(new Date());
                                        Info.mEventTIMSok = "실패";
                                        break;

                                    case 3:
                                        Info.mPowerTIMSok = false;
                                        break;

                                }
                            }

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Accept", "application/json");
                                conn.setRequestProperty("Content-type", "application/json");
                                conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);
                                conn.setDoOutput(true);
                                conn.setDoInput(true);

                                OutputStream os = conn.getOutputStream();
// 20220425                               os.write(que.mJson.toString().getBytes("UTF-8"));
                                os.write(que.mData.getBytes("UTF-8"));
                                os.flush();

                                Log.d("mfinalObj_mData_1", que.mData);   //{"phoneNo":"01050564465","carno":"서울02가0001","logs":{"logtime":"2022-05-23 09:18:49","logtype":"블루투스","log":"On\/  km"}}

                                if (Info.REPORTREADY)
                                    Info._displayLOG(Info.LOGDISPLAY, "" + que.mData.length(), "TIMS SEND-");

                                if (conn != null) {

                                    Log.d("finalObj_timsuses_conn","conn not null");

                                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        try {
                                            BufferedReader br = new BufferedReader(
                                                    new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                            String connres = br.readLine().toUpperCase();

                                            Log.d("mfinalObj_connres", que.mData);

                                            if (connres.contains("SUCCESS")) {
                                                if (true)
                                                    Info._displayLOG(Info.LOGDISPLAY, connres, "TIMS RECEIVE(POST)-" + que.mSubType);

                                                switch (que.mSubType) {
                                                    case 1:
                                                        Info.mDriveTIMSok = "성공";
                                                        mService.mCallback.serviceMessage(1, "TIMS 영업DATA전송 OK");
                                                        Info._displayLOG(Info.LOGDISPLAY, " TIMS 영업DATA전송 OK", connres);
                                                        break;

                                                    case 2:
                                                        Info.mEventTIMSok = "성공";
//                                                        mService.mCallback.serviceMessage(0, "TIMS 버튼DATA전송 OK");
                                                        Info._displayLOG(Info.LOGDISPLAY," TIMS 버튼DATA전송 OK", connres);
                                                        break;

                                                    case 3:
                                                        Info.mPowerTIMSok = true;
//                                                        mService.mCallback.serviceMessage(0, "TIMS POWER DATA전송 OK");
                                                        break;
                                                }

                                                if(que.mResend)
                                                {
                                                    _after_ResendTIMS_ok(que.mdrvtime, que.mSubType);
                                                }

                                            }
                                            else
                                                Info._displayLOG(Info.LOGDISPLAY, " 전송값 체크- ", connres);

                                        } catch (Exception e) {
                                            Log.e("e POST TIMS", e.getMessage());

                                            mSendFlag = false;

                                        }
                                    } else {
                                        Info._displayLOG(Info.LOGDISPLAY, "status code=" + conn.getResponseCode(), "");
                                        Info._displayLOG(Info.LOGDISPLAY, "content message=" + conn.getErrorStream(), "");
                                        mSendFlag = false;


                                    }

                                    conn.disconnect();

                                    if (mSendFlag == false) {

                                        switch (que.mSubType) {
                                            case 1:
//                                                mService.mCallback.serviceMessage(0, "TIMS 영업DATA전송 FAIL!");
                                                Info._displayLOG(Info.LOGDISPLAY, "TIMS 영업DATA전송 FAIL!", "");

                                                break;

                                            case 2:

//                                                mService.mCallback.serviceMessage(0, "TIMS 버튼DATA전송 FAIL!");
                                                Info._displayLOG(Info.LOGDISPLAY, "TIMS 버튼DATA전송 FAIL!", "");

                                                break;

                                            case 3:
                                                ;
                                                break;
                                        }
                                    }

                                }
                            }

                        } else if (que.mSendType == 2) {
//일반GET url
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();
                                        if (Info.REPORTREADY)
                                            Info._displayLOG(Info.LOGDISPLAY, " 성공 " + connres, "TIMS RECEIVE(GET)-");

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(Info.LOGDISPLAY, "status code=" + conn.getResponseCode(), "");
                                    Info._displayLOG(Info.LOGDISPLAY, "content message=" + conn.getErrorStream(), "");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        } else if (que.mSendType == 4) {
//택시차량번호인증
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine().toUpperCase();

                                        if (connres.contains("SUCCESS")) {
//                                            mService.mCallback.serviceMessage(0, "TIMS 차량번호인증성공");
                                            Info._displayLOG(Info.LOGDISPLAY, " 차량번호인증성공 " + connres, "TIMS RECEIVE(GET)-");
                                            Info.mAuthVehTIMS = "성공";
                                        } else {

                                            Info._displayLOG(Info.LOGDISPLAY, " 차량번호인증실패 " + connres, "TIMS RECEIVE(GET)-");
                                            mService.mCallback.serviceMessage(98, "차량번호인증실패");
                                        }

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(Info.LOGDISPLAY, "차량번호인증실패 " + "status code=" + conn.getResponseCode(), "");
                                    mService.mCallback.serviceMessage(98, "차량번호인증실패");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        } else if (que.mSendType == 5) {
//운전자격인증
                            URL url = new URL(que.mURLs);

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            if (conn != null) {
//                                    conn.setUseCaches(false);
                                conn.setConnectTimeout(2000);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    try {
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine().toUpperCase();

                                        if (connres.contains("SUCCESS")) {
//                                            mService.mCallback.serviceMessage(0, "TIMS 운전자격인증성공");
                                            Info._displayLOG(Info.LOGDISPLAY, " 운전자격인증성공 " + connres, "TIMS RECEIVE(GET)-");
                                            Info.mAuthdrvTIMS = "성공";
                                        } else {

                                            Info._displayLOG(Info.LOGDISPLAY, " 운전자격인증실패 " + connres, "TIMS RECEIVE(GET)-");
                                            mService.mCallback.serviceMessage(97, "운전자격인증실패");
                                        }

                                    } catch (Exception e) {
                                        Log.e("e GET TIMS", e.getMessage());
                                        mSendFlag = false;

                                    }

                                } else {
                                    Info._displayLOG(Info.LOGDISPLAY, "운전자격인증실패 " + "status code=" + conn.getResponseCode(), "");
                                    mService.mCallback.serviceMessage(97, "운전자격인증실패");
                                    mSendFlag = false;
                                }

                                conn.disconnect();
                            }
                        }

                        Thread.sleep(300); //20210419

                    } else {
                        Thread.sleep(1000);


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                    mSendFlag = false;

//                    Info._displayLOG(Info.LOGDISPLAY, "tims전송 통신오류발생", "");
                }

                _check_sending_TIMS(false, false);

                if(mSendFlag == false && que.mSendType == 1 && que.mResend == false)
                {
                    bTimsSendFail = true;
                    _after_sendTIMS_fail(que.mURLs, que.mData, que.mSubType);

                }

                if(bExit)
                    break;

                if (Info.REPORTREADY) {
                    if (mSendFlag == false) {
                        errorque = que;

                        Info._displayLOG(Info.LOGDISPLAY, "TIMS전송실패", "");
                    } else
                        errorque = null;

                }

//                Info._displayLOG(Info.LOGDISPLAY, "TIMS thread", "");

            }

        }
    }


    //연결상태 인터패스 서버 aka DTG/ 타코로 보내기
    class n_Log_Thread implements Runnable {
        @Override
        public void run() {

            DTGQueue que = null;
            try {
                if (mLogSendQ.remainingCapacity() < 6) {

                    que = mLogSendQ.take();
                    que.mtrycount = 0;
                    que.mtrytime = System.currentTimeMillis();
                    que.mtrycount++;

                    if (que.mSendType == 1) {  //POST
                        URL url = null;
                        try {
                            url = new URL(que.mURLs);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        Log.d("logSendFinalUrl", url.toString()); //
                        Log.d("logSendFinalData", que.mData);
                        try {
                            //http client
                            HttpURLConnection conn = null;
                            conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {

                                //연결방법 설정
                                conn.setRequestMethod("POST");
                                //charset 설정
                                conn.setRequestProperty("Accept", "application/json");
                                conn.setRequestProperty("Content-type", "application/json");
                                conn.setUseCaches(false);
                                //연결하는데 시간이 오래 걸리는 경우 time out 설정
                                conn.setConnectTimeout(2000);
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                //POST 로 넘겨 줄 파라미터 생성
                                OutputStream os = conn.getOutputStream();
                                os.write(que.mData.toString().getBytes("UTF-8"));
                                os.flush();
                                Log.d("logSendConn","통신코드: "+conn.getResponseCode());  //200

                                if (conn != null) {
                                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        //결과값을 받아온다
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                        String connres = br.readLine();
                                        Log.d("logSendConres", "(수신) " +connres);

                                        if (connres.contains("SUCCESS")) {

                                            if (true) {
//                                                threadToast("전송을 완료하였습니다.");
//                                                Log.d("logsend","전송 성공");
                                                Info._displayLOG(Info.LOGDISPLAY, connres, "DTG RECEIVE(POST)-" + que.mSubType);
                                            }else {}

                                        } else {
//                                            threadToast("전송할 데이터가 없습니다.");
                                            Info._displayLOG(Info.LOGDISPLAY, " 전송값 체크- ", connres);
                                            Thread.sleep(300);
                                        }
//                                            conn.disconnect();
                                    }
                                }else {
//                                    Log.d("logsendNull", "null");
                                }

                            }else {
//                                Log.d("logSendConn-null", "null");
                            }

                        }catch (Exception e) {
//                            Log.e("logsendExcept", e.toString());
                        }

                    }//else/ que.mSendType != 1
                }// !mLogSendQ.remainingCapacity < 6




            }catch (Exception e) {}

        }//run


        void stopThread(){

        }
    }

    public void threadToast(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mService.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }



    //me: 인터패스 서버로 가는 타코 쓰레드
    class DTG_NetworkThread implements Runnable {
        public void run() {

            DTGQueue que = null;
            URL url = null;

            Log.d("kkkkkkkk-11", mSendDTGQ.remainingCapacity()+"");  //5

            while (!Thread.currentThread().isInterrupted()) {
                try {

                        if (mSendDTGQ.remainingCapacity() < 5) {

                            Log.d("kkkkkkkk-2", mSendDTGQ.remainingCapacity()+"");

                            que = mSendDTGQ.take();

                            try {
                                url = new URL(que.mData);
//                                url = new URL("http://192.168.0.21:8080/AppMeterApi/Drive1MinAPI?info_dtti=220530141438&car_num=서울02가0004&driver_id=00068291855&speed=50&dist=0&car_x=37.481525690586&car_y=126.91843833014202&statte=1&office_id=8388801872");
                                Log.d("check_que_data", que.mData);   //이벤트값.. ex) 버튼값, 시외비용, 결제비용, 추가비용, 주행거리.. etc
                            }
                            catch(Exception ex)
                            {
                                if(que.mSubType == 1 && que.mResend == true)
                                    _after_ResendDTG_ok(que.mdrvtime);

                                continue;
                            }

                            Log.d("TimsDtg", "uri " + url.toString());



                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setConnectTimeout(2000);
                                conn.setUseCaches(false);
                                try {
                                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                        Log.d("TimsDtg_responseCode", conn.getResponseCode()+"");

                                            BufferedReader br = new BufferedReader(
                                                    new InputStreamReader((conn.getInputStream()), "UTF-8"));
                                            String connres = br.readLine();
                                            Log.d("TimsDtg", "(수신)" + connres);

                                            if(connres.equals("Y") && que.mSubType == 1 && que.mResend == false)
                                                _after_sendDTG_ok(que.mKeycode, url.toString());
                                            else if(connres.equals("N") && que.mSubType == 1 && que.mResend == false)
                                                _after_sendDTG_ok(que.mKeycode, url.toString());
                                            else if(connres.equals("Y") && que.mSubType == 1 && que.mResend == true)
                                            {
                                                _after_ResendDTG_ok(que.mdrvtime);

                                            }
                                            else if(connres.equals("N") && que.mSubType == 1 && que.mResend == true)
                                                _after_ResendDTG_ok(que.mdrvtime);

                                    }
                                } catch (Exception e) {

                                    Log.d("TimsDtg---", e.getMessage());
                                    if(que.mSubType == 1 && que.mResend == false)
                                        _after_sendDTG_fail(que.mKeycode, url.toString());

                                }
                            }
                            else {
                                if(que.mSubType == 1)
                                    _after_sendDTG_fail(que.mKeycode, url.toString());
//                                Log.d("TimsDtg---", "server connect error");
                            }

                            Thread.sleep(200);
                        }
                        else
                            Thread.sleep(500);

                } catch (Exception ex) {
                    if(que.mSubType == 1 && que.mResend == false) {
                        if(url != null)
                            _after_sendDTG_fail(que.mKeycode, que.mData);

                    }
                    Log.e("DTG Thread Error", "error is : " + ex.toString());
                    ex.printStackTrace();

                }

                if(bExit)
                    break;

            }

        }
    }

//=======================================//
//for TIMS.
//=======================================//

    void SendTIMS_Data(int div, int payType, List<TIMS_UnitVO> Params, String subParams) {

        /**
         * URL | div
         * POST:35000/app-meter/biz           | 1: 앱미터 자기인증 영업정보 이벤트 발생에 대한 전송
         * POST:35000/app-meter/btn           | 2: 앱미터 자기인증 이벤트 발생에 대한 전송
         * POST:35000/app-meter/power         | 3: 앱미터 자기인증 운행 시작/종료 이벤트 발생에 대한 전송
         * 1~3 테스트전용
         * POST:45000/app-meter/biz           | 4: 앱미터 상시 영업정보 이벤트 발생에 대한 전송
         * POST:45000/app-meter/btn           | 5: 앱미터 상시 이벤트 발생에 대한 전송
         * POST:45000/app-meter/power         | 6: 앱미터 상시 운행 시작/종료 이벤트 발생에 대한 전송
         * GET :55000/app-meter/auth/vehicle  | 7: 자동차 정보 확인 API
         * GET :55000/app-meter/auth/driver   | 8: 운전자 정보 확인 API
         */

        //todo: 2022-05-20
//        if (Info.TIMSUSE == false) {
//
//            return;
//        }

        double latitude = 0;
        double longitude = 0;


        if (Info.m_Service.mCalLocation != null) {

            Log.d("finalObj_location", "not null");

            latitude = mService.mCalLocation.getLatitude();
            longitude = mService.mCalLocation.getLongitude();
        }

        Date date = new Date();
        SimpleDateFormat dttiFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String dtti = dttiFormat.format(date);

        String sTIME = "";
        String eTIME = "";
        String sXPOS = "";
        String sYPOS = "";
        String eXPOS = "";
        String eYPOS = "";

        String mDIST = "0";
        String ePAY = "0";

        int mSendTYPE = 0;
        int mSubtype = 0;

        try {
            JSONObject infoObj = new JSONObject();
            JSONObject iObj = new JSONObject();

            Log.d("finalObj_div", div+"");

            switch (div) {

                case 1:

                    Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS TIMS 영업DATA전송idx " + Info.gTimsDayIdxtmp, "");
                    TIMS_ADDURL = TIMS_BIZ;
                    mSendTYPE = 1;
                    mSubtype = 1;

                    int nextra = 0;

                    //main { }
                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayIdxtmp);


                    JSONArray unitArray = new JSONArray();
                    for (int i = 0; i < Params.size(); i++) {

                        if (i == 0) {
                            sTIME = Params.get(i).getDt();
                            sXPOS = Params.get(i).getLongitude();
                            sYPOS = Params.get(i).getLatitude();

                        } else if (i == Params.size() - 1) {
                            eTIME = Params.get(i).getDt();
                            eXPOS = Params.get(i).getLongitude();
                            eYPOS = Params.get(i).getLatitude();
                            mDIST = Params.get(i).getSumdist();
                            ePAY = Params.get(i).getSumPay();
                        }

                        JSONObject uObj = new JSONObject();

                        //sub { }
                        uObj.put("idx", Params.get(i).getIdx());
                        uObj.put("dt", Params.get(i).getDt());
                        uObj.put("longitude", Params.get(i).getLongitude());
                        uObj.put("latitude", Params.get(i).getLatitude());
                        uObj.put("spd", Double.parseDouble(Params.get(i).getSpd()));
                        uObj.put("lnk", ""); //20210512 Params.get(i).getLnk());
                        uObj.put("dist", Double.parseDouble(Params.get(i).getDist()));
                        uObj.put("payType", Params.get(i).getPayType());
                        uObj.put("remainDist", Double.parseDouble(Params.get(i).getRemainDist()));
                        uObj.put("remainSec", Integer.parseInt(Params.get(i).getRemainSec()));
                        uObj.put("isAdded", Integer.parseInt(Params.get(i).getIsAdded()));
                        uObj.put("addedPay", Integer.parseInt(Params.get(i).getAddedPay()));
                        uObj.put("sumPay", Integer.parseInt(Params.get(i).getSumPay()));
                        uObj.put("isLimitSpd", Integer.parseInt(Params.get(i).getIsLimitSpd()));
                        uObj.put("isNight", Integer.parseInt(Params.get(i).getIsNight()));
                        if (Integer.parseInt(Params.get(i).getIsNight()) > 0) //20210520
                            nextra = 1;

                        uObj.put("isOutside", Integer.parseInt(Params.get(i).getIsOutside()));

                        if (Integer.parseInt(Params.get(i).getIsOutside()) > 0) //20210520
                            nextra = 1;

                        uObj.put("isOutGps", Integer.parseInt(Params.get(i).getIsOutGps()));
                        uObj.put("isOutPow", 0);
                        uObj.put("isWait", 0);

                        unitArray.put(uObj);
                    }

                    JSONObject finPayObj = new JSONObject();
                    finPayObj.put("dt", dtti);
                    finPayObj.put("pay", (int) (Math.round(Double.parseDouble(ePAY) / 100) * 100) + Info.ADDFARE); //호출요금제외
                    finPayObj.put("call", 0); //Integer.parseInt("0")); //호출요금제외
                    finPayObj.put("etc", Info.ADDFARE); //Integer.parseInt("0"));
                    finPayObj.put("ext", nextra); //20210520
                    finPayObj.put("inDt", sTIME);
                    finPayObj.put("inLongitude", sXPOS);
                    finPayObj.put("inLatitude", sYPOS);
                    finPayObj.put("outDt", eTIME);
                    finPayObj.put("outLongitude", eXPOS);
                    finPayObj.put("outLatitude", eYPOS);
                    finPayObj.put("onDist", Double.parseDouble(mDIST));
                    finPayObj.put("emptyDist", Double.parseDouble(subParams));
                    finPayObj.put("type", payType);
                    finPayObj.put("trepay", 0);

                    infoObj.put("units", unitArray);
                    infoObj.put("finalPayInfo", finPayObj);

                    if (Info.REPORTREADY)
                        Info._displayLOG(Info.LOGDISPLAY, infoObj.toString().length() + " " + infoObj.toString(), "TIMS BIZ-");

                    break;

                case 2:
                    mService.mCallback.serviceMessage(2, "TIMS 버튼DATA전송 시작");

                    Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS TIMS 버튼DATA전송idx " + Info.gTimsDayEventIdx, "");
                    TIMS_ADDURL = TIMS_BTN;
                    mSendTYPE = 1;
                    mSubtype = 2;
                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayEventIdx);

                    String[] subDatas = subParams.split("&");

                    /**
                     * btn info
                     * 01 : 지불
                     * 05 : 빈차
                     * 10 : 영수증 또는 출력
                     * 20 : 주행
                     * 30 : 할증
                     * 31 : 자동할증
                     * 32 : 시계할증-자동
                     * 33 : 시계할증-수동
                     */

                    if (subDatas[0].equals("01"))
                        Info.mEventTIMStype = "지불";
                    else if (subDatas[0].equals("05"))
                        Info.mEventTIMStype = "빈차";
                    else if (subDatas[0].equals("10"))
                        Info.mEventTIMStype = "영수증";
                    else if (subDatas[0].equals("20"))
                        Info.mEventTIMStype = "주행";
                    else if (subDatas[0].equals("30"))
                        Info.mEventTIMStype = "할증";
                    else if (subDatas[0].equals("31"))
                        Info.mEventTIMStype = "자동할증";
                    else if (subDatas[0].equals("32"))
                        Info.mEventTIMStype = "시계할증자동";
                    else if (subDatas[0].equals("33"))
                        Info.mEventTIMStype = "시계할증수동";
                    else
                        Info.mEventTIMStype = "-";

                    iObj.put("dt", dtti);
                    iObj.put("btn", subDatas[0]);
                    iObj.put("state", subDatas[1]);
                    iObj.put("longitude", String.format("%.6f", longitude));
                    iObj.put("latitude", String.format("%.6f", latitude));

                    infoObj.put("infos", iObj);

                    if (Info.REPORTREADY)
                        Info._displayLOG(Info.LOGDISPLAY, infoObj.toString(), "TIMS BTN-");

                    break;

                case 3:
                    mService.mCallback.serviceMessage(3, "TIMS POWER DATA전송 시작");
                    TIMS_ADDURL = TIMS_POWER;
                    mSendTYPE = 1;
                    mSubtype = 3;

                    infoObj.put("appId", AMBlestruct.AMLicense.timscode);
                    infoObj.put("license", AMBlestruct.AMLicense.timslicense);
                    //infoObj.put("license", AMBlestruct.AMLicense.licensecode);
                    infoObj.put("brn", AMBlestruct.AMLicense.companynum);
                    //infoObj.put("brn", "운수사업자 등록번호");
                    infoObj.put("regNo", AMBlestruct.AMLicense.timstaxinum);
                    infoObj.put("phone", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("type", 21);
                    infoObj.put("dailySeq", Info.gTimsDayPowerIdx);

                    iObj.put("dt", dtti);
                    iObj.put("state", subParams);

                    infoObj.put("infos", iObj);

                    break;

                case 4:
                    TIMS_ADDURL = TIMS_VEHICLE + AMBlestruct.AMLicense.timstaxinum +
                            "&APPMETER_ID=" + AMBlestruct.AMLicense.timscode + "&KEY=" + Info.TIMSKEY;
                    mSendTYPE = 4;

                    Info._displayLOG(Info.LOGDISPLAY, "TIMS차량인증요청GET " + TIMS_BASEURL + TIMS_ADDURL, "");

                    break;
                case 5:
                    TIMS_ADDURL = TIMS_DRIVER + AMBlestruct.AMLicense.timslicense +
                            "&APPMETER_ID=" + AMBlestruct.AMLicense.timscode + "&KEY=" + Info.TIMSKEY;
                    mSendTYPE = 5;
                    Info._displayLOG(Info.LOGDISPLAY, "TIMS자격인증요청GET " + TIMS_BASEURL + TIMS_ADDURL, "");

                    break;

                case 6:
                    //get 아니고 post 방식으로..
                {
                    infoObj.put("phoneNo", AMBlestruct.AMLicense.phonenumber);
                    infoObj.put("carno", AMBlestruct.AMLicense.taxinumber);
                    infoObj.put("logs", "");


                    JSONArray logArray = new JSONArray();
                    for (int i = 0; i < Params.size(); i++) {
                        iObj = new JSONObject();
                        iObj.put("logtime", Params.get(i).getLogtime());
                        iObj.put("logtype", Params.get(i).getLogtype());
                        iObj.put("log", Params.get(i).getLog());

                        logArray.put(iObj);
                    }

                    infoObj.put("logs", logArray);  //하위 -> 상위에 붙이기

//                    Log.d("finalObj_highObj", infoObj.toString());

                    DTGQueue que = new DTGQueue();

                    que.mData = infoObj.toString();

//                    que.mURLs = "http://192.168.0.21:8080/AppMeterApi/log-data"; //이안수 대리 로컬주소

                    //인터패스 서버
                    que.mURLs = "http://49.50.165.75/AppMeterApi/log-data";  //인터패스 공인 ip 서버

                    que.mSendType = 1;
                    que.mSubType = mSubtype;

                    que.mResend = false;
                    que.mdrvtime = "";

                    if (mLogSendQ.remainingCapacity() > 0)
                        mLogSendQ.add(que);

                    LogSendThread = new Thread(new n_Log_Thread());
                    LogSendThread.start();


                    return;
                }
            }

            TIMSQueue que = new TIMSQueue();

            que.mURLs = TIMS_BASEURL + TIMS_ADDURL;
//            Log.e("check_mURl", que.mURLs+"");  //https://tims-help.kotsa.or.kr:55000/app-meter/auth/driver?QUALF_NO=464989868&APPMETER_ID=0000000019&KEY=f4bbc1d0b067002e527e535338668b29164404fd18a4e5c331c70fcb9b07fd62
            que.mData = infoObj.toString();

            que.mSendType = mSendTYPE;
            que.mSubType = mSubtype;

            que.mResend = false;
            que.mdrvtime = "";

            add_TIMSQueue(que);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void _makeTIMSDriveData(int idx, String date, Location location, double speed, String lnk, double dist, double remainsDist,
                                   int fare, int isOutGps, int addfare, double tdist, boolean isnight, boolean issuburb) {

        if (Info.TIMSUSE == false)
            return;
//            double latitude = 0;
///            double longitude = 0;
        String latitude = "";
        String longitude = "";
        String payType = "";
        String isLimitSpd = "0";
        String isNight = "";
        String isOutside = "";
        String stemp = "";
        double intSpd = 0;
        int isAdded = 0;

        if (idx == 0) {

//                Info.deletefile(Info.g_nowKeyCode + ".txt", "TIMS");
        }

        if (location != null) {

            latitude = String.format("%.8f", location.getLatitude());
            longitude = String.format("%.8f", location.getLongitude());
        } else {

            latitude = String.format("%.8f", 0.0);
            longitude = String.format("%.8f", 0.0);
        }


        intSpd = (speed * 3.6);
        if (isnight) {
            isNight = "1";
        } else {
            isNight = "0";
        }
        if (issuburb) {
            isOutside = "1";
        } else {
            isOutside = "0";
        }

        if (intSpd < CalFareBase.TIMECOST_LIMITHOUR) {

            isLimitSpd = "1";

        } else
            isLimitSpd = "0";

        if (addfare > 0)
            isAdded = 1;

        if (isAdded == 1) {

            payType = "D";

            if (isnight) {

                payType += "E";

            }
            if (issuburb) {

                payType += "Z";

            }

        }

        if (idx == 0) {

            payType = "S";

            if (isnight) {

                payType += "E";

            }
            if (issuburb) {

                payType += "Z";

            }

        }

        stemp += idx + "|"
                + date + "|"
                + longitude + "|"
                + latitude + "|"
                + String.format("%.2f", intSpd) + "|"
                + "|"//+ lnk + "|"
                + String.format("%.4f", dist) + "|"
                + payType + "|"
                + String.format("%.4f", remainsDist) + "|"
                + "0|"//+ remainSec + "|"
                + isAdded + "|"
                + addfare + "|"
                + fare + "|"
                + isLimitSpd + "|"
                + isNight + "|"
                + isOutside + "|"
                + isOutGps + "|"
                + String.format("%.2f", tdist) + "&";


        Info.Savedata(Info.g_nowKeyCode + ".txt", stemp, "TIMS");

    }

    public void _sendTIMSAfterDrive() {

        if(Info.TIMSUSE == false)

            return;

        String result = Info.ReadTextFile(Info.g_nowKeyCode + ".txt", "TIMS");

        if (!result.equals("noFile")) { ///

            setSendTIMSVO(result);
        }
        else
        {
            Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS 영업정보파일없슴", "");
        }

        Info.Deletefile(Info.g_nowKeyCode + ".txt", "TIMS");

        _sendTIMSEventEmpty();
    }

    public void setSendTIMSVO(String data) {
        List<TIMS_UnitVO> params = new ArrayList<>();
        String[] splitDatas = data.split("&");

        int npaytype = 0;
        String semptydist = "";

        try {
            for (int i = 0; i < splitDatas.length; i++) {
                String[] unitData = splitDatas[i].split("\\|");

                if (unitData.length < 5) {

                    npaytype = Integer.parseInt(unitData[0].trim());
                    semptydist = unitData[1].trim();

                    break;
                }

                TIMS_UnitVO unit = new TIMS_UnitVO();

                unit.setIdx(Integer.parseInt(unitData[0].trim()));
                unit.setDt(unitData[1]);
                unit.setLongitude(unitData[2]);
                unit.setLatitude(unitData[3]);
                unit.setSpd(unitData[4]);
                unit.setLnk(""); //unitData[5]);
                unit.setDist(unitData[6]);
                unit.setPayType(unitData[7]);
                unit.setRemainDist(unitData[8]);
                unit.setRemainSec(unitData[9]);
                unit.setIsAdded(unitData[10]);
                unit.setAddedPay(unitData[11]);
                unit.setSumPay(unitData[12]);
                unit.setIsLimitSpd(unitData[13]);
                unit.setIsNight(unitData[14]);
                unit.setIsOutside(unitData[15]);
                unit.setIsOutGps(unitData[16]);
                unit.setSumdist(unitData[17]);

                params.add(i, unit);
            }

            SendTIMS_Data(1, npaytype, params, semptydist);
        }
        catch (Exception e)
        {
            return;
        }
    }

    public void setTIMSfinal(String paytype, double emptydist) {
        if(Info.TIMSUSE == false)
            return;
            Info.Savedata(Info.g_nowKeyCode + ".txt", paytype + "|" + String.format("%.2f", emptydist), "TIMS");

    }

    public void _sendTIMSEventEmpty()
    {
        if(Info.TIMSUSE == false)
            return;
            SendTIMS_Data(2, 0, null, "05&0");
    }

    public void _sendTIMSEventDrive()
    {
        if(Info.TIMSUSE == false)
            return;
        SendTIMS_Data(2, 0, null, "20&0");
    }

//심야할증.
    public void _sendTIMSEventExtra(boolean bon)
    {
        if(Info.TIMSUSE == false)
            return;

        if(bon)
            SendTIMS_Data(2, 0, null, "31&1");
        else
            SendTIMS_Data(2, 0, null, "31&0");
    }

//시계할증.
    public void _sendTIMSEventSuburb(boolean bon, boolean bauto)
    {
        if(Info.TIMSUSE == false)
            return;

        if(bon) {
            if(bauto)
                SendTIMS_Data(2, 0, null, "32&0");
            else
                SendTIMS_Data(2, 0, null, "33&0");
        }
        else
        {
            if(bauto)
                SendTIMS_Data(2, 0, null, "32&1");
            else
                SendTIMS_Data(2, 0, null, "33&1");

        }
    }

//복합.
    public void _sendTIMSEventComplex(boolean bon)
    {
        if(Info.TIMSUSE == false)
            return;

        if(bon)
            SendTIMS_Data(2, 0, null, "34&1");
        else
            SendTIMS_Data(2, 0, null, "34&0");
    }

//현금버튼.
    public void _sendTIMSEventCash()
    {
        if(Info.TIMSUSE == false)
            return;

            Log.d("_sendTIMSEventCash",Info.TIMSUSE+""); //false

            SendTIMS_Data(2, 0, null, "01&0");
    }



    public void _sendTIMSConnStatus() {
//        if (Info.TIMSUSE == false)
//            return;
        SQLiteHelper helper = new SQLiteHelper(mService.getBaseContext());
        SQLiteControl sqlite = new SQLiteControl(helper);
        String[] splt;

        List<TIMS_UnitVO> conn_params  = new ArrayList<>();
        TIMS_UnitVO unit;

        //1. 저장된 데이터 DB에서 뽑아오기..
        String connList[] = sqlite.selectConnStatus();

        if (connList.length > 0) {

            for ( int i=0; i<connList.length; i++ ) {

                splt = connList[i].split("#"); //한줄 당 # 을 기준으로 split

                unit = new TIMS_UnitVO();  //unit 객체를 계속 생성하지 않으면 connList[] 데이터 한줄만 들어감.

                //2. 서버에 전송할 데이터 vo 에 담기
                unit.setLogtime(splt[3]);
                unit.setLogtype(splt[4]);
                unit.setLog(splt[5]);

                //3. 전송할 connList 데이터 add
                conn_params.add(i, unit);
            }//for
        }

        //4. tims 전송데이터 보내기
        SendTIMS_Data(6, 0, conn_params, "");

        //5. 데이터 전체 삭제 -> 다음에 insert 할 데이터의 중복을 막기위한 것.
        sqlite.deleteConnStatus();
    }

    //블루투스 & 시경계데이터 - DTG
    public void _sendDTGConnStatus(String[] params) {

        Send_DTGData(6, params, 6);

    }

//영수증
    public void _sendTIMSEventReceipt()
    {
        if(Info.TIMSUSE == false)
            return;
        SendTIMS_Data(2, 0, null, "10&0");
    }

//앱미터시작 poweronoff
    public void _sendPowerOnoff()
    {
        if(Info.TIMSUSE == false)
            return;
        if(Info.APPMETERRUNSTOP == 1)
            SendTIMS_Data(2, 0, null, "10&0");
        else
            SendTIMS_Data(2, 0, null, "10&1");

    }

//차량인증.
    public void _sendTIMSCertVehicles()
    {
        if(Info.TIMSUSE == false)
            return;
        SendTIMS_Data(4, 2, null, "1"); //차량인증.
    }

//자격인증.
    public void _sendTIMSCertDriver()
    {
        if(Info.TIMSUSE == false)
            return;
        SendTIMS_Data(5, 2, null, "1"); //운전자격인증
    }

//=======================================//
//for DTG
//=======================================//
    /**
     * 버튼정보
     * <p>
     * 01. 지불
     * 05. 빈차
     * 10. 영수증, 출력
     * 20. 주행
     * 30. 할증
     * 31. 자동할증
     * 32. 시계할증-자동
     * 33. 시계할증-수동
     * 34. 복합할증
     * 35. 복합할증 취소
     * 37. 자동할증 취소
     * 38. 시계할증 취소
     * 40. 예약호출
     * 41. 예약호출 취소
     * 42. 도어1 열림
     * 43. 도어1 닫힘
     * 44. 도어2 열림
     * 45. 도어2 닫힘
     *
     * @param div
     * @param subParam - 이벤트정보
     */

    public void Send_DTGData(int div, String[] subParam, int event) {
        if (Info.SENDDTG == true) {
            Log.d("send_dtg", Info.SENDDTG + "");
            Log.d("send_dtg", div + "");
            Log.d("send_dtg", subParam + "");
            Log.d("send_dtg","event- "+event+"");
            ;
        } else {
            return;
        }

        Date date = new Date();
        SimpleDateFormat dttiFormat = new SimpleDateFormat("yyMMddHHmmss");
        String dtti = dttiFormat.format(date);
        String CARNUM = AMBlestruct.AMLicense.taxinumber;
        String DRIVERID =  AMBlestruct.AMLicense.licensecode;

        Info._displayLOG(Info.LOGDISPLAY, "TimsTIMS Send_DTGData", DRIVERID + "");

        String lXpos = "";
        String lYpos = "";

        if (mService.mLastLocation == null) {
            lXpos = 0.0 + "";
            lYpos = 0.0 + "";
        } else {
            lXpos = mService.mLastLocation.getLatitude() + "";
            lYpos = mService.mLastLocation.getLongitude() + "";
        }

        /*Log.e("dayDrvRecord")*/

        switch (div) {
            //info_dtti = "YYMMDDHHmmss"
            case 1:  //1뷴에 한번씩 위치정보

                DTG_PARAMS = "info_dtti=" + dtti +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&speed=" + (int) (mService.mGPSspeed * 3.6) +
                        "&dist=" + (int) mService.dtgReportDist +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&state=" + "1" +  // 시동 ON/OFF
                        "&office_id=" + AMBlestruct.AMLicense.companynum; //20220103
//                        "&office_id=" + "9876543211";
                DTG_PATH = "Drive1MinAPI?" + DTG_PARAMS;
                break;
            case 2:  //앱미터 시동

                String[] dayDrvRecordData = Info.sqlite.todayTotSelect().split("/");

                String driveCount = Info.sqlite.todayDriveCount();
                DTG_PARAMS = "info_dtti=" + dtti +
                        "&pairing_type=" + subParam[0] +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&dtg_model=" + subParam[1] +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&base_rate=" + CalFareBase.BASECOST +
                        "&base_dist=" + CalFareBase.BASEDRVDIST +
                        "&after_rate=" + CalFareBase.DISTCOST +
                        "&after_dist=" + CalFareBase.INTERVAL_DIST +
                        "&time_rate=" + CalFareBase.mNightTimerate +
                        "&time_fare=" + CalFareBase.TIMECOST +
                        "&time_fare_speed=" + CalFareBase.TIMECOST_LIMITHOUR +
                        "&start_extra_time=" + "00:00" +
                        "&end_extra_time=" + "04:00" +
                        "&city_extra_rate=" + CalFareBase.mSuburbrate +
                        "&call_fare=" + "0" +
                        "&call_extra_rate=" + "0" +
                        "&spec_extra_rate=" + CalFareBase.mComplexrate +
                        "&range_extra_dist=" + "0" + //구간할증구간 이동거리
                        "&range_extra_rate=" + CalFareBase.mComplexrate +
                        "&cut_off_fare=" + "2" + // 요금절사 - 0:안함 1:1원단위 2:10원단위
                        "&today_dist=" + (Integer.parseInt(dayDrvRecordData[0]) + Integer.parseInt(dayDrvRecordData[1])) +
                        "&bincha_dist=" + Integer.parseInt(dayDrvRecordData[1]) +
                        "&juhaeng_dist=" + Integer.parseInt(dayDrvRecordData[0]) +
                        "&juhaengcount=" + driveCount +
                        "&worktime=" + (Integer.parseInt(dayDrvRecordData[2]) + Integer.parseInt(dayDrvRecordData[3])) +
                        "&money=" + dayDrvRecordData[4];
                DTG_PATH = "PairingInfoAPI?" + DTG_PARAMS;
                break;
            case 3:   //버튼 이벤트
//                if (subParam[0].equals("05")) break;
                DTG_PARAMS = "info_dtti=" + dtti +
                        "&car_num=" + CARNUM +
                        "&driver_id=" + DRIVERID +
                        "&button_val=" + subParam[0] +
                        "&dist=" + subParam[1] +
                        "&car_x=" + lXpos +
                        "&car_y=" + lYpos +
                        "&state=" + "1" + //시동 0:OFF / 1:ON
                        "&paytype=" + subParam[2] + // 1: 현금 2: 카드 3: 지역화폐
                        "&fare=" + subParam[3] +
                        "&zero_base_fare=" + subParam[4] + // 기본요금 : 0 , 기본요금외요금 :1
                        "&call_payment=" + subParam[16] + //호출요금 //20220413
                        "&add_fare=" + subParam[5] + //추가요금
                        "&time_extra_fare=" + subParam[6] + //야간할증요금
                        "&range_extra_fare=" + subParam[7] + //구간할증요금
                        "&city_extra_fare=" + subParam[8] + //시외할증요금
                        "&time_rate_fare=" + subParam[9] + //차가 서있을 때 미터기 감소로 발생하는 요금
                        "&base_dist=" + CalFareBase.BASEDRVDIST +
                        "&meter_per_sec1=" + (int) (CalFareBase.INTERVAL_DIST / CalFareBase.INTERVAL_TIME) +
                        "&meter_per_sec2=" + (int) (CalFareBase.INTERVAL_DIST / CalFareBase.INTERVAL_TIME) +
                        "&time_extra_dist=" + subParam[10] + //야간할증거리
                        "&city_extra_dist=" + subParam[11] + //시외할증거리
                        "&range_extra_dist=" + subParam[12] + //구간할증거리
                        "&time_extra_sec=" + subParam[13] + //야간할증시간
                        "&city_extra_sec=" + subParam[14] + //시외할증시간
                        "&range_extra_sec=" + subParam[15] + //구간할증시간
                        "&time_fare=" + "0" + //◇계산 방법=시속 l5km를 기준으로 그 이상 속도일 때는 거리가 요금으로 나오고 그 이하일 때는 시간이 요금으로 계산되는 상호병산제. 시간 요금은 15km 이하 속도일 때는 l5km 속도로 계속 달린 것으로 상정해 그때 나올 거리 요금을 시간으로 쪼갠 1분 36초에 50원씩이다
                        "&fee_counter=" + "0" +
                        "&office_id=" + AMBlestruct.AMLicense.companynum; //20220103
                DTG_PATH = "EventAPI?" + DTG_PARAMS;

                break;

//            case 6:  //블루투스 & 시경계 연결상태 DTG
//
//                for (int i=0; i<subParam.length; i++) {
//
//                    Info.gSplt = subParam[i].split("#");
//                }
//                Log.d("gSplt", Info.gSplt[0]+": "+Info.gSplt[1]+": "+Info.gSplt[2]+": "+Info.gSplt[3]+": "+Info.gSplt[4]+": "+Info.gSplt[5]);
//
//                DTG_PARAMS = "phoneNo=" + Info.gSplt[0] +
//                        "&carno=" + Info.gSplt[1] +
//                        "&logs=" + Info.gSplt[2] +
//                        "&logtime=" + Info.gSplt[3] +
//                        "&logtype=" + Info.gSplt[4] +
//                        "&log=" + Info.gSplt[5];
//                DTG_PATH = "ConnStatusAPI?" + DTG_PARAMS;
//                Log.d("ddddd_path", DTG_PATH);  //ConnStatusAPI?phoneNo=01050564465&carno=서울02가0001&logs=log ble&logtime=2022-05-23 08:55:19&logtype=블루투스&log=On/  km
//
//                break;
//
//            default:
//                DTG_PATH = "empty";
//                break;

        }

        if (mSendDTGQ.remainingCapacity() == 0) {
            try {
                mSendDTGQ.take();
            } catch (Exception e) {

            }
        }

        DTGQueue que = new DTGQueue();

        que.mKeycode = Info.g_nowKeyCode;
        que.mData = DTG_BASEURL + DTG_PATH;
        que.mSendType = 1;
        que.mSubType = event;
        que.mResend = false;
        que.mdrvtime = "";

        add_DTGQueue(que);

//        if (div != 6) {
//
//
//        }else {
//
//            Log.d("check_dtg_path", DTG_PATH);  //ConnStatusAPI?phoneNo=01050564465&carno=서울02가0001&logs=log ble&logtime=2022-05-23 09:08:31&logtype=블루투스&log=On/  km
//
//            DTG_BASEURL = "인터패스서버주소/";
//            que.mData = DTG_BASEURL + DTG_PATH;
//
//            Log.d("check_dtg_url", que.mData);
//
////            que.mURLs = "http://was_server/";
//            que.mSubType = 6;
//            que.mSendType = 1;
//            que.mResend = false;
//            add_DTGQueue(que);
//
//        }


    }//Send_DTGData


    //20220413
    public void _sendPayDTGData(String payType)
    {
        if(Info.SENDDTG == false)
            return;

        int basePayType = 0;
        if(AMBlestruct.AMCardFare.mFare > CalFareBase.BASECOST) {
            basePayType++;
        }
        if(AMBlestruct.AMCardFare.mstype.equals("01") || AMBlestruct.AMCardFare.mstype.equals("05") || AMBlestruct.AMCardFare.mstype.equals("06")) {
            String[] param = {"01", AMBlestruct.AMCardFare.mMoveDistance+"", payType, AMBlestruct.AMCardResult.mFare + "", basePayType+"", AMBlestruct.AMCardFare.mAddCharge + "", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"
            , AMBlestruct.AMCardFare.mCallCharge + ""};
            Send_DTGData(3, param, 1);
//            Log.d("param_check", "TimsDtg " + param[0]);
        }

    }

    public void _sendPosition()
    {
        Send_DTGData(1, null, 0);

    }

    public void _sendDTGPowerON()
    {
        String param[] = {"1", "AM100"};
        Send_DTGData(2, param, 0);
    }

    public void _sendDTGPowerOFF()
    {
        String param[] = {"0", "AM100"};
        Send_DTGData(2, param, 0);
    }

    public void _sendDTGEventEmpty()
    {
        if(Info.SENDDTG == false)
            return;
            String[] param = {"05", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}; //20220411 add , "0"
            Send_DTGData(3, param, 0);

    }

    public void _sendDTGEventDrive()
    {
        if(Info.SENDDTG == false)
            return;

        String[] param = {"20", "0", "0", "0" + "", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}; //20220411 add , "0"
        Send_DTGData(3, param, 0);

    }

    public void _sendDTGEventPay()
    {
        if(Info.SENDDTG == false)
            return;

    }

    public void _sendDTGEventComplexON()
    {
        if(Info.SENDDTG == false)
            return;

         String[] param = {"34", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}; //20220411 add , "0"

        Send_DTGData(3, param, 0);
    }

    public void _sendDTGEventComplexOFF()
    {
        if(Info.SENDDTG == false)
            return;

        String[] param = {"35", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}; //20220411 add , "0"
        Send_DTGData(3, param, 0);
    }

}
