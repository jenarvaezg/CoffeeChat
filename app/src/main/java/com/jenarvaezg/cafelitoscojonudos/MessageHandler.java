package com.jenarvaezg.cafelitoscojonudos;

import android.location.Location;
import android.net.wifi.WifiManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
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
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.lang.Thread;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by jose on 3/2/16.
 */
public class MessageHandler {

    private static final String salt = "CAFELITOS";

    private static final String TARGET_HOST = "http://beta.aulas.gsyc.es:8000";
    private static final String LOGIN_RESOURCE = "/login";
    private static final String REGISTER_RESOURCE = "/register";
    private static final String POLL_RESOURCE = "/refresh";
    private static final String SEND_MESSAGE_RESOURCE = "/message";
    private static final String ADD_USER_RESOURCE = "/add_user";
    private static final String CREATE_GROUP_RESOURCE = "/create_group";
    private static final String GET_OFFICE_RESOURCE = "/get_office";

    private static boolean exceptioned = false;

    static Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            System.out.println("Uncaught exception: " + ex);
            exceptioned = true;
        }
    };

    protected static String login(String email, String password){
        JSONObject request = new JSONObject();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.update(salt.getBytes());
        digest.update(password.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        try {
            request.put("login", email);
            request.put("pass", h);
        } catch (JSONException e) {
            return null;
        }
        POST(request.toString(), LOGIN_RESOURCE);
        if(exceptioned) {
            exceptioned = false;
            return null;
        }
        return email;
    }

    protected static String register(String email, String password){
        JSONObject request = new JSONObject();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.update(salt.getBytes());
        digest.update(password.getBytes());
        String h = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        try {
            request.put("login", email);
            request.put("pass", h);
        } catch (JSONException e) {
            return null;
        }
        POST(request.toString(), REGISTER_RESOURCE);
        if(exceptioned){
            exceptioned = false;
            return null;
        }
        return email;
    }

    public static int createGroup(String myID, String groupName, String[] contacts) {
        JSONObject request = new JSONObject();
        try {
            request.put("name", groupName);
            request.put("creator", myID);
            JSONArray users = new JSONArray();
            for(String user: contacts){
                users.put(user);
            }
            request.put("users", users);

        }catch (JSONException e){
            return -1;
        }
        String response = POST(request.toString(), CREATE_GROUP_RESOURCE);
        if(exceptioned){
            exceptioned = false;
            return -2;
        }
        return 0;
    }

    public static String poll(String id, Location location) {

        String response = GET(id, location, POLL_RESOURCE);
        if(exceptioned){
            exceptioned = false;
            return null;
        }
        return response;
    }

    protected static String sendMessage(String message, String to, String from){
        JSONObject request = new JSONObject();
        String response;
        Log.d("JOSE", "FROM: " + from + " TO:  " + to);
        try {
            request.put("body", message);
            request.put("from", from);
            request.put("date", System.currentTimeMillis());
            request.put("to", to);
            response = POST(request.toString(), SEND_MESSAGE_RESOURCE);
        } catch (JSONException e) {
            return null;
        }
        if(exceptioned){
            exceptioned = false;
            return null;
        }
        return response;
    }

    protected static Boolean addUserAsFriend(String userToAdd, String me){
        JSONObject request = new JSONObject();
        try {
            request.put("userToAdd", userToAdd);
            request.put("userAdding", me);
        } catch (JSONException e) {
            return false;
        }
        Log.d("JOSE", request.toString());
        String response = POST(request.toString(), ADD_USER_RESOURCE);
        if(exceptioned){
            exceptioned = false;
            return false;
        }
        return response.equals("OK");
    }

    protected static Float[] getOffice(String me){
        String response = GET(me, null, GET_OFFICE_RESOURCE);
        if(exceptioned){
            exceptioned = false;
            return null;
        }
        String[] spl = response.split("&");
        if(spl.length != 3){
            Log.d("JOSE", Arrays.toString(spl) + " len: " + Integer.toString(spl.length));
            return new Float[]{-1f, -1f, -1f};
        }
        Float[] values = new Float[3];
        for(int i = 0; i < values.length; i++){
            values[i] = Float.parseFloat(spl[i]);
        }
        return values;
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
        c.setUncaughtExceptionHandler(h);
        c.start();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }
        return sb.toString();
    }


    protected static String GET(final String id, final Location location, final String resource){
        final StringBuilder sb = new StringBuilder();
        Thread c = new Thread(){
            @Override
            public void run(){
                BufferedReader rd = null;
                URL  url;
                try{
                    String urlString = TARGET_HOST + resource + "?id=" + id;
                    if(location != null){
                        urlString += "&lat=" + Double.toString(location.getLatitude());
                        urlString += "&long=" + Double.toString(location.getLongitude());
                    }
                    url = new URL(urlString);
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
        c.setUncaughtExceptionHandler(h);
        c.start();
        try {
            c.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Timeout");
        }
        return sb.toString();
    }


}
