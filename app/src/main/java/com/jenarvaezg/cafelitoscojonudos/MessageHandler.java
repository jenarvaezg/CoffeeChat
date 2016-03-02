package com.jenarvaezg.cafelitoscojonudos;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by jose on 3/2/16.
 */
public class MessageHandler {

    protected static byte[] communicate(byte[] request)  {
        final byte[] req = request;
        final byte[] resp = new byte[1024];
        Thread c = new Thread(){
            @Override
            public void run(){
                Socket s;
                OutputStream o = null;
                InputStream i = null;
                try{
                    s = new Socket("alpha.aulas.gsyc.es", 8000);
                    o = s.getOutputStream();
                    i = s.getInputStream();
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
    }
}
