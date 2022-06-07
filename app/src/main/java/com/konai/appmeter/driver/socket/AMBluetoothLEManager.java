package com.konai.appmeter.driver.socket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.konai.appmeter.driver.serialport.SerialPort_Device;
import com.konai.appmeter.driver.service.LocService;
import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.setting.setting;
import com.konai.appmeter.driver.struct.AMBlestruct;
import com.konai.appmeter.driver.struct.AMdtgform;
import com.konai.appmeter.driver.struct.CalFareBase;

import org.apache.http.util.ByteArrayBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class AMBluetoothLEManager {

    private final static String TAG = AMBluetoothLEManager.class.getSimpleName();
    private Context mContext;
    public static LocService m_Service = null;

    ////////////////////////////////////
//bluetooth 20200711
    private boolean startFlags = false;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt = null; //20220407
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothDevice m_BTdevice = null; //20220407
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public static BluetoothGattService m_gattService = null;
    private static BluetoothGattCharacteristic m_gattCharTrans = null;
    private static BluetoothGattCharacteristic m_gattCharConfig = null;
    private static BluetoothGattDescriptor m_descriptor = null;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    ////////////////////////////////////
//20210311
    private static SerialPort_Device serialPort_AM = null;
/////////////

    private AMPacket topkt = null;
    private AMPacket outpkt = null;
    private static byte[] packetdata;
    public byte[] outpacket = null;

    int m_nQueuedSize = 0;
    ByteArrayBuffer outbuffer = null;

    BlockingQueue<AMdtgform> mDTGqueue;

    boolean bExit = false;

    com.konai.appmeter.driver.setting.setting setting = new setting();

    AMdtgform dtgform = new AMdtgform();

    public AMBluetoothLEManager(Context context, BlockingQueue queue, LocService service) {
        // TODO Auto-generated constructor stub

        mContext = context;
        mDTGqueue = queue;
        m_Service = service;

        if (topkt == null) {

            topkt = new AMPacket();

        }

        if (outpkt == null) {

            outpkt = new AMPacket();

        }

        if (packetdata == null)
            packetdata = new byte[2048];

        if (outpacket == null)
            outpacket = new byte[2048];

        if(outbuffer == null)
            outbuffer = new ByteArrayBuffer(2048);

        new Thread(new faresendThread())
                .start();

    }

    //20201215
    public void close() {

        if(setting.gUseBLE) {

            disconnectBLE();

        }
        else {
            if (serialPort_AM != null)
                serialPort_AM.serialexit();

            AMBlestruct.mBTConnected = false;
        }

        bExit = true;
    }

    public boolean connectAM()
    {
        if(setting.gUseBLE)
        {
            Log.d(TAG, "bluetooth!! connectBLE() BLE!!!");
            return connectBLE();
        }
        else {
            if (setting.gSerialUnit==1){  //me: 아이나비
                serialPort_AM = new SerialPort_Device("/dev/ttyXRM1", 115200, 11000);  //(115200-통신속도, 속도 구분자-11000)
            }else if (setting.gSerialUnit==2){  //me: 아트뷰
//20220329                serialPort_AM = new SerialPort_Device("/dev/ttySCA0", 115200, 11000);
                serialPort_AM = new SerialPort_Device("/dev/ttyMT2", 115200, 11000);
            }else if (setting.gSerialUnit==3){  //me: 아틀란
                serialPort_AM = new SerialPort_Device("/dev/ttymxc2", 115200, 11000);
//20211220
                if(serialPort_AM.out == null) {
                    serialPort_AM = null;
                    Log.d(TAG, "openfail serial");
                    serialPort_AM = new SerialPort_Device("/dev/ttySAC5", 115200, 11000);
                }

            }else {
                return false;
            }

//20211220            serialPort_AM.start("", 11000, null);
//            serialPort_AM.setManager(this);
//
//            AMBlestruct.mBTConnected = true;
//
//            Log.d(TAG, "opened serial");

//20211220
            if(serialPort_AM.out != null) {
                serialPort_AM.start("", 11000, null);
                serialPort_AM.setManager(this);

                AMBlestruct.mBTConnected = true;
                Log.d(TAG, "opened serial");
            }
            else
                Log.d(TAG, "openfail serial");

            return true;
        }
    }

    public boolean disconnectAM()
    {
        if(setting.gUseBLE) {

            disconnectBLE();

            Log.d(TAG, "bluetooth!! disconnectBLE() BLE!!!");
        }

        m_Service.int_aboutDTG(); //20220407

        return true;

    }

    //20201215
    public void disconnectBLE()
    {

        if (mBluetoothGatt != null) {

            mBluetoothGatt.close();
            mBluetoothGatt = null;

        }

       AMBlestruct.mBTConnected = false;

    }

    class faresendThread implements Runnable { //20201112

        public void run() {

            while(!bExit)
            {
                if(AMBlestruct.AMmenu.mMenu == false)
                {
                    if(AMBlestruct.mbSStateupdated)
                    {

                        makepacketsend("15");
                        AMBlestruct.setSStateupdate(false);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    }

                    if(AMBlestruct.AMFare.mbFareupdated)
                    {

                        makepacketsend("31");
                        AMBlestruct.setFareupdate(false);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    }

                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

        }
    }

    //20210831 tra..sh
    class get1sDataThread implements Runnable { //20201112{
        public void run() {

            int ncount11 = 0;
            int ncount14 = 1;

            while (true) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                if (AMBlestruct.mb1sdata13code == true && AMBlestruct.mb1sdata12code == true) {

                    m_Service.set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.BLE1SDATAOK);
                    break;
                }
                else
                    makepacketsend("10"); //20210831 tra..sh

                if(false) {
                    if (AMBlestruct.mb1sdata13code == false) {
                        if (ncount11 % 10 == 0) {

                            makepacketsend("11");

                        }

                        ncount11++;
                    } else if (AMBlestruct.mb1sdata12code == false) //20210520
                    {

                        if (ncount14 % 5 == 0) {

                            makepacketsend("14");

                        }

                        ncount14++;
                    }
                }

                if (ncount11 > 240 || ncount14 > 120) {
//                    disconnectBLE(); //20201215
                    disconnectAM();
                    m_Service.set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.BLE1SDATAERROR);
                    break;

                }

            }
        }
    }

    //////////////////20201215
    public void _init_Am100DTGdata()
    {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        AMBlestruct.mb1sdata13code = false;
        AMBlestruct.mb1sdata12code = false;

        Log.e("ble Drive State", m_Service.mbDrivestart + "");
        if(m_Service.mbDrivestart) {
            m_Service.set_drivestate(true);
        } else {
            if(Info.mAM100FirstOK)
                m_Service.drive_state(AMBlestruct.MeterState.EMPTYBYEMPTY); //for send event timsdtg no
            else
                m_Service.drive_state(AMBlestruct.MeterState.EMPTY); //for send event timsdtg on

        }

        try {
            Thread.sleep(200);
        } catch (Exception e) {

        }
        /*if(!startFlags) {

        }*/

//        makepacketsend("11");
        makepacketsend("10"); //20210402


        new Thread(new get1sDataThread())
                .start();
    }

    public boolean _is_gattCharTrans()
    {
        if(m_gattCharTrans != null)
            return true;

        return false;

    }
    ////////////////

    public boolean connectBLE() {

        m_gattCharTrans = null; //20201110

        if (mBluetoothManager == null) //20220407 ???
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothManager == null) {
            //Log.e(TAG, "Unable to initialize BluetoothManager.");
            return false;
        }
