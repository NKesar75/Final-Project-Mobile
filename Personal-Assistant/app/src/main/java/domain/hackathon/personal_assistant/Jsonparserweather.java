package domain.hackathon.personal_assistant;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by darkness7245 on 3/29/2018.
 */

public class Jsonparserweather {
    private static final String TAG = Jsonparserweather.class.getSimpleName();
    public static String response;
    public static boolean isdoneconn = false;

    public Jsonparserweather() {
    }

    public void makeServiceCall(final String reqUrl) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(reqUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //InputStream conn =  url.openStream();

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    //conn.setUseCaches(false);
                    //conn.setAllowUserInteraction(false);
                    //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    // read the response
                    InputStream in = conn.getInputStream();
                    response = convertStreamToString(in);
                    //String line;
                    //BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    //while ((line = br.readLine()) != null)
                    //{
                    //    response = "";
                    //    response += line;
                    //    Log.i("response_line", response);
                    //}
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }
                isdoneconn = true;
            }
        });
        thread.start();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
