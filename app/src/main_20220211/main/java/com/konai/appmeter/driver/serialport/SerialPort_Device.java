package com.konai.appmeter.driver.serialport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.konai.appmeter.driver.setting.Info;
import com.konai.appmeter.driver.socket.AMBluetoothLEManager;

import org.apache.http.util.ByteArrayBuffer;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class SerialPort_Device extends Thread {// extends Thread
		// ////////////////////////////////////thread
        private String TAG = "SerialPort_Device";
		private int threadint = 0;
		// ////////////////////////////////////
		SerialPort mSerialPort = null;
        AMBluetoothLEManager AMManager = null;

		private byte[] packetdata;
		public byte[] outpacket = null;
		ByteArrayBuffer outbuffer = null;
		private InputStream in = null;
		public OutputStream out = null;
		private DataInputStream mmDataInputStream;
		private int timerrtn = 99; //thread start value 소켓연결은 1, 종료는 99
		public int Threadcut = 0;
		private boolean socketready = false;
		private byte cmdcnt;
		private int test=0;
		Calendar cal2;
		int beforetime = 0;
		int nowtime = 0;
		int rstime = 0;
		private String mCurCode = "-1"; //20160827 tra..sh

		private int m_nRunPort = 0;
		private Handler mServerHandler;

		boolean bflag = false;

		int m_nQueuedSize = 0;
		int mBaseSize = 0;

//for meter
		private int m_nstate = 0; //미터상태. 동일상태는 1회전송함, 0 최기, 1 빈차, 2 승차, 3 지불

//for remocon
		boolean m_bEmgergency = false;

		protected void finalize()// 소멸??
		{

			Log.d("serial", "serial socket 소멸------");

		}

		public SerialPort_Device(String path, int baudrate, int iTP) {

			init(path, baudrate, iTP);

		}

		public void setManager(AMBluetoothLEManager manger)
        {

            AMManager = manger;

        }

		private void init(String path, int baudrate, int iTP) {
			Log.d("SerialPort_Device: ",  "init()");

			try{
				mSerialPort = new SerialPort(new File(path), baudrate, 0);


				try
				{

					Thread.sleep(50);

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();

				}

				out = mSerialPort.getOutputStream();
				in = mSerialPort.getInputStream();
				m_nRunPort = iTP;
				//sendData();
				Log.e("mSerialPort" , "success");

			} catch(IOException e) {
				e.printStackTrace();
			}

//20151102		if (socket == null) {
//			socket = new Socket();
//		}
			if (packetdata == null)
				packetdata = new byte[2048];
			if (outpacket == null)
				outpacket = new byte[2048];
			if(outbuffer == null)
				outbuffer = new ByteArrayBuffer(2048);

		}

		public void sendData(byte[] writeBytes) {
			try {
				if(out != null) {
					out.write(writeBytes);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void start(String sip, int nport, Handler serverHandler) {

			this.mServerHandler = serverHandler;

			if (timerrtn != 99) {

				return ;

			} else if (timerrtn == 99) {
				Log.d("serialPort_device", "start " + nport);
				timerrtn = 1; //20151102 -99;
//			Threadcut = -99;

/*20151102
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(java.lang.NullPointerException e)
			{
				util.log(e+"");
				e.printStackTrace();
			}

*/

				Threadcut = 0;
				new Thread(this).start();

				return;

			} else {

                Log.d("serial", "serial socket error------" +  + timerrtn);

			}



		}

		public void serialexit()
		{

			Threadcut = -99;
			socketready = false;

			closeport();

			for(;;)
			{

				if(timerrtn == 99)
					break;

				try
				{

					Thread.sleep(100);

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();

				} //20151102

			}

		}

		public void closeport() {


			try{

				if (out != null) {
					out.close();

					out = null;
				}

				if(in != null) //20160805 tra..sh
				{
					in.close();

					in = null;
				}

				if(mSerialPort != null)
				{

					mSerialPort.close();
					mSerialPort = null;
				}

//				socketready = false;

//20151102			socket = new Socket(); //?????????


			} catch (IOException e) {
				e.printStackTrace();
			}catch(NullPointerException e)
			{

				e.printStackTrace();

			}
		}

		public void run() {
			int ThreadcutRead = Threadcut;
			int bytesRead = 0;
			while (true) {
				try {
//					util.log(EventInfo.getNETWORK, "[socket] =============in.read socket : "
///							+ socket + " in : " + in);

					if(true) //m_nRunPort == 10002) //meter serial
					{
						if (mSerialPort != null &&  in != null) {
							bytesRead = 0;

							try {
								socketready = true;
								bytesRead = in.available();

								if (bytesRead > 0)
								{
									bytesRead = 0;

										bytesRead = in.read(outpacket);

//										bytesRead = in.read(outpacket);
//									Log.d("SerialPort " + m_nRunPort, " " + "(" + bytesRead + ")");
								}

//							util.log(EventInfo.getNETWORK, "[socket]=============in.read bytesRead : "
///									+ bytesRead);
							} catch (IOException e) {

//								Log.d(TAG, "SerialPort error" + m_nRunPort);
//20200620								break;
                    		}

							if (bytesRead > 0) {
								socketready = true;

								// 반복 횟수 초기화
								{

									setdata_queue(outpacket, bytesRead);

								}

							}
						}

					}

					Thread.sleep(300);

				} catch (InterruptedException e) {

					e.printStackTrace();

				}

				try{

					if(socketready == false)
					{
//						util.log(EventInfo.getNETWORK, "[socket]socket.isConnected() : " + socket.isConnected());
//						util.log(EventInfo.getNETWORK, "[socket]ThreadcutRead : " + ThreadcutRead);
//						util.log(EventInfo.getNETWORK, "[socket]Threadcut : " + Threadcut);
//						util.log(EventInfo.getNETWORK, "[socket] socketready == false : " + socketready);

						break;
					}
				}catch(NullPointerException e)
				{


//20200620					break;
				}
			}

			timerrtn = 99;
		}

		private void setdata_queue(byte[] outdata, int bytesRead)
		{

			byte[] bytetmp = new byte[bytesRead];
			System.arraycopy(outdata, 0, bytetmp, 0, bytesRead);

//			Log.e("SerialPort " + m_nRunPort, " " + util.bytesToHexString(bytetmp) + "(" + bytesRead + ")");

            if(m_nRunPort == 11000)
            {

                if(false) //Info.TESTMODE)
                {
                    final StringBuilder stringBuilder = new StringBuilder(bytesRead);
                    for (byte byteChar : bytetmp)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    Log.d(TAG, "------receive(" + bytesRead + ")" + stringBuilder.toString());
                }

                AMManager.checkgetData_AMBle(outdata, bytesRead);

            }
			else if(m_nRunPort == 10002) //meter serial
			{
				m_nQueuedSize = m_nQueuedSize + bytesRead;
				if( m_nQueuedSize >= 2048) //20190507
				{

					outbuffer.clear();
					m_nQueuedSize = 0;

				}

				outbuffer.append(outdata, 0, bytesRead);

				{

//					Log.d("SerialPort " + m_nRunPort, " " + util.bytesToHexString(outbuffer.toByteArray()) + "(" + outbuffer.length() + ")");
					parsing_meter(outbuffer.toByteArray(), outbuffer.length());
				}

			}

		}

		private void parsing_meter(byte[] bytetmp, int bytesRead)
		{

			for(int i = 0; i < bytesRead;) {
				if (bytetmp[i] == 0x02) {
					if(bytesRead - i >= 4)
					{
						if(bytetmp[i + 3]  == 0x5A) {
							mBaseSize = 52;
							byte[] outdata = new byte[bytesRead - i]; //20190507 error mBaseSize];

							if (bytesRead - i >= mBaseSize) {
								System.arraycopy(bytetmp, i, outdata, 0, mBaseSize);
								if (outdata[6] == '0' || outdata[6] == '4' || outdata[6] == '8')
								{
									if (outdata[5] == '2') //20190119 빈차
									{
										Log.d("SerialPort meter", "빈차"); //testcode

										if(m_nstate != 1)
											mServerHandler.sendEmptyMessage(1);
										m_nstate = 1;
									} else if (outdata[5] == '4'
											|| outdata[5] == '8' || outdata[5] == '0') //20180119 승차
									{
										Log.d("SerialPort meter", "승차"); //testcode
										if(m_nstate != 2)
											mServerHandler.sendEmptyMessage(2);
										m_nstate = 2;

									}
									else if(outdata[5] == '1') //지불.
									{
										String spayment = "";
										int npayment = 0;
										int nstime = 0;
										int netime = 0;

										if(m_nstate != 3) {
											spayment = String.format("%c%c%c%c%c%c", outdata[25], outdata[26], outdata[27], outdata[28], outdata[29], outdata[30]);
											if (!spayment.equals(""))
												npayment = npayment + Integer.parseInt(spayment);

											Bundle data = new Bundle();
											data.putInt("data", npayment);
//////////////////////////
											spayment = String.format("%c%c", outdata[17], outdata[18]);
											nstime = Integer.parseInt(spayment) * 60;
											spayment = String.format("%c%c", outdata[19], outdata[20]);
											nstime += Integer.parseInt(spayment);
											Log.d("meter시간", "s-" + nstime);

											spayment = String.format("%c%c", outdata[21], outdata[22]);
											netime = Integer.parseInt(spayment) * 60;
											spayment = String.format("%c%c", outdata[23], outdata[24]);
											netime += Integer.parseInt(spayment);
											Log.d("meter시간", "e-" + netime);

											if(nstime > netime)
												netime += 24 * 60;
											netime = netime - nstime;
											Log.d("meter시간", "t-" + netime + " " + netime / 60 + "시간" + netime % 60 + "분");
											data.putInt("etime", netime);

											spayment = String.format("%c%c%c%c%c%c", outdata[31], outdata[32], outdata[33], outdata[34], outdata[35], outdata[36]);
											Log.d("meter거리", "m" + spayment);
											data.putInt("distance", Integer.parseInt(spayment));
/////////////////////////////

											Message msg = mServerHandler.obtainMessage(3);
											msg.setData(data);
											mServerHandler.sendMessage(msg);

											m_nstate = 3;
										}
									}
								}

								i += mBaseSize;

								System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
								outbuffer.clear();

								outbuffer.append(outdata, 0, bytesRead - i);
								m_nQueuedSize = bytesRead - i;

							} else {

								System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
								outbuffer.clear();

								outbuffer.append(outdata, 0, bytesRead - i);
								m_nQueuedSize = bytesRead - i;

								return;
							}
						}
						else
						{

							i++;
							continue;
						}
					}
					else
					{

						byte[] outdata = new byte[bytesRead - i];
						System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
						outbuffer.clear();

						outbuffer.append(outdata, 0, bytesRead - i);
						m_nQueuedSize = bytesRead - i;

						return;

					}
				} else if (bytetmp[i] == (byte) 0xAA)
				{
					if(bytesRead - i >= 5) //0xAA 0xD4 0x75 size 0x70
					{
						if(bytetmp[i + 1] == (byte) 0xD4) {
							mBaseSize = bytetmp[i + 3] + 5;

							if (bytesRead - i >= mBaseSize) {
								byte[] outdata = new byte[bytesRead - i]; //20190507 error mBaseSize];
								int noffset = 0;
								{
									System.arraycopy(bytetmp, i, outdata, 0, mBaseSize);

									noffset = noffset + mBaseSize;
									if (outdata[0] == (byte) 0xAA && outdata[1] == (byte) 0xD4) {
										if (outdata[2] == (byte) 0x75 && outdata[4] == (byte) 0x70) {
											switch (outdata[5]) {
												case 0x01: //빈차.
												case 0x11: //복합빈차.
													//					case 0x10: //복합.
													//					case 0x20: //호출.
													Log.d("SerialPort meter", "빈차"); //testcode
													if (m_nstate != 1)
														mServerHandler.sendEmptyMessage(1);
													m_nstate = 1;
													break;
												case 0x02: //승차.
												case 0x04: //할증.
												case 0x06: //할증
												case 0x20: //호출. //20140610
												case 0x22: //승차
													Log.d("SerialPort meter", "승차"); //testcode
													if (m_nstate != 2)
														mServerHandler.sendEmptyMessage(2);
													m_nstate = 2;

													break;
												case 0x0C: //승차.
												case 0x2C: //승차.
												case 0x08: //지불.
												case 0x0A: //지불.
												case 0x1A: //지불.
												case 0x2A: //지불.
													String spayment = "";
													int npayment = 0;

													spayment = String.format("%02x%02x%02x", outdata[17], outdata[18], outdata[19]);
													if (!spayment.equals(""))
														npayment = Integer.parseInt(spayment);

													spayment = String.format("%02x%02x%02x", outdata[20], outdata[21], outdata[22]);
													if (!spayment.equals(""))
														npayment = npayment + Integer.parseInt(spayment);

													Bundle data = new Bundle();
													data.putInt("data", npayment);
													Message msg = mServerHandler.obtainMessage(3);
													msg.setData(data);
													mServerHandler.sendMessage(msg);

													spayment = String.format("%02x %02x %02x %02x", outdata[23], outdata[24], outdata[25],outdata[26] );
													Log.d("meter시간", "s-" + spayment);

													spayment = String.format("%02x %02x %02x %02x", outdata[27], outdata[28], outdata[29],outdata[30] );
													Log.d("meter시간", "s-" + spayment);

													break;

											} //switch
										}
									}
								}

								i += mBaseSize;

								System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
								outbuffer.clear();

								outbuffer.append(outdata, 0, bytesRead - i);
								m_nQueuedSize = bytesRead - i;

							} else {

								byte[] outdata = new byte[bytesRead - i];
								System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
								outbuffer.clear();
								outbuffer.append(outdata, 0, bytesRead - i);
								m_nQueuedSize = bytesRead - i;
								return;
							}
						}
						else {
							i++;
							continue;

						}
					}
					else {

						byte[] outdata = new byte[bytesRead - i];
						System.arraycopy(bytetmp, i, outdata, 0, bytesRead - i);
						outbuffer.clear();
						outbuffer.append(outdata, 0, bytesRead - i);
						m_nQueuedSize = bytesRead - i;

						return;
					}

				} else
					i++;
			}

			if( m_nQueuedSize >= 2048)
			{

				outbuffer.clear();
				m_nQueuedSize = 0;

			}

			return;

	};

	public void _change_baudrate(String path, int baudrate, int iTP) {

		Log.d("SerialPort_Device: ", "_change_baudrate()");

		try {

			closeport();

			mSerialPort = 	new SerialPort(new File(path), baudrate, 0);

			try
			{

				Thread.sleep(50);

			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

			out= mSerialPort.getOutputStream();
			in = mSerialPort.getInputStream();

			outbuffer.clear();
			m_nQueuedSize = 0;
			m_nRunPort = iTP;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

////////////////////////
}
