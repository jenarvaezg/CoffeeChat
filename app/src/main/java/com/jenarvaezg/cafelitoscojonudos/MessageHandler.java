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
    private static final String POLL_RESOURCE = "/refresh";

    protected static String login(String email, String password) throws Exception {
        JSONObject request = new JSONObject();
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update(salt.getBytes());
        digest.update(password.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        request.put("login", email);
        request.put("pass", h);
        request.toString();
        return POST(request.toString(), LOGIN_RESOURCE);
    }

    protected static String register(String email, String password) throws Exception {
        JSONObject request = new JSONObject();
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update(salt.getBytes());
        digest.update(password.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        request.put("login", email);
        request.put("pass", h);
        request.toString();
        return POST(request.toString(), REGISTER_RESOURCE);
    }

    public static String poll(String id) {
        return GET(id, POLL_RESOURCE);
    }

    protected static String POST(final String body, final String resource) {
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
        c.start();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }
        return sb.toString();
    }


    protected static String GET(final String id, final String resource){
        final StringBuilder sb = new StringBuilder();
        Thread c = new Thread(){
            @Override
            public void run(){
                BufferedReader rd = null;
                URL  url;
                try{
                    url = new URL(TARGET_HOST + resource + "?id=" + id);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoOutput(false);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setAllowUserInteraction(false);
                    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    conn.connect();
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
        c.start();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }
        return sb.toString();
    }

}
