package com.konai.appmeter.driver.socket;

import android.util.Log;

import com.konai.appmeter.driver.setting.setting;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientUtil {

    /**
     * Shared Instance
     */
    private static UDPClientUtil __sharedUDPClient = null;

    /**
     * UDPClientUtil Get Shared Instance
     * @return
     */
    public static UDPClientUtil getInstance() {
        if ( __sharedUDPClient == null ) {
            __sharedUDPClient = new UDPClientUtil();
        }
        return __sharedUDPClient;
    }

    /**
     * UDP 연결 시작
     */
    private boolean mIsStart = false;

    /**
     * UDP Connect / Receiver Thread
     */
    private UDPConnector mUdpConnectorThread = null;

    /**
     * UDP Socket
     */
    DatagramSocket mUDPSocket = null;

    private int mUdpPort;

    private InetAddress mserverAddr;

    /**
     * Constructor
     */
    public UDPClientUtil() {

        super();

    }

    /**
     * UDP 연결
     * @param port
     */
    public void connectUdpAddressAndPort(String serverIP, int port) {
        if ( mIsStart == false ) {
            mIsStart = true;
            mUdpConnectorThread = new UDPConnector(port, serverIP);
            Thread connector = new Thread(mUdpConnectorThread);
            connector.start();

        }
    }

    /**
     * 메세지 보내기
     * @param msg
     */
    public void sendMessage(String msg) {
        if ( msg != null ) {
            UDPSendPacket sendPacket = new UDPSendPacket(mUDPSocket, msg.getBytes());
            sendPacket.run();
        }
    }

    public void sendphonenum() {
        byte[] command = new byte[22];

        command[0] = 0x16;
        command[1] = 0x00;
        command[2] = 0x70;
        command[3] = 0x61;
        command[4] = 0x6e;
        command[5] = 0x74;
        command[6] = 0x65;
        command[7] = 0x63;
        command[8] = 0x68;
        command[9] = 0x00;
        command[10] = 0x30;
        command[11] = 0x30;
        command[12] = 0x32;
        command[13] = 0x30;
        command[14] = 0x31;
        command[15] = 0x34;
        command[16] = 0x01;
        command[17] = 0x00;
        command[18] = 0x00;
        command[19] = 0x00;
        command[20] = 0x40;
        command[21] = 0x17;

        UDPSendPacket sendPacket = new UDPSendPacket(mUDPSocket, command);
        sendPacket.run();
    }

    private void getPhonenum(byte[] pdata)
    {
        String phone;
        int len = Getshortint(pdata, 18);
        phone = GetString(pdata, 20, len-1);
        Log.d("UDP", len + " ip " + phone);

        if(len > 3) {
            String tmp = phone.substring(0, 3);
            if (tmp.equals("+82")) {
                phone = "0" + phone.substring(3);
            } else {
                tmp = phone.substring(0, 2);
                if (tmp.equals("82")) {
                    phone = "0" + phone.substring(2);
                }
            }
        }

        setting.phoneNumber = phone;
    }
    /**
     * Stop UDP
     */
    public void stopUdp() {
        if ( mUdpConnectorThread != null ) {
            mUdpConnectorThread.udpStop();
        }
    }

    /**
     * UDP Connect And Receive Packet Thread
     */
    private class UDPConnector extends Thread {
        /**
         * UDP 연결 포트
         */
        /**
         * UDP Thread Stop Flag
         */
        private boolean mThreadStop;

        /**
         * Constructor
         * @param port
         */
        public UDPConnector(int port, String serverIP) {
            mUdpPort = port;
            mThreadStop = false;
            try{

                mserverAddr = InetAddress.getByName(serverIP);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        /**
         * UDP Thread Stop
         */
        public void udpStop() {
            mThreadStop = true;
        }

        @Override
        public void run() {

            int cmd = 0;
//            while (mThreadStop == false)
            {
                try {
                    // UDP 소켓 Open / Listen Timeout 5초 설정
                    if ( mUDPSocket == null ) {

                        mUDPSocket = new DatagramSocket();

//                        mUDPSocket = new DatagramSocket(mUdpPort, mserverAddr);
                        mUDPSocket.setSoTimeout(5000);
                    }


                    // 메세지 버퍼 1024 설정
                    byte[] receiveBuf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(receiveBuf,receiveBuf.length);
                    mUDPSocket.receive(packet);

                    cmd = Getshortint(packet.getData(), 16);

                    if(cmd == 0x0101)
                        getPhonenum(packet.getData());

                    Log.d("UDP", "Receive " + packet.getLength());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

            Log.d("UDP", "UDPConnector exit!");
        }
    }

    /**
     * UDP Send Packet Thread
     */
    private class UDPSendPacket extends Thread {
        /**
         * UDP Socket
         */
        private final DatagramSocket mSocket;

        /**
         * Send Byte
         */
        private final byte[] mSendByte;

        /**
         * Constructor
         * @param socket
         * @param bytes
         */
        public UDPSendPacket(DatagramSocket socket, byte[] bytes) {
            mSocket = socket;
            mSendByte = bytes;
        }

        @Override
        public void run() {

            if ( mSocket != null ) {

                try {
                    // 패킷 전송
                    DatagramPacket sendPacket = new DatagramPacket(mSendByte, mSendByte.length, mserverAddr, mUdpPort);
                    mUDPSocket.send(sendPacket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String GetString(byte[] v, int start, int done) {
        int cut = 0;
        int tmp = start;

        byte tmpbyte[] = new byte[done];

        for (; start < done + tmp; start++) {
            tmpbyte[start - tmp] = v[start];
            // util.log(tmpbyte[start-tmp]);
        }
        String str = null;
        try {
            str = new String(tmpbyte, "KSC5601");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    public int Getint(byte[] v, int start) {
        int rtn = 0;
        rtn |= (v[start] & (int) 0xFF);
        rtn |= (v[start + 1] & (int) 0xFF) << 8;
        rtn |= (v[start + 2] & (int) 0xFF) << 16;
        rtn |= (v[start + 3] & (int) 0xFF) << 24;
        return rtn;
    }

    public int Getbyte_int(byte v) {

        int rtn = 0;
        rtn |= (v & (int) 0xFF);

        return rtn;
    }
    public int Getshortint(byte[] v, int start) {
        int rtn = 0;
        rtn |= (v[start] & (int) 0xFF);
        rtn |= (v[start + 1] & (int) 0xFF) << 8;
        return rtn;
    }

}
