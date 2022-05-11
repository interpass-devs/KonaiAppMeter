package com.konai.appmeter.driver.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper  {

    private static final String DB_RECORD = "DrvRecord.db";
    private static final int DB_VERSION = 10;
//릴리즈버전 7
    /**
     * 1. 날짜 YYMMDD
     * 2. 운행시작시간
     * 3. 총 영업주행거리(km
     * 4. 총 빈차주행거리(km
     * 5. 총 요금(원
     * 6. 총 영업주행시간(초
     * 7. 총 빈차주행시간(초
     * 8. 기본요금 카운트   //(3800 건수)
     * 9. 이후요금 카운트   //(3800+추가요금 건수)
     * 10. 할증 기본요금 카운트
     * 11. 할증 이후요금 카운트
     */

    public static final String TOTAL_TABLE_NAME = "TOTALDRVDATA";

    public static final String tCOL_1 = "tDate";
    public static final String tCOL_2 = "tStartDate";
    public static final String tCOL_3 = "tPayDrv";
    public static final String tCOL_4 = "tEmptyDrv";
    public static final String tCOL_5 = "tPayment";
    public static final String tCOL_6 = "tPaySec";
    public static final String tCOL_7 = "tEmptySec";
    public static final String tCOL_8 = "tBasePayCnt";
    public static final String tCOL_9 = "tAfterPayCnt";
    public static final String tCOL_10 = "tExtraBPayCnt";
    public static final String tCOL_11 = "tExtraAPayCnt";

    private static final String TOT_DATABASE_CREATE_DRVRECORD = "create table "
            + TOTAL_TABLE_NAME
            + "("
            + tCOL_1 + " text primary key, "
            + tCOL_2 + " date,"
            + tCOL_3 + " integer,"
            + tCOL_4 + " integer,"
            + tCOL_5 + " integer,"
            + tCOL_6 + " integer,"
            + tCOL_7 + " integer,"
            + tCOL_8 + " integer,"
            + tCOL_9 + " integer,"
            + tCOL_10 + " integer,"
            + tCOL_11 + " integer"
            + ")";
    /**
     * 1. drvCode : 운행코드
     * 2. drvDivision : 할증여부(0: 기본 / 1: 할증)
     * 3. drvPay : 요금
     * 4. drvPayDivision : 결제종류(0: 현금 / 1: 카드 / 2: 모바일)
     * 5. addPay : 추가요금
     * 6. coordsX : x좌표 모음
     * 7. coordsY : y좌표 모음
     * 8. 시작시간.
     * 9. 종료시간.
     * 10. 거리
     * 11. 시간
     * 12. 빈차 0 승차 1
     */

    public static final String TABLE_NAME = "DRVRECORDS";

    public static final String COL_1 = "drvCode";
    public static final String COL_2 = "drvDivision";
    public static final String COL_3 = "drvPay";
    public static final String COL_4 = "drvPayDivision";
    public static final String COL_5 = "addPay";
    public static final String COL_6 = "coordsX";
    public static final String COL_7 = "coordsY";
    public static final String COL_8 = "sDate";
    public static final String COL_9 = "eDate";
    public static final String COL_10 = "distance";
    public static final String COL_11 = "elapes";
    public static final String COL_12 = "runmode";

    private static final String DATABASE_CREATE_DRVRECORD = "create table "
            + TABLE_NAME
            + "(" + COL_1 + " text primary key, "
            + COL_2 + " integer,"
            + COL_3 + " integer,"
            + COL_4 + " integer,"
            + COL_5 + " integer,"
            + COL_6 + " text,"
            + COL_7 + " text,"
            + COL_8 + " date,"
            + COL_9 + " date,"
            + COL_10 + " integer,"
            + COL_11 + " integer,"
            + COL_12 + " integer"
            + ")";

    /**
     *
     * @param context
     */


    /**
     * 로그인 정보
     * 1. num :
     * 2. name : 운전자 이름
     * 3. license_num : 운전자 자격증 번호
     * **/
    public static final String MEMBER_TABLE_NAME = "DRIVERMEMBER";

    public static final String mCOL_1 = "num";
    public static final String mCOL_2 = "name";
    public static final String mCOL_3 = "licenseNum";
    public static final String mCOL_4 = "identiNum";

    private static final String DATABASE_CREATE_MEMBER = "create table "
            + MEMBER_TABLE_NAME
            + "("
            + mCOL_2 + " text,"
            + mCOL_3 + " text primary key,"
            + mCOL_4 + " text"
            + ")";

////////////////////
//20220415
    /**
     * tims전송 정보
     * 1. 날짜
     * 2. 운행관리코드
     * 3. 전송여부.
     * 4. 전송내용.(uridata)
     * **/
    public static final String TIMS_TABLE_NAME = "TIMSDATA";

    public static final String TIMSCOL_1 = "drvdate";
    public static final String TIMSCOL_2 = "url";
    public static final String TIMSCOL_3 = "sendyn";
    public static final String TIMSCOL_4 = "event";
    public static final String TIMSCOL_5 = "contents";

    private static final String DATABASE_CREATE_TIMSDATA = "create table "
            + TIMS_TABLE_NAME
            + "("
            + TIMSCOL_1 + " text primary key,"
            + TIMSCOL_2 + " text,"
            + TIMSCOL_3 + " integer,"
            + TIMSCOL_4 + " integer,"
            + TIMSCOL_5 + " text"
            + ")";

//20220415
    /**
     * dtg전송 정보
     * 1. 날짜
     * 2. 운행관리코드
     * 3. 전송여부.
     * 4. 전송내용.(uridata)
     * **/
    public static final String DTG_TABLE_NAME = "DTGDATA";

    public static final String DTGCOL_1 = "drvdate";
    public static final String DTGCOL_2 = "keycode";
    public static final String DTGCOL_3 = "sendyn";
    public static final String DTGCOL_4 = "contents";

    private static final String DATABASE_CREATE_DTGDATA = "create table "
            + DTG_TABLE_NAME
            + "("
            + DTGCOL_1 + " text primary key,"
            + DTGCOL_2 + " text,"
            + DTGCOL_3 + " integer,"
            + DTGCOL_4 + " text"
            + ")";
/////////////////

    public SQLiteHelper(Context context) {
        super(context, DB_RECORD, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_DRVRECORD);
        db.execSQL(TOT_DATABASE_CREATE_DRVRECORD);
        db.execSQL(DATABASE_CREATE_MEMBER);

        db.execSQL(DATABASE_CREATE_TIMSDATA);
        db.execSQL(DATABASE_CREATE_DTGDATA);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TOTAL_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MEMBER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TIMS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DTG_TABLE_NAME);
        onCreate(db);
    }


}