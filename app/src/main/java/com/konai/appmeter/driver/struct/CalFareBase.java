package com.konai.appmeter.driver.struct;

public class CalFareBase {


    public static double distanceForAdding = 0;
    public static double timeForAdding = 0;
    /**
     * 성남
     */
    //기본요금
    public static int BASECOST = 3800;
    //20201203
    //심야할증기본요금
    public static int BASECOSTEXTRATIME = 4560;
    //시외할증기본요금
    public static int BASECOSTEXTRASUBURB = 4560;
    //복합할증기본요금
    public static int BASECOSTEXTRACOMPLEX = 4560;
    //거리당요금
    public static int DISTCOST = 100;
    //시간당요금
    public static int TIMECOST = 100;
    //시간별제한속도
//    public static int TIMECOST_LIMITHOUR = 15; //hour speed
//    public static int TIMECOST_LIMITSECOND = (int)(TIMECOST_LIMITHOUR / 3.6); //second speed
    public static double TIMECOST_LIMITHOUR = 15.0; //hour speed
    public static double TIMECOST_LIMITSECOND = (TIMECOST_LIMITHOUR / 3.6); //second speed
    //기본요금 거리
    public static int BASEDRVDIST = 2000;
    //기본요금 거리(테스트용)
    //public static int BASEDRVDIST = 100;
    //거리간격
    public static double INTERVAL_DIST = 132; //20201211
    //시간간격
    public static double INTERVAL_TIME = 31; //20201211

//20201211
//20210703    public static double BASEDIST_PER1S = INTERVAL_DIST / INTERVAL_TIME; //20201211 for double
    public static double BASEDIST_PER1S = INTERVAL_DIST / INTERVAL_TIME; // - 0.005; //20201211 for double

    public static int distanceLimit = 0;

    //고객 지불금액
    public static int PAYMENT_COST = 0;

    public static int tLeftDist = 0;

    //이동총거리
    public static double tDistance = 0;
    public static int drvOperatingTime = 0;

    public static int mDistance = 0;

    public static double mComplexrate = 0.0; //복함.
    public static double mNightTimerate = 0.2; //심야할증.
    public static double mSuburbrate = 0.2; //20201203 시외.

    public static int CALTYPE = 0; //20210416 0 동시, 1 시간만계산, 2 완전 시간을요금 ?, 3거리만계산, 4 상호병산, 5 기본거리검사

    /** 인천
     * //기본요금
     public static int BASECOST = 3800;
     //20201203
     //심야할증기본요금
     public static int BASECOSTEXTRATIME = 4560;
     //시외할증기본요금
     public static int BASECOSTEXTRASUBURB = 4940;
     //복합할증기본요금
     public static int BASECOSTEXTRACOMPLEX = 4560;
     //거리당요금
     public static int DISTCOST = 100;
     //시간당요금
     public static int TIMECOST = 100;
     //시간별제한속도
     public static int TIMECOST_LIMITHOUR = 15; //hour speed
     public static int TIMECOST_LIMITSECOND = (int)(TIMECOST_LIMITHOUR / 3.6); //second speed
     //기본요금 거리
     public static int BASEDRVDIST = 2000;
     //기본요금 거리(테스트용)
     //public static int BASEDRVDIST = 100;
     //거리간격
     public static double INTERVAL_DIST = 135; //20201211
     //시간간격
     public static double INTERVAL_TIME = 33; //20201211

     //20201211
     public static double BASEDIST_PER1S = INTERVAL_DIST / INTERVAL_TIME; //20201211 for double

     public static int distanceLimit = 0;

     //고객 지불금액
     public static int PAYMENT_COST = 0;

     public static int tLeftDist = 0;

     //이동총거리
     public static double tDistance = 0;
     public static int drvOperatingTime = 0;

     public static int mDistance = 0;

     public static double mComplexrate = 0.0; //복함.
     public static double mNightTimerate = 0.2; //심야할증.
     public static double mSuburbrate = 0.3; //20201203 시외.
     */

}