///        }

        if(mBluetoothAdapter == null) //20220407 ???
            mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            //Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (mBluetoothAdapter == null || setting.BLUETOOTH_DEVICE_ADDRESS.equals("")) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

//20210429
/*
        // Previously connected device.  Try to reconnect. (재연결)_
        if (mBluetoothDeviceAddress != null && setting.BLUETOOTH_DEVICE_ADDRESS.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
*/

        if(m_BTdevice == null) //20220407 ???
            m_BTdevice = mBluetoothAdapter.getRemoteDevice(setting.BLUETOOTH_DEVICE_ADDRESS);

        if (m_BTdevice == null) {
            Log.w(TAG, "Device found not.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
//20220407
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = m_BTdevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }
        else
            mBluetoothGatt = m_BTdevice.connectGatt(mContext, false, mGattCallback);

        Log.d(TAG, " Device found Trying to create a new connection.");
        mBluetoothDeviceAddress = setting.BLUETOOTH_DEVICE_ADDRESS;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                AMBlestruct.mBTConnected = true;
                Info.mBTFirstOK = true; //20210329
                m_Service.set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.BLELEDON);
//				broadcastUpdate(intentAction);
                Log.i(TAG, "bluetooth!! Connected to GATT server. " + AMBlestruct.mBTConnected);
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;

                Log.i(TAG, "bluetooth!! Disconnected from GATT server. " + AMBlestruct.mBTConnected);

                if(AMBlestruct.mBTConnected == true)
                    m_Service.set_meterhandler.sendEmptyMessage(AMBlestruct.MeterState.BLELEDOFF);

                AMBlestruct.mBTConnected = false;

