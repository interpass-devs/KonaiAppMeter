package com.konai.appmeter.driver.struct;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AMBlestruct {

    public static double katchlat = 0;
    public static double katchlon = 0;
    public static double wgslat = 0;
    public static double wgslon = 0;
    public static double speed = 0;
    public static int direction = 0;

    public static double beforekatchlat = 0;
    public static double beforekatchlon = 0;
    public static double beforewgslat = 0;
    public static double beforewgslon = 0;
    public static double beforespeed = 0;
    public static int beforedirection = 0;

    //20160819 tra..sh 서버로 전송한 최근좌표
    public static double Sendedwgslat = 0;
    public static double Sendedwgslon = 0;

    public static long lastpositiontime = 0; //20160819 tra..sh

    public static boolean gpscheck = true;
    public static String MSG = "";

    public static int CUT = 0; //gps수신값이없을동안 check 15초지나면 gps error, wgslat, wgslon 0
    //onLocationChanged함수 값을 clear.

    public static int GPSERRORTIME = 15; //20160805 tra..sh gps모드 15, network모드 30

    //20161106 tra..sh
    private  int historyIDX = 0;
    private  double[] speedhistory = new double[10];
    private  int[] intervalhistory = new int[10];
/////////

    public static boolean mBTConnected = false; //BLE device 연결상태.

    public static final String EMPTY = "05";
    public static final String BOARD = "20";

    public static String mParsingtime = ""; //.
    public static String mParsingResult = "00"; //.

    ///////// for 1s data수신 인증진행.
    //20201215
    public static boolean mb1sdata13code = false;
    public static boolean mb1sdata12code = false;
//20210407
    public static boolean mbSendfareOK = false; //AM100실시간요금diplay전달 ACK수신
    ////////////

    ///////////인증정보.
    public static class AMLicense { //20201110
        public static String licensecode = "071111122222"; //12자리
        public static String drivername = "홍길동"; //대전99바9999"; // 201200012345";//
        public static String taxinumber = "서울00아0000"; //대전99바9999"; // 201200012345";//
        public static String securecode = "BBF03160CA7394BD"; //16자리 BBF03160CA7394BD
        public static String licensetaxi = "00000000";
        public static String shacodetaxi = ""; //taxi sha 수신정보
        public static String shacodedriver = ""; //driver sha 송신정보.
        public static String phonenumber = "";
        public static String drivernum = "0000"; //사원번호.

        public static String companynum = "0000000000"; // "1228227201";

        public static String timscode = "0000000019";
        public static String timslicense = "000000000"; //""020400773";
        public static String timstaxinum = "서울00아0000";

        public static String companynumtmp = "3122783779";
        public static String timstaxinumtmp = "충남51바1985";
        public static String timslicensetmp = "071020365";

    }

    public static String mSState = ""; //단말전송빈승상태값
    public static boolean mbSStateupdated = false;

    public static String mRState = ""; //단말수신빈승상태값
    public static boolean mbRStateupdated = false;

    public static final byte OPEN = '1';
    public static final byte CLOSE = '2';
    ////////////////////
    public static class MeterState
    {
        public static final int EMPTY = 1;
        public static final int EMPTYBYEMPTY = 111; //20220413
        public static final int DRIVE = 2;
        public static final int POWERONDRIVE = 22;
        public static final int PAY = 3;
        public static final int SUBURBAN = 4;
        public static final int SUBURBANEXTRA = 41;
        public static final int COMPLEX = 5;
        public static final int COMPLEXEXTRA = 51;
        public static final int EXTRA = 6;
        public static final int HOLIDAY =8; //20210823
        public static final int APPACTIVATE =9; //20211220
        public static final int EXTRAAUTO = 61;
        public static final int APPOINT =7;
        public static final int ENDPAYMENT =10;
        public static final int ENDCANCELPAYMENT = 11; //20210512
        public static final int EMPTYPAY = 13;
        public static final int ADDPAY = 23;
        public static final int MANUALPAY = 33;
        public static final int CANCELPAY = 53;
        public static final int NONE = 99;
        public static final int EXTRATIME = 61; //20201203
        public static final int EXTRATIMEOFF = 161; //20201023
        public static final int EXTRACOMPLEX = 62; //20201203
        public static final int EXTRACOMPLEXOFF = 162; //20201203
        public static final int EXTRASUBURB = 63; //20201203
        public static final int EXTRASUBURBOFF = 163; //20201203
        public static final int BLELEDON = 70;
        public static final int BLELEDOFF = 170;
        public static final int BLE1SDATAOK = 80; //20201215
        public static final int BLE1SDATAERROR = 180; //20201215
        public static final int SUBURBSIN = 191; //20210325
        public static final int SUBURBSOUT = 192; //20210325
    }

    /////////menu.
    public static class AMmenu
    {
        public static byte mMenuState = '0';
        public static boolean mMenu = false; //메뉴동작상태
        public static boolean mbMenuupdated = false; //메뉴업데이트 화면변경유무
        public static byte menutype = '0'; //code42
        public static String menudisplay = "";
        public static byte menuseltype = '0'; //code43
        public static byte menuselval = '0'; //code43
        public static byte menuinputtype = '0'; //code46
        public static String menuinputdisplay = ""; //code46
        public static byte menuinputsendhow = '0'; //code47
        public static String menuinputsendval = "0"; //code47
        public static int menuinputsendlen = 0; //code47
    }

    public static class PaymentType
    {
        public static final String BYCARD = "01";
        public static final String BYCASHRECEIPT_G = "02";
        public static final String BYCASHRECEIPT_C = "03";
        public static final String BYCASHRECEIPTCARD = "04";
        public static final String BYCASH = "05";
        public static final String BYETC = "06";
    }

    ///////////주행 택시요금전송
    public static class AMFare {
        public static boolean mbFareupdated = false;
        public static int mCurtaxifare = 0; //주행금액.
        public static int mCurtaxifareDis = 0; //할인금액.
        public static int mCurdistanceR = 0; //남은거리.

    }

    ///////////결제요금전송 21
    public static class AMCardFare {
        public static boolean mbCard = true;
        public static String msOpercode = "00000055"; //운행정보.8자리
        public static String  mstype = ""; //결제방법.
        public static int mFare = 0; //요금.
        public static int mFareDis = 0; //할인금액.
        public static int mCallCharge = 0; //호출요금.
        public static int mAddCharge = 0; //추가요금.
        public static int mMoveDistance = 0; //승차거리
        public static String mStarttime = ""; //승차시간.
        public static String mEndtime = ""; //하차시간.
        public static String mCardcode = ""; //승인번호.
        public static String mCardtime = ""; //결제시간.
        public static String mCashReceiptNum = "010111112222";
    }

    ///////////결제요청결과 23
    public static class AMCardResult {
        public static boolean mbCard = true;
        public static String msOpercode = ""; //운행정보.8자리
//                01: 카드결제 완료
//                02: 현금영수결제 완료 – 개인
//                03: 현금영수결제 완료 – 사업자
//                04: 현금영수결제 완료 – 카드
//                05: 일반현금결제 완료
//                06: 기타결제 완료 – 온라인 결제 등 외부의 결제 기능
        public static String msType = ""; //결제방법.
        public static String mCardtime = ""; //결제시간.
        public static String mResult = ""; //거래구분.
        public static String mCardcode = ""; //승인번호.
        public static String mPurchase = ""; //매입기관.
        public static String mCardno = ""; //카드번호.
        public static String mTraceno = ""; //전문추적번호.
        public static String mMDTno = ""; //단말기번호.
        public static String mMID = ""; //MID?.
        public static int mFare = 0; //요금.
        public static int mFareDis = 0; //할인금액.
        public static int mCallCharge = 0; //호출요금.
        public static int mAddCharge = 0; //추가요금.
        public static int mMoveDistance = 0; //승차거리
        public static String mStarttime = ""; //승차시간.
        public static String mEndtime = ""; //하차시간.
    }

    ///////////결제취소요청 결과 24
    public static class AMCardCancel {
        public static boolean mbCard = true;
        public static String msOpercode = ""; //운행정보.8자리
        public static String msType = ""; //결제취소방법.
        public static int mFare = 0; //요금.
        public static String mCanceltime = ""; //취소시간.
        public static String mResult = ""; //취소결과.
        public static String mCardcode = ""; //승인번호.
        public static String mTraceno = ""; //전문추적번호.

    }

    ////////////////////////meter모드 택시상태및요금수신 19
    public static class AMReceiveFare {
        public static String mreceivetime = ""; //미터수신시간.
        public static int mstate = 2; //1 지불, 2 빈차, 3 주행, 4 할증
        public static int mFare = 0; //요금.
        public static int mFarespare = 0; //요금spare.
        public static int mCallcharge = 0;
        public static int mEtccharge = 0;
        public static int msType = 0; //할증상태.
        public static String mStarttime = ""; //승차시간.
        public static double mgpsstartx;
        public static double mgpsstarty;
        public static String mEndtime = ""; //하차시간.
        public static double mgpsendx;
        public static double mgpsendy;
        public static int mBoarddist = 0;
        public static int mEmptydist = 0;

    }
///////////////////////

    synchronized public static void setSStateupdate(boolean bupdate)
    {

        mbSStateupdated = bupdate;

    }

    synchronized public static void setRStateupdate(boolean bupdate)
    {

        mbRStateupdated = bupdate;

    }

    synchronized public static void setMenuupdate(boolean bupdate)
    {

        AMmenu.mbMenuupdated = bupdate;

    }

    synchronized public static void setFareupdate(boolean bupdate)
    {

        AMFare.mbFareupdated = bupdate;

    }

    public  void setGpsclear()
    {
        if(false)
        {
            katchlat = 347360;
            katchlon = 413596;
            wgslat = 127;
            wgslon = 36;
            speed = 30;
        }else{
            katchlat = 0;
            katchlon = 0;
            wgslat = 0;
            wgslon = 0;
            speed = 0;
        }

    }

    //20160819 tra..sh
    public void  setSendedLocation() {
        if (wgslat == 0)
        {
            Sendedwgslat = beforewgslat;
            Sendedwgslon = beforekatchlon;

        }
        else
        {
            Sendedwgslat = wgslat;
            Sendedwgslon = wgslon;

        }

        lastpositiontime = System.currentTimeMillis() / 1000;
    }

    //20161106 tra..sh
    public void set_speed_interval()
    {
        speedhistory[historyIDX] = speed;
        intervalhistory[historyIDX++] = 0;
        historyIDX = historyIDX % 10;
    }

    //20161106 tra..sh
    public boolean check_speedhistory(int min)
    {
        int istart = historyIDX;
        int ncount = 0;
        for(int i = 0; i < 10; i++)
        {
            if(speedhistory[(istart + i) % 10] > 10)
                ncount++;

        }
        if(ncount > min) {

            return true;

        }
        else
            return false;
    }

    public static String getCurDateString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

}

