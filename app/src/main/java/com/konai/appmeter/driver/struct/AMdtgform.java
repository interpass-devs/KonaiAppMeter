package com.konai.appmeter.driver.struct;

public class AMdtgform {

    public long igpstime;
    public String sgpsdate;
    public double distance;
    public double speed;
    public int rpm;
    public int breakstate;
    public int gpsstate;
    public double gpsx;
    public double gpsy;
    public boolean bvalid;

/*
		이동거리
				속도
		RPM
		브레이크 상태
		GPS 수신 상태
		GPS 위도 좌표
		GPS 경도 좌표
		GPS 방위각
		GSP 속도
		조향각
				RFU
*/

    public void init()
    {
        igpstime = 0;
        sgpsdate = "";
        distance = 0;
        speed = 0;
        rpm = 0;
        breakstate = 0;
        gpsstate = 0;
        gpsx = 0;
        gpsy = 0;
        bvalid = false;
    }

}