//				broadcastUpdate(intentAction);
                m_Service.int_aboutDTG();
                disconnectAM(); //20210607

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { //
            if (status == BluetoothGatt.GATT_SUCCESS) {
//				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                findGattServices(mBluetoothGatt.getServices());


                mBluetoothGatt.requestMtu(512); // 사이즈

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                {

                    Log.e(TAG, "---------BluetoothGatt onmutechange: " + mtu);

                    initGattCharaceristic();

                }
            }
        }

        //데이터 요청시 들어오는곳
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                AMBleReciveData(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        //기존 데이터 들어오는곳
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            AMBleReciveData(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void findGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();

            if(uuid.equals(setting.UUID_SERVICE.toString())) {

                m_gattService = gattService;

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();

                    if(uuid.equals(setting.UUID_TRANJACTION.toString())) {
                        m_gattCharTrans = gattCharacteristic;
                        for (BluetoothGattDescriptor descriptor : gattCharacteristic.getDescriptors()) {
                            if(descriptor.getUuid().toString().equals(setting.UUID_DESCRIPTION.toString()))
                            {
                                m_descriptor = descriptor;
                                Log.e(TAG, "---------BluetoothGattDescriptor: " + descriptor.getUuid().toString());
                            }
                        }
                    }
                    else if(uuid.equals(setting.UUID_CONFIGURE.toString()))
                        m_gattCharConfig = gattCharacteristic;

                }

                break; //nomore find.
            }
        }

    }

    private void initGattCharaceristic() {

        if (m_gattCharTrans != null) {

            final int charaProp = m_gattCharTrans.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.

//							mBluetoothGatt.setCharacteristicNotification(m_gattCharTrans, false);

//							mBluetoothGatt.readCharacteristic(m_gattCharTrans);
            }

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                mBluetoothGatt.setCharacteristicNotification(m_gattCharTrans, true);

            }

            if(m_descriptor != null) {
                // This is specific to Heart Rate Measurement.
                m_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(m_descriptor);
            }

        }

        if(m_gattCharConfig != null)
        {
            //todo.
        }

    };

