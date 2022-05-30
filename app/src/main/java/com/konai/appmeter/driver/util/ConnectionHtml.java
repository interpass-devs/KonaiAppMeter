package com.konai.appmeter.driver.util;

import android.os.AsyncTask;

import com.konai.appmeter.driver.setting.Info;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class ConnectionHtml extends AsyncTask<String, Void, String> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
//String downsuburbs = new ConnectionHtml().execute(finalstruct.SUBBURBS, "suburb", "suburb" + info.getCentercode() + ".txt").get();

        StringBuilder jsonHtml = new StringBuilder();
        HttpURLConnection conn = null;
        int paramsCnt = params.length;

        String addr = params[0];
        String div = params[1];

        if (div.equals("file")) { //20210728

            String path = params[2];
            String filename = params[3];

            String urlPath = addr;
            urlPath = urlPath;

            try {

                URL url;

                url = new URL(urlPath);

                URLConnection conns = url.openConnection();
                conns.connect();
                InputStream is = conns.getInputStream();

                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                //We create an array of bytes
                byte[] data = new byte[50];
                int current = 0;

                while((current = bis.read(data,0,data.length)) != -1){
                    buffer.write(data,0,current);
                }

                File saveFile = null;
//20220503 tra..sh                if( Build.VERSION.SDK_INT < 29) saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
///                else saveFile = Info.gMainactivity.getExternalFilesDir("/" + path);

                saveFile = Info.gMainactivity.getExternalFilesDir("/" + path);

                try {
                    FileOutputStream fos = new FileOutputStream(saveFile + "/" + filename);
                    fos.write(buffer.toByteArray());
                    fos.flush();
                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                return filename;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "false";
            } catch (IOException e) {
                e.printStackTrace();
                return "false";
            }
        } else {

            try {
                URL url = new URL(addr);
                if (url.getProtocol().toLowerCase().equals("https_")) {

                    conn = getConnection(addr);

                } else {

                    conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        conn.connect();
                    }
                }

                if (conn != null) {

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Read Data From Web
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(),
                                        "UTF-8"));

                        for (; ; ) {

                            String line = br.readLine();
                            if (line == null)
                                break;

                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                } else {

                    return "false";
                }

                return jsonHtml.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "false";
            }
        }

    }

    private HttpsURLConnection getConnection(String url) throws MalformedURLException {
        URL request_url = new URL(url);
        HttpsURLConnection urlConnection = null;

        try {

            urlConnection = (HttpsURLConnection) request_url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("X-Environment", "android");

/*
				String cookie = cookieManager.getCookie(urlConnection.getURL().toString());
				cookieManager = CookieManager.getInstance();
				if (cookie != null)
					urlConnection.setRequestProperty("Cookie", cookie);

				List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
				if (cookieList != null) {
					for (String cookieTemp : cookieList) {
						cookieManager.setCookie(urlConnection.getURL().toString(), cookieTemp);
					}
				}
*/
            urlConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    /** if it necessarry get url verfication */
                    //return HttpsURLConnection.getDefaultHostnameVerifier().verify("your_domain.com", session);
                    return true;
                }
            });
            urlConnection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());


            urlConnection.connect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlConnection;
    }

}
