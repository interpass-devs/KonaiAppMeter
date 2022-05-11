//path : /com.enpsystem.gps_sample/struct/TIMSQueue.java

package com.konai.appmeter.driver.struct;

import org.json.JSONObject;

public class TIMSQueue {

    public String mURLs;
    public JSONObject mJson;
    public String mData; //20220425
    public int mSendType; // 1: POST, 2: GET
    public int mSubType; //20210419 1 운행정보, 2 버튼정보, 3 POWER
    public boolean mResend; //재전송인지
    public String mdrvtime; //table primarykey
    public long mtrytime; //20210419
    public int mtrycount;

}