//////////////////////////

    public void update_AMmeterfare(int nfare, int nfaredis, int ndistanceremain)
    {

        AMBlestruct.AMFare.mCurtaxifare = nfare;
        AMBlestruct.AMFare.mCurtaxifareDis = nfaredis;
        AMBlestruct.AMFare.mCurdistanceR = ndistanceremain;

        AMBlestruct.setFareupdate(true);
    }

    public void update_AMmeterstate(String sstate)
    {

        AMBlestruct.mSState = sstate;
        AMBlestruct.setSStateupdate(true);
    }

    public void send_Paymenttype(String sopercode, String stype)
    {

        AMBlestruct.AMCardFare.msOpercode = sopercode;
        AMBlestruct.AMCardFare.mstype = stype;
    }

    public void send_CashPayment(String sopercode, String stype, String receiptNum)
    {

        AMBlestruct.AMCardFare.msOpercode = sopercode;
        AMBlestruct.AMCardFare.mstype = stype;
        AMBlestruct.AMCardFare.mCashReceiptNum = receiptNum;
    }

    public boolean write(byte[] data) {

        data[0] = 0x02;

        data[data.length - 1] = 0x03;

        if(setting.gUseBLE) {
            if (mBluetoothGatt == null || m_gattCharTrans == null) {
//                Log.d(TAG, "# BluetoothGatt not initialized");
                return false;

            }

            m_gattCharTrans.setValue(data);
            m_gattCharTrans.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(m_gattCharTrans);

        }
        else
        {

            serialPort_AM.sendData(data);

        }

////////////////////

        if(Info.REPORTREADY) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            Log.d(TAG, "++++++++send(" + data.length + ")" + stringBuilder.toString());
        }

        return true;
    }

    private void AMBleReciveData(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        {
            // For all other profiles, writes the data formatted in HEX.
            byte[] outdata = characteristic.getValue();
            if (outdata != null && outdata.length > 0) {

                if(false) //Info.TESTMODE)
                {
                    final StringBuilder stringBuilder = new StringBuilder(outdata.length);
                    for (byte byteChar : outdata)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    Log.d(TAG, "========receive(" + outdata.length + ")" + stringBuilder.toString());

                }

                m_nQueuedSize = m_nQueuedSize + outdata.length;
                if( m_nQueuedSize >= 2048) //20190507
                {

                    outbuffer.clear();
                    m_nQueuedSize = 0;

                }

                outbuffer.append(outdata, 0, outdata.length);

//                Log.d(TAG, "am100수신 " + outbuffer.length());
                checkgetData_AMBle(outbuffer.toByteArray(), outbuffer.length());

                Info.mBTOnOffCheck = 0; //20210329
                Info.bBTRestarting = false;

            }

            outdata = null;

        }

    }

    public void checkgetData_AMBle(byte[] bytetmp, int bytesRead) {

        byte[] outPack;
        byte[] outdata;

        int stCnt = 0;
        int enCnt = 0;

        int reStCnt = 0;

        boolean stIdx = false;
        boolean edIdx = false;

        for(int i=0; i<bytesRead; i++)
        {
            if(bytetmp[i] == (byte)0x02)
            {
                if(!stIdx) {
                    stIdx = true;
                    stCnt = i;
                }
            }
            else if(bytetmp[i] == (byte)0x03)
            {
                if(i>0)
                {
                    edIdx = true;
                    enCnt = i;
                    reStCnt = i+1;
                }
            }

            if(stIdx && edIdx)
            {
                if(enCnt - stCnt + 1 > 0) //20220307 tra..sh
                {
                    outdata = new byte[enCnt - stCnt + 1];
                    System.arraycopy(bytetmp, stCnt, outdata, 0, enCnt - stCnt + 1);
                    parsingend_AMBle(outdata, outdata.length);
//                Log.d(TAG, "dtgform.distance parsingend_AMBle end");
                    stIdx = false;
                    edIdx = false;
                    outdata = null;
                }
            }
        }

        if(bytesRead - reStCnt > 0)
        {
            outPack = new byte[bytesRead - reStCnt];
            System.arraycopy(bytetmp, reStCnt, outPack, 0, bytesRead - reStCnt);
            outbuffer.clear();
            outbuffer.append(outPack, 0, outPack.length);
            outPack = null;

        }
        else
            outbuffer.clear();

    }

    ////////////////////////////// 결과값
    synchronized public void parsingend_AMBle(byte[] outdata, int packetlen) {
        try {

            if(packetlen == 0)
                return;

            if(Info.REPORTREADY) //Info.TESTMODE)
            {
                final StringBuilder stringBuilder = new StringBuilder(packetlen);
                for (byte byteChar : outdata)
                    stringBuilder.append(String.format("%02X ", byteChar));
                Log.d(TAG, "========receive(" + packetlen + ")" + stringBuilder.toString());

            }

//            Log.d(TAG, "========receive " + packetlen);

            String code = outpkt.GetCheckCode(outdata);

////////////////////////////////

            if(outpkt.GetAMBleCRC(outdata, packetlen).equals(String.format("%c%c", outdata[packetlen -3], outdata[packetlen - 2])) == false)
            {
                Log.d(TAG, "========receive(" + code + ")" + outpkt.GetAMBleCRC(outdata, packetlen)
                        + "  " + String.format("-------%c%c", outdata[packetlen -3], outdata[packetlen - 2]));
//                return;

            }
////////////////////////////

            outpkt.SetPoint(3);

            if(code.equals("12")) //인증코드.
            {

                outpkt.SetPoint(3);
                AMBlestruct.mParsingtime = outpkt.GetString(outdata, 14);

                AMBlestruct.mb1sdata12code = true; //20201215
                acksend("62", AMBlestruct.mParsingtime, AMBlestruct.mParsingResult);

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY, "빈차등AM100인증요청2단계 ACK ", "");

                }
            }
            else if(code.equals("13")) //차량인증서.
            {
                outpkt.SetPoint(3);
                AMBlestruct.mParsingtime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMLicense.licensetaxi = outpkt.GetString(outdata, 12);
                AMBlestruct.AMLicense.shacodetaxi = outpkt.GetString(outdata, 64);
                Log.d(TAG, "-13-" + AMBlestruct.AMLicense.licensetaxi);

                acksend("63", AMBlestruct.mParsingtime, AMBlestruct.mParsingResult);

                m_Service.set_meterhandler.sendEmptyMessage(13); //20201110

                AMBlestruct.mb1sdata13code = true; //20201215

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY, "빈차등AM100인증요청1단계 ACK ", "");

                }

            }
            else if (code.equals("17")) {//앱미터상태전송요청

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY, "빈차등시작 알림17. ", "");

                }
                makepacketsend("67"); //20210823

            }
            else if (code.equals("18")) {//빈차등수신.

                outpkt.SetPoint(17);

                AMBlestruct.mRState = outpkt.GetString(outdata, 2);
                AMBlestruct.setRStateupdate(true);
                m_Service.set_meterhandler.sendEmptyMessage(2); //20220407 tra..sh

            }
            else if (code.equals("19")) {//택시요금수신, 미터기모드

                outpkt.SetPoint(17);
                AMBlestruct.AMReceiveFare.mreceivetime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMReceiveFare.mstate = outpkt.Getint(outdata, 1);
                AMBlestruct.AMReceiveFare.mFare = outpkt.Getint(outdata, 6);
                AMBlestruct.AMReceiveFare.mFarespare = outpkt.Getint(outdata, 6);
                AMBlestruct.AMReceiveFare.mCallcharge = outpkt.Getint(outdata, 4);
                AMBlestruct.AMReceiveFare.mEtccharge = outpkt.Getint(outdata, 6);
                AMBlestruct.AMReceiveFare.msType = outpkt.Getint(outdata, 1);
                AMBlestruct.AMReceiveFare. mStarttime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMReceiveFare. mgpsstartx = outpkt.Getint(outdata, 9) / 1000000;
                AMBlestruct.AMReceiveFare. mgpsstarty = outpkt.Getint(outdata, 9) / 1000000;
                AMBlestruct.AMReceiveFare. mEndtime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMReceiveFare. mgpsendx = outpkt.Getint(outdata, 9) / 1000000;
                AMBlestruct.AMReceiveFare. mgpsendy = outpkt.Getint(outdata, 9) / 1000000;
                AMBlestruct.AMReceiveFare.mBoarddist = outpkt.Getint(outdata, 6);
                AMBlestruct.AMReceiveFare.mEmptydist = outpkt.Getint(outdata, 6);

                m_Service.set_meterhandler.sendEmptyMessage(12);

                Log.d(TAG, "-19-" + AMBlestruct.AMReceiveFare.mFare);

            }
            else if(code.equals("22")) //결제결과.
            {
                outpkt.SetPoint(3);
                AMBlestruct.mParsingtime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMCardResult.msOpercode = outpkt.GetString(outdata, 8);
                AMBlestruct.AMCardResult.msType = outpkt.GetString(outdata, 2);
                AMBlestruct.AMCardResult.mFare = outpkt.Getint(outdata, 6);
                AMBlestruct.AMCardResult.mCardtime = outpkt.GetString(outdata, 14);
                AMBlestruct.AMCardResult.mResult = outpkt.GetString(outdata, 2);


                AMBlestruct.AMCardResult.mCardcode = outpkt.GetString(outdata, 8); //승인번호.
                AMBlestruct.AMCardResult.mPurchase = outpkt.GetString(outdata, 4); //매입기관.
                AMBlestruct.AMCardResult.mCardno = outpkt.GetString(outdata, 12); //카드번호.
                AMBlestruct.AMCardResult.mTraceno = outpkt.GetString(outdata, 4); //전문추적번호.
                AMBlestruct.AMCardResult.mMDTno = outpkt.GetString(outdata, 10); //단말기번호.
                AMBlestruct.AMCardResult.mMID = outpkt.GetString(outdata, 10); //MID?.

//                m_Service.set_meterhandler.sendEmptyMessage(3);

                acksend("72", AMBlestruct.mParsingtime, AMBlestruct.mParsingResult);

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY,"빈차등결제성공 수신 ", "");

                }

