package com.jenarvaezg.cafelitoscojonudos;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jenarvaezg.cafelitoscojonudos.messages.Message;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by joseen on 10/03/16.
 */
public class PollerThread extends Thread{


    private String id;
    private Context ctx;
    private View rootView;
    private MainActivity mainAct;
    private static Boolean onlyOne = true;

    public PollerThread(String id, Context ctx, View rootView, MainActivity mainAct){
        this.id = id;
        this.ctx = ctx;
        this.mainAct = mainAct;
        this.rootView = rootView;
    }

    private synchronized void setOnlyOne(){
        onlyOne = true;
    }

    private synchronized boolean amIOnlyOne(){
        Boolean b = onlyOne;
        onlyOne = false;
        return b;
    }

    @Override
    public void run() {
        if(!amIOnlyOne())
            return;
        for(;;){
            try {
                Thread.sleep(2000, 0);
            } catch (InterruptedException e) {
                setOnlyOne();
                break;
            }
            String response = MessageHandler.poll(id);
            if(response != null && !"null".equals(response) && !"".equals(response)) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        String type = c.getString("type");
                        final String body = c.getString("body");
                        if("msg".equals(type)) {
                            String from = c.getString("from");
                            String to = c.getString("to");
                            Long date = c.getLong("date");
                            String chat = from;
                            if(!to.equals(id)){
                                chat = to;
                                to = "YOU";
                            }
                            new Message(from, to, date, body, chat).addMsg(ctx, false);
                        }else if("newGroup".equals(type)) {
                            rootView.post(new Runnable(){
                                @Override
                                public void run() {
                                    try {
                                        JSONObject c = new JSONObject(body);
                                        mainAct.addContact("ÇGROUP:" + c.getString("name"));
                                        mainAct.updateContactList();
                                    }catch (Exception e){}
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Log.e("JOSE", e.getMessage());

                }

            }

        }
    }
}
