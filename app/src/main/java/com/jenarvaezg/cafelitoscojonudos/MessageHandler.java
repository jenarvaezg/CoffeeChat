package com.jenarvaezg.cafelitoscojonudos;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;

/**
 * Created by jose on 3/2/16.
 */
public class MessageHandler {

    private static final String salt = "CAFELITOS";

    private static final String TARGET_HOST = "http://alpha.aulas.gsyc.es:8000";
    private static final String LOGIN_RESOURCE = "/login";
    private static final String REGISTER_RESOURCE = "/register";

    protected static String login(String email, String password) throws Exception {
        byte[] req = new byte[0];
        JSONObject request = new JSONObject();
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] output = digest.digest(password.getBytes());
        digest.update(salt.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        request.put("login", email);
        request.put("pass", h);
        request.toString();
        return POST(request.toString(), LOGIN_RESOURCE);
    }

    protected static String register(String email, String password) throws Exception {
        byte[] req = new byte[0];
        JSONObject request = new JSONObject();
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] output = digest.digest(password.getBytes());
        digest.update(salt.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        request.put("login", email);
        request.put("pass", h);
        request.toString();
        return POST(request.toString(), REGISTER_RESOURCE);
    }

    protected static String POST(final String body, final String resource){
        final StringBuilder sb = new StringBuilder();
        Thread c = new Thread(){
            @Override
            public void run(){
                OutputStreamWriter osw = null;
                BufferedReader rd = null;
                URL  url;
                try{
                    url = new URL(TARGET_HOST + resource);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setAllowUserInteraction(false);
                    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    conn.connect();
                    osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write(body);
                    osw.flush();
                    if(conn.getResponseCode() != 200){
                        throw new IOException(conn.getResponseMessage());
                    }
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    conn.disconnect();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unknown Host");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("IOException");
                } finally{
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                        if (rd != null) {
                            rd.close();
                        }
                    }catch(Exception e){
                        Log.e("JOSE", e.toString());
                        throw new RuntimeException("WOW");
                    }
                }
            }
        };
        c.run();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }
        return sb.toString();
    }

    /*protected static byte[] communicate(byte[] request, String resource)  {
        final byte[] req = request;
        final byte[] resp = new byte[1024];

        Thread c = new Thread(){
            @Override
            public void run(){
                OutputStream o = null;
                InputStream i = null;
                URL  url;
                try{
                    url = new URL("http", "alpha.aulas.gsyc.es", 8000,
                            "/services/index.html");
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    o = connection.getOutputStream();
                    i = connection.getInputStream();
                    o.write(req);
                    i.read(resp);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unknow Host");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("IOException");
                } finally{
                    try {
                        if (o != null) {
                            o.close();
                        }
                        if (i != null) {
                            i.close();
                        }
                    }catch(Exception e){
                        Log.e("JOSE", e.toString());
                        throw new RuntimeException("WOW");

                    }
                }
            }
        };
        c.run();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }

        return resp;
    }*/
}