//                Log.d("msType", AMBlestruct.AMCardResult.mFare+", " + AMBlestruct.AMCardFare.mMoveDistance+"");

                m_Service.set_meterhandler.sendEmptyMessage(3);

                //todo: 거리가 0으로 서버에 전송됨.
                //todo: end

            }
            else if(code.equals("24")) //결제취소결과
            {
                outpkt.SetPoint(17);
                AMBlestruct.AMCardCancel.msOpercode = outpkt.GetString(outdata, 8);
                AMBlestruct.AMCardCancel.msType = outpkt.GetString(outdata, 2); //결제취소방법.
                AMBlestruct.AMCardCancel.mFare = outpkt.Getint(outdata, 6); //요금.
                AMBlestruct.AMCardCancel.mCanceltime = outpkt.GetString(outdata, 14); //취소시간.
                AMBlestruct.AMCardCancel.mResult = outpkt.GetString(outdata, 2); //취소결과.
                AMBlestruct.AMCardCancel.mCardcode = outpkt.GetString(outdata, 8); //승인번호.
//                AMBlestruct.AMCardCancel.mTraceno = outpkt.GetString(outdata, 4); //전문추적번호.

                m_Service.set_meterhandler.sendEmptyMessage(5);

            }
            else if(code.equals("30")) //DTG (1초데이터)
            {
//                SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMddHHmmss");

//                    outdata[50] = 'A';
//                    outdata[52] = 'A';
//                    outdata[53] = 'A';
//                    outdata[55] = 'A';
//                    outdata[58] = 'A';

                    dtgform.sgpsdate = outpkt.GetString(outdata, 14);

//                try {
                    // 현재 yyyymmdd로된 날짜 형식으로 java.util.Date객체를 만든다.
//                    dtgform.igpstime = transFormat.parse(dtgform.sgpsdate).getTime();

//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                    dtgform.igpstime = Info.getStringTime(dtgform.sgpsdate); //20210520

                    dtgform.distance = outpkt.Getdouble(outdata, 11) / 100;
//                Log.d(TAG, "dtgform.distance " + dtgform.sgpsdate + " " + dtgform.distance);
                    dtgform.speed = outpkt.Getdouble(outdata, 5) / 100.0 / 3.6;
                    dtgform.rpm = outpkt.Getint(outdata, 4);
                    dtgform.breakstate = outpkt.Getint(outdata, 1);
                    dtgform.gpsstate = outpkt.Getint(outdata, 1);
                    dtgform.gpsy = outpkt.Getint(outdata, 9) / 1000000.0;
                    dtgform.gpsx = outpkt.Getint(outdata, 9) / 1000000.0;
                    dtgform.bvalid = true;

//                Log.d("am100 gps", "(" + dtgform.gpsstate + ")" + dtgform.gpsx + " " + dtgform.gpsy);

//                if(mDTGqueue.remainingCapacity() > 0)
///                    mDTGqueue.add(dtgform);

                    m_Service.get_dtg(dtgform);

                    if (Info.REPORTREADY) {

                        Info._displayLOG(Info.LOGDISPLAY, "빈차등1초데이터 수신 d " + dtgform.distance + ", s " + dtgform.speed, "");

                    }

//                Log.d("1초데이터", " 수신 d " + dtgform.distance + ", s " + dtgform.speed);

            }
            else if(code.equals("42"))
            {

                outpkt.SetPoint(17);

                if(AMBlestruct.AMmenu.mMenu) {
                    AMBlestruct.AMmenu.menutype = outpkt.Getbyte(outdata);
                    AMBlestruct.AMmenu.menudisplay = outpkt.Gettextbytoken(outdata, (byte) 0x03, packetlen, -2);
                    AMBlestruct.setMenuupdate(true);
                }
                else //card payment
                {

//20220107 TODO?                    m_Service.set_meterhandler.sendEmptyMessage(5);
                    ;
                }
                Log.d(TAG, "-42- " +  AMBlestruct.AMmenu.menutype + " " + AMBlestruct.AMmenu.menudisplay);


//				AMBlestruct.AMmenu.menuseltype = '2';
//				AMBlestruct.AMmenu.menuselval = '2';
//				makepacketsend("43");

            }

            else if(code.equals("46"))
            {
                outpkt.SetPoint(17);

                if(AMBlestruct.AMmenu.menutype == '2') //이전코드가 '42', 정보출력일때 '42'문구를 표기함
                {

                    AMBlestruct.AMmenu.menutype = '7';

                }
                else
                    AMBlestruct.AMmenu.menutype = '9';

                AMBlestruct.AMmenu.menuinputtype = outpkt.Getbyte(outdata);
                AMBlestruct.AMmenu.menuinputdisplay = outpkt.Gettextbytoken(outdata, (byte) 0x03, packetlen, -2);
                AMBlestruct.setMenuupdate(true);
                Log.d(TAG, "-46-" + AMBlestruct.AMmenu.menuinputdisplay);
            }
            else if(code.equals("60")) //20210402인증 0단계 프로토콜추가
            {
//20220107 moveto 98                AMBlestruct.mb1sdata13code = true;
//                AMBlestruct.mb1sdata12code = true;
//                Log.d(TAG, "-60-" + AMBlestruct.AMmenu.menuinputdisplay);

//20220107 nomore use                makepacketsend("11"); //20210520
                makepacketsend("48"); //20220107
            }
            else if(code.equals("61"))
            {

            }
            else if(code.equals("62"))
            {

            }
            else if(code.equals("65"))
            {

            }
            else if(code.equals("66"))
            {

            }
            else if(code.equals("70"))
            {

            }
            else if(code.equals("71"))
            {

                if(Info.REPORTREADY)
                {

                    Info._displayLOG(Info.LOGDISPLAY,"빈차등결제기요금전송ACK 수신 ", "");

                }
            }
            else if(code.equals("72"))
            {

            }
            else if(code.equals("73"))
            {

            }
            else if(code.equals("74"))
            {

            }
            else if(code.equals("76"))
            {

            }
            else if(code.equals("81")) //20210407
            {

                AMBlestruct.mbSendfareOK = true;

            }
            else if(code.equals("91"))
            {
                outpkt.GetDate(outdata);
                outpkt.GetString(outdata, 2);
                AMBlestruct.AMmenu.mMenu = true;
            }
            else if(code.equals("93"))
            {

            }
            else if(code.equals("97"))
            {

            }
            else if(code.equals("98")) //202120107 add
            {

                AMBlestruct.mb1sdata13code = true;
                AMBlestruct.mb1sdata12code = true;

            }

            m_Service.setCount_BleReceiveCheck(false); //20220407

            if(Info.REPORTREADY) {
                Info._displayLOG(Info.LOGDISPLAY, "send=============receive R " + code, "");
            }

        }
        catch (Exception e) {

            Log.d(TAG,"1초데이타 코드30 수신 parsing error!");

        }

    }

    //////////////////////////////
    synchronized public boolean makepacketsend(String mCurCode) {

        byte[] mData = null;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02); //0x02);// STX

