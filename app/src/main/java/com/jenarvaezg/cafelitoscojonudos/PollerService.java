package com.jenarvaezg.cafelitoscojonudos;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.jenarvaezg.cafelitoscojonudos.messages.Message;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by joseen on 28/04/16.
 */
public class PollerService extends IntentService {


    protected static String notifText = "";


    private static String id;
    private static Context ctx;
    private static View rootView;
    private static MainActivity mainAct;
    private static Boolean onlyOne = true;

    public PollerService(){
        super("EH");
    }


    protected static void setPollerVars(MainActivity act, String id){
        mainAct = act;
        PollerService.id = id;
        ctx = mainAct.getApplicationContext();
        rootView = mainAct.findViewById(android.R.id.content);
    }

    public PollerService(String name) {
        super(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("JOSE", "STARTING POLLING");
        for(;;){
            handleResponse(MessageHandler.poll(id, MainActivity.getLocation()));
            try {
                Thread.sleep(15*1000, 0);
            } catch (InterruptedException e) {
                setOnlyOne();
                return;
            }
        }
    }

    private synchronized void setOnlyOne(){
        onlyOne = true;
    }

    private synchronized boolean amIOnlyOne(){
        Boolean b = onlyOne;
        onlyOne = false;
        return b;
    }

    private void handleResponse(String response){
        if(response == null || "null".equals(response) || "".equals(response)) {
            return;
        }

        Log.d("JOSE", response);

        try {
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 400, 300, 400};

            v.vibrate(pattern, -1);
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
                    }else{
                        to = "YOU";
                    }

                    new Message(from, to, date, body, chat).addMsg(ctx, false);
                }else if("newGroup".equals(type)) {
                    JSONObject o = new JSONObject(body);
                    final String groupName = o.getString("name");
                    final String creator = o.getString("creator");
                    rootView.post(new Runnable(){
                        @Override
                        public void run() {
                            try {

                                mainAct.addContact("ÇGROUP:" + groupName);
                                mainAct.updateContactList();
                            }catch (Exception e){}
                        }
                    });
                }
                putNotification();
            }
        }catch (Exception e){
            Log.e("JOSE", e.getMessage());
        }
    }

    private void putNotification() {

        if(MainActivity.active){
            return;
        }


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Message.UnreadMessagesNumber[] unread = Message.getUnreadMessagesNumber();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
        Integer nm = 0, nc = 0;
        nc = unread.length;
        for(int i = 0; i < unread.length; i++) {
            nm += unread[i].getN();
        }
        mBuilder.setSmallIcon(R.drawable.launcher_icon)
                    .setContentTitle("Cafelitos Cojonudos")
                    .setContentText(Integer.toString(nm) + " messages from " + nc  + " chats.");
        Intent resultIntent = new Intent(mainAct, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);



// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setAutoCancel(true);
        mBuilder.setNumber(nc);
        mBuilder.setContentIntent(resultPendingIntent);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

}
