package com.jenarvaezg.cafelitoscojonudos.messages;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by joseen on 6/04/16.
 */
public class Message {

    private static HashMap<String, Integer> newMsgs = new HashMap<>();
    private static final String FILE_BASE = "CHATHISTORY_";

    private String from;
    private String to;
    private long date;
    private String body;
    private String chat;


    public static class UnreadMessagesNumber{
        private String from;
        private Integer n;


        public String getFrom(){
            return this.from;
        }

        public Integer getN(){
            return this.n;
        }


        public UnreadMessagesNumber(String from, Integer n){
            this.n = n;
            this.from = from;
        }

        public String toString(){
            return Integer.toString(n) + " messages from: " + from;
        }
    }

    public Message(String from, String to, long date, String body, String chat){
        this.from = from;
        this.to = to;
        this.date = date;
        this.body = body;
        this.chat = chat;
    }

    public String toString(){
        return "From:" + from + " To: " + to + " Date: " + Long.toString(date) + " Body: " + body;
    }

    public Boolean isFromYou(){
        return "YOU".equals(this.from);
    }

    public String getGroupChatString(){
        return "FROM: " + this.from + "\n" + this.body;
    }

    public String getPrivateChatString(){
       /* Date date=new Date(this.date);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM - HH:mm:ss");
        String dateText = df2.format(date);*/
        return /*dateText + ":" + */this.body;
    }

    public Boolean isPrivateForYou(){
        Log.d("JOSE", this.toString());
        return this.to.equals("YOU") || this.from.equals("YOU");
    }

    synchronized static public boolean removeHistory(Context ctx, String chat){
        File dir = ctx.getFilesDir();
        File file = new File(dir, FILE_BASE+chat);
        return file.delete();
    }

    synchronized public void addMsg(Context ctx, Boolean fromMe){
        if(!fromMe){
            Integer nBefore = newMsgs.get(this.chat);
            if (nBefore == null){
                nBefore = 0;
            }
            newMsgs.put(this.chat, nBefore + 1);
        }
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = ctx.openFileOutput(FILE_BASE+this.chat, Context.MODE_PRIVATE|Context.MODE_APPEND);
            dos = new DataOutputStream(new BufferedOutputStream(fos));
            dos.writeLong(date);
            dos.writeUTF(this.body);
            dos.writeUTF(this.from);
            dos.writeUTF(this.to);
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
        }finally{
            try{
                if(dos != null){
                    dos.close();
                }
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public static Message[] getMessages(String chat, Context ctx, Boolean all){
        Integer nBefore = newMsgs.get(chat);
        if (!all && (nBefore == null || nBefore <= 0)){
            return null;
        }
        newMsgs.put(chat, 0);
        ArrayList<Message> messages = new ArrayList<>();
        FileInputStream fis = null;
        DataInputStream dis = null;
        String id = null;
        try {
            fis = ctx.openFileInput(FILE_BASE + chat);

            dis = new DataInputStream(new BufferedInputStream(fis));
            try {
                for (Long date = dis.readLong(); ; date = dis.readLong()) {
                    String body = dis.readUTF();
                    String from = dis.readUTF();
                    String to = dis.readUTF();
                    if(from.equals("YOU")){
                        to = chat;
                    }
                    messages.add(new Message(from, to, date, body, chat));
                }
            }catch (IOException e){;}
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
            e.printStackTrace();
        }finally{
            try{
                if(dis != null){
                    dis.close();
                }
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message[] msgs = messages.toArray(new Message[0]);
            if(all) {
                return msgs;
            }
            return Arrays.copyOfRange(msgs, msgs.length - nBefore, msgs.length);
        }
    }

    synchronized public static UnreadMessagesNumber[] getUnreadMessagesNumber(){
        ArrayList<UnreadMessagesNumber> unread = new ArrayList<>();
        String[] froms = newMsgs.keySet().toArray(new String[0]);
        for(String from: froms){
            Integer n = newMsgs.get(from);
            if(n != 0){
                unread.add(new UnreadMessagesNumber(from, n));
            }
        }
        return unread.toArray(new UnreadMessagesNumber[0]);
    }


}