//        Log.d(TAG, mCurCode + "---send");

        if(mCurCode.equals("10")) //20201110
        {

//인증단계 10 48 (제거함 11 13 14 12)
            topkt.SetString(packetdata, "10"); //빈차등연결

            topkt.SetString(packetdata, getCurDateString());

            topkt.SetString(packetdata, "00000000"); //8자리.
            topkt.SetString(packetdata, AMBlestruct.AMLicense.licensecode);

            if(Info.REPORTREADY)
            {

                Info._displayLOG(Info.LOGDISPLAY, "빈차등AM100인증요청0단계 ", "");

            }

        }
        else if(mCurCode.equals("11")) //20201110
        {
            topkt.SetString(packetdata, "11"); //빈차등연결

            topkt.SetString(packetdata, getCurDateString());

            topkt.SetString(packetdata, "00000000"); //8자리.
            topkt.SetString(packetdata, AMBlestruct.AMLicense.licensecode);

            if(Info.REPORTREADY)
            {

                Info._displayLOG(Info.LOGDISPLAY, "빈차등AM100인증요청1단계 ", AMBlestruct.AMLicense.licensecode);

            }

        }
        else if(mCurCode.equals("14")) //20201110
        {
            String codes = "";
            String sdate ="";
            sdate = getCurDateString();
            codes = AMBlestruct.AMLicense.licensecode + AMBlestruct.AMLicense.securecode  + sdate;
//            Sha256ToBase64(codes);

            topkt.SetString(packetdata, "14"); //운전자인증서

            topkt.SetString(packetdata, sdate);

            topkt.SetString(packetdata, AMBlestruct.AMLicense.licensecode);
            topkt.SetString(packetdata, Sha256(codes));

            if(Info.REPORTREADY)
            {

                Info._displayLOG(Info.LOGDISPLAY, "빈차등AM100인증요청2단계 ", "");

            }

        }

        else if (mCurCode.equals("15")) { //빈/승차 상태전송.

            topkt.SetString(packetdata, "15"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());

            topkt.SetString(packetdata, AMBlestruct.mSState);

            Log.d("빈차등차량상태전송", AMBlestruct.mSState);

        }
        else if(mCurCode.equals("16"))
        {

        }
        else if(mCurCode.equals("20")) //결제정보요청
        {
            topkt.SetString(packetdata, "20"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.msOpercode);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mstype);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mCashReceiptNum);

        }


        else if(mCurCode.equals("21"))  //결제정보전송
        {
            topkt.SetString(packetdata, "21"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.msOpercode);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mFare, 6);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mFare, 6); //20201110 mFareDis, 6);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mCallCharge, 4);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mAddCharge, 6);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mMoveDistance, 6);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mStarttime);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mEndtime);
            topkt.SetString(packetdata, "none", 30);

            Log.d("distance_check", AMBlestruct.AMCardFare.mMoveDistance+"");

