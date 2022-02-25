package com.konai.appmeter.driver.struct;

public class CalQueue {
    public long icurtime;
    public long ilasttime;
    public double distance;
    public double speed;
    public double altitude;
    public long itimet; //경과시간.
    public int nType; //1 gps 2 dtg
    public double nowlong; //20210701 x position
    public double nowlat; //20210710 y position
    public double lastlong; //20210701 x 이전position
    public double lastlat; //20210701 y 이전position
    public boolean bused; //20210701 carculate_fare() 반영됐는지 아닌지
}

