package com.konai.appmeter.driver.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.struct.CalFareBase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SQLiteControl {

    SQLiteHelper helper;
    SQLiteDatabase sqlite;

    //생성자
    public SQLiteControl(SQLiteHelper _helper) {
        this.helper = _helper;
    }



    /** 등록된 운전자 정보 쿼리 **/
    public String[] selectMemberList(){

        sqlite = helper.getWritableDatabase();
        helper.getReadableDatabase();

        Cursor c = sqlite.query(helper.MEMBER_TABLE_NAME, null, null, null, null, null, null, null);

        String[] columnName = {helper.mCOL_2, helper.mCOL_3, helper.mCOL_4};

        String[] returnValue = new String[columnName.length];
        String[] getData = new String[c.getCount()];

        int cntValue = 0;

        while (c.moveToNext()){
            String addData = "";

            for (int i=0; i<returnValue.length; i++){
                if (i==2){
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                    getData[cntValue] = addData;
                    cntValue++;
                }else if (i==0){
                    addData += c.getString(c.getColumnIndex(columnName[i]));
                }else {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                }
                returnValue[i] = c.getString(c.getColumnIndex(columnName[i]));
                Log.d("8888", returnValue[i]);
            }
        }//while..
        c.close();
        sqlite.close();

        Log.d("77777", getData.toString());

        return getData;
    }


    public long insertMember(String drvName, String drvLicenseNum, String drvIdentiNum){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(helper.mCOL_2, drvName );
        values.put(helper.mCOL_3, drvLicenseNum);
        values.put(helper.mCOL_4, drvIdentiNum);

        long result = sqlite.insert(helper.MEMBER_TABLE_NAME, null, values);
        sqlite.close();

        Log.d("insertMember", result+"");

        return result;
    }

    public void updateMember(String drvName, String licenseNum, String original_licenseNum, String identiNum){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(helper.mCOL_2, drvName );
        values.put(helper.mCOL_3, licenseNum);
        values.put(helper.mCOL_4, identiNum);

        sqlite.update(helper.MEMBER_TABLE_NAME, values, "licenseNum=?", new String[]{original_licenseNum});
        sqlite.close();
    }


    public void deleteMember(String licenseNum){
        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(helper.mCOL_1, licenseNum );

        sqlite.delete(helper.MEMBER_TABLE_NAME, "licenseNum=?",new String[]{licenseNum});
    }



    /** 블루투스 & 시경계 연결상태 **/

    public long insertConnStatus (String phoneNum, String carno, String logs, String logtime, String logtype, String log) {

        //get the data repository in write mode
        sqlite = helper.getWritableDatabase();

        //create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(helper.cCOL_2, phoneNum);
        values.put(helper.cCOL_3, carno);
        values.put(helper.cCOL_4, logs);
        values.put(helper.cCOL_5, logtime);
        values.put(helper.cCOL_6, logtype);
        values.put(helper.cCOL_7, log);

        //insert the new row, returning the primary key value of the new row
        long result = sqlite.insert(helper.CONN_STATUS_TABLE_NAME, null, values);
        sqlite.close();

        Log.d("insertConnStatus", result+"");

        return result;
    }


    //3일전 데이터 삭제
    public void deleteConnStatus() {

        sqlite = helper.getWritableDatabase();

        Date time = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String logtime = sdf.format(time) + "";

        sqlite.delete(helper.CONN_STATUS_TABLE_NAME, "logtime<strftime('%Y%m%d%H%M%S', datetime(?, '-3 days'))", new String[]{logtime});
        sqlite.close();
//        return logtime;
    }



    //3일치 데이터 fetch
    public String[] selectConnStatus() {

        sqlite = helper.getWritableDatabase();

        String today = null;
        String today_3 = null;

        Date time = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);

        today = sdf.format(time);

        cal.add(Calendar.DATE, -3);
        today_3 = sdf.format(cal.getTime());

//        Cursor cursor = sqlite.query(helper.CONN_STATUS_TABLE_NAME, null, null, null,null,null,null); //모든 데이터 fetch

//        Cursor cursor = sqlite.query(helper.CONN_STATUS_TABLE_NAME, null, "logtime BETWEEN '" + sdf.format(time) + " 00:00:00' AND '" + sdf.format(time) + " 23:59:59'", null, null, null, null, null);

        // 3일전 ~ 오늘
        Cursor cursor = sqlite.query(helper.CONN_STATUS_TABLE_NAME, null, "logtime BETWEEN '" + today_3 + " 00:00:00' AND '" + today + " 23:59:59'", null, null, null, null);


        String[] columnName = { helper.cCOL_2, helper.cCOL_3, helper.cCOL_4, helper.cCOL_5, helper.cCOL_6, helper.cCOL_7};
        String[] returnValue = new String[columnName.length]; //0~3

//        Log.d("returnValue", returnValue.toString()+" 개");
        String[] getData = new String[cursor.getCount()];

        int cntValue = 0;

        while (cursor.moveToNext()) {

            String addData = "";

            for (int i=0; i<returnValue.length; i++) {

                if (i==5) {
                  addData += "#" + cursor.getString(cursor.getColumnIndex(columnName[i]));
                  getData[cntValue] = addData;
                  cntValue++;
                }else if (i==0) {
                    addData += cursor.getString(cursor.getColumnIndex(columnName[i]));
                }else {
                    addData += "#" + cursor.getString(cursor.getColumnIndex(columnName[i]));
                }
                returnValue[i] = cursor.getString(cursor.getColumnIndex(columnName[i]));
//                Log.d("returnValue", returnValue[i]);

            }//for

        }//while

        cursor.close();
        sqlite.close();

        return getData;
    }







    /** Drive TABLE QUERY **/

    public String[] select() {

        if(Info.USEDBRUNDATA == false)
            return new String[1];

        sqlite = helper.getWritableDatabase();
        Cursor c = sqlite.query(helper.TABLE_NAME, null, null, null, null, null, helper.COL_1+" desc", null);

        String[] columnName = {helper.COL_1,helper.COL_2,helper.COL_3,helper.COL_4,helper.COL_5,
                helper.COL_6,helper.COL_7,helper.COL_8,helper.COL_9, helper.COL_10, helper.COL_11, helper.COL_12};
        String[] returnValue = new String[columnName.length];

        //Log.e("getCount", c.getCount() + "");
        String[] getData = new String[c.getCount()];

        int cntValue = 0;

        while (c.moveToNext()) {
            String addData = "";

            for(int i=0; i<returnValue.length; i++) {

                if(i == 11) {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                    getData[cntValue] = addData;
                    cntValue++;
                } else if(i == 0) {
                    addData += c.getString(c.getColumnIndex(columnName[i]));
                } else {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                }
                returnValue[i] = c.getString(c.getColumnIndex(columnName[i]));
                //Log.e("DB Select : ", i + " - " + returnValue[i]);
            }
        }
        c.close();
        sqlite.close();

        return getData;
    }

    public String[] selectToday() {

        if(Info.USEDBRUNDATA == false)
            return new String[0];

        sqlite = helper.getWritableDatabase();

        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");

        Cursor c = sqlite.query(helper.TABLE_NAME, null, "sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59'", null, null, null, helper.COL_1+" desc", null);  //todo: 20210902

        String[] columnName = {helper.COL_1,helper.COL_2,helper.COL_3,helper.COL_4,helper.COL_5,
                helper.COL_6,helper.COL_7,helper.COL_8,helper.COL_9, helper.COL_10, helper.COL_11, helper.COL_12};
        String[] returnValue = new String[columnName.length];

        Log.d("getCount", c.getCount() + "");
        String[] getData = new String[c.getCount()];

        int cntValue = 0;

        while (c.moveToNext()) {
            String addData = "";

            for(int i=0; i<returnValue.length; i++) {

                if(i == 11) {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                    // Log.d("addDataCheck", addData);
                    getData[cntValue] = addData;
                    // Log.d("getDataCheck", getData[cntValue]);
                    cntValue++;
                } else if(i == 0) {
                    addData += c.getString(c.getColumnIndex(columnName[i]));
                } else {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                }
                returnValue[i] = c.getString(c.getColumnIndex(columnName[i]));
                //Log.e("DB Select : ", i + " - " + returnValue[i]);
            }
        }
        c.close();
        sqlite.close();

//        Log.d("today_data!", getData[]);

        return getData;
    }

    //세부내역 데이터 (1 row : 리스트 아님)
    public String[] selectedRecordDetail(String drvCode){
        if(Info.USEDBRUNDATA == false)
            return new String[1];

        sqlite = helper.getWritableDatabase();

        Cursor c = sqlite.query(helper.TABLE_NAME, null, "drvCode = '" + drvCode + "'", null, null, null, null, null );

        String[] columnName = {helper.COL_1,helper.COL_2,helper.COL_3,helper.COL_4,helper.COL_5,
                helper.COL_6,helper.COL_7,helper.COL_8,helper.COL_9, helper.COL_10, helper.COL_11, helper.COL_12};

        String[] returnValue = new String[columnName.length];  //0~11

        String[] getData = new String[c.getCount()];

        int cntValue = 0;

        while (c.moveToNext()){
            String addData = "";   // 컬럼 하나씩 붙여서 row 한줄을 String 으로 만들기..

            for (int i=0; i<returnValue.length; i++){

                if (i == 11){
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                    // Log.d("addDataCheck", addData);
                    getData[cntValue] = addData;
                    //Log.d("getDataCheck", getData[cntValue]);
                    cntValue++;
                }else if (i == 0){
                    addData += c.getString(c.getColumnIndex(columnName[i]));
                }else {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                }
            }//for..
        }//while..
        c.close();
        sqlite.close();

        return getData;
    }

    //운행코드 생성
    public String selectKey() {
        if(Info.USEDBRUNDATA == false)
            return "0";
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT IFNULL(MAX(drvCode), '0') FROM " + helper.TABLE_NAME;
        Cursor cursor = null;
        String data = "";

        try {
            cursor = sqlite.rawQuery(selectkey, null);
            while (cursor.moveToNext()) {
                data += cursor.getString(0);
                break;
            }
        } catch(Exception e) {
            data = "0";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }


    public void setUpdateLocation(String key, int drvPay, int drvPayDivision, int addPay,  String xPos, String yPos, int distance, int seconds, int state) {
        if(Info.USEDBRUNDATA == false)
            return;

        if(Info.USEDBLOCATIONDATA == false && state == 0)
            return;

        String o_CoordsX = "";
        String o_CoordsY = "";

        sqlite = helper.getWritableDatabase();
        String getCoordsX = "SELECT coordsX FROM " + helper.TABLE_NAME + " WHERE drvCode = '" + key +"'";
        String getCoordsY = "SELECT coordsY FROM " + helper.TABLE_NAME + " WHERE drvCode = '" + key +"'";
        Cursor cursor = null;
        cursor = sqlite.rawQuery(getCoordsX,null);

        while (cursor.moveToNext()) {
            o_CoordsX += cursor.getString(0);
            Log.d("X좌표", o_CoordsX);
            break;
        }

        cursor = sqlite.rawQuery(getCoordsY,null);

        while (cursor.moveToNext()) {
            o_CoordsY += cursor.getString(0);
            break;
        }

        ContentValues value = new ContentValues();

        // State == 0 : 계속운행 / State == 99 : 해당 운행종료
        if(state == 99) {
            Date time = new Date();
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            o_CoordsX += "|" + xPos;
            o_CoordsY += "|" + yPos;

            value.put("drvCode", key);
            value.put("drvPay", drvPay);
            value.put("drvPayDivision", drvPayDivision);
            value.put("addPay", addPay);
            value.put("coordsX", o_CoordsX);
            value.put("coordsY", o_CoordsY);
            value.put("eDate", transFormat.format(time));
            value.put("distance", distance);
            // Log.d("distanceCheck", distance+"");
            value.put("elapes", seconds);

            sqlite.update(helper.TABLE_NAME, value, "drvCode=?", new String[]{key});
        } else {
            o_CoordsX += "|" + xPos;
            o_CoordsY += "|" + yPos;

            value.put("drvCode", key);
            value.put("coordsX", o_CoordsX);
            value.put("coordsY", o_CoordsY);

            sqlite.update(helper.TABLE_NAME, value, "drvCode=?", new String[]{key});
        }
        sqlite.close();
    }

    public void insert(String drvCode, int drvDivision, int drvPay, int drvPayDivision, int addPay, String coordsX, String coordsY, int nmode) {
        if(Info.USEDBRUNDATA == false)
            return ;

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        values.put("drvCode", drvCode);
        values.put("drvDivision", drvDivision);
        values.put("drvPay", drvPay);
        values.put("drvPayDivision", drvPayDivision);
        values.put("addPay", addPay);
        values.put("coordsX", coordsX);
        values.put("coordsY", coordsY);
        values.put("sDate", transFormat.format(time));
        values.put("eDate", "-");
        values.put("distance", 0);
        values.put("elapes", 0);
        values.put("runmode", nmode);

        sqlite.insert(helper.TABLE_NAME, null, values);

        sqlite.close();
    }

    /**  TOTAL TABLE QUERY **/

    public void insertTotalData() {
        if(Info.USEDBRUNDATA == false)
            return;

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        values.put(helper.tCOL_1, transFormat.format(time));
        values.put(helper.tCOL_2, stTransFormat.format(time));
        values.put(helper.tCOL_3, 0);
        values.put(helper.tCOL_4, 0);
        values.put(helper.tCOL_5, 0);
        values.put(helper.tCOL_6, 0);
        values.put(helper.tCOL_7, 0);
        values.put(helper.tCOL_8, 0);
        values.put(helper.tCOL_9, 0);
        values.put(helper.tCOL_10, 0);
        values.put(helper.tCOL_11, 0);

        sqlite.insert(helper.TOTAL_TABLE_NAME, null, values);
        sqlite.close();
    }

    public String getTotalKey() {
        if(Info.USEDBRUNDATA == false)
            return "0";
        sqlite = helper.getWritableDatabase();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String selectkey = "SELECT IFNULL(tDate, '0') FROM " + helper.TOTAL_TABLE_NAME + " WHERE tDate = '" + transFormat.format(time) + "'";
        Cursor cursor = null;
        String data = "";

        try {
            cursor = sqlite.rawQuery(selectkey, null);
            while (cursor.moveToNext()) {
                data += cursor.getString(0);
                break;
            }
        } catch(Exception e) {
            data = "0";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }

    //당일 이력
    public String todayTotSelect() {
        String data = "0/0/0/0/0";
        if(Info.USEDBRUNDATA == false)
            return data;

        sqlite = helper.getWritableDatabase();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String selectkey = "SELECT " + helper.tCOL_3 + ", " + helper.tCOL_4 + ", " + helper.tCOL_6 + ", " + helper.tCOL_7 + ", " + helper.tCOL_5 + " FROM " + helper.TOTAL_TABLE_NAME + " WHERE tDate = '" + transFormat.format(time) + "'";
        Cursor cursor = null;

        try {
            cursor = sqlite.rawQuery(selectkey, null);
            while (cursor.moveToNext()) {
                data += cursor.getInt(0);
                data += "/" + cursor.getInt(1);
                data += "/" + cursor.getInt(2);
                data += "/" + cursor.getInt(3);
                data += "/" + cursor.getInt(4);
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }

    public String todayPaymentCount() {
        String data = "0/0";
        if(Info.USEDBRUNDATA == false)
            return data;
        sqlite = helper.getWritableDatabase();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String cashCnt = "SELECT COUNT(" + helper.COL_4 + ") FROM " + helper.TABLE_NAME + " WHERE sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59' AND " + helper.COL_4 + " = 0";
        String cardCnt = "SELECT COUNT(" + helper.COL_4 + ") FROM " + helper.TABLE_NAME + " WHERE sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59' AND " + helper.COL_4 + " = 1";
        Cursor cursor = null;

        try {
            cursor = sqlite.rawQuery(cashCnt, null);
            while (cursor.moveToNext()) {
                data += cursor.getInt(0);
                /*data += "/" + cursor.getInt(1);
                data += "/" + cursor.getInt(2);
                data += "/" + cursor.getInt(3);
                data += "/" + cursor.getInt(4);*/
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }

        try {
            cursor = sqlite.rawQuery(cardCnt, null);
            while (cursor.moveToNext()) {
                data += "/" + cursor.getInt(0);
                /*data += "/" + cursor.getInt(1);
                data += "/" + cursor.getInt(2);
                data += "/" + cursor.getInt(3);
                data += "/" + cursor.getInt(4);*/
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }

    public String todayDriveCount() {
        String data = "0";
        if(Info.USEDBRUNDATA == false)
            return data;
        sqlite = helper.getWritableDatabase();
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvCnt = "SELECT COUNT(" + helper.COL_1 + ") FROM " + helper.TABLE_NAME + " WHERE sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59'";
        //String drvCnt = "SELECT COUNT(" + helper.COL_1 + ") FROM " + helper.TABLE_NAME + " WHERE sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59' AND " + helper.COL_4 + " AND " + helper.COM_12 + " = 1";
        Cursor cursor = null;

        try {
            cursor = sqlite.rawQuery(drvCnt, null);
            while (cursor.moveToNext()) {
                data += cursor.getInt(0);
                /*data += "/" + cursor.getInt(1);
                data += "/" + cursor.getInt(2);
                data += "/" + cursor.getInt(3);
                data += "/" + cursor.getInt(4);*/
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }


    //종합거래 총 횟수
    public String totalDriveCount(){
        String data = "0";
        if(Info.USEDBRUNDATA == false)
            return data;
        sqlite = helper.getWritableDatabase();
        String totalDrvCnt = "SELECT COUNT(" + helper.COL_1 + ") FROM " + helper.TABLE_NAME;
        Cursor cursor = null;

        try {
            cursor = sqlite.rawQuery(totalDrvCnt, null);
            while (cursor.moveToNext()) {
                data += cursor.getInt(0);
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }




    public String[] totSelect() {

        if(Info.USEDBRUNDATA == false)
            return new String[1];

        sqlite = helper.getWritableDatabase();
        Cursor c = sqlite.query(helper.TOTAL_TABLE_NAME, null, null, null, null, null, null, null);

        String[] columnName = {helper.tCOL_1,helper.tCOL_2,helper.tCOL_3,helper.tCOL_4,helper.tCOL_5,helper.tCOL_6,helper.tCOL_7,helper.tCOL_8,helper.tCOL_9,helper.tCOL_10,helper.tCOL_11};
        String[] returnValue = new String[columnName.length];

        //Log.e("getCount", c.getCount() + "");
        String[] getData = new String[c.getCount()];

        int cntValue = 0;

        while (c.moveToNext()) {
            String addData = "";

            for(int i=0; i<returnValue.length; i++) {

                if(i == 10) {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                    getData[cntValue] = addData;
                    cntValue++;
                } else if(i == 0) {
                    addData += c.getString(c.getColumnIndex(columnName[i]));
                } else {
                    addData += "#" + c.getString(c.getColumnIndex(columnName[i]));
                }
                returnValue[i] = c.getString(c.getColumnIndex(columnName[i]));
                //Log.e("DB Select : ", i + " - " + returnValue[i]);
            }
        }
        c.close();
        sqlite.close();

        return getData;
    }

    public void setUpdateTotalData(int drvDiv, int payment, int distance, int seconds, int extra) {

        if(Info.USEDBRUNDATA == false)
            return;
        Date time = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sdate = transFormat.format(time);

        if(drvDiv == 0) { // 0: 빈차 | 1: 영업

            Info.insert_totaldata();

        }
        else
        {
            String stmp = _getLastDriveDate(Info.g_nowKeyCode);
            if(!stmp.equals(""))
                sdate = stmp;
        }

        sqlite = helper.getWritableDatabase();

        String col_1;
        String col_2;
        String col_3;
        String col_4;

        int oldDist = 0;
        int oldSec = 0;
        int oldPayment = 0;
        int oldBasePayCount = 0;
        int oldAfterPayCount = 0;

        String getQuery = "";

        if(drvDiv == 0) { // 0: 빈차 | 1: 영업
            col_1 = helper.tCOL_4;
            col_2 = helper.tCOL_7;
            getQuery = "SELECT " + col_1 + ", " + col_2 + " FROM " + helper.TOTAL_TABLE_NAME + " WHERE tDate = '" + sdate + "'";
        } else {
            col_1 = helper.tCOL_3;
            col_2 = helper.tCOL_6;

            if(sdate.indexOf(" ") > 0)
                sdate = sdate.substring(0, sdate.indexOf(" "));

            getQuery = "SELECT " + col_1 + ", " + col_2 + " FROM " + helper.TOTAL_TABLE_NAME + " WHERE tDate = '" + sdate + "'";
        }

        Cursor cursor = null;
        cursor = sqlite.rawQuery(getQuery,null);

        while (cursor.moveToNext()) {
            oldDist = cursor.getInt(0);
            oldSec = cursor.getInt(1);
            //Log.e("Get Total Datas!", oldDist + " / " + oldSec);
            break;
        }

        ContentValues value = new ContentValues();

        if(drvDiv == 0) {

            value.put(col_1, oldDist + distance);
            value.put(col_2, oldSec + seconds);

            sqlite.update(helper.TOTAL_TABLE_NAME, value, "tDate=?", new String[]{sdate});

        } else if(drvDiv == 1) {
            if(extra == 0) {
                col_3 = helper.tCOL_8;
                col_4 = helper.tCOL_9;
            } else {
                col_3 = helper.tCOL_10;
                col_4 = helper.tCOL_11;
            }


            String getPayQuery = "SELECT " + helper.tCOL_5 + "," + col_3 + "," + col_4 + " FROM " + helper.TOTAL_TABLE_NAME + " WHERE tDate = '" + sdate + "'";

            cursor = sqlite.rawQuery(getPayQuery,null);

            while (cursor.moveToNext()) {
                oldPayment = cursor.getInt(0);
                oldBasePayCount = cursor.getInt(1);
                oldAfterPayCount = cursor.getInt(2);
                break;
            }

            if(payment == CalFareBase.BASECOST || payment == CalFareBase.BASECOSTEXTRATIME) {
                value.put(col_3, oldBasePayCount + 1);
            } else {
                value.put(col_4, oldAfterPayCount + 1);
            }

            value.put(col_1, oldDist + distance);
            value.put(col_2, oldSec + seconds);
            value.put(helper.tCOL_5, oldPayment + payment);

            sqlite.update(helper.TOTAL_TABLE_NAME, value, "tDate=?", new String[]{sdate});
        }
        sqlite.close();

    }


    public String _getLastDriveDate(String sKeycode)
    {
        String data = "0";
        if(Info.USEDBRUNDATA == false)
            return data;

        sqlite = helper.getWritableDatabase();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = "SELECT " + helper.COL_8 + " FROM " + helper.TABLE_NAME + " WHERE drvCode = '" + sKeycode + "'";
        //String drvCnt = "SELECT COUNT(" + helper.COL_1 + ") FROM " + helper.TABLE_NAME + " WHERE sDate BETWEEN '" + transFormat.format(time) + " 00:00:00' AND '" + transFormat.format(time) + " 23:59:59' AND " + helper.COL_4 + " AND " + helper.COM_12 + " = 1";
        Cursor cursor = null;

        try {
            cursor = sqlite.rawQuery(drvdate, null);
            while (cursor.moveToNext()) {
                data = cursor.getString(0);
                break;
            }
        } catch(Exception e) {
            data = "";
            e.printStackTrace();
        }
        sqlite.close();

        return data;
    }

    public void delete(String drvCode) {
        sqlite = helper.getWritableDatabase();
        sqlite.delete(helper.TABLE_NAME, "drvCode=?", new String[]{drvCode});

    }

/////////////////////////
    /**  TIMSDATA TABLE INSERT **/
    public String insertTimsdata(String url, String sdata, int event, int iok) {
        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String drvdate = stTransFormat.format(time);
        values.put(helper.TIMSCOL_1, drvdate);
        values.put(helper.TIMSCOL_2, url);
        values.put(helper.TIMSCOL_3, iok);
        values.put(helper.TIMSCOL_4, event);
        values.put(helper.TIMSCOL_5, sdata);

        long n = 0;
        n = sqlite.insert(helper.TIMS_TABLE_NAME, null, values);
        sqlite.close();

//        Log.d("TimsTIMS3", "sqlindex " + drvdate + " " + event + " " + sdata.length());

        return drvdate;

    }

    /** TIMSDATA TABLE QUERY **/
    public String[] selectTimsResenddata() {
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT " + helper.TIMSCOL_1 + "," + helper.TIMSCOL_2 + "," + helper.TIMSCOL_3 + "," + helper.TIMSCOL_4 + "," + helper.TIMSCOL_5
                + " FROM " + helper.TIMS_TABLE_NAME + " ORDER BY " + helper.TIMSCOL_1 + " ASC LIMIT 1";
        Cursor c = sqlite.rawQuery(selectkey, null);

        String[] returnValue = new String[5];

        int cntValue = 0;

        returnValue[0] = "FAIL";

        while (c.moveToNext()) {
            returnValue[0] = c.getString(0);
            returnValue[1] = c.getString(1);
            returnValue[2] = c.getInt(2) + "";
            returnValue[3] = c.getString(3) + "";
            returnValue[4] = c.getString(4) + "";

            break;
        }//while..

        c.close();
        sqlite.close();

        return returnValue;
    }

    public String[] selectLastTimsdata(){
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT " + helper.TIMSCOL_1 + "," + helper.TIMSCOL_2 + "," + helper.TIMSCOL_3 + "," + helper.TIMSCOL_4 + "," + helper.TIMSCOL_5 +
                " FROM " + helper.TIMS_TABLE_NAME + " ORDER BY " + helper.TIMSCOL_1 + " DESC LIMIT 1";
        Cursor c = sqlite.rawQuery(selectkey, null);

        String[] returnValue = new String[5];

        int cntValue = 0;

        returnValue[0] = "FAIL";

        while (c.moveToNext()){

            returnValue[0] = c.getString(0);
            returnValue[1] = c.getString(1);
            returnValue[2] = c.getInt(2) + "";
            returnValue[3] = c.getInt(3) + "";
            returnValue[4] = c.getString(4);

            break;
        }//while..
        c.close();
        sqlite.close();

        return returnValue;
    }

    /** TIMSDATA TABLE UPDATE **/
    public void updateTimsdata(String drvdate){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(helper.TIMSCOL_3, 1);

        sqlite.update(helper.TIMS_TABLE_NAME, values, "drvdate=?", new String[]{drvdate});
        sqlite.close();
    }

    /** TIMSDATA TABLE CLEAR **/
    public void updateTimsdataClear(){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = stTransFormat.format(time) + "";

        values.put(helper.TIMSCOL_3, 1);

        sqlite.update(helper.TIMS_TABLE_NAME, values, "drvdate<strftime('%Y%m%d%H%M%S', datetime(?, '-3 days'))", new String[]{drvdate});

        sqlite.close();
    }

    /** TIMSDATA TABLE DELETE **/
    public void deleteTimsdata() {

        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = stTransFormat.format(time) + "";
        sqlite = helper.getWritableDatabase();
        sqlite.delete(helper.TIMS_TABLE_NAME, "drvdate<strftime('%Y%m%d%H%M%S', datetime(?, '-3 days'))", new String[]{drvdate});
        sqlite.close();
    }

    /** TIMSDATA TABLE DELETE WITH DATE**/
    public void deleteTimsdata(String drvdate) {
        sqlite = helper.getWritableDatabase();
        sqlite.delete(helper.TIMS_TABLE_NAME, "drvdate = ?", new String[]{drvdate});
        sqlite.close();
    }
//////////////////////

    /**  DGTDATA TABLE INSERT **/
    public void insertDtgdata(String keycode, String sdata, int iok) {
        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        values.put(helper.DTGCOL_1, stTransFormat.format(time));
        values.put(helper.DTGCOL_2, keycode);
        values.put(helper.DTGCOL_3, iok);
        values.put(helper.DTGCOL_4, sdata);

        sqlite.insert(helper.DTG_TABLE_NAME, null, values);
        sqlite.close();
    }

    /** DTGDATA TABLE QUERY **/
    public String[] selectDtgResenddata(){
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT " + helper.DTGCOL_1 + "," + helper.DTGCOL_2 + "," + helper.DTGCOL_3 + "," + helper.DTGCOL_4 + " FROM "
                + helper.DTG_TABLE_NAME + " WHERE " + helper.DTGCOL_3 + " = 0 ORDER BY " + helper.DTGCOL_1 + " ASC LIMIT 1";
        Cursor c = sqlite.rawQuery(selectkey, null);

        String[] returnValue = new String[4];

        int cntValue = 0;

        returnValue[0] = "FAIL";

        while (c.moveToNext()){
            returnValue[0] = c.getString(0);
            returnValue[1] = c.getString(1);
            returnValue[2] = c.getInt(2) + "";
            returnValue[3] = c.getString(3);

            break;
        }//while..
        c.close();
        sqlite.close();

//        Log.d("TimsDtg", returnValue[0] + " " + returnValue[1] + " " + returnValue[2]);
        return returnValue;
    }

    public String[] selectLastDtgdata(){
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT " + helper.DTGCOL_1 + "," + helper.DTGCOL_2 + "," + helper.DTGCOL_3 + "," + helper.DTGCOL_4 + " FROM "
                + helper.DTG_TABLE_NAME + " ORDER BY " + helper.DTGCOL_1 + " DESC LIMIT 1";
        Cursor c = sqlite.rawQuery(selectkey, null);

        String[] returnValue = new String[4];

        int cntValue = 0;

        returnValue[0] = "FAIL";

        while (c.moveToNext()){

            returnValue[0] = c.getString(0);
            returnValue[1] = c.getString(1);
            returnValue[2] = c.getInt(2) + "";
            returnValue[3] = c.getString(3);

            break;
        }//while..
        c.close();
        sqlite.close();

        return returnValue;
    }

    /** DTGDATA TABLE UPDATE **/
    public void updateDtgdata(String drvdate){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(helper.DTGCOL_3, 1);

        sqlite.update(helper.DTG_TABLE_NAME, values, "drvdate=?", new String[]{drvdate});
        sqlite.close();
    }

    /** DTGDATA TABLE CLEAR **/
    public void updateDtgdataClear(){

        sqlite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = stTransFormat.format(time) + "";

        values.put(helper.DTGCOL_3, 1);

        sqlite.update(helper.DTG_TABLE_NAME, values, "drvdate<strftime('%Y%m%d%H%M%S', datetime(?, '-3 days'))", new String[]{drvdate});

        sqlite.close();
    }

    /** DTGDATA TABLE DELETE **/
    public void deleteDtgdata() {
        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = stTransFormat.format(time) + "";
        sqlite = helper.getWritableDatabase();
        sqlite.delete(helper.DTG_TABLE_NAME, "drvdate<strftime('%Y%m%d%H%M%S', datetime(?, '-1 months'))", new String[]{drvdate});
        sqlite.close();
    }

    public void selecttest(){
        Date time = new Date();
        SimpleDateFormat stTransFormat = new SimpleDateFormat("yyyy-MM-dd");
        String drvdate = stTransFormat.format(time) + "";
        sqlite = helper.getWritableDatabase();
        String selectkey = "SELECT strftime('%Y%m%d%H%M%S', datetime('" + drvdate + "', '-1 days')) as strtime";
//        String selectkey = "SELECT strftime('%Y%m%d%H%M%S', datetime(" + drvdate + ", '-1 days')) as strtime";
        Cursor c = sqlite.rawQuery(selectkey, null);

        String sreturn = "";

        int cntValue = 0;

        while (c.moveToNext()){

            sreturn = c.getString(0);

            break;
        }//while..
        c.close();
        sqlite.close();

//        Log.d("TimsDtg", "-------" + sreturn + " " + drvdate);


    }

    public void closeDB() {
        sqlite.close();
        helper.close();
    }

}