//            String payType = "";
//            if(AMBlestruct.AMCardFare.mstype.equals("01")) {
//                payType = "2";
//            } else if(AMBlestruct.AMCardFare.mstype.equals("05")) {
//                payType = "1";
//            } else if(AMBlestruct.AMCardFare.mstype.equals("06")) {
//                payType = "3";
//            }
//
//            int basePayType = 0;
//            if(AMBlestruct.AMCardFare.mFare > CalFareBase.BASECOST) {
//                basePayType++;
//            }
//

//			public static String sOpercode = ""; //운행정보.8자리
//			public static int mFare = 0; //요금.
//			public static int mFareDis = 0; //할인금액.
//			public static int mCallCharge = 0; //호출요금.
//			public static int mAddCharge = 0; //추가요금.
//			public static int mMoveDistance = 0; //승차거리
//			public static String mStarttime = ""; //승차시간.
//			public static String mEndtime = ""; //하차시간.



        }
        //todo: 20210831 1758
        /**
         else if(mCurCode.equals("777")){
         String payType = "";
         if(AMBlestruct.AMCardFare.mstype.equals("01")) {
         payType = "2";
         } else if(AMBlestruct.AMCardFare.mstype.equals("05")) {
         payType = "1";
         } else if(AMBlestruct.AMCardFare.mstype.equals("06")) {
         payType = "3";
         }

         int basePayType = 0;
         if(AMBlestruct.AMCardFare.mFare > CalFareBase.BASECOST) {
         basePayType++;
         }


         }//todo: end
         **/
        else if(mCurCode.equals("23")) //결제취소요청
        {
            topkt.SetString(packetdata, "23"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.msOpercode);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mstype);
            topkt.SetIntString(packetdata, AMBlestruct.AMCardFare.mFare, 6);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mCardtime);
            topkt.SetString(packetdata, AMBlestruct.AMCardFare.mCardcode);

            Log.d("check_mstype___", AMBlestruct.AMCardFare.mstype);

        }
        else if(mCurCode.equals("26")) //영수증요청
        {
            topkt.SetString(packetdata, "26"); //20151101 90");// CODE

            Log.e("Receipt Code = ", AMBlestruct.AMCardResult.msOpercode);

            topkt.SetString(packetdata, getCurDateString());
            topkt.SetString(packetdata, AMBlestruct.AMCardResult.msOpercode);

        }
        else if (mCurCode.equals("31")) {//주행요금전달

            topkt.SetString(packetdata, "31"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());
            topkt.SetIntString(packetdata, AMBlestruct.AMFare.mCurtaxifare, 6);
            topkt.SetIntString(packetdata, AMBlestruct.AMFare.mCurtaxifareDis, 6);
            topkt.SetIntString(packetdata, AMBlestruct.AMFare.mCurdistanceR, 4);
//20210409 change protocol            topkt.SetIntString(packetdata, 0, 10);

        }
        else if (mCurCode.equals("41")) {//메뉴설정

            topkt.SetString(packetdata, "41"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());

            topkt.Setbyte(packetdata, AMBlestruct.AMmenu.mMenuState);

        }
        else if(mCurCode.equals("43"))
        {
            topkt.SetString(packetdata, "43"); //20151101 90");// CODE
            topkt.SetString(packetdata, getCurDateString());
            topkt.Setbyte(packetdata, AMBlestruct.AMmenu.menuseltype);
            topkt.Setbyte(packetdata, AMBlestruct.AMmenu.menuselval);

        }
        else if(mCurCode.equals("47"))
        {
            topkt.SetString(packetdata, "47"); //20151101 90");// CODE
            topkt.SetString(packetdata, getCurDateString());
            topkt.Setbyte(packetdata, AMBlestruct.AMmenu.menuinputsendhow);
            topkt.SetIntString(packetdata, AMBlestruct.AMmenu.menuinputsendlen, 2);
            topkt.SetString(packetdata, AMBlestruct.AMmenu.menuinputsendval);

            Log.d(TAG, "" + AMBlestruct.AMmenu.menuinputsendval);

        }
        else if(mCurCode.equals("48"))
        {
            topkt.SetString(packetdata, "48");
            topkt.SetString(packetdata, getCurDateString());
//            topkt.SetString(packetdata, AMBlestruct.AMLicense.timslicense);

            String tmp = "#########".substring(0, 9 - Info.G_driver_num.length())
                    + Info.G_driver_num + "";
            topkt.SetString(packetdata, tmp);

            Info._displayLOG(Info.LOGDISPLAY, "빈차등운전자격번호. " + tmp, "48 ");

        }
        else if(mCurCode.equals("67")) //20210823 앱미터상태전송
        {
            topkt.SetString(packetdata, "67"); //20151101 90");// CODE

            topkt.SetString(packetdata, getCurDateString());

            topkt.SetString(packetdata, AMBlestruct.mSState);

            if(Info.REPORTREADY)
            {

                Info._displayLOG(Info.LOGDISPLAY, "빈차등시작 알림ACK. " + AMBlestruct.mSState, "");

            }

        }
        else if(mCurCode.equals("68"))
        {

        }
        else if(mCurCode.equals("80"))
        {

        }
        else if(mCurCode.equals("92"))
        {

        }
        else if(mCurCode.equals("96"))
        {

        }

        topkt.SetString(packetdata, topkt.GetAMBleCRC(packetdata));

        topkt.Setbyte(packetdata, (byte) 0x03); //0x02);// STX

        mData = new byte[topkt.point];

        System.arraycopy(packetdata, 0, mData, 0, topkt.point);

        write(mData);

        if(Info.REPORTREADY) {
            Info._displayLOG(Info.LOGDISPLAY, "send=============receive S " + mCurCode, "");
        }

        mData = null;

        return true;
    }

    ////////////////////
    private String getCurDateString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    public static String Sha256ToBase64(String data){
//        MessageDigest digester = MessageDigest.getInstance("SHA-256");
//        digester.update(data.getBytes());
//        return Base64.getEncoder().encodeToString(digester.digest());

        try{

            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(data.getBytes());
            return Base64.getEncoder().encodeToString(sh.digest());

        }catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "";

        }

    }

    public static String Sha256(String data){
//        MessageDigest digester = MessageDigest.getInstance("SHA-256");
//        digester.update(data.getBytes());
//        return Base64.getEncoder().encodeToString(digester.digest());

        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(data.getBytes());
//            return sh.digest().toString();
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length; i++) sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1).toUpperCase());
//            for(int i = 0 ; i < byteData.length ; i++)
//                sb.append(String.format("%02x", byteData[i]));
            Log.d(TAG, "-SHA-" + byteData.length + " " + sb.toString());
            return sb.toString();

        }catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "";

        }

    }

    //////////////////////////////
    synchronized public boolean acksend(String mAckCode, String sdate, String sresult) {

        byte[] mData;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02); //0x02);// STX
        {
            topkt.SetString(packetdata, mAckCode); //빈차등연결

            topkt.SetString(packetdata, sdate);
            topkt.SetString(packetdata, sresult);

        }

        topkt.SetString(packetdata, topkt.GetAMBleCRC(packetdata));

        topkt.Setbyte(packetdata, (byte) 0x03); //0x02);// STX

        mData = new byte[topkt.point];

        System.arraycopy(packetdata, 0, mData, 0, topkt.point);

        write(mData);

        return true;
    }

}
